package com.msandro.skyislands.client;

import com.msandro.skyislands.common.WorldTypeSkyblock;
import com.msandro.skyislands.mixin.client.AccessorWorldPreset;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SkyIslandsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldTypeSkyblock.isVisibleByDefault(WorldTypeSkyblock.INSTANCE);
        AccessorWorldPreset.getAllTypes().add(0, WorldTypeSkyblock.INSTANCE);

    }
}
