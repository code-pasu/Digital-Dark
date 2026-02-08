/*
 * Copyright (c) 2024
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.LibraryNode;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search text field with Google-style autocomplete dropdown.
 * Shows matching components as the user types. Click a result to insert it into the circuit.
 */
public class AutocompleteSearchField extends JPanel {
    private static final int MAX_RESULTS = 12;
    private static final int CELL_HEIGHT = 34;

    private final JTextField textField;
    private JWindow popupWindow;
    private final JList<LibraryNode> resultList;
    private final DefaultListModel<LibraryNode> listModel;
    private final List<LibraryNode> allLeafNodes;
    private final Map<LibraryNode, String> categoryMap;
    private final CircuitComponent circuitComponent;
    private final ShapeFactory shapeFactory;
    private final InsertHistory insertHistory;

    /**
     * Creates a new autocomplete search field.
     *
     * @param library       the element library
     * @param component     the circuit component for inserting elements
     * @param shapeFactory  the shape factory
     * @param insertHistory the insert history
     */
    public AutocompleteSearchField(ElementLibrary library, CircuitComponent component,
                                   ShapeFactory shapeFactory, InsertHistory insertHistory) {
        super(new BorderLayout());
        this.circuitComponent = component;
        this.shapeFactory = shapeFactory;
        this.insertHistory = insertHistory;

        // Collect all leaf nodes from the library tree
        allLeafNodes = new ArrayList<>();
        categoryMap = new HashMap<>();
        collectLeafNodes(library.getRoot(), allLeafNodes, categoryMap, "");

        // --- Text field with placeholder that stays visible while focused ---
        textField = new JTextField() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(130, 130, 140));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    String hint = "\uD83D\uDD0D " + Lang.get("msg_search") + "...  (type to find components)";
                    g2.drawString(hint, 6, (getHeight() + getFont().getSize()) / 2 - 1);
                }
            }
        };
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(80, 85, 95)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        textField.setFont(textField.getFont().deriveFont(13f));

        // --- Clear button ---
        JButton clearButton = new JButton("\u2717");
        clearButton.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        clearButton.setFocusable(false);
        clearButton.addActionListener(e -> {
            textField.setText("");
            hidePopup();
        });

        add(textField, BorderLayout.CENTER);
        add(clearButton, BorderLayout.EAST);

        // --- Popup dropdown with result list ---
        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        resultList.setCellRenderer(new AutocompleteRenderer());
        resultList.setFixedCellHeight(CELL_HEIGHT);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setFocusable(false);

        // --- Text change listener for autocomplete ---
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onTextChanged();
            }
        });

        // --- Keyboard navigation in text field ---
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!isPopupVisible()) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN && listModel.getSize() > 0) {
                        showPopup();
                        resultList.setSelectedIndex(0);
                        e.consume();
                    }
                    return;
                }
                int selected = resultList.getSelectedIndex();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        if (selected < listModel.getSize() - 1) {
                            resultList.setSelectedIndex(selected + 1);
                            resultList.ensureIndexIsVisible(selected + 1);
                        }
                        e.consume();
                        break;
                    case KeyEvent.VK_UP:
                        if (selected > 0) {
                            resultList.setSelectedIndex(selected - 1);
                            resultList.ensureIndexIsVisible(selected - 1);
                        } else {
                            resultList.clearSelection();
                        }
                        e.consume();
                        break;
                    case KeyEvent.VK_ENTER:
                        if (selected >= 0) {
                            insertComponent(listModel.getElementAt(selected));
                            hidePopup();
                            textField.setText("");
                        }
                        e.consume();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        hidePopup();
                        e.consume();
                        break;
                    default:
                        break;
                }
            }
        });

        // --- Click to insert from result list ---
        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = resultList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    insertComponent(listModel.getElementAt(index));
                    hidePopup();
                    textField.setText("");
                }
            }
        });

        // --- Hover highlighting ---
        resultList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = resultList.locationToIndex(e.getPoint());
                if (index >= 0 && index != resultList.getSelectedIndex()) {
                    resultList.setSelectedIndex(index);
                }
            }
        });

        // --- Hide popup when text field loses focus ---
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Delay hiding to allow click on popup items
                Timer timer = new Timer(200, evt -> {
                    if (!textField.hasFocus()) {
                        hidePopup();
                    }
                });
                timer.setRepeats(false);
                timer.start();
                repaint();
            }

            @Override
            public void focusGained(FocusEvent e) {
                String text = textField.getText().trim();
                if (!text.isEmpty() && listModel.getSize() > 0) {
                    showPopup();
                }
                repaint();
            }
        });
    }

    /**
     * Recursively collect all leaf (component) nodes from the library tree.
     */
    private void collectLeafNodes(LibraryNode node, List<LibraryNode> result,
                                  Map<LibraryNode, String> categories, String parentCategory) {
        if (node.isLeaf()) {
            if (!node.isHidden() && node.isUnique()) {
                result.add(node);
                categories.put(node, parentCategory);
            }
        } else {
            String myName = node.getTranslatedName();
            for (LibraryNode child : node) {
                if (!child.isHidden()) {
                    collectLeafNodes(child, result, categories, myName);
                }
            }
        }
    }

    /**
     * Called when the text changes — filters components and shows/hides the dropdown.
     */
    private void onTextChanged() {
        String text = textField.getText().trim().toLowerCase();
        listModel.clear();

        if (text.isEmpty()) {
            hidePopup();
            return;
        }

        int count = 0;

        // First pass: names that START with the search text (higher priority)
        for (LibraryNode node : allLeafNodes) {
            if (count >= MAX_RESULTS) break;
            String name = node.getTranslatedName().toLowerCase();
            String id = node.getName().toLowerCase();
            if (name.startsWith(text) || id.startsWith(text)) {
                listModel.addElement(node);
                count++;
            }
        }

        // Second pass: names that CONTAIN the search text but don't start with it
        for (LibraryNode node : allLeafNodes) {
            if (count >= MAX_RESULTS) break;
            String name = node.getTranslatedName().toLowerCase();
            String id = node.getName().toLowerCase();
            if ((name.contains(text) || id.contains(text))
                    && !name.startsWith(text) && !id.startsWith(text)) {
                listModel.addElement(node);
                count++;
            }
        }

        if (listModel.getSize() > 0) {
            resultList.setSelectedIndex(0);
            showPopup();
        } else {
            hidePopup();
        }
    }

    private boolean isPopupVisible() {
        return popupWindow != null && popupWindow.isVisible();
    }

    private void showPopup() {
        if (!textField.isShowing()) return;

        Window ancestor = SwingUtilities.getWindowAncestor(textField);
        if (ancestor == null) return;

        // Create popup window on demand, attached to parent window
        if (popupWindow == null) {
            popupWindow = new JWindow(ancestor);
            popupWindow.setFocusableWindowState(false);  // key: do NOT steal focus or consume clicks
            popupWindow.setAlwaysOnTop(true);

            JScrollPane scrollPane = new JScrollPane(resultList);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            Color borderColor = UIManager.getColor("controlShadow");
            if (borderColor == null) borderColor = new Color(80, 80, 90);
            scrollPane.setBorder(BorderFactory.createLineBorder(borderColor));
            popupWindow.setContentPane(scrollPane);

            // Hide popup when parent window moves or deactivates
            ancestor.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent e) { hidePopup(); }
                @Override
                public void componentResized(ComponentEvent e) { hidePopup(); }
            });
            ((Window) ancestor).addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeactivated(WindowEvent e) { hidePopup(); }
                @Override
                public void windowIconified(WindowEvent e) { hidePopup(); }
            });
        }

        // Position below the text field
        Point loc = textField.getLocationOnScreen();
        int width = Math.max(textField.getWidth() + 30, 260);
        int visibleCount = Math.min(listModel.getSize(), MAX_RESULTS);
        int height = visibleCount * CELL_HEIGHT + 4;
        popupWindow.setBounds(loc.x - 4, loc.y + textField.getHeight() + 2, width, height);
        popupWindow.setVisible(true);
    }

    private void hidePopup() {
        if (popupWindow != null && popupWindow.isVisible()) {
            popupWindow.setVisible(false);
        }
    }

    /**
     * Insert the selected component into the circuit (same mechanism as SelectTree).
     */
    private void insertComponent(LibraryNode node) {
        if (node.isLeaf() && node.isUnique()) {
            try {
                ElementTypeDescription d = node.getDescription();
                VisualElement element = node.setWideShapeFlagTo(
                        new VisualElement(d.getName()).setShapeFactory(shapeFactory));
                circuitComponent.setPartToInsert(element);
                insertHistory.add(new InsertAction(node, insertHistory, circuitComponent, shapeFactory));
            } catch (IOException e) {
                SwingUtilities.invokeLater(new ErrorMessage(
                        Lang.get("msg_errorImportingModel_N0", node.getName())).addCause(e));
            }
        }
    }

    /**
     * @return the text field, for attaching external listeners (e.g. tree filtering)
     */
    public JTextField getTextField() {
        return textField;
    }

    private static Icon scaleIcon(Icon icon, int size) {
        if (icon == null) return null;
        if (icon instanceof ImageIcon) {
            Image img = ((ImageIcon) icon).getImage();
            Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(
                icon.getIconWidth(), icon.getIconHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        icon.paintIcon(null, g2, 0, 0);
        g2.dispose();
        Image scaled = bi.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * Custom list cell renderer for autocomplete dropdown results.
     * Shows component name (bold), category (dimmer), and icon if available.
     */
    private class AutocompleteRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            LibraryNode node = (LibraryNode) value;

            JPanel panel = new JPanel(new BorderLayout(6, 0));
            panel.setBorder(new EmptyBorder(4, 10, 4, 10));

            // Component name
            JLabel nameLabel = new JLabel(node.getTranslatedName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));

            // Category label
            String category = categoryMap.getOrDefault(node, "");
            JLabel catLabel = new JLabel(category);
            catLabel.setFont(catLabel.getFont().deriveFont(Font.PLAIN, 11f));

            // Try to show component icon — scaled to 24px
            Icon icon = scaleIcon(node.getIconOrNull(shapeFactory), 24);
            if (icon != null) {
                nameLabel.setIcon(icon);
                nameLabel.setIconTextGap(8);
            }

            panel.add(nameLabel, BorderLayout.CENTER);
            panel.add(catLabel, BorderLayout.EAST);

            Color selBg = UIManager.getColor("List.selectionBackground");
            Color selFg = UIManager.getColor("List.selectionForeground");
            Color bg = UIManager.getColor("List.background");
            Color fg = UIManager.getColor("List.foreground");

            if (isSelected) {
                panel.setBackground(selBg != null ? selBg : new Color(60, 80, 120));
                nameLabel.setForeground(selFg != null ? selFg : Color.WHITE);
                catLabel.setForeground(selFg != null ? selFg.darker() : new Color(180, 180, 195));
            } else {
                panel.setBackground(bg != null ? bg : new Color(50, 52, 58));
                nameLabel.setForeground(fg != null ? fg : new Color(220, 220, 225));
                catLabel.setForeground(new Color(130, 130, 145));
            }
            panel.setOpaque(true);

            return panel;
        }
    }
}
