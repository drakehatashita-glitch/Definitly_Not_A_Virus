# Chunk Mirroring Mod

**Minecraft:** 1.21.1  
**Loader:** Forge 47.3.0  

## What It Does

Every block you place or break is mirrored to the **exact same chunk-local coordinates** in every other **player-affected chunk** that is currently loaded.

- **Block place** — the placed block is copied to position (localX, Y, localZ) inside every other player-affected loaded chunk, replacing whatever was there.
- **Block break** — the block at that chunk-local position is destroyed (replaced with air) in every other player-affected loaded chunk.
- **Block interaction** — right-clicking an interactive block (lever, door, button, trapdoor, etc.) mirrors the resulting block state change to the same position in every other player-affected loaded chunk. Only fires if the block state actually changed.

### What counts as "player-affected"?

A chunk is considered player-affected the moment any player places or breaks a block inside it. Pure world-generation chunks (untouched since the world was created) are completely ignored — the mod will never mirror into them.

The list of player-affected chunks is saved to the world's data folder (`data/chunkmirroring_affected.dat`) and persists across server restarts.

### Example

You place a stone block at world position X=18, Z=5, Y=64.  
That is chunk (1, 0), local X=2, Z=5.  
The mod places stone at local X=2, Z=5, Y=64 inside every **other loaded chunk that a player has previously modified**. Untouched generated chunks are left alone.

## Building

### Prerequisites

- Java 21 (JDK)
- Internet connection (Gradle downloads Forge and mappings on first run)

### Steps

```bash
cd chunkmirroring-mod

# On Linux/macOS
./gradlew build

# On Windows
gradlew.bat build
```

The compiled `.jar` will be in:
```
build/libs/chunkmirroring-1.0.0.jar
```

### Getting the Gradle wrapper

If `gradlew` is missing, run:
```bash
gradle wrapper --gradle-version 8.8
```
or download it from https://gradle.org/install/

## Installation

1. Install **Minecraft Forge 47.3.0** for Minecraft 1.21.1 from https://files.minecraftforge.net
2. Copy `chunkmirroring-1.0.0.jar` into your `.minecraft/mods/` folder.
3. Launch Minecraft with the Forge profile.

## Notes

- Only works **server-side** — effects apply on the server (or in singleplayer where client = server).
- Only mirrors to **player-affected** chunks. World-generated chunks are never touched.
- Only affects **currently loaded** player-affected chunks. Unloaded chunks are skipped.
- If a different block already exists at the mirrored position, it is **replaced** (place) or **removed** (break) without dropping items.
- This mod is intended for creative/experimental use.
