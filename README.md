# VoidUI

A client-side UI reskin mod for Minecraft Forge 1.20.1 by Voidlessstar. Replaces the vanilla title/logo screens, splash, HUD hotbar, health/food bars, and window icon with a custom VoidUI style.

## Features

- Custom main-menu logo and splash renderer
- Custom loading screen and connect screen styling
- Styled pause screen with custom buttons
- HUD tweaks: hotbar slide behavior and custom health/food bar animations
- Custom window icon and mod credits screen
- Bundled Noto Sans Thai font support for Thai text rendering
- Glass texture pack assets included

## Requirements

- Minecraft 1.20.1
- Minecraft Forge 47.3.0
- Java 17

Bundled dependencies (jar-in-jar):
- Caxton 0.6.4
- BiaoHealthBarX 0.1.0

## Building

```bash
./gradlew build
```

Output jar: `build/libs/VoidUI-forge-1.20.1-1.0.0.jar`

## Running (dev)

```bash
./gradlew runClient
```

## Installation

Drop the built jar into your `mods` folder alongside Forge 1.20.1. Client-side only.

## Credits

Made by Voidlessstar. Logo assets in repo root.
