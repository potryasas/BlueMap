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
package de.bluecolored.bluemap.core.world.mca.entity.chunk;

import de.bluecolored.bluemap.core.world.mca.MCAWorld;
import de.bluecolored.bluemap.core.world.mca.entity.MCAEntity;
import de.tr7zw.nbtapi.NBTCompound;
import lombok.Getter;
import de.bluecolored.bluemap.core.world.Chunk;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.BlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class MCAEntityChunk implements Chunk {

    public static final MCAEntityChunk EMPTY_CHUNK = new MCAEntityChunk(null, new Data());
    public static final MCAEntityChunk ERRORED_CHUNK = new MCAEntityChunk(null, new Data());

    private final MCAWorld world;
    private final Data data;

    public MCAEntityChunk(MCAWorld world, Data data) {
        this.world = world;
        this.data = data;
    }

    public MCAEntity[] getEntities() {
        if (data == null || data.getEntities() == null) {
            return new MCAEntity[0];
        }
        return data.getEntities().toArray(new MCAEntity[data.getEntities().size()]);
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return BlockState.AIR;
    }

    @Override
    public boolean isGenerated() {
        return true;
    }

    @Override
    public boolean hasLightData() {
        return false;
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
        return target.set(0, 0);
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
        return false;
    }

    @Override
    public int getWorldSurfaceY(int x, int z) {
        return 0;
    }

    @Override
    public boolean hasOceanFloorHeights() {
        return false;
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
        // No block entities in entity chunks
    }

    @Getter
    public static class Data {
        private List<MCAEntity> entities = new ArrayList<>();

        public void readFromNBT(NBTCompound compound) {
            if (compound.hasKey("Entities")) {
                NBTCompound entitiesCompound = compound.getCompound("Entities");
                this.entities = new ArrayList<>();
                for (String key : entitiesCompound.getKeys()) {
                    NBTCompound entityCompound = entitiesCompound.getCompound(key);
                    MCAEntity entity = new MCAEntity();
                    entity.readFromNBT(entityCompound);
                    entities.add(entity);
                }
            }
        }

        public void writeToNBT(NBTCompound compound) {
            if (entities != null) {
                NBTCompound entitiesCompound = compound.getCompound("Entities");
                for (int i = 0; i < entities.size(); i++) {
                    NBTCompound entityCompound = entitiesCompound.getCompound(String.valueOf(i));
                    entities.get(i).writeToNBT(entityCompound);
                }
            }
        }
    }

}
