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

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import tatters.TattersMain;
import tatters.config.SkyblockConfig;
import tatters.config.TattersConfig;

public class Skyblocks extends PersistentState {

    public static final String PERSISTANCE_ID = Skyblocks.class.getName();

    public static UUID getTeamUUID(final Team team) {
        return UUID.nameUUIDFromBytes(("team:" + team.getName()).getBytes(StandardCharsets.UTF_8));
    }

    public static Skyblocks getSkyblocks(final ServerWorld world) {
        if (TattersMain.isTattersWorld(world) == false)
            return null;
        final Skyblocks result = getPersitantState(world);
        result.worldRef = new WeakReference<>(world);
        return result;
    }

    private static Skyblocks getPersitantState(final ServerWorld world) {
        final PersistentStateManager persistentStateManager = world.getPersistentStateManager();
        // Load existing state
        Skyblocks result = persistentStateManager.get(() -> new Skyblocks(), PERSISTANCE_ID);
        if (result != null) {
           return result;
        }
        // No state, create it
        result = new Skyblocks();
        final TattersConfig config = TattersConfig.getConfig();
        result.skyblockPos.spacing = config.spacing;
        result.skyblockPos.y = config.defaultY;
        result.lobbyFile = config.getLobbyConfig().fileName;
        result.skyblockFile = config.getSkyblockConfig().fileName;
        persistentStateManager.set(result);
        return result;
    }

    public static void onServerPlayerLoad(final ServerPlayerEntity player, final ServerWorld world) {
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

    private WeakReference<ServerWorld> worldRef = new WeakReference<ServerWorld>(null);

    private String lobbyFile;

    private String skyblockFile;

    private SkyblockPos skyblockPos = new SkyblockPos();

    private Map<UUID, Skyblock> skyblocksByPlayer = Maps.newConcurrentMap();

    private Skyblocks() {
        super(PERSISTANCE_ID);
    }

    public ServerWorld getWorld() {
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

    public Skyblock getSkyblock(final ServerPlayerEntity player) {
        return getSkyblock(player.getUuid());
    }

    public Skyblock getSkyblock(final Team team) {
        return getSkyblock(getTeamUUID(team));
    }

    public Skyblock getSkyblock(final UUID uuid) {
        return skyblocksByPlayer.get(uuid);
    }

    public Skyblock createLobby() {
        final Skyblock lobby = createSkyblock(Util.NIL_UUID, "<lobby>", this.lobbyFile);
        getWorld().setSpawnPos(lobby.getSpawnPos(), 0.0F);
        return lobby;
    }

    public Skyblock createSkyblock(final ServerPlayerEntity player) {
        return createSkyblock(player.getUuid(), player.getEntityName(), this.skyblockFile);
    }

    public Skyblock createSkyblock(final Team team) {
        return createSkyblock(getTeamUUID(team), team.getName(), this.skyblockFile);
    }

    public Skyblock createSkyblock(final UUID uuid, final String name, final String file) {
        final SkyblockConfig config = TattersConfig.getConfig().getSkyblockConfig(file);
        final Skyblock skyblock = new Skyblock(this, uuid, name);
        skyblock.create(config);
        this.skyblocksByPlayer.put(uuid, skyblock);
        markDirty();
        return skyblock;
    }

    @Override
    public void fromTag(final CompoundTag tag) {
        this.skyblockPos.fromTag(tag.getCompound("skyblockPos"));
        this.lobbyFile = tag.getString("lobby");
        this.skyblockFile = tag.getString("skyblock");

        final Map<UUID, Skyblock> map = Maps.newConcurrentMap();
        final CompoundTag skyblocks = tag.getCompound("skyblocks");
        skyblocks.getKeys().stream().forEach((key) -> {
            final UUID uuid = UUID.fromString(key);
            final Skyblock skyblock = new Skyblock(this, uuid);
            skyblock.fromTag(skyblocks.getCompound(key));
            map.put(uuid, skyblock);
        });
        this.skyblocksByPlayer = map;
    }

    @Override
    public CompoundTag toTag(final CompoundTag tag) {
        tag.put("skyblockPos", this.skyblockPos.toTag(new CompoundTag()));
        tag.putString("lobby", lobbyFile);
        tag.putString("skyblock", skyblockFile);
        
        final CompoundTag skyblocks = new CompoundTag();
        this.skyblocksByPlayer.forEach((uuid, skyblock) -> {
            skyblocks.put(uuid.toString(), skyblock.toTag(new CompoundTag()));
        });
        tag.put("skyblocks", skyblocks);
        return tag;
    }
}
