package com.msandro.skyislands.mixin;

import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Beardifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Beardifier.class)
public interface AccessorBeardifier {
    @Invoker("<init>")
    static Beardifier skyIslands_make(StructureFeatureManager sfm, ChunkAccess chunkAccess) {
        throw new IllegalStateException();
    }
}
