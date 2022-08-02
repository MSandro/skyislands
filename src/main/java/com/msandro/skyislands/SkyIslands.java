package com.msandro.skyislands;

import com.msandro.skyislands.common.SkyblockChunkGenerator;
import com.msandro.skyislands.common.Skyblocks;
import com.msandro.skyislands.common.SkyCommand;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;


public class SkyIslands implements ModInitializer {
    public static final Logger log = LogManager.getLogger();
    public static String MOD_ID = "skyislands";

    public static ModContainer getModContainer() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .orElseThrow(() -> new IllegalStateException("Unable to get ModContainer: " + MOD_ID));
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(SkyCommand::register);
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayer == false)
                return;
            final ServerPlayer player = (ServerPlayer) entity;
            if (!isSkyWorld(world))
                return;
            sky_onServerPlayerLoad(player, world);
        });
        Registry.register(Registry.CHUNK_GENERATOR, new ResourceLocation(MOD_ID, "void"), SkyblockChunkGenerator.CODEC);
    }

    @SuppressWarnings("resource")
    public static boolean isSkyWorld(final Level world) {
        if (world instanceof ServerLevel == false)
            return false;
        final ServerLevel serverWorld = (ServerLevel) world;
        return serverWorld.getChunkSource().getGenerator() instanceof SkyblockChunkGenerator && world.dimension().equals(Level.OVERWORLD);
    }

    // Separate method to try to avoid the config referenced in Skyblocks getting loaded when not needed
    private static void sky_onServerPlayerLoad(final ServerPlayer player, final ServerLevel world) {
        Skyblocks.onServerPlayerLoad(player, world);
    }
}
