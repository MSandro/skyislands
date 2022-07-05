package com.msandro.skyislands.config;

import java.util.Map;
import java.util.Optional;

import com.mojang.brigadier.StringReader;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class SkyblockBlockDefinition {

    private static Registry<Block> blocks = Registry.BLOCK;

    public static SkyblockBlockDefinition AIR = new SkyblockBlockDefinition(Blocks.AIR);

    private transient BlockState blockState;

    public String block;

    public Map<String, String> properties = null;

    public String nbt;

    public SkyblockBlockDefinition() {
    }

    public SkyblockBlockDefinition(final Block block) {
        this.blockState = block.defaultBlockState();
    }

    public void validate() {
        parseBlockState();
        parseNBT();
    }

    public void placeBlock(final ServerLevel world, final BlockPos pos) {
        try {
            parseBlockState();
            world.setBlockAndUpdate(pos.immutable(), this.blockState);
            if (this.nbt != null) {
                final BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity == null)
                    throw new IllegalArgumentException(this.block + " has no block entity for nbt: " + this.nbt);
                final CompoundTag tag = parseNBT();
                tag.putInt("x", pos.getX());
                tag.putInt("y", pos.getY());
                tag.putInt("z", pos.getZ());
                blockEntity.load(tag);
                blockEntity.setChanged();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error placing block: " + this.block, e);
        }
    }

    private void parseBlockState() {
        if (this.blockState != null) {
            return;
        }
        final ResourceLocation identifier = new ResourceLocation(this.block);
        final Optional<Block> blockTest = blocks.getOptional(identifier);
        if (!blockTest.isPresent()) {
            throw new IllegalArgumentException("Unknown block: " + identifier);
        }
        final Block testBlock = blockTest.get();
        this.blockState = testBlock.defaultBlockState();
        if (this.properties != null) {
            try {
                final StateDefinition<Block, BlockState> stateManager = testBlock.getStateDefinition();
                this.properties.forEach((name, value) -> {
                    final Property<?> property = stateManager.getProperty(name);
                    if (property == null) {
                        throw new IllegalArgumentException(this.block + " unknown property: " + name);
                    }
                    parsePropertyValue(property, value);
                });
            } catch (RuntimeException e) {
                this.blockState = null;
                throw e;
            }
        }
    }

    private <T extends Comparable<T>> void parsePropertyValue(final Property<T> property, final String value) {
        final Optional<T> optional = property.getValue(value);
        if (optional.isPresent()) {
            this.blockState = this.blockState.setValue(property, optional.get());
        } else {
            throw new IllegalArgumentException("Invalid value: " + value + " for property " + property.getName() + " of " + this.block); 
        }
    }

    private CompoundTag parseNBT(){
        if (this.nbt == null) {
            return null;
        }
        try {
            return new TagParser(new StringReader(this.nbt)).readStruct();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing nbt for " + this.block, e);
        }
    }
}
