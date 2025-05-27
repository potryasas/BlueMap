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
import de.bluecolored.bluemap.core.storage.GridStorage;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTFile;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CellStorage<T extends CellStorage.Cell> {

    private final Map<Long, T> cells;
    private final GridStorage storage;
    private final Class<T> cellType;

    public CellStorage(GridStorage storage, Class<T> cellType) {
        this.cells = new HashMap<>();
        this.storage = storage;
        this.cellType = cellType;
    }

    protected T cell(int x, int z) {
        long key = key(x, z);
        T cell = cells.get(key);
        if (cell == null) {
            cell = createNewCell();
            cells.put(key, cell);
        }
        return cell;
    }

    public void clear() {
        cells.clear();
    }

    public void save() throws IOException {
        for (Map.Entry<Long, T> entry : cells.entrySet()) {
            if (!entry.getValue().isModified()) continue;
            long key = entry.getKey();
            int x = (int) (key >> 32);
            int z = (int) (key & 0xFFFFFFFFL);
            
            File tempFile = File.createTempFile("bluemap", ".nbt");
            try {
                NBTFile nbtFile = new NBTFile(tempFile);
                NBTCompound cellCompound = nbtFile.getCompound("cell");
                entry.getValue().writeToNBT(cellCompound);
                nbtFile.save();
                
                // Copy the NBT data to the storage using a buffer
                try (OutputStream out = storage.cell(x, z).write()) {
                    byte[] buffer = new byte[8192];
                    java.io.FileInputStream fis = new java.io.FileInputStream(tempFile);
                    try {
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    } finally {
                        fis.close();
                    }
                }
            } finally {
                if (!tempFile.delete()) {
                    tempFile.deleteOnExit();
                }
            }
        }
    }

    public void load() throws IOException {
        List<GridStorage.Cell> cellList = new ArrayList<>();
        storage.stream().forEach(cell -> {
            try {
                cellList.add(cell);
            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw new RuntimeException(e);
                }
                throw e;
            }
        });
        
        for (GridStorage.Cell cell : cellList) {
            File tempFile = File.createTempFile("bluemap", ".nbt");
            try {
                // Copy the data to a temporary file using a buffer
                try (OutputStream out = new java.io.FileOutputStream(tempFile)) {
                    java.io.InputStream in = cell.read();
                    if (in != null) {
                        try {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        } finally {
                            in.close();
                        }
                    }
                }
                
                NBTFile nbtFile = new NBTFile(tempFile);
                NBTCompound cellCompound = nbtFile.getCompound("cell");
                T regionCell = createNewCell();
                regionCell.readFromNBT(cellCompound);
                cells.put(key(cell.getX(), cell.getZ()), regionCell);
            } catch (Exception ex) {
                Logger.global.logWarning("Failed to load cell at " + cell.getX() + ", " + cell.getZ() + ": " + ex.getMessage());
            } finally {
                if (!tempFile.delete()) {
                    tempFile.deleteOnExit();
                }
            }
        }
    }

    protected abstract T createNewCell();

    private static long key(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    public interface Cell {
        boolean isModified();
        void readFromNBT(NBTCompound compound);
        void writeToNBT(NBTCompound compound);
    }

}
