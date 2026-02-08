/*
 * Copyright (c) 2024
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.Vector;
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
 * A spotlight-style floating search dialog for quickly finding and inserting components.
 * Triggered by F2 or a toolbar button. The selected component attaches to the cursor
 * for placement, exactly like clicking from the component tree.
 */
public class ComponentSearchDialog extends JDialog {
    private static final int MAX_RESULTS = 10;
    private static final int DIALOG_WIDTH = 480;
    private static final int ROW_HEIGHT = 38;

    private final JTextField searchField;
    private final JList<LibraryNode> resultList;
    private final DefaultListModel<LibraryNode> listModel;
    private final List<LibraryNode> allLeafNodes;
    private final Map<LibraryNode, String> categoryMap;
    private final CircuitComponent circuitComponent;
    private final ShapeFactory shapeFactory;
    private final InsertHistory insertHistory;

    /**
     * Creates the search dialog.
     *
     * @param owner         parent frame
     * @param library       the element library
     * @param component     the circuit component for inserting elements
     * @param shapeFactory  the shape factory
     * @param insertHistory the insert history
     */
    public ComponentSearchDialog(Frame owner, ElementLibrary library, CircuitComponent component,
                                 ShapeFactory shapeFactory, InsertHistory insertHistory) {
        super(owner, false); // non-modal so circuit interaction works after placement
        setUndecorated(true);
        this.circuitComponent = component;
        this.shapeFactory = shapeFactory;
        this.insertHistory = insertHistory;

        // Collect all leaf nodes
        allLeafNodes = new ArrayList<>();
        categoryMap = new HashMap<>();
        collectLeafNodes(library.getRoot(), allLeafNodes, categoryMap, "");

        // --- Root panel with rounded border ---
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 75, 85), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        root.setBackground(new Color(42, 44, 50));

        // --- Search field ---
        searchField = new JTextField() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(120, 120, 130));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(Lang.get("msg_search") + " components...", 30, (getHeight() + getFont().getSize()) / 2 - 1);
                }
            }
        };
        searchField.setFont(searchField.getFont().deriveFont(16f));
        searchField.setBackground(new Color(52, 54, 62));
        searchField.setForeground(new Color(220, 220, 225));
        searchField.setCaretColor(new Color(220, 220, 225));
        searchField.setBorder(BorderFactory.createEmptyBorder(12, 34, 12, 12));

        // Search icon overlay
        JPanel searchPanel = new JPanel(new BorderLayout()) {
            @Override
            public void paintChildren(Graphics g) {
                super.paintChildren(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(new Color(140, 140, 150));
                g2.setFont(g2.getFont().deriveFont(16f));
                g2.drawString("\uD83D\uDD0D", 10, searchField.getHeight() / 2 + 6);
            }
        };
        searchPanel.setOpaque(false);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // F2 label hint
        JLabel shortcutHint = new JLabel("F2");
        shortcutHint.setFont(shortcutHint.getFont().deriveFont(Font.PLAIN, 10f));
        shortcutHint.setForeground(new Color(100, 100, 110));
        shortcutHint.setBorder(new EmptyBorder(0, 0, 0, 10));
        searchPanel.add(shortcutHint, BorderLayout.EAST);

        root.add(searchPanel, BorderLayout.NORTH);

        // --- Result list ---
        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        resultList.setCellRenderer(new ResultRenderer());
        resultList.setFixedCellHeight(ROW_HEIGHT);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultList.setBackground(new Color(42, 44, 50));
        resultList.setFocusable(false);

        JScrollPane scrollPane = new JScrollPane(resultList);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 62, 70)));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(new Color(42, 44, 50));
        root.add(scrollPane, BorderLayout.CENTER);

        setContentPane(root);

        // --- Text change → update results ---
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateResults(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateResults(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateResults(); }
        });

        // --- Keyboard: arrow keys, enter, escape ---
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int selected = resultList.getSelectedIndex();
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        if (listModel.getSize() > 0) {
                            int next = Math.min(selected + 1, listModel.getSize() - 1);
                            resultList.setSelectedIndex(next);
                            resultList.ensureIndexIsVisible(next);
                        }
                        e.consume();
                        break;
                    case KeyEvent.VK_UP:
                        if (selected > 0) {
                            resultList.setSelectedIndex(selected - 1);
                            resultList.ensureIndexIsVisible(selected - 1);
                        }
                        e.consume();
                        break;
                    case KeyEvent.VK_ENTER:
                        if (selected >= 0) {
                            insertAndClose(listModel.getElementAt(selected));
                        }
                        e.consume();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        closeDialog();
                        e.consume();
                        break;
                    default:
                        break;
                }
            }
        });

        // --- Click to insert ---
        resultList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = resultList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    insertAndClose(listModel.getElementAt(index));
                }
            }
        });

        // --- Hover to highlight ---
        resultList.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = resultList.locationToIndex(e.getPoint());
                if (index >= 0 && index != resultList.getSelectedIndex()) {
                    resultList.setSelectedIndex(index);
                }
            }
        });

        // --- Close when clicking outside ---
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                closeDialog();
            }
        });

        // Escape via input map as backup
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeDialog();
            }
        });
    }

    /**
     * Opens and positions the dialog centered over the parent frame.
     */
    public void showDialog() {
        searchField.setText("");
        listModel.clear();

        // Show with some initial results (all components, limited)
        populateAll();

        // Size: width fixed, height based on results
        int height = 52 + Math.min(listModel.getSize(), MAX_RESULTS) * ROW_HEIGHT + 6;
        setSize(DIALOG_WIDTH, height);

        // Center over parent
        Window parent = getOwner();
        if (parent != null && parent.isShowing()) {
            int x = parent.getX() + (parent.getWidth() - DIALOG_WIDTH) / 2;
            int y = parent.getY() + 80; // near top of window, like spotlight
            setLocation(x, y);
        }

        setVisible(true);
        searchField.requestFocusInWindow();
    }

    private void closeDialog() {
        setVisible(false);
    }

    /**
     * Show a limited set of all components when the field is empty.
     */
    private void populateAll() {
        listModel.clear();
        int count = 0;
        for (LibraryNode node : allLeafNodes) {
            if (count >= MAX_RESULTS) break;
            listModel.addElement(node);
            count++;
        }
        if (listModel.getSize() > 0) {
            resultList.setSelectedIndex(0);
        }
        resizeToFit();
    }

    private void updateResults() {
        String text = searchField.getText().trim().toLowerCase();
        listModel.clear();

        if (text.isEmpty()) {
            populateAll();
            return;
        }

        int count = 0;

        // Priority 1: starts with
        for (LibraryNode node : allLeafNodes) {
            if (count >= MAX_RESULTS) break;
            String name = node.getTranslatedName().toLowerCase();
            String id = node.getName().toLowerCase();
            if (name.startsWith(text) || id.startsWith(text)) {
                listModel.addElement(node);
                count++;
            }
        }

        // Priority 2: contains
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
        }
        resizeToFit();
    }

    private void resizeToFit() {
        int rows = Math.min(listModel.getSize(), MAX_RESULTS);
        int height = 52 + rows * ROW_HEIGHT + 6;
        if (rows == 0) height = 52 + ROW_HEIGHT; // show at least a "no results" size
        setSize(DIALOG_WIDTH, height);
    }

    /**
     * Insert the component and close the dialog. The component attaches to cursor.
     */
    private void insertAndClose(LibraryNode node) {
        if (node.isLeaf() && node.isUnique()) {
            try {
                ElementTypeDescription d = node.getDescription();
                VisualElement element = node.setWideShapeFlagTo(
                        new VisualElement(d.getName())
                                .setPos(new Vector(10, 10))
                                .setShapeFactory(shapeFactory));
                circuitComponent.setPartToInsert(element);
                insertHistory.add(new InsertAction(node, insertHistory, circuitComponent, shapeFactory));
            } catch (IOException ex) {
                SwingUtilities.invokeLater(new ErrorMessage(
                        Lang.get("msg_errorImportingModel_N0", node.getName())).addCause(ex));
            }
        }
        closeDialog();
    }

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

    private static Icon scaleIcon(Icon icon, int size) {
        if (icon == null) return null;
        if (icon instanceof ImageIcon) {
            Image img = ((ImageIcon) icon).getImage();
            Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        // For non-ImageIcon, paint into a BufferedImage
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(
                icon.getIconWidth(), icon.getIconHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        icon.paintIcon(null, g2, 0, 0);
        g2.dispose();
        Image scaled = bi.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    /**
     * Custom renderer for search results — icon, bold name, category.
     */
    private class ResultRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            LibraryNode node = (LibraryNode) value;

            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setBorder(new EmptyBorder(5, 12, 5, 12));
            panel.setOpaque(true);

            // Icon — scaled to 24px
            Icon icon = scaleIcon(node.getIconOrNull(shapeFactory), 24);

            // Name label
            JLabel nameLabel = new JLabel(node.getTranslatedName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 13f));
            if (icon != null) {
                nameLabel.setIcon(icon);
                nameLabel.setIconTextGap(10);
            }

            // Category
            String cat = categoryMap.getOrDefault(node, "");
            JLabel catLabel = new JLabel(cat);
            catLabel.setFont(catLabel.getFont().deriveFont(Font.PLAIN, 11f));

            panel.add(nameLabel, BorderLayout.CENTER);
            panel.add(catLabel, BorderLayout.EAST);

            if (isSelected) {
                panel.setBackground(new Color(55, 90, 140));
                nameLabel.setForeground(Color.WHITE);
                catLabel.setForeground(new Color(180, 195, 220));
            } else {
                panel.setBackground(new Color(42, 44, 50));
                nameLabel.setForeground(new Color(210, 212, 218));
                catLabel.setForeground(new Color(110, 112, 120));
            }

            return panel;
        }
    }
}
