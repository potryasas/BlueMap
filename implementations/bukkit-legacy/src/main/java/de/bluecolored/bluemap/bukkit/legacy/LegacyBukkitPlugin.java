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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

import de.bluecolored.bluemap.bukkit.legacy.java8compat.BlueMapStub;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.Logger;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.PluginImplementation;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.ServerWorld;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.ServerEventListener;
import de.bluecolored.bluemap.bukkit.legacy.java8compat.legacy.LegacyResourcePackExtensionType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtensionType;
import de.bluecolored.bluemap.bukkit.legacy.web.WebManager;
import de.bluecolored.bluemap.bukkit.legacy.texture.LegacyTextureManager;
import de.bluecolored.bluemap.bukkit.legacy.player.PlayerDataManager;
import de.bluecolored.bluemap.bukkit.legacy.render.LegacyRenderer;
import de.bluecolored.bluemap.bukkit.legacy.adapter.BukkitBridgeAdapter;
import de.bluecolored.bluemap.bukkit.legacy.adapter.LegacyBukkitServerAdapter;
import de.bluecolored.bluemap.bukkit.legacy.event.LegacyEventForwarder;
import de.bluecolored.bluemap.bukkit.legacy.marker.LegacyMarkerManager;
import de.bluecolored.bluemap.bukkit.legacy.marker.LegacyMarker;
import de.bluecolored.bluemap.bukkit.legacy.marker.LegacyMarkerSet;

public class LegacyBukkitPlugin extends JavaPlugin implements Listener {

    private static LegacyBukkitPlugin instance;
    
    private final LegacyBukkitServerAdapter serverAdapter;
    private final BukkitBridgeAdapter bridgeAdapter;
    private final LegacyEventForwarder eventForwarder;
    private final Map<String, de.bluecolored.bluemap.bukkit.legacy.java8compat.Player> onlinePlayerMap;
    private final Map<String, ServerWorld> worldMap;
    private Path webRoot;
    private final Path worldFolder;
    
    private BlueMapStub blueMap;
    private LegacyMarkerManager markerManager;
    private WebManager webManager;
    private LegacyTextureManager textureManager;
    private LegacyRenderer renderer;
    
    private de.bluecolored.bluemap.bukkit.legacy.java8compat.Plugin pluginDelegate;
    
    private PlayerDataManager playerManager;
    
    private final ConcurrentHashMap<World, ConcurrentLinkedQueue<org.bukkit.Chunk>> pendingChunks;
    private final int BATCH_SIZE = 16;
    private final int RENDER_DISTANCE = 10;
    
    public LegacyBukkitPlugin() {
        instance = this;
        this.worldMap = new ConcurrentHashMap<>();
        this.onlinePlayerMap = new ConcurrentHashMap<>();
        this.eventForwarder = new LegacyEventForwarder(this);
        this.worldFolder = new File(getDataFolder(), "world").toPath();
        this.pendingChunks = new ConcurrentHashMap<>();
        
        // Initialize data folder properly
        File dataFolder = getDataFolder();
        if (dataFolder == null) {
            dataFolder = new File("plugins/BlueMap");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            System.out.println("[BlueMap] Using fallback data folder: " + dataFolder.getAbsolutePath());
        }
        
        // Initialize adapters after data folder setup
        this.serverAdapter = new LegacyBukkitServerAdapter(super.getServer(), this);
        this.bridgeAdapter = new BukkitBridgeAdapter(this);
    }
    
    @Override
    public void onLoad() {
        // Initialize plugin delegate with server adapter
        this.pluginDelegate = new PluginImplementation(serverAdapter, getDescription());
    }
    
    @Override
    public void onEnable() {
        try {
            instance = this;
            
            // Create web root directory
            webRoot = getDataFolder().toPath().resolve("web");
            webRoot.toFile().mkdirs();
            
            // Initialize managers
            textureManager = new LegacyTextureManager(this);
            playerManager = new PlayerDataManager(this, webRoot);
            renderer = new LegacyRenderer(this);
            
            // Initialize web manager and start web server
            webManager = new WebManager(this);
            webManager.start();
            
            // Initialize marker manager
            markerManager = new LegacyMarkerManager(this);
            markerManager.load();
            
            // Register event listener
            getServer().getPluginManager().registerEvents(this, this);
            
            // Register all worlds
            for (World world : getServer().getWorlds()) {
                registerWorld(world);
            }
            
            getLogger().info("BlueMap Legacy plugin enabled!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable BlueMap: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable() {
        if (webManager != null) {
            webManager.stop();
        }
        
        if (blueMap != null) {
            try {
                // Stop BlueMap
                getLogger().info("Stopping BlueMap...");
                blueMap.stop();
                getLogger().info("BlueMap stopped.");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Error stopping BlueMap", e);
            }
        }
        unregisterAllListeners();
        onlinePlayerMap.clear();
        worldMap.clear();
        getLogger().info("BlueMap Legacy plugin disabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("bluemap")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("render")) {
                    if (!sender.hasPermission("bluemap.render")) {
                        sender.sendMessage("§cYou don't have permission to use this command");
                        return true;
                    }

                    int radius = 8; // Default radius
                    if (args.length > 1) {
                        try {
                            radius = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("§cInvalid radius. Using default radius of 8 chunks.");
                        }
                    }

                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        renderer.queueChunksAround(player.getLocation(), radius);
                        sender.sendMessage("§aQueued chunks for rendering in a radius of " + radius + " chunks around you.");
                    } else {
                        World world = Bukkit.getWorlds().get(0);
                        renderer.queueChunksAround(world.getSpawnLocation(), radius);
                        sender.sendMessage("§aQueued chunks for rendering in a radius of " + radius + " chunks around world spawn.");
                    }
                    return true;
                }
                
                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("bluemap.reload")) {
                        sender.sendMessage("§cYou don't have permission to reload BlueMap");
                        return true;
                    }
                    
                    // Reload
                        try {
                            sender.sendMessage("§aStarting BlueMap reload...");
                        onDisable();
                        onEnable();
                        sender.sendMessage("§aBlueMap has been reloaded!");
                        } catch (Exception e) {
                        sender.sendMessage("§cFailed to reload BlueMap: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            
            sender.sendMessage("§eBlueMap Commands:");
            sender.sendMessage("§e/bluemap render [radius] §7- Render chunks around you or world spawn");
            sender.sendMessage("§e/bluemap reload §7- Reload BlueMap");
            return true;
        }
        return false;
    }
    
    private void setupLogger() {
        // Set up global logger implementation
        Logger blueLogger = new Logger() {
            @Override
            public void logInfo(String message) {
                getLogger().info(message);
            }
            
            @Override
            public void logWarning(String message) {
                getLogger().warning(message);
            }
            
            @Override
            public void logError(String message) {
                getLogger().severe(message);
            }
            
            @Override
            public void logError(String message, Throwable throwable) {
                getLogger().log(Level.SEVERE, message, throwable);
            }
            
            @Override
            public void logDebug(String message) {
                if (getConfig().getBoolean("debug", false)) {
                    getLogger().info("[DEBUG] " + message);
                }
            }
        };
        
        // Set the global logger instance for BlueMap to use
        de.bluecolored.bluemap.bukkit.legacy.java8compat.LoggerImplementation.setGlobalLogger(blueLogger);
    }
    
    private void loadWorlds() {
        getLogger().info("=== LOADING WORLDS ===");
        Collection<World> allWorlds = Bukkit.getWorlds();
        getLogger().info("Total worlds found: " + allWorlds.size());
        
        if (allWorlds.isEmpty()) {
            getLogger().warning("No worlds found! Waiting for worlds to load...");
            // Wait a bit for worlds to load
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            allWorlds = Bukkit.getWorlds();
            getLogger().info("After waiting, total worlds found: " + allWorlds.size());
        }
        
        for (World world : allWorlds) {
            getLogger().info("Found world: " + world.getName() + " (type: " + world.getEnvironment() + ")");
            addWorld(world);
        }
        getLogger().info("Finished loading " + worldMap.size() + " worlds");
        getLogger().info("WorldMap contents: " + worldMap.keySet());
        getLogger().info("======================");
    }
    
    private void addWorld(World world) {
        String worldName = world.getName().toLowerCase();
        LegacyBukkitWorld serverWorld = new LegacyBukkitWorld(world);
        worldMap.put(worldName, serverWorld);
        getLogger().info("Registered world: " + world.getName() + " -> " + worldName + " (ID: " + serverWorld.getId() + ")");
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        LegacyBukkitWorld blueWorld = new LegacyBukkitWorld(world);
        
        // Создаем игрока с учетом его измерения
        LegacyBukkitPlayer blueMapPlayer = new LegacyBukkitPlayer(player, blueWorld);
        onlinePlayerMap.put(player.getName().toLowerCase(), blueMapPlayer);
        
        // Обновляем данные игрока в PlayerDataManager
        playerManager.updatePlayerData(player, getDimensionType(world.getEnvironment()));
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        onlinePlayerMap.remove(player.getName().toLowerCase());
    }
    
    public void registerListener(ServerEventListener listener) {
        eventForwarder.addListener(listener);
    }
    
    public void unregisterAllListeners() {
        eventForwarder.removeAllListeners();
    }
    
    public void unregisterListener(ServerEventListener listener) {
        eventForwarder.removeListener(listener);
    }
    
    public Collection<ServerWorld> getLoadedServerWorlds() {
        return new ArrayList<>(worldMap.values());
    }
    
    public Optional<ServerWorld> getServerWorld(Object world) {
        if (world instanceof org.bukkit.World) {
            return Optional.ofNullable(worldMap.get(((org.bukkit.World) world).getName().toLowerCase()));
        }
        return Optional.empty();
    }
    
    public de.bluecolored.bluemap.bukkit.legacy.java8compat.Plugin getPluginDelegate() {
        return pluginDelegate;
    }
    
    public LegacyBukkitServerAdapter getServerAdapter() {
        return serverAdapter;
    }
    
    public BukkitBridgeAdapter getBukkitBridge() {
        return bridgeAdapter;
    }
    
    public WebManager getWebManager() {
        return webManager;
    }
    
    public static LegacyBukkitPlugin getInstance() {
        return instance;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("reload");
            completions.add("render");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("render")) {
            completions.add("16"); // Default suggestion
            completions.add("32");
            completions.add("64");
        }
        
        return completions;
    }

    public Path getWebRoot() {
        return webRoot;
    }

    public Path getWorldFolder() {
        return worldFolder;
    }

    public String getWorldName() {
        List<World> worlds = new ArrayList<>(getServer().getWorlds());
        if (worlds.isEmpty()) {
            getLogger().warning("No worlds found! Using 'world' as default name.");
            return "world";
        }
        return worlds.get(0).getName();
    }

    public Collection<de.bluecolored.bluemap.bukkit.legacy.java8compat.Player> getOnlinePlayers() {
        return new ArrayList<>(onlinePlayerMap.values());
    }

    public LegacyMarkerManager getMarkerManager() {
        return markerManager;
    }

    public LegacyTextureManager getTextureManager() {
        return textureManager;
    }

    public PlayerDataManager getPlayerManager() {
        return playerManager;
    }

    private void registerWorld(World world) {
        pendingChunks.put(world, new ConcurrentLinkedQueue<>());
        getLogger().info("Registered world: " + world.getName());
    }

    private String getDimensionType(World.Environment env) {
        switch (env) {
            case NETHER:
                return "nether";
            case THE_END:
                return "the_end";
            default:
                return "overworld";
        }
    }

    public void saveMarkers() {
        if (markerManager != null) {
            markerManager.saveMarkers();
            markerManager.updateWebMap();
        }
    }
} 
