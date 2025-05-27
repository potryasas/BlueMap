package de.bluecolored.bluemap.bukkit.legacy.player;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitPlugin;

public class LegacyPlayerManager implements Listener {
    private final LegacyBukkitPlugin plugin;
    private final Map<String, PlayerData> playerDataMap;
    private long lastUpdateTime;
    private static final long UPDATE_INTERVAL = 1000; // 1 second
    
    public LegacyPlayerManager(LegacyBukkitPlugin plugin) {
        this.plugin = plugin;
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
        PlayerData data = new PlayerData(player.getName(), player.getUniqueId(), player.getLocation(), player.getWorld().getName());
        playerDataMap.put(player.getName().toLowerCase(), data);
        updateLiveData();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerDataMap.remove(player.getName().toLowerCase());
        updateLiveData();
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL) {
            Player player = event.getPlayer();
            PlayerData data = playerDataMap.get(player.getName().toLowerCase());
            if (data != null) {
                data.updateLocation(player.getLocation());
            }
            updateLiveData();
            lastUpdateTime = currentTime;
        }
    }
    
    private void startPeriodicUpdates() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                updateLiveData();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error updating live player data", e);
            }
        }, 20L, 20L); // Update every second
    }
    
    private void updateLiveData() {
        // Get all online players across all worlds
        Map<String, PlayerData> allPlayers = new HashMap<>(playerDataMap);
        
        // Update each world's live data
        for (org.bukkit.World world : plugin.getServer().getWorlds()) {
            JsonObject liveData = new JsonObject();
            JsonArray playersArray = new JsonArray();
            
            // Add all players, marking them as foreign if from different world
            for (PlayerData data : allPlayers.values()) {
                JsonObject playerData = new JsonObject();
                playerData.addProperty("uuid", data.getUuid().toString());
                playerData.addProperty("name", data.getName());
                
                // Check if player is in this world
                boolean isForeign = !data.getLocation().getWorld().equals(world);
                playerData.addProperty("foreign", isForeign);
                
                // Add position data
                JsonObject position = new JsonObject();
                position.addProperty("x", data.getLocation().getX());
                position.addProperty("y", data.getLocation().getY());
                position.addProperty("z", data.getLocation().getZ());
                playerData.add("position", position);
                
                // Add rotation data
                JsonObject rotation = new JsonObject();
                rotation.addProperty("pitch", data.getLocation().getPitch());
                rotation.addProperty("yaw", data.getLocation().getYaw());
                rotation.addProperty("roll", 0);
                playerData.add("rotation", rotation);
                
                playersArray.add(playerData);
            }
            
            // Create the full players data structure
            JsonObject playersData = new JsonObject();
            playersData.add("players", playersArray);
            playersData.addProperty("updateInterval", 1000);
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
            
            liveData.add("players", playersData);
            
            // Add markers
            JsonObject markers = plugin.getMarkerManager().getMarkersJson();
            liveData.add("markers", markers);
            
            // Update live data for this world
            plugin.getWebManager().updateLiveData(world.getName(), liveData);
        }
    }
    
    public Collection<PlayerData> getOnlinePlayers() {
        return playerDataMap.values();
    }
    
    public static class PlayerData {
        private final String name;
        private final UUID uuid;
        private Location location;
        private String dimension;
        
        public PlayerData(String name, UUID uuid, Location location, String dimension) {
            this.name = name;
            this.uuid = uuid;
            this.location = location;
            this.dimension = dimension;
        }
        
        public String getName() {
            return name;
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public void updateLocation(Location location) {
            this.location = location;
        }
        
        public String getDimension() {
            return dimension;
        }
        
        public void updateDimension(String dimension) {
            this.dimension = dimension;
        }
    }
} 