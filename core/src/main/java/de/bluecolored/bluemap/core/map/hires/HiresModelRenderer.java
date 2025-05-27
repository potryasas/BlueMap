package de.bluecolored.bluemap.core.map.hires;

import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.logger.Logger;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.TileMetaConsumer;
import de.bluecolored.bluemap.core.map.hires.block.BlockStateModelRenderer;
import de.bluecolored.bluemap.core.map.hires.entity.EntityModelRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.Chunk;
import de.bluecolored.bluemap.core.world.World;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.Block;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Renders map tiles using high-resolution models for blocks and entities.
 * Integrated tile rendering system for Minecraft 1.5.2 compatibility.
 */
public class HiresModelRenderer {

    private final ResourcePack resourcePack;
    private final RenderSettings renderSettings;
    private final Path mapsRoot;

    private static final int TILE_SIZE = 501;
    private static final int TILE_HEIGHT = 1002;
    private static final int MAP_HEIGHT = 501;
    private static final int CHUNK_SIZE = 16;
    private static final int ZOOM_LEVEL = 0; // Fixed zoom level

    private final ThreadLocal<BlockStateModelRenderer> threadLocalBlockRenderer;
    private final ThreadLocal<EntityModelRenderer> threadLocalEntityRenderer;

    private final Map<String, java.awt.Color> blockColorMap;

    public HiresModelRenderer(ResourcePack resourcePack, TextureGallery textureGallery, RenderSettings renderSettings, Path mapsRoot) {
        this.resourcePack = resourcePack;
        this.renderSettings = renderSettings;
        this.mapsRoot = mapsRoot;

        this.threadLocalBlockRenderer = ThreadLocal.withInitial(() -> new BlockStateModelRenderer(resourcePack, textureGallery, renderSettings));
        this.threadLocalEntityRenderer = ThreadLocal.withInitial(() -> new EntityModelRenderer(resourcePack, textureGallery, renderSettings));

        this.blockColorMap = new HashMap<>();
        initializeBlockColors();
    }

    public void render(World world, Vector3i modelMin, Vector3i modelMax, TileModel model) {
        render(world, modelMin, modelMax, model, (x, z, c, h, l) -> {});
    }

    public void render(World world, Vector3i modelMin, Vector3i modelMax, TileModel tileModel, TileMetaConsumer tileMetaConsumer) {
        try {
            Vector3i min = modelMin;
            Vector3i max = modelMax;

            int tileX = modelMin.getX() / (CHUNK_SIZE * (1 << ZOOM_LEVEL));
            int tileZ = modelMin.getZ() / (CHUNK_SIZE * (1 << ZOOM_LEVEL));

            renderTileIntegrated(world, min, max, tileX, tileZ);

        } catch (Exception e) {
            Logger.global.logError("Failed to render tile", e);
        }
    }

    private void renderTileIntegrated(World world, Vector3i min, Vector3i max, int tileX, int tileZ) throws IOException {
        BufferedImage tileImage = new BufferedImage(TILE_SIZE, TILE_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        int startWorldX = min.getX();
        int startWorldZ = min.getZ();

        Map<String, ChunkData> chunkCache = new HashMap<>();

        double scaleX = (double) TILE_SIZE / (max.getX() - startWorldX);
        double scaleZ = (double) TILE_SIZE / (max.getZ() - startWorldZ);

        // Top-down view
        for (int worldX = startWorldX; worldX < max.getX(); worldX++) {
            for (int worldZ = startWorldZ; worldZ < max.getZ(); worldZ++) {
                int px = (int)((worldX - startWorldX) * scaleX);
                int pz = (int)((worldZ - startWorldZ) * scaleZ);

                if (px >= 0 && px < TILE_SIZE && pz >= 0 && pz < MAP_HEIGHT) {
                    java.awt.Color color = getTopBlockColorCached(world, worldX, worldZ, chunkCache);
                    tileImage.setRGB(px, pz, color.getRGB());
                }
            }
        }

        // Height map
        for (int worldX = startWorldX; worldX < max.getX(); worldX++) {
            for (int worldZ = startWorldZ; worldZ < max.getZ(); worldZ++) {
                int px = (int)((worldX - startWorldX) * scaleX);
                int pz = (int)((worldZ - startWorldZ) * scaleZ);

                if (px >= 0 && px < TILE_SIZE && pz >= 0 && pz < MAP_HEIGHT) {
                    int height = getTopBlockYCached(world, worldX, worldZ, chunkCache);
                    java.awt.Color color = getHeightColor(height);
                    tileImage.setRGB(px, MAP_HEIGHT + pz, color.getRGB());
                }
            }
        }

        // Save tile
        Path worldDir = mapsRoot.resolve(world.getName());
        Path tilesDir = worldDir.resolve("tiles").resolve(String.valueOf(ZOOM_LEVEL));
        Path xDir = tilesDir.resolve("x" + tileX);
        Path tilePath = xDir.resolve("z" + tileZ + ".png");

        Files.createDirectories(xDir);
        ImageIO.write(tileImage, "PNG", tilePath.toFile());
        Logger.global.logInfo("Tile rendered and saved to: " + tilePath.toAbsolutePath());
    }

    private java.awt.Color getTopBlockColorCached(World world, int x, int z, Map<String, ChunkData> cache) {
        int y = getTopBlockYCached(world, x, z, cache);
        if (y == -1) return new java.awt.Color(64, 64, 255); // Water

        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int lx = x & 15;
        int lz = z & 15;

        ChunkData data = getChunkData(world, chunkX, chunkZ, cache);
        if (data == null) return new java.awt.Color(255, 0, 255); // Error magenta

        int blockId = data.getBlockId(lx, y, lz);
        byte metadata = data.getBlockData(lx, y, lz);

        return getBlockColorById(blockId, metadata);
    }

    private int getTopBlockYCached(World world, int x, int z, Map<String, ChunkData> cache) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int lx = x & 15;
        int lz = z & 15;

        ChunkData data = getChunkData(world, chunkX, chunkZ, cache);
        if (data == null) return -1;

        for (int y = 255; y >= 0; y--) {
            if (data.getBlockId(lx, y, lz) != 0) return y;
        }

        return -1;
    }

    private java.awt.Color getBlockColorById(int blockId, byte data) {
        String key = blockId + ":" + data;
        java.awt.Color cached = blockColorMap.get(key);
        if (cached != null) return cached;

        java.awt.Color color = getColorForBlock1_5_2(blockId, data);
        blockColorMap.put(key, color);
        return color;
    }

    private java.awt.Color getColorForBlock1_5_2(int blockId, byte data) {
        switch (blockId) {
            case 0: return new java.awt.Color(0, 0, 0, 0); // Air
            case 1: return new java.awt.Color(125, 125, 125); // Stone
            case 2: return new java.awt.Color(117, 176, 73); // Grass
            case 3: return new java.awt.Color(134, 96, 67); // Dirt
            case 4: return new java.awt.Color(118, 118, 118); // Cobblestone
            case 5: // Wood Planks
                switch (data) {
                    case 0: return new java.awt.Color(157, 128, 79); // Oak
                    case 1: return new java.awt.Color(102, 81, 51); // Spruce
                    case 2: return new java.awt.Color(221, 221, 155); // Birch
                    default: return new java.awt.Color(157, 128, 79);
                }
            case 8:
            case 9: return new java.awt.Color(64, 64, 255); // Water
            case 10:
            case 11: return new java.awt.Color(255, 90, 0); // Lava
            case 12: return new java.awt.Color(247, 233, 163); // Sand
            case 13: return new java.awt.Color(136, 126, 126); // Gravel
            case 14: return new java.awt.Color(255, 215, 0); // Gold Ore
            case 15: return new java.awt.Color(216, 175, 147); // Iron Ore
            case 17: // Log
                switch (data & 3) {
                    case 0: return new java.awt.Color(102, 81, 51); // Oak
                    case 1: return new java.awt.Color(54, 47, 30); // Spruce
                    default: return new java.awt.Color(102, 81, 51);
                }
            case 35: // Wool
                switch (data) {
                    case 0: return new java.awt.Color(221, 221, 221); // White
                    case 1: return new java.awt.Color(219, 125, 62); // Orange
                    case 2: return new java.awt.Color(179, 80, 188); // Magenta
                    case 3: return new java.awt.Color(107, 138, 201); // Light Blue
                    case 4: return new java.awt.Color(177, 166, 39); // Yellow
                    case 5: return new java.awt.Color(65, 174, 56); // Lime
                    case 6: return new java.awt.Color(208, 132, 153); // Pink
                    case 7: return new java.awt.Color(64, 64, 64); // Gray
                    case 8: return new java.awt.Color(154, 161, 161); // Light Gray
                    case 9: return new java.awt.Color(46, 110, 137); // Cyan
                    case 10: return new java.awt.Color(126, 61, 181); // Purple
                    case 11: return new java.awt.Color(46, 56, 141); // Blue
                    case 12: return new java.awt.Color(79, 50, 31); // Brown
                    case 13: return new java.awt.Color(53, 70, 27); // Green
                    case 14: return new java.awt.Color(150, 52, 48); // Red
                    case 15: return new java.awt.Color(25, 22, 22); // Black
                    default: return new java.awt.Color(221, 221, 221);
                }
            default: return new java.awt.Color(255, 0, 255); // Unknown
        }
    }

    private java.awt.Color getHeightColor(int height) {
        if (height < 0) return new java.awt.Color(0, 0, 139); // Deep blue for void
        if (height > 255) height = 255;
        
        // Convert height to G and B channels for the height map format
        int g = (height >> 8) & 0xFF; // Upper 8 bits
        int b = height & 0xFF;        // Lower 8 bits
        
        return new java.awt.Color(0, g, b); // R channel is used for lighting
    }

    private ChunkData getChunkData(World world, int cx, int cz, Map<String, ChunkData> cache) {
        String key = cx + "," + cz;
        ChunkData data = cache.get(key);
        if (data != null) return data;

        try {
            Chunk chunk = world.getChunk(cx, cz);
            if (chunk == null) return null;

            data = new ChunkData(chunk);
            cache.put(key, data);
            return data;
        } catch (Exception e) {
            Logger.global.logError("Failed to get chunk data at " + cx + "," + cz, e);
            return null;
        }
    }

    private static class ChunkData {
        private final int[][][] blockIds;
        private final byte[][][] blockData;

        public ChunkData(Chunk chunk) {
            this.blockIds = new int[16][256][16];
            this.blockData = new byte[16][256][16];

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 256; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = chunk.getBlockState(x, y, z);
                        if (state != null && !state.isAir()) {
                            String blockId = state.getFormatted();
                            // Convert modern block ID to legacy numeric ID
                            int legacyId = getLegacyBlockId(blockId);
                            byte legacyData = getLegacyBlockData(blockId, state.getProperties());
                            
                            blockIds[x][y][z] = legacyId;
                            blockData[x][y][z] = legacyData;
                        }
                    }
                }
            }
        }

        private int getLegacyBlockId(String blockId) {
            // Convert modern block ID to legacy numeric ID for MC 1.5.2
            switch (blockId) {
                case "minecraft:air": return 0;
                case "minecraft:stone": return 1;
                case "minecraft:grass_block": return 2;
                case "minecraft:dirt": return 3;
                case "minecraft:cobblestone": return 4;
                case "minecraft:planks": return 5;
                case "minecraft:sapling": return 6;
                case "minecraft:bedrock": return 7;
                case "minecraft:water": return 8;
                case "minecraft:flowing_water": return 9;
                case "minecraft:lava": return 10;
                case "minecraft:flowing_lava": return 11;
                case "minecraft:sand": return 12;
                case "minecraft:gravel": return 13;
                case "minecraft:gold_ore": return 14;
                case "minecraft:iron_ore": return 15;
                case "minecraft:coal_ore": return 16;
                case "minecraft:log": return 17;
                case "minecraft:leaves": return 18;
                case "minecraft:sponge": return 19;
                case "minecraft:glass": return 20;
                case "minecraft:wool": return 35;
                case "minecraft:yellow_flower": return 37;
                case "minecraft:red_flower": return 38;
                case "minecraft:brown_mushroom": return 39;
                case "minecraft:red_mushroom": return 40;
                case "minecraft:gold_block": return 41;
                case "minecraft:iron_block": return 42;
                case "minecraft:stone_slab": return 44;
                case "minecraft:brick_block": return 45;
                case "minecraft:tnt": return 46;
                case "minecraft:bookshelf": return 47;
                case "minecraft:mossy_cobblestone": return 48;
                case "minecraft:obsidian": return 49;
                case "minecraft:torch": return 50;
                case "minecraft:fire": return 51;
                case "minecraft:mob_spawner": return 52;
                case "minecraft:oak_stairs": return 53;
                case "minecraft:chest": return 54;
                case "minecraft:diamond_ore": return 56;
                case "minecraft:diamond_block": return 57;
                case "minecraft:crafting_table": return 58;
                case "minecraft:farmland": return 60;
                case "minecraft:furnace": return 61;
                case "minecraft:lit_furnace": return 62;
                default: return 1; // Stone as default
            }
        }

        private byte getLegacyBlockData(String blockId, Map<String, String> properties) {
            if (properties == null) return 0;

            switch (blockId) {
                case "minecraft:log": {
                    String axis = properties.get("axis");
                    if ("x".equals(axis)) return 4;
                    if ("y".equals(axis)) return 0;
                    if ("z".equals(axis)) return 8;
                    return 0;
                }
                case "minecraft:water":
                case "minecraft:flowing_water": {
                    String level = properties.get("level");
                    if (level != null) {
                        try {
                            int waterLevel = Integer.parseInt(level);
                            return (byte) Math.min(7, waterLevel);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    }
                    return 0;
                }
                case "minecraft:lava":
                case "minecraft:flowing_lava": {
                    String level = properties.get("level");
                    if (level != null) {
                        try {
                            int lavaLevel = Integer.parseInt(level);
                            return (byte) Math.min(7, lavaLevel);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    }
                    return 0;
                }
                case "minecraft:wool": {
                    String color = properties.get("color");
                    if (color != null) {
                        switch (color) {
                            case "white": return 0;
                            case "orange": return 1;
                            case "magenta": return 2;
                            case "light_blue": return 3;
                            case "yellow": return 4;
                            case "lime": return 5;
                            case "pink": return 6;
                            case "gray": return 7;
                            case "light_gray": return 8;
                            case "cyan": return 9;
                            case "purple": return 10;
                            case "blue": return 11;
                            case "brown": return 12;
                            case "green": return 13;
                            case "red": return 14;
                            case "black": return 15;
                        }
                    }
                    return 0;
                }
                default: return 0;
            }
        }

        public int getBlockId(int x, int y, int z) {
            return blockIds[x][y][z];
        }

        public byte getBlockData(int x, int y, int z) {
            return blockData[x][y][z];
        }
    }

    private void initializeBlockColors() {
        // Basic blocks
        blockColorMap.put("0:0", new java.awt.Color(0, 0, 0, 0)); // AIR
        blockColorMap.put("1:0", new java.awt.Color(128, 128, 128)); // STONE
        blockColorMap.put("2:0", new java.awt.Color(89, 145, 50)); // GRASS
        blockColorMap.put("3:0", new java.awt.Color(150, 108, 74)); // DIRT
        blockColorMap.put("4:0", new java.awt.Color(122, 122, 122)); // COBBLESTONE
        
        // Wood types
        blockColorMap.put("5:0", new java.awt.Color(156, 127, 78)); // OAK_PLANKS
        blockColorMap.put("5:1", new java.awt.Color(103, 77, 46)); // SPRUCE_PLANKS
        blockColorMap.put("5:2", new java.awt.Color(193, 179, 123)); // BIRCH_PLANKS
        
        // Liquids
        blockColorMap.put("8:0", new java.awt.Color(59, 87, 191)); // WATER
        blockColorMap.put("9:0", new java.awt.Color(59, 87, 191)); // FLOWING_WATER
        blockColorMap.put("10:0", new java.awt.Color(207, 92, 20)); // LAVA
        blockColorMap.put("11:0", new java.awt.Color(207, 92, 20)); // FLOWING_LAVA
        
        // Sand and gravel
        blockColorMap.put("12:0", new java.awt.Color(219, 211, 160)); // SAND
        blockColorMap.put("12:1", new java.awt.Color(169, 88, 33)); // RED_SAND
        blockColorMap.put("13:0", new java.awt.Color(136, 126, 126)); // GRAVEL
        
        // Ores
        blockColorMap.put("14:0", new java.awt.Color(143, 140, 125)); // GOLD_ORE
        blockColorMap.put("15:0", new java.awt.Color(136, 130, 127)); // IRON_ORE
        
        // Logs
        blockColorMap.put("17:0", new java.awt.Color(102, 81, 51)); // OAK_LOG
        blockColorMap.put("17:1", new java.awt.Color(75, 60, 37)); // SPRUCE_LOG
        blockColorMap.put("17:4", new java.awt.Color(102, 81, 51)); // OAK_LOG_EAST
        blockColorMap.put("17:8", new java.awt.Color(102, 81, 51)); // OAK_LOG_NORTH
        
        // Wool colors
        blockColorMap.put("35:0", new java.awt.Color(234, 236, 236)); // WHITE_WOOL
        blockColorMap.put("35:1", new java.awt.Color(240, 118, 19)); // ORANGE_WOOL
        blockColorMap.put("35:2", new java.awt.Color(189, 68, 179)); // MAGENTA_WOOL
        blockColorMap.put("35:3", new java.awt.Color(58, 175, 217)); // LIGHT_BLUE_WOOL
        blockColorMap.put("35:4", new java.awt.Color(248, 198, 39)); // YELLOW_WOOL
        blockColorMap.put("35:5", new java.awt.Color(112, 185, 25)); // LIME_WOOL
        blockColorMap.put("35:6", new java.awt.Color(237, 141, 172)); // PINK_WOOL
        blockColorMap.put("35:7", new java.awt.Color(62, 68, 71)); // GRAY_WOOL
        blockColorMap.put("35:8", new java.awt.Color(142, 142, 134)); // LIGHT_GRAY_WOOL
        blockColorMap.put("35:9", new java.awt.Color(21, 137, 145)); // CYAN_WOOL
        blockColorMap.put("35:10", new java.awt.Color(121, 42, 172)); // PURPLE_WOOL
        blockColorMap.put("35:11", new java.awt.Color(53, 57, 157)); // BLUE_WOOL
        blockColorMap.put("35:12", new java.awt.Color(114, 71, 40)); // BROWN_WOOL
        blockColorMap.put("35:13", new java.awt.Color(84, 109, 27)); // GREEN_WOOL
        blockColorMap.put("35:14", new java.awt.Color(161, 39, 34)); // RED_WOOL
        blockColorMap.put("35:15", new java.awt.Color(20, 21, 25)); // BLACK_WOOL
    }
}