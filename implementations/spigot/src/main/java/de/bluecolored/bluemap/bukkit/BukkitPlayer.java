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
package de.bluecolored.bluemap.bukkit;

import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.common.plugin.text.Text;
import de.bluecolored.bluemap.common.serverinterface.Gamemode;
import de.bluecolored.bluemap.common.serverinterface.Player;
import de.bluecolored.bluemap.common.serverinterface.ServerWorld;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BukkitPlayer extends Player {

    private static final Map<GameMode, Gamemode> GAMEMODE_MAP = new EnumMap<>(GameMode.class);
    static {
        GAMEMODE_MAP.put(GameMode.ADVENTURE, Gamemode.ADVENTURE);
        GAMEMODE_MAP.put(GameMode.SURVIVAL, Gamemode.SURVIVAL);
        GAMEMODE_MAP.put(GameMode.CREATIVE, Gamemode.CREATIVE);
        GAMEMODE_MAP.put(GameMode.SPECTATOR, Gamemode.SPECTATOR);
    }

    private final UUID uuid;
    private Text name;
    private ServerWorld world;
    private Vector3d position;
    private Vector3d rotation;
    private int skyLight;
    private int blockLight;
    private boolean sneaking;
    private boolean invisible;
    private boolean vanished;
    private Gamemode gamemode;
    private String dimension;

    public BukkitPlayer(org.bukkit.entity.Player player) {
        this.uuid = player.getUniqueId();
        update(player);
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public Text getName() {
        return this.name;
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }

    @Override
    public Vector3d getPosition() {
        return this.position;
    }

    @Override
    public Vector3d getRotation() {
        return rotation;
    }

    @Override
    public int getSkyLight() {
        return skyLight;
    }

    @Override
    public int getBlockLight() {
        return blockLight;
    }

    @Override
    public boolean isSneaking() {
        return this.sneaking;
    }

    @Override
    public boolean isInvisible() {
        return this.invisible;
    }

    @Override
    public boolean isVanished() {
        return vanished;
    }

    @Override
    public Gamemode getGamemode() {
        return this.gamemode;
    }

    /**
     * API access, only call on server thread!
     */
    public void update() {
        org.bukkit.entity.Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        update(player);
    }

    private void update(org.bukkit.entity.Player player) {
        // Log start of update
        BukkitPlugin.getInstance().getLogger().info("[BlueMap] Starting player update for: " + player.getName());

        this.gamemode = GAMEMODE_MAP.get(player.getGameMode());
        if (this.gamemode == null) this.gamemode = Gamemode.SURVIVAL;

        this.invisible = player.hasPotionEffect(PotionEffectType.INVISIBILITY);

        //also check for "vanished" players
        boolean vanished = false;
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) vanished = true;
        }
        this.vanished = vanished;

        this.name = Text.of(player.getName());

        Location location = player.getLocation();
        this.position = new Vector3d(location.getX(), location.getY(), location.getZ());
        this.rotation = new Vector3d(location.getPitch(), location.getYaw(), 0);
        this.sneaking = player.isSneaking();

        this.skyLight = player.getLocation().getBlock().getLightFromSky();
        this.blockLight = player.getLocation().getBlock().getLightFromBlocks();

        World playerWorld = player.getWorld();
        this.world = BukkitPlugin.getInstance().getServerWorld(playerWorld);
        
        // Log detailed player state
        BukkitPlugin.getInstance().getLogger().info(String.format(
            "[BlueMap] Player state - Name: %s, World: %s, Position: (%.2f, %.2f, %.2f), Rotation: (%.2f, %.2f), Gamemode: %s, Invisible: %b, Vanished: %b",
            player.getName(),
            playerWorld.getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getPitch(),
            location.getYaw(),
            this.gamemode,
            this.invisible,
            this.vanished
        ));
        
        // Store dimension information
        this.dimension = playerWorld.getName();
        if (playerWorld.getEnvironment() != World.Environment.NORMAL) {
            switch (playerWorld.getEnvironment()) {
                case NETHER:
                    this.dimension = "minecraft:nether";
                    break;
                case THE_END:
                    this.dimension = "minecraft:the_end";
                    break;
                default:
                    this.dimension = "minecraft:overworld";
            }
        }

        // Force update player data file
        try {
            Path liveDir = BukkitPlugin.getInstance().getWebRoot()
                .resolve("maps")
                .resolve(playerWorld.getName())
                .resolve("live");
            
            // Log directory creation attempt
            BukkitPlugin.getInstance().getLogger().info("[BlueMap] Creating directories at: " + liveDir);
            Files.createDirectories(liveDir);
            BukkitPlugin.getInstance().getLogger().info("[BlueMap] Directories created successfully");
            
            Path playersFile = liveDir.resolve("players.json");
            BukkitPlugin.getInstance().getLogger().info("[BlueMap] Updating players file at: " + playersFile);
            
            JsonObject playerData = new JsonObject();
            JsonArray playersArray = new JsonArray();
            
            JsonObject playerObj = new JsonObject();
            playerObj.addProperty("uuid", player.getUniqueId().toString());
            playerObj.addProperty("name", player.getName());
            playerObj.addProperty("foreign", false);
            playerObj.addProperty("dimension", this.dimension);
            
            JsonObject position = new JsonObject();
            position.addProperty("x", location.getX());
            position.addProperty("y", location.getY());
            position.addProperty("z", location.getZ());
            playerObj.add("position", position);
            
            JsonObject rotation = new JsonObject();
            rotation.addProperty("pitch", location.getPitch());
            rotation.addProperty("yaw", location.getYaw());
            rotation.addProperty("roll", 0);
            playerObj.add("rotation", rotation);
            
            playersArray.add(playerObj);
            
            playerData.add("players", playersArray);
            playerData.addProperty("updateInterval", 1000); // Update every second
            playerData.addProperty("showPlayerMarkers", true);
            playerData.addProperty("showPlayerBody", true);
            playerData.addProperty("showPlayerHead", true);
            playerData.addProperty("showLabelBackground", true);
            playerData.addProperty("markerSetId", "players");
            
            JsonObject markerSet = new JsonObject();
            markerSet.addProperty("id", "players");
            markerSet.addProperty("label", "Players");
            markerSet.addProperty("toggleable", true);
            markerSet.addProperty("defaultHidden", false);
            markerSet.addProperty("priority", 1000);
            playerData.add("markerSet", markerSet);
            
            // Convert to JSON string for logging
            String jsonContent = new GsonBuilder().setPrettyPrinting().create().toJson(playerData);
            
            // Log JSON content before writing
            BukkitPlugin.getInstance().getLogger().info("[BlueMap] Writing JSON content:\n" + jsonContent);
            
            // Write file
            try (FileWriter writer = new FileWriter(playersFile.toFile())) {
                writer.write(jsonContent);
                writer.flush();
                BukkitPlugin.getInstance().getLogger().info("[BlueMap] Successfully wrote players.json file");
            }
            
        } catch (Exception e) {
            // Log detailed error information
            BukkitPlugin.getInstance().getLogger().severe(String.format(
                "[BlueMap] Error updating player data - Player: %s, World: %s, Error: %s\nStack trace:\n%s",
                player.getName(),
                playerWorld.getName(),
                e.getMessage(),
                Arrays.toString(e.getStackTrace())
            ));
        }
        
        // Log completion of update
        BukkitPlugin.getInstance().getLogger().info("[BlueMap] Completed update for player: " + player.getName());
    }

}
