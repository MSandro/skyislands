package com.msandro.skyislands.config;

import static com.msandro.skyislands.SkyIslands.log;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.msandro.skyislands.SkyIslands;

public class SkyConfig extends Config {

    private static final String CONFIG_FILE_NAME = SkyIslands.MOD_ID + ".json";
    private static SkyConfig CONFIG = null;

    public final String spacingComment = "The size of the skyblock grid";
    public int spacing = 1000;

    // TODO parameterize this value based on being above squid/slime spawns and maybe the cloud level?
    public final String defaultYComment = "The height of the bottom block of the skyblock";
    public int defaultY = 80;

    public final String skyblockComment = "The name of the skyblock to use by default";
    public String skyblock = "default.json";

    public final String lobbyComment = "The name of the config to use for the lobby, no value means use the skyblock config";
    public String lobby = "";

    public static final SkyConfig getConfig() {
        if (CONFIG == null)
            CONFIG = loadConfig();
        return CONFIG;
    }

    private void validate() {
        if (this.spacing < 32)
            throw new IllegalArgumentException(CONFIG_FILE_NAME + " spacing=" + this.spacing + " should be at least 32");
        // TODO figure out a way to parameterise these values that works on 1.16 and 1.17 with the expanded block range
        if (this.defaultY < 1 || this.defaultY > 255)
            throw new IllegalArgumentException(CONFIG_FILE_NAME + " defaultY=" + this.defaultY + " should be between 1 and 255");
        getSkyblockConfig();
        if (this.lobby != null && !this.lobby.isEmpty()) {
            getLobbyConfig();
        }
    }

    public SkyblockConfig getSkyblockConfig() {
        return SkyblockConfig.getSkyblockConfig(this.skyblock);
    }

    public SkyblockConfig getLobbyConfig() {
        return getLobbyConfig(true);
    }

    public SkyblockConfig getLobbyConfig(final boolean defaultSkyblock) {
        if (this.lobby != null && !this.lobby.isEmpty()) {
            return SkyblockConfig.getSkyblockConfig(this.lobby);
        }
        return defaultSkyblock ? getSkyblockConfig() : null;
    }

    @SuppressWarnings("static-method")
    public SkyblockConfig getSkyblockConfig(final String file) {
        return SkyblockConfig.getSkyblockConfig(file);
    }

    @SuppressWarnings("static-method")
    public List<SkyblockConfig> getActiveSkyblockConfigs() {
        return SkyblockConfig.getActiveSkyblockConfigs();
    }

    public static SkyConfig loadConfig() {
        SkyblockConfig.copySkyblocks();

        SkyConfig result = new SkyConfig();
        final Path file = getConfigFile(CONFIG_FILE_NAME);
        if (Files.exists(file)) {
            result = readFile(file, SkyConfig.class);
        }
        SkyblockConfig.loadSkyblocks();
        result.validate();
        writeFile(file, result);
        return result;
    }

    public void save() {
        final Path file = getConfigFile(CONFIG_FILE_NAME);
        validate();
        writeFile(file, this);
    }

    public static boolean reload(final boolean throwError) {
        final Path file = getConfigFile(CONFIG_FILE_NAME);
        try {
            CONFIG = loadConfig();
            return true;
        } catch (Exception e) {
            if (throwError) {
                throw new RuntimeException("Error loading config: " + file, e);
            }
            log.error("Error reloading: " + file, e);
            return false;
        }
    }
}