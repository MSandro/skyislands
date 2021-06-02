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
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import tatters.common.TattersChunkGenerator;

public class TattersMain implements ModInitializer {

    public static final Logger log = LogManager.getLogger();
    public static String MOD_ID = "tatters";

    public static boolean isTattersWorld(final World world) {
        if (world instanceof ServerWorld == false)
            return false;
        final ServerWorld serverWorld = (ServerWorld) world;
        return serverWorld.getChunkManager().getChunkGenerator() instanceof TattersChunkGenerator 
                && world.getRegistryKey().equals(World.OVERWORLD);
    }

    public static ModContainer getModContainer() {
        return FabricLoader.getInstance().getModContainer(MOD_ID)
                .orElseThrow(() -> new IllegalStateException("Unable to get ModContainer: " + MOD_ID));
    }

    @Override
    public void onInitialize() {
        // nothing
    }
}
