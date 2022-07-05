package com.msandro.skyislands.config;

import java.util.Optional;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;

public class SkyblockFillerDefinition {

    private static Registry<Block> blocks = Registry.BLOCK;

    private transient Block theBlock;

    public String block;

    public int thickness = 1;

    public SkyblockFillerDefinition() {
    }

    public void validate() {
        parseBlock();
        if (this.thickness < 1 || this.thickness > 256) {
            throw new IllegalArgumentException("Invalid thickness for " + this.block);
        }
    }

    public FlatLayerInfo getChunkGeneratorLayer() {
        return new FlatLayerInfo(this.thickness, parseBlock());
    }

    private Block parseBlock() {
        if (this.block == null)
            throw new IllegalArgumentException("Null block");
        if (this.theBlock == null) {
            final ResourceLocation identifier = new ResourceLocation(this.block);
            final Optional<Block> blockTest = blocks.getOptional(identifier);
            if (!blockTest.isPresent()) {
                throw new IllegalArgumentException("Unknown block: " + identifier);
            }
            this.theBlock = blockTest.get();
        }
        return this.theBlock;
    }
}
