package com.chunkmirroring.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockEventHandler {

    /**
     * Fired after a player places a block.
     * Mirrors the placed block state to the same chunk-local (x, y, z) coordinates
     * in every other loaded chunk.
     */
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos origin = event.getPos();
        BlockState placedState = level.getBlockState(origin);

        ChunkPos originChunk = new ChunkPos(origin);

        int localX = origin.getX() - (originChunk.x << 4);
        int localZ = origin.getZ() - (originChunk.z << 4);
        int y = origin.getY();

        mirrorToAllChunks(level, originChunk, localX, y, localZ, placedState, false);
    }

    /**
     * Fired after a player breaks a block.
     * Mirrors the block destruction (replaces with air) to the same chunk-local coordinates
     * in every other loaded chunk.
     */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos origin = event.getPos();
        ChunkPos originChunk = new ChunkPos(origin);

        int localX = origin.getX() - (originChunk.x << 4);
        int localZ = origin.getZ() - (originChunk.z << 4);
        int y = origin.getY();

        mirrorToAllChunks(level, originChunk, localX, y, localZ, null, true);
    }

    /**
     * Iterates over all loaded chunks in the level and mirrors the block action.
     *
     * @param level        The server world
     * @param originChunk  The chunk the player acted in (excluded from mirroring)
     * @param localX       Chunk-local X (0–15)
     * @param y            Absolute Y coordinate
     * @param localZ       Chunk-local Z (0–15)
     * @param state        Block state to place, or null when breaking
     * @param isBreak      True if the action is a block break (place air)
     */
    private void mirrorToAllChunks(ServerLevel level, ChunkPos originChunk,
                                   int localX, int y, int localZ,
                                   BlockState state, boolean isBreak) {

        List<ChunkPos> loadedChunks = getLoadedChunks(level);

        for (ChunkPos chunkPos : loadedChunks) {
            if (chunkPos.x == originChunk.x && chunkPos.z == originChunk.z) {
                continue;
            }

            int worldX = (chunkPos.x << 4) + localX;
            int worldZ = (chunkPos.z << 4) + localZ;

            if (y < level.getMinBuildHeight() || y > level.getMaxBuildHeight()) {
                continue;
            }

            BlockPos targetPos = new BlockPos(worldX, y, worldZ);

            if (isBreak) {
                BlockState air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
                level.setBlock(targetPos, air, 3);
            } else {
                if (state != null) {
                    level.setBlock(targetPos, state, 3);
                }
            }
        }
    }

    /**
     * Collects all currently loaded chunk positions in the given server level.
     */
    private List<ChunkPos> getLoadedChunks(ServerLevel level) {
        List<ChunkPos> result = new ArrayList<>();
        level.getChunkSource().chunkMap.getChunks().forEach(holder -> {
            LevelChunk chunk = holder.getTickingChunk();
            if (chunk != null) {
                result.add(chunk.getPos());
            }
        });
        return result;
    }
}
