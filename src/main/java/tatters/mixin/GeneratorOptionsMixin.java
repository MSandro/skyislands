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
package tatters.mixin;

import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.base.MoreObjects;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import tatters.TattersMain;
import tatters.common.TattersChunkGenerator;

// TODO add void nether/end handling
@Mixin(WorldGenSettings.class)
public class GeneratorOptionsMixin {

    @SuppressWarnings("boxing")
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void tattersGeneratorOptions(final RegistryAccess dynamicRegistryManager, final Properties properties, final CallbackInfoReturnable<WorldGenSettings> ci) {
        String levelType = (String) properties.get("level-type");
        if (levelType == null)
            return;
        levelType = levelType.toLowerCase(Locale.ROOT);
        if (levelType.equals(TattersMain.MOD_ID) == false)
            return;
        properties.put("level-type", levelType);

        final String levelSeed = MoreObjects.firstNonNull((String)properties.get("level-seed"), "");
        properties.put("level-seed", levelSeed);
        long seed = (new Random()).nextLong();
        if (!levelSeed.isEmpty()) {
            try {
                long parsedSeed = Long.parseLong(levelSeed);
                if (parsedSeed != 0L) {
                    seed = parsedSeed;
                }
             } catch (@SuppressWarnings("unused") NumberFormatException ignored) {
                 seed = levelSeed.hashCode();
             }
        }

        final String generatorSettings = MoreObjects.firstNonNull((String) properties.get("generator-settings"), "");
        properties.put("generator-settings", generatorSettings);
        final String genStructures = (String) properties.get("generate-structures");
        final boolean generateStructures = genStructures == null || Boolean.parseBoolean(genStructures);
        properties.put("generate-structures", Objects.toString(generateStructures));

        final Registry<DimensionType> dimensionTypes = dynamicRegistryManager.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        final Registry<Biome> biomeRegistry = dynamicRegistryManager.registryOrThrow(Registry.BIOME_REGISTRY);
        final Registry<NoiseGeneratorSettings> noiseSettings = dynamicRegistryManager.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        final MappedRegistry<LevelStem> defaultDimensions = DimensionType.defaultDimensions(dimensionTypes, biomeRegistry, noiseSettings, seed);
        // TODO figure out a way to reliably start at the equivalent of this point in fromProperties()
        final FlatLevelGeneratorSettings config = TattersChunkGenerator.createConfig(biomeRegistry);
        final TattersChunkGenerator chunkGenerator = new TattersChunkGenerator(config);
        final MappedRegistry<LevelStem> dimensionOptions = WorldGenSettings.withOverworld(dimensionTypes, defaultDimensions, chunkGenerator);
        final WorldGenSettings result = new WorldGenSettings(seed, generateStructures, false, dimensionOptions);
        ci.setReturnValue(result);
    }
}
