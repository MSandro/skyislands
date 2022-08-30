package com.msandro.skyislands.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.msandro.skyislands.SkyIslands;
import com.msandro.skyislands.mixin.AccessorBeardifier;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public class SkyblockChunkGenerator extends ChunkGenerator {
    public static final Codec<SkyblockChunkGenerator> CODEC = RecordCodecBuilder.create(
            instance -> commonCodec(instance)
                    .and(instance.group(
                            RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(gen -> gen.noises),
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter((gen) -> gen.biomeSource),
                            RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter((generator) -> generator.biomeRegistry),
                            Codec.LONG.fieldOf("seed").stable().forGetter(gen -> gen.seed),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(gen -> gen.settings)))
                    .apply(instance, instance.stable(SkyblockChunkGenerator::new)));


    public static void init() {
        Registry.register(Registry.CHUNK_GENERATOR, SkyIslands.MOD_ID, SkyblockChunkGenerator.CODEC);
    }

    public static boolean isWorldSkyblock(Level world) {
        return world.getChunkSource() instanceof ServerChunkCache
                && ((ServerChunkCache) world.getChunkSource()).getGenerator() instanceof SkyblockChunkGenerator;
    }

    public static ChunkGenerator createForWorldType(RegistryAccess registryAccess, long seed) {
        return new SkyblockChunkGenerator(
                registryAccess.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY),
                registryAccess.registryOrThrow(Registry.NOISE_REGISTRY),
                MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(registryAccess.registryOrThrow(Registry.BIOME_REGISTRY)),
                registryAccess.registryOrThrow((Registry.BIOME_REGISTRY)),
                seed,
                registryAccess.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD)
        );
    }

    protected final BlockState defaultBlock;
    private final Registry<NormalNoise.NoiseParameters> noises;
    protected final long seed;
    protected final Holder<NoiseGeneratorSettings> settings;
    private final NoiseRouter router;
    protected final Climate.Sampler sampler;

    private final Registry<Biome> biomeRegistry;
    private final Aquifer.FluidPicker globalFluidPicker;

    private SkyblockChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, BiomeSource biomeSource, Registry<Biome> biomeRegistry, long seed, Holder<NoiseGeneratorSettings> settings) {
        this(structureSets, noises, biomeSource, biomeSource, seed, settings, biomeRegistry);
    }

    private SkyblockChunkGenerator(Registry<StructureSet> structureSets, Registry<NormalNoise.NoiseParameters> noises, BiomeSource biomeSource, BiomeSource runtimeBiomeSource, long seed, Holder<NoiseGeneratorSettings> settings, Registry<Biome> biomeRegistry) {
        super(structureSets, Optional.empty(), new FixedBiomeSource(biomeRegistry.getOrCreateHolder(Biomes.PLAINS)));
        this.noises = noises;
        this.seed = seed;
        this.settings = settings;
        this.biomeRegistry = biomeRegistry;
        NoiseGeneratorSettings genSettings = this.settings.value();
        this.defaultBlock = genSettings.defaultBlock();
        this.router = genSettings.noiseRouter();
        this.sampler = new Climate.Sampler(this.router.temperature(), this.router.continents(), this.router.erosion(), this.router.depth(), this.router.ridges(), this.router.spawnTarget());
        Aquifer.FluidStatus lava = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int i = genSettings.seaLevel();
        Aquifer.FluidStatus defaultFluid = new Aquifer.FluidStatus(i, genSettings.defaultFluid());
        this.globalFluidPicker = (p_198228_, p_198229_, p_198230_) -> p_198229_ < Math.min(-54, i) ? lava : defaultFluid;
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> biomes, Executor p_197006_, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            this.doCreateBiomes(blender, structureFeatureManager, chunkAccess);
            return chunkAccess;
        }), Util.backgroundExecutor());
    }

    private void doCreateBiomes(Blender blender, StructureManager sfm, ChunkAccess chunkAccess) {
        NoiseChunk chunk = chunkAccess.getOrCreateNoiseChunk(this.router, () -> AccessorBeardifier.skyIslands_make(sfm, chunkAccess), this.settings.value(), this.globalFluidPicker, blender);
        BiomeResolver biomeresolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.runtimeBiomeSource), chunkAccess);
        chunkAccess.fillBiomesFromNoise(biomeresolver, ((Accessor) chunk).skyIslands_cachedClimateSampler(this.router));
    }

    @VisibleForDebug
    public NoiseRouter router() {
        return this.router;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {

    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {

    }


    @Override
    public int getGenDepth() {
        return this.settings.value().noiseSettings().height();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        return null;
    }

    @Override
    public int getSeaLevel() {
        return this.settings.value().seaLevel();
    }

    @Override
    public int getMinY() {
        return this.settings.value().noiseSettings().minY();
    }

    @Override
    public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return null;
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {

    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {}

}
