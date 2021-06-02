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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Direction;
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

    public boolean teleport(final ServerPlayerEntity player) {
        if (!player.getServerWorld().equals(this.skyblocks.getWorld()))
            return false;

        if (player.hasVehicle()) {
            player.stopRiding();
        }
        player.requestTeleport(this.spawnPos.getX() + 0.5d, this.spawnPos.getY(), this.spawnPos.getZ() + 0.5d);
        return true;
    }

    public void setPlayerSpawn(final ServerPlayerEntity player) {
        player.setSpawnPoint(this.skyblocks.getWorld().getRegistryKey(), getSpawnPos(), 0.0F, true, true);
    }

    public void create(final SkyblockConfig config) {
        try {
            final SkyblockPos skyblockPos = this.skyblocks.getSkyblockPos();
            final Mutable startPos = skyblockPos.getPos().mutableCopy();
            skyblockPos.nextPos();
            this.skyblocks.markDirty();

            final ServerWorld world = this.skyblocks.getWorld();
            final List<List<String>> layers = config.layers;
            final Map<Character, SkyblockBlockDefinition> mapping = config.mapping;
            final Mutable layerPos = startPos.mutableCopy();
            for (List<String> layer : layers) {
                final Mutable rowPos = layerPos.east(layer.size()/2).mutableCopy(); 
                for (String row : layer) {
                    final Mutable columnPos = rowPos.north(row.length()/2).mutableCopy();
                    row.chars().forEach(c -> {
                        final Character key = (char) c;
                        final SkyblockBlockDefinition definition = mapping.getOrDefault(key, SkyblockBlockDefinition.AIR);
                        definition.placeBlock(world, columnPos);
                        if (c == '!') {
                            if (this.spawnPos != null) {
                                log.warn("Duplicate spawn points defined for " + config.name);
                            } else {
                                this.spawnPos = columnPos.toImmutable();
                            }
                        }
                        columnPos.move(Direction.SOUTH);
                    });
                    rowPos.move(Direction.WEST);
                }
                layerPos.move(Direction.UP);
            }
            if (this.spawnPos == null) {
                this.spawnPos = startPos.up(layers.size()).toImmutable();
            }
        }
        catch (RuntimeException e) {
            log.error("Unexpected error creating skyblock", e);
            throw e;
        }
    }
}
