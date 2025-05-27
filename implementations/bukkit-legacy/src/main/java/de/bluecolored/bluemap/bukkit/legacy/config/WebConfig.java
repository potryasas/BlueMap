package de.bluecolored.bluemap.bukkit.legacy.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class WebConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private int port = 8100;
    private String bindAddress = "0.0.0.0";
    private String defaultWorld = "world";
    private boolean singleWorldMode = false;
    private boolean useCookies = true;
    private boolean defaultToFlatView = true;
    private double resolutionDefault = 1.0;
    private double minZoomDistance = 100;
    private double maxZoomDistance = 100000;
    private double hiresSliderMax = 500;
    private double hiresSliderDefault = 100;
    private double hiresSliderMin = 0;
    private double lowresSliderMax = 7000;
    private double lowresSliderDefault = 2000;
    private double lowresSliderMin = 500;
    private String mapDataRoot = "maps";
    private String liveDataRoot = "maps";
    private List<String> maps = new ArrayList<>();
    private List<String> scripts = new ArrayList<>();
    private List<String> styles = new ArrayList<>();
    
    public WebConfig() {
        styles.add("assets/custom-style.css");
    }
    
    public void load(File file) throws IOException {
        if (!file.exists()) {
            save(file);
            return;
        }
        
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            
            if (json.has("port")) {
                port = json.get("port").getAsInt();
            }
            
            if (json.has("bind-address")) {
                bindAddress = json.get("bind-address").getAsString();
            }
            
            if (json.has("default-world")) {
                defaultWorld = json.get("default-world").getAsString();
            }
            
            if (json.has("single-world-mode")) {
                singleWorldMode = json.get("single-world-mode").getAsBoolean();
            }
            
            if (json.has("useCookies")) useCookies = json.get("useCookies").getAsBoolean();
            if (json.has("defaultToFlatView")) defaultToFlatView = json.get("defaultToFlatView").getAsBoolean();
            if (json.has("resolutionDefault")) resolutionDefault = json.get("resolutionDefault").getAsDouble();
            if (json.has("minZoomDistance")) minZoomDistance = json.get("minZoomDistance").getAsDouble();
            if (json.has("maxZoomDistance")) maxZoomDistance = json.get("maxZoomDistance").getAsDouble();
            if (json.has("hiresSliderMax")) hiresSliderMax = json.get("hiresSliderMax").getAsDouble();
            if (json.has("hiresSliderDefault")) hiresSliderDefault = json.get("hiresSliderDefault").getAsDouble();
            if (json.has("hiresSliderMin")) hiresSliderMin = json.get("hiresSliderMin").getAsDouble();
            if (json.has("lowresSliderMax")) lowresSliderMax = json.get("lowresSliderMax").getAsDouble();
            if (json.has("lowresSliderDefault")) lowresSliderDefault = json.get("lowresSliderDefault").getAsDouble();
            if (json.has("lowresSliderMin")) lowresSliderMin = json.get("lowresSliderMin").getAsDouble();
            if (json.has("mapDataRoot")) mapDataRoot = json.get("mapDataRoot").getAsString();
            if (json.has("liveDataRoot")) liveDataRoot = json.get("liveDataRoot").getAsString();
        }
    }
    
    public void save(File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        
        JsonObject json = new JsonObject();
        json.addProperty("port", port);
        json.addProperty("bind-address", bindAddress);
        json.addProperty("default-world", defaultWorld);
        json.addProperty("single-world-mode", singleWorldMode);
        
        json.addProperty("useCookies", useCookies);
        json.addProperty("defaultToFlatView", defaultToFlatView);
        json.addProperty("resolutionDefault", resolutionDefault);
        json.addProperty("minZoomDistance", minZoomDistance);
        json.addProperty("maxZoomDistance", maxZoomDistance);
        json.addProperty("hiresSliderMax", hiresSliderMax);
        json.addProperty("hiresSliderDefault", hiresSliderDefault);
        json.addProperty("hiresSliderMin", hiresSliderMin);
        json.addProperty("lowresSliderMax", lowresSliderMax);
        json.addProperty("lowresSliderDefault", lowresSliderDefault);
        json.addProperty("lowresSliderMin", lowresSliderMin);
        json.addProperty("mapDataRoot", mapDataRoot);
        json.addProperty("liveDataRoot", liveDataRoot);
        
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(json, writer);
        }
    }
    
    public int getPort() {
        return port;
    }
    
    public String getBindAddress() {
        return bindAddress;
    }
    
    public String getDefaultWorld() {
        return defaultWorld;
    }
    
    public boolean isSingleWorldMode() {
        return singleWorldMode;
    }
    
    public boolean isUseCookies() {
        return useCookies;
    }
    
    public boolean isDefaultToFlatView() {
        return defaultToFlatView;
    }
    
    public double getResolutionDefault() {
        return resolutionDefault;
    }
    
    public double getMinZoomDistance() {
        return minZoomDistance;
    }
    
    public double getMaxZoomDistance() {
        return maxZoomDistance;
    }
    
    public double getHiresSliderMax() {
        return hiresSliderMax;
    }
    
    public double getHiresSliderDefault() {
        return hiresSliderDefault;
    }
    
    public double getHiresSliderMin() {
        return hiresSliderMin;
    }
    
    public double getLowresSliderMax() {
        return lowresSliderMax;
    }
    
    public double getLowresSliderDefault() {
        return lowresSliderDefault;
    }
    
    public double getLowresSliderMin() {
        return lowresSliderMin;
    }
    
    public String getMapDataRoot() {
        return mapDataRoot;
    }
    
    public String getLiveDataRoot() {
        return liveDataRoot;
    }
    
    public List<String> getMaps() {
        return maps;
    }
    
    public List<String> getScripts() {
        return scripts;
    }
    
    public List<String> getStyles() {
        return styles;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }
    
    public void setDefaultWorld(String defaultWorld) {
        this.defaultWorld = defaultWorld;
    }
    
    public void setSingleWorldMode(boolean singleWorldMode) {
        this.singleWorldMode = singleWorldMode;
    }
    
    public void setUseCookies(boolean useCookies) {
        this.useCookies = useCookies;
    }
    
    public void setDefaultToFlatView(boolean defaultToFlatView) {
        this.defaultToFlatView = defaultToFlatView;
    }
    
    public void setResolutionDefault(double resolutionDefault) {
        this.resolutionDefault = resolutionDefault;
    }
    
    public void setMinZoomDistance(double minZoomDistance) {
        this.minZoomDistance = minZoomDistance;
    }
    
    public void setMaxZoomDistance(double maxZoomDistance) {
        this.maxZoomDistance = maxZoomDistance;
    }
    
    public void setHiresSliderMax(double hiresSliderMax) {
        this.hiresSliderMax = hiresSliderMax;
    }
    
    public void setHiresSliderDefault(double hiresSliderDefault) {
        this.hiresSliderDefault = hiresSliderDefault;
    }
    
    public void setHiresSliderMin(double hiresSliderMin) {
        this.hiresSliderMin = hiresSliderMin;
    }
    
    public void setLowresSliderMax(double lowresSliderMax) {
        this.lowresSliderMax = lowresSliderMax;
    }
    
    public void setLowresSliderDefault(double lowresSliderDefault) {
        this.lowresSliderDefault = lowresSliderDefault;
    }
    
    public void setLowresSliderMin(double lowresSliderMin) {
        this.lowresSliderMin = lowresSliderMin;
    }
    
    public void setMapDataRoot(String mapDataRoot) {
        this.mapDataRoot = mapDataRoot;
    }
    
    public void setLiveDataRoot(String liveDataRoot) {
        this.liveDataRoot = liveDataRoot;
    }
    
    public void setMaps(List<String> maps) {
        this.maps = maps;
    }
    
    public void setScripts(List<String> scripts) {
        this.scripts = scripts;
    }
    
    public void setStyles(List<String> styles) {
        this.styles = styles;
    }
} 