/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.bukkit.legacy;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.bluecolored.bluemap.bukkit.legacy.java8compat.ServerEventListener;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.ServerWorld;

/**
 * Forwards Bukkit events to BlueMap's event listeners
 */
public class LegacyEventForwarder implements Listener {
    
    private final LegacyBukkitPlugin plugin;
    private final Set<ServerEventListener> listeners;
    
    public LegacyEventForwarder(LegacyBukkitPlugin plugin) {
        this.plugin = plugin;
        this.listeners = new HashSet<>();
    }
    
    public void addListener(ServerEventListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(ServerEventListener listener) {
        listeners.remove(listener);
    }
    
    public void removeAllListeners() {
        listeners.clear();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        notifyBlockChange(event.getBlock().getWorld(), event.getBlock().getX() >> 4, event.getBlock().getZ() >> 4);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        notifyBlockChange(event.getBlock().getWorld(), event.getBlock().getX() >> 4, event.getBlock().getZ() >> 4);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Ignore movements within the same block
        }
        
        notifyPlayerMove(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        notifyPlayerJoin(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        notifyPlayerQuit(event.getPlayer());
    }
    
    private void notifyBlockChange(org.bukkit.World world, int chunkX, int chunkZ) {
        LegacyBukkitWorld serverWorld = getServerWorld(world);
        if (serverWorld == null) return;
        
        for (ServerEventListener listener : listeners) {
            try {
                listener.onChunkModified(serverWorld, chunkX, chunkZ);
            } catch (Exception e) {
                plugin.getLogger().severe("Error notifying listener about block change: " + e.getMessage());
            }
        }
    }
    
    private void notifyPlayerMove(Player player) {
        LegacyBukkitWorld world = getServerWorld(player.getWorld());
        if (world == null) return;
        
        LegacyBukkitPlayer blueMapPlayer = new LegacyBukkitPlayer(player, world);
        
        for (ServerEventListener listener : listeners) {
            try {
                listener.onPlayerMoved(blueMapPlayer);
            } catch (Exception e) {
                plugin.getLogger().severe("Error notifying listener about player movement: " + e.getMessage());
            }
        }
    }
    
    private void notifyPlayerJoin(Player player) {
        LegacyBukkitWorld world = getServerWorld(player.getWorld());
        if (world == null) return;
        
        LegacyBukkitPlayer blueMapPlayer = new LegacyBukkitPlayer(player, world);
        
        for (ServerEventListener listener : listeners) {
            try {
                listener.onPlayerJoined(blueMapPlayer);
            } catch (Exception e) {
                plugin.getLogger().severe("Error notifying listener about player join: " + e.getMessage());
            }
        }
    }
    
    private void notifyPlayerQuit(Player player) {
        LegacyBukkitWorld world = getServerWorld(player.getWorld());
        if (world == null) return;
        
        LegacyBukkitPlayer blueMapPlayer = new LegacyBukkitPlayer(player, world);
        
        for (ServerEventListener listener : listeners) {
            try {
                listener.onPlayerLeft(blueMapPlayer);
            } catch (Exception e) {
                plugin.getLogger().severe("Error notifying listener about player quit: " + e.getMessage());
            }
        }
    }
    
    private LegacyBukkitWorld getServerWorld(org.bukkit.World world) {
        String worldName = world.getName().toLowerCase();
        ServerWorld serverWorld = plugin.getLoadedServerWorlds()
                .stream()
                .filter(w -> w.getId().equals(worldName))
                .findFirst()
                .orElse(null);
                
        if (serverWorld instanceof LegacyBukkitWorld) {
            return (LegacyBukkitWorld) serverWorld;
        }
        
        return null;
    }
} 
