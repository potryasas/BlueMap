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

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.core.util.nbt.NBTAdapter;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTList;

public class Vector2iDeserializer implements NBTAdapter<Vector2i> {

    @Override
    public Vector2i read(NBTCompound compound) {
        if (compound.hasKey("")) {
            long[] values = compound.getLongArray("");
            if (values.length != 2) throw new IllegalArgumentException("Unexpected array length: " + values.length);
            return new Vector2i(values[0], values[1]);
        }

        if (compound.hasKey("x") || compound.hasKey("y") || compound.hasKey("z")) {
            int x = compound.getInteger("x");
            int y = compound.getInteger("y");
            return new Vector2i(x, y);
        }

        NBTList list = compound.getList("", NBTList.class);
        if (list != null) {
            return new Vector2i(
                list.getInteger(0),
                list.getInteger(1)
            );
        }

        throw new IllegalArgumentException("Could not read Vector2i from NBT");
    }

    @Override
    public void write(Vector2i value, NBTCompound compound) {
        compound.setInteger("x", value.getX());
        compound.setInteger("y", value.getY());
    }
}
