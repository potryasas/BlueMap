package de.bluecolored.bluemap.core.world.mca;

import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.nbt.BasicNBTAdapter;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Utility class for operations related to Minecraft Anvil (MCA) region files and NBT data.
 */
public class MCAUtil {

    public static final NBTFileWrapper NBT;
    public static final BasicNBTAdapter BLUENBT;

    private static final int SECTOR_SIZE = 4096;
    private static final int SECTORS_PER_REGION = 32;
    private static final int CHUNKS_PER_REGION = SECTORS_PER_REGION * SECTORS_PER_REGION;

    static {
        try {
            NBT = addCommonNbtSettings(new NBTFileWrapper(new File("temp.nbt")));
            BLUENBT = new BasicNBTAdapter();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize NBT wrapper", e);
        }
    }

    /**
     * Registers common deserializers for use with the given NBTFileWrapper.
     *
     * @param nbt the NBTFileWrapper to register settings on
     * @return the same NBTFileWrapper instance
     */
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

    /**
     * Returns the path to the region folder for the specified world folder and dimension.
     *
     * @param worldFolder the path to the world folder
     * @param dimension the dimension type
     * @return the path to the region folder
     */
    public static Path getRegionFolder(Path worldFolder, DimensionType dimension) {
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
        return worldFolder.resolve(dimensionPath);
    }

    /**
     * Returns the region folder as a File object for the specified world folder and dimension.
     *
     * @param worldFolder the world folder
     * @param dimension the dimension type
     * @return the region folder as a File
     */
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

    /**
     * Returns the region file corresponding to a chunk location in the overworld.
     *
     * @param worldFolder the world folder
     * @param chunkX the chunk x coordinate
     * @param chunkZ the chunk z coordinate
     * @return the region file
     */
    public static File getRegionFile(File worldFolder, int chunkX, int chunkZ) {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        String fileName = String.format("r.%d.%d.mca", regionX, regionZ);
        return new File(getRegionFolder(worldFolder, DimensionType.OVERWORLD), fileName);
    }

    /**
     * Reads an NBT file and returns a wrapper.
     *
     * @param file the NBT file
     * @return the NBTFileWrapper for the file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static NBTFileWrapper readNBTFile(File file) throws IOException {
        return new NBTFileWrapper(file);
    }

    /**
     * Returns the NBT compound stored under the given key in the provided compound.
     *
     * @param compound the parent NBT compound
     * @param key the key of the child compound
     * @return the child NBT compound
     */
    public static NBTCompound getCompound(NBTCompound compound, String key) {
        return compound.getCompound(key);
    }

    /**
     * Returns the NBT list stored under the given key in the provided compound.
     *
     * @param compound the parent NBT compound
     * @param key the key of the list
     * @return the NBT list
     */
    public static NBTList<?> getList(NBTCompound compound, String key) {
        return compound.getCompoundList(key);
    }

    /**
     * Returns the string value associated with the given key in the compound, or the default if not present.
     *
     * @param compound the NBT compound
     * @param key the key to get
     * @param defaultValue the value to return if the key is not present
     * @return the string value for the key, or defaultValue if not present
     */
    public static String getString(NBTCompound compound, String key, String defaultValue) {
        return compound.hasKey(key) ? compound.getString(key) : defaultValue;
    }

    /**
     * Returns the integer value associated with the given key in the compound, or the default if not present.
     *
     * @param compound the NBT compound
     * @param key the key to get
     * @param defaultValue the value to return if the key is not present
     * @return the integer value for the key, or defaultValue if not present
     */
    public static int getInt(NBTCompound compound, String key, int defaultValue) {
        return compound.hasKey(key) ? compound.getInteger(key) : defaultValue;
    }

    /**
     * Returns the long value associated with the given key in the compound, or the default if not present.
     *
     * @param compound the NBT compound
     * @param key the key to get
     * @param defaultValue the value to return if the key is not present
     * @return the long value for the key, or defaultValue if not present
     */
    public static long getLong(NBTCompound compound, String key, long defaultValue) {
        return compound.hasKey(key) ? compound.getLong(key) : defaultValue;
    }

    /**
     * Returns the float value associated with the given key in the compound.
     *
     * @param compound the NBT compound
     * @param key the key to get
     * @return the float value for the key
     */
    public static float getFloat(NBTCompound compound, String key) {
        return compound.getFloat(key);
    }

    /**
     * Returns the double value associated with the given key in the compound.
     *
     * @param compound the NBT compound
     * @param key the key to get
     * @return the double value for the key
     */
    public static double getDouble(NBTCompound compound, String key) {
        return compound.getDouble(key);
    }

    /**
     * Returns the boolean value associated with the given key in the compound, or the default if not present.
     *
     * @param compound the NBT compound
     * @param key the key to get
     * @param defaultValue the value to return if the key is not present
     * @return the boolean value for the key, or defaultValue if not present
     */
    public static boolean getBoolean(NBTCompound compound, String key, boolean defaultValue) {
        return compound.hasKey(key) ? compound.getBoolean(key) : defaultValue;
    }

    /**
     * Returns the byte array associated with the given key in the compound, or an empty array if not present.
     *
     * @param compound the NBT compound
     * @param key the key to get
     * @return the byte array for the key, or an empty array if not present
     */
    public static byte[] getByteArray(NBTCompound compound, String key) {
        return compound.hasKey(key) ? compound.getByteArray(key) : new byte[0];
    }

    /**
     * Returns the int array associated with the given key in the compound, or an empty array if not present.
     *
     * @param compound the NBT compound
     * @param key the key to get
     * @return the int array for the key, or an empty array if not present
     */
    public static int[] getIntArray(NBTCompound compound, String key) {
        return compound.hasKey(key) ? compound.getIntArray(key) : new int[0];
    }

    /**
     * Returns the long array associated with the given key in the compound, or an empty array if not present.
     *
     * @param compound the NBT compound
     * @param key the key to get
     * @return the long array for the key, or an empty array if not present
     */
    public static long[] getLongArray(NBTCompound compound, String key) {
        return compound.hasKey(key) ? compound.getLongArray(key) : new long[0];
    }

    /**
     * Treating the long array "data" as a continuous stream of bits, returning the "valueIndex"-th value when each value has "bitsPerValue" bits.
     *
     * @param data the long array containing bit-packed values
     * @param valueIndex the index of the value to retrieve
     * @param bitsPerValue the number of bits per value
     * @return the value at the specified index
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
     *
     * @param value the byte value to extract from
     * @param largeHalf true to extract the left (high) 4 bits, false for the right (low) 4 bits
     * @return the extracted 4-bit value
     */
    public static int getByteHalf(int value, boolean largeHalf) {
        if (largeHalf) return value >> 4 & 0xF;
        return value & 0xF;
    }

    /**
     * Returns the ceiling of the log base 2 of the given integer.
     *
     * @param n the integer
     * @return the smallest integer not less than log2(n)
     */
    public static int ceilLog2(int n) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(n - 1);
    }

}