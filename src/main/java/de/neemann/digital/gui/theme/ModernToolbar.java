/*
 * Copyright (c) 2024 Digital Contributors
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Factory for creating modern-styled toolbar buttons with hover effects.
 */
public final class ModernToolbar {

    private static final int BUTTON_PADDING = 4;
    private static final int TOOLBAR_GAP = 2;

    private ModernToolbar() {
    }

    /**
     * Styles a JToolBar for modern appearance.
     *
     * @param toolBar the toolbar to style
     */
    public static void styleToolbar(JToolBar toolBar) {
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(true);
        toolBar.setRollover(true);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getBorderColor()),
                new EmptyBorder(TOOLBAR_GAP, 6, TOOLBAR_GAP, 6)
        ));
        toolBar.setOpaque(true);
        toolBar.setBackground(ThemeManager.getToolbarBackground());

        // Style each component in the toolbar
        for (int i = 0; i < toolBar.getComponentCount(); i++) {
            Component c = toolBar.getComponent(i);
            if (c instanceof JButton) {
                styleToolbarButton((JButton) c);
            } else if (c instanceof JToolBar.Separator) {
                styleSeparator((JToolBar.Separator) c);
            }
        }
    }

    /**
     * Styles a toolbar button with modern hover/press effects.
     *
     * @param button the button to style
     */
    public static void styleToolbarButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(BUTTON_PADDING, BUTTON_PADDING + 2,
                BUTTON_PADDING, BUTTON_PADDING + 2));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Remove any existing mouse listeners for hover effect before adding
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setContentAreaFilled(true);
                if (ThemeManager.isDarkMode()) {
                    button.setBackground(new Color(60, 60, 65));
                } else {
                    button.setBackground(new Color(220, 220, 225));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setContentAreaFilled(false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setContentAreaFilled(true);
                if (ThemeManager.isDarkMode()) {
                    button.setBackground(new Color(80, 80, 85));
                } else {
                    button.setBackground(new Color(200, 200, 205));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (button.contains(e.getPoint())) {
                    if (ThemeManager.isDarkMode()) {
                        button.setBackground(new Color(60, 60, 65));
                    } else {
                        button.setBackground(new Color(220, 220, 225));
                    }
                } else {
                    button.setContentAreaFilled(false);
                }
            }
        });
    }

    /**
     * Styles a toolbar separator.
     *
     * @param separator the separator to style
     */
    public static void styleSeparator(JToolBar.Separator separator) {
        separator.setPreferredSize(new Dimension(12, 24));
    }

    /**
     * Creates a modern-styled status bar panel.
     *
     * @param label the label to put in the status bar
     * @return the styled status bar
     */
    public static JPanel createStatusBar(JLabel label) {
        JPanel statusBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(ThemeManager.getStatusBarBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        statusBar.setOpaque(true);
        statusBar.setBackground(ThemeManager.getStatusBarBackground());
        statusBar.setBorder(new EmptyBorder(3, 10, 3, 10));

        label.setForeground(ThemeManager.getStatusBarForeground());
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 11f));
        statusBar.add(label, BorderLayout.WEST);

        return statusBar;
    }

    /**
     * Creates a styled menu bar.
     *
     * @return a styled JMenuBar
     */
    public static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setOpaque(true);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getBorderColor()));
        menuBar.setBackground(ThemeManager.getToolbarBackground());
        return menuBar;
    }
}
