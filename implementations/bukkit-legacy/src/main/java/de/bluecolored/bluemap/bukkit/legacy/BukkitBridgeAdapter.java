package de.bluecolored.bluemap.bukkit.legacy;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class BukkitBridgeAdapter {
    private final LegacyBukkitPlugin plugin;
    private final Server server;

    public BukkitBridgeAdapter(LegacyBukkitPlugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

    public LegacyBukkitPlugin getPlugin() {
        return plugin;
    }

    public Server getServer() {
        return server;
    }
} 