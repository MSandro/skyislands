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
package tatters.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import tatters.TattersMain;
import tatters.common.TattersChunkGenerator;

@Environment(EnvType.CLIENT)
public class TattersGeneratorType extends GeneratorType {

    public TattersGeneratorType() {
        super(TattersMain.MOD_ID);
    }

    @Override
    protected ChunkGenerator getChunkGenerator(final Registry<Biome> biomeRegistry,
            final Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, final long seed) {
        return new TattersChunkGenerator(getConfig(biomeRegistry));
    }

    private static FlatChunkGeneratorConfig getConfig(final Registry<Biome> biomeRegistry) {
        return TattersChunkGenerator.createConfig(biomeRegistry);
    }
}
