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
package de.bluecolored.bluemap.common.commands.commands;

import de.bluecolored.bluemap.common.commands.java8compat.annotations.Argument;
import de.bluecolored.bluemap.common.commands.java8compat.annotations.Command;
import de.bluecolored.bluemap.common.commands.java8compat.annotations.Parser;
import de.bluecolored.bluemap.common.commands.Permission;
import de.bluecolored.bluemap.common.config.BlueMapConfigManager;
import de.bluecolored.bluemap.common.config.ConfigurationException;
import de.bluecolored.bluemap.common.config.storage.FileConfig;
import de.bluecolored.bluemap.common.config.storage.SQLConfig;
import de.bluecolored.bluemap.common.config.storage.StorageConfig;
import de.bluecolored.bluemap.common.plugin.Plugin;
import de.bluecolored.bluemap.common.rendermanager.StorageDeleteTask;
import de.bluecolored.bluemap.common.serverinterface.CommandSource;
import de.bluecolored.bluemap.core.map.BmMap;
import de.bluecolored.bluemap.core.storage.Storage;
import de.bluecolored.bluemap.core.storage.file.FileStorage;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static de.bluecolored.bluemap.common.commands.TextFormat.*;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

@RequiredArgsConstructor
public class StoragesCommand {

    private final Plugin plugin;

    @Command("storages")
    @Permission("bluemap.storages")
    public Component storageList() {
        Map<String, Storage> loadedStorages = plugin.getBlueMap().getLoadedStorages();
        Component[] lines = plugin.getBlueMap().getConfig().getStorageConfigs().entrySet().stream()
                .map(storage -> formatStorageEntry(storage.getKey(), storage.getValue(), loadedStorages.containsKey(storage.getKey())))
                .toArray(size -> new Component[size]);
        return paragraph("Storages", lines(lines));
    }

    private Component formatStorageEntry(String id, StorageConfig storage, boolean loaded) {
        Component loadedIcon = loaded ?
                text("✔").color(POSITIVE_COLOR):
                text("❌").color(BASE_COLOR);
        return format("% %",
                loadedIcon,
                text(id).color(HIGHLIGHT_COLOR)
        ).color(BASE_COLOR);
    }

    @Command("storages <storage>")
    @Permission("bluemap.storages")
    public Component storage(CommandSource source, @Argument("storage") @Parser(name="storage-id") String storageId)
            throws ConfigurationException, InterruptedException, IOException {

        StorageConfig storageConfig = plugin.getBlueMap().getConfig().getStorageConfigs().get(storageId);
        Storage storage = getOrLoadStorage(storageId, source);

        List<Component> lines = new LinkedList<>();

        lines.add(format("Type: %",
                text(storageConfig.getStorageType().getKey().getFormatted()).color(HIGHLIGHT_COLOR)
        ).color(BASE_COLOR));

        lines.add(formatStorage(storage, storageConfig));

        lines.add(empty());
        lines.add(text("Maps:").color(BASE_COLOR));
        storage.mapIds()
                .limit(20)
                .map(mapId -> formatMapEntry(mapId, storage))
                .forEach(lines::add);

        return paragraph("Storage '" + storageId + "'", lines(lines));
    }

    @Command("storages <storage> delete <map>")
    @Permission("bluemap.storages.delete")
    public void deleteMap(
            CommandSource source,
            @Argument("storage") @Parser(name="storage-id") String storageId,
            @Argument("map") String mapId
    ) throws ConfigurationException, InterruptedException {
        Storage storage = getOrLoadStorage(storageId, source);

        if (isMapLoaded(mapId, storage)) {
            source.sendMessage(text("Can't delete a loaded map!").color(NEGATIVE_COLOR)
                    .append(format("Unload the map by removing it's config-file first,\n" +
                                   "or use % if you want to purge it.",
                            command("/bluemap purge " + mapId).color(HIGHLIGHT_COLOR)
                    ).color(BASE_COLOR)));
            return;
        }

        StorageDeleteTask task = new StorageDeleteTask(storage.map(mapId), mapId);
        plugin.getRenderManager().scheduleRenderTaskNext(task);

        source.sendMessage(lines(
                format("Scheduled a new task to delete map % from storage %",
                        text(mapId).color(HIGHLIGHT_COLOR),
                        text(storageId).color(HIGHLIGHT_COLOR)
                ).color(POSITIVE_COLOR),
                format("Use % to see the progress",
                        command("/bluemap").color(HIGHLIGHT_COLOR)
                ).color(BASE_COLOR)
        ));
    }

    private Storage getOrLoadStorage(String storageId, CommandSource source) throws ConfigurationException, InterruptedException {
        Map<String, Storage> loadedStorages = plugin.getBlueMap().getLoadedStorages();
        Storage storage = loadedStorages.get(storageId);
        if (storage != null) return storage;

        source.sendMessage(format("Initializing storage '%'...", storageId).color(BASE_COLOR));
        return plugin.getBlueMap().getOrLoadStorage(storageId);
    }

    private boolean isMapLoaded(String mapId, Storage storage) {
        BmMap map = plugin.getBlueMap().getMaps().get(mapId);
        return map != null && map.getStorage().equals(storage.map(mapId));
    }

    private Component formatMapEntry(String id, Storage storage) {
        Component loadedIcon = isMapLoaded(id, storage) ?
                text("✔").color(POSITIVE_COLOR):
                text("❌").color(BASE_COLOR);

        return format("% %",
                loadedIcon,
                text(id).color(HIGHLIGHT_COLOR)
        ).color(BASE_COLOR);
    }

    private Component formatStorage(Storage storage, StorageConfig storageConfig) {
        Component storageInfo;

        if (storage instanceof FileStorage) {
            FileStorage fileStorage = (FileStorage) storage;
            storageInfo = format("File-Storage: %", fileStorage.getRoot());
        } else {
            storageInfo = format("Storage: %", storage.getClass().getSimpleName());
        }

        if (storageConfig instanceof FileConfig) {
            FileConfig fileConfig = (FileConfig) storageConfig;
            storageInfo = storageInfo.append(Component.newline())
                    .append(format("Root: %", fileConfig.getRoot()));
        } else if (storageConfig instanceof SQLConfig) {
            SQLConfig sqlConfig = (SQLConfig) storageConfig;
            storageInfo = storageInfo.append(Component.newline())
                    .append(format("URL: %", sqlConfig.getConnectionUrl()));
        }

        return storageInfo;
    }

}
