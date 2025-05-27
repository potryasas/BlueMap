package de.bluecolored.bluemap.renderapi;

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TileRegion {
    private static final Logger logger = LoggerFactory.getLogger(TileRegion.class);
    private static final int TILE_SIZE = 32;
    private static final int REGION_SIZE = 32;
    
    private final Vector2i position;
    private final WorldConfig config;
    private final Map<Vector2i, ChunkData> chunks;
    private final List<BufferedImage> tiles;
    
    public TileRegion(Vector2i position, WorldConfig config) {
        this.position = position;
        this.config = config;
        this.chunks = new ConcurrentHashMap<>();
        this.tiles = new ArrayList<>();
    }
    
    public void addChunk(ChunkData chunk) {
        Vector2i chunkPos = new Vector2i(chunk.getX() & (REGION_SIZE - 1), chunk.getZ() & (REGION_SIZE - 1));
        chunks.put(chunkPos, chunk);
    }
    
    public void render(Path tilesDir) throws IOException {
        // Create region directory
        Path regionDir = tilesDir.resolve(position.x + "_" + position.y);
        regionDir.toFile().mkdirs();
        
        // Render each tile in the region
        for (int x = 0; x < REGION_SIZE; x++) {
            for (int z = 0; z < REGION_SIZE; z++) {
                renderTile(x, z, regionDir);
            }
        }
    }
    
    private void renderTile(int tileX, int tileZ, Path regionDir) throws IOException {
        BufferedImage image = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        
        try {
            // Set rendering hints
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Get chunk data for this tile
            ChunkData chunk = chunks.get(new Vector2i(tileX, tileZ));
            if (chunk != null && chunk.isGenerated()) {
                renderChunk(g, chunk);
            }
            
        } finally {
            g.dispose();
        }
        
        // Save tile image
        Path tilePath = regionDir.resolve(tileX + "_" + tileZ + ".png");
        ImageIO.write(image, "PNG", tilePath.toFile());
    }
    
    private void renderChunk(Graphics2D g, ChunkData chunk) {
        // Get block data
        Map<Vector3i, ChunkData.BlockData> blocks = chunk.getBlocks();
        
        // Render blocks
        for (Map.Entry<Vector3i, ChunkData.BlockData> entry : blocks.entrySet()) {
            Vector3i pos = entry.getKey();
            ChunkData.BlockData block = entry.getValue();
            
            // Calculate block position in tile
            int x = pos.x & (TILE_SIZE - 1);
            int z = pos.z & (TILE_SIZE - 1);
            
            // Get block color based on type and biome
            Color color = getBlockColor(block);
            
            // Apply lighting
            if (config.getRenderSettings().isAmbientOcclusion()) {
                float light = block.getLight() / 15.0f;
                color = applyLighting(color, light);
            }
            
            // Draw block
            g.setColor(color);
            g.fillRect(x, z, 1, 1);
        }
    }
    
    private Color getBlockColor(ChunkData.BlockData block) {
        // TODO: Implement proper block color mapping based on block type and properties
        return switch (block.getBlockId()) {
            case "minecraft:grass_block" -> new Color(34, 139, 34);
            case "minecraft:stone" -> new Color(128, 128, 128);
            case "minecraft:dirt" -> new Color(139, 69, 19);
            case "minecraft:water" -> new Color(0, 0, 255, 128);
            case "minecraft:sand" -> new Color(238, 232, 170);
            default -> new Color(128, 128, 128);
        };
    }
    
    private Color applyLighting(Color base, float light) {
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2] * light);
    }
    
    public Vector2i getPosition() {
        return position;
    }
} 