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

import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.util.nbt.NBTAdapter;
import de.tr7zw.nbtapi.NBTCompound;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlockStateDeserializer implements NBTAdapter<BlockState> {

    @Override
    public BlockState read(NBTCompound compound) {
        String id = compound.getString("Name");
        if (id == null) throw new IllegalArgumentException("Invalid BlockState, Name is missing!");

        NBTCompound propertiesCompound = compound.getCompound("Properties");
        if (propertiesCompound == null) {
            return new BlockState(id);
        }

        Map<String, String> properties = new LinkedHashMap<>();
        for (String key : propertiesCompound.getKeys()) {
            properties.put(key, propertiesCompound.getString(key));
        }

        return new BlockState(id, properties);
    }

    @Override
    public void write(BlockState value, NBTCompound compound) {
        compound.setString("Name", value.getId());
        
        if (!value.getProperties().isEmpty()) {
            NBTCompound propertiesCompound = compound.getCompound("Properties");
            for (Map.Entry<String, String> entry : value.getProperties().entrySet()) {
                propertiesCompound.setString(entry.getKey(), entry.getValue());
            }
        }
    }
}
