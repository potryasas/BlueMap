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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Utility methods for ByteArrayOutputStream operations that maintain Java 8 compatibility
 */
public class ByteArrayOutputStreamUtil {
    
    /**
     * Java 8 compatible alternative to ByteArrayOutputStream.writeBytes(byte[]) (from Java 11)
     * @param outputStream The output stream to write to
     * @param bytes The bytes to write
     * @throws IOException If an I/O error occurs
     */
    public static void writeBytes(ByteArrayOutputStream outputStream, byte[] bytes) throws IOException {
        outputStream.write(bytes);
    }
    
    private ByteArrayOutputStreamUtil() {
        // Utility class, no instances
    }
} 