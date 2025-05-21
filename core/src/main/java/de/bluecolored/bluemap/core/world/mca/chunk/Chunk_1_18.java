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

import de.bluecolored.bluemap.core.logger.Logger;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.MCAWorld;
import de.bluecolored.bluemap.core.world.mca.PackedIntArrayAccess;
import de.bluecolored.bluemap.core.world.mca.data.LenientBlockEntityArrayDeserializer;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTFile;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class Chunk_1_18 implements Chunk {
    private final MCAWorld world;
    private final int x;
    private final int z;
    private final NBTCompound chunkData;
    private final NBTCompound sections;

    public Chunk_1_18(MCAWorld world, int x, int z, NBTFile nbtFile) {
        this.world = world;
        this.x = x;
        this.z = z;
        this.chunkData = nbtFile.getCompound("Level");
        this.sections = chunkData.getCompound("Sections");
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        int sectionY = y >> 4;
        NBTCompound section = sections.getCompound(String.valueOf(sectionY));
        if (section == null) return BlockState.AIR;

        int blockX = x & 0xF;
        int blockY = y & 0xF;
        int blockZ = z & 0xF;
        int index = blockY * 256 + blockZ * 16 + blockX;

        NBTCompound blockStates = section.getCompound("BlockStates");
        if (blockStates == null) return BlockState.AIR;

        long[] states = blockStates.getLongArray("data");
        if (states == null || states.length == 0) return BlockState.AIR;

        int bitsPerBlock = blockStates.getInteger("bits");
        int blocksPerLong = 64 / bitsPerBlock;
        int longIndex = index / blocksPerLong;
        int bitIndex = (index % blocksPerLong) * bitsPerBlock;

        if (longIndex >= states.length) return BlockState.AIR;

        long state = (states[longIndex] >> bitIndex) & ((1L << bitsPerBlock) - 1);
        return BlockState.get(state);
    }

    @Override
    public boolean isGenerated() {
        return true;
    }

    @Override
    public boolean hasLightData() {
        return true;
    }

    @Override
    public long getInhabitedTime() {
        return 0;
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        return Biome.DEFAULT;
    }

    @Override
    public LightData getLightData(int x, int y, int z, LightData target) {
        return target.set(15, 0);
    }

    @Override
    public int getMinY(int x, int z) {
        return 0;
    }

    @Override
    public int getMaxY(int x, int z) {
        return 255;
    }

    @Override
    public boolean hasWorldSurfaceHeights() {
        return true;
    }

    @Override
    public int getWorldSurfaceY(int x, int z) {
        return 0;
    }

    @Override
    public boolean hasOceanFloorHeights() {
        return true;
    }

    @Override
    public int getOceanFloorY(int x, int z) {
        return 0;
    }

    @Override
    public BlockEntity getBlockEntity(int x, int y, int z) {
        return null;
    }

    @Override
    public void iterateBlockEntities(Consumer<BlockEntity> consumer) {
        // Implementation needed
    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    public static class Data extends MCAChunk.Data {

        @NBTName("Status")
        private Key status = new Key("minecraft", "empty");

        @NBTName("InhabitedTime")
        private long inhabitedTime = 0;

        @NBTName("Heightmaps")
        private HeightmapsData heightmaps = new HeightmapsData();

        private SectionData @Nullable [] sections = null;

        @NBTDeserializer(LenientBlockEntityArrayDeserializer.class)
        private @Nullable BlockEntity [] blockEntities = EMPTY_BLOCK_ENTITIES_ARRAY;

    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    public static class HeightmapsData {

        @NBTName("WORLD_SURFACE")
        private long[] worldSurface = EMPTY_LONG_ARRAY;

        @NBTName("OCEAN_FLOOR")
        private long[] oceanFloor = EMPTY_LONG_ARRAY;

    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    public static class SectionData {

        @NBTName("Y")
        private int y = 0;

        @NBTName("BlockLight")
        private byte[] blockLight = EMPTY_BYTE_ARRAY;

        @NBTName("SkyLight")
        private byte[] skyLight = EMPTY_BYTE_ARRAY;

        private BlockStatesData blockStates = new BlockStatesData();

        private BiomesData biomes = new BiomesData();

    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    public static class BlockStatesData {

        private BlockState[] palette = EMPTY_BLOCKSTATE_ARRAY;

        private long[] data = EMPTY_LONG_ARRAY;

    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    public static class BiomesData {

        private Key[] palette = EMPTY_KEY_ARRAY;

        private long[] data = EMPTY_LONG_ARRAY;

    }

}
