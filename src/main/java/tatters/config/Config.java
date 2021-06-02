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

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import tatters.TattersMain;

public class Config {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    protected static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(TattersMain.MOD_ID).normalize();

    protected static Path getConfigFile(final String name) {
        return CONFIG_DIR.resolve(name).normalize();
    }

    protected static <T> T readFile(final Path file, final Class<T> type) {
        try (final InputStreamReader reader = new InputStreamReader(Files.newInputStream(file))) {
            return GSON.fromJson(reader, type);
        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + file, e);
        }
    }

    protected static void writeFile(final Path file, final Object object) {
        mkdirs(file.getParent());
        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(file))) {
            GSON.toJson(object, writer);
        } catch (Exception e) {
            throw new RuntimeException("Error writing file: " + file, e);
        }
    }

    protected static void mkdirs(final Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            throw new RuntimeException("Error creating directories: " + dir, e);
        }
    }}
