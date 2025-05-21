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

import de.bluecolored.bluemap.common.plugin.Plugin;
import de.bluecolored.bluemap.common.serverinterface.Player;
import de.bluecolored.bluemap.common.serverinterface.Server;
import de.bluecolored.bluemap.common.serverinterface.ServerEventListener;
import de.bluecolored.bluemap.common.serverinterface.ServerWorld;
import de.bluecolored.bluemap.core.BlueMap;
import de.bluecolored.bluemap.core.logger.JavaLogger;
import de.bluecolored.bluemap.core.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LegacyBukkitPlugin extends JavaPlugin implements Server, Listener {

    private static LegacyBukkitPlugin instance;

    private final Plugin pluginInstance;
    private final LegacyEventForwarder eventForwarder;
    private final String minecraftVersion = "1.5.2";

    private final Map<String, Player> onlinePlayerMap;
    private final List<LegacyBukkitPlayer> onlinePlayerList;
    private final Map<World, LegacyBukkitWorld> worldMap;

    public LegacyBukkitPlugin() {
        Logger.global.clear();
        Logger.global.put(new JavaLogger(getLogger()));

        this.onlinePlayerMap = new ConcurrentHashMap<String, Player>();
        this.onlinePlayerList = Collections.synchronizedList(new ArrayList<LegacyBukkitPlayer>());
        this.worldMap = new ConcurrentHashMap<World, LegacyBukkitWorld>();

        this.eventForwarder = new LegacyEventForwarder();
        this.pluginInstance = new Plugin("bukkit-legacy", this);

        LegacyBukkitPlugin.instance = this;
    }

    @Override
    public void onEnable() {
        // Save all world data to ensure level.dat is present
        Logger.global.logInfo("Saving all worlds once, to make sure the level.dat is present...");
        for (World world : getServer().getWorlds()) {
            world.save();
        }

        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(eventForwarder, this);

        // Update player collections
        this.onlinePlayerList.clear();
        this.onlinePlayerMap.clear();
        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            LegacyBukkitPlayer bukkitPlayer = new LegacyBukkitPlayer(player);
            onlinePlayerMap.put(player.getName(), bukkitPlayer);
            onlinePlayerList.add(bukkitPlayer);
        }

        // Load BlueMap asynchronously
        getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    Logger.global.logInfo("Loading...");
                    pluginInstance.load();
                    if (pluginInstance.isLoaded()) Logger.global.logInfo("Loaded!");

                    // Start player update task
                    getServer().getScheduler().scheduleSyncRepeatingTask(LegacyBukkitPlugin.this, new Runnable() {
                        @Override
                        public void run() {
                            updatePlayers();
                        }
                    }, 20, 20);
                } catch (IOException | RuntimeException e) {
                    Logger.global.logError("Failed to load!", e);
                    pluginInstance.unload();
                }
            }
        }, 1L);
    }

    @Override
    public void onDisable() {
        Logger.global.logInfo("Stopping...");
        getServer().getScheduler().cancelTasks(this);
        pluginInstance.unload();
        Logger.global.logInfo("Saved and stopped!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("bluemap")) {
            if (args.length == 0) {
                sender.sendMessage("§9BlueMap §7v" + getDescription().getVersion());
                sender.sendMessage("§7Type '§f/bluemap help§7' for a list of commands");
                return true;
            }

            if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("bluemap.reload")) {
                sender.sendMessage("§7Reloading BlueMap...");
                
                final CommandSender finalSender = sender;
                getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pluginInstance.reload();
                            finalSender.sendMessage("§aBlueMap reloaded!");
                        } catch (Exception e) {
                            finalSender.sendMessage("§cFailed to reload BlueMap! Check console for errors.");
                            Logger.global.logError("Failed to reload:", e);
                        }
                    }
                });
                return true;
            }

            if (args[0].equalsIgnoreCase("stop") && sender.hasPermission("bluemap.stop")) {
                sender.sendMessage("§7Stopping all BlueMap renders...");
                pluginInstance.stopRender();
                sender.sendMessage("§aAll renders stopped!");
                return true;
            }

            if (args[0].equalsIgnoreCase("render") && sender.hasPermission("bluemap.render")) {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /bluemap render <world> [options]");
                    return true;
                }

                String worldName = args[1];
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    sender.sendMessage("§cWorld not found: " + worldName);
                    return true;
                }

                sender.sendMessage("§7Starting render for world: " + worldName);
                final CommandSender finalSender = sender;
                final World finalWorld = world;
                
                getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LegacyBukkitWorld serverWorld = getServerWorld(finalWorld);
                            pluginInstance.render(serverWorld);
                            finalSender.sendMessage("§aRender started for world: " + finalWorld.getName());
                        } catch (Exception e) {
                            finalSender.sendMessage("§cFailed to start render! Check console for errors.");
                            Logger.global.logError("Failed to start render:", e);
                        }
                    }
                });
                return true;
            }

            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage("§9BlueMap Commands:");
                sender.sendMessage("§f/bluemap reload §7- Reload the plugin");
                sender.sendMessage("§f/bluemap stop §7- Stop all running renders");
                sender.sendMessage("§f/bluemap render <world> §7- Start a render for a world");
                return true;
            }

            sender.sendMessage("§cUnknown command. Type '/bluemap help' for help.");
            return true;
        }
        return false;
    }

    @Override
    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    @Override
    public void registerListener(ServerEventListener listener) {
        eventForwarder.addListener(listener);
    }

    @Override
    public void unregisterAllListeners() {
        eventForwarder.removeAllListeners();
    }

    @Override
    public Collection<ServerWorld> getLoadedServerWorlds() {
        Collection<ServerWorld> loadedWorlds = new ArrayList<ServerWorld>();
        for (World world : Bukkit.getWorlds()) {
            loadedWorlds.add(getServerWorld(world));
        }
        return loadedWorlds;
    }

    @Override
    public Optional<ServerWorld> getServerWorld(Object world) {
        if (world instanceof String) {
            World bukkitWorld = Bukkit.getWorld((String) world);
            if (bukkitWorld != null) world = bukkitWorld;
        }

        if (world instanceof World)
            return Optional.of(getServerWorld((World) world));

        return Optional.empty();
    }

    public LegacyBukkitWorld getServerWorld(World world) {
        LegacyBukkitWorld serverWorld = worldMap.get(world);
        if (serverWorld == null) {
            serverWorld = new LegacyBukkitWorld(world);
            worldMap.put(world, serverWorld);
        }
        return serverWorld;
    }

    @Override
    public Path getConfigFolder() {
        return Paths.get(getDataFolder().getAbsolutePath());
    }

    @Override
    public Optional<Path> getModsFolder() {
        return Optional.of(Paths.get("mods"));
    }

    public Plugin getPlugin() {
        return pluginInstance;
    }

    public static LegacyBukkitPlugin getInstance() {
        return instance;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent evt) {
        LegacyBukkitPlayer player = new LegacyBukkitPlayer(evt.getPlayer());
        onlinePlayerMap.put(evt.getPlayer().getName(), player);
        onlinePlayerList.add(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent evt) {
        onlinePlayerMap.remove(evt.getPlayer().getName());
        
        Iterator<LegacyBukkitPlayer> it = onlinePlayerList.iterator();
        while (it.hasNext()) {
            LegacyBukkitPlayer player = it.next();
            if (player.getName().equals(evt.getPlayer().getName())) {
                it.remove();
                break;
            }
        }
    }

    @Override
    public Collection<Player> getOnlinePlayers() {
        return new ArrayList<Player>(onlinePlayerMap.values());
    }

    private void updatePlayers() {
        for (LegacyBukkitPlayer player : onlinePlayerList) {
            player.update();
        }
    }
} 