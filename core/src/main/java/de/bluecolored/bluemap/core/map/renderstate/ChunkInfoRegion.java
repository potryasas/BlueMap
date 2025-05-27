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

import de.tr7zw.nbtapi.NBTCompound;
import lombok.Data;

import java.util.Arrays;

@Data
public class ChunkInfoRegion implements CellStorage.Cell {

    private static final int REGION_LENGTH = 32;
    private static final int REGION_MASK = REGION_LENGTH - 1;
    private static final int CHUNKS_PER_REGION = REGION_LENGTH * REGION_LENGTH;

    private long[] chunkHashes;

    private transient boolean modified;

    private ChunkInfo[] chunkInfos;

    private ChunkInfoRegion() {}

    private void init() {
        this.chunkInfos = new ChunkInfo[CHUNKS_PER_REGION];
        for (int i = 0; i < chunkInfos.length; i++) {
            chunkInfos[i] = new ChunkInfo(0);
        }
    }

    public long get(int x, int z) {
        return getChunkInfo(x, z).getHash();
    }

    public long set(int x, int z, long hash) {
        ChunkInfo old = getChunkInfo(x, z);
        long oldHash = old.getHash();
        setChunkInfo(x, z, new ChunkInfo(hash));
        modified = true;
        return oldHash;
    }

    public void readFromNBT(NBTCompound compound) {
        if (compound.hasKey("chunk-hashes")) {
            this.chunkHashes = compound.getLongArray("chunk-hashes");
            if (chunkHashes != null) {
                for (int i = 0; i < chunkHashes.length && i < chunkInfos.length; i++) {
                    chunkInfos[i] = new ChunkInfo(chunkHashes[i]);
                }
            }
        }
    }

    public void writeToNBT(NBTCompound compound) {
        long[] hashes = new long[chunkInfos.length];
        for (int i = 0; i < chunkInfos.length; i++) {
            hashes[i] = chunkInfos[i].getHash();
        }
        compound.setLongArray("chunk-hashes", hashes);
    }

    public ChunkInfo getChunkInfo(int x, int z) {
        return chunkInfos[index(x, z)];
    }

    public void setChunkInfo(int x, int z, ChunkInfo chunkInfo) {
        chunkInfos[index(x, z)] = chunkInfo;
        modified = true;
    }

    private static int index(int x, int z) {
        int zPart = (z & REGION_MASK) * REGION_LENGTH;
        int xPart = (x & REGION_MASK);
        return zPart + xPart;
    }

    public static ChunkInfoRegion create() {
        ChunkInfoRegion region = new ChunkInfoRegion();
        region.init();
        return region;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Data
    public static class ChunkInfo {
        private long hash;

        public ChunkInfo(long hash) {
            this.hash = hash;
        }
    }

}
