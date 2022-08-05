package com.msandro.skyislands.mixin;

import com.msandro.skyislands.common.SkyblockChunkGenerator;
import com.msandro.skyislands.common.WorldTypeSkyblock;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static net.minecraft.world.level.levelgen.WorldGenSettings.withOverworld;

@Mixin(WorldGenSettings.class)
public abstract class GeneratorOptionsMixin {

    @Inject(method = "create", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void addSkyLandGeneratorOptions(RegistryAccess registryManager, DedicatedServerProperties.WorldGenProperties worldGenProperties, CallbackInfoReturnable<WorldGenSettings> cir, long seed, Registry<DimensionType> registry, Registry<Biome> registry2, Registry<StructureSet> registry3, Registry<LevelStem> registry4) {
        if (WorldTypeSkyblock.ID.equals(worldGenProperties.levelType())) {
            cir.setReturnValue(new WorldGenSettings(seed, worldGenProperties.generateStructures(), false, withOverworld(registry4, registry.getHolderOrThrow(ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation("overworld"))), SkyblockChunkGenerator.createForWorldType(registryManager, seed))));
        }
    }
}
