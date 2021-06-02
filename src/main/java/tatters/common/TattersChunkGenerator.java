/*
 * This file is part of Tatters.
 * Copyright (c) 2021, warjort and others, All rights reserved.
 *
 * Tatters is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tatters is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Tatters.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package tatters.common;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import net.minecraft.world.gen.chunk.StructuresConfig;
import tatters.TattersMain;
import tatters.config.SkyblockConfig;
import tatters.config.TattersConfig;

public class TattersChunkGenerator extends FlatChunkGenerator implements TattersFlatChunkGenerator {

    public static final Codec<TattersChunkGenerator> CODEC;

    public static FlatChunkGeneratorConfig createConfig(final Registry<Biome> biomeRegistry) {
        List<FlatChunkGeneratorLayer> layers = Lists.newArrayList();
        try {
            final TattersConfig config = TattersConfig.getConfig();
            final SkyblockConfig lobby = config.getLobbyConfig();
            layers = lobby.getFiller();
        }
        catch (Throwable e) {
            TattersMain.log.warn("Error reading config", e);
        }
        final StructuresConfig structuresConfig = new StructuresConfig(Optional.empty(), Collections.emptyMap());
        final Biome biome = biomeRegistry.getOrThrow(BiomeKeys.PLAINS);
        return new FlatChunkGeneratorConfig(biomeRegistry, structuresConfig, layers, false, false, Optional.of(() -> biome));
    }

    public TattersChunkGenerator(final FlatChunkGeneratorConfig config) {
        super(config);
    }

    @Environment(EnvType.CLIENT)
    public void updateConfig() {
        final TattersConfig config = TattersConfig.getConfig();
        final SkyblockConfig lobby = config.getLobbyConfig();
        final StructuresConfig structuresConfig = new StructuresConfig(Optional.empty(), Collections.emptyMap());
        final List<FlatChunkGeneratorLayer> layers = lobby.getFiller();
        tattersSetConfig(getConfig().method_29965(layers, structuresConfig));
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
       return CODEC;
    }

    static {
       CODEC = FlatChunkGeneratorConfig.CODEC.fieldOf("settings").xmap(TattersChunkGenerator::new, TattersChunkGenerator::getConfig).codec();
    }
}
