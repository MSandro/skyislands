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
package com.msandro.skyislands.common;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.scores.Team;
import com.msandro.skyislands.SkyIslands;
import com.msandro.skyislands.config.SkyblockConfig;
import com.msandro.skyislands.config.SkyConfig;

public class Skyblocks extends SavedData {

    public static final String PERSISTANCE_ID = Skyblocks.class.getName();

    public static UUID getTeamUUID(final Team team) {
        return UUID.nameUUIDFromBytes(("team:" + team.getName()).getBytes(StandardCharsets.UTF_8));
    }

    public static Skyblocks getSkyblocks(final ServerLevel world) {
        if (SkyIslands.isSkyWorld(world) == false)
            return null;
        final Skyblocks result = getPersitantState(world);
        result.worldRef = new WeakReference<>(world);
        return result;
    }

    private static Skyblocks getPersitantState(final ServerLevel world) {
        final DimensionDataStorage persistentStateManager = world.getDataStorage();
        // Load existing state
        Skyblocks result = persistentStateManager.get(Skyblocks::load, PERSISTANCE_ID);
        if (result != null) {
           return result;
        }
        // No state, create it
        result = new Skyblocks();
        final SkyConfig config = SkyConfig.getConfig();
        result.skyblockPos.spacing = config.spacing;
        result.skyblockPos.y = config.defaultY;
        result.lobbyFile = config.getLobbyConfig().fileName;
        result.skyblockFile = config.getSkyblockConfig().fileName;
        persistentStateManager.set(PERSISTANCE_ID, result);
        return result;
    }

    public static void onServerPlayerLoad(final ServerPlayer player, final ServerLevel world) {
        final Skyblocks skyblocks = Skyblocks.getSkyblocks(world);
        if (skyblocks == null)
            return;

        // Bootstrap "lobby" on first player join
        Skyblock lobby = skyblocks.getLobby();
        if (lobby == null) {
            lobby = skyblocks.createLobby();
            lobby.teleport(player);
        }
    }

    private WeakReference<ServerLevel> worldRef = new WeakReference<>(null);

    private String lobbyFile;

    private String skyblockFile;

    private SkyblockPos skyblockPos = new SkyblockPos();

    private Map<UUID, Skyblock> skyblocksByPlayer = Maps.newConcurrentMap();

    private Skyblocks() {
    }

    public ServerLevel getWorld() {
        return this.worldRef.get();
    }

    SkyblockPos getSkyblockPos() {
        return this.skyblockPos;
    }

    public Collection<Skyblock> listSkyblocks() {
        return Collections.unmodifiableCollection(this.skyblocksByPlayer.values());
    }

    public Skyblock getLobby() {
        return getSkyblock(Util.NIL_UUID);
    }

    public Skyblock getSkyblock(final ServerPlayer player) {
        return getSkyblock(player.getUUID());
    }

    public Skyblock getSkyblock(final Team team) {
        return getSkyblock(getTeamUUID(team));
    }

    public Skyblock getSkyblock(final UUID uuid) {
        return this.skyblocksByPlayer.get(uuid);
    }

    @SuppressWarnings("resource")
    public Skyblock createLobby() {
        final Skyblock lobby = createSkyblock(Util.NIL_UUID, "<lobby>", this.lobbyFile);
        getWorld().setDefaultSpawnPos(lobby.getSpawnPos(), 0.0F);
        return lobby;
    }

    public Skyblock createSkyblock(final ServerPlayer player) {
        return createSkyblock(player.getUUID(), player.getScoreboardName(), this.skyblockFile);
    }

    public Skyblock createSkyblock(final Team team) {
        return createSkyblock(getTeamUUID(team), team.getName(), this.skyblockFile);
    }

    public Skyblock createSkyblock(final UUID uuid, final String name, final String file) {
        final SkyblockConfig config = SkyConfig.getConfig().getSkyblockConfig(file);
        final Skyblock skyblock = new Skyblock(this, uuid, name);
        skyblock.create(config);
        this.skyblocksByPlayer.put(uuid, skyblock);
        setDirty();
        return skyblock;
    }

    public static Skyblocks load(final CompoundTag tag) {
        final Skyblocks result = new Skyblocks();
        result.skyblockPos.fromTag(tag.getCompound("skyblockPos"));
        result.lobbyFile = tag.getString("lobby");
        result.skyblockFile = tag.getString("skyblock");

        final Map<UUID, Skyblock> map = Maps.newConcurrentMap();
        final CompoundTag skyblocks = tag.getCompound("skyblocks");
        skyblocks.getAllKeys().stream().forEach((key) -> {
            final UUID uuid = UUID.fromString(key);
            final Skyblock skyblock = new Skyblock(result, uuid);
            skyblock.fromTag(skyblocks.getCompound(key));
            map.put(uuid, skyblock);
        });
        result.skyblocksByPlayer = map;
        return result;
    }

    @Override
    public CompoundTag save(final CompoundTag tag) {
        tag.put("skyblockPos", this.skyblockPos.toTag(new CompoundTag()));
        tag.putString("lobby", this.lobbyFile);
        tag.putString("skyblock", this.skyblockFile);

        final CompoundTag skyblocks = new CompoundTag();
        this.skyblocksByPlayer.forEach((uuid, skyblock) -> {
            skyblocks.put(uuid.toString(), skyblock.toTag(new CompoundTag()));
        });
        tag.put("skyblocks", skyblocks);
        return tag;
    }
}
