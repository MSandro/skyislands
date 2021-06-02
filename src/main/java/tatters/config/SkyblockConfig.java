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
package tatters.config;

import static tatters.TattersMain.log;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import tatters.TattersMain;

public class SkyblockConfig extends Config {

    private static final List<List<String>> DEFAULT_LAYERS = Collections.emptyList();

    private static final Path SKYBLOCKS_DIR = CONFIG_DIR.resolve("skyblocks");
    private static Set<String> activeSkyblockConfigs = Sets.newHashSet();

    public final String enabledComment = "Set to false to not load this skyblock";
    public boolean enabled = true;

    public transient String fileName;
    public final String nameComment = "The name to display to the user, can be a translation key";
    public String name;

    public final String layersComment = "layers, rows, columns of characters specifying block keys, blank is air, ! is the spawn point";
    public List<List<String>> layers = DEFAULT_LAYERS;

    public final String mappingComment = "block key definitions: key -> block, properties, nbt";
    public Map<Character, SkyblockBlockDefinition> mapping = Maps.newLinkedHashMap();

    public final String fillersComment = "list of block and thickness (default 1)";
    public List<SkyblockFillerDefinition> fillers;

    public void validate() {
        if (!this.enabled)
            return;
        final Set<Character> blockKeys = Sets.newHashSet();
        for (List<String> row : this.layers) {
            for (String column : row) {
                if (column == null) {
                    throw new IllegalArgumentException("Null data in layers - misplaced syntax?");
                }
                column.chars().forEach(c -> blockKeys.add((char) c));
            }
        }
        blockKeys.remove(' ');
        blockKeys.remove('!');
        if (blockKeys.isEmpty()) {
            throw new IllegalArgumentException("No blocks defined in layers");
        }

        final Set<Character> keys = Sets.newHashSet();
        this.mapping.forEach((key, blockDef) -> {
            if (key == ' ') {
                throw new IllegalArgumentException("Cannot map the space character, it is reserved for the air block");
            }
            if (key == '!') {
                throw new IllegalArgumentException("Cannot map the ! character, it is reserved for the spawn point");
            }
            keys.add(key);
            if (blockDef.block == null || blockDef.block.isEmpty()) {
                throw new IllegalArgumentException("No block defined for " + key);
            }
            blockDef.validate();
        });

        blockKeys.removeAll(keys);
        if (!blockKeys.isEmpty()) {
            throw new IllegalArgumentException("Blocks have no mapping: " + blockKeys);
        }

        if (this.fillers != null) {
            this.fillers.stream().forEach((filler) -> filler.validate());
        }
    }

    public List<FlatLayerInfo> getFiller() {
        final List<FlatLayerInfo> result = Lists.newArrayList();
        if (this.fillers != null) {
            this.fillers.forEach(filler -> result.add(filler.getChunkGeneratorLayer()));
        }
        return result;
    }

    static SkyblockConfig getSkyblockConfig(final String name) {
        return getSkyblockConfig(name, false);
    }

    static SkyblockConfig getSkyblockConfig(final String name, final boolean ignoreDisabled) {
        final Path file = SKYBLOCKS_DIR.resolve(name);
        final SkyblockConfig result = readFile(file, SkyblockConfig.class);
        if (!result.enabled) {
            if (ignoreDisabled) {
                return null;
            }
            throw new IllegalStateException("Tried to load disabled skyblock: " + name);
        }
        result.fileName = name;
        result.validate();
        if (result.name == null) {
            result.name = name;
        }
        return result;
    }

    static List<SkyblockConfig> getActiveSkyblockConfigs() {
        return activeSkyblockConfigs.stream().map((path) -> {
            try {
                return getSkyblockConfig(path, true);
            } catch (Exception e) {
                log.warn("Ignoring skyblock: " + path, e);
                return null;
            }
        }).filter(Predicates.notNull()).collect(Collectors.toList());
    }

    static void copySkyblocks() {
        mkdirs(SKYBLOCKS_DIR);

        final Path skyblocks = TattersMain.getModContainer().getPath("assets/tatters/skyblocks");
        try {
            Files.list(skyblocks).filter(path -> path.getFileName().toString().endsWith(".json")).forEach(path -> {
                try {
                    final String fileName = path.getFileName().toString();
                    final Path destination = SKYBLOCKS_DIR.resolve(fileName);
                    if (!Files.exists(destination)) {
                        final SkyblockConfig skyblock = readFile(path, SkyblockConfig.class);
                        skyblock.fileName = fileName;
                        skyblock.validate();
                        writeFile(destination, skyblock);
                    }
                } catch (Exception e) {
                    log.warn("Not copying: " + path, e);
                }
            });
        }
        catch (Exception e) {
            throw new RuntimeException("Error copying skyblocks", e);
        }
    }

    static void loadSkyblocks() {
        final Set<String> set = Sets.newHashSet();
        try {
            Files.list(SKYBLOCKS_DIR).filter(path -> path.getFileName().toString().endsWith(".json")).forEach(path -> {
                try {
                    final SkyblockConfig skyblock = readFile(path, SkyblockConfig.class);
                    if (skyblock.enabled) {
                        final String pathName = path.getFileName().toString();
                        skyblock.validate();
                        skyblock.fileName = pathName;
                        if (skyblock.name == null || skyblock.name.isEmpty()) {
                            skyblock.name = pathName;
                        }
                        set.add(pathName);
                    }
                } catch (Exception e) {
                    log.warn("Not loading: " + path, e);
                }
            });
            activeSkyblockConfigs = set;
        }
        catch (Exception e) {
            throw new RuntimeException("Error loading skyblocks", e);
        }
    }
}
