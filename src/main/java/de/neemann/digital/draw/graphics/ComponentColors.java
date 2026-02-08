/*
 * Copyright (c) 2024 Digital Contributors
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.awt.*;

/**
 * Provides a vibrant multi-color palette for circuit components in dark mode.
 * Each component type gets a deterministic color based on its name, giving
 * circuits the colourful look of modern simulators like Sebastian's.
 * <p>
 * In light mode, returns null so the default (no fill / white) behavior is preserved.
 */
public final class ComponentColors {

    // ── Vibrant dark-mode palette ──────────────────────────────────────
    private static final Color[] DARK_PALETTE = {
            new Color(155, 89, 182),   // amethyst purple
            new Color(142, 68, 173),   // deep purple
            new Color(52, 152, 219),   // bright blue
            new Color(41, 128, 185),   // belize blue
            new Color(26, 188, 156),   // turquoise
            new Color(22, 160, 133),   // teal
            new Color(231, 76, 60),    // alizarin red
            new Color(192, 57, 43),    // pomegranate
            new Color(230, 126, 34),   // carrot orange
            new Color(211, 84, 0),     // pumpkin
            new Color(46, 204, 113),   // emerald green
            new Color(39, 174, 96),    // nephritis
            new Color(52, 73, 94),     // wet asphalt
            new Color(241, 196, 15),   // sunflower
            new Color(243, 156, 18),   // orange
            new Color(127, 140, 141),  // concrete (for generic)
    };

    // ── Vibrant embedded-circuit palette (different hues) ──────────────
    private static final Color[] EMBEDDED_PALETTE = {
            new Color(155, 89, 182),   // amethyst
            new Color(52, 152, 219),   // peter river
            new Color(26, 188, 156),   // turquoise
            new Color(230, 126, 34),   // carrot
            new Color(231, 76, 60),    // alizarin
            new Color(46, 204, 113),   // emerald
            new Color(241, 196, 15),   // sunflower
            new Color(52, 73, 94),     // wet asphalt
            new Color(243, 156, 18),   // orange
            new Color(41, 128, 185),   // belize
            new Color(192, 57, 43),    // pomegranate
            new Color(22, 160, 133),   // teal
            new Color(142, 68, 173),   // wisteria
            new Color(39, 174, 96),    // nephritis
            new Color(211, 84, 0),     // pumpkin
            new Color(127, 140, 141),  // concrete
    };

    private static int embeddedCounter = 0;

    private ComponentColors() {
    }

    /**
     * Returns a deterministic fill color for a component based on its name.
     * In dark mode picks from the vibrant palette; in light mode returns null
     * (meaning no fill).
     *
     * @param componentName the short display name of the component
     *                      (e.g. "&amp;", "≥1", "=1", "MUX", etc.)
     * @return a vibrant fill color, or null if light mode
     */
    public static Color getColorForComponent(String componentName) {
        if (!isDarkMode()) return null;
        if (componentName == null || componentName.isEmpty()) return DARK_PALETTE[0];
        int hash = componentName.hashCode() & 0x7FFFFFFF;
        return DARK_PALETTE[hash % DARK_PALETTE.length];
    }

    /**
     * Returns the next auto-assigned color for an embedded circuit.
     * Cycles through the embedded palette so each new sub-circuit
     * gets a different colour.
     *
     * @return a vibrant color for the embedded circuit
     */
    public static Color nextEmbeddedColor() {
        Color c = EMBEDDED_PALETTE[embeddedCounter % EMBEDDED_PALETTE.length];
        embeddedCounter++;
        return c;
    }

    /**
     * Returns a color for an embedded circuit based on its name.
     * Deterministic — same name always gets the same color.
     *
     * @param circuitName the name of the embedded circuit
     * @return a vibrant color from the embedded palette
     */
    public static Color getColorForEmbedded(String circuitName) {
        if (!isDarkMode()) return null;
        if (circuitName == null || circuitName.isEmpty()) return EMBEDDED_PALETTE[0];
        int hash = circuitName.hashCode() & 0x7FFFFFFF;
        return EMBEDDED_PALETTE[hash % EMBEDDED_PALETTE.length];
    }

    /**
     * Returns the ideal text color (white or near-black) for maximum
     * readability on the given background color.
     *
     * @param bg the background color
     * @return Color.WHITE if the background is dark, dark gray if light
     */
    public static Color getContrastText(Color bg) {
        if (bg == null) return null;
        // W3C relative luminance formula
        double lum = 0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue();
        return lum < 160 ? Color.WHITE : new Color(30, 30, 30);
    }

    private static boolean isDarkMode() {
        Color bg = ColorScheme.getSelected().getColor(ColorKey.BACKGROUND);
        // If the background is dark, we're in dark mode
        return bg.getRed() < 100 && bg.getGreen() < 100 && bg.getBlue() < 100;
    }
}
