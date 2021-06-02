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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import tatters.TattersMain;
import tatters.common.Skyblocks;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {

    @Final
    @Shadow
    private boolean tickingEntities;

    @SuppressWarnings("resource")
    @Inject(method = "add", at = @At("TAIL"))
    private void tatters_onServerEntityLoad(final Entity entity, final CallbackInfo ci) {
        if (this.tickingEntities) {
            return;
        }
        if (entity instanceof ServerPlayer == false)
            return;
        final ServerPlayer player = (ServerPlayer) entity;
        final ServerLevel world = (ServerLevel) (Object) this;
        if (!TattersMain.isTattersWorld(world))
            return;
        tatters_onServerPlayerLoad(player, world);
    }

    // Separate method to try to avoid the config referenced in Skyblocks getting loaded when not needed
    private static void tatters_onServerPlayerLoad(final ServerPlayer player, final ServerLevel world) {
        Skyblocks.onServerPlayerLoad(player, world);
    }
}
