package de.bluecolored.bluemap.bukkit.legacy.java8compat;

public interface ServerWorld {
    String getName();
    String getId();
    String getWorldType();
    int getSeaLevel();
    int getMaxHeight();
    int getMinHeight();
    BlockState getBlockState(int x, int y, int z);
} 