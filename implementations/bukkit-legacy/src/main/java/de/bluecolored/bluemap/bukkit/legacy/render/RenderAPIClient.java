package de.bluecolored.bluemap.bukkit.legacy.render;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RenderAPIClient {
    private static final String API_URL = "http://localhost:8200";
    private static final Gson GSON = new GsonBuilder().create();
    private final Logger logger;
    
    public RenderAPIClient(Logger logger) {
        this.logger = logger;
    }
    
    public void registerWorld(World world) {
        try {
            WorldConfig config = new WorldConfig();
            config.setName(world.getName());
            config.setDimension(getDimensionType(world.getEnvironment()));
            config.setMinY(0); // Legacy default
            config.setMaxY(256); // Legacy default
            config.setHires(true);
            
            RenderSettings renderSettings = new RenderSettings();
            renderSettings.setShadows(true);
            renderSettings.setAmbientOcclusion(true);
            renderSettings.setRenderDistance(8);
            renderSettings.setOrientation("north-east");
            renderSettings.setHeightmapResolution(32);
            config.setRenderSettings(renderSettings);
            
            String json = GSON.toJson(config);
            sendRequest("POST", "/world/" + world.getName(), json);
            
            logger.info("Registered world with render API: " + world.getName());
        } catch (Exception e) {
            logger.warning("Failed to register world with render API: " + e.getMessage());
        }
    }
    
    public void updateChunks(World world, List<org.bukkit.Chunk> chunks) {
        try {
            List<ChunkData> chunkDataList = new ArrayList<>();
            
            for (org.bukkit.Chunk chunk : chunks) {
                ChunkData chunkData = new ChunkData();
                chunkData.setX(chunk.getX());
                chunkData.setZ(chunk.getZ());
                chunkData.setGenerated(true);
                chunkData.setInhabitedTime(0L); // Default value for legacy versions
                
                // Collect block data
                Map<BlockPos, BlockData> blocks = new HashMap<>();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 256; y++) { // Legacy height range
                            Block block = chunk.getBlock(x, y, z);
                            if (!block.isEmpty()) {
                                BlockPos pos = new BlockPos(x, y, z);
                                BlockData blockData = new BlockData();
                                blockData.setBlockId(block.getType().name().toLowerCase());
                                blockData.setLight((byte) block.getLightLevel());
                                blocks.put(pos, blockData);
                            }
                        }
                    }
                }
                chunkData.setBlocks(blocks);
                
                chunkDataList.add(chunkData);
            }
            
            String json = GSON.toJson(chunkDataList);
            sendRequest("POST", "/world/" + world.getName() + "/chunks", json);
            
            logger.info("Sent " + chunks.size() + " chunks to render API for world: " + world.getName());
        } catch (Exception e) {
            logger.warning("Failed to send chunks to render API: " + e.getMessage());
        }
    }
    
    private void sendRequest(String method, String path, String body) throws IOException {
        URL url = new URL(API_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("API request failed with code: " + responseCode);
        }
    }
    
    private String getDimensionType(World.Environment env) {
        switch (env) {
            case NETHER:
                return "nether";
            case THE_END:
                return "the_end";
            default:
                return "overworld";
        }
    }
    
    private static class WorldConfig {
        private String name;
        private String dimension;
        private int minY;
        private int maxY;
        private boolean hires;
        private RenderSettings renderSettings;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDimension() { return dimension; }
        public void setDimension(String dimension) { this.dimension = dimension; }
        public int getMinY() { return minY; }
        public void setMinY(int minY) { this.minY = minY; }
        public int getMaxY() { return maxY; }
        public void setMaxY(int maxY) { this.maxY = maxY; }
        public boolean isHires() { return hires; }
        public void setHires(boolean hires) { this.hires = hires; }
        public RenderSettings getRenderSettings() { return renderSettings; }
        public void setRenderSettings(RenderSettings renderSettings) { this.renderSettings = renderSettings; }
    }
    
    private static class RenderSettings {
        private boolean shadows;
        private boolean ambientOcclusion;
        private int renderDistance;
        private String orientation;
        private int heightmapResolution;
        
        // Getters and setters
        public boolean isShadows() { return shadows; }
        public void setShadows(boolean shadows) { this.shadows = shadows; }
        public boolean isAmbientOcclusion() { return ambientOcclusion; }
        public void setAmbientOcclusion(boolean ambientOcclusion) { this.ambientOcclusion = ambientOcclusion; }
        public int getRenderDistance() { return renderDistance; }
        public void setRenderDistance(int renderDistance) { this.renderDistance = renderDistance; }
        public String getOrientation() { return orientation; }
        public void setOrientation(String orientation) { this.orientation = orientation; }
        public int getHeightmapResolution() { return heightmapResolution; }
        public void setHeightmapResolution(int heightmapResolution) { this.heightmapResolution = heightmapResolution; }
    }
    
    private static class ChunkData {
        private int x;
        private int z;
        private Map<BlockPos, BlockData> blocks;
        private long inhabitedTime;
        private boolean generated;
        
        // Getters and setters
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getZ() { return z; }
        public void setZ(int z) { this.z = z; }
        public Map<BlockPos, BlockData> getBlocks() { return blocks; }
        public void setBlocks(Map<BlockPos, BlockData> blocks) { this.blocks = blocks; }
        public long getInhabitedTime() { return inhabitedTime; }
        public void setInhabitedTime(long inhabitedTime) { this.inhabitedTime = inhabitedTime; }
        public boolean isGenerated() { return generated; }
        public void setGenerated(boolean generated) { this.generated = generated; }
    }
    
    private static class BlockPos {
        private final int x;
        private final int y;
        private final int z;
        
        public BlockPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public int getX() { return x; }
        public int getY() { return y; }
        public int getZ() { return z; }
    }
    
    private static class BlockData {
        private String blockId;
        private byte light;
        
        // Getters and setters
        public String getBlockId() { return blockId; }
        public void setBlockId(String blockId) { this.blockId = blockId; }
        public byte getLight() { return light; }
        public void setLight(byte light) { this.light = light; }
    }
} 