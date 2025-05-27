package de.bluecolored.bluemap.core.world.mca;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.logger.Logger;
import de.bluecolored.bluemap.core.resources.pack.datapack.DataPack;
import de.bluecolored.bluemap.core.util.Grid;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.WatchService;
import de.bluecolored.bluemap.core.world.*;
import de.bluecolored.bluemap.core.world.mca.chunk.Chunk;
import de.bluecolored.bluemap.core.world.mca.chunk.MCAChunkLoader;
import de.bluecolored.bluemap.core.world.mca.data.LevelData;
import de.bluecolored.bluemap.core.world.mca.entity.chunk.MCAEntityChunk;
import de.bluecolored.bluemap.core.world.mca.entity.chunk.MCAEntityChunkLoader;
import de.bluecolored.bluemap.core.world.mca.entity.MCAEntity;
import de.tr7zw.nbtapi.NBTFile;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

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

    private final ChunkGrid blockChunkGrid;
    private final ChunkGrid entityChunkGrid;

    public MCAWorld(File worldFolder, Key dimension, DataPack dataPack) throws IOException {
        this.id = World.id(worldFolder.toPath(), dimension);
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

        LevelData levelDataTmp;
        try {
            NBTFile nbtFile = new NBTFile(levelFile);
            levelDataTmp = new LevelData(nbtFile);
        } catch (IOException e) {
            throw new IOException("Failed to read level.dat!", e);
        }
        this.levelData = levelDataTmp;

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
        this.dimensionFolder = resolveDimensionFolder(worldFolder.toPath(), dimension);

        ChunkLoader chunkLoader = new MCAChunkLoader(this);
        ChunkLoader entityLoader = new MCAEntityChunkLoader();
        this.blockChunkGrid = new ChunkGrid(chunkLoader, dimensionFolder.resolve("region"));
        this.entityChunkGrid = new ChunkGrid(entityLoader, dimensionFolder.resolve("entities"));
    }

    // Adapter class to wrap MCA Chunks as core Chunks
    private class ChunkAdapter implements de.bluecolored.bluemap.core.world.Chunk {
        private final Chunk mcaChunk;
        public ChunkAdapter(Chunk mcaChunk) { this.mcaChunk = mcaChunk; }
        @Override public BlockState getBlockState(int x, int y, int z) { return mcaChunk.getBlockState(x, y, z); }
        @Override public de.bluecolored.bluemap.core.world.biome.Biome getBiome(int x, int y, int z) { return (de.bluecolored.bluemap.core.world.biome.Biome) mcaChunk.getBiome(x, y, z); }
        @Override public boolean isGenerated() { return mcaChunk.isGenerated(); }
        @Override public boolean hasLightData() { return mcaChunk.hasLightData(); }
        @Override public long getInhabitedTime() { return mcaChunk.getInhabitedTime(); }
        @Override public LightData getLightData(int x, int y, int z, LightData target) { return mcaChunk.getLightData(x, y, z, target); }
        @Override public int getMinY(int x, int z) { return mcaChunk.getMinY(x, z); }
        @Override public int getMaxY(int x, int z) { return mcaChunk.getMaxY(x, z); }
        @Override public boolean hasWorldSurfaceHeights() { return mcaChunk.hasWorldSurfaceHeights(); }
        @Override public int getWorldSurfaceY(int x, int z) { return mcaChunk.getWorldSurfaceY(x, z); }
        @Override public boolean hasOceanFloorHeights() { return mcaChunk.hasOceanFloorHeights(); }
        @Override public int getOceanFloorY(int x, int z) { return mcaChunk.getOceanFloorY(x, z); }
        @Override public BlockEntity getBlockEntity(int x, int y, int z) { return mcaChunk.getBlockEntity(x, y, z); }
        @Override public void iterateBlockEntities(Consumer<BlockEntity> consumer) { mcaChunk.iterateBlockEntities(consumer); }
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
    public de.bluecolored.bluemap.core.world.Chunk getChunkAtBlock(int x, int z) {
        return getChunk(x >> 4, z >> 4);
    }

    @Override
    public de.bluecolored.bluemap.core.world.Chunk getChunk(int x, int z) {
        long key = (((long) x) << 32) | (((long) z) & 0xFFFFFFFFL);
        Chunk mcaChunk = chunks.computeIfAbsent(key, k -> loadChunk(x, z));
        if (mcaChunk == null) return null;
        return new ChunkAdapter(mcaChunk);
    }

    @Override
    public Region<de.bluecolored.bluemap.core.world.Chunk> getRegion(int x, int z) {
        final Region<Chunk> mcaRegion;
        try {
            mcaRegion = (Region<Chunk>) blockChunkGrid.getRegion(x, z);
        } catch (Exception e) {
            Logger.global.logError("Failed to get region at " + x + "," + z, e);
            return new Region<de.bluecolored.bluemap.core.world.Chunk>() {
                @Override
                public de.bluecolored.bluemap.core.world.Chunk loadChunk(int chunkX, int chunkZ) throws IOException { 
                    return null; 
                }
                @Override
                public void iterateAllChunks(ChunkConsumer<de.bluecolored.bluemap.core.world.Chunk> consumer) throws IOException {}
                @Override
                public de.bluecolored.bluemap.core.world.Chunk emptyChunk() { 
                    return de.bluecolored.bluemap.core.world.Chunk.EMPTY_CHUNK; 
                }
            };
        }

        return new Region<de.bluecolored.bluemap.core.world.Chunk>() {
            @Override
            public de.bluecolored.bluemap.core.world.Chunk loadChunk(int chunkX, int chunkZ) throws IOException {
                try {
                    Chunk mcaChunk = mcaRegion.loadChunk(chunkX, chunkZ);
                    if (mcaChunk == null) return null;
                    return new ChunkAdapter(mcaChunk);
                } catch (IOException e) {
                    Logger.global.logError("Failed to load chunk in region at " + chunkX + "," + chunkZ, e);
                    throw e;
                }
            }

            @Override
            public void iterateAllChunks(ChunkConsumer<de.bluecolored.bluemap.core.world.Chunk> consumer) throws IOException {
                try {
                    mcaRegion.iterateAllChunks(new ChunkConsumer<Chunk>() {
                        @Override
                        public boolean filter(int chunkX, int chunkZ, int lastModified) {
                            return consumer.filter(chunkX, chunkZ, lastModified);
                        }
                        @Override
                        public void accept(int chunkX, int chunkZ, Chunk chunk) {
                            consumer.accept(chunkX, chunkZ, new ChunkAdapter(chunk));
                        }
                        @Override
                        public void fail(int chunkX, int chunkZ, IOException exception) throws IOException {
                            try {
                                consumer.fail(chunkX, chunkZ, exception);
                            } catch (IOException e) {
                                Logger.global.logError("Failed to handle chunk fail in region at " + chunkX + "," + chunkZ, e);
                                throw e;
                            }
                        }
                    });
                } catch (IOException e) {
                    Logger.global.logError("Failed to iterate chunks in region", e);
                    throw e;
                }
            }

            @Override
            public de.bluecolored.bluemap.core.world.Chunk emptyChunk() {
                return de.bluecolored.bluemap.core.world.Chunk.EMPTY_CHUNK;
            }
        };
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
    }

    @Override
    public void invalidateChunkCache() {
        chunks.clear();
        entities.clear();
    }

    @Override
    public void invalidateChunkCache(int x, int z) {
        long key = (((long) x) << 32) | (((long) z) & 0xFFFFFFFFL);
        chunks.remove(key);
        entities.remove(key);
    }

    @Override
    public void iterateEntities(int minX, int minZ, int maxX, int maxZ, Consumer<Entity> entityConsumer) {
        int minChunkX = minX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkX = maxX >> 4;
        int maxChunkZ = maxZ >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                final int chunkX = cx;
                final int chunkZ = cz;
                long key = (((long) chunkX) << 32) | (((long) chunkZ) & 0xFFFFFFFFL);
                Chunk entityChunk = entities.computeIfAbsent(key, k -> {
                    try {
                        return (Chunk) entityChunkGrid.getChunk(chunkX, chunkZ);
                    } catch (Exception e) {
                        Logger.global.logError("Failed to load entity chunk at " + chunkX + "," + chunkZ, e);
                        return null;
                    }
                });

                if (entityChunk != null && entityChunk instanceof MCAEntityChunk) {
                    for (MCAEntity entity : ((MCAEntityChunk) entityChunk).getEntities()) {
                        entityConsumer.accept(entity);
                    }
                }
            }
        }
    }

    private Chunk loadChunk(int x, int z) {
        try {
            return (Chunk) blockChunkGrid.getChunk(x, z);
        } catch (Exception e) {
            Logger.global.logError("Failed to load chunk at " + x + "," + z, e);
            return null;
        }
    }

    private Chunk loadEntityChunk(int x, int z) {
        try {
            return (Chunk) entityChunkGrid.getChunk(x, z);
        } catch (Exception e) {
            Logger.global.logError("Failed to load entity chunk at " + x + "," + z, e);
            return null;
        }
    }

    @Override
    public void close() {
        chunks.clear();
        entities.clear();
    }

    public static Path resolveDimensionFolder(Path worldFolder, Key dimension) {
        if (DataPack.DIMENSION_OVERWORLD.equals(dimension)) {
            return worldFolder;
        } else if (DataPack.DIMENSION_THE_NETHER.equals(dimension)) {
            return worldFolder.resolve("DIM-1");
        } else if (DataPack.DIMENSION_THE_END.equals(dimension)) {
            return worldFolder.resolve("DIM1");
        } else {
            return worldFolder.resolve("dimensions").resolve(dimension.getNamespace()).resolve(dimension.getValue());
        }
    }

    public Chunk getMcaChunk(int x, int z) {
        long key = (((long) x) << 32) | (((long) z) & 0xFFFFFFFFL);
        return chunks.get(key);
    }

    public static int getXFromKey(long key) {
        return (int) (key >> 32);
    }

    public static int getZFromKey(long key) {
        return (int) (key & 0xFFFFFFFFL);
    }
}