package tatters.common;

import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;

public interface TattersFlatChunkGenerator {

    default void tattersSetConfig(FlatChunkGeneratorConfig config) {
        throw new RuntimeException("unreachable");
    }
}
