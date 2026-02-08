# Digital Dark v1.0.0 — Release Notes

**Release Date:** February 8, 2026  
**Platform:** Windows, macOS, Linux (Java 8+)  
**Download:** [Digital.jar](release/Digital.jar) | [Digital.exe](release/Digital.exe) (Windows)

---

## 🚀 First Release

Digital Dark is a modern, dark-themed digital logic designer and circuit simulator for education. It builds upon [Helmut Neemann's Digital](https://github.com/hneemann/Digital), adding a sleek dark interface, vibrant component coloring, and quality-of-life improvements for a better design experience.

---

## ✨ Highlights

### 🌙 Always-On Dark Mode
A custom Metal Look & Feel theme provides a fully dark interface across every window — the main canvas, dialogs, tables, Karnaugh maps, menus, tooltips, and popups. No toggle needed, no eye strain.

### 🎨 Vibrant Component Colors
Each component type gets its own color from a hand-picked 16-color palette, making complex circuits easier to read at a glance. Wire state colors are vivid: **bright green** for HIGH, **dark red** for LOW, **gray** for Hi-Z. Pin dots change color to match the live signal state during simulation.

### 🔍 Spotlight Search (F2)
Press **F2** anywhere to open a floating search popup. Type a few letters to fuzzy-find any component in the library, press **Enter**, and it attaches to your cursor — ready to place. Also accessible via the 🔍 toolbar button.

### ⌨️ Keyboard Shortcuts Editor
Open **Edit → Keyboard Shortcuts...** to view all 34 configurable shortcuts in a clean table. Click any shortcut cell, press a new key combo, and it's rebound instantly. Your customizations persist to `~/.digital-shortcuts.cfg`.

### 🔤 Configurable Component Font Size
Go to **Edit → Settings → Component Label Font Size** and pick any size from 6pt to 48pt. Changes apply live — no restart required.

### 🖱️ Shift-Click Multi-Select
Hold **Shift** and click on components or wires to build up a selection. Delete all selected items at once with **Del** or **Backspace**.

### 🛈 Shortcut-Aware Tooltips
Every toolbar button tooltip now shows its keyboard shortcut (e.g., "Undo (Ctrl+Z)"), so you can learn keybindings naturally as you work.

### 🎛️ Modern Toolbar
Flat, dark toolbar buttons with hover highlights and a styled status bar replace the default Swing look.

---

## 📦 What's Included

| File | Description |
|------|-------------|
| `Digital.jar` | Fat JAR — runs on any OS with Java 8+ |
| `Digital.exe` | Windows launcher (wraps the JAR) |
| `examples/` | Example circuits (74xx, CMOS, combinatorial, sequential, processors) |
| `lib/` | Component libraries (DIL Chips, RAMs) |

---

## 🔧 Build from Source

```bash
git clone https://github.com/code-pasu/Digital-Dark.git
cd Digital-Dark
mvn package -Pno-git-rev -Dcheckstyle.skip=true -Denforcer.skip=true -DskipTests
java -jar target/Digital.jar
```

**Requirements:** JDK 8+, Maven 3.6+

---

## 📋 Full Change List

### New Files
- `ThemeManager.java` — Central dark theme installer (custom Metal L&F)
- `ModernToolbar.java` — Flat dark toolbar with hover effects
- `ComponentColors.java` — 16-color deterministic palette for components
- `ComponentSearchDialog.java` — F2 spotlight search popup
- `AutocompleteSearchField.java` — Sidebar search with JWindow dropdown
- `ShortcutDialog.java` — Keyboard shortcut configuration dialog
- `MouseShortcuts.java` — Enhanced mouse interaction handling

### Modified Files
- `Main.java` — Integrated all new features, menu items, and startup wiring
- `ToolTipAction.java` — HTML tooltips with dark-safe colors and accelerator display
- `CircuitComponent.java` — Shift-click multi-select for components and wires
- `VisualElement.java` — Pin dots reflect live signal state via ioState
- `Style.java` — Mutable SHAPE_PIN, runtime font size updates
- `GenericShape.java / LayoutShape.java / ShapeFactory.java` — Per-component fill colors
- `ColorScheme.java` — Reworked DARK palette
- `LineBreaker.java` — Dark-mode-safe HTML tooltip text color injection
- `TableDialog.java` — Dark-aware cell renderers
- `KarnaughMapComponent.java` — Dark-aware drawing
- `Keys.java / Settings.java` — Component font size setting
- `OutputShape.java` — Fixed output dots ignoring signal state
- Language resources — New strings for search, shortcuts, settings

### Bug Fixes
- `ComponentColors.java` — Fixed `Math.abs(Integer.MIN_VALUE)` overflow → bitwise mask
- `ShortcutDialog.java` — Removed 3 entries referencing non-existent shortcuts
- Tooltip text invisible in dark mode → fixed in both `LineBreaker` and `ToolTipAction`
- Dropdown component icons oversized (75px) → scaled to 24px

---

## 🙏 Acknowledgments

Digital Dark builds upon the excellent [Digital](https://github.com/hneemann/Digital) simulator by **Helmut Neemann**. All original features are preserved. Licensed under **GPL v3**.

---

*Made with ☕ by [code-pasu](https://github.com/code-pasu)*
