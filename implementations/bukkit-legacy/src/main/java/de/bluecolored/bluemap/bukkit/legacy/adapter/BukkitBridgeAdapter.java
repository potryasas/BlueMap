package de.bluecolored.bluemap.bukkit.legacy.adapter;

import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitPlugin;

public class BukkitBridgeAdapter {
    private final LegacyBukkitPlugin plugin;
    
    public BukkitBridgeAdapter(LegacyBukkitPlugin plugin) {
        this.plugin = plugin;
    }
    
    public LegacyBukkitPlugin getPlugin() {
        return plugin;
    }
    
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
} 