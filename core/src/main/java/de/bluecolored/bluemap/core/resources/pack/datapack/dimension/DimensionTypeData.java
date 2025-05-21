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
package de.bluecolored.bluemap.core.resources.pack.datapack.dimension;

import de.tr7zw.nbtapi.NBTCompound;
import lombok.Data;

@Data
public class DimensionTypeData {

    private boolean natural;
    private boolean hasSkylight;
    private boolean hasCeiling;
    private float ambientLight;
    private int minY;
    private int height;
    private Long fixedTime;
    private double coordinateScale;

    public DimensionTypeData() {
        this.natural = false;
        this.hasSkylight = false;
        this.hasCeiling = false;
        this.ambientLight = 0.0f;
        this.minY = 0;
        this.height = 256;
        this.fixedTime = null;
        this.coordinateScale = 1.0;
    }

    public void readFromNBT(NBTCompound compound) {
        if (compound.hasKey("natural")) {
            this.natural = compound.getBoolean("natural");
        }
        if (compound.hasKey("has_skylight")) {
            this.hasSkylight = compound.getBoolean("has_skylight");
        }
        if (compound.hasKey("has_ceiling")) {
            this.hasCeiling = compound.getBoolean("has_ceiling");
        }
        if (compound.hasKey("ambient_light")) {
            this.ambientLight = compound.getFloat("ambient_light");
        }
        if (compound.hasKey("min_y")) {
            this.minY = compound.getInteger("min_y");
        }
        if (compound.hasKey("height")) {
            this.height = compound.getInteger("height");
        }
        if (compound.hasKey("fixed_time")) {
            this.fixedTime = compound.getLong("fixed_time");
        }
        if (compound.hasKey("coordinate_scale")) {
            this.coordinateScale = compound.getDouble("coordinate_scale");
        }
    }

    public void writeToNBT(NBTCompound compound) {
        compound.setBoolean("natural", natural);
        compound.setBoolean("has_skylight", hasSkylight);
        compound.setBoolean("has_ceiling", hasCeiling);
        compound.setFloat("ambient_light", ambientLight);
        compound.setInteger("min_y", minY);
        compound.setInteger("height", height);
        if (fixedTime != null) {
            compound.setLong("fixed_time", fixedTime);
        }
        compound.setDouble("coordinate_scale", coordinateScale);
    }
}
