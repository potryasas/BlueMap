package de.bluecolored.bluemap.bukkit.legacy.java8compat.legacy;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

public class LegacyResourcePackExtension implements ResourcePackExtension {

    @Override
    public void loadResources(Path root) throws IOException {
        // Legacy resource pack loading logic
    }

    @Override
    public Iterable<Texture> loadTextures(Path root) throws IOException {
        // Legacy texture loading logic
        return Collections.emptyList();
    }

    @Override
    public void bake() throws IOException {
        // Legacy baking logic
    }
} 