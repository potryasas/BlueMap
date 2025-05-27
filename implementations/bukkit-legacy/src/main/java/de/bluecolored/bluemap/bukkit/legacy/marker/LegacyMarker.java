package de.bluecolored.bluemap.bukkit.legacy.marker;

public class LegacyMarker {
    private final String id;
    private final String label;
    private final double x;
    private final double y;
    private final double z;
    private final String icon;
    private final String dimension;
    
    public LegacyMarker(String id, String label, double x, double y, double z, String icon, String dimension) {
        this.id = id;
        this.label = label;
        this.x = x;
        this.y = y;
        this.z = z;
        this.icon = icon;
        this.dimension = dimension;
    }
    
    public String getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getZ() {
        return z;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getDimension() {
        return dimension;
    }
} 