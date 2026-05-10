package com.chunkmirroring.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockEventHandler {

    /**
     * Fired after a player places a block.
     * Records the chunk as player-affected and mirrors the placed block
     * to the same local position in all other player-affected loaded chunks.
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

        mirrorToPlayerChunks(level, data, originChunk, localX, origin.getY(), localZ, placedState, false);
    }

    /**
     * Fired after a player breaks a block.
     * Records the chunk as player-affected and mirrors the destruction
     * to the same local position in all other player-affected loaded chunks.
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

        mirrorToPlayerChunks(level, data, originChunk, localX, origin.getY(), localZ, null, true);
    }

    /**
     * Fired when a player right-clicks a block (lever, door, button, trapdoor, etc.).
     * Captures the block state before the interaction, defers a check to the next
     * server task, and mirrors the new state if it changed.
     */
    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockPos origin = event.getPos();
        BlockState stateBefore = level.getBlockState(origin);

        level.getServer().execute(() -> {
            BlockState stateAfter = level.getBlockState(origin);
            if (stateAfter.equals(stateBefore)) return;

            ChunkPos originChunk = new ChunkPos(origin);
            PlayerChunkData data = PlayerChunkData.get(level);
            data.markAffected(originChunk);

            int localX = origin.getX() - (originChunk.x << 4);
            int localZ = origin.getZ() - (originChunk.z << 4);

            mirrorToPlayerChunks(level, data, originChunk, localX, origin.getY(), localZ, stateAfter, false);
        });
    }

    /**
     * Fired when an explosion detonates (TNT, bed in wrong dimension, etc.).
     *
     * For each block the explosion destroys, marks its chunk as player-affected
     * and mirrors that destruction to the same local position in all other
     * player-affected loaded chunks. The explosion's block list is cleared from
     * non-player-affected chunks so they are not blown up (Minecraft will still
     * deal damage / fire to the area; only the mirrored block destruction matters).
     */
    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        List<BlockPos> affected = event.getAffectedBlocks();
        if (affected.isEmpty()) return;

        PlayerChunkData data = PlayerChunkData.get(level);

        for (BlockPos pos : affected) {
            data.markAffected(new ChunkPos(pos));
        }

        for (BlockPos pos : affected) {
            ChunkPos originChunk = new ChunkPos(pos);
            int localX = pos.getX() - (originChunk.x << 4);
            int localZ = pos.getZ() - (originChunk.z << 4);

            mirrorToPlayerChunks(level, data, originChunk, localX, pos.getY(), localZ, null, true);
        }
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    /**
     * Mirrors a block action only to loaded chunks that a player has previously
     * modified. Pure world-generation chunks are skipped entirely.
     */
    static void mirrorToPlayerChunks(ServerLevel level, PlayerChunkData data,
                                     ChunkPos originChunk,
                                     int localX, int y, int localZ,
                                     BlockState state, boolean isBreak) {

        if (y < level.getMinBuildHeight() || y > level.getMaxBuildHeight()) return;

        for (ChunkPos chunkPos : getLoadedPlayerChunks(level, data, originChunk)) {
            int worldX = (chunkPos.x << 4) + localX;
            int worldZ = (chunkPos.z << 4) + localZ;
            BlockPos targetPos = new BlockPos(worldX, y, worldZ);

            if (isBreak) {
                level.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
            } else if (state != null) {
                level.setBlock(targetPos, state, 3);
            }
        }
    }

    /**
     * Returns all loaded chunks that are player-affected and are not the origin chunk.
     */
    static List<ChunkPos> getLoadedPlayerChunks(ServerLevel level,
                                                 PlayerChunkData data,
                                                 ChunkPos originChunk) {
        List<ChunkPos> result = new ArrayList<>();
        level.getChunkSource().chunkMap.getChunks().forEach(holder -> {
            LevelChunk chunk = holder.getTickingChunk();
            if (chunk == null) return;
            ChunkPos pos = chunk.getPos();
            if (pos.x == originChunk.x && pos.z == originChunk.z) return;
            if (data.isAffected(pos)) result.add(pos);
        });
        return result;
    }
}
