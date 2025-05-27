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

import de.tr7zw.nbtapi.NBTCompound;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@SuppressWarnings("FieldMayBeFinal")
public class SignBlockEntity extends MCABlockEntity {

    @Nullable String customName;
    @Nullable String text;
    @Nullable String color;
    boolean glowingText;

    public SignBlockEntity() {
        super();
    }

    public SignBlockEntity(String id, int x, int y, int z) {
        super(id, x, y, z);
    }

    @Override
    public void readFromNBT(NBTCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("CustomName")) {
            this.customName = compound.getString("CustomName");
        }
        if (compound.hasKey("Text")) {
            this.text = compound.getString("Text");
        }
        if (compound.hasKey("Color")) {
            this.color = compound.getString("Color");
        }
        if (compound.hasKey("GlowingText")) {
            this.glowingText = compound.getBoolean("GlowingText");
        }
    }

    @Override
    public void writeToNBT(NBTCompound compound) {
        super.writeToNBT(compound);
        if (customName != null) {
            compound.setString("CustomName", customName);
        }
        if (text != null) {
            compound.setString("Text", text);
        }
        if (color != null) {
            compound.setString("Color", color);
        }
        compound.setBoolean("GlowingText", glowingText);
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString
    public static class LegacySignBlockEntity extends SignBlockEntity {

        boolean hasGlowingText;
        String color = "black";
        String text1;
        String text2;
        String text3;
        String text4;

        public LegacySignBlockEntity() {
            super();
        }

        public LegacySignBlockEntity(String id, int x, int y, int z) {
            super(id, x, y, z);
        }

        @Override
        public void readFromNBT(NBTCompound compound) {
            super.readFromNBT(compound);
            if (compound.hasKey("GlowingText")) {
                this.hasGlowingText = compound.getBoolean("GlowingText");
            }
            if (compound.hasKey("Color")) {
                this.color = compound.getString("Color");
            }
            if (compound.hasKey("Text1")) {
                this.text1 = compound.getString("Text1");
            }
            if (compound.hasKey("Text2")) {
                this.text2 = compound.getString("Text2");
            }
            if (compound.hasKey("Text3")) {
                this.text3 = compound.getString("Text3");
            }
            if (compound.hasKey("Text4")) {
                this.text4 = compound.getString("Text4");
            }
        }

        @Override
        public void writeToNBT(NBTCompound compound) {
            super.writeToNBT(compound);
            compound.setBoolean("GlowingText", hasGlowingText);
            compound.setString("Color", color);
            if (text1 != null) {
                compound.setString("Text1", text1);
            }
            if (text2 != null) {
                compound.setString("Text2", text2);
            }
            if (text3 != null) {
                compound.setString("Text3", text3);
            }
            if (text4 != null) {
                compound.setString("Text4", text4);
            }
        }

        public List<String> getTexts() {
            return Arrays.asList(text1, text2, text3, text4);
        }
    }
}
