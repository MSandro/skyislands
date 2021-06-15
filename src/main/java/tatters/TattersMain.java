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
package tatters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import tatters.common.Skyblocks;
import tatters.common.TattersChunkGenerator;
import tatters.common.TattersCommand;

public class TattersMain implements ModInitializer {

    public static final Logger log = LogManager.getLogger();
    public static final String MOD_ID = "tatters";

    @SuppressWarnings("resource")
    public static boolean isTattersWorld(final Level world) {
        if (world instanceof ServerLevel == false)
            return false;
        final ServerLevel serverWorld = (ServerLevel) world;
        return serverWorld.getChunkSource().getGenerator() instanceof TattersChunkGenerator
                && world.dimension().equals(Level.OVERWORLD);
    }

    public static ModContainer getModContainer() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .orElseThrow(() -> new IllegalStateException("Unable to get ModContainer: " + MOD_ID));
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(TattersCommand::register);
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ServerPlayer == false)
                return;
            final ServerPlayer player = (ServerPlayer) entity;
            if (!isTattersWorld(world))
                return;
            tatters_onServerPlayerLoad(player, world);
        });
    }

    // Separate method to try to avoid the config referenced in Skyblocks getting loaded when not needed
    private static void tatters_onServerPlayerLoad(final ServerPlayer player, final ServerLevel world) {
        Skyblocks.onServerPlayerLoad(player, world);
    }
}
