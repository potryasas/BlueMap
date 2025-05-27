package de.bluecolored.bluemap.bukkit.legacy.java8compat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.World;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.GsonBuilder;

import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitPlugin;
import de.bluecolored.bluemap.bukkit.legacy.render.LegacyRenderer;

public class BlueMapStub {
    private final Plugin plugin;
    private final LegacyRenderer renderer;
    private final Path webRoot;
    private boolean running;
    
    public BlueMapStub(Plugin plugin) {
        this.plugin = plugin;
        this.renderer = new LegacyRenderer(LegacyBukkitPlugin.getInstance());
        this.webRoot = new File(plugin.getDataFolder(), "web").toPath();
        this.running = false;
    }
    
    public void start() throws IOException {
        if (running) {
            return;
        }
        
        // Create web directory if it doesn't exist
        Files.createDirectories(webRoot);
        
        // Start periodic rendering
        startPeriodicRendering();
        
        running = true;
    }
    
    public void stop() {
        if (!running) {
            return;
        }
        
        // Stop renderer
        renderer.shutdown();
        
        running = false;
    }
    
    public void forceRenderArea(ServerWorld world, int centerX, int centerZ, int radius) {
        if (!running) {
            return;
        }
        
        // Convert ServerWorld to Bukkit World
        World bukkitWorld = LegacyBukkitPlugin.getInstance().getServer().getWorld(world.getName());
        if (bukkitWorld != null) {
            renderer.renderWorld(bukkitWorld, centerX, centerZ, radius);
        }
    }
    
    public void checkAndRenderWorld(ServerWorld world) {
        if (!running) {
            return;
        }
        
        // Convert ServerWorld to Bukkit World
        World bukkitWorld = LegacyBukkitPlugin.getInstance().getServer().getWorld(world.getName());
        if (bukkitWorld != null) {
            // Get spawn location
            int spawnX = bukkitWorld.getSpawnLocation().getBlockX();
            int spawnZ = bukkitWorld.getSpawnLocation().getBlockZ();
            
            // Render spawn area
            renderer.renderWorld(bukkitWorld, spawnX, spawnZ, 256);
        }
    }
    
    private void startPeriodicRendering() {
        LegacyBukkitPlugin.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(
            LegacyBukkitPlugin.getInstance(),
            () -> {
                try {
                    // Update live data for each world
                    for (World world : LegacyBukkitPlugin.getInstance().getServer().getWorlds()) {
                        // Create live data object
                        JsonObject liveData = new JsonObject();
                        
                        // Add player positions
                        JsonArray playersArray = new JsonArray();
                        
                        world.getPlayers().forEach(player -> {
                            JsonObject playerData = new JsonObject();
                            playerData.addProperty("uuid", player.getUniqueId().toString());
                            playerData.addProperty("name", player.getName());
                            playerData.addProperty("foreign", false);
                            playerData.addProperty("dimension", player.getWorld().getName());
                            
                            JsonObject position = new JsonObject();
                            position.addProperty("x", player.getLocation().getX());
                            position.addProperty("y", player.getLocation().getY());
                            position.addProperty("z", player.getLocation().getZ());
                            playerData.add("position", position);
                            
                            JsonObject rotation = new JsonObject();
                            rotation.addProperty("pitch", player.getLocation().getPitch());
                            rotation.addProperty("yaw", player.getLocation().getYaw());
                            rotation.addProperty("roll", 0);
                            playerData.add("rotation", rotation);
                            
                            playersArray.add(playerData);
                            
                            // Debug log
                            LegacyBukkitPlugin.getInstance().getLogger().info("Updating player " + player.getName() + " at " + player.getLocation());
                        });
                        
                        // Create the full players data structure
                        JsonObject playersData = new JsonObject();
                        playersData.add("players", playersArray);
                        playersData.addProperty("updateInterval", 1000); // Update every second
                        playersData.addProperty("showPlayerMarkers", true);
                        playersData.addProperty("showPlayerBody", true);
                        playersData.addProperty("showPlayerHead", true);
                        playersData.addProperty("showLabelBackground", true);
                        playersData.addProperty("markerSetId", "players");
                        
                        JsonObject markerSet = new JsonObject();
                        markerSet.addProperty("id", "players");
                        markerSet.addProperty("label", "Players");
                        markerSet.addProperty("toggleable", true);
                        markerSet.addProperty("defaultHidden", false);
                        markerSet.addProperty("priority", 1000);
                        playersData.add("markerSet", markerSet);
                        
                        // Add the players data to live data
                        liveData.add("players", playersData);
                        
                        // Update live data file
                        Path liveDir = LegacyBukkitPlugin.getInstance().getWebRoot()
                            .resolve("maps")
                            .resolve(world.getName())
                            .resolve("live");
                        Files.createDirectories(liveDir);
                        
                        Path playersFile = liveDir.resolve("players.json");
                        try (FileWriter writer = new FileWriter(playersFile.toFile())) {
                            new GsonBuilder().setPrettyPrinting().create().toJson(playersData, writer);
                            writer.flush();
                        }
                        
                        // Debug log
                        LegacyBukkitPlugin.getInstance().getLogger().info("Updated players.json for world " + world.getName());
                        
                    }
                } catch (Exception e) {
                    LegacyBukkitPlugin.getInstance().getLogger().log(Level.WARNING, "Failed to update player positions", e);
                }
            }, 0L, 20L); // Run every tick (20 ticks = 1 second)
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public Plugin getPlugin() {
        return plugin;
    }
} 