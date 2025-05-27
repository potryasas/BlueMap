package de.bluecolored.bluemap.bukkit.legacy.java8compat.legacy;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtensionType;
import de.bluecolored.bluemap.core.util.Key;

public class LegacyResourcePackExtensionType implements ResourcePackExtensionType<LegacyResourcePackExtension> {
    public static final LegacyResourcePackExtensionType INSTANCE = new LegacyResourcePackExtensionType();
    private static final Key LEGACY_KEY = new Key("legacy");

    private LegacyResourcePackExtensionType() {}

    @Override
    public Key getKey() {
        return LEGACY_KEY;
    }

    @Override
    public LegacyResourcePackExtension create() {
        return new LegacyResourcePackExtension();
    }
} 