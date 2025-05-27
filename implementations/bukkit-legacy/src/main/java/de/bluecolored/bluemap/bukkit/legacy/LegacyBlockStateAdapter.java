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
package de.bluecolored.bluemap.bukkit.legacy;

import org.bukkit.block.Block;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.BlockState;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.BlockProperty;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.LegacyBlockProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LegacyBlockStateAdapter implements BlockState {
    private final Block block;

    public LegacyBlockStateAdapter(Block block) {
        this.block = block;
    }

    @Override
    public String getId() {
        return block.getType().name().toLowerCase();
    }

    @Override
    public Map<String, BlockProperty> getProperties() {
        Map<String, BlockProperty> properties = new HashMap<>();
        properties.put("data", new LegacyBlockProperty(String.valueOf(block.getData())));
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public boolean isAir() {
        return block.getType().name().equals("AIR");
    }
}
