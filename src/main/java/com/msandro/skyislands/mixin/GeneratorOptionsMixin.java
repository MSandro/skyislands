package com.msandro.skyislands.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.msandro.skyislands.common.SkyblockChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Random;

import static net.minecraft.world.level.levelgen.WorldGenSettings.*;

@Mixin(WorldGenSettings.class)
public class GeneratorOptionsMixin {

    @SuppressWarnings("boxing")
    @Inject(method = "create", at = @At("HEAD"), cancellable = true)
    private static void tattersGeneratorOptions(RegistryAccess registryAccess, DedicatedServerProperties.WorldGenProperties worldGenProperties, CallbackInfoReturnable<WorldGenSettings> cir) {
        long l = parseSeed(worldGenProperties.levelSeed()).orElse((new Random()).nextLong());
        Registry<DimensionType> registry = registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        Registry<Biome> registry2 = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        Registry<StructureSet> registry3 = registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
        Registry<LevelStem> registry4 = DimensionType.defaultDimensions(registryAccess, l);
        switch (worldGenProperties.levelType()) {
            case "flat":
                Dynamic<JsonElement> dynamic = new Dynamic(JsonOps.INSTANCE, worldGenProperties.generatorSettings());
                boolean var10003 = worldGenProperties.generateStructures();
                DataResult var10010 = FlatLevelGeneratorSettings.CODEC.parse(dynamic);
                var var10011 = com.msandro.skyislands.SkyIslands.log;
                Objects.requireNonNull(var10011);
                cir.setReturnValue(new WorldGenSettings(l, var10003, false, withOverworld(registry, (Registry)registry4, new FlatLevelSource(registry3, (FlatLevelGeneratorSettings)var10010.resultOrPartial(var10011::error).orElseGet(() -> {
                    return FlatLevelGeneratorSettings.getDefault(registry2, registry3);
                })))));
            case "debug_all_block_states":
                cir.setReturnValue(new WorldGenSettings(l, worldGenProperties.generateStructures(), false, withOverworld(registry, (Registry)registry4, new DebugLevelSource(registry3, registry2))));
            case "amplified":
                cir.setReturnValue(new WorldGenSettings(l, worldGenProperties.generateStructures(), false, withOverworld(registry, (Registry)registry4, makeOverworld(registryAccess, l, NoiseGeneratorSettings.AMPLIFIED))));
            case "largebiomes":
                cir.setReturnValue(new WorldGenSettings(l, worldGenProperties.generateStructures(), false, withOverworld(registry, (Registry)registry4, makeOverworld(registryAccess, l, NoiseGeneratorSettings.LARGE_BIOMES))));
            case "skyblock":
                cir.setReturnValue(new WorldGenSettings(l, worldGenProperties.generateStructures(), false, withOverworld(registry, (Registry)registry4, SkyblockChunkGenerator.createForWorldType(registryAccess, l))));
            default:
                cir.setReturnValue(new WorldGenSettings(l, worldGenProperties.generateStructures(), false, withOverworld(registry, (Registry)registry4, makeDefaultOverworld(registryAccess, l))));
        }
    }
}
