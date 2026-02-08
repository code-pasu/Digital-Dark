/*
 * Copyright (c) 2024 Digital Contributors
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.theme;

import de.neemann.digital.draw.graphics.ColorScheme;
import de.neemann.digital.gui.Settings;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the UI theme (dark/light) for the entire application.
 * Installs a custom MetalTheme so that ALL Metal LaF painting — toolbars,
 * menubars, buttons, scrollbars — uses our colors. OceanTheme's gradient
 * painting ignores simple UIManager.put() calls; the only way to control
 * it is through the MetalTheme primary/secondary color methods.
 */
public final class ThemeManager {

    // ── Dark palette ──────────────────────────────────────────────────────
    private static final Color D_BG          = new Color(42, 44, 50);
    private static final Color D_BG_LIGHT    = new Color(48, 50, 56);
    private static final Color D_PANEL       = new Color(42, 44, 50);
    private static final Color D_INPUT       = new Color(52, 54, 60);
    private static final Color D_SELECTED    = new Color(0, 100, 180);
    private static final Color D_TOOLBAR     = new Color(36, 38, 44);
    private static final Color D_MENU        = new Color(38, 40, 46);
    private static final Color D_MENU_HOVER  = new Color(58, 60, 68);
    private static final Color D_FG          = new Color(210, 212, 218);
    private static final Color D_FG2         = new Color(160, 162, 168);
    private static final Color D_FG_DIS      = new Color(90, 92, 98);
    private static final Color D_BORDER      = new Color(58, 60, 66);
    private static final Color D_FOCUS       = new Color(0, 120, 215);
    private static final Color D_ACCENT      = new Color(0, 120, 215);
    private static final Color D_SCR_TRACK   = new Color(40, 42, 48);
    private static final Color D_SCR_THUMB   = new Color(75, 77, 83);

    // ── Light palette ─────────────────────────────────────────────────────
    private static final Color L_BG          = new Color(250, 250, 250);
    private static final Color L_PANEL       = new Color(245, 245, 245);
    private static final Color L_INPUT       = Color.WHITE;
    private static final Color L_SELECTED    = new Color(0, 120, 215);
    private static final Color L_TOOLBAR     = new Color(240, 240, 240);
    private static final Color L_MENU        = new Color(248, 248, 248);
    private static final Color L_MENU_HOVER  = new Color(225, 232, 240);
    private static final Color L_FG          = new Color(30, 30, 30);
    private static final Color L_FG2         = new Color(100, 100, 100);
    private static final Color L_FG_DIS      = new Color(170, 170, 170);
    private static final Color L_BORDER      = new Color(210, 210, 210);

    private static boolean darkMode = true;
    private static final List<ThemeChangeListener> LISTENERS = new ArrayList<>();

    private ThemeManager() {
    }

    /** @return true if dark mode is active */
    public static boolean isDarkMode() {
        return darkMode;
    }

    /** Adds a theme change listener. */
    public static void addThemeChangeListener(ThemeChangeListener listener) {
        LISTENERS.add(listener);
    }

    /** Removes a theme change listener. */
    public static void removeThemeChangeListener(ThemeChangeListener listener) {
        LISTENERS.remove(listener);
    }

    /** Toggles between dark and light mode. */
    public static void toggleTheme() {
        setDarkMode(!darkMode);
    }

    /** Sets the theme mode. */
    public static void setDarkMode(boolean dark) {
        darkMode = dark;
        applyTheme();
        for (ThemeChangeListener l : LISTENERS) {
            l.themeChanged(dark);
        }
    }

    /**
     * Initializes the theme from the saved color scheme setting.
     * Must be called once at startup, after Settings are available.
     */
    public static void initFromSettings() {
        // Always use dark mode
        darkMode = true;
        Settings.getInstance().getAttributes().set(ColorScheme.COLOR_SCHEME, ColorScheme.ColorSchemes.DARK);
        applyTheme();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Core: custom MetalTheme + UIManager defaults
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Applies the current theme by installing a custom MetalTheme and
     * fine-tuning UIManager defaults on top.
     */
    public static void applyTheme() {
        if (darkMode) {
            installDarkMetalTheme();
            applyDarkDefaults();
        } else {
            installLightMetalTheme();
            applyLightDefaults();
        }
    }

    /**
     * Installs a custom dark DefaultMetalTheme. The 6 primary/secondary
     * methods plus black/white control ALL Metal LaF rendering:
     * <ul>
     *   <li>secondary3 → control/panel/toolbar/menubar background</li>
     *   <li>black → text/foreground color</li>
     *   <li>white → input field background</li>
     *   <li>primary1-3 → accent/selection/focus colors</li>
     *   <li>secondary1-2 → 3D border shadows</li>
     * </ul>
     */
    private static void installDarkMetalTheme() {
        MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme() {
            private final ColorUIResource p1 = new ColorUIResource(0, 85, 155);
            private final ColorUIResource p2 = new ColorUIResource(40, 110, 180);
            private final ColorUIResource p3 = new ColorUIResource(0, 100, 180);
            private final ColorUIResource s1 = new ColorUIResource(48, 50, 56);
            private final ColorUIResource s2 = new ColorUIResource(56, 58, 64);
            private final ColorUIResource s3 = new ColorUIResource(42, 44, 50);
            private final ColorUIResource blk = new ColorUIResource(210, 212, 218);
            private final ColorUIResource wht = new ColorUIResource(52, 54, 60);

            @Override public String getName() { return "DigitalDark"; }
            @Override protected ColorUIResource getPrimary1() { return p1; }
            @Override protected ColorUIResource getPrimary2() { return p2; }
            @Override protected ColorUIResource getPrimary3() { return p3; }
            @Override protected ColorUIResource getSecondary1() { return s1; }
            @Override protected ColorUIResource getSecondary2() { return s2; }
            @Override protected ColorUIResource getSecondary3() { return s3; }
            @Override protected ColorUIResource getBlack() { return blk; }
            @Override protected ColorUIResource getWhite() { return wht; }
        });
        reinstallLaF();
    }

    /** Restores the standard OceanTheme for light mode. */
    private static void installLightMetalTheme() {
        MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        reinstallLaF();
    }

    private static void reinstallLaF() {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  UIManager fine-tuning (on top of the MetalTheme base)
    // ══════════════════════════════════════════════════════════════════════

    private static void applyDarkDefaults() {
        // Kill gradient painting that OceanTheme leaves behind
        UIManager.put("MenuBar.gradient", null);
        UIManager.put("Button.gradient", null);
        UIManager.put("ToggleButton.gradient", null);
        UIManager.put("ScrollBar.gradient", null);
        UIManager.put("Slider.gradient", null);
        UIManager.put("InternalFrame.activeTitleGradient", null);

        // Menu
        p("MenuBar.background", D_TOOLBAR);
        p("MenuBar.foreground", D_FG);
        p("Menu.background", D_MENU);
        p("Menu.foreground", D_FG);
        p("Menu.selectionBackground", D_MENU_HOVER);
        p("Menu.selectionForeground", D_FG);
        p("Menu.disabledForeground", D_FG_DIS);
        p("MenuItem.background", D_MENU);
        p("MenuItem.foreground", D_FG);
        p("MenuItem.selectionBackground", D_MENU_HOVER);
        p("MenuItem.selectionForeground", D_FG);
        p("MenuItem.disabledForeground", D_FG_DIS);
        p("MenuItem.acceleratorForeground", D_FG2);
        p("CheckBoxMenuItem.background", D_MENU);
        p("CheckBoxMenuItem.foreground", D_FG);
        p("CheckBoxMenuItem.selectionBackground", D_MENU_HOVER);
        p("CheckBoxMenuItem.selectionForeground", D_FG);
        p("RadioButtonMenuItem.background", D_MENU);
        p("RadioButtonMenuItem.foreground", D_FG);
        p("RadioButtonMenuItem.selectionBackground", D_MENU_HOVER);
        p("RadioButtonMenuItem.selectionForeground", D_FG);
        p("PopupMenu.background", D_MENU);
        p("PopupMenu.foreground", D_FG);

        // Toolbar & Buttons
        p("ToolBar.background", D_TOOLBAR);
        p("ToolBar.foreground", D_FG);
        p("ToolBar.dockingBackground", D_TOOLBAR);
        p("ToolBar.floatingBackground", D_TOOLBAR);
        p("Button.background", D_BG_LIGHT);
        p("Button.foreground", D_FG);
        p("Button.select", D_SELECTED);
        p("Button.disabledText", D_FG_DIS);
        p("Button.focus", D_FOCUS);
        p("ToggleButton.background", D_BG_LIGHT);
        p("ToggleButton.foreground", D_FG);
        p("ToggleButton.select", D_SELECTED);
        p("ToggleButton.disabledText", D_FG_DIS);

        // Text fields
        p("TextField.background", D_INPUT);
        p("TextField.foreground", D_FG);
        p("TextField.caretForeground", D_FG);
        p("TextField.selectionBackground", D_SELECTED);
        p("TextField.selectionForeground", Color.WHITE);
        p("TextField.inactiveBackground", D_BG_LIGHT);
        p("TextField.inactiveForeground", D_FG2);
        p("TextArea.background", D_INPUT);
        p("TextArea.foreground", D_FG);
        p("TextArea.caretForeground", D_FG);
        p("TextArea.selectionBackground", D_SELECTED);
        p("TextArea.selectionForeground", Color.WHITE);
        p("TextPane.background", D_INPUT);
        p("TextPane.foreground", D_FG);
        p("TextPane.caretForeground", D_FG);
        p("EditorPane.background", D_INPUT);
        p("EditorPane.foreground", D_FG);
        p("EditorPane.caretForeground", D_FG);
        p("FormattedTextField.background", D_INPUT);
        p("FormattedTextField.foreground", D_FG);
        p("PasswordField.background", D_INPUT);
        p("PasswordField.foreground", D_FG);
        p("PasswordField.caretForeground", D_FG);

        // Combo, List, Table, Tree
        p("ComboBox.background", D_INPUT);
        p("ComboBox.foreground", D_FG);
        p("ComboBox.selectionBackground", D_SELECTED);
        p("ComboBox.selectionForeground", Color.WHITE);
        p("ComboBox.disabledBackground", D_BG_LIGHT);
        p("ComboBox.disabledForeground", D_FG_DIS);
        p("ComboBox.buttonBackground", D_INPUT);
        p("List.background", D_INPUT);
        p("List.foreground", D_FG);
        p("List.selectionBackground", D_SELECTED);
        p("List.selectionForeground", Color.WHITE);
        p("Table.background", D_INPUT);
        p("Table.foreground", D_FG);
        p("Table.selectionBackground", D_SELECTED);
        p("Table.selectionForeground", Color.WHITE);
        p("Table.gridColor", D_BORDER);
        p("TableHeader.background", D_BG_LIGHT);
        p("TableHeader.foreground", D_FG);
        p("Tree.background", D_INPUT);
        p("Tree.foreground", D_FG);
        p("Tree.selectionBackground", D_SELECTED);
        p("Tree.selectionForeground", Color.WHITE);
        p("Tree.textBackground", D_INPUT);
        p("Tree.textForeground", D_FG);
        p("Tree.hash", D_BORDER);
        p("Tree.line", D_BORDER);

        // Scroll
        p("ScrollBar.background", D_SCR_TRACK);
        p("ScrollBar.foreground", D_FG);
        p("ScrollBar.thumb", D_SCR_THUMB);
        p("ScrollBar.thumbHighlight", D_SCR_THUMB);
        p("ScrollBar.thumbShadow", D_SCR_TRACK);
        p("ScrollBar.thumbDarkShadow", D_SCR_TRACK);
        p("ScrollBar.track", D_SCR_TRACK);
        p("ScrollBar.trackHighlight", D_SCR_TRACK);
        p("ScrollPane.background", D_PANEL);
        p("ScrollPane.foreground", D_FG);
        p("Viewport.background", D_PANEL);
        p("Viewport.foreground", D_FG);

        // Tabs
        Color tabBg = new Color(45, 47, 53);
        p("TabbedPane.background", tabBg);
        p("TabbedPane.foreground", D_FG);
        p("TabbedPane.selected", D_BG);
        p("TabbedPane.contentAreaColor", D_BG);
        p("TabbedPane.tabAreaBackground", tabBg);
        p("TabbedPane.unselectedBackground", tabBg);
        p("TabbedPane.selectHighlight", D_ACCENT);
        p("TabbedPane.highlight", D_BORDER);
        p("TabbedPane.light", D_BORDER);
        p("TabbedPane.shadow", D_BORDER);
        p("TabbedPane.darkShadow", D_BORDER);

        // Split, Option, Label, Spinner
        p("SplitPane.background", D_PANEL);
        p("SplitPane.dividerFocusColor", D_BORDER);
        UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
        p("OptionPane.background", D_PANEL);
        p("OptionPane.foreground", D_FG);
        p("OptionPane.messageForeground", D_FG);
        p("Label.background", D_PANEL);
        p("Label.foreground", D_FG);
        p("Label.disabledForeground", D_FG_DIS);
        p("Spinner.background", D_INPUT);
        p("Spinner.foreground", D_FG);

        // ToolTip, Progress, Separator, Check, Radio
        p("ToolTip.background", new Color(50, 52, 58));
        p("ToolTip.foreground", D_FG);
        p("ProgressBar.background", D_BG_LIGHT);
        p("ProgressBar.foreground", D_ACCENT);
        p("ProgressBar.selectionBackground", D_FG);
        p("ProgressBar.selectionForeground", D_FG);
        p("Separator.foreground", D_BORDER);
        p("Separator.background", D_PANEL);
        p("CheckBox.background", D_PANEL);
        p("CheckBox.foreground", D_FG);
        p("RadioButton.background", D_PANEL);
        p("RadioButton.foreground", D_FG);

        // Panel & system
        p("Panel.background", D_PANEL);
        p("Panel.foreground", D_FG);
        p("window", D_BG);
        p("control", D_PANEL);
        p("controlText", D_FG);
        p("text", D_FG);
        p("textText", D_FG);
        p("info", D_BG_LIGHT);
        p("infoText", D_FG);
        p("desktop", D_BG);
        p("activeCaption", D_ACCENT);
        p("activeCaptionText", Color.WHITE);
        p("inactiveCaption", D_BG_LIGHT);
        p("inactiveCaptionText", D_FG2);
        p("FileChooser.listViewBackground", D_INPUT);

        // Borders
        UIManager.put("MenuBar.border",
                new BorderUIResource(BorderFactory.createMatteBorder(0, 0, 1, 0, D_BORDER)));
        UIManager.put("ToolBar.border",
                new BorderUIResource(BorderFactory.createMatteBorder(0, 0, 1, 0, D_BORDER)));
        UIManager.put("PopupMenu.border",
                new BorderUIResource(new LineBorder(D_BORDER, 1)));
        UIManager.put("TextField.border",
                new BorderUIResource(new CompoundBorder(
                        new LineBorder(D_BORDER, 1), new EmptyBorder(2, 6, 2, 6))));
        UIManager.put("ScrollPane.border",
                new BorderUIResource(new LineBorder(D_BORDER, 1)));
        UIManager.put("Table.scrollPaneBorder",
                new BorderUIResource(new LineBorder(D_BORDER, 1)));
        UIManager.put("ToolTip.border",
                new BorderUIResource(new LineBorder(D_BORDER, 1)));
        UIManager.put("TitledBorder.border",
                new BorderUIResource(new LineBorder(D_BORDER, 1)));
        UIManager.put("TitledBorder.titleColor", new ColorUIResource(D_FG));
    }

    private static void applyLightDefaults() {
        UIManager.put("MenuBar.gradient", null);
        UIManager.put("Button.gradient", null);

        p("MenuBar.background", L_MENU);
        p("MenuBar.foreground", L_FG);
        p("Menu.background", L_MENU);
        p("Menu.foreground", L_FG);
        p("Menu.selectionBackground", L_MENU_HOVER);
        p("Menu.selectionForeground", L_FG);
        p("Menu.disabledForeground", L_FG_DIS);
        p("MenuItem.background", L_MENU);
        p("MenuItem.foreground", L_FG);
        p("MenuItem.selectionBackground", L_MENU_HOVER);
        p("MenuItem.selectionForeground", L_FG);
        p("MenuItem.disabledForeground", L_FG_DIS);
        p("MenuItem.acceleratorForeground", L_FG2);
        p("CheckBoxMenuItem.background", L_MENU);
        p("CheckBoxMenuItem.foreground", L_FG);
        p("CheckBoxMenuItem.selectionBackground", L_MENU_HOVER);
        p("CheckBoxMenuItem.selectionForeground", L_FG);
        p("RadioButtonMenuItem.background", L_MENU);
        p("RadioButtonMenuItem.foreground", L_FG);
        p("RadioButtonMenuItem.selectionBackground", L_MENU_HOVER);
        p("RadioButtonMenuItem.selectionForeground", L_FG);
        p("PopupMenu.background", L_MENU);
        p("PopupMenu.foreground", L_FG);
        p("ToolBar.background", L_TOOLBAR);
        p("ToolBar.foreground", L_FG);
        p("ToolBar.dockingBackground", L_TOOLBAR);
        p("ToolBar.floatingBackground", L_TOOLBAR);
        p("Button.background", new Color(230, 230, 230));
        p("Button.foreground", L_FG);
        p("Button.select", L_SELECTED);
        p("Button.disabledText", L_FG_DIS);
        p("Button.focus", new Color(0, 120, 215));
        p("ToggleButton.background", new Color(230, 230, 230));
        p("ToggleButton.foreground", L_FG);
        p("ToggleButton.select", L_SELECTED);
        p("TextField.background", L_INPUT);
        p("TextField.foreground", L_FG);
        p("TextField.caretForeground", L_FG);
        p("TextField.selectionBackground", L_SELECTED);
        p("TextField.selectionForeground", Color.WHITE);
        p("TextArea.background", L_INPUT);
        p("TextArea.foreground", L_FG);
        p("TextArea.caretForeground", L_FG);
        p("TextArea.selectionBackground", L_SELECTED);
        p("TextArea.selectionForeground", Color.WHITE);
        p("TextPane.background", L_INPUT);
        p("TextPane.foreground", L_FG);
        p("EditorPane.background", L_INPUT);
        p("EditorPane.foreground", L_FG);
        p("FormattedTextField.background", L_INPUT);
        p("FormattedTextField.foreground", L_FG);
        p("PasswordField.background", L_INPUT);
        p("PasswordField.foreground", L_FG);
        p("ComboBox.background", L_INPUT);
        p("ComboBox.foreground", L_FG);
        p("ComboBox.selectionBackground", L_SELECTED);
        p("ComboBox.selectionForeground", Color.WHITE);
        p("ComboBox.buttonBackground", L_INPUT);
        p("List.background", L_INPUT);
        p("List.foreground", L_FG);
        p("List.selectionBackground", L_SELECTED);
        p("List.selectionForeground", Color.WHITE);
        p("Table.background", L_INPUT);
        p("Table.foreground", L_FG);
        p("Table.selectionBackground", L_SELECTED);
        p("Table.selectionForeground", Color.WHITE);
        p("Table.gridColor", L_BORDER);
        p("TableHeader.background", L_PANEL);
        p("TableHeader.foreground", L_FG);
        p("Tree.background", L_INPUT);
        p("Tree.foreground", L_FG);
        p("Tree.selectionBackground", L_SELECTED);
        p("Tree.selectionForeground", Color.WHITE);
        p("Tree.textBackground", L_INPUT);
        p("Tree.textForeground", L_FG);
        p("Tree.hash", L_BORDER);
        p("Tree.line", L_BORDER);
        Color st = new Color(235, 235, 235);
        Color sb = new Color(190, 190, 190);
        p("ScrollBar.background", st);
        p("ScrollBar.thumb", sb);
        p("ScrollBar.thumbHighlight", sb);
        p("ScrollBar.thumbShadow", st);
        p("ScrollBar.thumbDarkShadow", st);
        p("ScrollBar.track", st);
        p("ScrollBar.trackHighlight", st);
        p("ScrollPane.background", L_PANEL);
        p("Viewport.background", L_PANEL);
        p("TabbedPane.background", L_TOOLBAR);
        p("TabbedPane.foreground", L_FG);
        p("TabbedPane.selected", Color.WHITE);
        p("TabbedPane.contentAreaColor", Color.WHITE);
        p("TabbedPane.tabAreaBackground", L_TOOLBAR);
        p("TabbedPane.selectHighlight", new Color(0, 120, 215));
        p("TabbedPane.highlight", L_BORDER);
        p("TabbedPane.shadow", L_BORDER);
        p("TabbedPane.darkShadow", L_BORDER);
        p("SplitPane.background", L_PANEL);
        p("SplitPane.dividerFocusColor", L_BORDER);
        p("OptionPane.background", L_PANEL);
        p("OptionPane.foreground", L_FG);
        p("OptionPane.messageForeground", L_FG);
        p("Label.background", L_PANEL);
        p("Label.foreground", L_FG);
        p("Label.disabledForeground", L_FG_DIS);
        p("Spinner.background", L_INPUT);
        p("Spinner.foreground", L_FG);
        p("ToolTip.background", new Color(255, 255, 225));
        p("ToolTip.foreground", L_FG);
        p("ProgressBar.background", L_PANEL);
        p("ProgressBar.foreground", new Color(0, 120, 215));
        p("Separator.foreground", L_BORDER);
        p("Separator.background", L_PANEL);
        p("CheckBox.background", L_PANEL);
        p("CheckBox.foreground", L_FG);
        p("RadioButton.background", L_PANEL);
        p("RadioButton.foreground", L_FG);
        p("Panel.background", L_PANEL);
        p("Panel.foreground", L_FG);
        p("window", L_BG);
        p("control", L_PANEL);
        p("controlText", L_FG);
        p("text", L_FG);
        p("textText", L_FG);
        p("info", new Color(255, 255, 225));
        p("infoText", L_FG);
        p("desktop", L_BG);
        p("activeCaption", new Color(0, 120, 215));
        p("activeCaptionText", Color.WHITE);
        p("inactiveCaption", new Color(220, 220, 220));
        p("inactiveCaptionText", L_FG2);
        UIManager.put("MenuBar.border",
                new BorderUIResource(BorderFactory.createMatteBorder(0, 0, 1, 0, L_BORDER)));
        UIManager.put("ToolBar.border",
                new BorderUIResource(BorderFactory.createMatteBorder(0, 0, 1, 0, L_BORDER)));
        UIManager.put("PopupMenu.border",
                new BorderUIResource(new LineBorder(L_BORDER, 1)));
        UIManager.put("TextField.border",
                new BorderUIResource(new CompoundBorder(
                        new LineBorder(L_BORDER, 1), new EmptyBorder(2, 6, 2, 6))));
        UIManager.put("ScrollPane.border",
                new BorderUIResource(new LineBorder(L_BORDER, 1)));
        UIManager.put("Table.scrollPaneBorder",
                new BorderUIResource(new LineBorder(L_BORDER, 1)));
        UIManager.put("ToolTip.border",
                new BorderUIResource(new LineBorder(L_BORDER, 1)));
        UIManager.put("TitledBorder.border",
                new BorderUIResource(new LineBorder(L_BORDER, 1)));
        UIManager.put("TitledBorder.titleColor", new ColorUIResource(L_FG));
    }

    // ══════════════════════════════════════════════════════════════════════

    private static void p(String key, Object value) {
        if (value instanceof Color) {
            UIManager.put(key, new ColorUIResource((Color) value));
        } else {
            UIManager.put(key, value);
        }
    }

    /**
     * Refreshes all open windows to pick up the new theme.
     */
    public static void refreshAllWindows() {
        for (Window w : Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w);
            w.repaint();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Color getters for custom-painted components
    // ══════════════════════════════════════════════════════════════════════

    /** @return the current theme background */
    public static Color getBackground() {
        return darkMode ? D_BG : L_BG;
    }

    /** @return the current theme foreground */
    public static Color getForeground() {
        return darkMode ? D_FG : L_FG;
    }

    /** @return the accent color */
    public static Color getAccent() {
        return D_ACCENT;
    }

    /** @return the border color */
    public static Color getBorderColor() {
        return darkMode ? D_BORDER : L_BORDER;
    }

    /** @return the toolbar background */
    public static Color getToolbarBackground() {
        return darkMode ? D_TOOLBAR : L_TOOLBAR;
    }

    /** @return the status bar background (dark teal in dark mode) */
    public static Color getStatusBarBackground() {
        return darkMode ? new Color(0, 70, 95) : new Color(0, 122, 204);
    }

    /** @return the status bar foreground */
    public static Color getStatusBarForeground() {
        return Color.WHITE;
    }

    /** @return the toolbar button hover color */
    public static Color getButtonHoverColor() {
        return darkMode ? new Color(58, 60, 68) : new Color(220, 220, 225);
    }

    /** @return the toolbar button pressed color */
    public static Color getButtonPressColor() {
        return darkMode ? new Color(70, 72, 80) : new Color(200, 200, 205);
    }

    /**
     * Listener interface for theme changes.
     */
    public interface ThemeChangeListener {
        /**
         * Called when the theme changes.
         *
         * @param isDark true if the new theme is dark
         */
        void themeChanged(boolean isDark);
    }
}
