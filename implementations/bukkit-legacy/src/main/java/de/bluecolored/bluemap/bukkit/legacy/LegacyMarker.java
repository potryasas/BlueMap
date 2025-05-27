/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 */
package de.bluecolored.bluemap.bukkit.legacy;

import java.util.Objects;

/**
 * Представляет маркер на карте
 */
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LegacyMarker that = (LegacyMarker) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.z, z) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(label, that.label) &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(dimension, that.dimension);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, label, x, y, z, icon, dimension);
    }
    
    @Override
    public String toString() {
        return "LegacyMarker{id='" + id + "', label='" + label + "', pos=(" + x + "," + y + "," + z + "), icon='" + icon + "', dimension='" + dimension + "'}";
    }
} 