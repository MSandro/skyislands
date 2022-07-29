package com.msandro.skyislands.common;

import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class WorldTypeSkyblock extends WorldPreset {
    public static final WorldTypeSkyblock INSTANCE = new WorldTypeSkyblock();

    private WorldTypeSkyblock() {
        super("skyislands-skyblock");
    }

    @Override
    protected ChunkGenerator generator(RegistryAccess registryAccess, long seed) {
        return SkyblockChunkGenerator.createForWorldType(registryAccess, seed);
    }
}
