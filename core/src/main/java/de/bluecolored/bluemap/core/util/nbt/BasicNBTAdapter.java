package de.bluecolored.bluemap.core.util.nbt;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTList;
import de.tr7zw.nbtapi.NBTType;
import java.io.IOException;
import java.io.InputStream;

/**
 * A simple implementation of NBTAdapter for backward compatibility.
 * This provides basic NBT methods that are missing in older NBT API versions.
 */
public class BasicNBTAdapter {

    /**
     * Reads data of the specified type from an input stream.
     *
     * @param <D> the type of data to read
     * @param in the input stream to read from
     * @param dataType the class type to convert to
     * @return the deserialized data, or {@code null} if not implemented
     * @throws IOException if an I/O error occurs
     */
    public <D> D read(InputStream in, Class<D> dataType) throws IOException {
        // This is a simplified implementation that just returns null
        // In a real implementation, it would deserialize NBT data from the input stream
        return null;
    }

    /**
     * Gets a list of a specific type from an NBT compound.
     *
     * @param <T> the type of list elements
     * @param compound the NBT compound
     * @param key the key to get
     * @param type the class type of the list elements
     * @return the NBT list, or {@code null} if the key is not present
     */
    public static <T> NBTList getList(NBTCompound compound, String key, Class<T> type) {
        // For Java 8 compatibility, we use the generic method
        if (compound.hasKey(key)) {
            return compound.getCompoundList(key);
        }
        return null;
    }

    /**
     * Gets a float value from a list at the specified index.
     *
     * @param list the NBT list
     * @param index the index to get
     * @return the float value, or 0.0f if extraction fails
     */
    public static float getFloat(NBTList list, int index) {
        // Use reflection or alternative approach to extract float value
        try {
            // Try to get value using standard approach
            if (list.getType() == NBTType.NBTTagFloat) {
                Object value = list.get(index);
                if (value instanceof Float) {
                    return (Float) value;
                }
            }
            return 0.0f; // Default value if extraction fails
        } catch (Exception e) {
            return 0.0f; // Default value if method not available
        }
    }

    /**
     * Gets an integer value from a list at the specified index.
     *
     * @param list the NBT list
     * @param index the index to get
     * @return the integer value, or 0 if extraction fails
     */
    public static int getInteger(NBTList list, int index) {
        // Use reflection or alternative approach to extract integer value
        try {
            if (list.getType() == NBTType.NBTTagInt) {
                Object value = list.get(index);
                if (value instanceof Integer) {
                    return (Integer) value;
                }
            }
            return 0; // Default value if extraction fails
        } catch (Exception e) {
            return 0; // Default value if method not available
        }
    }

    /**
     * Gets a double value from a list at the specified index.
     *
     * @param list the NBT list
     * @param index the index to get
     * @return the double value, or 0.0 if extraction fails
     */
    public static double getDouble(NBTList list, int index) {
        // Use reflection or alternative approach to extract double value
        try {
            if (list.getType() == NBTType.NBTTagDouble) {
                Object value = list.get(index);
                if (value instanceof Double) {
                    return (Double) value;
                }
            }
            return 0.0; // Default value if extraction fails
        } catch (Exception e) {
            return 0.0; // Default value if method not available
        }
    }

    /**
     * Creates a new list in the compound with the given type.
     *
     * @param <T> the type of list elements
     * @param compound the NBT compound
     * @param key the key to create the list at
     * @param type the class type of the list elements
     * @return the created NBT list
     */
    public static <T> NBTList createList(NBTCompound compound, String key, Class<T> type) {
        // For Java 8 compatibility, we create a generic list
        return compound.getCompoundList(key);
    }
}