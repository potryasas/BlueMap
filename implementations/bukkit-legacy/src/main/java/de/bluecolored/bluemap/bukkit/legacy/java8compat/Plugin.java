package de.bluecolored.bluemap.bukkit.legacy.java8compat;

import org.bukkit.Server;
import java.io.File;

public interface Plugin {
    Server getServer();
    String getName();
    String getVersion();
    String getDescription();
    String getAuthor();
    String getWebsite();
    File getDataFolder();
    ServerAdapter getServerAdapter();
} 