# Chunk Mirroring Mod

**Minecraft:** 1.21.1  
**Loader:** Forge 52.1.14  

## What It Does

Every block you place or break is mirrored to the **exact same chunk-local coordinates** in every other **player-affected chunk** that is currently loaded.

- **Block place** — the placed block is copied to position (localX, Y, localZ) inside every other player-affected loaded chunk, replacing whatever was there.
- **Block break** — the block at that chunk-local position is destroyed (replaced with air) in every other player-affected loaded chunk.
- **Block interaction** — right-clicking an interactive block (lever, door, button, trapdoor, etc.) mirrors the resulting block state change to the same position in every other player-affected loaded chunk. Only fires if the block state actually changed.
- **Explosions** — when a TNT, bed, or any other explosion destroys blocks, every destroyed block position is mirrored as air in all other player-affected loaded chunks.
- **Container inventory** — when a player places items into a chest, barrel, furnace, hopper, or any other single-block container, every item change is immediately copied to the matching block entity at the same chunk-local position in every other player-affected loaded chunk.

### What counts as "player-affected"?

A chunk is considered player-affected the moment any player places or breaks a block inside it. Pure world-generation chunks (untouched since the world was created) are completely ignored — the mod will never mirror into them. The list is saved and persists across server restarts.

---

## How to get the JAR (no installs needed — works in any browser)

This is the easiest method and works on Chromebooks, school computers, or anything with a browser.

### Step 1 — Create a free GitHub account
Go to **https://github.com** and sign up (it's free).

### Step 2 — Create a new repository
1. Click the **+** button (top right) → **New repository**
2. Name it anything, e.g. `my-mod`
3. Set it to **Public**
4. Click **Create repository**

### Step 3 — Upload the mod files
1. On your new repository page, click **uploading an existing file**
2. Drag and drop **all the files and folders** from the extracted `chunkmirroring-mod` folder
   - Make sure `.github/workflows/build.yml` is included
3. Click **Commit changes**

### Step 4 — Watch it build
1. Click the **Actions** tab at the top of your repository
2. You'll see a workflow called **Build Mod** running (yellow circle = in progress)
3. Wait for it to turn green (takes 5–15 minutes — Forge is large)

### Step 5 — Download your JAR
1. Click the completed workflow run
2. Scroll down to **Artifacts**
3. Click **Definitly-not-a-virus** to download a zip
4. Extract the zip — inside is **`Definitly not a virus-1.0.0.jar`**

### Step 6 — Install
1. Install **Forge 52.1.14** for Minecraft 1.21.1 from https://files.minecraftforge.net
2. Drop the jar into `.minecraft/mods/`
3. Launch Minecraft with the Forge profile

---

## Building locally (if you have Java 21)

```bash
# Linux/Mac
./gradlew build

# Windows
gradlew.bat build
```

Output: `build/libs/Definitly not a virus-1.0.0.jar`
