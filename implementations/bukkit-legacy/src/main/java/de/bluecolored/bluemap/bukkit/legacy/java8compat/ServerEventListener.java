package de.bluecolored.bluemap.bukkit.legacy.java8compat;

public interface ServerEventListener {
    void onPlayerJoined(Player player);
    void onPlayerLeft(Player player);
    void onPlayerMoved(Player player);
    void onWorldLoad(ServerWorld world);
    void onWorldUnload(ServerWorld world);
    void onChunkModified(ServerWorld world, int chunkX, int chunkZ);
} 