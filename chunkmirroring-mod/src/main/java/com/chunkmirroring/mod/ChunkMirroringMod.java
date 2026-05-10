package com.chunkmirroring.mod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("chunkmirroring")
public class ChunkMirroringMod {

    public static final String MOD_ID = "chunkmirroring";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ChunkMirroringMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(new BlockEventHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("Chunk Mirroring Mod loaded. Block actions will be mirrored across all loaded chunks.");
    }
}
