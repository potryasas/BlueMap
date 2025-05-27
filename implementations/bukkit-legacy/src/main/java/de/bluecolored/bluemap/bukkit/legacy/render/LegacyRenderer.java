package de.bluecolored.bluemap.bukkit.legacy.render;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.Chunk;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.Writer;

import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitPlugin;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.io.FileReader;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.ByteArrayInputStream;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.io.OutputStream;
import java.util.Arrays;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class LegacyRenderer {
    private static final int CHUNK_SIZE = 16;
    private static final int MAX_HEIGHT = 256;
    private static final int TILE_SIZE = 501;
    private static final int TILE_HEIGHT = 1002;
    private static final int MAP_HEIGHT = 501;
    private static final int MAX_ZOOM_LEVEL = 6;
    private static final int ASYNC_THREADS = 4;
    private static final int RENDER_DISTANCE = 8; // Chunks to render around each player
    private static final double ISOMETRIC_ANGLE = Math.PI / 6; // 30 degrees for isometric view
    
    private final LegacyBukkitPlugin plugin;
    private final ExecutorService executor;
    private final Path renderRoot;
    private final Queue<RenderTask> renderQueue;
    private final Map<String, float[]> blockColorMap;
    private final Set<ChunkCoord> renderedChunks;
    private boolean isRunning = true;
    private final Map<String, TextureData> textureAtlas;
    private final Map<String, Integer> texturePathToIdMap;
    private final List<TextureData> orderedTextures;
    private final ArrayTileModelLegacy tileModelCache;
    private int notFoundLogCount = 0;

    // New inner class to store texture data
    public static class TextureData {
        String resourcePath;
        float[] color;
        boolean halfTransparent;
        String texture; // Base64 encoded PNG

        // transient field for decoded image, to avoid re-decoding every time
        transient BufferedImage image;
    }
    
    public LegacyRenderer(LegacyBukkitPlugin plugin) {
        this.plugin = plugin;
        this.executor = Executors.newFixedThreadPool(ASYNC_THREADS);
        this.renderRoot = plugin.getWebRoot().resolve("maps");
        this.renderQueue = new ConcurrentLinkedQueue<>();
        this.renderedChunks = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.blockColorMap = new HashMap<>();
        this.textureAtlas = new LinkedHashMap<>();
        this.texturePathToIdMap = new HashMap<>();
        this.orderedTextures = new ArrayList<>();
        this.tileModelCache = new ArrayTileModelLegacy();
        
        plugin.getLogger().info("[BlueMap] Initializing LegacyRenderer");
        plugin.getLogger().info("[BlueMap] Web root path: " + plugin.getWebRoot());
        plugin.getLogger().info("[BlueMap] Render root path: " + renderRoot);
        
        try {
            Files.createDirectories(renderRoot);
            plugin.getLogger().info("[BlueMap] Created render root directory");
        } catch (IOException e) {
            plugin.getLogger().severe("[BlueMap] Failed to create render root directory: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Используем только output2.json
        loadTextureAtlas();
        
        plugin.getLogger().info("[BlueMap] LegacyRenderer initialized");
        startRenderQueueProcessor();
    }
    
    private void startRenderQueueProcessor() {
        plugin.getLogger().info("[BlueMap] Starting render queue processor");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isRunning) {
                    plugin.getLogger().info("[BlueMap] Render queue processor is not running");
                    return;
                }
                if (renderQueue.isEmpty()) {
                    return;
                }

                RenderTask task = renderQueue.poll();
                if (task != null) {
                    // Сбор chunk-данных только в основном потоке
                    final int chunkX = task.chunkX;
                    final int chunkZ = task.chunkZ;
                    final World world = task.world;
                    
                    plugin.getLogger().info(String.format("[BlueMap] Processing chunk at %d,%d in world %s", chunkX, chunkZ, world.getName()));
                    
                    if (!world.isChunkLoaded(chunkX, chunkZ)) {
                        plugin.getLogger().info("[BlueMap] Loading chunk " + chunkX + "," + chunkZ);
                        world.loadChunk(chunkX, chunkZ);
                    }
                    
                    if (!world.isChunkLoaded(chunkX, chunkZ)) {
                        plugin.getLogger().warning("[BlueMap] Skipping chunk at " + chunkX + "," + chunkZ + ": not loaded after sync load!");
                        return;
                    }
                    
                    final Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                    final Vector3i min = new Vector3i(chunk.getX() * 16, 0, chunk.getZ() * 16);
                    final Vector3i max = new Vector3i((chunk.getX() + 1) * 16, MAX_HEIGHT, (chunk.getZ() + 1) * 16);

                    final String worldName = world.getName();
                    final Path worldDir = renderRoot.resolve(worldName);
                    try {
                        Files.createDirectories(worldDir);
                        plugin.getLogger().info("[BlueMap] Created/verified world directory: " + worldDir);
                    } catch (IOException e) {
                        plugin.getLogger().warning("[BlueMap] Failed to create world dir: " + worldDir + ", " + e.getMessage());
                        return;
                    }

                    // Сохраняем все нужные чанки для тайла
                    final Map<String, Chunk> chunkMap = new HashMap<>();
                    int minChunkX = min.getX() >> 4;
                    int maxChunkX = (max.getX() - 1) >> 4;
                    int minChunkZ = min.getZ() >> 4;
                    int maxChunkZ = (max.getZ() - 1) >> 4;
                    
                    plugin.getLogger().info(String.format("[BlueMap] Loading chunks from %d,%d to %d,%d", minChunkX, minChunkZ, maxChunkX, maxChunkZ));
                    
                    boolean allChunksLoaded = true;
                    for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                        for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                            if (!world.isChunkLoaded(cx, cz)) {
                                plugin.getLogger().info("[BlueMap] Loading chunk " + cx + "," + cz);
                                world.loadChunk(cx, cz);
                            }
                            if (!world.isChunkLoaded(cx, cz)) {
                                plugin.getLogger().warning("[BlueMap] Skipping tile: chunk " + cx + "," + cz + " not loaded!");
                                allChunksLoaded = false;
                            } else {
                                Chunk loadedChunk = world.getChunkAt(cx, cz);
                                chunkMap.put(cx + "," + cz, loadedChunk);
                                plugin.getLogger().info("[BlueMap] Added chunk " + cx + "," + cz + " to render queue");
                            }
                        }
                    }
                    
                    if (!allChunksLoaded) {
                        plugin.getLogger().warning("[BlueMap] Skipping tile due to missing chunks");
                        return;
                    }

                    // Передаем chunkMap в асинхронный поток для рендера
                    plugin.getLogger().info("[BlueMap] Starting async render for chunk " + chunkX + "," + chunkZ);
                    try {
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    plugin.getLogger().info("[BlueMap] Beginning render for chunk " + chunkX + "," + chunkZ);
                                    renderLowresTileWithChunks(world, min, max, worldDir, chunkMap, 1);
                                    plugin.getLogger().info("[BlueMap] Completed render for chunk " + chunkX + "," + chunkZ);
                                } catch (Exception e) {
                                    plugin.getLogger().severe("[BlueMap] Error processing chunk at " + chunkX + "," + chunkZ + ": " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        plugin.getLogger().severe("[BlueMap] Failed to submit render task: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
        plugin.getLogger().info("[BlueMap] Render queue processor started");
    }

    private void generateTexturesJson(Path texturesFile) throws IOException {
        JsonArray texturesArray = new JsonArray();
        
        // Add missing texture as first element
        JsonObject missingTexture = new JsonObject();
        missingTexture.addProperty("resourcePath", "bluemap:missing");
        JsonArray missingColor = new JsonArray();
        missingColor.add(0.5);
        missingColor.add(0.0);
        missingColor.add(0.5);
        missingColor.add(1.0);
        missingTexture.add("color", missingColor);
        missingTexture.addProperty("halfTransparent", false);
        missingTexture.addProperty("texture", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAPklEQVR4Xu3MsQkAMAwDQe2/tFPnBB4gpLhG8MpkZpNkZ6AKZKAKZKAKZKAKZKAKZKAKZKAKWg0XD/UPnjg4MbX+EDdeTUwAAAAASUVORK5CYII=");
        texturesArray.add(missingTexture);

        // Add block textures
        for (Map.Entry<String, float[]> entry : blockColorMap.entrySet()) {
            JsonObject texture = new JsonObject();
            texture.addProperty("resourcePath", "minecraft:block/" + entry.getKey().replace(':', '/'));
            
            float[] color = entry.getValue();
            JsonArray colorArray = new JsonArray();
            colorArray.add(color[0]);
            colorArray.add(color[1]);
            colorArray.add(color[2]);
            colorArray.add(color[3]);
            texture.add("color", colorArray);
            
            texture.addProperty("halfTransparent", color[3] < 1.0f);
            texture.addProperty("texture", generateTextureBase64(color));
            
            texturesArray.add(texture);
        }

        try (Writer writer = Files.newBufferedWriter(texturesFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(texturesArray, writer);
        }
    }

    private String generateTextureBase64(float[] color) {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(color[0], color[1], color[2], color[3]));
        g2d.fillRect(0, 0, 16, 16);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "PNG", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to generate texture: " + e.getMessage());
            return "";
        }
    }

    private static class ChunkCoord {
        final World world;
        final int x;
        final int z;

        ChunkCoord(World world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkCoord that = (ChunkCoord) o;
            return x == that.x && z == that.z && world.equals(that.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, x, z);
        }
    }
    
    public void renderWorld(World world, int centerX, int centerZ, int radius) {
        // Only render if explicitly requested via command
        if (!isRunning) return;
        
        try {
            String worldName = world.getName();
            Path worldDir = renderRoot.resolve(worldName);
            Files.createDirectories(worldDir);
            
            int minChunkX = (centerX - radius) >> 4;
            int maxChunkX = (centerX + radius) >> 4;
            int minChunkZ = (centerZ - radius) >> 4;
            int maxChunkZ = (centerZ + radius) >> 4;
            
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                    ChunkCoord coord = new ChunkCoord(world, chunkX, chunkZ);
                    if (!renderedChunks.contains(coord)) {
                        renderQueue.offer(new RenderTask(world, chunkX, chunkZ));
                        renderedChunks.add(coord);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error queueing world render " + world.getName() + ": " + e.getMessage());
        }
    }
    
    public void renderChunk(World world, Chunk chunk) {
        try {
            renderQueue.offer(new RenderTask(world, chunk.getX(), chunk.getZ()));
        } catch (Exception e) {
            plugin.getLogger().warning("Error queueing chunk at " + chunk.getX() + "," + chunk.getZ() + ": " + e.getMessage());
        }
    }

    private void renderTileIntegrated(World world, Vector3i modelMin, Vector3i modelMax, Path worldDir, int zoomLevel) throws IOException {
        if (zoomLevel == 0) {
            renderHiresTile(world, modelMin, modelMax, worldDir);
        } else {
            renderLowresTile(world, modelMin, modelMax, worldDir, zoomLevel);
        }
    }

    private void renderLowresTile(World world, Vector3i modelMin, Vector3i modelMax, Path worldDir, int zoomLevel) throws IOException {
        Path tilePath = null;
        try {
            plugin.getLogger().info("[BlueMap] Starting renderLowresTile: world=" + world.getName() + 
                ", zoom=" + zoomLevel + ", min=" + modelMin + ", max=" + modelMax);

            int tileX = Math.floorDiv(modelMin.getX(), CHUNK_SIZE * (1 << zoomLevel));
            int tileZ = Math.floorDiv(modelMin.getZ(), CHUNK_SIZE * (1 << zoomLevel));
            int tileOriginX = tileX * CHUNK_SIZE * (1 << zoomLevel);
            int tileOriginZ = tileZ * CHUNK_SIZE * (1 << zoomLevel);

            plugin.getLogger().info("[BlueMap] Tile coordinates: tileX=" + tileX + ", tileZ=" + tileZ + 
                ", originX=" + tileOriginX + ", originZ=" + tileOriginZ);

            BufferedImage tileImage = new BufferedImage(TILE_SIZE, TILE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = tileImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int scale = 1 << zoomLevel;
            int startX = modelMin.getX();
            int startZ = modelMin.getZ();
            double scaleX = (double) TILE_SIZE / ((modelMax.getX() - startX) * scale);
            double scaleZ = (double) MAP_HEIGHT / ((modelMax.getZ() - startZ) * scale);

            plugin.getLogger().info("[BlueMap] Rendering parameters: scale=" + scale + 
                ", scaleX=" + scaleX + ", scaleZ=" + scaleZ);

            Map<String, ChunkData> chunkCache = new HashMap<>();
            int[][] heightMap = new int[TILE_SIZE][MAP_HEIGHT];
            float[][] lightMap = new float[TILE_SIZE][MAP_HEIGHT];
            int renderedPixels = 0;
            int logCount = 0;

            // First pass: Calculate heightmap and light map
            plugin.getLogger().info("[BlueMap] Calculating heightmap...");
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int z = 0; z < MAP_HEIGHT; z++) {
                    int worldX = startX + (int)(x / scaleX);
                    int worldZ = startZ + (int)(z / scaleZ);
                    heightMap[x][z] = getTopBlockYCached(world, worldX, worldZ, chunkCache);
                    
                    // Calculate lighting based on height differences
                    if (x > 0 && z > 0) {
                        int heightDiff = Math.max(
                            Math.abs(heightMap[x][z] - heightMap[x-1][z]),
                            Math.abs(heightMap[x][z] - heightMap[x][z-1])
                        );
                        lightMap[x][z] = 1.0f - (heightDiff * 0.1f);
                        lightMap[x][z] = Math.max(0.4f, Math.min(1.0f, lightMap[x][z]));
                    } else {
                        lightMap[x][z] = 0.8f;
                    }
                }
            }

            // Second pass: Render blocks using heightmap
            plugin.getLogger().info("[BlueMap] Rendering blocks...");
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int z = 0; z < MAP_HEIGHT; z++) {
                    int worldX = startX + (int)(x / scaleX);
                    int worldZ = startZ + (int)(z / scaleZ);
                    int height = heightMap[x][z];
                    
                    if (height >= 0) {
                        int chunkX = worldX >> 4;
                        int chunkZ = worldZ >> 4;
                        int localX = worldX & 15;
                        int localZ = worldZ & 15;
                        ChunkData chunkData = getChunkData(world, chunkX, chunkZ, chunkCache);
                        
                        if (chunkData != null) {
                            Material material = chunkData.getBlockType(localX, height, localZ);
                            if (material != Material.AIR) {
                                byte dataValue = chunkData.getBlockData(localX, height, localZ);
                                ResolvedBlockTexture resolvedTexture = getResolvedBlockTexture(material, dataValue);
                                float light = lightMap[x][z];

                                if (logCount < 10) {
                                    String atlasKey = "Material." + material.name();
                                    String resourcePath = (resolvedTexture.textureData != null) ? resolvedTexture.textureData.resourcePath : "null";
                                    String colorStr = (resolvedTexture.textureData != null && resolvedTexture.textureData.color != null) ?
                                        String.format("[%.3f, %.3f, %.3f, %.3f]", resolvedTexture.textureData.color[0], resolvedTexture.textureData.color[1], resolvedTexture.textureData.color[2], resolvedTexture.textureData.color[3]) : "null";
                                    int texLen = (resolvedTexture.textureData != null && resolvedTexture.textureData.texture != null) ? resolvedTexture.textureData.texture.length() : 0;
                                    plugin.getLogger().info(String.format("[BlueMap] PIXEL: worldX=%d, worldZ=%d, height=%d, Material=%s, data=%d, atlasKey=%s, resourcePath=%s, color=%s, texLen=%d", 
                                        worldX, worldZ, height, material.name(), dataValue, atlasKey, resourcePath, colorStr, texLen));
                                    logCount++;
                                }

                                if (resolvedTexture.textureData != null) {
                                    if (resolvedTexture.textureData.texture != null && !resolvedTexture.textureData.texture.isEmpty()) {
                                        BufferedImage blockImage = getDecodedImage(resolvedTexture.textureData);
                                        if (blockImage != null) {
                                            RescaleOp rescaleOp = new RescaleOp(new float[]{light, light, light, 1f}, new float[]{0, 0, 0, 0}, null);
                                            BufferedImage lightedImage = rescaleOp.filter(blockImage, null);
                                            g2d.drawImage(lightedImage, x, z, 1, 1, null);
                                            renderedPixels++;
                                        } else {
                                            drawSolidBlock(g2d, x, z, resolvedTexture.textureData.color, light);
                                            renderedPixels++;
                                        }
                                    } else if (resolvedTexture.textureData.color != null) {
                                        drawSolidBlock(g2d, x, z, resolvedTexture.textureData.color, light);
                                        renderedPixels++;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Third pass: Generate height map image
            plugin.getLogger().info("[BlueMap] Generating height map...");
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int z = 0; z < MAP_HEIGHT; z++) {
                    int height = heightMap[x][z];
                    if (height < 0) height = 0;
                    if (height > 255) height = 255;
                    Color heightColor = new Color(0, 0, height);
                    tileImage.setRGB(x, MAP_HEIGHT + z, heightColor.getRGB());
                }
            }

            g2d.dispose();

            // Save the tile
            Path tilesDir = worldDir.resolve("tiles").resolve(String.valueOf(zoomLevel));
            Path xDir = tilesDir.resolve("x" + tileX);
            Files.createDirectories(xDir);
            tilePath = xDir.resolve("z" + tileZ + ".png");

            plugin.getLogger().info("[BlueMap] Saving tile to: " + tilePath + 
                " (rendered " + renderedPixels + " pixels)");

            try {
                ImageIO.write(tileImage, "PNG", tilePath.toFile());
                plugin.getLogger().info("[BlueMap] Successfully saved tile: " + tilePath);
            } catch (IOException e) {
                plugin.getLogger().severe("[BlueMap] Failed to save tile: " + tilePath + " - " + e.getMessage());
                throw e;
            }

        } catch (Exception e) {
            plugin.getLogger().severe("[BlueMap] Error rendering tile: " + 
                (tilePath != null ? tilePath.toString() : "unknown") + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void renderHiresTile(World world, Vector3i initialChunkModelMin, Vector3i initialChunkModelMax, Path worldDir) throws IOException {
        tileModelCache.clear(); // Reuse the model object
        final int HIRESTILE_BLOCK_WIDTH = 32; 
        int tileX = (int) Math.floor((double) initialChunkModelMin.getX() / HIRESTILE_BLOCK_WIDTH);
        int tileZ = (int) Math.floor((double) initialChunkModelMin.getZ() / HIRESTILE_BLOCK_WIDTH);
        int tileOriginX = tileX * HIRESTILE_BLOCK_WIDTH;
        int tileOriginZ = tileZ * HIRESTILE_BLOCK_WIDTH;
        plugin.getLogger().info("[BlueMap] HIRES Tile info: tileX=" + tileX + ", tileZ=" + tileZ + ", tileOriginX=" + tileOriginX + ", tileOriginZ=" + tileOriginZ + ", min=" + initialChunkModelMin + ", max=" + initialChunkModelMax);
        int logCount = 0;
        for (int worldCoordX = tileOriginX; worldCoordX < tileOriginX + HIRESTILE_BLOCK_WIDTH; worldCoordX++) {
            for (int worldCoordZ = tileOriginZ; worldCoordZ < tileOriginZ + HIRESTILE_BLOCK_WIDTH; worldCoordZ++) {
                int currentChunkX = worldCoordX >> 4;
                int currentChunkZ = worldCoordZ >> 4;
                if (!world.isChunkLoaded(currentChunkX, currentChunkZ)) {
                    world.loadChunk(currentChunkX, currentChunkZ, false);
                    if(!world.isChunkLoaded(currentChunkX, currentChunkZ)){
                         continue;
                    }
                }
                Chunk bukkitChunk = world.getChunkAt(currentChunkX, currentChunkZ);
                int localX = worldCoordX & 15;
                int localZ = worldCoordZ & 15;
                for (int y = 0; y < MAX_HEIGHT; y++) {
                    Material material = bukkitChunk.getBlock(localX, y, localZ).getType();
                    if (material == Material.AIR) {
                        continue;
                    }
                    byte data = bukkitChunk.getBlock(localX, y, localZ).getData();
                    ResolvedBlockTexture resolvedTexture = getResolvedBlockTexture(material, data);
                    if (logCount < 10) {
                        String atlasKey = "Material." + material.name();
                        String resourcePath = (resolvedTexture.textureData != null) ? resolvedTexture.textureData.resourcePath : "null";
                        String colorStr = (resolvedTexture.textureData != null && resolvedTexture.textureData.color != null) ?
                            String.format("[%.3f, %.3f, %.3f, %.3f]", resolvedTexture.textureData.color[0], resolvedTexture.textureData.color[1], resolvedTexture.textureData.color[2], resolvedTexture.textureData.color[3]) : "null";
                        int texLen = (resolvedTexture.textureData != null && resolvedTexture.textureData.texture != null) ? resolvedTexture.textureData.texture.length() : 0;
                        plugin.getLogger().info(String.format("[BlueMap] HIRES BLOCK: worldX=%d, worldY=%d, worldZ=%d, Material=%s, data=%d, atlasKey=%s, resourcePath=%s, color=%s, texLen=%d", worldCoordX, y, worldCoordZ, material.name(), data, atlasKey, resourcePath, colorStr, texLen));
                        logCount++;
                    }
                    byte sunlightVal = 15; 
                    byte blocklightVal = bukkitChunk.getBlock(localX,y,localZ).getLightFromBlocks();
                    boolean renderFaceTop = isFaceVisible(world, worldCoordX, y + 1, worldCoordZ, bukkitChunk, localX, y, localZ);
                    boolean renderFaceBottom = isFaceVisible(world, worldCoordX, y - 1, worldCoordZ, bukkitChunk, localX, y, localZ);
                    boolean renderFaceNorth = isFaceVisible(world, worldCoordX, y, worldCoordZ - 1, bukkitChunk, localX, y, localZ);
                    boolean renderFaceSouth = isFaceVisible(world, worldCoordX, y, worldCoordZ + 1, bukkitChunk, localX, y, localZ);
                    boolean renderFaceWest = isFaceVisible(world, worldCoordX - 1, y, worldCoordZ, bukkitChunk, localX, y, localZ);
                    boolean renderFaceEast = isFaceVisible(world, worldCoordX + 1, y, worldCoordZ, bukkitChunk, localX, y, localZ);
                    if (renderFaceTop || renderFaceBottom || renderFaceNorth || renderFaceSouth || renderFaceWest || renderFaceEast) {
                        addBlockToModel(tileModelCache, worldCoordX, y, worldCoordZ, 
                                        tileOriginX, tileOriginZ, 
                                        resolvedTexture, sunlightVal, blocklightVal,
                                        renderFaceTop, renderFaceBottom, renderFaceNorth, renderFaceSouth, renderFaceWest, renderFaceEast);
                    }
                }
            }
        }
        Path tilesDir = worldDir.resolve("tiles").resolve("0");
        Path xDir = tilesDir.resolve("x" + tileX);
        Files.createDirectories(xDir);
        Path tilePath = xDir.resolve("z" + tileZ + ".prbm");
        try (OutputStream fos = Files.newOutputStream(tilePath);
             PRBMWriterLegacy writer = new PRBMWriterLegacy(fos)) {
            writer.write(tileModelCache);
            plugin.getLogger().info("Saved PRBM tile: " + tilePath);
        } catch (IOException e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to write PRBM tile: " + tilePath, e);
            throw e;
        }
    }

    // Helper to add a standard cube model for a block
    private void addBlockToModel(ArrayTileModelLegacy model, 
                                 int worldX, int worldY, int worldZ, 
                                 int tileOriginX, int tileOriginZ, 
                                 ResolvedBlockTexture resolvedTexture, 
                                 byte sunlight, byte blocklight,
                                 boolean renderTop, boolean renderBottom, 
                                 boolean renderNorth, boolean renderSouth, 
                                 boolean renderWest, boolean renderEast) {

        if (resolvedTexture == null || resolvedTexture.textureData == null) return;
        int textureId = resolvedTexture.textureId;
        float relX = (float) (worldX - tileOriginX);
        float y = worldY; 
        float relZ = (float) (worldZ - tileOriginZ);

        // Cube vertices (counter-clockwise when viewed from outside)
        //    v7-----v6
        //   /|    /|
        //  v3----v2 |
        //  | v4---|-v5
        //  |/    |/
        //  v0----v1

        float[][] v = {
            {relX,     y,     relZ    }, // v0 (front-bottom-left)
            {relX + 1, y,     relZ    }, // v1 (front-bottom-right)
            {relX + 1, y + 1, relZ    }, // v2 (front-top-right)
            {relX,     y + 1, relZ    }, // v3 (front-top-left)
            {relX,     y,     relZ + 1}, // v4 (back-bottom-left)
            {relX + 1, y,     relZ + 1}, // v5 (back-bottom-right)
            {relX + 1, y + 1, relZ + 1}, // v6 (back-top-right)
            {relX,     y + 1, relZ + 1}  // v7 (back-top-left)
        };
        
        // UV Coordinates (top-left, top-right, bottom-right, bottom-left for a quad)
        float[] uvTL = {0f, 1f};
        float[] uvTR = {1f, 1f};
        float[] uvBR = {1f, 0f};
        float[] uvBL = {0f, 0f};

        // Top face (+Y) (v7, v6, v2, v3)
        if (renderTop) {
            addFaceToModel(model, v[7], v[6], v[2], uvTL, uvTR, uvBR, textureId, sunlight, blocklight);
            addFaceToModel(model, v[7], v[2], v[3], uvTL, uvBR, uvBL, textureId, sunlight, blocklight);
        }

        // Bottom face (-Y) (v0, v1, v5, v4) - viewed from below, so wind opposite or invert UVs vertically.
        // Standard winding for consistency, renderer handles culling if needed.
        if (renderBottom) {
            addFaceToModel(model, v[0], v[1], v[5], uvBL, uvBR, uvTR, textureId, sunlight, blocklight); // UVs mapped for viewing from top.
            addFaceToModel(model, v[0], v[5], v[4], uvBL, uvTR, uvTL, textureId, sunlight, blocklight); // If texture has orientation, might need V flip.
        }

        // North face (-Z) (v3, v2, v1, v0)
        if (renderNorth) {
            addFaceToModel(model, v[3], v[2], v[1], uvTL, uvTR, uvBR, textureId, sunlight, blocklight);
            addFaceToModel(model, v[3], v[1], v[0], uvTL, uvBR, uvBL, textureId, sunlight, blocklight);
        }

        // South face (+Z) (v7, v4, v5, v6) - viewed from front, v4 is FBL for this face
        if (renderSouth) {
            addFaceToModel(model, v[7], v[4], v[5], uvTR, uvTL, uvBL, textureId, sunlight, blocklight); // UVs TL,TR,BR,BL from this face's perspective.
            addFaceToModel(model, v[7], v[5], v[6], uvTR, uvBL, uvBR, textureId, sunlight, blocklight); // V[7] (top-left on this face), V4 (bottom-left), V5 (bottom-right), V6 (top-right)
        }

        // West face (-X) (v7, v3, v0, v4)
        if (renderWest) {
            addFaceToModel(model, v[7], v[3], v[0], uvTR, uvTL, uvBL, textureId, sunlight, blocklight); // v7(TR), v3(TL), v0(BL), v4(BR)
            addFaceToModel(model, v[7], v[0], v[4], uvTR, uvBL, uvBR, textureId, sunlight, blocklight);
        }

        // East face (+X) (v2, v6, v5, v1)
        if (renderEast) {
            addFaceToModel(model, v[2], v[6], v[5], uvTL, uvTR, uvBR, textureId, sunlight, blocklight); // v2(TL), v6(TR), v5(BR), v1(BL)
            addFaceToModel(model, v[2], v[5], v[1], uvTL, uvBR, uvBL, textureId, sunlight, blocklight);
        }
    }

    // Helper to add one triangle face to the model
    private void addFaceToModel(ArrayTileModelLegacy model, float[] p1, float[] p2, float[] p3, 
                                float[] uv1, float[] uv2, float[] uv3, // Added UV params per vertex
                                int textureId, byte sunlight, byte blocklight) {
        model.addFace(
            p1[0], p1[1], p1[2], uv1[0], uv1[1],
            p2[0], p2[1], p2[2], uv2[0], uv2[1],
            p3[0], p3[1], p3[2], uv3[0], uv3[1],
            textureId, 
            1f, 1f, 1f, // Color (white)
            1f, 1f, 1f, // AO (none) - Placeholder, real AO is complex
            sunlight, blocklight
        );
    }

    // Helper to determine if a face should be rendered (neighbor is air or transparent)
    private boolean isFaceVisible(World world, int neighborX, int neighborY, int neighborZ, 
                                  Chunk currentChunk, int currentBlockLocalX_unused, int currentBlockLocalY_unused, int currentBlockLocalZ_unused) { // Renamed unused params
        if (neighborY < 0 || neighborY >= MAX_HEIGHT) return false; 

        Material neighborMaterial;
        int neighborChunkX = neighborX >> 4;
        int neighborChunkZ = neighborZ >> 4;

        if (neighborChunkX == currentChunk.getX() && neighborChunkZ == currentChunk.getZ()) {
            int nLocalX = neighborX & 15;
            int nLocalZ = neighborZ & 15;
            // No need to check nLocalY because neighborY is already checked against MAX_HEIGHT
            neighborMaterial = currentChunk.getBlock(nLocalX, neighborY, nLocalZ).getType();
        } else {
            if (!world.isChunkLoaded(neighborChunkX, neighborChunkZ)) {
                 world.loadChunk(neighborChunkX, neighborChunkZ, false); // Attempt to load it
                 if(!world.isChunkLoaded(neighborChunkX, neighborChunkZ)) return true; // Assume visible if cannot load
            }
            neighborMaterial = world.getBlockAt(neighborX, neighborY, neighborZ).getType();
        }

        if (neighborMaterial == Material.AIR) return true;

        byte neighborData = world.getBlockAt(neighborX, neighborY, neighborZ).getData(); 
        ResolvedBlockTexture neighborResolvedTexture = getResolvedBlockTexture(neighborMaterial, neighborData);
        
        if (neighborResolvedTexture != null && neighborResolvedTexture.textureData != null) {
            if (neighborResolvedTexture.textureData.halfTransparent) return true;
            if (neighborResolvedTexture.textureData.color != null && neighborResolvedTexture.textureData.color[3] < 1.0f) return true; // Alpha < 1
            // Consider if the texture itself is see-through (e.g. glass texture). 
            // This simple check only uses halfTransparent flag and color alpha.
        }
        
        // Add specific transparent block types from Minecraft if not covered by TextureData
        switch(neighborMaterial){
            case GLASS:
            case THIN_GLASS: // Stained Glass Pane
            // case WATER: // Already handled by AIR if water is considered AIR for culling against solids
            // case STATIONARY_WATER:
            case LEAVES:
                return true;
            default:
                return false;
        }
    }

    // Cache structure for chunk data
    private static class ChunkData {
        private final Material[][][] blocks;
        private final byte[][][] data;
        private final int chunkX, chunkZ;
        
        public ChunkData(Chunk chunk) {
            this.chunkX = chunk.getX();
            this.chunkZ = chunk.getZ();
            this.blocks = new Material[16][256][16];
            this.data = new byte[16][256][16];
            
            // Cache all block data from this chunk
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 256; y++) {
                    for (int z = 0; z < 16; z++) {
                        Block block = chunk.getBlock(x, y, z);
                        blocks[x][y][z] = block.getType();
                        data[x][y][z] = block.getData();
                    }
                }
            }
        }
        
        public Material getBlockType(int x, int y, int z) {
            if (x < 0 || x >= 16 || y < 0 || y >= 256 || z < 0 || z >= 16) {
                return Material.AIR;
            }
            return blocks[x][y][z];
        }
        
        public byte getBlockData(int x, int y, int z) {
            if (x < 0 || x >= 16 || y < 0 || y >= 256 || z < 0 || z >= 16) {
                return 0;
            }
            return data[x][y][z];
        }
    }

    private ChunkData getChunkData(World world, int chunkX, int chunkZ, Map<String, ChunkData> cache) {
        String key = chunkX + "," + chunkZ;
        ChunkData cached = cache.get(key);
        if (cached == null) {
            Chunk chunk = world.getChunkAt(chunkX, chunkZ);
            cached = new ChunkData(chunk);
            cache.put(key, cached);
        }
        return cached;
    }

    private Color getTopBlockColorCached(World world, int x, int z, Map<String, ChunkData> cache) {
        int y = getTopBlockYCached(world, x, z, cache);
        if (y < 0) return new Color(0, 0, 139); // Deep blue for void
        
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int localX = x & 15;
        int localZ = z & 15;
        
        ChunkData chunkData = getChunkData(world, chunkX, chunkZ, cache);
        if (chunkData == null) return new Color(255, 0, 255); // Magenta for error
        
        Material material = chunkData.getBlockType(localX, y, localZ);
        byte data = chunkData.getBlockData(localX, y, localZ);
        
        Color baseColor = getBlockColor(material, data);
        
        // Add some variation based on height for better visual depth
        float heightFactor = Math.max(0.5f, Math.min(1.5f, y / 64.0f));
        return new Color(
            Math.min(255, (int)(baseColor.getRed() * heightFactor)),
            Math.min(255, (int)(baseColor.getGreen() * heightFactor)),
            Math.min(255, (int)(baseColor.getBlue() * heightFactor))
        );
    }

    private int getTopBlockYCached(World world, int x, int z, Map<String, ChunkData> cache) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int localX = x & 15;
        int localZ = z & 15;
        
        ChunkData chunkData = getChunkData(world, chunkX, chunkZ, cache);
        if (chunkData == null) return -1;
        
        // Find the highest non-air block
        for (int y = 255; y >= 0; y--) {
            Material material = chunkData.getBlockType(localX, y, localZ);
            if (material != Material.AIR) {
                return y;
            }
        }
        return -1;
    }

    private Color getBlockColor(Material material, byte data) {
        // Get block type and data
        String blockKey = getMaterialKey(material);
        String atlasKey = "Material." + material.name();
        
        // Handle variants by potentially modifying blockKey or atlasKey
        switch(material) {
            case WOOD:
            case WOOD_STEP:
                String woodType;
                String woodPrefix = (material == Material.WOOD ? "planks_" : "slab_");
                switch(data & 0x3) {
                    case 0: woodType = "oak"; break;
                    case 1: woodType = "spruce"; break;
                    case 2: woodType = "birch"; break;
                    case 3: woodType = "jungle"; break;
                    default: woodType = "oak";
                }
                blockKey = "minecraft:" + woodPrefix + woodType;
                atlasKey = "Material." + woodType.toUpperCase() + (material == Material.WOOD ? "_PLANKS" : "_SLAB");
                break;
                
            case LOG:
                String logType;
                switch(data & 0x3) {
                    case 0: logType = "oak"; break;
                    case 1: logType = "spruce"; break;
                    case 2: logType = "birch"; break;
                    case 3: logType = "jungle"; break;
                    default: logType = "oak";
                }
                blockKey = "minecraft:log_" + logType;
                atlasKey = "Material." + logType.toUpperCase() + "_LOG";
                break;
                
            case LEAVES:
                String leafType;
                switch(data & 0x3) {
                    case 0: leafType = "oak"; break;
                    case 1: leafType = "spruce"; break;
                    case 2: leafType = "birch"; break;
                    case 3: leafType = "jungle"; break;
                    default: leafType = "oak";
                }
                blockKey = "minecraft:leaves_" + leafType;
                atlasKey = "Material." + leafType.toUpperCase() + "_LEAVES";
                break;
                
            case WOOL:
                String woolColorName;
                switch(data & 0xF) {
                    case 0: woolColorName = "white"; break;
                    case 1: woolColorName = "orange"; break;
                    case 2: woolColorName = "magenta"; break;
                    case 3: woolColorName = "light_blue"; break;
                    case 4: woolColorName = "yellow"; break;
                    case 5: woolColorName = "lime"; break;
                    case 6: woolColorName = "pink"; break;
                    case 7: woolColorName = "gray"; break;
                    case 8: woolColorName = "light_gray"; break;
                    case 9: woolColorName = "cyan"; break;
                    case 10: woolColorName = "purple"; break;
                    case 11: woolColorName = "blue"; break;
                    case 12: woolColorName = "brown"; break;
                    case 13: woolColorName = "green"; break;
                    case 14: woolColorName = "red"; break;
                    case 15: woolColorName = "black"; break;
                    default: woolColorName = "white";
                }
                blockKey = "minecraft:wool_" + woolColorName;
                atlasKey = "Material." + woolColorName.toUpperCase() + "_WOOL";
                break;
                
            case STEP:
                // Stone slab types
                String slabType;
                switch(data & 0x7) {
                    case 0: slabType = "stone"; break;
                    case 1: slabType = "sandstone"; break;
                    case 3: slabType = "cobblestone"; break;
                    case 4: slabType = "brick"; break;
                    case 5: slabType = "stone_brick"; break;
                    case 6: slabType = "nether_brick"; break;
                    case 7: slabType = "quartz"; break;
                    default: slabType = "stone";
                }
                blockKey = "minecraft:" + slabType + "_slab";
                atlasKey = "Material." + slabType.toUpperCase() + "_SLAB";
                break;
                
            case WATER:
            case STATIONARY_WATER:
                blockKey = "minecraft:water";
                break;
                
            case LAVA:
            case STATIONARY_LAVA:
                blockKey = "minecraft:lava";
                break;
                
            case LONG_GRASS:
                blockKey = "minecraft:tall_grass";
                break;
                
            case VINE:
                blockKey = "minecraft:vine";
                break;
                
            case SNOW:
            case SNOW_BLOCK:
                blockKey = "minecraft:snow";
                break;
                
            case WEB:
                blockKey = "minecraft:web";
                break;
                
            case SUGAR_CANE_BLOCK:
                blockKey = "minecraft:sugar_cane";
                break;
                
            case DEAD_BUSH:
                blockKey = "minecraft:dead_bush";
                break;
                
            default:
                // Convert Material name to minecraft:block_name format
                blockKey = "minecraft:" + material.name().toLowerCase();
        }
        
        // Attempt to get TextureData from the atlas first
        TextureData textureData = textureAtlas.get(atlasKey);
        if (textureData == null && !atlasKey.equals("Material." + material.name())) {
            // Fallback for variants if specific variant key not found, try base material
            textureData = textureAtlas.get("Material." + material.name());
        }
        
        // If no specific texture data, try with the more general blockKey (minecraft:...)
        // This is more for robustness if atlas keys are inconsistent.
        // The primary lookup should be via atlasKey derived from Material + data.
        if (textureData == null) {
             // Attempt to find a match in textureAtlas using the "minecraft:block_name" style key
            // This requires output2.json to potentially have keys like "minecraft:stone"
            // or a mapping logic. For now, this might not hit if output2.json strictly uses "Material.XXX".
            String mcKey = blockKey.startsWith("minecraft:") ? blockKey.substring("minecraft:".length()).toUpperCase() : blockKey.toUpperCase();
            textureData = textureAtlas.get("Material." + mcKey);
            if(textureData == null) textureData = textureAtlas.get(blockKey); // Direct check if blockKey matches an atlas key
        }

        float[] colorArray = null;
        if (textureData != null && textureData.color != null && textureData.color.length == 4) {
            colorArray = textureData.color;
        } else {
            // Fallback to blockColorMap if texture or its color is not found
            colorArray = blockColorMap.get(blockKey);
            if (colorArray == null) {
                // Fallback for base material if variant-specific key not found
                colorArray = blockColorMap.get(getMaterialKey(material)); 
            }
        }
        
        if (colorArray == null) {
            // Last resort: missing texture color
            TextureData missingTextureData = textureAtlas.get("Material.AIR"); // Or a dedicated missing texture entry
            if (missingTextureData != null && missingTextureData.color != null) {
                colorArray = missingTextureData.color;
            } else {
                 colorArray = blockColorMap.get("bluemap:missing"); // From original code
                 if (colorArray == null) {
                    colorArray = new float[]{0.7f, 0.7f, 0.7f, 1.0f}; // Default fallback
                 }
            }
            plugin.getLogger().warning("Missing color/texture for block: " + material.name() + " (data: " + data + "), resolved key: " + blockKey + ", atlasKey: " + atlasKey);
        }
        
        return new Color(colorArray[0], colorArray[1], colorArray[2], colorArray[3]);
    }

    private Color getHeightColor(int height) {
        if (height < 0) return Color.BLACK;
        
        // Create a more interesting height map with color gradients
        if (height < 32) {
            // Deep areas - dark blue to blue
            float factor = height / 32.0f;
            return new Color(0, 0, (int)(128 + 127 * factor));
        } else if (height < 64) {
            // Water level - blue to green
            float factor = (height - 32) / 32.0f;
            return new Color(0, (int)(255 * factor), (int)(255 - 127 * factor));
        } else if (height < 128) {
            // Land - green to yellow
            float factor = (height - 64) / 64.0f;
            return new Color((int)(255 * factor), 255, 0);
        } else if (height < 192) {
            // Hills - yellow to orange
            float factor = (height - 128) / 64.0f;
            return new Color(255, (int)(255 - 128 * factor), 0);
        } else {
            // Mountains - orange to white
            float factor = (height - 192) / 64.0f;
            int value = (int)(127 + 128 * factor);
            return new Color(255, value, value);
        }
    }

    private void loadTextureAtlas() {
        Path atlasPath = plugin.getDataFolder().toPath().resolve("output2.json");
        plugin.getLogger().info("[BlueMap] Attempting to load texture atlas from: " + atlasPath);
        plugin.getLogger().info("[BlueMap] Data folder path: " + plugin.getDataFolder().getAbsolutePath());
        
        if (!Files.exists(atlasPath)) {
            plugin.getLogger().severe("[BlueMap] Texture atlas file not found at: " + atlasPath);
            // Попробуем найти файл в других местах
            Path altPath = Paths.get("output2.json");
            if (Files.exists(altPath)) {
                plugin.getLogger().info("[BlueMap] Found texture atlas in alternate location: " + altPath);
                atlasPath = altPath;
            } else {
                plugin.getLogger().severe("[BlueMap] Could not find texture atlas file in alternate location either");
                return;
            }
        }

        try {
            plugin.getLogger().info("[BlueMap] Reading texture atlas file...");
            String content = new String(Files.readAllBytes(atlasPath), StandardCharsets.UTF_8);
            plugin.getLogger().info("[BlueMap] File size: " + content.length() + " bytes");
            
            Type listType = new TypeToken<List<TextureData>>(){}.getType();
            List<TextureData> atlasEntriesFromJson = new Gson().fromJson(content, listType);
            
            if (atlasEntriesFromJson == null) {
                plugin.getLogger().severe("[BlueMap] Failed to parse texture atlas - null result");
                return;
            }
            
            plugin.getLogger().info("[BlueMap] Successfully parsed " + atlasEntriesFromJson.size() + " texture entries from JSON");
            
            int currentId = 0;
            // Ensure "bluemap:missing" is first
            TextureData missingTex = null;
            for (TextureData entry : atlasEntriesFromJson) {
                if ("bluemap:missing".equals(entry.resourcePath)) {
                    missingTex = entry;
                    break;
                }
            }
            
            if (missingTex == null) {
                plugin.getLogger().info("[BlueMap] Creating default missing texture");
                missingTex = new TextureData();
                missingTex.resourcePath = "bluemap:missing";
                missingTex.color = new float[]{0.5f, 0.0f, 0.5f, 1.0f};
                missingTex.halfTransparent = false;
                missingTex.texture = "";
            } else {
                plugin.getLogger().info("[BlueMap] Found existing missing texture");
                atlasEntriesFromJson.remove(missingTex);
            }

            // Add missing texture first
            orderedTextures.add(missingTex);
            textureAtlas.put(missingTex.resourcePath, missingTex);
            texturePathToIdMap.put(missingTex.resourcePath, currentId++);
            plugin.getLogger().info("[BlueMap] Added missing texture with ID 0");

            int texturesWithColor = 0;
            int texturesWithImage = 0;

            // Add all other textures
            for (TextureData entry : atlasEntriesFromJson) {
                if (entry.resourcePath == null) {
                    plugin.getLogger().warning("[BlueMap] Skipping texture entry with null resourcePath");
                    continue;
                }
                
                if (textureAtlas.containsKey(entry.resourcePath)) {
                    plugin.getLogger().warning("[BlueMap] Duplicate texture entry: " + entry.resourcePath);
                    continue;
                }

                orderedTextures.add(entry);
                textureAtlas.put(entry.resourcePath, entry);
                texturePathToIdMap.put(entry.resourcePath, currentId++);
                
                if (entry.color != null && entry.color.length == 4) {
                    texturesWithColor++;
                    String materialName = entry.resourcePath.startsWith("Material.") ? 
                        entry.resourcePath.substring("Material.".length()) : entry.resourcePath;
                    blockColorMap.put("minecraft:" + materialName.toLowerCase(), entry.color);
                }
                
                if (entry.texture != null && !entry.texture.isEmpty()) {
                    texturesWithImage++;
                }
            }
            
            plugin.getLogger().info(String.format("[BlueMap] Texture atlas loaded successfully:" +
                "\n - Total entries: %d" +
                "\n - Textures with color: %d" +
                "\n - Textures with images: %d" +
                "\n - Block color map size: %d",
                orderedTextures.size(), texturesWithColor, texturesWithImage, blockColorMap.size()));
            
            // Debug output for first few entries
            int debugLimit = Math.min(5, textureAtlas.size());
            plugin.getLogger().info("[BlueMap] First " + debugLimit + " texture entries:");
            List<String> keys = new ArrayList<>(textureAtlas.keySet());
            for (int i = 0; i < debugLimit; i++) {
                String key = keys.get(i);
                TextureData data = textureAtlas.get(key);
                plugin.getLogger().info(String.format(" - %s: color=%s, hasTexture=%b", 
                    key,
                    data.color != null ? Arrays.toString(data.color) : "null",
                    data.texture != null && !data.texture.isEmpty()));
            }
            
        } catch (IOException e) {
            plugin.getLogger().severe("[BlueMap] Failed to read texture atlas file: " + e.getMessage());
            e.printStackTrace();
        } catch (JsonParseException e) {
            plugin.getLogger().severe("[BlueMap] Failed to parse texture atlas JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            plugin.getLogger().severe("[BlueMap] Unexpected error loading texture atlas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    private static class RenderTask {
        final World world;
        final int chunkX;
        final int chunkZ;
        
        RenderTask(World world, int chunkX, int chunkZ) {
            this.world = world;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        World getWorld() {
            return world;
        }

        int getChunkX() {
            return chunkX;
        }

        int getChunkZ() {
            return chunkZ;
        }
    }

    public void render() {
        if (!isRunning) return;

        // Only render if explicitly called
        if (renderQueue.isEmpty()) return;

        RenderTask task = renderQueue.poll();
        if (task == null) return;

        World world = task.getWorld();
        int chunkX = task.getChunkX();
        int chunkZ = task.getChunkZ();

        // ... rest of the render method ...
    }

    public void queueChunk(World world, int chunkX, int chunkZ) {
        if (!isRunning) return;
        renderQueue.offer(new RenderTask(world, chunkX, chunkZ));
    }

    public void queueChunksAround(Location location, int radius) {
        if (!isRunning) return;
        World world = location.getWorld();
        int centerChunkX = location.getBlockX() >> 4;
        int centerChunkZ = location.getBlockZ() >> 4;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                queueChunk(world, centerChunkX + x, centerChunkZ + z);
            }
        }
    }

    private Color darken(Color color, float factor) {
        return new Color(
            Math.max((int)(color.getRed() * factor), 0),
            Math.max((int)(color.getGreen() * factor), 0),
            Math.max((int)(color.getBlue() * factor), 0),
            color.getAlpha()
        );
    }

    private String getMaterialKey(Material material) {
        String name = material.name().toLowerCase();
        
        // Convert legacy material names to modern format
        switch (material) {
            case STATIONARY_WATER:
                return "minecraft:water";
            case STATIONARY_LAVA:
                return "minecraft:lava";
            case LONG_GRASS:
                return "minecraft:tall_grass";
            case SUGAR_CANE_BLOCK:
                return "minecraft:sugar_cane";
            case WOOD:
                return "minecraft:planks";
            case LOG:
                return "minecraft:log";
            case LEAVES:
                return "minecraft:leaves";
            case STEP:
                return "minecraft:stone_slab";
            case WOOD_STEP:
                return "minecraft:wooden_slab";
            case WORKBENCH:
                return "minecraft:crafting_table";
            case SOIL:
                return "minecraft:farmland";
            case BURNING_FURNACE:
                return "minecraft:lit_furnace";
            case SIGN_POST:
                return "minecraft:standing_sign";
            case WOODEN_DOOR:
                return "minecraft:wooden_door";
            case RAILS:
                return "minecraft:rail";
            case COBBLESTONE_STAIRS:
                return "minecraft:stone_stairs";
            case WALL_SIGN:
                return "minecraft:wall_sign";
            case STONE_PLATE:
                return "minecraft:stone_pressure_plate";
            case WOOD_PLATE:
                return "minecraft:wooden_pressure_plate";
            case IRON_DOOR_BLOCK:
                return "minecraft:iron_door";
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
                return "minecraft:redstone_torch";
            case GLOWING_REDSTONE_ORE:
                return "minecraft:lit_redstone_ore";
            case SNOW:
            case SNOW_BLOCK:
                return "minecraft:snow";
            case JACK_O_LANTERN:
                return "minecraft:jack_o_lantern";
            case WOOD_STAIRS:
                return "minecraft:oak_stairs";
            case PISTON_STICKY_BASE:
                return "minecraft:sticky_piston";
            case RED_ROSE:
                return "minecraft:red_flower";
            case WATER_LILY:
                return "minecraft:lily_pad";
            case SMOOTH_BRICK:
                return "minecraft:stone_bricks";
            case SKULL:
                return "minecraft:skull";
            default:
                return "minecraft:" + name;
        }
    }

    // New helper method to draw a solid colored block with lighting
    private void drawSolidBlock(Graphics2D g2d, int x, int z, float[] baseColorComponents, float light) {
        if (baseColorComponents == null || baseColorComponents.length < 4) {
            baseColorComponents = new float[]{0.7f, 0.0f, 0.7f, 1.0f}; // Magenta fallback
        }
        
        // Create a small 1x1 ARGB image for the block
        BufferedImage blockImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D blockG2d = blockImage.createGraphics();
        
        Color baseColor = new Color(
            Math.min(1.0f, Math.max(0.0f, baseColorComponents[0])), 
            Math.min(1.0f, Math.max(0.0f, baseColorComponents[1])),
            Math.min(1.0f, Math.max(0.0f, baseColorComponents[2])),
            Math.min(1.0f, Math.max(0.0f, baseColorComponents[3]))
        );
        
        blockG2d.setColor(baseColor);
        blockG2d.fillRect(0, 0, 1, 1);
        blockG2d.dispose();
        
        // Apply lighting using RescaleOp
        RescaleOp rescaleOp = new RescaleOp(
            new float[]{light, light, light, 1f},
            new float[]{0, 0, 0, 0},
            null
        );
        
        BufferedImage lightedImage = rescaleOp.filter(blockImage, null);
        g2d.drawImage(lightedImage, x, z, 1, 1, null);
    }

    // New method to decode base64 texture and cache it
    private BufferedImage getDecodedImage(TextureData textureData) {
        if (textureData.image != null) {
            return textureData.image;
        }
        if (textureData.texture == null || textureData.texture.isEmpty()) {
            return null;
        }
        try {
            String base64Data = textureData.texture;
            String prefix = "data:image/png;base64,";
            if (base64Data.startsWith(prefix)) {
                base64Data = base64Data.substring(prefix.length());
            }
            
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(bais);

            if (originalImage != null) {
                // Always convert to TYPE_INT_ARGB to ensure compatibility with RescaleOp
                BufferedImage convertedImage = new BufferedImage(
                    originalImage.getWidth(), 
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
                );
                
                // Use Graphics2D for high quality conversion
                Graphics2D g2d = convertedImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw the original image onto the new ARGB image
                g2d.drawImage(originalImage, 0, 0, null);
                g2d.dispose();
                
                // Cache the converted image
                textureData.image = convertedImage;
                return convertedImage;
            }

            return null;
        } catch (IOException | IllegalArgumentException e) {
            plugin.getLogger().warning("Failed to decode texture for " + textureData.resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    // New method to encapsulate texture/color lookup logic (replaces direct getBlockColor in render loop)
    // Now also needs to provide the texture ID
    private static class ResolvedBlockTexture {
        TextureData textureData;
        int textureId;

        ResolvedBlockTexture(TextureData textureData, int textureId) {
            this.textureData = textureData;
            this.textureId = textureId;
        }
    }

    private ResolvedBlockTexture getResolvedBlockTexture(Material material, byte data) {
        String atlasKey = "Material." + material.name();
        String blockKey = getMaterialKey(material);
        List<String> triedKeys = new ArrayList<>();

        // Специальная обработка для воды и лавы
        if (material == Material.WATER || material == Material.STATIONARY_WATER) {
            atlasKey = "Material.WATER";
            blockKey = "minecraft:water";
        } else if (material == Material.LAVA || material == Material.STATIONARY_LAVA) {
            atlasKey = "Material.LAVA";
            blockKey = "minecraft:lava";
        } else {
            // Handle variants by potentially modifying atlasKey
            switch(material) {
                case WOOD:
                case WOOD_STEP:
                    String woodType;
                    switch(data & 0x3) { 
                        case 0: woodType = "OAK"; break;
                        case 1: woodType = "SPRUCE"; break;
                        case 2: woodType = "BIRCH"; break;
                        case 3: woodType = "JUNGLE"; break;
                        default: woodType = "OAK";
                    }
                    String woodKeyPart = woodType + (material == Material.WOOD ? "_PLANKS" : "_SLAB");
                    atlasKey = "Material." + woodKeyPart;
                    blockKey = "minecraft:" + woodKeyPart.toLowerCase();
                    break;

                case LOG:
                    String logType;
                    switch(data & 0x3) { 
                        case 0: logType = "OAK"; break;
                        case 1: logType = "SPRUCE"; break;
                        case 2: logType = "BIRCH"; break;
                        case 3: logType = "JUNGLE"; break;
                        default: logType = "OAK";
                    }
                    atlasKey = "Material." + logType + "_LOG";
                    blockKey = "minecraft:" + logType.toLowerCase() + "_log";
                    break;

                case LEAVES:
                    String leafType;
                    switch(data & 0x3) { 
                        case 0: leafType = "OAK"; break;
                        case 1: leafType = "SPRUCE"; break;
                        case 2: leafType = "BIRCH"; break;
                        case 3: leafType = "JUNGLE"; break;
                        default: leafType = "OAK";
                    }
                    atlasKey = "Material." + leafType + "_LEAVES";
                    blockKey = "minecraft:" + leafType.toLowerCase() + "_leaves";
                    break;

                case WOOL:
                    String woolColorName;
                    switch(data & 0xF) { 
                        case 0: woolColorName = "WHITE"; break;
                        case 1: woolColorName = "ORANGE"; break;
                        case 2: woolColorName = "MAGENTA"; break;
                        case 3: woolColorName = "LIGHT_BLUE"; break;
                        case 4: woolColorName = "YELLOW"; break;
                        case 5: woolColorName = "LIME"; break;
                        case 6: woolColorName = "PINK"; break;
                        case 7: woolColorName = "GRAY"; break;
                        case 8: woolColorName = "LIGHT_GRAY"; break;
                        case 9: woolColorName = "CYAN"; break;
                        case 10: woolColorName = "PURPLE"; break;
                        case 11: woolColorName = "BLUE"; break;
                        case 12: woolColorName = "BROWN"; break;
                        case 13: woolColorName = "GREEN"; break;
                        case 14: woolColorName = "RED"; break;
                        case 15: woolColorName = "BLACK"; break;
                        default: woolColorName = "WHITE";
                    }
                    atlasKey = "Material." + woolColorName + "_WOOL";
                    blockKey = "minecraft:" + woolColorName.toLowerCase() + "_wool";
                    break;

                case GRASS:
                    atlasKey = "Material.GRASS_BLOCK";
                    blockKey = "minecraft:grass_block";
                    break;

                case DIRT:
                    if ((data & 0x1) == 1) {
                        atlasKey = "Material.COARSE_DIRT";
                        blockKey = "minecraft:coarse_dirt";
                    } else {
                        atlasKey = "Material.DIRT";
                        blockKey = "minecraft:dirt";
                    }
                    break;

                case STONE:
                    switch(data) {
                        case 1: 
                            atlasKey = "Material.GRANITE";
                            blockKey = "minecraft:granite";
                            break;
                        case 2:
                            atlasKey = "Material.POLISHED_GRANITE";
                            blockKey = "minecraft:polished_granite";
                            break;
                        case 3:
                            atlasKey = "Material.DIORITE";
                            blockKey = "minecraft:diorite";
                            break;
                        case 4:
                            atlasKey = "Material.POLISHED_DIORITE";
                            blockKey = "minecraft:polished_diorite";
                            break;
                        case 5:
                            atlasKey = "Material.ANDESITE";
                            blockKey = "minecraft:andesite";
                            break;
                        case 6:
                            atlasKey = "Material.POLISHED_ANDESITE";
                            blockKey = "minecraft:polished_andesite";
                            break;
                        default:
                            atlasKey = "Material.STONE";
                            blockKey = "minecraft:stone";
                    }
                    break;
            }
        }

        // Попытка найти текстуру
        TextureData textureData = null;
        Integer textureId = null;

        // 1. Попробовать точное совпадение по atlasKey
        if (textureAtlas.containsKey(atlasKey)) {
            textureData = textureAtlas.get(atlasKey);
            textureId = texturePathToIdMap.get(atlasKey);
            if (textureData != null) {
                return new ResolvedBlockTexture(textureData, textureId != null ? textureId : 0);
            }
        }

        // 2. Попробовать blockKey
        if (textureAtlas.containsKey(blockKey)) {
            textureData = textureAtlas.get(blockKey);
            textureId = texturePathToIdMap.get(blockKey);
            if (textureData != null) {
                return new ResolvedBlockTexture(textureData, textureId != null ? textureId : 0);
            }
        }

        // 3. Попробовать базовое имя материала
        String baseMaterialKey = "Material." + material.name();
        if (!baseMaterialKey.equals(atlasKey) && textureAtlas.containsKey(baseMaterialKey)) {
            textureData = textureAtlas.get(baseMaterialKey);
            textureId = texturePathToIdMap.get(baseMaterialKey);
            if (textureData != null) {
                return new ResolvedBlockTexture(textureData, textureId != null ? textureId : 0);
            }
        }

        // Если текстура не найдена, используем missing texture
        if (textureAtlas.containsKey("bluemap:missing")) {
            textureData = textureAtlas.get("bluemap:missing");
            textureId = texturePathToIdMap.get("bluemap:missing");
        }

        if (textureData == null) {
            textureData = new TextureData();
            textureData.resourcePath = "bluemap:missing";
            textureData.color = new float[]{0.7f, 0.0f, 0.7f, 1.0f};
            textureData.halfTransparent = false;
            textureData.texture = "";
            textureId = 0;
        }

        return new ResolvedBlockTexture(textureData, textureId != null ? textureId : 0);
    }

    // Новый метод: рендер lowres tile с уже загруженными чанками
    private void renderLowresTileWithChunks(World world, Vector3i modelMin, Vector3i modelMax, Path worldDir, Map<String, Chunk> chunkMap, int zoomLevel) throws IOException {
        Path tilePath = null;
        try {
            plugin.getLogger().info("[BlueMap] Starting renderLowresTileWithChunks: world=" + world.getName() + 
                ", zoom=" + zoomLevel + ", chunks=" + chunkMap.size());

            int tileX = Math.floorDiv(modelMin.getX(), CHUNK_SIZE * (1 << zoomLevel));
            int tileZ = Math.floorDiv(modelMin.getZ(), CHUNK_SIZE * (1 << zoomLevel));
            int tileOriginX = tileX * CHUNK_SIZE * (1 << zoomLevel);
            int tileOriginZ = tileZ * CHUNK_SIZE * (1 << zoomLevel);

            plugin.getLogger().info(String.format("[BlueMap] Tile coordinates: tileX=%d, tileZ=%d, originX=%d, originZ=%d", 
                tileX, tileZ, tileOriginX, tileOriginZ));

            BufferedImage tileImage = new BufferedImage(TILE_SIZE, TILE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = tileImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Fill background with sky color
            g2d.setColor(new Color(0.529f, 0.808f, 0.922f, 1.0f)); // Sky blue
            g2d.fillRect(0, 0, TILE_SIZE, MAP_HEIGHT);

            int scale = 1 << zoomLevel;
            int startX = modelMin.getX();
            int startZ = modelMin.getZ();
            double scaleX = (double) TILE_SIZE / ((modelMax.getX() - startX) * scale);
            double scaleZ = (double) MAP_HEIGHT / ((modelMax.getZ() - startZ) * scale);

            Map<String, ChunkData> chunkDataCache = new HashMap<>();
            for (Map.Entry<String, Chunk> entry : chunkMap.entrySet()) {
                chunkDataCache.put(entry.getKey(), new ChunkData(entry.getValue()));
            }

            int[][] heightMap = new int[TILE_SIZE][MAP_HEIGHT];
            float[][] lightMap = new float[TILE_SIZE][MAP_HEIGHT];
            int renderedPixels = 0;
            int processedBlocks = 0;
            int skippedBlocks = 0;

            // Calculate heightmap and lighting
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int z = 0; z < MAP_HEIGHT; z++) {
                    int worldX = startX + (int)(x / scaleX);
                    int worldZ = startZ + (int)(z / scaleZ);
                    heightMap[x][z] = getTopBlockYCached(world, worldX, worldZ, chunkDataCache);
                    processedBlocks++;
                    
                    if (x > 0 && z > 0) {
                        int heightDiff = Math.max(
                            Math.abs(heightMap[x][z] - heightMap[x-1][z]),
                            Math.abs(heightMap[x][z] - heightMap[x][z-1])
                        );
                        lightMap[x][z] = 1.0f - (heightDiff * 0.05f); // Уменьшил влияние высоты на освещение
                        lightMap[x][z] = Math.max(0.6f, Math.min(1.0f, lightMap[x][z])); // Увеличил минимальную яркость
                    } else {
                        lightMap[x][z] = 0.9f; // Увеличил базовую яркость
                    }
                }
            }

            // Render blocks
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int z = 0; z < MAP_HEIGHT; z++) {
                    int worldX = startX + (int)(x / scaleX);
                    int worldZ = startZ + (int)(z / scaleZ);
                    int height = heightMap[x][z];
                    processedBlocks++;
                    
                    if (height >= 0) {
                        int chunkX = worldX >> 4;
                        int chunkZ = worldZ >> 4;
                        int localX = worldX & 15;
                        int localZ = worldZ & 15;
                        ChunkData chunkData = getChunkData(world, chunkX, chunkZ, chunkDataCache);
                        
                        if (chunkData != null) {
                            Material material = chunkData.getBlockType(localX, height, localZ);
                            if (material != Material.AIR) {
                                byte dataValue = chunkData.getBlockData(localX, height, localZ);
                                ResolvedBlockTexture resolvedTexture = getResolvedBlockTexture(material, dataValue);
                                float light = lightMap[x][z];

                                if (resolvedTexture.textureData != null) {
                                    // Debug logging для первых нескольких блоков
                                    if (renderedPixels < 5) {
                                        plugin.getLogger().info(String.format("[BlueMap] Rendering block at x=%d,z=%d: material=%s, data=%d, light=%.2f", 
                                            x, z, material.name(), dataValue, light));
                                    }

                                    if (resolvedTexture.textureData.texture != null && !resolvedTexture.textureData.texture.isEmpty()) {
                                        BufferedImage blockImage = getDecodedImage(resolvedTexture.textureData);
                                        if (blockImage != null) {
                                            try {
                                                RescaleOp rescaleOp = new RescaleOp(
                                                    new float[]{light, light, light, 1f},
                                                    new float[]{0, 0, 0, 0},
                                                    null
                                                );
                                                BufferedImage lightedImage = rescaleOp.filter(blockImage, null);
                                                g2d.drawImage(lightedImage, x, z, 1, 1, null);
                                                renderedPixels++;
                                            } catch (Exception e) {
                                                plugin.getLogger().warning(String.format("[BlueMap] Failed to apply lighting to block at x=%d,z=%d: %s", 
                                                    x, z, e.getMessage()));
                                                // Fallback to direct color rendering
                                                drawSolidBlock(g2d, x, z, resolvedTexture.textureData.color, light);
                                            }
                                        } else {
                                            drawSolidBlock(g2d, x, z, resolvedTexture.textureData.color, light);
                                            renderedPixels++;
                                        }
                                    } else if (resolvedTexture.textureData.color != null) {
                                        drawSolidBlock(g2d, x, z, resolvedTexture.textureData.color, light);
                                        renderedPixels++;
                                    } else {
                                        skippedBlocks++;
                                        if (skippedBlocks < 5) {
                                            plugin.getLogger().warning(String.format("[BlueMap] No texture/color for block: %s (data: %d)", 
                                                material.name(), dataValue));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Generate height map
            for (int x = 0; x < TILE_SIZE; x++) {
                for (int z = 0; z < MAP_HEIGHT; z++) {
                    int height = heightMap[x][z];
                    if (height < 0) height = 0;
                    if (height > 255) height = 255;
                    
                    // Используем градиент для карты высот
                    Color heightColor;
                    if (height < 64) { // Вода и низины
                        float factor = height / 64.0f;
                        heightColor = new Color(0, 0, (int)(128 + 127 * factor));
                    } else if (height < 128) { // Равнины
                        float factor = (height - 64) / 64.0f;
                        heightColor = new Color(0, (int)(255 * factor), 255);
                    } else { // Горы
                        float factor = (height - 128) / 127.0f;
                        heightColor = new Color((int)(255 * factor), 255, (int)(255 * (1 - factor)));
                    }
                    
                    tileImage.setRGB(x, MAP_HEIGHT + z, heightColor.getRGB());
                }
            }

            g2d.dispose();

            // Save tile
            Path tilesDir = worldDir.resolve("tiles").resolve(String.valueOf(zoomLevel));
            Path xDir = tilesDir.resolve("x" + tileX);
            Files.createDirectories(xDir);
            tilePath = xDir.resolve("z" + tileZ + ".png");

            ImageIO.write(tileImage, "PNG", tilePath.toFile());
            plugin.getLogger().info(String.format("[BlueMap] Successfully saved tile: %s (rendered: %d, skipped: %d)", 
                tilePath, renderedPixels, skippedBlocks));

        } catch (Exception e) {
            plugin.getLogger().severe("[BlueMap] Error rendering tile: " + 
                (tilePath != null ? tilePath.toString() : "unknown") + " - " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
} 