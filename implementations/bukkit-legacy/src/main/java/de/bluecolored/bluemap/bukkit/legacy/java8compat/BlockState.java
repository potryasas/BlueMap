package de.bluecolored.bluemap.bukkit.legacy.java8compat;

import java.util.Map;

public interface BlockState {
    String getId();
    Map<String, BlockProperty> getProperties();
    boolean isAir();
} 