package de.bluecolored.bluemap.bukkit.legacy;

import org.bukkit.plugin.java.JavaPlugin;

public class DummyClass extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("BlueMap Legacy for Minecraft 1.5.2 started!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BlueMap Legacy stopped!");
    }
}
