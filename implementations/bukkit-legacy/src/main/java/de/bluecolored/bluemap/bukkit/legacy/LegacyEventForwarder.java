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

import de.bluecolored.bluemap.common.serverinterface.ServerEventListener;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class LegacyEventForwarder implements Listener {

    private final Set<ServerEventListener> listeners;

    public LegacyEventForwarder() {
        this.listeners = new HashSet<ServerEventListener>();
    }

    public void addListener(ServerEventListener listener) {
        listeners.add(listener);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent evt) {
        World world = evt.getWorld();
        LegacyBukkitWorld serverWorld = LegacyBukkitPlugin.getInstance().getServerWorld(world);
        
        for (ServerEventListener listener : listeners) {
            try {
                listener.onWorldLoad(serverWorld);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent evt) {
        World world = evt.getWorld();
        LegacyBukkitWorld serverWorld = LegacyBukkitPlugin.getInstance().getServerWorld(world);
        
        for (ServerEventListener listener : listeners) {
            try {
                listener.onWorldUnload(serverWorld);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent evt) {
        if (!evt.isNewChunk()) return;
        
        World world = evt.getWorld();
        LegacyBukkitWorld serverWorld = LegacyBukkitPlugin.getInstance().getServerWorld(world);
        int chunkX = evt.getChunk().getX();
        int chunkZ = evt.getChunk().getZ();
        
        for (ServerEventListener listener : listeners) {
            try {
                listener.onChunkCreated(serverWorld, chunkX, chunkZ);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent evt) {
        updateBlock(evt.getBlock().getWorld(), evt.getBlock().getX(), evt.getBlock().getY(), evt.getBlock().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent evt) {
        updateBlock(evt.getBlock().getWorld(), evt.getBlock().getX(), evt.getBlock().getY(), evt.getBlock().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent evt) {
        updateBlock(evt.getBlock().getWorld(), evt.getBlock().getX(), evt.getBlock().getY(), evt.getBlock().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent evt) {
        updateBlock(evt.getBlock().getWorld(), evt.getBlock().getX(), evt.getBlock().getY(), evt.getBlock().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent evt) {
        updateBlock(evt.getBlock().getWorld(), evt.getBlock().getX(), evt.getBlock().getY(), evt.getBlock().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent evt) {
        updateBlock(evt.getBlock().getWorld(), evt.getBlock().getX(), evt.getBlock().getY(), evt.getBlock().getZ());
        updateBlock(evt.getToBlock().getWorld(), evt.getToBlock().getX(), evt.getToBlock().getY(), evt.getToBlock().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent evt) {
        updateBlock(evt.getBlock().getWorld(), evt.getBlock().getX(), evt.getBlock().getY(), evt.getBlock().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent evt) {
        updateBlock(evt.getBlock().getWorld(), evt.getBlock().getX(), evt.getBlock().getY(), evt.getBlock().getZ());
        updateBlock(evt.getSource().getWorld(), evt.getSource().getX(), evt.getSource().getY(), evt.getSource().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent evt) {
        updateBlock(evt.getBlock().getWorld(), evt.getBlock().getX(), evt.getBlock().getY(), evt.getBlock().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent evt) {
        updateBlock(evt.getLocation().getWorld(), evt.getLocation().getBlockX(), evt.getLocation().getBlockY(), evt.getLocation().getBlockZ());
        
        for (org.bukkit.block.BlockState block : evt.getBlocks()) {
            updateBlock(block.getWorld(), block.getX(), block.getY(), block.getZ());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent evt) {
        for (org.bukkit.block.Block block : evt.blockList()) {
            updateBlock(block.getWorld(), block.getX(), block.getY(), block.getZ());
        }
    }

    private void updateBlock(World world, int x, int y, int z) {
        LegacyBukkitWorld serverWorld = LegacyBukkitPlugin.getInstance().getServerWorld(world);
        
        for (ServerEventListener listener : listeners) {
            try {
                listener.onBlockChanged(serverWorld, x, y, z);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
} 