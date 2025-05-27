package de.bluecolored.bluemap.bukkit.legacy.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.World;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitPlugin;
import de.bluecolored.bluemap.bukkit.legacy.config.WebConfig;
import de.bluecolored.bluemap.bukkit.legacy.config.WorldConfig;

public class WebManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String[] REQUIRED_WEB_FILES = {
        "index.html",
        "assets/css/main.css",
        "assets/js/app.js",
        "assets/js/map.js",
        "assets/js/markers.js",
        "assets/images/icons/marker-icon.png",
        "assets/images/icons/marker-shadow.png"
    };
    
    private final LegacyBukkitPlugin plugin;
    private final WebConfig webConfig;
    private final Map<String, WorldConfig> worldConfigs;
    private final LegacyWebServer webServer;
    
    public WebManager(LegacyBukkitPlugin plugin) {
        this.plugin = plugin;
        this.webConfig = new WebConfig();
        this.worldConfigs = new HashMap<>();
        this.webServer = new LegacyWebServer(plugin, webConfig);
        
        // Load configs
        loadConfigs();
        
        // Setup web files
        setupWebFiles();
    }
    
    public void start() {
        try {
            webServer.start();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start web server", e);
        }
    }
    
    public void stop() {
        webServer.stop();
    }
    
    private void loadConfigs() {
        // Load web config
        File webConfigFile = new File(plugin.getDataFolder(), "web/settings.json");
        try {
            webConfig.load(webConfigFile);
            
            // Force single world mode
            webConfig.setDefaultWorld(plugin.getWorldName());
            webConfig.setSingleWorldMode(true);
            
            webConfig.save(webConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load web config", e);
        }
        
        // Load world configs
        for (World world : plugin.getServer().getWorlds()) {
            String worldName = world.getName();
            WorldConfig worldConfig = new WorldConfig();
            File worldConfigFile = new File(plugin.getDataFolder(), "web/maps/" + worldName + "/settings.json");
            try {
                worldConfig.load(worldConfigFile);
                worldConfigs.put(worldName, worldConfig);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load world config for " + worldName, e);
            }
        }
    }
    
    private void setupWebFiles() {
        try {
            // Create web directory structure
            Path webRoot = plugin.getWebRoot();
            Files.createDirectories(webRoot);
            
            // Extract webapp.zip if it exists
            File webappZip = new File(plugin.getDataFolder().getParentFile(), "webapp.zip");
            if (webappZip.exists()) {
                plugin.getLogger().info("Found webapp.zip, extracting...");
                try {
                    extractZip(webappZip, webRoot);
                    plugin.getLogger().info("Successfully extracted webapp.zip");
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to extract webapp.zip: " + e.getMessage());
                }
            } else {
                plugin.getLogger().warning("webapp.zip not found in plugins folder");
            }
            
            // Create maps directory
            Path mapsDir = webRoot.resolve("maps");
            Files.createDirectories(mapsDir);
            
            // Create world directories and settings
            String worldName = plugin.getWorldName();
            if (worldName != null) {
                Path worldDir = mapsDir.resolve(worldName);
                Files.createDirectories(worldDir);
                
                // Create live directory for markers and players
                Path liveDir = worldDir.resolve("live");
                Files.createDirectories(liveDir);
                
                // Create empty markers.json if not exists
                File markersFile = liveDir.resolve("markers.json").toFile();
                if (!markersFile.exists()) {
                    JsonObject emptyMarkers = new JsonObject();
                    emptyMarkers.add("sets", new JsonObject());
                    try (FileOutputStream fos = new FileOutputStream(markersFile)) {
                        fos.write(GSON.toJson(emptyMarkers).getBytes("UTF-8"));
                    }
                }
                
                // Create empty players.json if not exists
                File playersFile = liveDir.resolve("players.json").toFile();
                if (!playersFile.exists()) {
                    JsonObject emptyPlayers = new JsonObject();
                    JsonArray playersArray = new JsonArray();
                    emptyPlayers.add("players", playersArray);
                    emptyPlayers.addProperty("updateInterval", 1000);
                    emptyPlayers.addProperty("showPlayerMarkers", true);
                    emptyPlayers.addProperty("showPlayerBody", true);
                    emptyPlayers.addProperty("showPlayerHead", true);
                    emptyPlayers.addProperty("showLabelBackground", true);
                    emptyPlayers.addProperty("markerSetId", "players");
                    
                    JsonObject markerSet = new JsonObject();
                    markerSet.addProperty("id", "players");
                    markerSet.addProperty("label", "Players");
                    markerSet.addProperty("toggleable", true);
                    markerSet.addProperty("defaultHidden", false);
                    markerSet.addProperty("priority", 1000);
                    emptyPlayers.add("markerSet", markerSet);
                    
                    try (FileOutputStream fos = new FileOutputStream(playersFile)) {
                        fos.write(GSON.toJson(emptyPlayers).getBytes("UTF-8"));
                    }
                }
                
                // Save world config
                WorldConfig worldConfig = worldConfigs.get(worldName);
                if (worldConfig == null) {
                    worldConfig = new WorldConfig();
                    worldConfigs.put(worldName, worldConfig);
                }
                worldConfig.save(worldDir.resolve("settings.json").toFile());
            }
            
            // Save web config with single world mode enabled
            webConfig.setDefaultWorld(plugin.getWorldName());
            webConfig.setSingleWorldMode(true);
            webConfig.save(new File(webRoot.toFile(), "settings.json"));
            
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to setup web files", e);
        }
    }
    
    private void extractZip(File zipFile, Path targetDir) throws IOException {
        java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipFile);
        try {
            for (java.util.Enumeration<? extends java.util.zip.ZipEntry> e = zip.entries(); e.hasMoreElements();) {
                java.util.zip.ZipEntry entry = e.nextElement();
                Path targetPath = targetDir.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    try (InputStream in = zip.getInputStream(entry)) {
                        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } finally {
            zip.close();
        }
    }
    
    public WebConfig getWebConfig() {
        return webConfig;
    }
    
    public WorldConfig getWorldConfig(String worldName) {
        return worldConfigs.get(worldName);
    }
    
    public void updateLiveData(String worldName, JsonObject data) {
        Path liveDir = plugin.getWebRoot().resolve("maps").resolve(worldName).resolve("live");
        try {
            Files.createDirectories(liveDir);
            
            // Update markers.json
            if (data.has("markers")) {
                File markersFile = liveDir.resolve("markers.json").toFile();
                try (FileOutputStream fos = new FileOutputStream(markersFile)) {
                    String markersJson = GSON.toJson(data.get("markers"));
                    fos.write(markersJson.getBytes("UTF-8"));
                    plugin.getLogger().info("Updated markers.json for world " + worldName + ": " + markersJson);
                }
            }
            
            // Update players.json
            if (data.has("players")) {
                File playersFile = liveDir.resolve("players.json").toFile();
                try (FileOutputStream fos = new FileOutputStream(playersFile)) {
                    String playersJson = GSON.toJson(data.get("players"));
                    fos.write(playersJson.getBytes("UTF-8"));
                    plugin.getLogger().info("Updated players.json for world " + worldName + ": " + playersJson);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update live data for " + worldName, e);
        }
    }
} 