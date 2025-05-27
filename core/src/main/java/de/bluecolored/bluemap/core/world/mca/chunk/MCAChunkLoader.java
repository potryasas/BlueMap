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

import de.bluecolored.bluemap.core.storage.compression.Compression;
import de.bluecolored.bluemap.core.world.Chunk;
import de.bluecolored.bluemap.core.world.mca.ChunkLoader;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.MCAWorld;
import de.tr7zw.nbtapi.NBTFile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class MCAChunkLoader implements ChunkLoader<Chunk> {

    private final MCAWorld world;
    // Create a static temporary file for the dummy NBTFile
    private static final File TEMP_NBT_FILE;
    
    static {
        File tempFile;
        try {
            tempFile = File.createTempFile("bluemap-dummy-nbt", ".dat");
            tempFile.deleteOnExit();
        } catch (IOException e) {
            tempFile = new File("bluemap-dummy-nbt.dat");
        }
        TEMP_NBT_FILE = tempFile;
    }

    public MCAChunkLoader(MCAWorld world) {
        this.world = world;
    }

    // Using concrete types for each loader to avoid type issues with Java 8
    private static final ChunkVersionLoader<Chunk_1_18.Data> LOADER_1_18 = new ChunkVersionLoader<>(
            Chunk_1_18.Data.class,
            new BiFunction<MCAWorld, Chunk_1_18.Data, Chunk>() {
                @Override
                public Chunk apply(MCAWorld world, Chunk_1_18.Data data) {
                    try {
                        // Create a dummy NBTFile for compatibility
                        NBTFile dummyFile = new NBTFile(TEMP_NBT_FILE);
                        // Create chunk and cast to Chunk interface
                        Chunk_1_18 chunk = new Chunk_1_18(world, 0, 0, dummyFile);
                        return (Chunk) chunk;
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to create dummy NBTFile", e);
                    }
                }
            },
            2844
    );
    
    private static final ChunkVersionLoader<Chunk_1_16.Data> LOADER_1_16 = new ChunkVersionLoader<>(
            Chunk_1_16.Data.class,
            new BiFunction<MCAWorld, Chunk_1_16.Data, Chunk>() {
                @Override
                public Chunk apply(MCAWorld world, Chunk_1_16.Data data) {
                    return new Chunk_1_16(world, data);
                }
            },
            2500
    );
    
    private static final ChunkVersionLoader<Chunk_1_15.Data> LOADER_1_15 = new ChunkVersionLoader<>(
            Chunk_1_15.Data.class,
            new BiFunction<MCAWorld, Chunk_1_15.Data, Chunk>() {
                @Override
                public Chunk apply(MCAWorld world, Chunk_1_15.Data data) {
                    return new Chunk_1_15(world, data);
                }
            },
            2200
    );
    
    private static final ChunkVersionLoader<Chunk_1_13.Data> LOADER_1_13 = new ChunkVersionLoader<>(
            Chunk_1_13.Data.class,
            new BiFunction<MCAWorld, Chunk_1_13.Data, Chunk>() {
                @Override
                public Chunk apply(MCAWorld world, Chunk_1_13.Data data) {
                    return new Chunk_1_13(world, data);
                }
            },
            0
    );
    
    private static final List<ChunkVersionLoader<?>> CHUNK_VERSION_LOADERS = Arrays.asList(
            LOADER_1_18,
            LOADER_1_16,
            LOADER_1_15,
            LOADER_1_13
    );

    private ChunkVersionLoader<?> lastUsedLoader = CHUNK_VERSION_LOADERS.get(0);

    public Chunk load(byte[] data, int offset, int length, Compression compression) throws IOException {
        InputStream in = new ByteArrayInputStream(data, offset, length);
        in.mark(-1);

        // try last used version
        ChunkVersionLoader<?> usedLoader = lastUsedLoader;
        Chunk chunk;
        try (InputStream decompressedIn = compression.decompress(in)) {
            chunk = usedLoader.load(world, decompressedIn);
        }

        // check version and reload chunk if the wrong loader has been used and a better one has been found
        ChunkVersionLoader<?> actualLoader = findBestLoaderForVersion(getDataVersion(chunk));
        if (actualLoader != null && usedLoader != actualLoader) {
            in.reset(); // reset read position
            try (InputStream decompressedIn = compression.decompress(in)) {
                chunk = actualLoader.load(world, decompressedIn);
            }
            lastUsedLoader = actualLoader;
        }

        return chunk;
    }
    
    // Helper method to get data version from any chunk type
    private int getDataVersion(Chunk chunk) {
        if (chunk instanceof MCAChunk) {
            return ((MCAChunk) chunk).getDataVersion();
        }
        // Default to highest version for unknown chunks
        return Integer.MAX_VALUE;
    }

    @Override
    public Chunk emptyChunk() {
        return Chunk.EMPTY_CHUNK;
    }

    @Override
    public Chunk erroredChunk() {
        return Chunk.ERRORED_CHUNK;
    }

    private @Nullable ChunkVersionLoader<?> findBestLoaderForVersion(int version) {
        for (ChunkVersionLoader<?> loader : CHUNK_VERSION_LOADERS) {
            if (loader.mightSupport(version)) return loader;
        }
        return null;
    }

    @RequiredArgsConstructor
    @Getter
    private static class ChunkVersionLoader<D extends MCAChunk.Data> {

        private final Class<D> dataType;
        private final BiFunction<MCAWorld, D, Chunk> constructor;
        private final int dataVersion;

        public Chunk load(MCAWorld world, InputStream in) throws IOException {
            try {
                D data = MCAUtil.BLUENBT.read(in, dataType);
                return mightSupport(data.getDataVersion()) ? 
                    constructor.apply(world, data) : 
                    new MCAChunk(world, data) {};
            } catch (Exception e) {
                throw new IOException(String.format("Failed to parse chunk-data (%s): %s", dataType.getSimpleName(), e), e);
            }
        }

        public boolean mightSupport(int dataVersion) {
            return dataVersion >= this.dataVersion;
        }
    }
}
