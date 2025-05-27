package de.bluecolored.bluemap.bukkit.legacy.adapter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitPlugin;
import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitWorld;
import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitPlayer;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.ServerWorld;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.ServerEventListener;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.ServerAdapter;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.Player;

/**
 * Adapter to bridge between Bukkit's Server and our custom Server interface
 */
public class LegacyBukkitServerAdapter implements ServerAdapter {
    private final Server bukkitServer;
    private final LegacyBukkitPlugin plugin;
    private final Collection<ServerEventListener> listeners;

    public LegacyBukkitServerAdapter(Server bukkitServer, LegacyBukkitPlugin plugin) {
        this.bukkitServer = bukkitServer;
        this.plugin = plugin;
        this.listeners = new HashSet<>();
    }

    @Override
    public Server getServer() {
        return bukkitServer;
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    public Collection<ServerWorld> getLoadedServerWorlds() {
        Set<ServerWorld> worlds = new HashSet<>();
        for (org.bukkit.World world : bukkitServer.getWorlds()) {
            worlds.add(new LegacyBukkitWorld(world));
        }
        return worlds;
    }

    public Collection<Player> getOnlinePlayers() {
        Set<Player> players = new HashSet<>();
        for (org.bukkit.entity.Player player : bukkitServer.getOnlinePlayers()) {
            players.add(new LegacyBukkitPlayer(player, new LegacyBukkitWorld(player.getWorld())));
        }
        return players;
    }

    public void registerListener(ServerEventListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(ServerEventListener listener) {
        listeners.remove(listener);
    }

    public Optional<ServerWorld> getServerWorld(Object world) {
        if (world instanceof org.bukkit.World) {
            return Optional.of(new LegacyBukkitWorld((org.bukkit.World) world));
        }
        return Optional.empty();
    }

    public LegacyBukkitPlugin getBukkitPlugin() {
        return plugin;
    }

    public static LegacyBukkitServerAdapter fromPlugin(Plugin plugin) {
        if (plugin instanceof LegacyBukkitPlugin) {
            return ((LegacyBukkitPlugin) plugin).getServerAdapter();
        }
        throw new IllegalArgumentException("Plugin must be an instance of LegacyBukkitPlugin");
    }
} 