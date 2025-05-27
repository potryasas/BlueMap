/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 */
package de.bluecolored.bluemap.bukkit.legacy;

/**
 * Представляет категорию маркеров
 */
public class LegacyMarkerSet {
    private final String id;
    private final String label;
    private final boolean toggleable;
    private final boolean defaultHidden;
    
    public LegacyMarkerSet(String id, String label, boolean toggleable, boolean defaultHidden) {
        this.id = id;
        this.label = label;
        this.toggleable = toggleable;
        this.defaultHidden = defaultHidden;
    }
    
    public String getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public boolean isToggleable() {
        return toggleable;
    }
    
    public boolean isDefaultHidden() {
        return defaultHidden;
    }
    
    @Override
    public String toString() {
        return "LegacyMarkerSet{id='" + id + "', label='" + label + "', toggleable=" + toggleable + ", defaultHidden=" + defaultHidden + "}";
    }
} 