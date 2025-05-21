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
package de.bluecolored.bluemap.core.util;

import de.bluecolored.bluemap.core.util.nbt.NBTAdapter;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTList;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;

@RequiredArgsConstructor
public class PalettedArrayAdapter<T> implements NBTAdapter<T[]> {

    private final Class<T> type;
    private final ArrayAdapter<T[]> paletteAdapter;

    @SuppressWarnings("unchecked")
    public PalettedArrayAdapter(Class<T> type) {
        this.type = type;
        this.paletteAdapter = new ArrayAdapter<>(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] read(NBTCompound compound) throws IOException {
        T[] palette = null;
        byte[] data = null;

        if (compound.hasKey("palette")) {
            palette = paletteAdapter.read(compound.getCompound("palette"));
        }
        if (compound.hasKey("data")) {
            data = compound.getByteArray("data");
        }

        if (palette == null || palette.length == 0) throw new IOException("Missing or empty palette");
        if (data == null) return (T[]) Array.newInstance(type, 0);
        T[] result = (T[]) Array.newInstance(type, data.length);
        for (int i = 0; i < data.length; i++) {
            byte index = data[i];
            if (index >= palette.length) throw new IOException("Palette (size: " + palette.length + ") does not contain entry-index (" + index + ")");
            result[i] = palette[data[i]];
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(T[] value, NBTCompound compound) throws IOException {
        HashMap<T, Byte> paletteMap = new HashMap<>();
        byte[] data = new byte[value.length];
        for (int i = 0; i < value.length; i++) {
            byte index = paletteMap.computeIfAbsent(value[i], v -> (byte) paletteMap.size());
            data[i] = index;
        }

        T[] palette = (T[]) Array.newInstance(type, paletteMap.size());
        paletteMap.forEach((k, v) -> palette[v] = k);

        NBTCompound paletteCompound = compound.addCompound("palette");
        paletteAdapter.write(palette, paletteCompound);
        compound.setByteArray("data", data);
    }

    private static class ArrayAdapter<T> implements NBTAdapter<T[]> {
        private final Class<T> type;

        public ArrayAdapter(Class<T> type) {
            this.type = type;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T[] read(NBTCompound compound) {
            NBTList list = compound.getList("values", type);
            T[] result = (T[]) Array.newInstance(type, list.size());
            for (int i = 0; i < list.size(); i++) {
                result[i] = (T) list.get(i);
            }
            return result;
        }

        @Override
        public void write(T[] value, NBTCompound compound) {
            NBTList list = compound.createList("values", type);
            for (T t : value) {
                list.add(t);
            }
        }
    }
}
