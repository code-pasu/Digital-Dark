/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.graphics.ColorKey;
import de.neemann.digital.draw.graphics.ColorScheme;
import de.neemann.digital.draw.graphics.ComponentColors;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.model.InverterConfig;

import java.awt.*;

/**
 * Universal Shape. Used for most components.
 * Shows a simple Box with inputs at the left and outputs at the right.
 */
public class GenericShape implements Shape {
    /**
     * Half the size of the used raster
     */
    public static final int SIZE2 = 10;
    /**
     * The size of the used raster
     */
    public static final int SIZE = SIZE2 * 2;

    private final String name;
    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private int width;
    private final boolean symmetric;
    private final String label;
    private final boolean showPinLabels;

    private boolean invert = false;
    private Color color = Color.WHITE;

    private Pins pins;
    private InverterConfig inverterConfig;
    private int topBottomBorder = SIZE2;

    /**
     * Creates a new generic shape.
     *
     * @param name    the name shown in or below the shape
     * @param inputs  the used inputs
     * @param outputs the used outputs
     */
    public GenericShape(String name, PinDescriptions inputs, PinDescriptions outputs) {
        this(name, inputs, outputs, null, false);
    }

    /**
     * Creates a new generic shape.
     *
     * @param name          the name shown in or below the shape
     * @param inputs        the used inputs
     * @param outputs       the used outputs
     * @param label         the label shown above the shape
     * @param showPinLabels true if pin names visible
     */
    public GenericShape(String name, PinDescriptions inputs, PinDescriptions outputs, String label, boolean showPinLabels) {
        this(name, inputs, outputs, label, showPinLabels, inputs.size() == 1 && outputs.size() == 1 && !showPinLabels ? 1 : 3);
    }

    /**
     * Creates a new generic shape.
     *
     * @param name          the name shown in or below the shape
     * @param inputs        the used inputs
     * @param outputs       the used outputs
     * @param label         the label shown above the shape
     * @param showPinLabels true if pin names visible
     * @param width         the width of the box
     */
    public GenericShape(String name, PinDescriptions inputs, PinDescriptions outputs, String label, boolean showPinLabels, int width) {
        this.name = name;
        this.inputs = inputs;
        this.outputs = outputs;
        if (label != null && label.length() == 0)
            label = null;
        this.label = label == null ? null : format(label);
        this.showPinLabels = showPinLabels;
        this.width = width;
        symmetric = outputs.size() == 1;
    }

    /**
     * Sets the invert flag.
     * If set to true a little circle at the output is shown.
     *
     * @param invert true is output is inverted
     * @return this for chaind calls
     */
    public GenericShape invert(boolean invert) {
        this.invert = invert;
        return this;
    }

    /**
     * Sets the top bottom border
     *
     * @param topBottomBorder the border
     * @return this for chaind calls
     */
    public GenericShape setTopBottomBorder(int topBottomBorder) {
        this.topBottomBorder = topBottomBorder;
        return this;
    }

    /**
     * Sets the background color
     *
     * @param color the color
     * @return this for chained calls
     */
    public GenericShape setColor(Color color) {
        if (color != null)
            this.color = color;
        return this;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = createPins(inputs, outputs, invert, width, symmetric, inverterConfig);
        }
        return pins;
    }

    /**
     * Creates pins
     *
     * @param inputs    the inputs
     * @param outputs   the outputs
     * @param invert    true if invert output
     * @param ic        input inverter configuration
     * @param wideShape true if a wide shape is selected
     * @return the pins
     */
    public static Pins createPins(PinDescriptions inputs, PinDescriptions outputs, boolean invert, InverterConfig ic, boolean wideShape) {
        return createPins(inputs, outputs, invert, wideShape ? 4 : 3, true, ic);
    }

    /**
     * Creates pins
     *
     * @param inputs    the inputs
     * @param outputs   the outputs
     * @param invert    true if invert output
     * @param width     with of symbol
     * @param symmetric true if outputs in the center
     * @param ic        iput inverter configuration
     * @return the pins
     */
    private static Pins createPins(PinDescriptions inputs, PinDescriptions outputs, boolean invert, int width, boolean symmetric, InverterConfig ic) {
        Pins pins = new Pins();

        int offs = symmetric ? inputs.size() / 2 * SIZE : 0;


        for (int i = 0; i < inputs.size(); i++) {
            int correct = 0;
            if (symmetric && ((inputs.size() & 1) == 0) && i >= inputs.size() / 2)
                correct = SIZE;

            int dx = 0;
            if (isInverted(inputs.get(i).getName(), ic))
                dx = -SIZE;

            pins.add(new Pin(new Vector(dx, i * SIZE + correct), inputs.get(i)));
        }


        if (invert) {
            for (int i = 0; i < outputs.size(); i++)
                pins.add(new Pin(new Vector(SIZE * (width + 1), i * SIZE + offs), outputs.get(i)));

        } else {
            for (int i = 0; i < outputs.size(); i++)
                pins.add(new Pin(new Vector(SIZE * width, i * SIZE + offs), outputs.get(i)));
        }

        return pins;
    }

    private static boolean isInverted(String name, InverterConfig ic) {
        return ic != null && ic.contains(name);
    }

    @Override
    public Interactor applyStateMonitor(IOState ioState) {
        return null;
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        int max = Math.max(inputs.size(), outputs.size());
        int yBottom = (max - 1) * SIZE + topBottomBorder;

        if (symmetric && inputs.size() > 0 && ((inputs.size() & 1) == 0)) yBottom += SIZE;

        Polygon polygon = new Polygon(true)
                .add(1, -topBottomBorder)
                .add(SIZE * width - 1, -topBottomBorder)
                .add(SIZE * width - 1, yBottom)
                .add(1, yBottom);

        // ── Resolve fill color ─────────────────────────────────────────
        Color fillColor = color;
        if (fillColor.equals(Color.WHITE)) {
            // No explicit color set → use per-component vibrant color in dark mode
            Color auto = ComponentColors.getColorForComponent(name);
            fillColor = (auto != null) ? auto : ColorScheme.getSelected().getColor(ColorKey.FILL);
        }
        boolean hasFill = !fillColor.equals(ColorScheme.getSelected().getColor(ColorKey.BACKGROUND));
        if (hasFill) {
            graphic.drawPolygon(polygon, Style.NORMAL.deriveFillStyle(fillColor));
        }
        // Border: use contrasting outline in dark mode if component is filled
        Style borderStyle = Style.NORMAL;
        if (hasFill) {
            Color borderColor = darker(fillColor, 0.55f);
            borderStyle = Style.NORMAL.deriveColor(borderColor);
        }
        graphic.drawPolygon(polygon, borderStyle);

        // ── Determine text style for on-component labels ─────────────
        Style componentTextStyle = Style.NORMAL;
        Style pinLabelStyle = Style.SHAPE_PIN;
        if (hasFill) {
            Color textColor = ComponentColors.getContrastText(fillColor);
            if (textColor != null) {
                componentTextStyle = Style.NORMAL.deriveColor(textColor);
                pinLabelStyle = Style.SHAPE_PIN.deriveColor(textColor);
            }
        }

        if (invert) {
            int offs = symmetric ? inputs.size() / 2 * SIZE : 0;
            for (int i = 0; i < outputs.size(); i++)
                graphic.drawCircle(new Vector(SIZE * width + 1, i * SIZE - SIZE2 + 1 + offs),
                        new Vector(SIZE * (width + 1) - 1, i * SIZE + SIZE2 - 1 + offs), borderStyle);

        }

        if (label != null) {
            Vector pos = new Vector(SIZE2 * width, -topBottomBorder - 8);
            graphic.drawText(pos, label, Orientation.CENTERBOTTOM, Style.NORMAL);
        }

        if (showPinLabels) {
            for (Pin p : getPins()) {
                int dx = 4;
                if (isInverted(p.getName(), inverterConfig))
                    dx += SIZE;
                if (p.getDirection() == Pin.Direction.input) {
                    if (p.isClock()) {
                        final int triangle = SIZE2 / 2 + 2;
                        graphic.drawPolygon(new Polygon(false)
                                .add(p.getPos().add(dx - 3, triangle))
                                .add(p.getPos().add(dx + triangle - 3, 0))
                                .add(p.getPos().add(dx - 3, -triangle)), Style.THIN);
                        dx += triangle;
                    }
                    graphic.drawText(p.getPos().add(dx, 0), format(p.getName()), Orientation.LEFTCENTER, pinLabelStyle);
                } else
                    graphic.drawText(p.getPos().add(-4, 0), format(p.getName()), Orientation.RIGHTCENTER, pinLabelStyle);
            }
        }
        if (name.length() > 0) {
            // Use smaller font for component labels so they fit inside the box
            Style smallTextStyle = componentTextStyle.deriveFontStyle(18, false);
            if (name.length() <= 3 && !showPinLabels) {
                Vector pos = new Vector(SIZE2 * width, -topBottomBorder + 4);
                graphic.drawText(pos, name, Orientation.CENTERTOP, smallTextStyle);
            } else {
                Style smallPinStyle = componentTextStyle.deriveFontStyle(14, false);
                Vector pos = new Vector(SIZE2 * width, yBottom + 4);
                graphic.drawText(pos, name, Orientation.CENTERTOP, smallPinStyle);
            }
        }

        drawInputInvert(graphic, inverterConfig, getPins());
    }

    /**
     * Draw the inverted inputs
     *
     * @param graphic        the graphic to paint on
     * @param inverterConfig the inverter configuration
     * @param pins           the pins containing the inputs
     */
    public static void drawInputInvert(Graphic graphic, InverterConfig inverterConfig, Pins pins) {
        if (inverterConfig != null && !inverterConfig.isEmpty())
            for (Pin p : pins) {
                if (p.getDirection() == Pin.Direction.input) {
                    if (inverterConfig.contains(p.getName())) {
                        graphic.drawCircle(p.getPos().add(2, -SIZE2 + 2),
                                p.getPos().add(SIZE - 2, SIZE2 - 2), Style.NORMAL);
                    }
                }
            }
    }

    /**
     * Sets the inverter config
     *
     * @param inverterConfig the inverter config
     * @return this for chained calls
     */
    public GenericShape setInverterConfig(InverterConfig inverterConfig) {
        if (inverterConfig.isEmpty())
            this.inverterConfig = null;
        else
            this.inverterConfig = inverterConfig;
        return this;
    }

    /**
     * Selects a wide shape.
     *
     * @param wideShape true is a wide shape is selected
     * @return this for chained calls
     */
    public GenericShape setWide(boolean wideShape) {
        if (wideShape)
            width += 1;
        return this;
    }

    /**
     * Formats the component specific strings.
     * This implementation returns the given string unmodified.
     *
     * @param s the string to format
     * @return the formatted string
     */
    public String format(String s) {
        return s;
    }

    /**
     * Returns a darker shade of the given color.
     *
     * @param c      the original color
     * @param factor the brightness factor (0.0 = black, 1.0 = unchanged)
     * @return the darker color
     */
    private static Color darker(Color c, float factor) {
        return new Color(
                Math.max((int) (c.getRed() * factor), 0),
                Math.max((int) (c.getGreen() * factor), 0),
                Math.max((int) (c.getBlue() * factor), 0),
                c.getAlpha());
    }
}
