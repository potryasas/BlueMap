package de.bluecolored.bluemap.core.world.mca.chunk;

import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.Biome;
import de.bluecolored.bluemap.core.world.LightData;

import java.util.function.Consumer;

public interface Chunk {
    BlockState getBlockState(int x, int y, int z);
    boolean isGenerated();
    boolean hasLightData();
    long getInhabitedTime();
    Biome getBiome(int x, int y, int z);
    LightData getLightData(int x, int y, int z, LightData target);
    int getMinY(int x, int z);
    int getMaxY(int x, int z);
    boolean hasWorldSurfaceHeights();
    int getWorldSurfaceY(int x, int z);
    boolean hasOceanFloorHeights();
    int getOceanFloorY(int x, int z);
    BlockEntity getBlockEntity(int x, int y, int z);
    void iterateBlockEntities(Consumer<BlockEntity> consumer);
} 