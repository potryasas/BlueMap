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

import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.PathUtil;
import de.bluecolored.bluemap.core.util.WatchService;

import java.nio.file.Path;

public abstract class AbstractWorld implements World {

    private static Path workingDirectory = null;
    protected final Path worldFolder;
    protected final WatchService watchService;

    protected AbstractWorld(Path worldFolder, WatchService watchService) {
        this.worldFolder = worldFolder;
        this.watchService = watchService;
    }

    @Override
    public String getId() {
        return World.id(worldFolder, new Key(getDimensionType().getFormatted()));
    }

    @Override
    public String getName() {
        return worldFolder.getFileName().toString();
    }

    @Override
    public Vector3i getSpawnPoint() {
        return new Vector3i(0, 64, 0); // Default spawn point
    }

    public static Path getWorkingDirectory() {
        if (workingDirectory == null) {
            Path workingDir = PathUtil.emptyPath().toAbsolutePath().normalize();
            workingDirectory = workingDir;
        }
        return workingDirectory;
    }

} 
