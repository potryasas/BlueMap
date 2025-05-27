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
package de.bluecolored.bluemap.common.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility methods for file operations that maintain Java 8 compatibility
 */
public class FileUtil {
    
    /**
     * Java 8 compatible alternative to File.toAbsolutePath()
     * @param file The File object to get absolute path for
     * @return A Path representing the absolute path
     */
    public static Path toAbsolutePath(File file) {
        return Paths.get(file.getAbsolutePath());
    }
    
    /**
     * Java 8 compatible alternative to Path.toAbsolutePath() when used on a File object
     * @param path The Path object
     * @return The absolute path 
     */
    public static Path toAbsolutePath(Path path) {
        return path.toAbsolutePath();
    }

    private FileUtil() {
        // Utility class, no instances
    }
} 