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

import de.bluecolored.bluemap.core.logger.Logger;
import de.bluecolored.bluemap.core.resources.pack.datapack.DataPack;
import de.bluecolored.bluemap.core.resources.pack.datapack.dimension.DimensionTypeData;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.util.nbt.NBTAdapter;
import de.bluecolored.bluemap.core.util.nbt.NBTFileWrapper;
import de.tr7zw.nbtapi.NBTCompound;

import java.io.IOException;

public class DimensionTypeDeserializer implements NBTAdapter<DimensionType> {

    private final NBTAdapter<DimensionTypeData> defaultTypeDeserializer;
    private final DataPack dataPack;

    public DimensionTypeDeserializer(NBTFileWrapper nbt, DataPack dataPack) throws IOException {
        this.defaultTypeDeserializer = nbt.getAdapter(DimensionTypeData.class);
        this.dataPack = dataPack;
    }

    @Override
    public DimensionType read(NBTCompound compound) throws IOException {
        if (compound.hasKey("")) {
            DimensionTypeData data = defaultTypeDeserializer.read(compound);
            return mapToDimensionType(data);
        }

        Key key = Key.parse(compound.getString(""), Key.MINECRAFT_NAMESPACE);

        DimensionType dimensionType = dataPack.getDimensionType(key);
        if (dimensionType == null) {
            Logger.global.logWarning("No dimension-type found with the id '" + key.getFormatted() + "', using fallback.");
            dimensionType = DimensionType.OVERWORLD;
        }

        return dimensionType;
    }

    @Override
    public void write(DimensionType value, NBTCompound compound) {
        compound.setString("", value.getFormatted());
    }

    private DimensionType mapToDimensionType(DimensionTypeData data) {
        if (data.isNatural()) {
            if (data.isHasCeiling()) {
                return DimensionType.OVERWORLD_CAVES;
            }
            return DimensionType.OVERWORLD;
        } else if (data.getFixedTime() != null) {
            if (data.getFixedTime() == 6000L) {
                return DimensionType.NETHER;
            } else if (data.getFixedTime() == 18000L) {
                return DimensionType.END;
            }
        }

        return DimensionType.OVERWORLD;
    }
}
