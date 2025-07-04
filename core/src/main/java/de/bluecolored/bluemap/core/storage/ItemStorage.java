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
package de.bluecolored.bluemap.core.storage;

import de.bluecolored.bluemap.core.storage.compression.CompressedInputStream;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;

public interface ItemStorage {

    /**
     * Returns an {@link OutputStream} that can be used to write the item-data of this storage
     * (overwriting any existing item).
     * The OutputStream is expected to be closed by the caller of this method.
     *
     * @return an OutputStream to write the item-data
     * @throws IOException if an I/O error occurs
     */
    OutputStream write() throws IOException;

    /**
     * Returns a {@link CompressedInputStream} that can be used to read the item-data from this storage
     * or null if there is nothing stored.
     * The CompressedInputStream is expected to be closed by the caller of this method.
     *
     * @return a CompressedInputStream to read the item-data, or null if nothing is stored
     * @throws IOException if an I/O error occurs
     */
    @Nullable CompressedInputStream read() throws IOException;

    /**
     * Deletes the item from this storage.
     *
     * @throws IOException if an I/O error occurs
     */
    void delete() throws IOException;

    /**
     * Tests if this item of this storage exists.
     *
     * @return true if the item exists, false otherwise
     * @throws IOException if an I/O error occurs
     */
    boolean exists() throws IOException;

    /**
     * Checks if this storage is closed.
     *
     * @return true if the storage is closed, false otherwise
     */
    boolean isClosed();

}