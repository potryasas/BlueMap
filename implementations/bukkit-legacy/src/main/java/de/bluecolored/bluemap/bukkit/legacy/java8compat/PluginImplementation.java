package de.bluecolored.bluemap.bukkit.legacy.java8compat;

import org.bukkit.Server;
import de.bluecolored.bluemap.bukkit.legacy.adapter.LegacyBukkitServerAdapter;
import org.bukkit.plugin.PluginDescriptionFile;
import java.io.File;

public class PluginImplementation implements Plugin {
    private final LegacyBukkitServerAdapter serverAdapter;
    private final PluginDescriptionFile description;
    private final File dataFolder;

    public PluginImplementation(LegacyBukkitServerAdapter serverAdapter, PluginDescriptionFile description) {
        if (description == null) {
            throw new IllegalArgumentException("PluginDescriptionFile cannot be null");
        }
        this.serverAdapter = serverAdapter;
        this.description = description;
        this.dataFolder = new File("plugins", description.getName());
        if (!this.dataFolder.exists()) {
            this.dataFolder.mkdirs();
        }
    }

    @Override
    public Server getServer() {
        return serverAdapter.getServer();
    }

    @Override
    public String getName() {
        return description.getName();
    }

    @Override
    public String getVersion() {
        return description.getVersion();
    }

    @Override
    public String getDescription() {
        return description.getDescription();
    }

    @Override
    public String getAuthor() {
        return !description.getAuthors().isEmpty() ? description.getAuthors().get(0) : "Unknown";
    }

    @Override
    public String getWebsite() {
        return description.getWebsite() != null ? description.getWebsite() : "https://bluemap.bluecolored.de/";
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public LegacyBukkitServerAdapter getServerAdapter() {
        return serverAdapter;
    }
} 