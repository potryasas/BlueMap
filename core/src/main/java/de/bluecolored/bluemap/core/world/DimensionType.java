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
package de.bluecolored.bluemap.core.world;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

public enum DimensionType {
    OVERWORLD,
    OVERWORLD_CAVES,
    NETHER,
    END;

    public String getFormatted() {
        switch (this) {
            case OVERWORLD:
                return "minecraft:overworld";
            case NETHER:
                return "minecraft:the_nether";
            case END:
                return "minecraft:the_end";
            default:
                return "minecraft:overworld";
        }
    }

    public boolean isNatural() {
        return this == OVERWORLD || this == OVERWORLD_CAVES;
    }

    public boolean hasSkylight() {
        return this == OVERWORLD || this == OVERWORLD_CAVES || this == NETHER;
    }

    public boolean hasCeiling() {
        return this == OVERWORLD || this == OVERWORLD_CAVES;
    }

    public float getAmbientLight() {
        switch (this) {
            case OVERWORLD:
            case OVERWORLD_CAVES:
            case END:
                return 0.0f;
            case NETHER:
                return 0.1f;
            default:
                return 0.0f;
        }
    }

    public int getMinY() {
        switch (this) {
            case OVERWORLD:
            case OVERWORLD_CAVES:
                return -64;
            case NETHER:
            case END:
                return 0;
            default:
                return 0;
        }
    }

    public int getHeight() {
        switch (this) {
            case OVERWORLD:
            case OVERWORLD_CAVES:
                return 384;
            case NETHER:
            case END:
                return 256;
            default:
                return 256;
        }
    }

    public Long getFixedTime() {
        switch (this) {
            case OVERWORLD:
            case OVERWORLD_CAVES:
                return null;
            case NETHER:
                return 6000L;
            case END:
                return 18000L;
            default:
                return null;
        }
    }

    public double getCoordinateScale() {
        switch (this) {
            case OVERWORLD:
            case OVERWORLD_CAVES:
            case END:
                return 1.0;
            case NETHER:
                return 8.0;
            default:
                return 1.0;
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builtin {
        private final boolean natural;
        @Accessors(fluent = true) private final boolean hasSkylight;
        @Accessors(fluent = true) private final boolean hasCeiling;
        private final float ambientLight;
        private final int minY;
        private final int height;
        private final Long fixedTime;
        private final double coordinateScale;
    }
}
