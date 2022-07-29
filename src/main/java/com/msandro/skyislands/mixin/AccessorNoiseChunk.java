package com.msandro.skyislands.mixin;

import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseRouter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NoiseChunk.class)
public interface AccessorNoiseChunk {
    @Invoker("cachedClimateSampler")
    Climate.Sampler skyIslands_cachedClimateSampler(NoiseRouter router);
}
