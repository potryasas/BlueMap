package de.bluecolored.bluemap.renderapi;

import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WorldRenderer {
    private static final Logger logger = LoggerFactory.getLogger(WorldRenderer.class);
    
    private final String worldId;
    private final WorldConfig config;
    private final Path webRoot;
    private final Map<Vector2i, ChunkData> chunks;
    private final Map<Vector2i, Long> chunkUpdateTimes;
    private final AtomicInteger pendingUpdates;
    
    public WorldRenderer(String worldId, WorldConfig config, Path webRoot) {
        this.worldId = worldId;
        this.config = config;
        this.webRoot = webRoot;
        this.chunks = new ConcurrentHashMap<>();
        this.chunkUpdateTimes = new ConcurrentHashMap<>();
        this.pendingUpdates = new AtomicInteger(0);
        
        // Create world directory structure
        createDirectories();
        
        logger.info("Initialized renderer for world: {} ({})", worldId, config.getName());
    }
    
    private void createDirectories() {
        try {
            Path worldDir = webRoot.resolve("maps").resolve(worldId);
            Path liveDir = worldDir.resolve("live");
            Path tilesDir = worldDir.resolve("tiles");
            
            worldDir.toFile().mkdirs();
            liveDir.toFile().mkdirs();
            tilesDir.toFile().mkdirs();
            
            logger.info("Created directory structure for world: {}", worldId);
        } catch (Exception e) {
            logger.error("Failed to create directories for world: " + worldId, e);
        }
    }
    
    public void updateChunks(ChunkData[] newChunks) {
        pendingUpdates.incrementAndGet();
        try {
            for (ChunkData chunk : newChunks) {
                Vector2i pos = new Vector2i(chunk.getX(), chunk.getZ());
                chunks.put(pos, chunk);
                chunkUpdateTimes.put(pos, System.currentTimeMillis());
            }
            
            // Start rendering tiles
            renderTiles(newChunks);
            
        } finally {
            pendingUpdates.decrementAndGet();
        }
    }
    
    private void renderTiles(ChunkData[] updatedChunks) {
        // Group chunks by region
        Map<Vector2i, TileRegion> regions = new ConcurrentHashMap<>();
        
        for (ChunkData chunk : updatedChunks) {
            Vector2i regionPos = new Vector2i(
                chunk.getX() >> 5,
                chunk.getZ() >> 5
            );
            
            regions.computeIfAbsent(regionPos, pos -> new TileRegion(pos, config))
                .addChunk(chunk);
        }
        
        // Render each region
        regions.values().forEach(region -> {
            try {
                region.render(webRoot.resolve("maps").resolve(worldId).resolve("tiles"));
                logger.info("Rendered region {} for world {}", region.getPosition(), worldId);
            } catch (Exception e) {
                logger.error("Failed to render region " + region.getPosition() + " for world " + worldId, e);
            }
        });
    }
    
    public RenderStatus getStatus() {
        RenderStatus status = new RenderStatus();
        status.setPendingUpdates(pendingUpdates.get());
        status.setTotalChunks(chunks.size());
        status.setLastUpdate(chunkUpdateTimes.values().stream().max(Long::compareTo).orElse(0L));
        return status;
    }
    
    public static class RenderStatus {
        private int pendingUpdates;
        private int totalChunks;
        private long lastUpdate;
        
        public int getPendingUpdates() {
            return pendingUpdates;
        }
        
        public void setPendingUpdates(int pendingUpdates) {
            this.pendingUpdates = pendingUpdates;
        }
        
        public int getTotalChunks() {
            return totalChunks;
        }
        
        public void setTotalChunks(int totalChunks) {
            this.totalChunks = totalChunks;
        }
        
        public long getLastUpdate() {
            return lastUpdate;
        }
        
        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }
} 