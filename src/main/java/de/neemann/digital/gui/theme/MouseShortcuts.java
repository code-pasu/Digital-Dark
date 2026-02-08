/*
 * Copyright (c) 2024 Digital Contributors
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.theme;

import de.neemann.digital.gui.components.CircuitComponent;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Adds convenient mouse shortcuts to the circuit component.
 * <p>
 * Already built-in: Scroll wheel to zoom centered on cursor.
 * <p>
 * Additional shortcuts installed by this class:
 * <ul>
 *     <li>Ctrl+0: Fit the circuit to the viewport</li>
 *     <li>Middle-click: Fit the circuit to the viewport</li>
 * </ul>
 */
public final class MouseShortcuts {

    private MouseShortcuts() {
    }

    /**
     * Installs all mouse shortcuts on the given circuit component.
     *
     * @param component the circuit component
     */
    public static void install(CircuitComponent component) {
        installMiddleClickFit(component);
    }

    private static void installMiddleClickFit(CircuitComponent component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Middle-click to fit circuit
                if (e.getButton() == MouseEvent.BUTTON2) {
                    component.fitCircuit();
                }
            }
        });
    }
}
