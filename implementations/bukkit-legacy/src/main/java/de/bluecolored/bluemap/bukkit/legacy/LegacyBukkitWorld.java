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

import de.bluecolored.bluemap.common.serverinterface.*;
import de.bluecolored.bluemap.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class LegacyBukkitWorld implements ServerWorld {

    private final World world;
    private final Key key;
    private final Path worldFolder;
    private final Environment environment;
    private final String name;
    private final UUID uuid;
    private final LegacyBlockStateAdapter blockStateAdapter;

    public LegacyBukkitWorld(World world) {
        this.world = world;
        this.environment = world.getEnvironment();
        this.name = world.getName();
        
        // Generate a stable UUID from the world name
        this.uuid = UUID.nameUUIDFromBytes(("BlueMap-World-" + this.name).getBytes());

        // Get world folder path
        this.worldFolder = getWorldFolder(world);
        
        // Create key
        String dimension = "";
        if (environment == Environment.NETHER) {
            dimension = "DIM-1";
        } else if (environment == Environment.THE_END) {
            dimension = "DIM1";
        }
        
        this.key = new Key("minecraft", this.name + (dimension.isEmpty() ? "" : "/" + dimension));
        
        // Create block state adapter for legacy Minecraft
        this.blockStateAdapter = new LegacyBlockStateAdapter(world);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public Path getWorldFolder() {
        return worldFolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        LegacyBukkitWorld that = (LegacyBukkitWorld) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return blockStateAdapter.getBlockState(x, y, z);
    }

    @Override
    public Biome getBiome(int x, int z) {
        // In 1.5.2, biome data is stored in chunk files
        // We need to adapt this based on block IDs in the chunk
        return biomeFromBlockPosition(x, z);
    }

    private Biome biomeFromBlockPosition(int x, int z) {
        // In 1.5.2 we can try to detect biomes based on block types
        // This is a simplified approach - for a complete solution
        // we would need to parse the chunk data
        
        if (environment == Environment.NETHER) {
            return Biome.NETHER_WASTES;
        }
        
        if (environment == Environment.THE_END) {
            return Biome.THE_END;
        }
        
        // For overworld, try to determine based on top blocks
        int highestBlockY = world.getHighestBlockYAt(x, z);
        
        if (highestBlockY < 0) {
            return Biome.OCEAN; // No blocks at this position
        }
        
        // Get the top block ID
        int blockId = world.getBlockTypeIdAt(x, highestBlockY, z);
        
        // Simple biome detection based on top block
        if (blockId == 12) { // Sand
            return Biome.DESERT;
        } else if (blockId == 78 || blockId == 80) { // Snow layer or snow block
            return Biome.SNOWY_PLAINS;
        } else if (blockId == 9 || blockId == 8) { // Water
            return Biome.OCEAN;
        } else if (blockId == 17) { // Log
            return Biome.FOREST;
        } else if (blockId == 2) { // Grass
            // Check if there are trees nearby to determine if it's a plains or forest
            boolean hasTreesNearby = hasTreesNearby(x, z, 5);
            return hasTreesNearby ? Biome.FOREST : Biome.PLAINS;
        }
        
        // Default biome
        return Biome.PLAINS;
    }
    
    private boolean hasTreesNearby(int x, int z, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int cx = x + dx;
                int cz = z + dz;
                int highestY = world.getHighestBlockYAt(cx, cz);
                
                if (highestY > 0) {
                    // Check for log blocks (ID 17)
                    if (world.getBlockTypeIdAt(cx, highestY - 1, cz) == 17) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    private Path getWorldFolder(World world) {
        // Try to find the world folder from the server directory
        File worldContainer = Bukkit.getWorldContainer();
        File worldFolder = new File(worldContainer, world.getName());
        
        if (worldFolder.exists() && worldFolder.isDirectory()) {
            return worldFolder.toPath();
        }
        
        // Fallback: try to guess the world folder location
        File serverDir = new File(".");
        worldFolder = new File(serverDir, world.getName());
        
        return worldFolder.toPath();
    }
    
    public World getBukkitWorld() {
        return world;
    }
} 