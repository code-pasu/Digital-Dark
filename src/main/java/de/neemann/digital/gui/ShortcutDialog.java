/*
 * Copyright (c) 2024 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.gui.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Dialog for viewing and editing keyboard shortcuts.
 */
public class ShortcutDialog extends JDialog {

    private static final String SHORTCUTS_FILE = ".digital-shortcuts.cfg";
    private static final Map<String, String> customShortcuts = new LinkedHashMap<>();
    private static boolean loaded = false;

    private final List<ShortcutEntry> entries;
    private final ShortcutTableModel tableModel;
    private final JTable table;
    private boolean modified = false;

    /**
     * A single shortcut entry
     */
    public static class ShortcutEntry {
        private final String actionId;
        private final String description;
        private final String defaultShortcut;
        private String currentShortcut;

        /**
         * Creates a new entry
         *
         * @param actionId        unique id
         * @param description     human-readable description
         * @param defaultShortcut default key binding
         */
        public ShortcutEntry(String actionId, String description, String defaultShortcut) {
            this.actionId = actionId;
            this.description = description;
            this.defaultShortcut = defaultShortcut;
            this.currentShortcut = customShortcuts.getOrDefault(actionId, defaultShortcut);
        }

        /**
         * @return the action id
         */
        public String getActionId() {
            return actionId;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @return the current shortcut
         */
        public String getCurrentShortcut() {
            return currentShortcut;
        }
    }

    /**
     * Creates the shortcut dialog
     *
     * @param parent the parent frame
     */
    public ShortcutDialog(JFrame parent) {
        super(parent, "Keyboard Shortcuts", true);
        loadShortcuts();

        entries = createDefaultEntries();
        tableModel = new ShortcutTableModel();
        table = new JTable(tableModel);

        setupUI();
        setSize(650, 550);
        setLocationRelativeTo(parent);
    }

    private void setupUI() {
        boolean dark = ThemeManager.isDarkMode();
        Color bg = dark ? new Color(42, 44, 50) : Color.WHITE;
        Color fg = dark ? new Color(220, 220, 225) : Color.BLACK;
        Color headerBg = dark ? new Color(55, 57, 63) : new Color(230, 230, 230);
        Color selBg = dark ? new Color(70, 100, 160) : new Color(184, 207, 229);
        Color gridColor = dark ? new Color(65, 67, 73) : new Color(210, 210, 210);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 8));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(bg);

        // Header label
        JLabel headerLabel = new JLabel("Configure keyboard shortcuts for actions:");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 13f));
        headerLabel.setForeground(fg);
        headerLabel.setBorder(new EmptyBorder(0, 0, 6, 0));
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Table setup
        table.setRowHeight(32);
        table.setBackground(bg);
        table.setForeground(fg);
        table.setGridColor(gridColor);
        table.setSelectionBackground(selBg);
        table.setSelectionForeground(fg);
        table.setFont(table.getFont().deriveFont(12f));
        table.getTableHeader().setBackground(headerBg);
        table.getTableHeader().setForeground(fg);
        table.getTableHeader().setFont(table.getFont().deriveFont(Font.BOLD, 12f));
        table.setShowGrid(true);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(350);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);

        // Custom renderer for shortcut column
        table.getColumnModel().getColumn(1).setCellRenderer(new ShortcutCellRenderer(dark));
        table.getColumnModel().getColumn(0).setCellRenderer(new DescriptionCellRenderer(dark));

        // Custom editor for shortcut column
        table.getColumnModel().getColumn(1).setCellEditor(new ShortcutCellEditor());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(gridColor));
        scrollPane.getViewport().setBackground(bg);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setBackground(bg);

        JButton resetBtn = new JButton("Reset All");
        resetBtn.setToolTipText("Reset all shortcuts to defaults");
        resetBtn.addActionListener(e -> resetAllDefaults());

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> {
            if (modified)
                saveShortcuts();
            dispose();
        });

        buttonPanel.add(resetBtn);
        buttonPanel.add(closeBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void resetAllDefaults() {
        for (ShortcutEntry entry : entries) {
            entry.currentShortcut = entry.defaultShortcut;
        }
        customShortcuts.clear();
        modified = true;
        tableModel.fireTableDataChanged();
        saveShortcuts();
        JOptionPane.showMessageDialog(this,
                "All shortcuts reset to defaults.\nRestart the application for changes to take effect.",
                "Shortcuts Reset", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<ShortcutEntry> createDefaultEntries() {
        List<ShortcutEntry> list = new ArrayList<>();

        // File
        list.add(new ShortcutEntry("file.new", "New File", "Ctrl+N"));
        list.add(new ShortcutEntry("file.open", "Open File", "Ctrl+O"));
        list.add(new ShortcutEntry("file.save", "Save File", "Ctrl+S"));

        // Edit
        list.add(new ShortcutEntry("edit.undo", "Undo", "Ctrl+Z"));
        list.add(new ShortcutEntry("edit.redo", "Redo", "Ctrl+Y"));
        list.add(new ShortcutEntry("edit.copy", "Copy", "Ctrl+C"));
        list.add(new ShortcutEntry("edit.cut", "Cut", "Ctrl+X"));
        list.add(new ShortcutEntry("edit.paste", "Paste", "Ctrl+V"));
        list.add(new ShortcutEntry("edit.delete", "Delete Selection", "Delete"));
        list.add(new ShortcutEntry("edit.selectAll", "Select All", "Ctrl+A"));
        list.add(new ShortcutEntry("edit.rotate", "Rotate Component", "R"));
        list.add(new ShortcutEntry("edit.find", "Find Elements", "Ctrl+F"));
        list.add(new ShortcutEntry("edit.flipWire", "Flip Wire Direction", "F"));
        list.add(new ShortcutEntry("edit.splitWire", "Split Wire", "S"));
        list.add(new ShortcutEntry("edit.escape", "Cancel / Deselect", "Escape"));

        // View
        list.add(new ShortcutEntry("view.zoomIn", "Zoom In", "Ctrl+Plus"));
        list.add(new ShortcutEntry("view.zoomOut", "Zoom Out", "Ctrl+Minus"));
        list.add(new ShortcutEntry("view.fit", "Fit to Window", "F1"));
        list.add(new ShortcutEntry("view.treeToggle", "Toggle Tree View", "F5"));
        list.add(new ShortcutEntry("view.presentation", "Presentation Mode", "F4"));

        // Simulation
        list.add(new ShortcutEntry("sim.start", "Start/Stop Simulation", "Space"));
        list.add(new ShortcutEntry("sim.microStep", "Micro Step", "V"));
        list.add(new ShortcutEntry("sim.runToBreakMicro", "Run to Break (Micro)", "B"));
        list.add(new ShortcutEntry("sim.runMicro", "Run Micro Mode", "G"));
        list.add(new ShortcutEntry("sim.fastRun", "Fast Run to Break", "F7"));
        list.add(new ShortcutEntry("sim.toggleClock", "Toggle Clock", "C"));
        list.add(new ShortcutEntry("sim.showData", "Show Data Table", "F6"));
        list.add(new ShortcutEntry("sim.runTests", "Run Tests", "F8"));
        list.add(new ShortcutEntry("sim.runAllTests", "Run All Tests", "F11"));

        // Tools
        list.add(new ShortcutEntry("tools.componentSearch", "Component Search", "F2"));
        list.add(new ShortcutEntry("tools.insertLast", "Insert Last Component", "L"));

        // Analysis
        list.add(new ShortcutEntry("analysis.analyse", "Analyse Circuit", "F9"));

        return list;
    }

    /**
     * Get the configured shortcut for an action
     *
     * @param actionId the action id
     * @param defaultShortcut the default shortcut
     * @return the configured shortcut string
     */
    public static String getShortcut(String actionId, String defaultShortcut) {
        loadShortcuts();
        return customShortcuts.getOrDefault(actionId, defaultShortcut);
    }

    /**
     * Convert a shortcut string like "Ctrl+N" to a KeyStroke
     *
     * @param shortcut the shortcut string
     * @return the KeyStroke, or null if invalid
     */
    public static KeyStroke parseShortcut(String shortcut) {
        if (shortcut == null || shortcut.isEmpty())
            return null;

        // Convert our display format to Swing format
        // "Ctrl+Shift+N" -> "ctrl shift N"
        String swingFormat = shortcut
                .replace("Ctrl+", "ctrl ")
                .replace("Alt+", "alt ")
                .replace("Shift+", "shift ")
                .replace("Plus", "PLUS")
                .replace("Minus", "MINUS")
                .replace("Space", "SPACE")
                .trim();

        // Handle function keys like F1..F12
        if (swingFormat.matches(".*F\\d+$")) {
            // Already in correct format for KeyStroke.getKeyStroke
        }

        KeyStroke ks = KeyStroke.getKeyStroke(swingFormat);
        if (ks != null)
            return ks;

        // Try uppercase
        ks = KeyStroke.getKeyStroke(swingFormat.toUpperCase());
        return ks;
    }

    /**
     * Format a KeyStroke for display
     *
     * @param ks the keystroke
     * @return the display string
     */
    public static String formatKeyStroke(KeyStroke ks) {
        if (ks == null)
            return "";

        StringBuilder sb = new StringBuilder();
        int mod = ks.getModifiers();
        if ((mod & InputEvent.CTRL_DOWN_MASK) != 0)
            sb.append("Ctrl+");
        if ((mod & InputEvent.ALT_DOWN_MASK) != 0)
            sb.append("Alt+");
        if ((mod & InputEvent.SHIFT_DOWN_MASK) != 0)
            sb.append("Shift+");

        int code = ks.getKeyCode();
        String keyText = KeyEvent.getKeyText(code);

        // Clean up common names
        switch (code) {
            case KeyEvent.VK_SPACE:
                keyText = "Space";
                break;
            case KeyEvent.VK_PLUS:
            case KeyEvent.VK_ADD:
                keyText = "Plus";
                break;
            case KeyEvent.VK_MINUS:
            case KeyEvent.VK_SUBTRACT:
                keyText = "Minus";
                break;
            default:
                break;
        }

        sb.append(keyText);
        return sb.toString();
    }

    private static void loadShortcuts() {
        if (loaded)
            return;
        loaded = true;

        File home = new File(System.getProperty("user.home"));
        File file = new File(home, SHORTCUTS_FILE);
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                    continue;
                int eq = line.indexOf('=');
                if (eq > 0) {
                    String key = line.substring(0, eq).trim();
                    String value = line.substring(eq + 1).trim();
                    customShortcuts.put(key, value);
                }
            }
        } catch (IOException e) {
            // Silently ignore
        }
    }

    private void saveShortcuts() {
        File home = new File(System.getProperty("user.home"));
        File file = new File(home, SHORTCUTS_FILE);

        // Update customShortcuts from entries
        for (ShortcutEntry entry : entries) {
            if (!entry.currentShortcut.equals(entry.defaultShortcut)) {
                customShortcuts.put(entry.actionId, entry.currentShortcut);
            } else {
                customShortcuts.remove(entry.actionId);
            }
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("# Digital Simulator - Custom Keyboard Shortcuts");
            writer.println("# Format: actionId=Shortcut");
            writer.println("# Restart application after changes");
            for (Map.Entry<String, String> e : customShortcuts.entrySet()) {
                writer.println(e.getKey() + "=" + e.getValue());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save shortcuts: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---- Table Model ----

    private class ShortcutTableModel extends AbstractTableModel {
        private final String[] columns = {"Action", "Shortcut"};

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            ShortcutEntry entry = entries.get(row);
            if (col == 0) return entry.description;
            return entry.currentShortcut;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 1;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 1) {
                entries.get(row).currentShortcut = (String) value;
                modified = true;
                fireTableCellUpdated(row, col);
            }
        }
    }

    // ---- Renderers ----

    private static class DescriptionCellRenderer extends DefaultTableCellRenderer {
        private final boolean dark;

        DescriptionCellRenderer(boolean dark) {
            this.dark = dark;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setBorder(new EmptyBorder(4, 10, 4, 4));
            setFont(getFont().deriveFont(12f));
            return this;
        }
    }

    private static class ShortcutCellRenderer extends DefaultTableCellRenderer {
        private final boolean dark;

        ShortcutCellRenderer(boolean dark) {
            this.dark = dark;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            setHorizontalAlignment(CENTER);
            setFont(getFont().deriveFont(Font.BOLD, 12f));

            if (!isSelected) {
                setForeground(dark ? new Color(100, 200, 255) : new Color(0, 80, 160));
            }
            return this;
        }
    }

    // ---- Cell Editor that captures keystrokes ----

    private class ShortcutCellEditor extends AbstractCellEditor implements TableCellEditor {
        private final JTextField field;
        private String currentValue;

        ShortcutCellEditor() {
            field = new JTextField();
            field.setHorizontalAlignment(JTextField.CENTER);
            field.setFont(field.getFont().deriveFont(Font.BOLD, 12f));
            field.setEditable(false);
            field.setBackground(new Color(60, 80, 110));
            field.setForeground(Color.WHITE);
            field.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 255), 2));

            field.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    int code = e.getKeyCode();
                    // Ignore pure modifier keys
                    if (code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_CONTROL
                            || code == KeyEvent.VK_ALT || code == KeyEvent.VK_META)
                        return;

                    // Escape = cancel
                    if (code == KeyEvent.VK_ESCAPE) {
                        cancelCellEditing();
                        return;
                    }

                    KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
                    currentValue = formatKeyStroke(ks);
                    field.setText(currentValue);
                    stopCellEditing();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int col) {
            currentValue = (String) value;
            field.setText("Press a key...");
            SwingUtilities.invokeLater(field::requestFocusInWindow);
            return field;
        }

        @Override
        public Object getCellEditorValue() {
            return currentValue;
        }
    }
}
