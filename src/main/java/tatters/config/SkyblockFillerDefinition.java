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

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;

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

    public FlatChunkGeneratorLayer getChunkGeneratorLayer() {
        return new FlatChunkGeneratorLayer(this.thickness, parseBlock());
    }

    private Block parseBlock() {
        if (this.block == null)
            throw new IllegalArgumentException("Null block");
        if (this.theBlock == null) {
            final Identifier identifier = new Identifier(this.block);
            final Optional<Block> blockTest = blocks.getOrEmpty(identifier);
            if (!blockTest.isPresent()) {
                throw new IllegalArgumentException("Unknown block: " + identifier);
            }
            this.theBlock = blockTest.get();
        }
        return this.theBlock;
    }
}
