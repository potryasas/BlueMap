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

import de.bluecolored.bluemap.core.util.Key;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTFile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.bluecolored.bluemap.core.map.renderstate.MapTileState.SHIFT;

@Data
public class TileInfoRegion implements CellStorage.Cell {

    private static final int REGION_LENGTH = 1 << SHIFT;
    private static final int REGION_MASK = REGION_LENGTH - 1;
    private static final int TILES_PER_REGION = REGION_LENGTH * REGION_LENGTH;

    private int[] lastRenderTimes;
    private TileState[] tileStates;

    private transient boolean modified;

    private TileInfo[] tileInfos;

    private TileInfoRegion() {}

    private void init() {
        this.tileInfos = new TileInfo[TILES_PER_REGION];
        for (int i = 0; i < tileInfos.length; i++) {
            tileInfos[i] = new TileInfo(0, TileState.UNKNOWN);
        }
    }

    public TileInfo get(int x, int z) {
        return getTileInfo(x, z);
    }

    public TileInfo set(int x, int z, TileInfo info) {
        TileInfo old = getTileInfo(x, z);
        setTileInfo(x, z, info);
        modified = true;
        return old;
    }

    public void readFromNBT(NBTCompound compound) {
        if (compound.hasKey("last-render-times")) {
            this.lastRenderTimes = compound.getIntArray("last-render-times");
        }
        if (compound.hasKey("tile-states")) {
            NBTCompound statesCompound = compound.getCompound("tile-states");
            List<TileState> states = new ArrayList<>();
            for (String key : statesCompound.getKeys()) {
                states.add(TileState.REGISTRY.get(Key.parse(key)));
            }
            this.tileStates = states.toArray(new TileState[states.size()]);
        }
    }

    public void writeToNBT(NBTCompound compound) {
        compound.setIntArray("last-render-times", lastRenderTimes);
        NBTCompound statesCompound = compound.getCompound("tile-states");
        for (TileState state : tileStates) {
            statesCompound.setString(state.getKey().getFormatted(), state.getKey().getFormatted());
        }
    }

    public TileInfo getTileInfo(int x, int z) {
        return tileInfos[index(x, z)];
    }

    public void setTileInfo(int x, int z, TileInfo tileInfo) {
        tileInfos[index(x, z)] = tileInfo;
        modified = true;
    }

    int findLatestRenderTime() {
        if (lastRenderTimes == null) return -1;
        int max = -1;
        for (int time : lastRenderTimes) {
            if (time > max) max = time;
        }
        return max;
    }

    private static int index(int x, int z) {
        int zPart = (z & REGION_MASK) * REGION_LENGTH;
        int xPart = (x & REGION_MASK);
        return zPart + xPart;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Data
    @AllArgsConstructor
    public static class TileInfo {
        private int renderTime;
        private TileState state;
    }

    public static TileInfoRegion create() {
        TileInfoRegion region = new TileInfoRegion();
        region.init();
        return region;
    }

    /**
     * Only loads the palette-part from a TileState-file.
     *
     * @param in the input stream containing the TileState NBT data
     * @return an array of {@link TileState} representing the palette loaded from the input stream
     * @throws IOException if an I/O error occurs during reading or file operations
     */
    public static TileState[] loadPalette(InputStream in) throws IOException {
        File tempFile = File.createTempFile("bluemap", ".nbt");
        try {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            NBTFile nbtFile = new NBTFile(tempFile);
            NBTCompound tileStatesCompound = nbtFile.getCompound("tile-states");
            NBTCompound paletteCompound = tileStatesCompound.getCompound("palette");
            List<TileState> palette = new ArrayList<>();
            for (String key : paletteCompound.getKeys()) {
                palette.add(TileState.REGISTRY.get(Key.parse(key)));
            }
            return palette.toArray(new TileState[0]);
        } finally {
            tempFile.delete();
        }
    }
}
