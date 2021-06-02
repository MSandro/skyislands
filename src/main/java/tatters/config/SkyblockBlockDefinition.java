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
package tatters.config;

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
                blockEntity.load(this.blockState, tag);
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
        final Block block = blockTest.get();
        this.blockState = block.defaultBlockState();
        if (this.properties != null) {
            try {
                final StateDefinition<Block, BlockState> stateManager = block.getStateDefinition();
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
