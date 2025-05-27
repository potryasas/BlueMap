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

import java.io.IOException;
import java.util.function.DoublePredicate;

public interface MapStorage {

    /**
     * Returns the {@link GridStorage} holding the map's hires-tiles.
     *
     * @return the GridStorage for hi-res tiles
     */
    GridStorage hiresTiles();

    /**
     * Returns the {@link GridStorage} holding the map's lowres-tiles of the given lod level.
     *
     * @param lod the level of detail
     * @return the GridStorage for low-res tiles at the given LOD
     */
    GridStorage lowresTiles(int lod);

    /**
     * Returns a {@link GridStorage} for the tile-state (meta-) data of this map.
     *
     * @return the GridStorage for tile state data
     */
    GridStorage tileState();

    /**
     * Returns a {@link GridStorage} for the chunk-state (meta-) data of this map.
     *
     * @return the GridStorage for chunk state data
     */
    GridStorage chunkState();

    /**
     * Returns a {@link ItemStorage} for a map asset with the given name.
     *
     * @param name the name of the asset
     * @return the ItemStorage for the specified asset
     */
    ItemStorage asset(String name);

    /**
     * Returns a {@link ItemStorage} for the settings (settings.json) of this map.
     *
     * @return the ItemStorage for the settings
     */
    ItemStorage settings();

    /**
     * Returns a {@link ItemStorage} for the texture-data (textures.json) of this map.
     *
     * @return the ItemStorage for the textures
     */
    ItemStorage textures();

    /**
     * Returns a {@link ItemStorage} for the marker-data (live/markers.json) of this map.
     *
     * @return the ItemStorage for the markers
     */
    ItemStorage markers();

    /**
     * Returns a {@link ItemStorage} for the player-data (live/players.json) of this map.
     *
     * @return the ItemStorage for the players
     */
    ItemStorage players();

    /**
     * Deletes the entire map from the storage.
     *
     * @throws IOException if an I/O error occurs
     */
    default void delete() throws IOException {
        delete(info -> true);
    }

    /**
     * Deletes the entire map from the storage.
     *
     * @param onProgress a function that takes in a progress-percentage and returns true
     *                   if the deletion should continue or false if it should be aborted.
     *                   No guarantees are made on how often (if at all) this method is actually being called and if the
     *                   progress is actually aborted when false is returned.
     * @throws IOException if an I/O error occurs
     */
    void delete(DoublePredicate onProgress) throws IOException;

    /**
     * Tests whether this map currently exists on the storage or not.
     *
     * @return true if the map exists, false otherwise
     * @throws IOException if an I/O error occurs
     */
    boolean exists() throws IOException;

    /**
     * Checks if this storage is closed.
     *
     * @return true if the storage is closed, false otherwise
     */
    boolean isClosed();

    static String escapeAssetName(String name) {
        return name
                .replaceAll("[^\\w\\d.\\-_/]", "_")
                .replace("..", "_.");
    }

}