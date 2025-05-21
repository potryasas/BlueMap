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
package de.bluecolored.bluemap.core.storage.sql.commandset;

import de.bluecolored.bluemap.core.storage.sql.Database;
import org.intellij.lang.annotations.Language;

public class PostgreSQLCommandSet extends AbstractCommandSet {

    public PostgreSQLCommandSet(Database db) {
        super(db);
    }

    @Override
    @Language("postgresql")
    public String createMapTableStatement() {
        return "CREATE TABLE IF NOT EXISTS bluemap_map (\n" +
               " id SMALLSERIAL PRIMARY KEY,\n" +
               " map_id VARCHAR(190) UNIQUE NOT NULL\n" +
               ")";
    }

    @Override
    @Language("postgresql")
    public String createCompressionTableStatement() {
        return "CREATE TABLE IF NOT EXISTS bluemap_compression (\n" +
               " id SMALLSERIAL PRIMARY KEY,\n" +
               " key VARCHAR(190) UNIQUE NOT NULL\n" +
               ")";
    }

    @Override
    @Language("postgresql")
    public String createItemStorageTableStatement() {
        return "CREATE TABLE IF NOT EXISTS bluemap_item_storage (\n" +
               " id SERIAL PRIMARY KEY,\n" +
               " key VARCHAR(190) UNIQUE NOT NULL\n" +
               ")";
    }

    @Override
    @Language("postgresql")
    public String createItemStorageDataTableStatement() {
        return "CREATE TABLE IF NOT EXISTS bluemap_item_storage_data (\n" +
               " map SMALLINT NOT NULL\n" +
               "  REFERENCES bluemap_map (id)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE,\n" +
               " storage INT NOT NULL\n" +
               "  REFERENCES bluemap_item_storage (id)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE,\n" +
               " compression SMALLINT NOT NULL\n" +
               "  REFERENCES bluemap_compression (id)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE,\n" +
               " data BYTEA NOT NULL,\n" +
               " PRIMARY KEY (map, storage)\n" +
               ")";
    }

    @Override
    @Language("postgresql")
    public String createGridStorageTableStatement() {
        return "CREATE TABLE IF NOT EXISTS bluemap_grid_storage (\n" +
               " id SMALLSERIAL PRIMARY KEY,\n" +
               " key VARCHAR(190) UNIQUE NOT NULL\n" +
               ")";
    }

    @Override
    @Language("postgresql")
    public String createGridStorageDataTableStatement() {
        return "CREATE TABLE IF NOT EXISTS bluemap_grid_storage_data (\n" +
               " map SMALLINT NOT NULL\n" +
               "  REFERENCES bluemap_map (id)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE,\n" +
               " storage SMALLINT NOT NULL\n" +
               "  REFERENCES bluemap_grid_storage (id)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE,\n" +
               " x INT NOT NULL,\n" +
               " z INT NOT NULL,\n" +
               " compression SMALLINT NOT NULL\n" +
               "  REFERENCES bluemap_compression (id)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE,\n" +
               " data BYTEA NOT NULL,\n" +
               " PRIMARY KEY (map, storage, x, z)\n" +
               ")";
    }

    @Override
    @Language("postgresql")
    public String itemStorageWriteStatement() {
        return "INSERT\n" +
               "INTO bluemap_item_storage_data (map, storage, compression, data)\n" +
               "VALUES (?, ?, ?, ?)\n" +
               "ON CONFLICT (map, storage)\n" +
               " DO UPDATE SET\n" +
               "  compression = excluded.compression,\n" +
               "  data = excluded.data";
    }

    @Override
    @Language("postgresql")
    public String itemStorageReadStatement() {
        return "SELECT data\n" +
               "FROM bluemap_item_storage_data\n" +
               "WHERE map = ?\n" +
               "AND storage = ?\n" +
               "AND compression = ?";
    }

    @Override
    @Language("postgresql")
    public String itemStorageDeleteStatement() {
        return "DELETE\n" +
               "FROM bluemap_item_storage_data\n" +
               "WHERE map = ?\n" +
               "AND storage = ?";
    }

    @Override
    @Language("postgresql")
    public String itemStorageHasStatement() {
        return "SELECT COUNT(*) > 0\n" +
               "FROM bluemap_item_storage_data\n" +
               "WHERE map = ?\n" +
               "AND storage = ?\n" +
               "AND compression = ?";
    }

    @Override
    @Language("postgresql")
    public String gridStorageWriteStatement() {
        return "INSERT\n" +
               "INTO bluemap_grid_storage_data (map, storage, x, z, compression, data)\n" +
               "VALUES (?, ?, ?, ?, ?, ?)\n" +
               "ON CONFLICT (map, storage, x, z)\n" +
               " DO UPDATE SET\n" +
               "  compression = excluded.compression,\n" +
               "  data = excluded.data";
    }

    @Override
    @Language("postgresql")
    public String gridStorageReadStatement() {
        return "SELECT data\n" +
               "FROM bluemap_grid_storage_data\n" +
               "WHERE map = ?\n" +
               "AND storage = ?\n" +
               "AND x = ?\n" +
               "AND z = ?\n" +
               "AND compression = ?";
    }

    @Override
    @Language("postgresql")
    public String gridStorageDeleteStatement() {
        return "DELETE\n" +
               "FROM bluemap_grid_storage_data\n" +
               "WHERE map = ?\n" +
               "AND storage = ?\n" +
               "AND x = ?\n" +
               "AND z = ?";
    }

    @Override
    @Language("postgresql")
    public String gridStorageHasStatement() {
        return "SELECT COUNT(*) > 0\n" +
               "FROM bluemap_grid_storage_data\n" +
               "WHERE map = ?\n" +
               "AND storage = ?\n" +
               "AND x = ?\n" +
               "AND z = ?\n" +
               "AND compression = ?";
    }

    @Override
    @Language("postgresql")
    public String gridStorageListStatement() {
        return "SELECT x, z\n" +
               "FROM bluemap_grid_storage_data\n" +
               "WHERE map = ?\n" +
               "AND storage = ?\n" +
               "AND compression = ?\n" +
               "LIMIT ? OFFSET ?";
    }

    @Override
    @Language("postgresql")
    public String gridStorageCountMapItemsStatement() {
        return "SELECT COUNT(*)\n" +
               "FROM bluemap_grid_storage_data\n" +
               "WHERE map = ?";
    }

    @Override
    @Language("postgresql")
    public String gridStoragePurgeMapStatement() {
        return "DELETE\n" +
               "FROM bluemap_grid_storage_data\n" +
               "WHERE map = ?\n" +
               "LIMIT ?";
    }

    @Override
    @Language("postgresql")
    public String purgeMapStatement() {
        return "DELETE\n" +
               "FROM bluemap_map\n" +
               "WHERE id = ?";
    }

    @Override
    @Language("postgresql")
    public String hasMapStatement() {
        return "SELECT COUNT(*) > 0\n" +
               "FROM bluemap_map\n" +
               "WHERE map_id = ?";
    }

    @Override
    @Language("postgresql")
    public String listMapIdsStatement() {
        return "SELECT map_id\n" +
               "FROM bluemap_map\n" +
               "LIMIT ? OFFSET ?";
    }

    @Override
    @Language("postgresql")
    public String findMapKeyStatement() {
        return "SELECT id\n" +
               "FROM bluemap_map\n" +
               "WHERE map_id = ?";
    }

    @Override
    @Language("postgresql")
    public String createMapKeyStatement() {
        return "INSERT INTO bluemap_map (map_id)\n" +
               "VALUES (?)";
    }

    @Override
    @Language("postgresql")
    public String findCompressionKeyStatement() {
        return "SELECT id\n" +
               "FROM bluemap_compression\n" +
               "WHERE key = ?";
    }

    @Override
    @Language("postgresql")
    public String createCompressionKeyStatement() {
        return "INSERT INTO bluemap_compression (key)\n" +
               "VALUES (?)";
    }

    @Override
    @Language("postgresql")
    public String findItemStorageKeyStatement() {
        return "SELECT id\n" +
               "FROM bluemap_item_storage\n" +
               "WHERE key = ?";
    }

    @Override
    @Language("postgresql")
    public String createItemStorageKeyStatement() {
        return "INSERT INTO bluemap_item_storage (key)\n" +
               "VALUES (?)";
    }

    @Override
    @Language("postgresql")
    public String findGridStorageKeyStatement() {
        return "SELECT id\n" +
               "FROM bluemap_grid_storage\n" +
               "WHERE key = ?";
    }

    @Override
    @Language("postgresql")
    public String createGridStorageKeyStatement() {
        return "INSERT INTO bluemap_grid_storage (key)\n" +
               "VALUES (?)";
    }

}
