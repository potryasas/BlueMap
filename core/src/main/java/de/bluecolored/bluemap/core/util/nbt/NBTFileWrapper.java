package de.bluecolored.bluemap.core.util.nbt;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTFile;
import de.tr7zw.nbtapi.NBTList;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NBTFileWrapper {
    private final NBTFile nbtFile;
    private final Map<Class<?>, NBTAdapter<?>> adapters;

    public NBTFileWrapper(File file) throws IOException {
        this.nbtFile = new NBTFile(file);
        this.adapters = new HashMap<>();
    }

    public <T> void register(Class<T> type, NBTAdapter<T> adapter) {
        adapters.put(type, adapter);
    }

    @SuppressWarnings("unchecked")
    public <T> T read(Class<T> type) throws IOException {
        NBTAdapter<T> adapter = (NBTAdapter<T>) adapters.get(type);
        if (adapter == null) {
            throw new IOException("No adapter registered for type: " + type);
        }
        return adapter.read(nbtFile);
    }

    @SuppressWarnings("unchecked")
    public <T> void write(T value) throws IOException {
        NBTAdapter<T> adapter = (NBTAdapter<T>) adapters.get(value.getClass());
        if (adapter == null) {
            throw new IOException("No adapter registered for type: " + value.getClass());
        }
        adapter.write(value, nbtFile);
    }

    public NBTCompound getCompound() {
        return nbtFile;
    }

    public void save() throws IOException {
        nbtFile.save();
    }

    @SuppressWarnings("unchecked")
    public <T> NBTAdapter<T> getAdapter(Class<T> type) {
        return (NBTAdapter<T>) adapters.get(type);
    }
} 