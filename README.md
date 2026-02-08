# Digital Dark — Modern Dark-Themed Circuit Simulator

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://adoptium.net/)

A modern, dark-themed digital logic designer and circuit simulator for education. Built on [Helmut Neemann's Digital](https://github.com/hneemann/Digital), reimagined with a sleek dark UI, vibrant component colors, and quality-of-life improvements.

---

## ✨ Features

### 🌙 Dark Mode (Always On)
- Custom Metal L&F theme with a carefully tuned dark palette
- All dialogs, tables, Karnaugh maps, and menus are dark-mode aware
- No more eye-strain during late-night lab sessions

### 🎨 Vibrant Multi-Color Components
- 16-color palette assigns a unique color to each component type
- White contrast text for readability
- Wire state colors: **bright red** (HIGH), **dark maroon** (LOW), **gray** (Hi-Z)
- Pin connection dots change color to match signal state during simulation

### 🔍 Spotlight Component Search (F2)
- Press **F2** to open a spotlight-style search popup
- Type to fuzzy-find any component, press Enter to attach it to your cursor
- Also available via the 🔍 toolbar button

### ⌨️ Keyboard Shortcuts Editor
- **Edit → Keyboard Shortcuts...** opens a full shortcut configuration dialog
- View all shortcuts, click any cell and press a key combo to rebind
- Persists to `~/.digital-shortcuts.cfg`

### 🔤 Configurable Component Font Size
- **Edit → Settings → Component Label Font Size**
- Choose from 6–48pt, live preview without restart

### 🖱️ Shift-Click Multi-Select
- Hold **Shift** and click components/wires to select multiple items
- Delete all selected items at once

### 🎛️ Modern Toolbar
- Flat, dark toolbar with hover effects
- Tooltips display keyboard shortcuts
- Styled status bar

### 📦 Full Simulator Features (Inherited from Digital)
- Signal visualization with measurement graphs
- Combinatorial and sequential circuit analysis/synthesis
- Test case editor and runner
- 74xx series IC library & generic (parameterized) circuits
- VHDL/Verilog export and simulation
- FPGA support (BASYS3, TinyFPGA BX)
- GAL16v8 / GAL22v10 JEDEC export
- Remote TCP interface for assembler IDEs
- Custom Java components via JAR plugins

---

## Download & Run

### Quick Start (Windows)
1. Download the latest release from the [Releases](../../releases) page
2. Unzip the `Digital-Dark.zip`
3. Double-click `Digital.exe` (or run `java -jar Digital.jar`)

### Requirements
- **Java 8** or later — [Download Eclipse Temurin](https://adoptium.net/)

### Build from Source
```bash
git clone https://github.com/code-pasu/Digital-Dark.git

```

---

## Project Structure

```
Digital-Dark/
├── README.md              ← You are here
├── LICENSE                ← GPL v3 (same as upstream)
├── CHANGELOG.md           ← What changed from upstream
├── CONTRIBUTING.md         ← How to contribute
├── pom.xml                ← Maven build file
├── src/                   ← Full Java source
│   ├── main/java/...      ← Application code
│   ├── main/resources/... ← Languages, icons, settings
│   └── test/...           ← Tests
├── release/               ← Pre-built release (ready to run)
│   ├── Digital.jar        ← Fat JAR
│   ├── Digital.exe        ← Windows launcher
│   ├── examples/          ← Example circuits
│   └── lib/               ← Component libraries (74xx, etc.)
└── screenshots/           ← Screenshots for README
```

---


## Credits & License

- **Based on:** [Digital](https://github.com/hneemann/Digital) by [Helmut Neemann](https://github.com/hneemann)
- **Dark theme & UI enhancements by:** [code-pasu](https://github.com/code-pasu)
- **License:** [GNU General Public License v3.0](LICENSE)

This project builds upon the original Digital simulator. Per GPL v3, all source code is provided and the original license is preserved. See [NOTICE.md](NOTICE.md) for full attribution.
