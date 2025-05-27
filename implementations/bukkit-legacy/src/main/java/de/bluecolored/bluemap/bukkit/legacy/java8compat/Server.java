package de.bluecolored.bluemap.bukkit.legacy.java8compat;

import java.util.Collection;
import java.util.Optional;

public interface Server {
    Collection<ServerWorld> getLoadedServerWorlds();
    Collection<Player> getOnlinePlayers();
    void registerListener(ServerEventListener listener);
    void unregisterListener(ServerEventListener listener);
    Optional<ServerWorld> getServerWorld(Object world);
    Plugin getPlugin();
} 