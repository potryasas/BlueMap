package de.bluecolored.bluemap.bukkit.legacy.texture;

public class TextureData {
    private String color;
    private String texture;
    
    public TextureData() {
        this.color = "#808080";  // Default gray color
        this.texture = null;
    }
    
    public TextureData(String color, String texture) {
        this.color = color;
        this.texture = texture;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getTexture() {
        return texture;
    }
    
    public void setTexture(String texture) {
        this.texture = texture;
    }
} 