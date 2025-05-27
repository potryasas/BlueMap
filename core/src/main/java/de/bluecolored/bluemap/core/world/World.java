package de.bluecolored.bluemap.core.world;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.util.Grid;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.WatchService;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.util.PathUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a World on the Server.<br>
 * This is usually one of the dimensions of a level.<br>
 * <br>
 * <i>The implementation of this class has to be thread-save!</i><br>
 */
public interface World extends AutoCloseable {

    /**
     * Returns the unique ID of this world.
     *
     * @return the world ID
     */
    String getId();

    /**
     * Returns the name of this world.
     *
     * @return the world name
     */
    String getName();

    /**
     * Returns the spawn point of this world.
     *
     * @return the spawn point as a Vector3i
     */
    Vector3i getSpawnPoint();

    /**
     * Returns the dimension type of this world.
     *
     * @return the dimension type
     */
    DimensionType getDimensionType();

    /**
     * Returns the chunk grid of this world.
     *
     * @return the chunk grid
     */
    Grid getChunkGrid();

    /**
     * Returns the region grid of this world.
     *
     * @return the region grid
     */
    Grid getRegionGrid();

    /**
     * Returns the {@link Chunk} on the specified block-position.
     *
     * @param x the block x coordinate
     * @param z the block z coordinate
     * @return the chunk at the specified block position
     */
    Chunk getChunkAtBlock(int x, int z);

    /**
     * Returns the {@link Chunk} on the specified chunk-position.
     *
     * @param x the chunk x coordinate
     * @param z the chunk z coordinate
     * @return the chunk at the specified chunk position
     */
    Chunk getChunk(int x, int z);

    /**
     * Returns the {@link Region} on the specified region-position.
     *
     * @param x the region x coordinate
     * @param z the region z coordinate
     * @return the region at the specified region position
     */
    Region<Chunk> getRegion(int x, int z);

    /**
     * Returns a collection of all regions in this world.
     * <i>(Be aware that the collection is not cached and recollected each time from the world-files!)</i>
     *
     * @return a collection of all region positions in this world
     */
    Collection<Vector2i> listRegions();

    /**
     * Creates a new {@link WatchService} that watches for region-changes in this world.
     *
     * @return a WatchService for region changes
     * @throws IOException if creating the watch-service fails
     * @throws UnsupportedOperationException if watching this world is not supported
     */
    default WatchService<Vector2i> createRegionWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Loads all chunks from the specified region into the chunk cache (if there is a cache).
     *
     * @param x the region x coordinate
     * @param z the region z coordinate
     */
    default void preloadRegionChunks(int x, int z) {
        preloadRegionChunks(x, z, pos -> true);
    }

    /**
     * Loads the filtered chunks from the specified region into the chunk cache (if there is a cache).
     *
     * @param x the region x coordinate
     * @param z the region z coordinate
     * @param chunkFilter a predicate to filter the chunks to preload
     */
    void preloadRegionChunks(int x, int z, Predicate<Vector2i> chunkFilter);

    /**
     * Invalidates the complete chunk cache (if there is a cache), so that every chunk has to be reloaded from disk.
     */
    void invalidateChunkCache();

    /**
     * Invalidates the chunk from the chunk-cache (if there is a cache), so that the chunk has to be reloaded from disk.
     *
     * @param x the chunk x coordinate
     * @param z the chunk z coordinate
     */
    void invalidateChunkCache(int x, int z);

    /**
     * Iterates over all entities in the specified area and applies the given consumer.
     *
     * @param minX the minimum x coordinate
     * @param minZ the minimum z coordinate
     * @param maxX the maximum x coordinate
     * @param maxZ the maximum z coordinate
     * @param entityConsumer the consumer to process each entity
     */
    void iterateEntities(int minX, int minZ, int maxX, int maxZ, Consumer<Entity> entityConsumer);

    /**
     * Generates a unique world-id based on a world-folder and a dimension.
     *
     * @param worldFolder the world folder path
     * @param dimension the dimension key
     * @return the generated world ID
     */
    static String id(Path worldFolder, Key dimension) {
        worldFolder = worldFolder.toAbsolutePath().normalize();

        Path workingDir = PathUtil.emptyPath().toAbsolutePath().normalize();
        if (worldFolder.startsWith(workingDir))
            worldFolder = workingDir.relativize(worldFolder);

        return worldFolder.toString().replace('\\', '/') + ":" + dimension.getFormatted();
    }

    /**
     * Returns the working directory path.
     *
     * @return the working directory path
     */
    static Path getWorkingDirectory() {
        return AbstractWorld.getWorkingDirectory();
    }

    @Override
    void close() throws IOException;

}