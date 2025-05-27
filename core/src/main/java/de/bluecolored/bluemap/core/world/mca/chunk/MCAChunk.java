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
package de.bluecolored.bluemap.core.world.mca.chunk;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.Chunk;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.mca.MCAWorld;
import de.tr7zw.nbtapi.NBTCompound;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
public abstract class MCAChunk implements Chunk {

    protected static final int BLOCKS_PER_SECTION = 16 * 16 * 16;
    protected static final int BIOMES_PER_SECTION = 4 * 4 * 4;
    protected static final int VALUES_PER_HEIGHTMAP = 16 * 16;

    protected static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    protected static final int[] EMPTY_INT_ARRAY = new int[0];
    protected static final long[] EMPTY_LONG_ARRAY = new long[0];
    protected static final Key[] EMPTY_KEY_ARRAY = new Key[0];
    protected static final BlockState[] EMPTY_BLOCKSTATE_ARRAY = new BlockState[0];
    protected static final BlockEntity[] EMPTY_BLOCK_ENTITIES_ARRAY = new BlockEntity[0];

    private final MCAWorld world;
    private final int dataVersion;

    public MCAChunk(MCAWorld world, Data chunkData) {
        this.world = world;
        this.dataVersion = chunkData.getDataVersion();
    }

    /**
     * Gets the position of this chunk as a vector
     * @return A string representation of this chunk's position
     */
    public String getPosition() {
        return "ChunkPosition";  // Placeholder - needs to be implemented by subclasses
    }
    
    /**
     * Gets the sections in this chunk
     * @return A map containing the sections
     */
    public Map<String, Object> getSections() {
        return new HashMap<>();  // Placeholder - needs to be implemented by subclasses
    }

    @SuppressWarnings("FieldMayBeFinal")
    @Getter
    public static class Data {
        private int dataVersion = 0;

        public void readFromNBT(NBTCompound compound) {
            if (compound.hasKey("DataVersion")) {
                this.dataVersion = compound.getInteger("DataVersion");
            }
        }

        public void writeToNBT(NBTCompound compound) {
            compound.setInteger("DataVersion", dataVersion);
        }
    }

}
