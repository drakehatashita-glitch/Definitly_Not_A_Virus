# Chunk Mirroring Mod

**Minecraft:** 1.21.1  
**Loader:** Forge 47.3.0  

## What It Does

Every block you place or break is mirrored to the **exact same chunk-local coordinates** in every other loaded chunk simultaneously.

- **Block place** — the placed block is copied to position (localX, Y, localZ) inside every other loaded chunk, replacing whatever was there.
- **Block break** — the block at that chunk-local position is destroyed (replaced with air) in every other loaded chunk.

"Chunk-local" means the position within the 16×16 footprint of the chunk. For example, if you place a block at world coordinate X=18, Z=5, that is local X=2, Z=5 inside chunk (1, 0). The mod will place the same block at local X=2, Z=5 (same Y) inside every other chunk that is currently loaded.

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
- Mirroring only affects **currently loaded chunks**. Chunks that are not loaded are unaffected.
- If a different block already exists at the mirrored position, it is **replaced** (place) or **removed** (break) without dropping items.
- This mod is intended for creative/experimental use.
