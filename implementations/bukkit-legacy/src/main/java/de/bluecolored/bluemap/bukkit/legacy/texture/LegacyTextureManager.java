package de.bluecolored.bluemap.bukkit.legacy.texture;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitPlugin;

public class LegacyTextureManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final LegacyBukkitPlugin plugin;
    private final Map<String, TextureData> textureMap;
    private final Path texturesPath;
    
    public LegacyTextureManager(LegacyBukkitPlugin plugin) {
        this.plugin = plugin;
        this.textureMap = new HashMap<>();
        this.texturesPath = plugin.getWebRoot().resolve("textures.json");
        
        loadTextures();
    }
    
    private void loadTextures() {
        try {
            // Copy default textures.json if it doesn't exist
            if (!Files.exists(texturesPath)) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("textures.json")) {
                    if (is != null) {
                        Files.copy(is, texturesPath, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        plugin.getLogger().warning("Default textures.json not found in resources");
                        // If default is not found, try to generate from blocks.txt
                        generateTexturesFromJson(); 
                        return;
                    }
                }
            }
            
            // Load textures from file
            try (FileReader reader = new FileReader(texturesPath.toFile())) {
                JsonArray jsonArray = GSON.fromJson(reader, JsonArray.class); // Expecting an array now
                
                // Parse texture data
                for (JsonElement element : jsonArray) {
                    JsonObject textureDataJson = element.getAsJsonObject();
                    
                    String resourcePath = textureDataJson.get("resourcePath").getAsString();
                    
                    TextureData data = new TextureData();
                    if (textureDataJson.has("color")) {
                        JsonArray colorArray = textureDataJson.get("color").getAsJsonArray();
                        float[] color = new float[4];
                        for (int i = 0; i < 4; i++) {
                            color[i] = colorArray.get(i).getAsFloat();
                        }
                        // Convert float array to hex string for TextureData (or adapt TextureData)
                        // For now, let's assume TextureData can handle float[] or we adapt it later
                        // This part might need adjustment based on TextureData's capabilities
                        data.setColor(convertFloatArrayToHexString(color)); 
                    }
                    if (textureDataJson.has("texture")) {
                        data.setTexture(textureDataJson.get("texture").getAsString());
                    }
                    // We need a key for the map, resourcePath seems appropriate
                    textureMap.put(resourcePath, data); 
                }
            }
            
            plugin.getLogger().info("Loaded " + textureMap.size() + " block textures");
            
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load textures", e);
        } catch (Exception e) { // Catch other potential parsing errors
            plugin.getLogger().log(Level.SEVERE, "Error parsing textures.json", e);
            // Fallback or error handling
            plugin.getLogger().info("Attempting to generate textures from blocks.txt as a fallback.");
            try {
                generateTexturesFromJson();
            } catch (IOException ioe) {
                plugin.getLogger().log(Level.SEVERE, "Failed to generate textures from blocks.txt", ioe);
            }
        }
    }

    // Helper method to convert float array [r,g,b,a] to hex string #RRGGBB
    // Alpha is ignored for this hex format.
    private String convertFloatArrayToHexString(float[] color) {
        int r = (int) (color[0] * 255);
        int g = (int) (color[1] * 255);
        int b = (int) (color[2] * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }
    
    public void saveTextures() {
        try {
            JsonArray jsonArray = new JsonArray(); // Saving as an array
            
            // Convert texture data to JSON
            for (Map.Entry<String, TextureData> entry : textureMap.entrySet()) {
                JsonObject textureDataJson = new JsonObject();
                TextureData data = entry.getValue();
                
                textureDataJson.addProperty("resourcePath", entry.getKey()); // Use map key as resourcePath
                
                if (data.getColor() != null) {
                    // Assuming getColor returns a hex string, we might need to parse it back to float array
                    // or store color as float[] in TextureData
                    textureDataJson.addProperty("color", data.getColor()); // Placeholder, might need conversion
                }
                if (data.getTexture() != null) {
                    textureDataJson.addProperty("texture", data.getTexture());
                }
                // Add halfTransparent if needed by your structure, assuming false for now
                textureDataJson.addProperty("halfTransparent", false); 
                
                jsonArray.add(textureDataJson);
            }
            
            // Save to file
            try (FileWriter writer = new FileWriter(texturesPath.toFile())) {
                GSON.toJson(jsonArray, writer);
            }
            
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save textures", e);
        }
    }

    // New method to generate textures.json content from blocks.txt logic
    // This is a simplified version, you'll need to adapt your TextureGenerator logic here
    public void generateTexturesFromJson() throws IOException {
        // This would involve reading blocks.txt, parsing colors,
        // and generating the base64 textures similar to TextureGenerator
        // For now, let's re-initialize with defaults as a placeholder
        // to prevent load failures.
        plugin.getLogger().info("Generating default textures as textures.json was missing or invalid.");
        initializeDefaultTextures(); // This puts basic textures in textureMap
        saveTextures(); // This will now save in the array format
        
        // Reload to ensure consistency after generation
        textureMap.clear(); 
        loadTextures(); 
    }
    
    public TextureData getTextureData(String blockIdOrResourcePath) {
        // Try resourcePath first, then legacy blockId if needed
        TextureData data = textureMap.get(blockIdOrResourcePath);
        if (data == null) {
            // Attempt to find by simple name if blockIdOrResourcePath is like "minecraft:block/grass"
            String simpleName = blockIdOrResourcePath.contains("/") ? 
                                blockIdOrResourcePath.substring(blockIdOrResourcePath.lastIndexOf("/") + 1) : 
                                blockIdOrResourcePath;
            data = textureMap.get(simpleName);
        }
        return data;
    }
    
    public void registerTexture(String resourcePath, TextureData textureData) {
        textureMap.put(resourcePath, textureData);
    }
    
    public void clearTextures() {
        textureMap.clear();
        // initializeDefaultTextures(); // Don't auto-init here, let load/generate handle it
    }
    
    private void initializeDefaultTextures() {
        // This method now ensures resourcePath style keys
        // And colors are hex strings
        textureMap.clear(); // Clear before adding defaults

        TextureData missing = new TextureData();
        missing.setColor("#FF00FF"); // Default purple for missing
        missing.setTexture("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAPklEQVR4Xu3MsQkAMAwDQe2/tFPnBB4gpLhG8MpkZpNkZ6AKZKAKZKAKZKAKZKAKZKAKZKAKWg0XD/UPnjg4MbX+EDdeTUwAAAAASUVORK5CYII=");
        textureMap.put("minecraft:block/missing", missing);

        TextureData grass = new TextureData();
        grass.setColor("#55AA55"); // Example, pull from blocks.txt if possible
        // grass.setTexture("base64_encoded_grass_texture_or_path");
        textureMap.put("minecraft:block/grass", grass);

        TextureData dirt = new TextureData();
        dirt.setColor("#866526");
        textureMap.put("minecraft:block/dirt", dirt);

        TextureData stone = new TextureData();
        stone.setColor("#888888");
        textureMap.put("minecraft:block/stone", stone);

        TextureData water = new TextureData();
        water.setColor("#3355FF"); // This should also consider alpha for halfTransparent
        textureMap.put("minecraft:block/water", water);

        TextureData sand = new TextureData();
        sand.setColor("#FFFF55");
        textureMap.put("minecraft:block/sand", sand);

        TextureData gravel = new TextureData();
        gravel.setColor("#777777");
        textureMap.put("minecraft:block/gravel", gravel);

        TextureData log = new TextureData();
        log.setColor("#6B511B"); // Oak log example
        textureMap.put("minecraft:block/log_oak", log); // More specific key

        TextureData leaves = new TextureData();
        leaves.setColor("#3D7A3D"); // Oak leaves example
        textureMap.put("minecraft:block/leaves_oak", leaves); // More specific key
        
        plugin.getLogger().info("Initialized " + textureMap.size() + " default textures in textureMap.");
    }
    
    public static class TextureData {
        private String color; // Hex string e.g., "#RRGGBB"
        private String texture; // Base64 encoded string or path
        // You might want to add halfTransparent boolean here if it's part of your core texture data
        
        public TextureData() {
            this.color = "#808080";  // Default gray color
            this.texture = null;
        }
        
        public String getColor() {
            return color;
        }
        
        public void setColor(String color) {
            // Basic validation for hex color
            if (color != null && color.matches("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{3})$")) {
                this.color = color;
            } else {
                this.color = "#808080"; // Fallback
            }
        }
        
        public String getTexture() {
            return texture;
        }
        
        public void setTexture(String texture) {
            this.texture = texture;
        }
    }
} 