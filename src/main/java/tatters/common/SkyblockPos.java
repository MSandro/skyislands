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
package tatters.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

public class SkyblockPos {

    int spacing = 1000;

    private int layer = 0;

    private int x = 0;

    int y = 80;

    private int z = 0;

    // Used to put skblocks in the centre of a chunk
    public static int centre(final int original) {
        return 8 + 16 * (original / 16);
    }

    public BlockPos getPos() {
        return new BlockPos(centre(this.x * this.spacing), this.y, centre(this.z * this.spacing));
    }

    public void nextPos() {
        // Finished the previous layer, start a new one
        if (this.x == this.layer && this.z == this.layer) {
            ++this.layer;
            this.x = -this.layer;
            this.z = -this.layer;
            return;
        }
        // Iterate to the next x position if we are on a z edge 
        if (this.x < this.layer && (this.z == this.layer || this.z == -this.layer)) {
            ++this.x;
            return;
        }
        // When not on a z edge just oscillate between the max x values
        this.x = -this.x;
        // Advancing the z when we switch to the negative x side
        if (this.x < 0)
            ++this.z;
    }

    public void fromTag(final CompoundTag tag) {
        this.spacing = tag.getInt("spacing");
        this.layer = tag.getInt("layer");
        this.x = tag.getInt("x");
        this.y = tag.getInt("y");
        this.z = tag.getInt("z");
    }

    public CompoundTag toTag(final CompoundTag tag) {
        tag.putInt("spacing", this.spacing);
        tag.putInt("layer", this.layer);
        tag.putInt("x", this.x);
        tag.putInt("y", this.y);
        tag.putInt("z", this.z);
        return tag;
    }
}
