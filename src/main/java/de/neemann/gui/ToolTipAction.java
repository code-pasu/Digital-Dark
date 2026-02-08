/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.gui.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Action to handle tool tips.
 */
public abstract class ToolTipAction extends AbstractAction {
    private Icon icon;
    private String toolTipText;
    private ToolTipProvider toolTipProvider;
    private KeyStroke accelerator;

    /**
     * Creates a new instance
     *
     * @param name the name of the action
     */
    public ToolTipAction(String name) {
        super(name);
    }

    /**
     * Creates a new instance
     *
     * @param name the name of the action
     * @param icon the icon
     */
    public ToolTipAction(String name, Icon icon) {
        super(name, icon);
        this.icon = icon;
    }

    /**
     * sets the icon
     *
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        putValue(Action.SMALL_ICON, icon);
        this.icon = icon;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Sets the tool tip text
     *
     * @param text the tool tip text
     * @return this for call chaining
     */
    public ToolTipAction setToolTip(String text) {
        this.toolTipText = new LineBreaker().toHTML().breakLines(text);
        return this;
    }

    /**
     * Sets a tool tip provider
     *
     * @param toolTipProvider the tool tip provider
     * @return this for call chaining
     */
    public ToolTipAction setToolTipProvider(ToolTipProvider toolTipProvider) {
        this.toolTipProvider = toolTipProvider;
        return this;
    }

    /**
     * Sets an accelerator to the action
     *
     * @param key the accelerator key
     * @return this for call chaining
     */
    public ToolTipAction setAcceleratorCTRLplus(char key) {
        return setAccelerator(KeyStroke.getKeyStroke(key, getCTRLMask()));
    }

    /**
     * Sets an accelerator to the action
     *
     * @param key the accelerator key
     * @return this for call chaining
     */
    public ToolTipAction setAcceleratorCTRLplus(String key) {
        int keyCode = KeyStroke.getKeyStroke(key).getKeyCode();
        return setAccelerator(KeyStroke.getKeyStroke(keyCode, getCTRLMask()));
    }

    /**
     * @return the system specific CTRL mask.
     */
    public static int getCTRLMask() {
        int mask = InputEvent.CTRL_DOWN_MASK;
        if (Screen.isMac())
            mask = InputEvent.META_DOWN_MASK;
        return mask;
    }

    /**
     * Sets an accelerator to the action
     *
     * @param key the accelerator key
     * @return this for call chaining
     */
    public ToolTipAction setAccelerator(String key) {
        return setAccelerator(KeyStroke.getKeyStroke(key));
    }

    /**
     * Sets an accelerator to the action
     *
     * @param accelerator the accelerator
     * @return this for call chaining
     */
    public ToolTipAction setAccelerator(KeyStroke accelerator) {
        if (accelerator.getKeyCode() == KeyEvent.VK_PLUS && Settings.getInstance().get(Keys.SETTINGS_USE_EQUALS_KEY))
            accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, accelerator.getModifiers());
        this.accelerator = accelerator;
        return this;
    }

    /**
     * enables the accelerator in the given component
     *
     * @param component the component
     * @return this for call chaining
     */
    public ToolTipAction enableAcceleratorIn(JComponent component) {
        if (accelerator == null)
            throw new RuntimeException("no accelerator given");
        component.getInputMap().put(accelerator, this);
        component.getActionMap().put(this, this);
        return this;
    }

    /**
     * Sets the activated state for this action
     *
     * @param newValue the new state
     * @return this for call chaining
     */
    public ToolTipAction setEnabledChain(boolean newValue) {
        super.setEnabled(newValue);
        return this;
    }

    /**
     * Wraps a plain tooltip string in HTML with the correct foreground color.
     * Ensures tooltip text is visible in dark themes where Swing HTML ignores
     * UIManager's ToolTip.foreground color.
     *
     * @param text the plain tooltip text
     * @return the HTML-wrapped tooltip string
     */
    public static String wrapTooltipHtml(String text) {
        if (text == null) return null;
        if (text.startsWith("<html>")) return text;

        Color fg = UIManager.getColor("ToolTip.foreground");
        if (fg == null) fg = Color.BLACK;
        String hex = String.format("#%02x%02x%02x", fg.getRed(), fg.getGreen(), fg.getBlue());
        String escaped = text.replace("<", "&lt;").replace(">", "&gt;");
        return "<html><span style='color:" + hex + "'>" + escaped + "</span></html>";
    }

    /**
     * Returns a human-readable display string for the accelerator key.
     *
     * @return the accelerator display text, or null if no accelerator is set
     */
    public String getAcceleratorDisplayText() {
        if (accelerator == null) return null;
        StringBuilder sb = new StringBuilder();
        int mod = accelerator.getModifiers();
        if ((mod & InputEvent.CTRL_DOWN_MASK) != 0) sb.append("Ctrl+");
        if ((mod & InputEvent.META_DOWN_MASK) != 0) sb.append("Cmd+");
        if ((mod & InputEvent.ALT_DOWN_MASK) != 0) sb.append("Alt+");
        if ((mod & InputEvent.SHIFT_DOWN_MASK) != 0) sb.append("Shift+");
        sb.append(KeyEvent.getKeyText(accelerator.getKeyCode()));
        return sb.toString();
    }

    private String appendAccelerator(String tip) {
        String accel = getAcceleratorDisplayText();
        if (accel == null || tip == null) return tip;
        if (tip.startsWith("<html>")) {
            return tip.replace("</span></html>", " (" + accel + ")</span></html>");
        }
        return tip + " (" + accel + ")";
    }

    /**
     * @return a JButton associated with this action
     */
    public JButton createJButton() {
        JButton b = new JButton(this);
        if (toolTipText != null) {
            b.setToolTipText(appendAccelerator(toolTipText));
        }
        return b;
    }

    /**
     * @return a JButton associated with this action, contains only the icon
     */
    public JButton createJButtonNoText() {
        JButton b;
        final String accelText = getAcceleratorDisplayText();
        if (toolTipProvider == null) {
            b = new JButton(this);
            String tip = toolTipText != null ? toolTipText : b.getText();
            if (accelText != null && tip != null) {
                if (tip.startsWith("<html>")) {
                    tip = tip.replace("</span></html>", " (" + accelText + ")</span></html>");
                } else {
                    tip = wrapTooltipHtml(tip + " (" + accelText + ")");
                }
            }
            b.setToolTipText(tip);
        } else {
            b = new JButton(this) {
                @Override
                public String getToolTipText() {
                    String tip = toolTipProvider.getToolTip();
                    if (accelText != null && tip != null) {
                        tip = tip + " (" + accelText + ")";
                    }
                    return wrapTooltipHtml(tip);
                }
            };
            ToolTipManager.sharedInstance().registerComponent(b);
        }
        b.setText(null);
        b.setFocusable(false);
        return b;
    }

    /**
     * @return a JButton associated with this action, contains only the icon
     */
    public JButton createJButtonNoTextSmall() {
        JButton b = createJButtonNoText();
        b.setPreferredSize(new Dimension(icon.getIconWidth() + 4, icon.getIconHeight() + 4));
        return b;
    }

    /**
     * @return a JMenuItem associated with this action
     */
    public JMenuItem createJMenuItem() {
        JMenuItem i;
        if (toolTipProvider == null) {
            i = new JMenuItem(this);
            if (toolTipText != null)
                i.setToolTipText(toolTipText);
        } else {
            i = new JMenuItem(this) {
                @Override
                public String getToolTipText() {
                    return wrapTooltipHtml(toolTipProvider.getToolTip());
                }
            };
            ToolTipManager.sharedInstance().registerComponent(i);
        }
        if (accelerator != null)
            i.setAccelerator(accelerator);
        return i;
    }

    /**
     * @return a JMenuItem associated with this action, contains no icon
     */
    public JMenuItem createJMenuItemNoIcon() {
        JMenuItem i = createJMenuItem();
        i.setIcon(null);
        return i;
    }

}
