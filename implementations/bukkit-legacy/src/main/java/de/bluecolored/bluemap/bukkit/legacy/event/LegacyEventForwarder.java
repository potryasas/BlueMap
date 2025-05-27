package de.bluecolored.bluemap.bukkit.legacy.event;

import java.util.ArrayList;
import java.util.List;

import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitPlugin;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.ServerEventListener;

public class LegacyEventForwarder {
    private final LegacyBukkitPlugin plugin;
    private final List<ServerEventListener> listeners;
    
    public LegacyEventForwarder(LegacyBukkitPlugin plugin) {
        this.plugin = plugin;
        this.listeners = new ArrayList<>();
    }
    
    public void addListener(ServerEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(ServerEventListener listener) {
        listeners.remove(listener);
    }
    
    public void removeAllListeners() {
        listeners.clear();
    }
    
    public List<ServerEventListener> getListeners() {
        return new ArrayList<>(listeners);
    }
} 