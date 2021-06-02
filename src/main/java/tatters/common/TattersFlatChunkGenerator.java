package tatters.common;

import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public interface TattersFlatChunkGenerator {

    default void tattersSetConfig(FlatLevelGeneratorSettings config) {
        throw new RuntimeException("unreachable");
    }
}
