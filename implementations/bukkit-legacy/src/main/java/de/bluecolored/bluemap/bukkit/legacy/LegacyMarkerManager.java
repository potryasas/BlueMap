/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 */
package de.bluecolored.bluemap.bukkit.legacy;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

/**
 * Менеджер маркеров для Legacy BlueMap - без Gson для совместимости с MC 1.5.2
 * Улучшенная версия с поддержкой Unicode и проверкой дубликатов
 */
public class LegacyMarkerManager {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final LegacyBukkitPlugin plugin;
    private final File markersFile;
    private final File markerSetsFile;
    private final File webAssetsDir;
    private final File webMapsDir;
    
    // Маркеры: category -> markers
    private final Map<String, Map<String, LegacyMarker>> markers = new ConcurrentHashMap<String, Map<String, LegacyMarker>>();
    
    // Категории маркеров
    private final Map<String, LegacyMarkerSet> markerSets = new ConcurrentHashMap<String, LegacyMarkerSet>();
    
    // Доступные иконки
    private final Map<String, String> availableIcons = new HashMap<String, String>();
    
    public LegacyMarkerManager(LegacyBukkitPlugin plugin) {
        this.plugin = plugin;
        
        // Initialize directories
        File webRoot = new File(plugin.getDataFolder(), "web");
        this.webAssetsDir = new File(webRoot, "assets");
        this.webMapsDir = new File(webRoot, "maps");
        this.markersFile = new File(webMapsDir, "markers.json");
        this.markerSetsFile = new File(webMapsDir, "markersets.json");
        
        // Create directories
        webRoot.mkdirs();
        webAssetsDir.mkdirs();
        webMapsDir.mkdirs();
        
        // Initialize default marker sets
        createDefaultSets();
        
        // Load existing data
        loadMarkerSets();
        loadMarkers();
        
        // Initialize available icons
        initializeIcons();
        
        plugin.getLogger().info("Marker manager initialized with " + markerSets.size() + " sets");
    }
    
    private void initializeIcons() {
        availableIcons.put("house", "house.png");
        availableIcons.put("bighouse", "bighouse.png");
        availableIcons.put("building", "building.png");
        availableIcons.put("castle", "castle.png");
        availableIcons.put("church", "church.png");
        availableIcons.put("temple", "temple.png");
        availableIcons.put("spawn", "spawn.png");
        availableIcons.put("portal", "portal.png");
        availableIcons.put("anchor", "anchor.png");
        availableIcons.put("compass", "compass.png");
        availableIcons.put("world", "world.png");
        availableIcons.put("pin", "pin.png");
        availableIcons.put("mine", "mine.png");
        availableIcons.put("chest", "chest.png");
        availableIcons.put("diamond", "diamond.png");
        availableIcons.put("ruby", "ruby.png");
        availableIcons.put("coins", "coins.png");
        availableIcons.put("goldstar", "goldstar.png");
        availableIcons.put("minecart", "minecart.png");
        availableIcons.put("truck", "truck.png");
        availableIcons.put("cart", "cart.png");
        availableIcons.put("walk", "walk.png");
        availableIcons.put("redflag", "redflag.png");
        availableIcons.put("blueflag", "blueflag.png");
        availableIcons.put("greenflag", "greenflag.png");
        availableIcons.put("yellowflag", "yellowflag.png");
        availableIcons.put("fire", "fire.png");
        availableIcons.put("skull", "skull.png");
        availableIcons.put("heart", "heart.png");
        availableIcons.put("star", "star.png");
        availableIcons.put("warning", "warning.png");
        availableIcons.put("lightbulb", "lightbulb.png");
    }
    
    private void createDefaultSets() {
        addMarkerSet("places", "Важные места", true, false);
        addMarkerSet("buildings", "Постройки", true, false);
        addMarkerSet("resources", "Ресурсы", true, false);
        addMarkerSet("transport", "Транспорт", true, false);
    }
    
    public boolean addMarkerWithNumericId(String setId, String label, double x, double y, double z, String icon, String dimension) {
        String markerId = getNextNumericMarkerId(setId);
        LegacyMarker marker = new LegacyMarker(markerId, label, x, y, z, icon, dimension);
        
        if (!markers.containsKey(setId)) {
            markers.put(setId, new ConcurrentHashMap<>());
        }
        
        markers.get(setId).put(markerId, marker);
        saveMarkers();
        updateWebMap();
        
        return true;
    }
    
    public void updateWebMap() {
        try {
            // Create base directories
            File mapsDir = new File(plugin.getDataFolder(), "web/maps");
            mapsDir.mkdirs();
            
            // Update markers for each dimension with correct paths
            updateDimensionMarkers(new File(mapsDir, "world"), "overworld");
            updateDimensionMarkers(new File(mapsDir, "world_nether"), "nether");
            updateDimensionMarkers(new File(mapsDir, "world_the_end"), "the_end");
            
            plugin.getLogger().info("Updated web map markers for all dimensions");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to update web map: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateDimensionMarkers(File worldDir, String dimension) throws IOException {
        // Ensure directories exist
        worldDir.mkdirs();
        File liveDir = new File(worldDir, "live");
        liveDir.mkdirs();
        
        File markersFile = new File(liveDir, "markers.json");
        
        // Create markers JSON structure
        JsonObject root = new JsonObject();
        
        // Add marker sets
        for (Map.Entry<String, LegacyMarkerSet> setEntry : markerSets.entrySet()) {
            String setId = setEntry.getKey();
            LegacyMarkerSet set = setEntry.getValue();
            
            JsonObject setJson = new JsonObject();
            setJson.addProperty("label", set.getLabel());
            setJson.addProperty("toggleable", set.isToggleable());
            setJson.addProperty("defaultHidden", set.isDefaultHidden());
            setJson.addProperty("sorting", 0);
            
            // Add markers for this dimension
            JsonObject markersJson = new JsonObject();
            Map<String, LegacyMarker> setMarkers = markers.get(setId);
            
            if (setMarkers != null) {
                for (Map.Entry<String, LegacyMarker> markerEntry : setMarkers.entrySet()) {
                    LegacyMarker marker = markerEntry.getValue();
                    
                    // Only include markers for this dimension
                    if (marker.getDimension().equals(dimension)) {
                        JsonObject markerJson = new JsonObject();
                        markerJson.addProperty("label", marker.getLabel());
                        markerJson.addProperty("type", "poi");
                        markerJson.addProperty("icon", marker.getIcon());
                        markerJson.addProperty("detail", "");
                        markerJson.addProperty("listed", true);
                        markerJson.addProperty("sorting", 0);
                        
                        JsonObject position = new JsonObject();
                        position.addProperty("x", marker.getX());
                        position.addProperty("y", marker.getY());
                        position.addProperty("z", marker.getZ());
                        markerJson.add("position", position);
                        
                        JsonObject anchor = new JsonObject();
                        anchor.addProperty("x", 25);
                        anchor.addProperty("y", 45);
                        markerJson.add("anchor", anchor);
                        
                        markerJson.addProperty("minDistance", 0.0);
                        markerJson.addProperty("maxDistance", 1.0E7);
                        markerJson.add("classes", new JsonArray());
                        
                        markersJson.add(markerEntry.getKey(), markerJson);
                    }
                }
            }
            
            setJson.add("markers", markersJson);
            root.add(setId, setJson);
        }
        
        // Write to file
        try (FileWriter writer = new FileWriter(markersFile)) {
            GSON.toJson(root, writer);
        }
    }
    
    /**
     * Копирует PNG иконки из ресурсов JAR в веб-папку сервера
     */
    private void copyIconsToWebFolder() {
        try {
            // Создаем папку web/assets если не существует
            File webAssetsDir = new File(plugin.getDataFolder(), "web/assets");
            if (!webAssetsDir.exists()) {
                webAssetsDir.mkdirs();
            }
            
            // Список всех PNG иконок для копирования
            String[] iconFiles = {
                "default.png", "house.png", "bighouse.png", "building.png", "church.png", 
                "temple.png", "tower.png", "factory.png", "bank.png", "lighthouse.png",
                "portal.png", "anchor.png", "compass.png", "world.png", "pin.png",
                "chest.png", "diamond.png", "ruby.png", "coins.png", "goldstar.png", 
                "silverstar.png", "bronzestar.png", "minecart.png", "truck.png", "cart.png", 
                "walk.png", "redflag.png", "blueflag.png", "greenflag.png", "yellowflag.png",
                "pinkflag.png", "purpleflag.png", "orangeflag.png", "pirateflag.png",
                "wrench.png", "hammer.png", "gear.png", "key.png", "lock.png", "construction.png",
                "tree.png", "flower.png", "cake.png", "beer.png", "drink.png", "cutlery.png",
                "fire.png", "skull.png", "heart.png", "star.png", "sun.png", "warning.png",
                "caution.png", "exclamation.png", "pointup.png", "pointdown.png", "pointleft.png",
                "pointright.png", "up.png", "down.png", "left.png", "right.png",
                "camera.png", "bed.png", "door.png", "sign.png", "comment.png", "lightbulb.png",
                "theater.png", "cup.png", "bookshelf.png", "basket.png", "bomb.png", "bricks.png",
                "dog.png", "king.png", "queen.png", "scales.png", "shield.png", "tornado.png",
                "cross.png", "offlineuser.png", "goldmedal.png", "silvermedal.png", "bronzemedal.png"
            };
            
            int copiedCount = 0;
            for (String iconFile : iconFiles) {
                try {
                    // Пытаемся скопировать из ресурсов JAR
                    InputStream resourceStream = plugin.getResource("web/assets/" + iconFile);
                    if (resourceStream != null) {
                        File targetFile = new File(webAssetsDir, iconFile);
                        if (!targetFile.exists()) { // копируем только если файла нет
                            FileOutputStream out = new FileOutputStream(targetFile);
                            
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = resourceStream.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                            
                            out.close();
                            resourceStream.close();
                            copiedCount++;
                        }
                    }
                } catch (Exception e) {
                    // Игнорируем ошибки отдельных файлов
                }
            }
            
            plugin.getLogger().info("Copied " + copiedCount + " icon files to web/assets folder");
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error copying icons to web folder: " + e.getMessage());
        }
    }
    
    public boolean createMarkerSet(String id, String label) {
        // Проверка на дубликаты
        if (markerSets.containsKey(id)) {
            plugin.getLogger().warning("Marker set with ID '" + id + "' already exists!");
            return false;
        }
        
        // Проверка на одинаковые названия
        for (LegacyMarkerSet existingSet : markerSets.values()) {
            if (existingSet.getLabel().equals(label)) {
                plugin.getLogger().warning("Marker set with label '" + label + "' already exists!");
                return false;
            }
        }
        
        LegacyMarkerSet set = new LegacyMarkerSet(id, label, true, false);
        markerSets.put(id, set);
        saveMarkerSets();
        plugin.getLogger().info("Created marker set: " + id + " (" + label + ")");
        return true;
    }
    
    /**
     * Поиск маркера по названию в категории
     */
    public LegacyMarker findMarkerByLabel(String setId, String label) {
        if (label == null || label.trim().isEmpty()) {
            return null;
        }
        
        Map<String, LegacyMarker> setMarkers = markers.get(setId);
        if (setMarkers == null) {
            return null;
        }
        
        String searchLabel = label.trim().toLowerCase();
        
        // Сначала ищем точное совпадение
        for (LegacyMarker marker : setMarkers.values()) {
            if (marker.getLabel().toLowerCase().equals(searchLabel)) {
                return marker;
            }
        }
        
        // Затем ищем частичное совпадение
        for (LegacyMarker marker : setMarkers.values()) {
            if (marker.getLabel().toLowerCase().contains(searchLabel) || 
                searchLabel.contains(marker.getLabel().toLowerCase())) {
                return marker;
            }
        }
        
        return null;
    }

    /**
     * Получить ID маркера по его объекту
     */
    public String getMarkerIdByMarker(String setId, LegacyMarker marker) {
        Map<String, LegacyMarker> setMarkers = markers.get(setId);
        if (setMarkers == null) {
            return null;
        }
        
        for (Map.Entry<String, LegacyMarker> entry : setMarkers.entrySet()) {
            if (entry.getValue() == marker) {
                return entry.getKey();
            }
        }
        
        return null;
    }

    /**
     * Удаляет маркер из категории по ID или названию
     */
    public boolean removeMarkerByIdOrLabel(String setId, String markerIdOrLabel) {
        // Проверяем существование категории
        if (!markerSets.containsKey(setId)) {
            plugin.getLogger().warning("Cannot remove marker - category " + setId + " not found");
            return false;
        }
        
        Map<String, LegacyMarker> setMarkers = markers.get(setId);
        if (setMarkers == null) {
            plugin.getLogger().warning("Cannot remove marker - category " + setId + " has no markers");
            return false;
        }
        
        // Сначала пробуем найти по ID
        if (setMarkers.containsKey(markerIdOrLabel)) {
            LegacyMarker removed = setMarkers.remove(markerIdOrLabel);
            saveMarkers();
            updateWebMap();
            plugin.getLogger().info("Removed marker " + markerIdOrLabel + " (" + removed.getLabel() + ") from category " + setId);
            return true;
        }
        
        // Если не нашли по ID, ищем по названию
        LegacyMarker markerToRemove = findMarkerByLabel(setId, markerIdOrLabel);
        if (markerToRemove != null) {
            String markerId = getMarkerIdByMarker(setId, markerToRemove);
            if (markerId != null) {
                setMarkers.remove(markerId);
                saveMarkers();
                updateWebMap();
                plugin.getLogger().info("Removed marker " + markerId + " (" + markerToRemove.getLabel() + ") from category " + setId);
                return true;
            }
        }
        
        plugin.getLogger().warning("Cannot remove marker - marker " + markerIdOrLabel + " not found in category " + setId);
        return false;
    }
    
    /**
     * Удаляет категорию и все её маркеры
     */
    public boolean removeMarkerSet(String setId) {
        // Проверяем существование категории
        if (!markerSets.containsKey(setId)) {
            plugin.getLogger().warning("Cannot remove category - category " + setId + " not found");
            return false;
        }
        
        // Получаем количество маркеров для лога
        int markerCount = 0;
        Map<String, LegacyMarker> setMarkers = markers.get(setId);
        if (setMarkers != null) {
            markerCount = setMarkers.size();
        }
        
        // Удаляем категорию и все её маркеры
        LegacyMarkerSet removed = markerSets.remove(setId);
        markers.remove(setId);
        
        // Сохраняем изменения
        saveMarkerSets();
        saveMarkers();
        updateWebMap();
        
        plugin.getLogger().info("Removed category " + setId + " (" + removed.getLabel() + ") with " + markerCount + " markers");
        return true;
    }
    
    /**
     * Возвращает следующий свободный числовой ID для маркера в категории
     */
    public String getNextNumericMarkerId(String setId) {
        int maxId = 0;
        Map<String, LegacyMarker> setMarkers = markers.get(setId);
        if (setMarkers != null) {
            for (String id : setMarkers.keySet()) {
                try {
                    int num = Integer.parseInt(id);
                    if (num > maxId) maxId = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.valueOf(maxId + 1);
    }

    public boolean addMarker(String setId, String markerId, String label, double x, double y, double z, String icon) {
        return addMarker(setId, markerId, label, x, y, z, icon, "overworld");
    }

    public boolean addMarker(String setId, String markerId, String label, double x, double y, double z, String icon, String dimension) {
        // Проверяем существование категории
        if (!markerSets.containsKey(setId)) {
            plugin.getLogger().warning("Marker set " + setId + " does not exist!");
            return false;
        }
        
        // Проверяем дубликаты маркеров в этой категории
        Map<String, LegacyMarker> setMarkers = markers.get(setId);
        if (setMarkers != null) {
            if (setMarkers.containsKey(markerId)) {
                plugin.getLogger().warning("Marker with ID '" + markerId + "' already exists in set '" + setId + "'!");
                return false;
            }
            
            // Проверяем одинаковые названия в категории
            for (LegacyMarker existingMarker : setMarkers.values()) {
                if (existingMarker.getLabel().equals(label)) {
                    plugin.getLogger().warning("Marker with label '" + label + "' already exists in set '" + setId + "'!");
                    return false;
                }
            }
        }
        
        // Проверяем иконку
        String iconPath = availableIcons.get(icon);
        if (iconPath == null) {
            iconPath = availableIcons.get("default"); // web/assets/default.png
        }
        if (iconPath == null) {
            iconPath = "web/assets/default.png"; // резервный вариант
        }
        
        // Создаем маркер
        LegacyMarker marker = new LegacyMarker(markerId, label, x, y, z, icon, dimension);
        
        if (!markers.containsKey(setId)) {
            markers.put(setId, new ConcurrentHashMap<>());
        }
        markers.get(setId).put(markerId, marker);
        
        // Сохраняем и обновляем
        saveMarkers();
        updateWebMap();
        
        plugin.getLogger().info("Added marker: " + markerId + " (" + label + ") to set " + setId + " at (" + (int)x + "," + (int)y + "," + (int)z + ") in dimension " + dimension);
        return true;
    }

    /**
     * Поиск категории по названию (лейблу)
     */
    public String findMarkerSetIdByLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            return null;
        }
        
        String searchLabel = label.trim().toLowerCase();
        String bestMatch = null;
        int bestMatchScore = 0;
        
        for (Map.Entry<String, LegacyMarkerSet> entry : markerSets.entrySet()) {
            String setLabel = entry.getValue().getLabel().toLowerCase();
            
            // Точное совпадение
            if (setLabel.equals(searchLabel)) {
                return entry.getKey();
            }
            
            // Частичное совпадение
            if (setLabel.contains(searchLabel) || searchLabel.contains(setLabel)) {
                int score = Math.min(setLabel.length(), searchLabel.length());
                if (score > bestMatchScore) {
                    bestMatchScore = score;
                    bestMatch = entry.getKey();
                }
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Получить все названия категорий для автодополнения
     */
    public Set<String> getMarkerSetLabels() {
        Set<String> labels = new HashSet<String>();
        for (LegacyMarkerSet set : markerSets.values()) {
            labels.add(set.getLabel());
        }
        return labels;
    }

    public JsonObject getMarkersJson() {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Map<String, LegacyMarker>> setEntry : markers.entrySet()) {
            JsonObject setData = new JsonObject();
            
            for (Map.Entry<String, LegacyMarker> markerEntry : setEntry.getValue().entrySet()) {
                JsonObject markerData = new JsonObject();
                LegacyMarker marker = markerEntry.getValue();
                
                markerData.addProperty("label", marker.getLabel());
                markerData.addProperty("x", marker.getX());
                markerData.addProperty("y", marker.getY());
                markerData.addProperty("z", marker.getZ());
                markerData.addProperty("icon", marker.getIcon());
                
                setData.add(markerEntry.getKey(), markerData);
            }
            
            json.add(setEntry.getKey(), setData);
        }
        return json;
    }

    public void saveMarkers() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            // Save markers to JSON file
            JsonObject root = new JsonObject();
            
            for (Map.Entry<String, Map<String, LegacyMarker>> setEntry : markers.entrySet()) {
                String setId = setEntry.getKey();
                Map<String, LegacyMarker> setMarkers = setEntry.getValue();
                
                JsonObject setJson = new JsonObject();
                for (Map.Entry<String, LegacyMarker> markerEntry : setMarkers.entrySet()) {
                    String markerId = markerEntry.getKey();
                    LegacyMarker marker = markerEntry.getValue();
                    
                    JsonObject markerJson = new JsonObject();
                    markerJson.addProperty("label", marker.getLabel());
                    markerJson.addProperty("x", marker.getX());
                    markerJson.addProperty("y", marker.getY());
                    markerJson.addProperty("z", marker.getZ());
                    markerJson.addProperty("icon", marker.getIcon());
                    markerJson.addProperty("dimension", marker.getDimension());
                    
                    setJson.add(markerId, markerJson);
                }
                
                root.add(setId, setJson);
            }
            
            try (FileWriter writer = new FileWriter(markersFile)) {
                GSON.toJson(root, writer);
            }
            
            plugin.getLogger().info("Saved " + getTotalMarkerCount() + " markers to " + markersFile.getName());
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving markers: " + e.getMessage());
        }
    }
    
    public void loadMarkers() {
        if (!markersFile.exists()) {
            plugin.getLogger().info("No markers file found, starting fresh");
            return;
        }
        
        try {
            JsonObject root = GSON.fromJson(new FileReader(markersFile), JsonObject.class);
            markers.clear();
            
            for (Map.Entry<String, JsonElement> setEntry : root.entrySet()) {
                String setId = setEntry.getKey();
                JsonObject setJson = setEntry.getValue().getAsJsonObject();
                
                Map<String, LegacyMarker> setMarkers = new ConcurrentHashMap<>();
                for (Map.Entry<String, JsonElement> markerEntry : setJson.entrySet()) {
                    String markerId = markerEntry.getKey();
                    JsonObject markerJson = markerEntry.getValue().getAsJsonObject();
                    
                    String label = markerJson.get("label").getAsString();
                    double x = markerJson.get("x").getAsDouble();
                    double y = markerJson.get("y").getAsDouble();
                    double z = markerJson.get("z").getAsDouble();
                    String icon = markerJson.get("icon").getAsString();
                    String dimension = markerJson.has("dimension") ? 
                        markerJson.get("dimension").getAsString() : "overworld";
                    
                    LegacyMarker marker = new LegacyMarker(markerId, label, x, y, z, icon, dimension);
                    setMarkers.put(markerId, marker);
                }
                
                markers.put(setId, setMarkers);
            }
            
            plugin.getLogger().info("Loaded " + getTotalMarkerCount() + " markers from " + markersFile.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading markers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void saveMarkerSets() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            JsonObject root = new JsonObject();
            
            for (Map.Entry<String, LegacyMarkerSet> entry : markerSets.entrySet()) {
                String setId = entry.getKey();
                LegacyMarkerSet set = entry.getValue();
                
                JsonObject setJson = new JsonObject();
                setJson.addProperty("label", set.getLabel());
                setJson.addProperty("toggleable", set.isToggleable());
                setJson.addProperty("defaultHidden", set.isDefaultHidden());
                
                root.add(setId, setJson);
            }
            
            try (FileWriter writer = new FileWriter(markerSetsFile)) {
                GSON.toJson(root, writer);
            }
            
            plugin.getLogger().info("Saved " + markerSets.size() + " marker sets to " + markerSetsFile.getName());
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving marker sets: " + e.getMessage());
        }
    }
    
    public void loadMarkerSets() {
        if (!markerSetsFile.exists()) {
            plugin.getLogger().info("No marker sets file found, using defaults");
            return;
        }
        
        try {
            JsonObject root = GSON.fromJson(new FileReader(markerSetsFile), JsonObject.class);
            
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String setId = entry.getKey();
                JsonObject setJson = entry.getValue().getAsJsonObject();
                
                String label = setJson.get("label").getAsString();
                boolean toggleable = setJson.get("toggleable").getAsBoolean();
                boolean defaultHidden = setJson.get("defaultHidden").getAsBoolean();
                
                LegacyMarkerSet set = new LegacyMarkerSet(setId, label, toggleable, defaultHidden);
                markerSets.put(setId, set);
            }
            
            plugin.getLogger().info("Loaded " + markerSets.size() + " marker sets from " + markerSetsFile.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading marker sets: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addMarkerSet(String id, String label, boolean toggleable, boolean defaultHidden) {
        if (!markerSets.containsKey(id)) {
            markerSets.put(id, new LegacyMarkerSet(id, label, toggleable, defaultHidden));
        }
    }
    
    private int getTotalMarkerCount() {
        int total = 0;
        for (Map<String, LegacyMarker> setMarkers : markers.values()) {
            total += setMarkers.size();
        }
        return total;
    }
    
    public Set<String> getMarkerSetIds() {
        return new HashSet<>(markerSets.keySet());
    }
    
    public LegacyMarkerSet getMarkerSet(String id) {
        return markerSets.get(id);
    }
    
    public Map<String, LegacyMarker> getMarkersInSet(String setId) {
        Map<String, LegacyMarker> result = markers.get(setId);
        return result != null ? result : new HashMap<>();
    }
    
    public Set<String> getAvailableIcons() {
        return new HashSet<>(availableIcons.keySet());
    }
    
    public void createExampleMarkers() {
        // Create examples only if categories are empty
        
        // Places
        if (getMarkersInSet("places").isEmpty()) {
            addMarkerWithNumericId("places", "Спавн", 0, 70, 0, "goldstar", "overworld");
            addMarkerWithNumericId("places", "Портал в Ад", 100, 65, 100, "portal", "nether");
        }
        
        // Buildings
        if (getMarkersInSet("buildings").isEmpty()) {
            addMarkerWithNumericId("buildings", "Дом игрока", 50, 70, 50, "house", "overworld");
            addMarkerWithNumericId("buildings", "Замок", -50, 80, -50, "castle", "overworld");
        }
        
        // Resources
        if (getMarkersInSet("resources").isEmpty()) {
            addMarkerWithNumericId("resources", "Алмазная шахта", 200, 12, 200, "chest", "overworld");
            addMarkerWithNumericId("resources", "Ферма", -200, 70, 200, "tree", "overworld");
        }
        
        plugin.getLogger().info("Created example markers");
    }

    public void load() {
        loadMarkerSets();
        loadMarkers();
        copyIconsToWebFolder();
        updateWebMap();
        plugin.getLogger().info("Loaded marker manager with " + getTotalMarkerCount() + " markers in " + markerSets.size() + " sets");
    }

    public void reload() {
        markers.clear();
        markerSets.clear();
        createDefaultSets();
        loadMarkerSets();
        loadMarkers();
        updateWebMap();
        plugin.getLogger().info("Reloaded marker manager with " + getTotalMarkerCount() + " markers in " + markerSets.size() + " sets");
    }

    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== Marker Manager Debug Info ===\n");
        info.append("Total marker sets: ").append(markerSets.size()).append("\n");
        info.append("Total markers: ").append(getTotalMarkerCount()).append("\n");
        info.append("\nMarker Sets:\n");
        
        for (Map.Entry<String, LegacyMarkerSet> entry : markerSets.entrySet()) {
            String setId = entry.getKey();
            LegacyMarkerSet set = entry.getValue();
            Map<String, LegacyMarker> setMarkers = markers.get(setId);
            int markerCount = setMarkers != null ? setMarkers.size() : 0;
            
            info.append("- ").append(setId).append(" (").append(set.getLabel()).append("): ")
                .append(markerCount).append(" markers\n");
        }
        
        info.append("\nMarkers by dimension:\n");
        Map<String, Integer> dimensionCounts = new HashMap<>();
        for (Map<String, LegacyMarker> setMarkers : markers.values()) {
            for (LegacyMarker marker : setMarkers.values()) {
                String dim = marker.getDimension();
                dimensionCounts.put(dim, dimensionCounts.getOrDefault(dim, 0) + 1);
            }
        }
        
        for (Map.Entry<String, Integer> entry : dimensionCounts.entrySet()) {
            info.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" markers\n");
        }
        
        info.append("\nAvailable icons: ").append(availableIcons.size()).append("\n");
        info.append("Web assets dir: ").append(webAssetsDir.getAbsolutePath()).append("\n");
        info.append("Web maps dir: ").append(webMapsDir.getAbsolutePath()).append("\n");
        
        return info.toString();
    }

    public LegacyMarker createMarker(String setId, String markerId, String label, Location location, String icon) {
        // Get or create marker set
        LegacyMarkerSet markerSet = markerSets.get(setId);
        if (markerSet == null) {
            markerSet = new LegacyMarkerSet(setId, setId, true, false);
            markerSets.put(setId, markerSet);
        }
        
        // Get dimension from world
        String dimension = getDimensionFromWorld(location.getWorld());
        
        // Create marker
        LegacyMarker marker = new LegacyMarker(
            markerId,
            label,
            location.getX(),
            location.getY(),
            location.getZ(),
            icon,
            dimension
        );
        
        // Add to markers map
        Map<String, LegacyMarker> setMarkers = markers.get(setId);
        if (setMarkers == null) {
            setMarkers = new ConcurrentHashMap<>();
            markers.put(setId, setMarkers);
        }
        setMarkers.put(markerId, marker);
        
        // Update all dimensions
        updateAllDimensions();
        
        return marker;
    }

    private String getDimensionFromWorld(org.bukkit.World world) {
        String name = world.getName().toLowerCase();
        if (name.contains("nether")) {
            return "nether";
        } else if (name.contains("the_end") || name.contains("end")) {
            return "end";
        }
        return "overworld";
    }

    private void updateAllDimensions() {
        try {
            // Update markers for each dimension
            for (String dimension : new String[]{"overworld", "nether", "end"}) {
                String worldName = getWorldNameForDimension(dimension);
                if (worldName != null) {
                    File webRoot = plugin.getWebRoot().toFile();
                    File worldDir = new File(webRoot, "maps/" + worldName);
                    updateDimensionMarkers(worldDir, dimension);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update markers", e);
        }
    }

    private String getWorldNameForDimension(String dimension) {
        switch (dimension) {
            case "nether":
                return "world_nether";
            case "end":
                return "world_the_end";
            case "overworld":
                return "world";
            default:
                return null;
        }
    }
} 