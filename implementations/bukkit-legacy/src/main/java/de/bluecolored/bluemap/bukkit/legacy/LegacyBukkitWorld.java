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

import java.nio.file.Path;
import java.io.File;

import org.bukkit.World;
import org.bukkit.block.Block;

import de.bluecolored.bluemap.bukkit.legacy.java8compat.BlockState;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.ServerWorld;

/**
 * Legacy Bukkit implementation of ServerWorld interface
 */
public class LegacyBukkitWorld implements ServerWorld {
    private final World bukkitWorld;
    private final File worldFolder;

    public LegacyBukkitWorld(World bukkitWorld) {
        this.bukkitWorld = bukkitWorld;
        this.worldFolder = bukkitWorld.getWorldFolder();
    }

    @Override
    public String getName() {
        return bukkitWorld.getName();
    }

    @Override
    public String getId() {
        return bukkitWorld.getName().toLowerCase().replace(" ", "_");
    }

    public Path getWorldFolder() {
        return worldFolder.toPath();
    }

    @Override
    public String getWorldType() {
        return bukkitWorld.getWorldType().getName();
    }

    @Override
    public int getSeaLevel() {
        return bukkitWorld.getSeaLevel();
    }

    @Override
    public int getMaxHeight() {
        return bukkitWorld.getMaxHeight();
    }

    @Override
    public int getMinHeight() {
        return 0; // Minecraft 1.5.2 doesn't have negative heights
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        Block block = bukkitWorld.getBlockAt(x, y, z);
        return new LegacyBlockStateAdapter(block);
    }

    public World getBukkitWorld() {
        return bukkitWorld;
    }
}
