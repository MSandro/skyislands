package com.msandro.skyislands.mixin.client;

import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(WorldPresets.class)
public interface AccessorWorldPreset {
    /*static List<WorldPreset> getAllTypes() {
        throw new IllegalStateException();
    }*/
}
