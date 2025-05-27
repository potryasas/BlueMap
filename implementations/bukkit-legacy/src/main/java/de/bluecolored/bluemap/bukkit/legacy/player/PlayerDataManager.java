package de.bluecolored.bluemap.bukkit.legacy.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.Plugin;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerDataManager implements Listener {
    private final Plugin plugin;
    private final Map<String, PlayerData> playerDataMap;
    private final Logger logger;
    private final Path webRoot;
    private long lastUpdateTime;
    private static final long UPDATE_INTERVAL = 1000; // 1 second

    public PlayerDataManager(Plugin plugin, Path webRoot) {
        this.plugin = plugin;
        this.webRoot = webRoot;
        this.logger = plugin.getLogger();
        this.playerDataMap = new ConcurrentHashMap<>();
        this.lastUpdateTime = System.currentTimeMillis();

        // Register events
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Start periodic updates
        startPeriodicUpdates();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = new PlayerData(player.getName(), player.getUniqueId(), player.getLocation(), getDimensionType(player.getWorld().getEnvironment()));
        playerDataMap.put(player.getName().toLowerCase(), data);
        updateLiveData();
        logger.info("[BlueMap] Player joined: " + player.getName() + " in dimension: " + data.getDimension());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerDataMap.remove(player.getName().toLowerCase());
        updateLiveData();
        logger.info("[BlueMap] Player quit: " + player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String dimension = getDimensionType(player.getWorld().getEnvironment());
        
        PlayerData data = playerDataMap.get(player.getName().toLowerCase());
        if (data != null) {
            data.updateLocation(player.getLocation());
            data.updateDimension(dimension);
            updateLiveData();
            logger.info("[BlueMap] Player " + player.getName() + " changed dimension to: " + dimension);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL) {
            Player player = event.getPlayer();
            PlayerData data = playerDataMap.get(player.getName().toLowerCase());
            if (data != null) {
                data.updateLocation(player.getLocation());
                updateLiveData();
                lastUpdateTime = currentTime;
            }
        }
    }

    private void startPeriodicUpdates() {
        // Run updates more frequently (every 5 ticks = 250ms)
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    // Update all online players
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        PlayerData data = playerDataMap.get(player.getName().toLowerCase());
                        if (data != null) {
                            data.updateLocation(player.getLocation());
                            data.updateDimension(getDimensionType(player.getWorld().getEnvironment()));
                        }
                    }
                    updateLiveData();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error updating live player data", e);
                }
            }
        }, 5L, 5L); // Update every 250ms instead of 1000ms
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

    private void updateLiveData() {
        try {
            // Create base directories
            Path mapsDir = webRoot.resolve("maps");
            Files.createDirectories(mapsDir);

            // Update for each dimension with correct paths
            updateDimensionData(mapsDir.resolve("world"), "overworld");
            updateDimensionData(mapsDir.resolve("world_nether"), "nether");
            updateDimensionData(mapsDir.resolve("world_the_end"), "the_end");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to update live player data", e);
        }
    }

    private void updateDimensionData(Path worldDir, String dimension) throws Exception {
        // Create directory structure
        Files.createDirectories(worldDir);
        Path liveDir = worldDir.resolve("live");
        Files.createDirectories(liveDir);
        Path playersFile = liveDir.resolve("players.json");

        // Create players data structure
        JsonObject playersData = new JsonObject();
        JsonArray playersArray = new JsonArray();

        // Add all players that are in this dimension
        for (PlayerData data : playerDataMap.values()) {
            if (data.getDimension().equals(dimension)) {
                JsonObject playerObj = new JsonObject();
                playerObj.addProperty("uuid", data.getUuid().toString());
                playerObj.addProperty("name", data.getName());
                playerObj.addProperty("foreign", false);
                playerObj.addProperty("dimension", dimension);

                Location loc = data.getLocation();
                JsonObject position = new JsonObject();
                position.addProperty("x", loc.getX());
                position.addProperty("y", loc.getY());
                position.addProperty("z", loc.getZ());
                playerObj.add("position", position);

                JsonObject rotation = new JsonObject();
                rotation.addProperty("pitch", loc.getPitch());
                rotation.addProperty("yaw", loc.getYaw());
                rotation.addProperty("roll", 0);
                playerObj.add("rotation", rotation);

                playersArray.add(playerObj);
            }
        }

        playersData.add("players", playersArray);
        playersData.addProperty("updateInterval", 250); // Match the new update interval
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

        // Write to file with pretty printing
        try (FileWriter writer = new FileWriter(playersFile.toFile())) {
            new GsonBuilder().setPrettyPrinting().create().toJson(playersData, writer);
        }

        if (playersArray.size() > 0) {
            logger.fine(String.format("[BlueMap] Updated players.json for dimension %s with %d players", 
                dimension, playersArray.size()));
        }
    }

    public void updatePlayerData(Player player, String dimension) {
        PlayerData data = playerDataMap.get(player.getName().toLowerCase());
        if (data != null) {
            data.updateLocation(player.getLocation());
            data.updateDimension(dimension);
        } else {
            data = new PlayerData(player.getName(), player.getUniqueId(), player.getLocation(), dimension);
            playerDataMap.put(player.getName().toLowerCase(), data);
        }
        updateLiveData();
        logger.fine("[BlueMap] Updated player data for " + player.getName() + " in dimension: " + dimension);
    }

    public static class PlayerData {
        private final String name;
        private final java.util.UUID uuid;
        private Location location;
        private String dimension;

        public PlayerData(String name, java.util.UUID uuid, Location location, String dimension) {
            this.name = name;
            this.uuid = uuid;
            this.location = location;
            this.dimension = dimension;
        }

        public String getName() {
            return name;
        }

        public java.util.UUID getUuid() {
            return uuid;
        }

        public Location getLocation() {
            return location;
        }

        public String getDimension() {
            return dimension;
        }

        public void updateLocation(Location location) {
            this.location = location;
        }

        public void updateDimension(String dimension) {
            this.dimension = dimension;
        }
    }
} 