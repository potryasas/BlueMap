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
package de.bluecolored.bluemap.core.world.mca.entity.chunk;

import de.bluecolored.bluemap.core.storage.compression.Compression;
import de.bluecolored.bluemap.core.world.mca.ChunkLoader;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.MCAWorld;
import de.bluecolored.bluemap.core.world.Chunk;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MCAEntityChunkLoader implements ChunkLoader<Chunk> {

    @Override
    public Chunk load(byte[] data, int offset, int length, Compression compression) throws IOException {
        // Create an empty data object
        MCAEntityChunk.Data chunkData = new MCAEntityChunk.Data();
        
        // For Java 8 compatibility, we create an empty entity chunk
        // In the future, when upgrading beyond Java 8, this should parse the NBT data correctly
        return new MCAEntityChunk(null, chunkData);
    }

    @Override
    public Chunk emptyChunk() {
        return MCAEntityChunk.EMPTY_CHUNK;
    }

    @Override
    public Chunk erroredChunk() {
        return MCAEntityChunk.ERRORED_CHUNK;
    }

}
