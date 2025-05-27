package de.bluecolored.bluemap.bukkit.legacy.java8compat;

public class LegacyBlockProperty implements BlockProperty {
    private final String value;

    public LegacyBlockProperty(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
} 