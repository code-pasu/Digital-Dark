# Digital Dark — Modern Dark-Themed Circuit Simulator

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://adoptium.net/)

A modern, dark-themed fork of [hneemann's Digital](https://github.com/hneemann/Digital) — an easy-to-use digital logic designer and circuit simulator for education.

![Dark Mode Screenshot](screenshots/dark-mode.png)

---

## What's New in This Fork

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

### 🖱️ Modern Toolbar
- Flat, dark toolbar with hover effects
- Styled status bar

---

## Download & Run

### Quick Start (Windows)
1. Download the latest release from the [Releases](../../releases) page
2. Unzip the `Digital-Dark.zip`
3. Double-click `Digital.exe` (or run `java -jar Digital.jar`)

### Requirements
- **Java 8** or later — [Download Eclipse Temurin](https://adoptium.net/)

### From Source
```bash
git clone https://github.com/YOUR_USERNAME/Digital-Dark.git
cd Digital-Dark
mvn package -Pno-git-rev -Dcheckstyle.skip=true -Denforcer.skip=true -DskipTests
java -jar target/Digital.jar
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

## Building

```bash
# Full build (requires Maven 3.x + JDK 8+)
mvn package -Pno-git-rev -Dcheckstyle.skip=true -Denforcer.skip=true

# Quick build (skip tests)
mvn package -Pno-git-rev -Dcheckstyle.skip=true -Denforcer.skip=true -DskipTests
```

The output JAR is at `target/Digital.jar`.

---

## Credits & License

- **Original author:** [Helmut Neemann](https://github.com/hneemann) — [Digital](https://github.com/hneemann/Digital)
- **License:** [GNU General Public License v3.0](LICENSE)
- **This fork** adds dark mode UI, modern styling, and UX enhancements

This is a modified version of Digital. Per GPL v3, all source code is provided and the original license is preserved.

---

## Upstream Features (Inherited)

All features from the original Digital are preserved:

- Signal visualization with measurement graphs
- Single gate mode for oscillation analysis
- Combinatorial and sequential circuit analysis/synthesis
- Test case editor and runner
- 74xx series IC library
- Generic (parameterized) circuits
- VHDL/Verilog export and simulation
- FPGA support (BASYS3, TinyFPGA BX)
- GAL16v8 / GAL22v10 JEDEC export
- Remote TCP interface for assembler IDEs
- Custom Java components via JAR plugins
