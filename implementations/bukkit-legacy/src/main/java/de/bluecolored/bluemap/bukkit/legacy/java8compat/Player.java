package de.bluecolored.bluemap.bukkit.legacy.java8compat;

public interface Player {
    String getName();
    String getUUID();
    ServerWorld getWorld();
    double getX();
    double getY();
    double getZ();
    float getYaw();
    float getPitch();
} 