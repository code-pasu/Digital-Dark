# Changelog — Digital Dark

All notable changes from the [upstream Digital](https://github.com/hneemann/Digital) are documented here.

## [1.0.0] — 2026-02-08

### Added
- **Dark Mode Theme** — Custom `DefaultMetalTheme` subclass with full dark palette; always-on (no toggle needed)
- **ThemeManager** (`gui/theme/ThemeManager.java`) — Central dark mode management, installs custom Metal L&F
- **ModernToolbar** (`gui/theme/ModernToolbar.java`) — Flat dark toolbar with hover effects and styled status bar
- **ComponentColors** (`draw/shapes/ComponentColors.java`) — 16-color vibrant palette for per-component coloring
- **ComponentSearchDialog** (`gui/ComponentSearchDialog.java`) — Spotlight-style F2 popup for finding and inserting components
- **AutocompleteSearchField** (`gui/AutocompleteSearchField.java`) — Sidebar search with JWindow dropdown
- **ShortcutDialog** (`gui/ShortcutDialog.java`) — Full keyboard shortcut configuration dialog (Edit menu)
- **MouseShortcuts** (`gui/MouseShortcuts.java`) — Enhanced mouse interaction handling
- **Component Font Size Setting** — `SETTINGS_COMPONENT_FONT_SIZE` key in Settings, adjustable 6–48pt

### Changed
- **ColorScheme.DARK** — Completely reworked dark palette: dark background, bright wires, vivid signal colors
- **GenericShape / LayoutShape / ShapeFactory** — Per-component fill colors from ComponentColors palette
- **VisualElement.drawShape()** — Pin dots now reflect live signal state (bright red for HIGH, dark for LOW)
- **Style.SHAPE_PIN** — Made mutable; font size configurable from Settings at runtime
- **TableDialog** — Dark-mode aware cell renderers (background, foreground, selection colors)
- **KarnaughMapComponent** — Dark-mode aware drawing (no more white backgrounds)
- **LedMatrixComponent / GraphicComponent** — Dark mode compatibility fixes
- **OutputShape** — Fixed output dots always showing as red regardless of state
- **Wire dots** — Now use signal-state colors during simulation
- **Main.java** — Integrated all new features: F2 search, toolbar button, settings wiring, menu items




