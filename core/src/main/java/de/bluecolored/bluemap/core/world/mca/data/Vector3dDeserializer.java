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

import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.core.util.nbt.NBTAdapter;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTList;

public class Vector3dDeserializer implements NBTAdapter<Vector3d> {

    @Override
    public Vector3d read(NBTCompound compound) {
        if (compound.hasKey("")) {
            long[] values = compound.getLongArray("");
            if (values.length != 3) throw new IllegalArgumentException("Unexpected array length: " + values.length);
            return new Vector3d(values[0], values[1], values[2]);
        }

        if (compound.hasKey("x") || compound.hasKey("y") || compound.hasKey("z")) {
            double x = compound.getDouble("x");
            double y = compound.getDouble("y");
            double z = compound.getDouble("z");
            return new Vector3d(x, y, z);
        }

        NBTList list = compound.getList("", NBTList.class);
        if (list != null) {
            return new Vector3d(
                list.getDouble(0),
                list.getDouble(1),
                list.getDouble(2)
            );
        }

        throw new IllegalArgumentException("Could not read Vector3d from NBT");
    }

    @Override
    public void write(Vector3d value, NBTCompound compound) {
        compound.setDouble("x", value.getX());
        compound.setDouble("y", value.getY());
        compound.setDouble("z", value.getZ());
    }
}
