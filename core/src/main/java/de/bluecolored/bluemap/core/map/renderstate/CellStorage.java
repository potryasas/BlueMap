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
package de.bluecolored.bluemap.core.map.renderstate;

import de.bluecolored.bluemap.core.logger.Logger;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTFile;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CellStorage<T extends CellStorage.Cell> {

    private final Map<Long, T> cells;
    private final CellFactory<T> cellFactory;

    public CellStorage(CellFactory<T> cellFactory) {
        this.cells = new HashMap<>();
        this.cellFactory = cellFactory;
    }

    public T getCell(int x, int z) {
        long key = key(x, z);
        T cell = cells.get(key);
        if (cell == null) {
            cell = cellFactory.create();
            cells.put(key, cell);
        }
        return cell;
    }

    public void clear() {
        cells.clear();
    }

    public void save(File file) throws IOException {
        NBTFile nbtFile = new NBTFile(file);
        NBTCompound root = nbtFile.getCompound("cells");
        for (Map.Entry<Long, T> entry : cells.entrySet()) {
            if (!entry.getValue().isModified()) continue;
            NBTCompound cellCompound = root.getCompound(String.valueOf(entry.getKey()));
            entry.getValue().writeToNBT(cellCompound);
        }
        nbtFile.save();
    }

    public void load(File file) throws IOException {
        if (!file.exists()) return;

        NBTFile nbtFile = new NBTFile(file);
        NBTCompound root = nbtFile.getCompound("cells");
        for (String key : root.getKeys()) {
            try {
                long cellKey = Long.parseLong(key);
                NBTCompound cellCompound = root.getCompound(key);
                T cell = cellFactory.create();
                cell.readFromNBT(cellCompound);
                cells.put(cellKey, cell);
            } catch (NumberFormatException ex) {
                Logger.global.logWarning("Failed to parse cell-key: " + key);
            }
        }
    }

    private static long key(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    @FunctionalInterface
    public interface CellFactory<T extends Cell> {
        T create();
    }

    public interface Cell {
        boolean isModified();
        void readFromNBT(NBTCompound compound);
        void writeToNBT(NBTCompound compound);
    }

}
