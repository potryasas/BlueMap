package de.bluecolored.bluemap.bukkit.legacy.java8compat;

public class LoggerImplementation {
    private static Logger globalLogger;

    public static void setGlobalLogger(Logger logger) {
        globalLogger = logger;
    }

    public static Logger getGlobalLogger() {
        return globalLogger;
    }
} 