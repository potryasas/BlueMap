package de.bluecolored.bluemap.bukkit.legacy.marker;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitPlugin;

public class LegacyMarkerManager {
    private final LegacyBukkitPlugin plugin;
    private final Map<String, LegacyMarkerSet> markerSets;
    private final Map<String, Map<String, LegacyMarker>> markers;
    private final Path markersPath;
    
    public LegacyMarkerManager(LegacyBukkitPlugin plugin) {
        this.plugin = plugin;
        this.markerSets = new ConcurrentHashMap<>();
        this.markers = new ConcurrentHashMap<>();
        this.markersPath = plugin.getWebRoot().resolve("maps").resolve(plugin.getWorldName()).resolve("live").resolve("markers.json");
    }
    
    public void load() {
        try {
            Files.createDirectories(markersPath.getParent());
            if (!Files.exists(markersPath)) {
                saveMarkers();
            }
            loadMarkers();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load markers", e);
        }
    }
    
    public void reload() {
        markerSets.clear();
        markers.clear();
        load();
    }
    
    public boolean createMarkerSet(String id, String label) {
        if (markerSets.containsKey(id)) {
            return false;
        }
        LegacyMarkerSet set = new LegacyMarkerSet(id, label);
        markerSets.put(id, set);
        markers.put(id, new ConcurrentHashMap<>());
        saveMarkers();
        return true;
    }
    
    public LegacyMarkerSet getMarkerSet(String id) {
        return markerSets.get(id);
    }
    
    public Set<String> getMarkerSetIds() {
        return markerSets.keySet();
    }
    
    public List<String> getMarkerSetLabels() {
        List<String> labels = new ArrayList<>();
        for (LegacyMarkerSet set : markerSets.values()) {
            labels.add(set.getLabel());
        }
        return labels;
    }
    
    public String findMarkerSetIdByLabel(String label) {
        for (Map.Entry<String, LegacyMarkerSet> entry : markerSets.entrySet()) {
            if (entry.getValue().getLabel().equalsIgnoreCase(label)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    public Map<String, LegacyMarker> getMarkersInSet(String setId) {
        return markers.getOrDefault(setId, new HashMap<>());
    }
    
    public boolean addMarkerWithNumericId(String setId, String label, double x, double y, double z, String icon, String dimension) {
        if (!markerSets.containsKey(setId)) {
            return false;
        }
        
        Map<String, LegacyMarker> setMarkers = markers.get(setId);
        int nextId = 1;
        while (setMarkers.containsKey(String.valueOf(nextId))) {
            nextId++;
        }
        
        LegacyMarker marker = new LegacyMarker(String.valueOf(nextId), label, x, y, z, icon, dimension);
        setMarkers.put(marker.getId(), marker);
        saveMarkers();
        updateWebMap();
        return true;
    }
    
    public boolean removeMarkerByIdOrLabel(String setId, String idOrLabel) {
        Map<String, LegacyMarker> setMarkers = markers.get(setId);
        if (setMarkers == null) {
            return false;
        }
        
        // Try by ID first
        if (setMarkers.remove(idOrLabel) != null) {
            saveMarkers();
            updateWebMap();
            return true;
        }
        
        // Try by label
        for (Map.Entry<String, LegacyMarker> entry : setMarkers.entrySet()) {
            if (entry.getValue().getLabel().equalsIgnoreCase(idOrLabel)) {
                setMarkers.remove(entry.getKey());
                saveMarkers();
                updateWebMap();
                return true;
            }
        }
        
        return false;
    }
    
    public boolean removeMarkerSet(String setId) {
        if (markerSets.remove(setId) != null) {
            markers.remove(setId);
            saveMarkers();
            updateWebMap();
            return true;
        }
        return false;
    }
    
    public void saveMarkers() {
        try {
            JsonObject json = new JsonObject();
            
            for (Map.Entry<String, LegacyMarkerSet> setEntry : markerSets.entrySet()) {
                JsonObject setJson = new JsonObject();
                setJson.addProperty("label", setEntry.getValue().getLabel());
                
                JsonObject markersJson = new JsonObject();
                Map<String, LegacyMarker> setMarkers = markers.get(setEntry.getKey());
                if (setMarkers != null) {
                    for (LegacyMarker marker : setMarkers.values()) {
                        JsonObject markerJson = new JsonObject();
                        markerJson.addProperty("label", marker.getLabel());
                        markerJson.addProperty("x", marker.getX());
                        markerJson.addProperty("y", marker.getY());
                        markerJson.addProperty("z", marker.getZ());
                        markerJson.addProperty("icon", marker.getIcon());
                        markerJson.addProperty("dimension", marker.getDimension());
                        markersJson.add(marker.getId(), markerJson);
                    }
                }
                setJson.add("markers", markersJson);
                json.add(setEntry.getKey(), setJson);
            }
            
            try (FileWriter writer = new FileWriter(markersPath.toFile())) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
            
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save markers", e);
        }
    }
    
    private void loadMarkers() {
        try (FileReader reader = new FileReader(markersPath.toFile())) {
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            
            if (json == null) {
                plugin.getLogger().warning("Empty or invalid markers.json file - creating new one");
                saveMarkers();
                return;
            }
            
            for (Map.Entry<String, com.google.gson.JsonElement> setEntry : json.entrySet()) {
                String setId = setEntry.getKey();
                JsonObject setJson = setEntry.getValue().getAsJsonObject();
                
                // Skip invalid set entries
                if (setJson == null) {
                    plugin.getLogger().warning("Skipping invalid marker set: " + setId);
                    continue;
                }
                
                // Get label with fallback
                String label = setJson.has("label") ? 
                    setJson.get("label").getAsString() : 
                    setId;
                
                LegacyMarkerSet set = new LegacyMarkerSet(setId, label);
                markerSets.put(setId, set);
                
                Map<String, LegacyMarker> setMarkers = new ConcurrentHashMap<>();
                
                // Handle missing markers object
                if (!setJson.has("markers")) {
                    plugin.getLogger().warning("No markers found in set: " + setId);
                    markers.put(setId, setMarkers);
                    continue;
                }
                
                JsonObject markersJson = setJson.getAsJsonObject("markers");
                if (markersJson == null) {
                    plugin.getLogger().warning("Invalid markers object in set: " + setId);
                    markers.put(setId, setMarkers);
                    continue;
                }
                
                for (Map.Entry<String, com.google.gson.JsonElement> markerEntry : markersJson.entrySet()) {
                    try {
                        JsonObject markerJson = markerEntry.getValue().getAsJsonObject();
                        
                        // Extract values with defaults
                        String markerLabel = getJsonString(markerJson, "label", markerEntry.getKey());
                        double x = getJsonDouble(markerJson, "x", 0.0);
                        double y = getJsonDouble(markerJson, "y", 64.0);
                        double z = getJsonDouble(markerJson, "z", 0.0);
                        String icon = getJsonString(markerJson, "icon", "pin");
                        String dimension = getJsonString(markerJson, "dimension", "overworld");
                        
                        LegacyMarker marker = new LegacyMarker(
                            markerEntry.getKey(),
                            markerLabel,
                            x, y, z,
                            icon,
                            dimension
                        );
                        setMarkers.put(marker.getId(), marker);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load marker " + markerEntry.getKey() + " in set " + setId + ": " + e.getMessage());
                    }
                }
                markers.put(setId, setMarkers);
            }
            
            plugin.getLogger().info("Loaded " + markerSets.size() + " marker sets");
            
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load markers", e);
            // Create empty markers file if none exists
            saveMarkers();
        }
    }
    
    // Helper methods for safe JSON value extraction
    private String getJsonString(JsonObject json, String key, String defaultValue) {
        if (json == null || !json.has(key) || json.get(key) == null || json.get(key).isJsonNull()) {
            return defaultValue;
        }
        try {
            return json.get(key).getAsString();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    private double getJsonDouble(JsonObject json, String key, double defaultValue) {
        if (json == null || !json.has(key) || json.get(key) == null || json.get(key).isJsonNull()) {
            return defaultValue;
        }
        try {
            return json.get(key).getAsDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    public void updateWebMap() {
        saveMarkers();
    }
    
    public void createExampleMarkers() {
        createMarkerSet("buildings", "Buildings");
        createMarkerSet("resources", "Resources");
        createMarkerSet("transport", "Transport");
        
        addMarkerWithNumericId("buildings", "Spawn Building", 0, 64, 0, "house", "overworld");
        addMarkerWithNumericId("resources", "Diamond Mine", 100, 30, -150, "mine", "overworld");
        addMarkerWithNumericId("transport", "Main Portal", -50, 70, 80, "portal", "nether");
    }
    
    public Set<String> getAvailableIcons() {
        Set<String> icons = ConcurrentHashMap.newKeySet();
        icons.addAll(Arrays.asList(
            "house", "bighouse", "building", "castle", "church", "temple",
            "spawn", "portal", "anchor", "compass", "world", "pin",
            "mine", "chest", "diamond", "ruby", "coins", "goldstar",
            "minecart", "truck", "cart", "walk",
            "redflag", "blueflag", "greenflag", "yellowflag",
            "fire", "skull", "heart", "star", "warning", "lightbulb"
        ));
        return icons;
    }
    
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== Marker Manager Debug Info ===\n");
        info.append("Total marker sets: ").append(markerSets.size()).append("\n");
        info.append("Markers file: ").append(markersPath).append("\n");
        info.append("\nMarker Sets:\n");
        
        for (Map.Entry<String, LegacyMarkerSet> setEntry : markerSets.entrySet()) {
            info.append("- ").append(setEntry.getKey())
                .append(" (").append(setEntry.getValue().getLabel()).append(")\n");
            
            Map<String, LegacyMarker> setMarkers = markers.get(setEntry.getKey());
            if (setMarkers != null) {
                for (LegacyMarker marker : setMarkers.values()) {
                    info.append("  * ").append(marker.getId())
                        .append(": ").append(marker.getLabel())
                        .append(" [").append(marker.getIcon()).append("]")
                        .append(" at (").append((int)marker.getX())
                        .append(",").append((int)marker.getY())
                        .append(",").append((int)marker.getZ())
                        .append(") in ").append(marker.getDimension())
                        .append("\n");
                }
            }
        }
        
        return info.toString();
    }
    
    public JsonObject getMarkersJson() {
        JsonObject json = new JsonObject();
        
        for (Map.Entry<String, LegacyMarkerSet> setEntry : markerSets.entrySet()) {
            JsonObject setJson = new JsonObject();
            setJson.addProperty("label", setEntry.getValue().getLabel());
            
            JsonObject markersJson = new JsonObject();
            Map<String, LegacyMarker> setMarkers = markers.get(setEntry.getKey());
            if (setMarkers != null) {
                for (LegacyMarker marker : setMarkers.values()) {
                    JsonObject markerJson = new JsonObject();
                    markerJson.addProperty("label", marker.getLabel());
                    markerJson.addProperty("x", marker.getX());
                    markerJson.addProperty("y", marker.getY());
                    markerJson.addProperty("z", marker.getZ());
                    markerJson.addProperty("icon", marker.getIcon());
                    markerJson.addProperty("dimension", marker.getDimension());
                    markersJson.add(marker.getId(), markerJson);
                }
            }
            setJson.add("markers", markersJson);
            json.add(setEntry.getKey(), setJson);
        }
        
        return json;
    }
} 