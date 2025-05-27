/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.common.config;

import de.bluecolored.bluemap.common.BlueMapConfiguration;
import de.bluecolored.bluemap.common.config.storage.StorageConfig;
import de.bluecolored.bluemap.common.serverinterface.ServerWorld;
import de.bluecolored.bluemap.core.BlueMap;
import de.bluecolored.bluemap.core.logger.Logger;
import de.bluecolored.bluemap.core.resources.pack.datapack.DataPack;
import de.bluecolored.bluemap.core.util.FileHelper;
import de.bluecolored.bluemap.core.util.Key;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class BlueMapConfigManager implements BlueMapConfiguration {

    public static final String CORE_CONFIG_NAME = "core";
    public static final String WEBSERVER_CONFIG_NAME = "webserver";
    public static final String WEBAPP_CONFIG_NAME = "webapp";
    public static final String PLUGIN_CONFIG_NAME = "plugin";
    public static final String MAPS_CONFIG_FOLDER_NAME = "maps";
    public static final String STORAGES_CONFIG_FOLDER_NAME = "storages";

    public static final String MAP_STORAGE_CONFIG_NAME = MAPS_CONFIG_FOLDER_NAME + "/map";

    public static final String FILE_STORAGE_CONFIG_NAME = STORAGES_CONFIG_FOLDER_NAME + "/file";
    public static final String SQL_STORAGE_CONFIG_NAME = STORAGES_CONFIG_FOLDER_NAME + "/sql";

    private final ConfigManager configManager;

    private final CoreConfig coreConfig;
    private final WebserverConfig webserverConfig;
    private final WebappConfig webappConfig;
    private final PluginConfig pluginConfig;
    private final Map<String, MapConfig> mapConfigs;
    private final Map<String, StorageConfig> storageConfigs;
    private final Path packsFolder;
    private final @Nullable String minecraftVersion;
    private final @Nullable Path modsFolder;

    @Builder
    private BlueMapConfigManager(
            @NonNull Path configRoot,
            @Nullable String minecraftVersion,
            @Nullable Path defaultDataFolder,
            @Nullable Path defaultWebroot,
            @Nullable Collection<ServerWorld> autoConfigWorlds,
            @Nullable Boolean usePluginConfig,
            @Nullable Boolean useMetricsConfig,
            @Nullable Path packsFolder,
            @Nullable Path modsFolder
    ) throws ConfigurationException {
        // set defaults
        if (defaultDataFolder == null) defaultDataFolder = Paths.get("bluemap");
        if (defaultWebroot == null) defaultWebroot = Paths.get("bluemap", "web");
        if (autoConfigWorlds == null) autoConfigWorlds = Collections.emptyList();
        if (usePluginConfig == null) usePluginConfig = true;
        if (useMetricsConfig == null) useMetricsConfig = true;
        if (packsFolder == null) packsFolder = configRoot.resolve("packs");

        // load
        this.configManager = new ConfigManager(configRoot);
        this.coreConfig = loadCoreConfig(defaultDataFolder, useMetricsConfig);
        this.webappConfig = loadWebappConfig(defaultWebroot);
        this.webserverConfig = loadWebserverConfig(webappConfig.getWebroot(), coreConfig.getData());
        this.pluginConfig = usePluginConfig ? loadPluginConfig() : new PluginConfig();
        this.storageConfigs = Collections.unmodifiableMap(loadStorageConfigs(webappConfig.getWebroot()));
        this.mapConfigs = Collections.unmodifiableMap(loadMapConfigs(autoConfigWorlds));
        this.packsFolder = packsFolder;
        this.minecraftVersion = minecraftVersion;
        this.modsFolder = modsFolder;
    }

    private CoreConfig loadCoreConfig(Path defaultDataFolder, boolean useMetricsConfig) throws ConfigurationException {
        Path configFile = configManager.resolveConfigFile(CORE_CONFIG_NAME);
        Path configFolder = configFile.getParent();

        if (!Files.exists(configFile)) {
            try {
                FileHelper.createDirectories(configFolder);
                String content = configManager.loadConfigTemplate(CORE_CONFIG_NAME)
                        .setConditional("metrics", useMetricsConfig)
                        .setVariable("timestamp", LocalDateTime.now().withNano(0).toString())
                        .setVariable("version", BlueMap.VERSION)
                        .setVariable("mcVersion", minecraftVersion)
                        .setVariable("data", formatPath(defaultDataFolder))
                        .setVariable("implementation", "bukkit")
                        .setVariable("render-thread-count", Integer.toString(suggestRenderThreadCount()))
                        .setVariable("logfile", formatPath(defaultDataFolder.resolve("logs").resolve("debug.log")))
                        .setVariable("logfile-with-time", formatPath(defaultDataFolder.resolve("logs").resolve("debug_%1$tF_%1$tT.log")))
                        .build();
                Files.write(configFile, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException | NullPointerException ex) {
                Logger.global.logWarning("Failed to create default core-configuration-file: " + ex);
            }
        }

        return configManager.loadConfig(CORE_CONFIG_NAME, CoreConfig.class);
    }

    /**
     * determine render-thread preset (very pessimistic, rather let people increase it themselves)
     */
    private int suggestRenderThreadCount() {
        Runtime runtime = Runtime.getRuntime();
        int availableCores = runtime.availableProcessors();
        long availableMemoryMiB = runtime.maxMemory() / 1024L / 1024L;
        int presetRenderThreadCount = 1;
        if (availableCores >= 6 && availableMemoryMiB >= 4096)
            presetRenderThreadCount = 2;
        if (availableCores >= 10 && availableMemoryMiB >= 8192)
            presetRenderThreadCount = 3;
        return presetRenderThreadCount;
    }

    private WebserverConfig loadWebserverConfig(Path defaultWebroot, Path dataRoot) throws ConfigurationException {
        Path configFile = configManager.resolveConfigFile(WEBSERVER_CONFIG_NAME);
        Path configFolder = configFile.getParent();

        if (!Files.exists(configFile)) {
            try {
                FileHelper.createDirectories(configFolder);
                String content = configManager.loadConfigTemplate(WEBSERVER_CONFIG_NAME)
                        .setVariable("webroot", formatPath(defaultWebroot))
                        .setVariable("logfile", formatPath(dataRoot.resolve("logs").resolve("webserver.log")))
                        .setVariable("logfile-with-time", formatPath(dataRoot.resolve("logs").resolve("webserver_%1$tF_%1$tT.log")))
                        .build();
                Files.write(configFile, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException | NullPointerException ex) {
                Logger.global.logWarning("Failed to create default webserver-configuration-file: " + ex);
            }
        }

        return configManager.loadConfig(WEBSERVER_CONFIG_NAME, WebserverConfig.class);
    }

    private WebappConfig loadWebappConfig(Path defaultWebroot) throws ConfigurationException {
        Path configFile = configManager.resolveConfigFile(WEBAPP_CONFIG_NAME);
        Path configFolder = configFile.getParent();

        if (!Files.exists(configFile)) {
            try {
                FileHelper.createDirectories(configFolder);
                String content = configManager.loadConfigTemplate(WEBAPP_CONFIG_NAME)
                        .setVariable("webroot", formatPath(defaultWebroot))
                        .build();
                Files.write(configFile, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException | NullPointerException ex) {
                Logger.global.logWarning("Failed to create default webapp-configuration-file: " + ex);
            }
        }

        return configManager.loadConfig(WEBAPP_CONFIG_NAME, WebappConfig.class);
    }

    private PluginConfig loadPluginConfig() throws ConfigurationException {
        Path configFile = configManager.resolveConfigFile(PLUGIN_CONFIG_NAME);
        Path configFolder = configFile.getParent();

        if (!Files.exists(configFile)) {
            try {
                FileHelper.createDirectories(configFolder);
                String content = configManager.loadConfigTemplate(PLUGIN_CONFIG_NAME)
                        .build();
                Files.write(configFile, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException | NullPointerException ex) {
                Logger.global.logWarning("Failed to create default webapp-configuration-file: " + ex);
            }
        }

        return configManager.loadConfig(PLUGIN_CONFIG_NAME, PluginConfig.class);
    }

    private Map<String, MapConfig> loadMapConfigs(Collection<ServerWorld> autoConfigWorlds) throws ConfigurationException {
        Map<String, MapConfig> mapConfigs = new HashMap<>();

        Path mapConfigFolder = configManager.getConfigRoot().resolve(MAPS_CONFIG_FOLDER_NAME);

        if (!Files.exists(mapConfigFolder)){
            try {
                FileHelper.createDirectories(mapConfigFolder);
                if (autoConfigWorlds.isEmpty()) {
                    Path worldFolder = Paths.get("world");
                    String content = createOverworldMapTemplate("Overworld", worldFolder,
                            DataPack.DIMENSION_OVERWORLD, 0).build();
                    Files.write(configManager.resolveConfigFile(MAPS_CONFIG_FOLDER_NAME + "/overworld"),
                            content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                    content = createNetherMapTemplate("Nether", worldFolder,
                            DataPack.DIMENSION_THE_NETHER, 0).build();
                    Files.write(configManager.resolveConfigFile(MAPS_CONFIG_FOLDER_NAME + "/nether"),
                            content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                    content = createEndMapTemplate("End", worldFolder,
                            DataPack.DIMENSION_THE_END, 0).build();
                    Files.write(configManager.resolveConfigFile(MAPS_CONFIG_FOLDER_NAME + "/end"),
                            content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                } else {
                    // make sure overworld-dimensions come first, so they are the ones where the
                    // world-folder is configured
                    List<ServerWorld> sortedWorlds = new ArrayList<>(autoConfigWorlds);
                    Collections.sort(sortedWorlds, (w1, w2) -> {
                        if (w1.getDimension().equals(DataPack.DIMENSION_OVERWORLD)) return -1;
                        if (w2.getDimension().equals(DataPack.DIMENSION_OVERWORLD)) return 1;
                        return 0;
                    });

                    int index = 0;
                    for (ServerWorld world : sortedWorlds) {
                        Path worldFolder = world.getWorldFolder();
                        String worldName = worldFolder.getFileName().toString();
                        String mapName = worldName;
                        if (world.getDimension().equals(DataPack.DIMENSION_THE_NETHER)) {
                            mapName = worldName + " (Nether)";
                        } else if (world.getDimension().equals(DataPack.DIMENSION_THE_END)) {
                            mapName = worldName + " (End)";
                        }

                        Path configFile = configManager.resolveConfigFile(MAPS_CONFIG_FOLDER_NAME + "/" + sanitiseMapId(worldName));
                        ConfigTemplate template;

                        Key dimension = world.getDimension();
                        if (dimension.equals(DataPack.DIMENSION_THE_NETHER)) {
                            template = createNetherMapTemplate(mapName, worldFolder, dimension, index);
                        } else if (dimension.equals(DataPack.DIMENSION_THE_END)) {
                            template = createEndMapTemplate(mapName, worldFolder, dimension, index);
                        } else {
                            template = createOverworldMapTemplate(mapName, worldFolder, dimension, index);
                        }

                        String content = template.build();
                        Files.write(configFile, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        index++;
                    }
                }
            } catch (IOException | NullPointerException ex) {
                Logger.global.logWarning("Failed to create default map-configuration-files: " + ex);
            }
        }

        List<Path> configFiles = new ArrayList<>();
        try (Stream<Path> files = Files.list(mapConfigFolder)) {
            configFiles = files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".conf"))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            Logger.global.logWarning("Failed to list map-configuration-files: " + ex);
        }

        for (Path configFile : configFiles.toArray(new Path[0])) {
            try {
                String id = configFile.getFileName().toString();
                id = id.substring(0, id.length() - 5); // remove .conf
                MapConfig config = configManager.loadConfig(MAPS_CONFIG_FOLDER_NAME + "/" + id, MapConfig.class);
                mapConfigs.put(id, config);
            } catch (ConfigurationException ex) {
                Logger.global.logWarning("Failed to load map-configuration: " + configFile.getFileName() + " (" + ex.getMessage() + ")");
            }
        }

        return mapConfigs;
    }

    private Map<String, StorageConfig> loadStorageConfigs(Path defaultWebroot) throws ConfigurationException {
        Map<String, StorageConfig> storageConfigs = new HashMap<>();

        Path storageConfigFolder = configManager.getConfigRoot().resolve(STORAGES_CONFIG_FOLDER_NAME);

        if (!Files.exists(storageConfigFolder)) {
            try {
                FileHelper.createDirectories(storageConfigFolder);
                String content = configManager.loadConfigTemplate(FILE_STORAGE_CONFIG_NAME)
                        .setVariable("webroot", formatPath(defaultWebroot))
                        .build();
                Files.write(configManager.resolveConfigFile(FILE_STORAGE_CONFIG_NAME),
                        content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

                content = configManager.loadConfigTemplate(SQL_STORAGE_CONFIG_NAME)
                        .build();
                Files.write(configManager.resolveConfigFile(SQL_STORAGE_CONFIG_NAME),
                        content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException | NullPointerException ex) {
                Logger.global.logWarning("Failed to create default storage-configuration-files: " + ex);
            }
        }

        List<Path> configFiles = new ArrayList<>();
        try (Stream<Path> files = Files.list(storageConfigFolder)) {
            configFiles = files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".conf"))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            Logger.global.logWarning("Failed to list storage-configuration-files: " + ex);
        }

        for (Path configFile : configFiles.toArray(new Path[0])) {
            try {
                String id = configFile.getFileName().toString();
                id = id.substring(0, id.length() - 5); // remove .conf
                StorageConfig config = configManager.loadConfig(STORAGES_CONFIG_FOLDER_NAME + "/" + id, StorageConfig.class);
                storageConfigs.put(id, config);
            } catch (ConfigurationException ex) {
                Logger.global.logWarning("Failed to load storage-configuration: " + configFile.getFileName() + " (" + ex.getMessage() + ")");
            }
        }

        return storageConfigs;
    }

    private String sanitiseMapId(String id) {
        return id.toLowerCase().replaceAll("[^a-z0-9\\-_]", "_");
    }

    private ConfigTemplate createOverworldMapTemplate(String name, Path worldFolder, Key dimension, int index) throws IOException {
        return configManager.loadConfigTemplate(MAP_STORAGE_CONFIG_NAME)
                .setVariable("name", name)
                .setVariable("world", formatPath(worldFolder))
                .setVariable("dimension", dimension.getFormatted())
                .setVariable("order", Integer.toString(index))
                .setVariable("storage", "file");
    }

    private ConfigTemplate createNetherMapTemplate(String name, Path worldFolder, Key dimension, int index) throws IOException {
        return configManager.loadConfigTemplate(MAP_STORAGE_CONFIG_NAME)
                .setVariable("name", name)
                .setVariable("world", formatPath(worldFolder))
                .setVariable("dimension", dimension.getFormatted())
                .setVariable("order", Integer.toString(index))
                .setVariable("storage", "file");
    }

    private ConfigTemplate createEndMapTemplate(String name, Path worldFolder, Key dimension, int index) throws IOException {
        return configManager.loadConfigTemplate(MAP_STORAGE_CONFIG_NAME)
                .setVariable("name", name)
                .setVariable("world", formatPath(worldFolder))
                .setVariable("dimension", dimension.getFormatted())
                .setVariable("order", Integer.toString(index))
                .setVariable("storage", "file");
    }

    public static String formatPath(Path path) {
        return path.toString().replace(FileSystems.getDefault().getSeparator(), "/");
    }

}
