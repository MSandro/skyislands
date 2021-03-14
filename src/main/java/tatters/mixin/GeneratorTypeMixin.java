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

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.world.GeneratorType;
import tatters.client.TattersGeneratorType;

@Mixin(GeneratorType.class)
public class GeneratorTypeMixin {

    @Final
    @Shadow
    protected static List<GeneratorType> VALUES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void tatters_clinit(final CallbackInfo callback) {
        VALUES.add(1, new TattersGeneratorType());
        // TODO add a configuration screen to change skyblock config in single player mode
        // TODO figure out how to display a toast for config errors instead of crashing
    }
}
