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
import de.bluecolored.bluemap.core.world.Biome;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.MCAWorld;
import de.bluecolored.bluemap.core.world.mca.PackedIntArrayAccess;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTFile;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static de.bluecolored.bluemap.core.world.mca.chunk.MCAChunk.*;

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
        // For Java 8 compatibility, just return null as a placeholder
        return null;
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
        private Key status = new Key("minecraft", "empty");
        private long inhabitedTime = 0;
        private HeightmapsData heightmaps = new HeightmapsData();
        private SectionData[] sections = null;
        private BlockEntity[] blockEntities = EMPTY_BLOCK_ENTITIES_ARRAY;

        @Override
        public void readFromNBT(NBTCompound compound) {
            super.readFromNBT(compound);
            if (compound.hasKey("Status")) {
                this.status = Key.parse(compound.getString("Status"));
            }
            if (compound.hasKey("InhabitedTime")) {
                this.inhabitedTime = compound.getLong("InhabitedTime");
            }
            if (compound.hasKey("Heightmaps")) {
                NBTCompound heightmapsCompound = compound.getCompound("Heightmaps");
                this.heightmaps.readFromNBT(heightmapsCompound);
            }
            if (compound.hasKey("Sections")) {
                NBTCompound sectionsCompound = compound.getCompound("Sections");
                this.sections = new SectionData[sectionsCompound.getKeys().size()];
                int i = 0;
                for (String key : sectionsCompound.getKeys()) {
                    NBTCompound sectionCompound = sectionsCompound.getCompound(key);
                    SectionData section = new SectionData();
                    section.readFromNBT(sectionCompound);
                    sections[i++] = section;
                }
            }
            if (compound.hasKey("BlockEntities")) {
                NBTCompound blockEntitiesCompound = compound.getCompound("BlockEntities");
                this.blockEntities = new BlockEntity[blockEntitiesCompound.getKeys().size()];
                int i = 0;
                for (String key : blockEntitiesCompound.getKeys()) {
                    NBTCompound entityCompound = blockEntitiesCompound.getCompound(key);
                    BlockEntity entity = new BlockEntity(
                        entityCompound.getString("id"),
                        entityCompound.getInteger("x"),
                        entityCompound.getInteger("y"),
                        entityCompound.getInteger("z")
                    );
                    if (entity != null) {
                        blockEntities[i++] = entity;
                    }
                }
            }
        }

        @Override
        public void writeToNBT(NBTCompound compound) {
            super.writeToNBT(compound);
            compound.setString("Status", status.toString());
            compound.setLong("InhabitedTime", inhabitedTime);
            NBTCompound heightmapsCompound = compound.getCompound("Heightmaps");
            heightmaps.writeToNBT(heightmapsCompound);
            if (sections != null) {
                NBTCompound sectionsCompound = compound.getCompound("Sections");
                for (int i = 0; i < sections.length; i++) {
                    if (sections[i] != null) {
                        NBTCompound sectionCompound = sectionsCompound.getCompound(String.valueOf(i));
                        sections[i].writeToNBT(sectionCompound);
                    }
                }
            }
            if (blockEntities != null) {
                NBTCompound blockEntitiesCompound = compound.getCompound("BlockEntities");
                for (int i = 0; i < blockEntities.length; i++) {
                    if (blockEntities[i] != null) {
                        NBTCompound entityCompound = blockEntitiesCompound.getCompound(String.valueOf(i));
                        entityCompound.setString("id", blockEntities[i].getId().toString());
                        entityCompound.setInteger("x", blockEntities[i].getX());
                        entityCompound.setInteger("y", blockEntities[i].getY());
                        entityCompound.setInteger("z", blockEntities[i].getZ());
                    }
                }
            }
        }
    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    public static class HeightmapsData {
        private long[] worldSurface = EMPTY_LONG_ARRAY;
        private long[] oceanFloor = EMPTY_LONG_ARRAY;

        public void readFromNBT(NBTCompound compound) {
            if (compound.hasKey("WORLD_SURFACE")) {
                this.worldSurface = compound.getLongArray("WORLD_SURFACE");
            }
            if (compound.hasKey("OCEAN_FLOOR")) {
                this.oceanFloor = compound.getLongArray("OCEAN_FLOOR");
            }
        }

        public void writeToNBT(NBTCompound compound) {
            if (worldSurface != null) {
                compound.setLongArray("WORLD_SURFACE", worldSurface);
            }
            if (oceanFloor != null) {
                compound.setLongArray("OCEAN_FLOOR", oceanFloor);
            }
        }
    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    public static class SectionData {
        private int y = 0;
        private byte[] blockLight = EMPTY_BYTE_ARRAY;
        private byte[] skyLight = EMPTY_BYTE_ARRAY;
        private BiomesData biomes = new BiomesData();
        private BlockStatesData blockStates = new BlockStatesData();

        public void readFromNBT(NBTCompound compound) {
            if (compound.hasKey("Y")) {
                this.y = compound.getInteger("Y");
            }
            if (compound.hasKey("BlockLight")) {
                this.blockLight = compound.getByteArray("BlockLight");
            }
            if (compound.hasKey("SkyLight")) {
                this.skyLight = compound.getByteArray("SkyLight");
            }
            if (compound.hasKey("Biomes")) {
                NBTCompound biomesCompound = compound.getCompound("Biomes");
                this.biomes.readFromNBT(biomesCompound);
            }
            if (compound.hasKey("BlockStates")) {
                NBTCompound blockStatesCompound = compound.getCompound("BlockStates");
                this.blockStates.readFromNBT(blockStatesCompound);
            }
        }

        public void writeToNBT(NBTCompound compound) {
            compound.setInteger("Y", y);
            if (blockLight != null) {
                compound.setByteArray("BlockLight", blockLight);
            }
            if (skyLight != null) {
                compound.setByteArray("SkyLight", skyLight);
            }
            if (biomes != null) {
                NBTCompound biomesCompound = compound.getCompound("Biomes");
                biomes.writeToNBT(biomesCompound);
            }
            if (blockStates != null) {
                NBTCompound blockStatesCompound = compound.getCompound("BlockStates");
                blockStates.writeToNBT(blockStatesCompound);
            }
        }
    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    public static class BlockStatesData {
        private BlockState[] palette = EMPTY_BLOCKSTATE_ARRAY;
        private long[] data = EMPTY_LONG_ARRAY;

        public void readFromNBT(NBTCompound compound) {
            if (compound.hasKey("Palette")) {
                NBTCompound paletteCompound = compound.getCompound("Palette");
                this.palette = new BlockState[paletteCompound.getKeys().size()];
                int i = 0;
                for (String key : paletteCompound.getKeys()) {
                    NBTCompound stateCompound = paletteCompound.getCompound(key);
                    String name = stateCompound.getString("Name");
                    palette[i++] = new BlockState(name);
                }
            }
            if (compound.hasKey("Data")) {
                this.data = compound.getLongArray("Data");
            }
        }

        public void writeToNBT(NBTCompound compound) {
            if (palette != null) {
                NBTCompound paletteCompound = compound.getCompound("Palette");
                for (int i = 0; i < palette.length; i++) {
                    NBTCompound stateCompound = paletteCompound.getCompound(String.valueOf(i));
                    stateCompound.setString("Name", palette[i].getId());
                }
            }
            if (data != null) {
                compound.setLongArray("Data", data);
            }
        }
    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    public static class BiomesData {
        private Key[] palette = EMPTY_KEY_ARRAY;
        private long[] data = EMPTY_LONG_ARRAY;

        public void readFromNBT(NBTCompound compound) {
            if (compound.hasKey("Palette")) {
                NBTCompound paletteCompound = compound.getCompound("Palette");
                this.palette = new Key[paletteCompound.getKeys().size()];
                int i = 0;
                for (String key : paletteCompound.getKeys()) {
                    NBTCompound keyCompound = paletteCompound.getCompound(key);
                    String name = keyCompound.getString("Name");
                    palette[i++] = Key.parse(name);
                }
            }
            if (compound.hasKey("Data")) {
                this.data = compound.getLongArray("Data");
            }
        }

        public void writeToNBT(NBTCompound compound) {
            if (palette != null) {
                NBTCompound paletteCompound = compound.getCompound("Palette");
                for (int i = 0; i < palette.length; i++) {
                    NBTCompound keyCompound = paletteCompound.getCompound(String.valueOf(i));
                    keyCompound.setString("Name", palette[i].toString());
                }
            }
            if (data != null) {
                compound.setLongArray("Data", data);
            }
        }
    }
}
