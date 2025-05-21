package de.bluecolored.bluemap.core.world;

import lombok.Getter;

@Getter
public class Biome {
    public static final Biome DEFAULT = new Biome("minecraft:plains");

    private final String id;

    public Biome(String id) {
        this.id = id;
    }
} 