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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

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
        blockState = block.getDefaultState();
    }

    public void validate() {
        parseBlockState();
        parseNBT();
    }

    public void placeBlock(final ServerWorld world, final BlockPos pos) {
        try {
            parseBlockState();
            world.setBlockState(pos.toImmutable(), this.blockState);
            if (this.nbt != null) {
                final BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity == null)
                    throw new IllegalArgumentException(this.block + " has no block entity for nbt: " + nbt);
                final CompoundTag tag = parseNBT();
                tag.putInt("x", pos.getX());
                tag.putInt("y", pos.getY());
                tag.putInt("z", pos.getZ());
                blockEntity.fromTag(this.blockState, tag);
                blockEntity.markDirty();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error placing block: " + this.block, e);
        }
    }

    private void parseBlockState() {
        if (this.blockState != null) {
            return;
        }
        final Identifier identifier = new Identifier(this.block);
        final Optional<Block> blockTest = blocks.getOrEmpty(identifier);
        if (!blockTest.isPresent()) {
            throw new IllegalArgumentException("Unknown block: " + identifier);
        }
        final Block block = blockTest.get();
        this.blockState = block.getDefaultState();
        if (this.properties != null) {
            try {
                final StateManager<Block, BlockState> stateManager = block.getStateManager();
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
        final Optional<T> optional = property.parse(value);
        if (optional.isPresent()) {
            this.blockState = this.blockState.with(property, optional.get());
        } else {
            throw new IllegalArgumentException("Invalid value: " + value + " for property " + property.getName() + " of " + this.block); 
        }
    }

    private CompoundTag parseNBT(){
        if (this.nbt == null) {
            return null;
        }
        try {
            return new StringNbtReader(new StringReader(this.nbt)).parseCompoundTag();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing nbt for " + this.block, e);
        }
    }
}
