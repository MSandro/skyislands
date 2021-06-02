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
import java.util.Map;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import tatters.client.TattersGeneratorType;
import tatters.client.TattersLevelScreen;

@Mixin(WorldPreset.class)
public class GeneratorTypeMixin {

    @Final
    @Shadow
    protected static List<WorldPreset> PRESETS;

    @Mutable
    @Shadow
    protected static Map<Optional<WorldPreset>, WorldPreset.PresetEditor> EDITORS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void tatters_clinit(final CallbackInfo callback) {
        final TattersGeneratorType tattersGenerator = new TattersGeneratorType();
        PRESETS.add(1, tattersGenerator);
        EDITORS = Maps.newHashMap(EDITORS);
        EDITORS.put(Optional.of(tattersGenerator), (screen, generatorOptions) -> {
            return new TattersLevelScreen(screen, generatorOptions);
        });
    }
}
