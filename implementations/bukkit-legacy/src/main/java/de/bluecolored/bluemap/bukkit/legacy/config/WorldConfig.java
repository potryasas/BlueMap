package de.bluecolored.bluemap.bukkit.legacy.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class WorldConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private String name;
    private int sorting;
    private HiresConfig hires;
    private LowresConfig lowres;
    private double[] startPos;
    private double[] skyColor;
    private double[] voidColor;
    private double ambientLight;
    private double skyLight;
    private boolean perspectiveView;
    private boolean flatView;
    private boolean freeFlightView;
    
    public WorldConfig() {
        this.name = "Overworld";
        this.sorting = 0;
        this.hires = new HiresConfig();
        this.lowres = new LowresConfig();
        this.startPos = new double[]{-1, 0};
        this.skyColor = new double[]{0.5686274766921997, 0.6745098233222961, 1.0, 1.0};
        this.voidColor = new double[]{0.239215686917305, 0.3137255012989044, 0.686274528503418, 1.0};
        this.ambientLight = 0.1;
        this.skyLight = 1.0;
        this.perspectiveView = true;
        this.flatView = true;
        this.freeFlightView = false;
    }
    
    public void load(File file) throws IOException {
        if (!file.exists()) {
            save(file);
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            
            if (json.has("name")) name = json.get("name").getAsString();
            if (json.has("sorting")) sorting = json.get("sorting").getAsInt();
            if (json.has("hires")) hires = GSON.fromJson(json.get("hires"), HiresConfig.class);
            if (json.has("lowres")) lowres = GSON.fromJson(json.get("lowres"), LowresConfig.class);
            if (json.has("startPos")) startPos = GSON.fromJson(json.get("startPos"), double[].class);
            if (json.has("skyColor")) skyColor = GSON.fromJson(json.get("skyColor"), double[].class);
            if (json.has("voidColor")) voidColor = GSON.fromJson(json.get("voidColor"), double[].class);
            if (json.has("ambientLight")) ambientLight = json.get("ambientLight").getAsDouble();
            if (json.has("skyLight")) skyLight = json.get("skyLight").getAsDouble();
            if (json.has("perspectiveView")) perspectiveView = json.get("perspectiveView").getAsBoolean();
            if (json.has("flatView")) flatView = json.get("flatView").getAsBoolean();
            if (json.has("freeFlightView")) freeFlightView = json.get("freeFlightView").getAsBoolean();
        }
    }
    
    public void save(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        }
    }
    
    public static class HiresConfig {
        private int[] tileSize;
        private double[] scale;
        private double[] translate;
        
        public HiresConfig() {
            this.tileSize = new int[]{32, 32};
            this.scale = new double[]{1, 1};
            this.translate = new double[]{2, 2};
        }
        
        public int[] getTileSize() {
            return tileSize;
        }
        
        public double[] getScale() {
            return scale;
        }
        
        public double[] getTranslate() {
            return translate;
        }
        
        public void setTileSize(int[] tileSize) {
            this.tileSize = tileSize;
        }
        
        public void setScale(double[] scale) {
            this.scale = scale;
        }
        
        public void setTranslate(double[] translate) {
            this.translate = translate;
        }
    }
    
    public static class LowresConfig {
        private int[] tileSize;
        private int lodFactor;
        private int lodCount;
        
        public LowresConfig() {
            this.tileSize = new int[]{500, 500};
            this.lodFactor = 5;
            this.lodCount = 3;
        }
        
        public int[] getTileSize() {
            return tileSize;
        }
        
        public int getLodFactor() {
            return lodFactor;
        }
        
        public int getLodCount() {
            return lodCount;
        }
        
        public void setTileSize(int[] tileSize) {
            this.tileSize = tileSize;
        }
        
        public void setLodFactor(int lodFactor) {
            this.lodFactor = lodFactor;
        }
        
        public void setLodCount(int lodCount) {
            this.lodCount = lodCount;
        }
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public int getSorting() {
        return sorting;
    }
    
    public HiresConfig getHires() {
        return hires;
    }
    
    public LowresConfig getLowres() {
        return lowres;
    }
    
    public double[] getStartPos() {
        return startPos;
    }
    
    public double[] getSkyColor() {
        return skyColor;
    }
    
    public double[] getVoidColor() {
        return voidColor;
    }
    
    public double getAmbientLight() {
        return ambientLight;
    }
    
    public double getSkyLight() {
        return skyLight;
    }
    
    public boolean isPerspectiveView() {
        return perspectiveView;
    }
    
    public boolean isFlatView() {
        return flatView;
    }
    
    public boolean isFreeFlightView() {
        return freeFlightView;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setSorting(int sorting) {
        this.sorting = sorting;
    }
    
    public void setHires(HiresConfig hires) {
        this.hires = hires;
    }
    
    public void setLowres(LowresConfig lowres) {
        this.lowres = lowres;
    }
    
    public void setStartPos(double[] startPos) {
        this.startPos = startPos;
    }
    
    public void setSkyColor(double[] skyColor) {
        this.skyColor = skyColor;
    }
    
    public void setVoidColor(double[] voidColor) {
        this.voidColor = voidColor;
    }
    
    public void setAmbientLight(double ambientLight) {
        this.ambientLight = ambientLight;
    }
    
    public void setSkyLight(double skyLight) {
        this.skyLight = skyLight;
    }
    
    public void setPerspectiveView(boolean perspectiveView) {
        this.perspectiveView = perspectiveView;
    }
    
    public void setFlatView(boolean flatView) {
        this.flatView = flatView;
    }
    
    public void setFreeFlightView(boolean freeFlightView) {
        this.freeFlightView = freeFlightView;
    }
} 