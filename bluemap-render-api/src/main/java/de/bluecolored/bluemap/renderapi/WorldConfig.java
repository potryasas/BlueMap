package de.bluecolored.bluemap.renderapi;

public class WorldConfig {
    private String name;
    private String dimension;
    private int minY;
    private int maxY;
    private boolean hires;
    private RenderSettings renderSettings;
    
    public static class RenderSettings {
        private boolean shadows = true;
        private boolean ambientOcclusion = true;
        private int renderDistance = 8;
        private String orientation = "north-east";
        private int heightmapResolution = 32;
        
        public boolean isShadows() {
            return shadows;
        }
        
        public void setShadows(boolean shadows) {
            this.shadows = shadows;
        }
        
        public boolean isAmbientOcclusion() {
            return ambientOcclusion;
        }
        
        public void setAmbientOcclusion(boolean ambientOcclusion) {
            this.ambientOcclusion = ambientOcclusion;
        }
        
        public int getRenderDistance() {
            return renderDistance;
        }
        
        public void setRenderDistance(int renderDistance) {
            this.renderDistance = renderDistance;
        }
        
        public String getOrientation() {
            return orientation;
        }
        
        public void setOrientation(String orientation) {
            this.orientation = orientation;
        }
        
        public int getHeightmapResolution() {
            return heightmapResolution;
        }
        
        public void setHeightmapResolution(int heightmapResolution) {
            this.heightmapResolution = heightmapResolution;
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDimension() {
        return dimension;
    }
    
    public void setDimension(String dimension) {
        this.dimension = dimension;
    }
    
    public int getMinY() {
        return minY;
    }
    
    public void setMinY(int minY) {
        this.minY = minY;
    }
    
    public int getMaxY() {
        return maxY;
    }
    
    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }
    
    public boolean isHires() {
        return hires;
    }
    
    public void setHires(boolean hires) {
        this.hires = hires;
    }
    
    public RenderSettings getRenderSettings() {
        return renderSettings;
    }
    
    public void setRenderSettings(RenderSettings renderSettings) {
        this.renderSettings = renderSettings;
    }
} 