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

public class MySQLCommandSet extends AbstractCommandSet {

    public MySQLCommandSet(Database db) {
        super(db);
    }

    @Override
    @Language("mysql")
    public String createMapTableStatement() {
        return "CREATE TABLE IF NOT EXISTS `bluemap_map` (\n" +
               " `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
               " `map_id` VARCHAR(190) NOT NULL,\n" +
               " PRIMARY KEY (`id`),\n" +
               " UNIQUE INDEX `map_id` (`map_id`)\n" +
               ") COLLATE 'utf8mb4_bin'";
    }

    @Override
    @Language("mysql")
    public String createCompressionTableStatement() {
        return "CREATE TABLE IF NOT EXISTS `bluemap_compression` (\n" +
               " `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
               " `key` VARCHAR(190) NOT NULL,\n" +
               " PRIMARY KEY (`id`),\n" +
               " UNIQUE INDEX `key` (`key`)\n" +
               ") COLLATE 'utf8mb4_bin'";
    }

    @Override
    @Language("mysql")
    public String createItemStorageTableStatement() {
        return "CREATE TABLE IF NOT EXISTS `bluemap_item_storage` (\n" +
               " `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
               " `key` VARCHAR(190) NOT NULL,\n" +
               " PRIMARY KEY (`id`),\n" +
               " UNIQUE INDEX `key` (`key`)\n" +
               ") COLLATE 'utf8mb4_bin'";
    }

    @Override
    @Language("mysql")
    public String createItemStorageDataTableStatement() {
        return "CREATE TABLE IF NOT EXISTS `bluemap_item_storage_data` (\n" +
               " `map` SMALLINT UNSIGNED NOT NULL,\n" +
               " `storage` INT UNSIGNED NOT NULL,\n" +
               " `compression` SMALLINT UNSIGNED NOT NULL,\n" +
               " `data` LONGBLOB NOT NULL,\n" +
               " PRIMARY KEY (`map`, `storage`),\n" +
               " CONSTRAINT `fk_bluemap_item_map`\n" +
               "  FOREIGN KEY (`map`)\n" +
               "  REFERENCES `bluemap_map` (`id`)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE,\n" +
               " CONSTRAINT `fk_bluemap_item`\n" +
               "  FOREIGN KEY (`storage`)\n" +
               "  REFERENCES `bluemap_item_storage` (`id`)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE,\n" +
               " CONSTRAINT `fk_bluemap_item_compression`\n" +
               "  FOREIGN KEY (`compression`)\n" +
               "  REFERENCES `bluemap_compression` (`id`)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE\n" +
               ") COLLATE 'utf8mb4_bin'";
    }

    @Override
    @Language("mysql")
    public String createGridStorageTableStatement() {
        return "CREATE TABLE IF NOT EXISTS `bluemap_grid_storage` (\n" +
               " `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
               " `key` VARCHAR(190) NOT NULL,\n" +
               " PRIMARY KEY (`id`),\n" +
               " UNIQUE INDEX `key` (`key`)\n" +
               ") COLLATE 'utf8mb4_bin'";
    }

    @Override
    @Language("mysql")
    public String createGridStorageDataTableStatement() {
        return "CREATE TABLE IF NOT EXISTS `bluemap_grid_storage_data` (\n" +
               " `map` SMALLINT UNSIGNED NOT NULL,\n" +
               " `storage` SMALLINT UNSIGNED NOT NULL,\n" +
               " `x` INT NOT NULL,\n" +
               " `z` INT NOT NULL,\n" +
               " `compression` SMALLINT UNSIGNED NOT NULL,\n" +
               " `data` LONGBLOB NOT NULL,\n" +
               " PRIMARY KEY (`map`, `storage`, `x`, `z`),\n" +
               " CONSTRAINT `fk_bluemap_grid_map`\n" +
               "  FOREIGN KEY (`map`)\n" +
               "  REFERENCES `bluemap_map` (`id`)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE,\n" +
               " CONSTRAINT `fk_bluemap_grid`\n" +
               "  FOREIGN KEY (`storage`)\n" +
               "  REFERENCES `bluemap_grid_storage` (`id`)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE,\n" +
               " CONSTRAINT `fk_bluemap_grid_compression`\n" +
               "  FOREIGN KEY (`compression`)\n" +
               "  REFERENCES `bluemap_compression` (`id`)\n" +
               "  ON UPDATE RESTRICT\n" +
               "  ON DELETE CASCADE\n" +
               ") COLLATE 'utf8mb4_bin'";
    }

    @Override
    @Language("mysql")
    public String itemStorageWriteStatement() {
        return "REPLACE\n" +
               "INTO `bluemap_item_storage_data` (`map`, `storage`, `compression`, `data`)\n" +
               "VALUES (?, ?, ?, ?)";
    }

    @Override
    @Language("mysql")
    public String itemStorageReadStatement() {
        return "SELECT `data`\n" +
               "FROM `bluemap_item_storage_data`\n" +
               "WHERE `map` = ?\n" +
               "AND `storage` = ?\n" +
               "AND `compression` = ?";
    }

    @Override
    @Language("mysql")
    public String itemStorageDeleteStatement() {
        return "DELETE\n" +
               "FROM `bluemap_item_storage_data`\n" +
               "WHERE `map` = ?\n" +
               "AND `storage` = ?";
    }

    @Override
    @Language("mysql")
    public String itemStorageHasStatement() {
        return "SELECT COUNT(*) > 0\n" +
               "FROM `bluemap_item_storage_data`\n" +
               "WHERE `map` = ?\n" +
               "AND `storage` = ?\n" +
               "AND `compression` = ?";
    }

    @Override
    @Language("mysql")
    public String gridStorageWriteStatement() {
        return "REPLACE\n" +
               "INTO `bluemap_grid_storage_data` (`map`, `storage`, `x`, `z`, `compression`, `data`)\n" +
               "VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    @Language("mysql")
    public String gridStorageReadStatement() {
        return "SELECT `data`\n" +
               "FROM `bluemap_grid_storage_data`\n" +
               "WHERE `map` = ?\n" +
               "AND `storage` = ?\n" +
               "AND `x` = ?\n" +
               "AND `z` = ?\n" +
               "AND `compression` = ?";
    }

    @Override
    @Language("mysql")
    public String gridStorageDeleteStatement() {
        return "DELETE\n" +
               "FROM `bluemap_grid_storage_data`\n" +
               "WHERE `map` = ?\n" +
               "AND `storage` = ?\n" +
               "AND `x` = ?\n" +
               "AND `z` = ?";
    }

    @Override
    @Language("mysql")
    public String gridStorageHasStatement() {
        return "SELECT COUNT(*) > 0\n" +
               "FROM `bluemap_grid_storage_data`\n" +
               "WHERE `map` = ?\n" +
               "AND `storage` = ?\n" +
               "AND `x` = ?\n" +
               "AND `z` = ?\n" +
               "AND `compression` = ?";
    }

    @Override
    @Language("mysql")
    public String gridStorageListStatement() {
        return "SELECT `x`, `z`\n" +
               "FROM `bluemap_grid_storage_data`\n" +
               "WHERE `map` = ?\n" +
               "AND `storage` = ?\n" +
               "AND `compression` = ?\n" +
               "LIMIT ? OFFSET ?";
    }

    @Override
    @Language("mysql")
    public String gridStorageCountMapItemsStatement() {
        return "SELECT COUNT(*)\n" +
               "FROM `bluemap_grid_storage_data`\n" +
               "WHERE `map` = ?";
    }

    @Override
    @Language("mysql")
    public String gridStoragePurgeMapStatement() {
        return "DELETE\n" +
               "FROM `bluemap_grid_storage_data`\n" +
               "WHERE `map` = ?\n" +
               "LIMIT ?";
    }

    @Override
    @Language("mysql")
    public String purgeMapStatement() {
        return "DELETE\n" +
               "FROM `bluemap_map`\n" +
               "WHERE `id` = ?";
    }

    @Override
    @Language("mysql")
    public String hasMapStatement() {
        return "SELECT COUNT(*) > 0\n" +
               "FROM `bluemap_map`\n" +
               "WHERE `map_id` = ?";
    }

    @Override
    @Language("mysql")
    public String listMapIdsStatement() {
        return "SELECT `map_id`\n" +
               "FROM `bluemap_map`\n" +
               "LIMIT ? OFFSET ?";
    }

    @Override
    @Language("mysql")
    public String findMapKeyStatement() {
        return "SELECT `id`\n" +
               "FROM `bluemap_map`\n" +
               "WHERE `map_id` = ?";
    }

    @Override
    @Language("mysql")
    public String createMapKeyStatement() {
        return "INSERT INTO `bluemap_map` (`map_id`)\n" +
               "VALUES (?)";
    }

    @Override
    @Language("mysql")
    public String findCompressionKeyStatement() {
        return "SELECT `id`\n" +
               "FROM `bluemap_compression`\n" +
               "WHERE `key` = ?";
    }

    @Override
    @Language("mysql")
    public String createCompressionKeyStatement() {
        return "INSERT INTO `bluemap_compression` (`key`)\n" +
               "VALUES (?)";
    }

    @Override
    @Language("mysql")
    public String findItemStorageKeyStatement() {
        return "SELECT `id`\n" +
               "FROM `bluemap_item_storage`\n" +
               "WHERE `key` = ?";
    }

    @Override
    @Language("mysql")
    public String createItemStorageKeyStatement() {
        return "INSERT INTO `bluemap_item_storage` (`key`)\n" +
               "VALUES (?)";
    }

    @Override
    @Language("mysql")
    public String findGridStorageKeyStatement() {
        return "SELECT `id`\n" +
               "FROM `bluemap_grid_storage`\n" +
               "WHERE `key` = ?";
    }

    @Override
    @Language("mysql")
    public String createGridStorageKeyStatement() {
        return "INSERT INTO `bluemap_grid_storage` (`key`)\n" +
               "VALUES (?)";
    }

}
