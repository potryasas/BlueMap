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

import java.util.ArrayList;
import java.util.List;

@Getter
public class MCAEntityChunk {

    private final MCAWorld world;
    private final Data data;

    public MCAEntityChunk(MCAWorld world, Data data) {
        this.world = world;
        this.data = data;
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
