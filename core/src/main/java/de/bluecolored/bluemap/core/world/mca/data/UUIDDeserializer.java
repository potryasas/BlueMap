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

import de.bluecolored.bluemap.core.util.nbt.NBTAdapter;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTList;

import java.util.UUID;

public class UUIDDeserializer implements NBTAdapter<UUID> {

    @Override
    public UUID read(NBTCompound compound) {
        if (compound.hasKey("")) {
            return UUID.fromString(compound.getString(""));
        }

        if (compound.hasKey("UUID")) {
            int[] ints = compound.getIntArray("UUID");
            if (ints.length != 4) throw new IllegalArgumentException("Unexpected number of UUID-ints, expected 4, got " + ints.length);
            // Fix for Java 8 compatibility - properly handle int to long conversion with masking
            long mostSigBits = ((long) ints[0] << 32) | (ints[1] & 0xFFFFFFFFL);
            long leastSigBits = ((long) ints[2] << 32) | (ints[3] & 0xFFFFFFFFL);
            return new UUID(mostSigBits, leastSigBits);
        }

        long[] longs = compound.getLongArray("UUID");
        if (longs.length != 2) throw new IllegalArgumentException("Unexpected number of UUID-longs, expected 2, got " + longs.length);
        return new UUID(longs[0], longs[1]);
    }

    @Override
    public void write(UUID value, NBTCompound compound) {
        compound.setString("", value.toString());
    }
}
