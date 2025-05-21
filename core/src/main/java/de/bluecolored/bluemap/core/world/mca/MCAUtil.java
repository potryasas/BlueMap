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
package de.bluecolored.bluemap.core.world.mca;

import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.nbt.NBTAdapter;
import de.bluecolored.bluemap.core.util.nbt.NBTFileWrapper;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.Entity;
import de.bluecolored.bluemap.core.world.mca.blockentity.SignBlockEntity;
import de.bluecolored.bluemap.core.world.mca.data.*;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTFile;
import de.tr7zw.nbtapi.NBTList;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class MCAUtil {

    public static final NBTFileWrapper NBT;

    static {
        try {
            NBT = addCommonNbtSettings(new NBTFileWrapper(new File("temp.nbt")));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize NBT wrapper", e);
        }
    }

    private static final int SECTOR_SIZE = 4096;
    private static final int SECTORS_PER_REGION = 32;
    private static final int CHUNKS_PER_REGION = SECTORS_PER_REGION * SECTORS_PER_REGION;

    @Contract(value = "_ -> param1", mutates = "param1")
    public static NBTFileWrapper addCommonNbtSettings(NBTFileWrapper nbt) {
        nbt.register(BlockState.class, new BlockStateDeserializer());
        nbt.register(Key.class, new KeyDeserializer());
        nbt.register(UUID.class, new UUIDDeserializer());
        nbt.register(Vector3d.class, new Vector3dDeserializer());
        nbt.register(Vector2i.class, new Vector2iDeserializer());
        nbt.register(Vector2f.class, new Vector2fDeserializer());

        return nbt;
    }

    public static File getRegionFolder(File worldFolder, DimensionType dimension) {
        String dimensionPath;
        switch (dimension) {
            case OVERWORLD:
                dimensionPath = "region";
                break;
            case NETHER:
                dimensionPath = "DIM-1/region";
                break;
            case END:
                dimensionPath = "DIM1/region";
                break;
            default:
                throw new IllegalArgumentException("Unknown dimension: " + dimension);
        }
        return new File(worldFolder, dimensionPath);
    }

    public static File getRegionFile(File worldFolder, int chunkX, int chunkZ) {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        String fileName = String.format("r.%d.%d.mca", regionX, regionZ);
        return new File(getRegionFolder(worldFolder, DimensionType.OVERWORLD), fileName);
    }

    public static NBTFileWrapper readNBTFile(File file) throws IOException {
        return new NBTFileWrapper(file);
    }

    public static NBTCompound getCompound(NBTCompound compound, String key) {
        return compound.getCompound(key);
    }

    public static NBTList<?> getList(NBTCompound compound, String key) {
        return compound.getCompoundList(key);
    }

    public static String getString(NBTCompound compound, String key, String defaultValue) {
        return compound.hasKey(key) ? compound.getString(key) : defaultValue;
    }

    public static int getInt(NBTCompound compound, String key, int defaultValue) {
        return compound.hasKey(key) ? compound.getInteger(key) : defaultValue;
    }

    public static long getLong(NBTCompound compound, String key, long defaultValue) {
        return compound.hasKey(key) ? compound.getLong(key) : defaultValue;
    }

    public static float getFloat(NBTCompound compound, String key) {
        return compound.getFloat(key);
    }

    public static double getDouble(NBTCompound compound, String key) {
        return compound.getDouble(key);
    }

    public static boolean getBoolean(NBTCompound compound, String key, boolean defaultValue) {
        return compound.hasKey(key) ? compound.getBoolean(key) : defaultValue;
    }

    public static byte[] getByteArray(NBTCompound compound, String key) {
        return compound.hasKey(key) ? compound.getByteArray(key) : new byte[0];
    }

    public static int[] getIntArray(NBTCompound compound, String key) {
        return compound.hasKey(key) ? compound.getIntArray(key) : new int[0];
    }

    public static long[] getLongArray(NBTCompound compound, String key) {
        return compound.hasKey(key) ? compound.getLongArray(key) : new long[0];
    }

    /**
     * Treating the long array "data" as a continuous stream of bits, returning the "valueIndex"-th value when each value has "bitsPerValue" bits.
     */
    @SuppressWarnings("ShiftOutOfRange")
    public static long getValueFromLongStream(long[] data, int valueIndex, int bitsPerValue) {
        int bitIndex = valueIndex * bitsPerValue;
        int firstLong = bitIndex >> 6; // index / 64
        int bitOffset = bitIndex & 0x3F; // Math.floorMod(index, 64)

        if (firstLong >= data.length) return 0;
        long value = data[firstLong] >>> bitOffset;

        if (bitOffset > 0 && firstLong + 1 < data.length) {
            long value2 = data[firstLong + 1];
            value2 = value2 << -bitOffset;
            value = value | value2;
        }

        return value & (0xFFFFFFFFFFFFFFFFL >>> -bitsPerValue);
    }

    /**
     * Extracts the 4 bits of the left (largeHalf = <code>true</code>) or the right (largeHalf = <code>false</code>) side of the byte stored in <code>value</code>.<br>
     * The value is treated as an unsigned byte.
     */
    public static int getByteHalf(int value, boolean largeHalf) {
        if (largeHalf) return value >> 4 & 0xF;
        return value & 0xF;
    }

    public static int ceilLog2(int n) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(n - 1);
    }

}
