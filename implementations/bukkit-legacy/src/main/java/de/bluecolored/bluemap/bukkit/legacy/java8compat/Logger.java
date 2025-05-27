package de.bluecolored.bluemap.bukkit.legacy.java8compat;

public interface Logger {
    void logInfo(String message);
    void logWarning(String message);
    void logError(String message);
    void logError(String message, Throwable throwable);
    void logDebug(String message);
} 