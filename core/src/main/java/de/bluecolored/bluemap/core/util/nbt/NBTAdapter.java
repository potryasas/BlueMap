package de.bluecolored.bluemap.core.util.nbt;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTList;

import java.io.IOException;

public interface NBTAdapter<T> {
    T read(NBTCompound compound) throws IOException;
    void write(T value, NBTCompound compound) throws IOException;
} 