package de.bluecolored.bluemap.renderapi;

import org.joml.Vector3i;

import java.util.Map;

public class ChunkData {
    private int x;
    private int z;
    private Map<Vector3i, BlockData> blocks;
    private byte[] heightmap;
    private byte[] biomes;
    private byte[] lightmap;
    private long inhabitedTime;
    private boolean generated;
    
    public static class BlockData {
        private String blockId;
        private Map<String, String> properties;
        private byte light;
        
        public String getBlockId() {
            return blockId;
        }
        
        public void setBlockId(String blockId) {
            this.blockId = blockId;
        }
        
        public Map<String, String> getProperties() {
            return properties;
        }
        
        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }
        
        public byte getLight() {
            return light;
        }
        
        public void setLight(byte light) {
            this.light = light;
        }
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getZ() {
        return z;
    }
    
    public void setZ(int z) {
        this.z = z;
    }
    
    public Map<Vector3i, BlockData> getBlocks() {
        return blocks;
    }
    
    public void setBlocks(Map<Vector3i, BlockData> blocks) {
        this.blocks = blocks;
    }
    
    public byte[] getHeightmap() {
        return heightmap;
    }
    
    public void setHeightmap(byte[] heightmap) {
        this.heightmap = heightmap;
    }
    
    public byte[] getBiomes() {
        return biomes;
    }
    
    public void setBiomes(byte[] biomes) {
        this.biomes = biomes;
    }
    
    public byte[] getLightmap() {
        return lightmap;
    }
    
    public void setLightmap(byte[] lightmap) {
        this.lightmap = lightmap;
    }
    
    public long getInhabitedTime() {
        return inhabitedTime;
    }
    
    public void setInhabitedTime(long inhabitedTime) {
        this.inhabitedTime = inhabitedTime;
    }
    
    public boolean isGenerated() {
        return generated;
    }
    
    public void setGenerated(boolean generated) {
        this.generated = generated;
    }
} 