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

import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.util.nbt.NBTAdapter;
import de.bluecolored.bluemap.core.util.nbt.NBTFileWrapper;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTList;
import de.tr7zw.nbtapi.NBTType;

import java.io.IOException;

public class LenientBlockEntityArrayDeserializer implements NBTAdapter<BlockEntity[]> {

    private static final BlockEntity[] EMPTY_BLOCK_ENTITIES_ARRAY = new BlockEntity[0];

    private final NBTAdapter<BlockEntity[]> delegate;

    public LenientBlockEntityArrayDeserializer(NBTFileWrapper nbt) throws IOException {
        delegate = nbt.getAdapter(BlockEntity[].class);
    }

    @Override
    public BlockEntity[] read(NBTCompound compound) throws IOException {
        if (!compound.hasTag("") || compound.getType("") != NBTType.NBTTagList) {
            return EMPTY_BLOCK_ENTITIES_ARRAY;
        }
        return delegate.read(compound);
    }

    @Override
    public void write(BlockEntity[] value, NBTCompound compound) throws IOException {
        if (value == null || value.length == 0) {
            return;
        }
        delegate.write(value, compound);
    }
}
