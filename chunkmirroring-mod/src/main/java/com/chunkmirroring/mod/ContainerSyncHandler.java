package com.chunkmirroring.mod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * Mirrors container inventory changes (chest, barrel, furnace, hopper, etc.)
 * to the matching block entity at the same chunk-local position in every other
 * player-affected loaded chunk.
 *
 * How it works:
 * 1. When a player opens a block container, find the BlockEntity backing its slots.
 * 2. Attach a ContainerListener to the menu.
 * 3. Whenever a slot that belongs to that BlockEntity changes, copy the new
 *    ItemStack to the same slot index in every matching block entity found at
 *    the same local coordinates in all other player-affected loaded chunks.
 */
public class ContainerSyncHandler {

    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        AbstractContainerMenu menu = event.getContainer();

        BlockEntity sourceEntity = findBackingBlockEntity(menu);
        if (sourceEntity == null) return;

        BlockPos sourcePos = sourceEntity.getBlockPos();
        ChunkPos originChunk = new ChunkPos(sourcePos);
        int localX = sourcePos.getX() - (originChunk.x << 4);
        int localZ = sourcePos.getZ() - (originChunk.z << 4);

        menu.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu containerMenu, int slotIndex, ItemStack stack) {
                if (slotIndex < 0 || slotIndex >= containerMenu.slots.size()) return;

                Slot slot = containerMenu.slots.get(slotIndex);

                if (!(slot.container instanceof BlockEntity)) return;

                int containerSlot = slot.getContainerSlot();

                PlayerChunkData data = PlayerChunkData.get(level);
                List<ChunkPos> targets = BlockEventHandler.getLoadedPlayerChunks(level, data, originChunk);

                for (ChunkPos chunkPos : targets) {
                    int worldX = (chunkPos.x << 4) + localX;
                    int worldZ = (chunkPos.z << 4) + localZ;
                    BlockPos targetPos = new BlockPos(worldX, sourcePos.getY(), worldZ);

                    BlockEntity targetEntity = level.getBlockEntity(targetPos);
                    if (!(targetEntity instanceof Container targetInventory)) continue;
                    if (containerSlot >= targetInventory.getContainerSize()) continue;

                    targetInventory.setItem(containerSlot, stack.copy());
                }
            }

            @Override
            public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
                // Not needed — furnace progress, etc. don't require mirroring.
            }
        });
    }

    /**
     * Scans the menu's slots for one whose container is a BlockEntity.
     * This works for all vanilla single-block containers (chest, barrel,
     * furnace, hopper, dropper, dispenser, etc.).
     *
     * Double chests use a DoubleSidedInventory wrapper and are not matched here;
     * they will be silently skipped.
     */
    private BlockEntity findBackingBlockEntity(AbstractContainerMenu menu) {
        for (Slot slot : menu.slots) {
            if (slot.container instanceof BlockEntity be) {
                return be;
            }
        }
        return null;
    }
}
