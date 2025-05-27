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
package de.bluecolored.bluemap.core.world.mca.blockentity;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.tr7zw.nbtapi.NBTCompound;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuppressWarnings("FieldMayBeFinal")
public class MCABlockEntity extends BlockEntity {

    Key id;
    boolean keepPacked;

    public MCABlockEntity() {
        super("", 0, 0, 0);
    }

    public MCABlockEntity(String id, int x, int y, int z) {
        super(id, x, y, z);
        this.id = Key.parse(id);
    }

    public void readFromNBT(NBTCompound compound) {
        if (compound.hasKey("keepPacked")) {
            this.keepPacked = compound.getBoolean("keepPacked");
        }
        if (compound.hasKey("x")) {
            this.x = compound.getInteger("x");
        }
        if (compound.hasKey("y")) {
            this.y = compound.getInteger("y");
        }
        if (compound.hasKey("z")) {
            this.z = compound.getInteger("z");
        }
        if (compound.hasKey("id")) {
            this.id = Key.parse(compound.getString("id"));
        }
    }

    public void writeToNBT(NBTCompound compound) {
        compound.setBoolean("keepPacked", keepPacked);
        compound.setInteger("x", x);
        compound.setInteger("y", y);
        compound.setInteger("z", z);
        compound.setString("id", id.getFormatted());
    }
}
