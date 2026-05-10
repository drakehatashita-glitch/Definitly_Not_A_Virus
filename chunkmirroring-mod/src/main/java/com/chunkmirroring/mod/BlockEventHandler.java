package com.chunkmirroring.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockEventHandler {

    /**
     * Fired after a player places a block.
     *
     * 1. Records the origin chunk as player-affected.
     * 2. Mirrors the placed block to the same chunk-local position in every
     *    other loaded chunk that a player has previously modified.
     */
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos origin = event.getPos();
        ChunkPos originChunk = new ChunkPos(origin);

        PlayerChunkData data = PlayerChunkData.get(level);
        data.markAffected(originChunk);

        BlockState placedState = level.getBlockState(origin);

        int localX = origin.getX() - (originChunk.x << 4);
        int localZ = origin.getZ() - (originChunk.z << 4);
        int y = origin.getY();

        mirrorToPlayerChunks(level, data, originChunk, localX, y, localZ, placedState, false);
    }

    /**
     * Fired after a player breaks a block.
     *
     * 1. Records the origin chunk as player-affected.
     * 2. Mirrors the block removal to the same chunk-local position in every
     *    other loaded chunk that a player has previously modified.
     */
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos origin = event.getPos();
        ChunkPos originChunk = new ChunkPos(origin);

        PlayerChunkData data = PlayerChunkData.get(level);
        data.markAffected(originChunk);

        int localX = origin.getX() - (originChunk.x << 4);
        int localZ = origin.getZ() - (originChunk.z << 4);
        int y = origin.getY();

        mirrorToPlayerChunks(level, data, originChunk, localX, y, localZ, null, true);
    }

    /**
     * Mirrors a block action only to loaded chunks that a player has
     * previously placed or broken a block in.
     * Pure world-generation chunks are skipped entirely.
     */
    private void mirrorToPlayerChunks(ServerLevel level, PlayerChunkData data,
                                      ChunkPos originChunk,
                                      int localX, int y, int localZ,
                                      BlockState state, boolean isBreak) {

        if (y < level.getMinBuildHeight() || y > level.getMaxBuildHeight()) return;

        List<ChunkPos> candidates = getLoadedPlayerChunks(level, data, originChunk);

        for (ChunkPos chunkPos : candidates) {
            int worldX = (chunkPos.x << 4) + localX;
            int worldZ = (chunkPos.z << 4) + localZ;
            BlockPos targetPos = new BlockPos(worldX, y, worldZ);

            if (isBreak) {
                level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
            } else {
                if (state != null) {
                    level.setBlock(targetPos, state, 3);
                }
            }
        }
    }

    /**
     * Returns all currently loaded chunks that:
     *  - are NOT the origin chunk, and
     *  - have been previously touched by a player (tracked in PlayerChunkData).
     */
    private List<ChunkPos> getLoadedPlayerChunks(ServerLevel level,
                                                  PlayerChunkData data,
                                                  ChunkPos originChunk) {
        List<ChunkPos> result = new ArrayList<>();
        level.getChunkSource().chunkMap.getChunks().forEach(holder -> {
            LevelChunk chunk = holder.getTickingChunk();
            if (chunk == null) return;

            ChunkPos pos = chunk.getPos();

            if (pos.x == originChunk.x && pos.z == originChunk.z) return;

            if (data.isAffected(pos)) {
                result.add(pos);
            }
        });
        return result;
    }
}
