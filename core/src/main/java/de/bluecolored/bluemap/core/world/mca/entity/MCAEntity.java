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
package de.bluecolored.bluemap.core.world.mca.entity;

import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.Entity;
import de.tr7zw.nbtapi.NBTCompound;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@EqualsAndHashCode
@ToString
@SuppressWarnings("FieldMayBeFinal")
public class MCAEntity implements Entity {

    Key id;
    UUID uuid;
    String customName;
    boolean customNameVisible;
    Vector3d pos;
    Vector3d motion;
    Vector2f rotation;

    @Override
    public Key getId() {
        return id;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getCustomName() {
        return customName;
    }

    @Override
    public boolean isCustomNameVisible() {
        return customNameVisible;
    }

    @Override
    public Vector3d getPos() {
        return pos;
    }

    @Override
    public Vector3d getMotion() {
        return motion;
    }

    @Override
    public Vector2f getRotation() {
        return rotation;
    }

    public void readFromNBT(NBTCompound compound) {
        if (compound.hasKey("UUID")) {
            this.uuid = UUID.fromString(compound.getString("UUID"));
        }
        if (compound.hasKey("CustomName")) {
            this.customName = compound.getString("CustomName");
        }
        if (compound.hasKey("CustomNameVisible")) {
            this.customNameVisible = compound.getBoolean("CustomNameVisible");
        }
        if (compound.hasKey("Pos")) {
            NBTCompound pos = compound.getCompound("Pos");
            this.pos = new Vector3d(pos.getDouble("X"), pos.getDouble("Y"), pos.getDouble("Z"));
        }
        if (compound.hasKey("Motion")) {
            NBTCompound motion = compound.getCompound("Motion");
            this.motion = new Vector3d(motion.getDouble("X"), motion.getDouble("Y"), motion.getDouble("Z"));
        }
        if (compound.hasKey("Rotation")) {
            NBTCompound rotation = compound.getCompound("Rotation");
            this.rotation = new Vector2f(rotation.getFloat("X"), rotation.getFloat("Y"));
        }
    }

    public void writeToNBT(NBTCompound compound) {
        compound.setString("UUID", uuid.toString());
        if (customName != null) {
            compound.setString("CustomName", customName);
        }
        compound.setBoolean("CustomNameVisible", customNameVisible);
        
        NBTCompound pos = compound.getCompound("Pos");
        pos.setDouble("X", this.pos.getX());
        pos.setDouble("Y", this.pos.getY());
        pos.setDouble("Z", this.pos.getZ());

        NBTCompound motion = compound.getCompound("Motion");
        motion.setDouble("X", this.motion.getX());
        motion.setDouble("Y", this.motion.getY());
        motion.setDouble("Z", this.motion.getZ());

        NBTCompound rotation = compound.getCompound("Rotation");
        rotation.setFloat("X", this.rotation.getX());
        rotation.setFloat("Y", this.rotation.getY());
    }
}
