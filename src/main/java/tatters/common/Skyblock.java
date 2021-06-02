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

import static tatters.TattersMain.log;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import tatters.config.SkyblockBlockDefinition;
import tatters.config.SkyblockConfig;

public class Skyblock {

    private final Skyblocks skyblocks;

    private final UUID uuid;

    private String name;

    // TODO add validation of spawn position so it is not over the void or above too many air blocks?
    private BlockPos spawnPos;

    public Skyblock(final Skyblocks skyblocks, final UUID uuid) {
        this(skyblocks, uuid, uuid.toString());
    }

    public Skyblock(final Skyblocks skyblocks, final UUID uuid, final String name) {
        this.skyblocks = skyblocks;
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public BlockPos getSpawnPos() {
        return this.spawnPos;
    }

    public void fromTag(final CompoundTag tag) {
        this.name = tag.getString("name");
        this.spawnPos = new BlockPos(tag.getInt("spawnX"), tag.getInt("spawnY"), tag.getInt("spawnZ"));
    }

    public CompoundTag toTag(final CompoundTag tag) {
        tag.putString("name", this.name);
        tag.putInt("spawnX", this.spawnPos.getX());
        tag.putInt("spawnY", this.spawnPos.getY());
        tag.putInt("spawnZ", this.spawnPos.getZ());
        return tag;
    }

    public boolean teleport(final ServerPlayer player) {
        if (!player.getLevel().equals(this.skyblocks.getWorld()))
            return false;

        if (player.isPassenger()) {
            player.stopRiding();
        }
        player.teleportTo(this.spawnPos.getX() + 0.5d, this.spawnPos.getY(), this.spawnPos.getZ() + 0.5d);
        return true;
    }

    public void setPlayerSpawn(final ServerPlayer player) {
        player.setRespawnPosition(this.skyblocks.getWorld().dimension(), getSpawnPos(), 0.0F, true, true);
    }

    public void create(final SkyblockConfig config) {
        try {
            final SkyblockPos skyblockPos = this.skyblocks.getSkyblockPos();
            final MutableBlockPos startPos = skyblockPos.getPos().mutable();
            skyblockPos.nextPos();
            this.skyblocks.setDirty();

            final ServerLevel world = this.skyblocks.getWorld();
            final List<List<String>> layers = config.layers;
            final Map<Character, SkyblockBlockDefinition> mapping = config.mapping;
            final MutableBlockPos layerPos = startPos.mutable();
            for (List<String> layer : layers) {
                final MutableBlockPos rowPos = layerPos.east(layer.size()/2).mutable(); 
                for (String row : layer) {
                    final MutableBlockPos columnPos = rowPos.north(row.length()/2).mutable();
                    row.chars().forEach(c -> {
                        final Character key = (char) c;
                        final SkyblockBlockDefinition definition = mapping.getOrDefault(key, SkyblockBlockDefinition.AIR);
                        definition.placeBlock(world, columnPos);
                        if (c == '!') {
                            if (this.spawnPos != null) {
                                log.warn("Duplicate spawn points defined for " + config.name);
                            } else {
                                this.spawnPos = columnPos.immutable();
                            }
                        }
                        columnPos.move(Direction.SOUTH);
                    });
                    rowPos.move(Direction.WEST);
                }
                layerPos.move(Direction.UP);
            }
            if (this.spawnPos == null) {
                this.spawnPos = startPos.above(layers.size()).immutable();
            }
        }
        catch (RuntimeException e) {
            log.error("Unexpected error creating skyblock", e);
            throw e;
        }
    }
}
