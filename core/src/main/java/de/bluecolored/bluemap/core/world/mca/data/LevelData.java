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
package de.bluecolored.bluemap.core.world.mca.data;

import com.flowpowered.math.vector.Vector3i;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTFile;
import de.bluecolored.bluemap.core.world.DimensionType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class LevelData {
    private final Data data;

    public LevelData(NBTFile nbtFile) {
        NBTCompound data = nbtFile.getCompound("Data");
        this.data = new Data(data);
    }

    @Getter
    public static class Data {
        private final String levelName;
        private final int spawnX;
        private final int spawnY;
        private final int spawnZ;
        private final WorldGenSettings worldGenSettings;

        public Data(NBTCompound data) {
            this.levelName = data.getString("LevelName");
            this.spawnX = data.getInteger("SpawnX");
            this.spawnY = data.getInteger("SpawnY");
            this.spawnZ = data.getInteger("SpawnZ");
            this.worldGenSettings = new WorldGenSettings(data.getCompound("WorldGenSettings"));
        }
    }

    @Getter
    public static class WorldGenSettings {
        private final Map<String, Dimension> dimensions;

        public WorldGenSettings(NBTCompound settings) {
            this.dimensions = new HashMap<>();
            NBTCompound dims = settings.getCompound("dimensions");
            for (String key : dims.getKeys()) {
                dimensions.put(key, new Dimension(dims.getCompound(key)));
            }
        }
    }

    @Getter
    public static class Dimension {
        private final DimensionType type;

        public Dimension(NBTCompound dim) {
            this.type = DimensionType.valueOf(dim.getString("type").toUpperCase());
        }

        public Dimension(DimensionType type) {
            this.type = type;
        }
    }
}
