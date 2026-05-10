package com.chunkmirroring.mod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Persists the set of chunk positions that a player has ever placed or broken
 * a block in. Stored in the world's data folder as "chunkmirroring_affected".
 */
public class PlayerChunkData extends SavedData {

    private static final String DATA_NAME = "chunkmirroring_affected";
    private static final String TAG_CHUNKS = "chunks";

    private final Set<Long> affectedChunks = new HashSet<>();

    public PlayerChunkData() {}

    public static PlayerChunkData load(CompoundTag tag) {
        PlayerChunkData data = new PlayerChunkData();
        ListTag list = tag.getList(TAG_CHUNKS, Tag.TAG_LONG);
        for (Tag entry : list) {
            data.affectedChunks.add(((LongTag) entry).getAsLong());
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (long packed : affectedChunks) {
            list.add(LongTag.valueOf(packed));
        }
        tag.put(TAG_CHUNKS, list);
        return tag;
    }

    /** Marks a chunk as player-affected and schedules a save. */
    public void markAffected(ChunkPos pos) {
        if (affectedChunks.add(ChunkPos.asLong(pos.x, pos.z))) {
            setDirty();
        }
    }

    /** Returns true if the chunk has ever been touched by a player. */
    public boolean isAffected(ChunkPos pos) {
        return affectedChunks.contains(ChunkPos.asLong(pos.x, pos.z));
    }

    /** Returns an unmodifiable view of all tracked chunk positions as packed longs. */
    public Set<Long> getAffectedChunks() {
        return Collections.unmodifiableSet(affectedChunks);
    }

    /** Retrieves (or creates) the SavedData instance for the given level. */
    public static PlayerChunkData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                PlayerChunkData::load,
                PlayerChunkData::new,
                DATA_NAME
        );
    }
}
