package de.bluecolored.bluemap.core.world;

import java.io.IOException;

/**
 * Represents a region consisting of multiple chunks.
 *
 * @param <T> the chunk type
 */
public interface Region<T> {

    /**
     * Directly loads and returns the specified chunk.<br>
     * (implementations should consider overriding this method for a faster implementation)
     *
     * @param chunkX the chunk x coordinate
     * @param chunkZ the chunk z coordinate
     * @return the loaded chunk, or an empty chunk if not found
     * @throws IOException if an I/O error occurs while loading the chunk
     */
    default T loadChunk(int chunkX, int chunkZ) throws IOException {
        class SingleChunkConsumer implements ChunkConsumer<T> {
            private T foundChunk = emptyChunk();

            @Override
            public boolean filter(int x, int z, int lastModified) {
                return x == chunkX && z == chunkZ;
            }

            @Override
            public void accept(int chunkX, int chunkZ, T chunk) {
                this.foundChunk = chunk;
            }

        }

        SingleChunkConsumer singleChunkConsumer = new SingleChunkConsumer();
        iterateAllChunks(singleChunkConsumer);
        return singleChunkConsumer.foundChunk;
    }

    /**
     * Iterates over all chunks in this region and first calls {@link ChunkConsumer#filter(int, int, int)}.<br>
     * And if (and only if) that method returned <code>true</code>, the chunk will be loaded and 
     * {@link ChunkConsumer#accept} will be called with the loaded chunk.
     *
     * @param consumer the consumer choosing which chunks to load and accepting them
     * @throws IOException if an IOException occurred trying to read the region
     */
    void iterateAllChunks(ChunkConsumer<T> consumer) throws IOException;

    /**
     * Returns an instance representing an empty chunk.
     *
     * @return an empty chunk instance
     */
    T emptyChunk();

    /**
     * Returns an instance representing an errored chunk.
     *
     * @return a chunk instance to use when errors occur
     */
    default T erroredChunk() {
        return emptyChunk();
    }

}