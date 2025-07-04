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
import de.bluecolored.bluemap.core.world.mca.data.BlockEntityTypeResolver;
import de.tr7zw.nbtapi.NBTCompound;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Chunk_1_16 extends MCAChunk {

    private static final Key STATUS_EMPTY = new Key("minecraft", "empty");
    private static final Key STATUS_FULL = new Key("minecraft", "full");

    private final boolean generated;
    private final boolean hasLightData;
    private final long inhabitedTime;

    private final int skyLight;

    private final boolean hasWorldSurfaceHeights;
    private final PackedIntArrayAccess worldSurfaceHeights;
    private final boolean hasOceanFloorHeights;
    private final PackedIntArrayAccess oceanFloorHeights;

    private final Section[] sections;
    private final int sectionMin, sectionMax;

    private final int[] biomes;
    private final Map<Long, BlockEntity> blockEntities;

    public Chunk_1_16(MCAWorld world, Data data) {
        super(world, data);

        Level level = data.level;

        this.generated = !STATUS_EMPTY.equals(level.status);
        this.hasLightData = STATUS_FULL.equals(level.status);
        this.inhabitedTime = level.inhabitedTime;

        DimensionType dimensionType = getWorld().getDimensionType();
        this.skyLight = dimensionType.hasSkylight() ? 15 : 0;

        int worldHeight = dimensionType.getHeight();
        int bitsPerHeightmapElement = MCAUtil.ceilLog2(worldHeight + 1);

        this.worldSurfaceHeights = new PackedIntArrayAccess(bitsPerHeightmapElement, level.heightmaps.worldSurface);
        this.oceanFloorHeights = new PackedIntArrayAccess(bitsPerHeightmapElement, level.heightmaps.oceanFloor);

        this.hasWorldSurfaceHeights = this.worldSurfaceHeights.isCorrectSize(VALUES_PER_HEIGHTMAP);
        this.hasOceanFloorHeights = this.oceanFloorHeights.isCorrectSize(VALUES_PER_HEIGHTMAP);

        this.biomes = level.biomes;

        SectionData[] sectionsData = level.sections;
        if (sectionsData != null && sectionsData.length > 0) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;

            // find section min/max y
            for (SectionData sectionData : sectionsData) {
                int y = sectionData.getY();
                if (min > y) min = y;
                if (max < y) max = y;
            }

            // load sections into ordered array
            this.sections = new Section[1 + max - min];
            for (SectionData sectionData : sectionsData) {
                Section section = new Section(sectionData);
                int y = section.getSectionY();

                if (min > y) min = y;
                if (max < y) max = y;

                this.sections[section.sectionY - min] = section;
            }

            this.sectionMin = min;
            this.sectionMax = max;
        } else {
            this.sections = new Section[0];
            this.sectionMin = 0;
            this.sectionMax = 0;
        }

        // load block-entities
        this.blockEntities = new HashMap<>(level.blockEntities.length);
        for (int i = 0; i < level.blockEntities.length; i++) {
            BlockEntity be = level.blockEntities[i];
            if (be == null) continue;

            long hash = (long) be.getY() << 8 | (be.getX() & 0xF) << 4 | be.getZ() & 0xF;
            blockEntities.put(hash, be);
        }
    }

    @Override
    public boolean isGenerated() {
        return generated;
    }

    @Override
    public boolean hasLightData() {
        return hasLightData;
    }

    @Override
    public long getInhabitedTime() {
        return inhabitedTime;
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        Section section = getSection(y >> 4);
        if (section == null) return BlockState.AIR;

        return section.getBlockState(x, y, z);
    }

    @Override
    public Biome getBiome(int x, int y, int z) {
        if (this.biomes.length < 16) return Biome.DEFAULT;

        int biomeIntIndex = (y & 0b1100) << 2 | z & 0b1100 | (x & 0b1100) >> 2;

        // shift y up/down if not in range
        if (biomeIntIndex >= biomes.length) biomeIntIndex -= (((biomeIntIndex - biomes.length) >> 4) + 1) * 16;
        if (biomeIntIndex < 0) biomeIntIndex -= (biomeIntIndex >> 4) * 16;

        Biome biome = getWorld().getDataPack().getBiome(biomes[biomeIntIndex]);
        return biome != null ? biome : Biome.DEFAULT;
    }

    @Override
    public LightData getLightData(int x, int y, int z, LightData target) {
        if (!hasLightData) return target.set(skyLight, 0);

        int sectionY = y >> 4;
        Section section = getSection(sectionY);
        if (section == null) return (sectionY < sectionMin) ? target.set(0, 0) : target.set(skyLight, 0);

        return section.getLightData(x, y, z, target);
    }

    @Override
    public int getMinY(int x, int z) {
        return sectionMin * 16;
    }

    @Override
    public int getMaxY(int x, int z) {
        return sectionMax * 16 + 15;
    }

    @Override
    public boolean hasWorldSurfaceHeights() {
        return hasWorldSurfaceHeights;
    }

    @Override
    public int getWorldSurfaceY(int x, int z) {
        return worldSurfaceHeights.get((z & 0xF) << 4 | x & 0xF);
    }

    @Override
    public boolean hasOceanFloorHeights() {
        return hasOceanFloorHeights;
    }

    @Override
    public int getOceanFloorY(int x, int z) {
        return oceanFloorHeights.get((z & 0xF) << 4 | x & 0xF);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(int x, int y, int z) {
        long hash = (long) y << 8 | (x & 0xF) << 4 | z & 0xF;
        return blockEntities.get(hash);
    }

    @Override
    public void iterateBlockEntities(Consumer<BlockEntity> consumer) {
        blockEntities.values().forEach(consumer);
    }

    private @Nullable Section getSection(int y) {
        int index = y - sectionMin;
        if (index < 0 || index >= sections.length) return null;
        return sections[index];
    }

    protected static class Section {
        private final int sectionY;
        private final BlockState[] blockPalette;
        private final PackedIntArrayAccess blocks;
        private final byte[] blockLight;
        private final byte[] skyLight;

        public Section(SectionData sectionData) {
            this.sectionY = sectionData.y;
            this.blockPalette = sectionData.palette;
            this.blocks = new PackedIntArrayAccess(MCAUtil.ceilLog2(sectionData.palette.length), sectionData.blockStates);
            this.blockLight = sectionData.blockLight;
            this.skyLight = sectionData.skyLight;
        }

        public BlockState getBlockState(int x, int y, int z) {
            int index = (y & 0xF) << 8 | (z & 0xF) << 4 | x & 0xF;
            int paletteIndex = blocks.get(index);
            if (paletteIndex >= blockPalette.length) return BlockState.AIR;
            return blockPalette[paletteIndex];
        }

        public LightData getLightData(int x, int y, int z, LightData target) {
            int index = (y & 0xF) << 8 | (z & 0xF) << 4 | x & 0xF;
            int blockLightValue = blockLight != null ? MCAUtil.getByteHalf(blockLight[index >> 1], (index & 1) == 0) : 0;
            int skyLightValue = skyLight != null ? MCAUtil.getByteHalf(skyLight[index >> 1], (index & 1) == 0) : 15;
            return target.set(skyLightValue, blockLightValue);
        }

        public int getSectionY() {
            return sectionY;
        }
    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    public static class Data extends MCAChunk.Data {
        private Level level = new Level();

        @Override
        public void readFromNBT(NBTCompound compound) {
            if (compound.hasKey("Level")) {
                NBTCompound levelCompound = compound.getCompound("Level");
                level.readFromNBT(levelCompound);
            }
        }

        @Override
        public void writeToNBT(NBTCompound compound) {
            NBTCompound levelCompound = compound.getCompound("Level");
            level.writeToNBT(levelCompound);
        }
    }

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    public static class Level {
        private Key status = STATUS_EMPTY;
        private long inhabitedTime = 0;
        private HeightmapsData heightmaps = new HeightmapsData();
        private SectionData[] sections = null;
        private int[] biomes = EMPTY_INT_ARRAY;
        private BlockEntity[] blockEntities = EMPTY_BLOCK_ENTITIES_ARRAY;

        public void readFromNBT(NBTCompound compound) {
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
            if (compound.hasKey("Biomes")) {
                this.biomes = compound.getIntArray("Biomes");
            }
            if (compound.hasKey("TileEntities")) {
                NBTCompound tileEntitiesCompound = compound.getCompound("TileEntities");
                this.blockEntities = new BlockEntity[tileEntitiesCompound.getKeys().size()];
                int i = 0;
                for (String key : tileEntitiesCompound.getKeys()) {
                    NBTCompound entityCompound = tileEntitiesCompound.getCompound(key);
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

        public void writeToNBT(NBTCompound compound) {
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
            if (biomes != null) {
                compound.setIntArray("Biomes", biomes);
            }
            if (blockEntities != null) {
                NBTCompound tileEntitiesCompound = compound.getCompound("TileEntities");
                for (int i = 0; i < blockEntities.length; i++) {
                    if (blockEntities[i] != null) {
                        NBTCompound entityCompound = tileEntitiesCompound.getCompound(String.valueOf(i));
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
        private BlockState[] palette = EMPTY_BLOCKSTATE_ARRAY;
        private long[] blockStates = EMPTY_LONG_ARRAY;

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
            if (compound.hasKey("BlockStates")) {
                this.blockStates = compound.getLongArray("BlockStates");
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
            if (palette != null) {
                NBTCompound paletteCompound = compound.getCompound("Palette");
                for (int i = 0; i < palette.length; i++) {
                    NBTCompound stateCompound = paletteCompound.getCompound(String.valueOf(i));
                    stateCompound.setString("Name", palette[i].getId());
                }
            }
            if (blockStates != null) {
                compound.setLongArray("BlockStates", blockStates);
            }
        }
    }
}
