package de.bluecolored.bluemap.core.util.nbt;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A wrapper around NBTFile that provides type-safe reading and writing of NBT data.
 * This is a replacement for the bluenbt NBTFile to support NBT API migration.
 */
public class NBTFileWrapper {
    private final NBTFile nbtFile;
    private final Map<Class<?>, NBTAdapter<?>> adapters;

    public NBTFileWrapper(File file) throws IOException {
        this.nbtFile = new NBTFile(file);
        this.adapters = new HashMap<>();
    }

    /**
     * Registers an adapter for a specific type.
     *
     * @param type The class to register the adapter for
     * @param adapter The adapter to use for the type
     * @param <T> The type parameter
     */
    public <T> void register(Class<T> type, NBTAdapter<T> adapter) {
        adapters.put(type, adapter);
    }

    /**
     * Gets the adapter for a specific type.
     *
     * @param type The class to get the adapter for
     * @param <T> The type parameter
     * @return The adapter for the type
     * @throws IOException if no adapter is registered for the type
     */
    @SuppressWarnings("unchecked")
    public <T> NBTAdapter<T> getAdapter(Class<T> type) throws IOException {
        NBTAdapter<T> adapter = (NBTAdapter<T>) adapters.get(type);
        if (adapter == null) {
            throw new IOException("No adapter registered for type: " + type);
        }
        return adapter;
    }

    /**
     * Reads data from the NBT file using the registered adapter for the given type.
     *
     * @param type The class to read
     * @param <T> The type parameter
     * @return The read data
     * @throws IOException if reading fails or no adapter is registered
     */
    public <T> T read(Class<T> type) throws IOException {
        NBTAdapter<T> adapter = getAdapter(type);
        return adapter.read(nbtFile);
    }

    /**
     * Writes data to the NBT file using the registered adapter for the value's type.
     *
     * @param value The value to write
     * @param <T> The type parameter
     * @throws IOException if writing fails or no adapter is registered
     */
    @SuppressWarnings("unchecked")
    public <T> void write(T value) throws IOException {
        if (value == null) {
            throw new IOException("Cannot write null value");
        }
        NBTAdapter<T> adapter = (NBTAdapter<T>) getAdapter(value.getClass());
        adapter.write(value, nbtFile);
    }

    /**
     * Gets the underlying NBTCompound.
     *
     * @return The NBTCompound
     */
    public NBTCompound getCompound() {
        return nbtFile;
    }

    /**
     * Saves the NBT file to disk.
     *
     * @throws IOException if saving fails
     */
    public void save() throws IOException {
        nbtFile.save();
    }
} 
