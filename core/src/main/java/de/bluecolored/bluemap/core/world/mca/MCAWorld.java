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
package de.bluecolored.bluemap.core.world.mca;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.logger.Logger;
import de.bluecolored.bluemap.core.resources.pack.datapack.DataPack;
import de.bluecolored.bluemap.core.storage.compression.Compression;
import de.bluecolored.bluemap.core.util.Grid;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.WatchService;
import de.bluecolored.bluemap.core.world.*;
import de.bluecolored.bluemap.core.world.mca.chunk.Chunk;
import de.bluecolored.bluemap.core.world.mca.chunk.Chunk_1_18;
import de.bluecolored.bluemap.core.world.mca.data.DimensionTypeDeserializer;
import de.bluecolored.bluemap.core.world.mca.data.LevelData;
import de.bluecolored.bluemap.core.world.mca.entity.chunk.MCAEntityChunk;
import de.bluecolored.bluemap.core.world.mca.entity.chunk.MCAEntityChunkLoader;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTFile;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Getter
@ToString
public class MCAWorld implements World {

    private final String id;
    private final File worldFolder;
    private final Key dimension;
    private final DataPack dataPack;
    private final LevelData levelData;
    private final Map<Long, Chunk> chunks;
    private final Map<Long, Chunk> entities;

    private final DimensionType dimensionType;
    private final Vector3i spawnPoint;
    private final Path dimensionFolder;

    private final ChunkGrid<Chunk> blockChunkGrid;
    private final ChunkGrid<MCAEntityChunk> entityChunkGrid;

    public MCAWorld(File worldFolder, Key dimension, DataPack dataPack) throws IOException {
        this.id = World.id(worldFolder, dimension);
        this.worldFolder = worldFolder;
        this.dimension = dimension;
        this.dataPack = dataPack;
        this.chunks = new ConcurrentHashMap<>();
        this.entities = new ConcurrentHashMap<>();

        // Load level.dat
        File levelFile = new File(worldFolder, "level.dat");
        if (!levelFile.exists()) {
            throw new IOException("World folder does not contain a level.dat file!");
        }

        try {
            NBTFile nbtFile = new NBTFile(levelFile);
            this.levelData = new LevelData(nbtFile);
        } catch (IOException e) {
            throw new IOException("Failed to read level.dat!", e);
        }

        LevelData.Dimension dimensionData = levelData.getData().getWorldGenSettings().getDimensions().get(dimension.getFormatted());
        if (dimensionData == null) {
            if (DataPack.DIMENSION_OVERWORLD.equals(dimension)) dimensionData = new LevelData.Dimension(DimensionType.OVERWORLD);
            else if (DataPack.DIMENSION_THE_NETHER.equals(dimension)) dimensionData = new LevelData.Dimension(DimensionType.NETHER);
            else if (DataPack.DIMENSION_THE_END.equals(dimension)) dimensionData = new LevelData.Dimension(DimensionType.END);
            else {
                Logger.global.logWarning("The level-data does not contain any dimension with the id '" + dimension +
                        "', using fallback.");
                dimensionData = new LevelData.Dimension();
            }
        }

        this.dimensionType = dimensionData.getType();
        this.spawnPoint = new Vector3i(
                levelData.getData().getSpawnX(),
                levelData.getData().getSpawnY(),
                levelData.getData().getSpawnZ()
        );
        this.dimensionFolder = resolveDimensionFolder(worldFolder, dimension);

        this.blockChunkGrid = new ChunkGrid<>(new MCAChunkLoader(this), dimensionFolder.resolve("region"));
        this.entityChunkGrid = new ChunkGrid<>(new MCAEntityChunkLoader(), dimensionFolder.resolve("entities"));
    }

    @Override
    public String getName() {
        return levelData.getData().getLevelName();
    }

    @Override
    public Grid getChunkGrid() {
        return blockChunkGrid.getChunkGrid();
    }

    @Override
    public Grid getRegionGrid() {
        return blockChunkGrid.getRegionGrid();
    }

    @Override
    public Chunk getChunkAtBlock(int x, int z) {
        return getChunk(x >> 4, z >> 4);
    }

    @Override
    public Chunk getChunk(int x, int z) {
        long key = ((long) x << 32) | (z & 0xFFFFFFFFL);
        return chunks.computeIfAbsent(key, k -> {
            try {
                return loadChunk(x, z);
            } catch (IOException e) {
                Logger.global.logError("Failed to load chunk at " + x + "," + z, e);
                return null;
            }
        });
    }

    @Override
    public Region<Chunk> getRegion(int x, int z) {
        return blockChunkGrid.getRegion(x, z);
    }

    @Override
    public Collection<Vector2i> listRegions() {
        return blockChunkGrid.listRegions();
    }

    @Override
    public WatchService<Vector2i> createRegionWatchService() throws IOException {
        return blockChunkGrid.createRegionWatchService();
    }

    @Override
    public void preloadRegionChunks(int x, int z, Predicate<Vector2i> chunkFilter) {
        blockChunkGrid.preloadRegionChunks(x, z, chunkFilter);
        entityChunkGrid.preloadRegionChunks(x, z, chunkFilter);
    }

    @Override
    public void invalidateChunkCache() {
        blockChunkGrid.invalidateChunkCache();
        entityChunkGrid.invalidateChunkCache();
    }

    @Override
    public void invalidateChunkCache(int x, int z) {
        blockChunkGrid.invalidateChunkCache(x, z);
        entityChunkGrid.invalidateChunkCache(x, z);
    }

    @Override
    public void iterateEntities(int minX, int minZ, int maxX, int maxZ, Consumer<Entity> entityConsumer) {
        int minChunkX = minX >> 4, minChunkZ = minZ >> 4;
        int maxChunkX = maxX >> 4, maxChunkZ = maxZ >> 4;

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                Entity[] entities = entityChunkGrid.getChunk(x, z).getEntities();
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < entities.length; i++) {
                    Entity entity = entities[i];
                    Vector3d pos = entity.getPos();
                    int pX = pos.getFloorX();
                    int pZ = pos.getFloorZ();

                    if (
                            pX >= minX && pX <= maxX &&
                            pZ >= minZ && pZ <= maxZ
                    ) {
                        entityConsumer.accept(entities[i]);
                    }
                }
            }
        }
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        Chunk chunk = getChunkAtBlock(x, z);
        if (chunk == null) return BlockState.AIR;
        return chunk.getBlockState(x, y, z);
    }

    private Chunk loadChunk(int x, int z) throws IOException {
        File regionFile = MCAUtil.getRegionFile(worldFolder, x, z);
        if (!regionFile.exists()) return null;

        NBTFile nbtFile = new NBTFile(regionFile);
        return new Chunk_1_18(this, x, z, nbtFile);
    }

    public void load() throws IOException {
        // Load all chunks in the world
        File regionFolder = MCAUtil.getRegionFolder(worldFolder, dimension);
        if (!regionFolder.exists()) return;

        File[] regionFiles = regionFolder.listFiles((dir, name) -> name.endsWith(".mca"));
        if (regionFiles == null) return;

        for (File regionFile : regionFiles) {
            try {
                NBTFile nbtFile = new NBTFile(regionFile);
                String[] coords = regionFile.getName().split("\\.");
                int regionX = Integer.parseInt(coords[1]);
                int regionZ = Integer.parseInt(coords[2]);

                for (int x = 0; x < 32; x++) {
                    for (int z = 0; z < 32; z++) {
                        int chunkX = (regionX << 5) + x;
                        int chunkZ = (regionZ << 5) + z;
                        long key = ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
                        chunks.put(key, new Chunk_1_18(this, chunkX, chunkZ, nbtFile));
                    }
                }
            } catch (IOException e) {
                Logger.global.logError("Failed to load region file: " + regionFile.getName(), e);
            }
        }
    }

    public static Path resolveDimensionFolder(Path worldFolder, Key dimension) {
        if (DataPack.DIMENSION_OVERWORLD.equals(dimension)) return worldFolder;
        if (DataPack.DIMENSION_THE_NETHER.equals(dimension)) return worldFolder.resolve("DIM-1");
        if (DataPack.DIMENSION_THE_END.equals(dimension)) return worldFolder.resolve("DIM1");
        return worldFolder.resolve("dimensions").resolve(dimension.getNamespace()).resolve(dimension.getValue());
    }

}
