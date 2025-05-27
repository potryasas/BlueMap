package de.bluecolored.bluemap.bukkit.legacy.marker;

public class LegacyMarkerSet {
    private final String id;
    private final String label;
    
    public LegacyMarkerSet(String id, String label) {
        this.id = id;
        this.label = label;
    }
    
    public String getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
} 