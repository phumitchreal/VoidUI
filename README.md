<p align="center">
  <img src="logo.png" alt="VoidUI Logo" width="400">
</p>

<h1 align="center">VoidUI</h1>

<p align="center">
  A client-side UI reskin mod for Minecraft Forge 1.20.1<br>
  by <b>Voidlessstar</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.20.1-62B47A?style=for-the-badge&logo=minecraft">
  <img src="https://img.shields.io/badge/Forge-47.3.0-F16436?style=for-the-badge">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk">
  <img src="https://img.shields.io/badge/Side-Client-9b59b6?style=for-the-badge">
</p>

---

## Features

| Area | What it does |
|------|--------------|
| Title screen | Custom logo, wordmark, and splash renderer |
| Loading & connect | Styled loading overlay and connect screen |
| Pause screen | Custom buttons and layout |
| HUD | Hotbar slide behavior, animated health/food bars |
| Window | Custom icon and mod credits screen |
| Font | Bundled Noto Sans Thai for Thai text |
| Textures | Glass texture pack assets included |

## Requirements

- Minecraft 1.20.1
- Minecraft Forge 47.3.0
- Java 17

Bundled (jar-in-jar): Caxton 0.6.4, BiaoHealthBarX 0.1.0

## Installation

1. Install Minecraft Forge 1.20.1 (47.3.0).
2. Drop the jar into your `mods` folder.
3. Launch the game — client-side only.

```text
mods/
└── VoidUI-forge-1.20.1-1.0.0.jar
```

## Building from source

```bash
./gradlew build
```

Output: `build/libs/VoidUI-forge-1.20.1-1.0.0.jar`

## Dev run

```bash
./gradlew runClient
```

## Credits

Made by **Voidlessstar**.
