package de.bluecolored.bluemap.bukkit.legacy.render;

import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.ChunkSnapshot;

public class BlockNeighborhoodLegacy {
    private final World world;
    private final int x, y, z;
    private final ChunkSnapshot chunk;
    private final int blockX, blockY, blockZ;
    private final Block block;

    public BlockNeighborhoodLegacy(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;

        // Получаем координаты в чанке
        this.blockX = x & 0xF;
        this.blockY = y;
        this.blockZ = z & 0xF;

        // Получаем снапшот чанка и блок для быстрого доступа
        this.chunk = world.getChunkAt(x >> 4, z >> 4).getChunkSnapshot();
        this.block = world.getBlockAt(x, y, z);
    }

    public Block getBlock() {
        return block;
    }

    @SuppressWarnings("deprecation")
    public Material getType() {
        int typeId = chunk.getBlockTypeId(blockX, blockY, blockZ);
        return Material.getMaterial(typeId);
    }

    @SuppressWarnings("deprecation")
    public byte getData() {
        return (byte) chunk.getBlockData(blockX, blockY, blockZ);
    }

    public BlockNeighborhoodLegacy getRelative(int dx, int dy, int dz) {
        return new BlockNeighborhoodLegacy(world, x + dx, y + dy, z + dz);
    }

    public byte getBlockLight() {
        return block.getLightLevel();
    }

    public byte getSkyLight() {
        return block.getLightFromSky();
    }

    public boolean isOpaque() {
        Material type = getType();
        return type.isOccluding();
    }

    public boolean isTransparent() {
        Material type = getType();
        return !type.isOccluding();
    }

    public boolean isAir() {
        return getType() == Material.AIR;
    }

    public boolean isWater() {
        Material type = getType();
        return type == Material.WATER || type == Material.STATIONARY_WATER;
    }

    public boolean isLava() {
        Material type = getType();
        return type == Material.LAVA || type == Material.STATIONARY_LAVA;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public World getWorld() {
        return world;
    }
} 