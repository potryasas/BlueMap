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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@SuppressWarnings("FieldMayBeFinal")
public class BannerBlockEntity extends MCABlockEntity {

    @Nullable String customName;
    List<Pattern> patterns = Collections.emptyList();
    
    public BannerBlockEntity() {
        super();
    }
    
    public BannerBlockEntity(String id, int x, int y, int z) {
        super(id, x, y, z);
    }

    @Override
    public void readFromNBT(NBTCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("CustomName")) {
            this.customName = compound.getString("CustomName");
        }
        if (compound.hasKey("Patterns")) {
            List<Pattern> patterns = new ArrayList<>();
            NBTCompound patternsCompound = compound.getCompound("Patterns");
            for (String key : patternsCompound.getKeys()) {
                NBTCompound patternCompound = patternsCompound.getCompound(key);
                Pattern pattern = new Pattern();
                pattern.pattern = patternCompound.getString("Pattern");
                pattern.color = patternCompound.getString("Color");
                patterns.add(pattern);
            }
            this.patterns = patterns;
        }
    }

    @Override
    public void writeToNBT(NBTCompound compound) {
        super.writeToNBT(compound);
        if (customName != null) {
            compound.setString("CustomName", customName);
        }
        if (!patterns.isEmpty()) {
            NBTCompound patternsCompound = compound.getCompound("Patterns");
            for (int i = 0; i < patterns.size(); i++) {
                Pattern pattern = patterns.get(i);
                NBTCompound patternCompound = patternsCompound.getCompound(String.valueOf(i));
                patternCompound.setString("Pattern", pattern.pattern.toString());
                patternCompound.setString("Color", pattern.color.toString());
            }
        }
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    @SuppressWarnings("FieldMayBeFinal")
    public static class Pattern {
        // TODO: proper pattern-data implementation
        Object pattern;
        Object color;
    }

    /*
    public enum Color {
        WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY, LIGHT_GRAY, CYAN, PURPLE, BLUE, BROWN, GREEN,
        RED, BLACK
    }
    */
}
