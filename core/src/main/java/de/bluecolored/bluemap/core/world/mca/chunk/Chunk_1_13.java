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
import de.tr7zw.nbtapi.NBTCompound;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@ToString(callSuper = true)
public class Chunk_1_13 extends MCAChunk {

    private static final Key STATUS_EMPTY = new Key("minecraft", "empty");
    private static final Key STATUS_FULL = new Key("minecraft", "full");
    private static final Key STATUS_FULLCHUNK = new Key("minecraft", "fullchunk");
    private static final Key STATUS_POSTPROCESSED = new Key("minecraft", "postprocessed");

    private final boolean generated;
    private final boolean hasLightData;
    private final long inhabitedTime;

    private final int skyLight;

    private final boolean hasWorldSurfaceHeights;
    private final long[] worldSurfaceHeights;
    private final boolean hasOceanFloorHeights;
    private final long[] oceanFloorHeights;

    private final Section[] sections;
    private final int sectionMin, sectionMax;

    final int[] biomes;
    private final Map<Long, BlockEntity> blockEntities;

    @Getter
    private Data data;

    public Chunk_1_13(MCAWorld world, Data data) {
        super(world, data);

        Level level = data.level;

        this.generated = !STATUS_EMPTY.equals(level.status);
        this.hasLightData =
                STATUS_FULL.equals(level.status) ||
                STATUS_FULLCHUNK.equals(level.status) ||
                STATUS_POSTPROCESSED.equals(level.status);
        this.inhabitedTime = level.inhabitedTime;

        DimensionType dimensionType = getWorld().getDimensionType();
        this.skyLight = dimensionType.hasSkylight() ? 15 : 0;

        this.worldSurfaceHeights = level.heightmaps.worldSurface;
        this.oceanFloorHeights = level.heightmaps.oceanFloor;

        this.hasWorldSurfaceHeights = this.worldSurfaceHeights.length >= 36;
        this.hasOceanFloorHeights = this.oceanFloorHeights.length >= 36;

        this.biomes = level.biomes;

        List<SectionData> sectionsData = level.sections;
        if (sectionsData != null && !sectionsData.isEmpty()) {
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
        this.blockEntities = new HashMap<>();
        if (level.tileEntities != null) {
            for (BlockEntity be : level.tileEntities) {
                if (be == null) continue;

                long hash = (long) be.getY() << 8 | (be.getX() & 0xF) << 4 | be.getZ() & 0xF;
                blockEntities.put(hash, be);
            }
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
        if (this.biomes.length < 256) return Biome.DEFAULT;

        int biomeIntIndex = (z & 0xF) << 4 | x & 0xF;

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
    public int getWorldSurfaceY(int x, int z) {
        if (!hasWorldSurfaceHeights) return getMaxY(x, z);

        int index = (z & 0xF) << 4 | x & 0xF;
        int bitsPerValue = MCAUtil.ceilLog2(getMaxY(x, z) - getMinY(x, z) + 1);
        return (int) MCAUtil.getValueFromLongStream(worldSurfaceHeights, index, bitsPerValue);
    }

    @Override
    public boolean hasOceanFloorHeights() {
        return hasOceanFloorHeights;
    }

    @Override
    public int getOceanFloorY(int x, int z) {
        if (!hasOceanFloorHeights) return getMinY(x, z);

        int index = (z & 0xF) << 4 | x & 0xF;
        int bitsPerValue = MCAUtil.ceilLog2(getMaxY(x, z) - getMinY(x, z) + 1);
        return (int) MCAUtil.getValueFromLongStream(oceanFloorHeights, index, bitsPerValue);
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
        private final long[] blocks;
        private final byte[] blockLight;
        private final byte[] skyLight;

        private final int bitsPerBlock;

        public Section(SectionData sectionData) {
            this.sectionY = sectionData.getY();
            this.blockPalette = sectionData.getPalette().toArray(new BlockState[0]);
            this.blocks = sectionData.getBlockStates();
            this.blockLight = sectionData.getBlockLight();
            this.skyLight = sectionData.getSkyLight();

            this.bitsPerBlock = MCAUtil.ceilLog2(blockPalette.length);
        }

        public BlockState getBlockState(int x, int y, int z) {
            int index = (y & 0xF) << 8 | (z & 0xF) << 4 | x & 0xF;
            int paletteIndex = (int) (MCAUtil.getValueFromLongStream(blocks, index, bitsPerBlock) & 0x7FFFFFFF);
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
    public static class Data extends MCAChunk.Data {
        private Level level = new Level();

        public void readFromNBT(NBTCompound compound) {
            if (compound.hasKey("Level")) {
                this.level = new Level();
                this.level.readFromNBT(compound.getCompound("Level"));
            }
        }

        public void writeToNBT(NBTCompound compound) {
            if (level != null) {
                NBTCompound levelCompound = compound.getCompound("Level");
                level.writeToNBT(levelCompound);
            }
        }
    }

    @Getter
    public static class Level {
        private Key status = STATUS_EMPTY;
        private long inhabitedTime = 0;
        private HeightmapsData heightmaps = new HeightmapsData();
        private List<SectionData> sections = new ArrayList<>();
        private int[] biomes = new int[0];
        private List<BlockEntity> tileEntities = new ArrayList<>();

        public void readFromNBT(NBTCompound compound) {
            if (compound.hasKey("Status")) {
                this.status = Key.parse(compound.getString("Status"));
            }
            if (compound.hasKey("InhabitedTime")) {
                this.inhabitedTime = compound.getLong("InhabitedTime");
            }
            if (compound.hasKey("Heightmaps")) {
                this.heightmaps = new HeightmapsData();
                this.heightmaps.readFromNBT(compound.getCompound("Heightmaps"));
            }
            if (compound.hasKey("Sections")) {
                NBTCompound sectionsCompound = compound.getCompound("Sections");
                this.sections = new ArrayList<>();
                for (String key : sectionsCompound.getKeys()) {
                    SectionData section = new SectionData();
                    section.readFromNBT(sectionsCompound.getCompound(key));
                    sections.add(section);
                }
            }
            if (compound.hasKey("Biomes")) {
                this.biomes = compound.getIntArray("Biomes");
            }
            if (compound.hasKey("TileEntities")) {
                NBTCompound tileEntitiesCompound = compound.getCompound("TileEntities");
                this.tileEntities = new ArrayList<>();
                for (String key : tileEntitiesCompound.getKeys()) {
                    NBTCompound tileEntityCompound = tileEntitiesCompound.getCompound(key);
                    BlockEntity tileEntity = new BlockEntity(
                        tileEntityCompound.getString("id"),
                        tileEntityCompound.getInteger("x"),
                        tileEntityCompound.getInteger("y"),
                        tileEntityCompound.getInteger("z")
                    );
                    tileEntities.add(tileEntity);
                }
            }
        }

        public void writeToNBT(NBTCompound compound) {
            compound.setString("Status", status.toString());
            compound.setLong("InhabitedTime", inhabitedTime);
            if (heightmaps != null) {
                NBTCompound heightmapsCompound = compound.getCompound("Heightmaps");
                heightmaps.writeToNBT(heightmapsCompound);
            }
            if (sections != null) {
                NBTCompound sectionsCompound = compound.getCompound("Sections");
                for (int i = 0; i < sections.size(); i++) {
                    NBTCompound sectionCompound = sectionsCompound.getCompound(String.valueOf(i));
                    sections.get(i).writeToNBT(sectionCompound);
                }
            }
            if (biomes != null) {
                compound.setIntArray("Biomes", biomes);
            }
            if (tileEntities != null) {
                NBTCompound tileEntitiesCompound = compound.getCompound("TileEntities");
                for (int i = 0; i < tileEntities.size(); i++) {
                    NBTCompound tileEntityCompound = tileEntitiesCompound.getCompound(String.valueOf(i));
                    BlockEntity tileEntity = tileEntities.get(i);
                    tileEntityCompound.setString("id", tileEntity.getId().toString());
                    tileEntityCompound.setInteger("x", tileEntity.getX());
                    tileEntityCompound.setInteger("y", tileEntity.getY());
                    tileEntityCompound.setInteger("z", tileEntity.getZ());
                }
            }
        }
    }

    @Getter
    public static class HeightmapsData {
        private long[] worldSurface = new long[0];
        private long[] oceanFloor = new long[0];

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
    public static class SectionData {
        private int y = 0;
        private byte[] blockLight = new byte[0];
        private byte[] skyLight = new byte[0];
        private List<BlockState> palette = new ArrayList<>();
        private long[] blockStates = new long[0];

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
                this.palette = new ArrayList<>();
                for (String key : paletteCompound.getKeys()) {
                    NBTCompound stateCompound = paletteCompound.getCompound(key);
                    BlockState state = new BlockState(stateCompound.getString("Name"));
                    palette.add(state);
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
                for (int i = 0; i < palette.size(); i++) {
                    NBTCompound stateCompound = paletteCompound.getCompound(String.valueOf(i));
                    BlockState state = palette.get(i);
                    stateCompound.setString("Name", state.getId());
                }
            }
            if (blockStates != null) {
                compound.setLongArray("BlockStates", blockStates);
            }
        }
    }
}
