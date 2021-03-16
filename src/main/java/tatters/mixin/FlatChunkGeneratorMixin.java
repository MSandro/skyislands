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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import tatters.common.TattersFlatChunkGenerator;

@Mixin(FlatChunkGenerator.class)
public class FlatChunkGeneratorMixin implements TattersFlatChunkGenerator {

    @Mutable
    @Shadow
    private FlatChunkGeneratorConfig config;

    @Override
    public void tattersSetConfig(final FlatChunkGeneratorConfig config) {
        this.config = config;
    }
}
