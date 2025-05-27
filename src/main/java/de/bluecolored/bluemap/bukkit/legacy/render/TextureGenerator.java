package de.bluecolored.bluemap.bukkit.legacy.render;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextureGenerator {
    private static final Pattern RGBA_PATTERN = Pattern.compile("RGBA\\((\\d+),\\s*(\\d+),\\s*(\\d+),\\s*([\\d.]+)\\)");
    private static final Pattern FLOAT_ARRAY_PATTERN = Pattern.compile("\\[([\\.\\d]+),\\s*([\\.\\d]+),\\s*([\\.\\d]+),\\s*([\\.\\d]+)\\]");
    
    private final Map<String, float[]> blockColors = new HashMap<>();
    private final Path texturesJsonPath;
    
    public TextureGenerator(Path texturesJsonPath) {
        this.texturesJsonPath = texturesJsonPath;
        loadBlockColors();
    }
    
    private void loadBlockColors() {
        try (BufferedReader reader = new BufferedReader(new FileReader("blocks.txt"))) {
            String line;
            String currentBlock = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (!line.startsWith("RGBA") && !line.startsWith("[")) {
                    currentBlock = line;
                    continue;
                }
                
                if (currentBlock != null) {
                    float[] color = null;
                    
                    // Try parsing RGBA format
                    Matcher rgbaMatcher = RGBA_PATTERN.matcher(line);
                    if (rgbaMatcher.find()) {
                        color = new float[] {
                            Integer.parseInt(rgbaMatcher.group(1)) / 255f,
                            Integer.parseInt(rgbaMatcher.group(2)) / 255f,
                            Integer.parseInt(rgbaMatcher.group(3)) / 255f,
                            Float.parseFloat(rgbaMatcher.group(4))
                        };
                    }
                    
                    // Try parsing float array format
                    Matcher floatMatcher = FLOAT_ARRAY_PATTERN.matcher(line);
                    if (floatMatcher.find()) {
                        color = new float[] {
                            Float.parseFloat(floatMatcher.group(1)),
                            Float.parseFloat(floatMatcher.group(2)),
                            Float.parseFloat(floatMatcher.group(3)),
                            Float.parseFloat(floatMatcher.group(4))
                        };
                    }
                    
                    if (color != null) {
                        blockColors.put(currentBlock, color);
                        currentBlock = null;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void generateTexturesJson() throws IOException {
        JsonArray texturesArray = new JsonArray();
        
        // Add missing texture
        float[] missingColor = blockColors.get("bluemap:block/missing");
        if (missingColor != null) {
            JsonObject missingTexture = createTextureObject(
                "bluemap:missing",
                missingColor,
                false,
                generateTextureBase64(missingColor)
            );
            texturesArray.add(missingTexture);
        }
        
        // Add all block textures
        for (Map.Entry<String, float[]> entry : blockColors.entrySet()) {
            if (entry.getKey().equals("bluemap:block/missing")) continue;
            
            String resourcePath = entry.getKey();
            float[] color = entry.getValue();
            
            JsonObject texture = createTextureObject(
                resourcePath,
                color,
                color[3] < 1.0f,
                generateTextureBase64(color)
            );
            texturesArray.add(texture);
        }
        
        // Write textures.json
        try (Writer writer = Files.newBufferedWriter(texturesJsonPath)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(texturesArray, writer);
        }
    }
    
    private JsonObject createTextureObject(String resourcePath, float[] color, boolean halfTransparent, String textureBase64) {
        JsonObject texture = new JsonObject();
        texture.addProperty("resourcePath", resourcePath);
        
        JsonArray colorArray = new JsonArray();
        for (float value : color) {
            colorArray.add(value);
        }
        texture.add("color", colorArray);
        
        texture.addProperty("halfTransparent", halfTransparent);
        texture.addProperty("texture", textureBase64);
        
        return texture;
    }
    
    private String generateTextureBase64(float[] color) {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Create a simple texture pattern
        g2d.setColor(new Color(color[0], color[1], color[2], color[3]));
        g2d.fillRect(0, 0, 16, 16);
        
        // Add some shading for 3D effect
        g2d.setColor(new Color(0, 0, 0, 0.1f));
        g2d.fillRect(0, 0, 16, 8);
        g2d.setColor(new Color(1, 1, 1, 0.1f));
        g2d.fillRect(0, 8, 16, 8);
        
        g2d.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "PNG", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public Map<String, float[]> getBlockColors() {
        return blockColors;
    }
} 