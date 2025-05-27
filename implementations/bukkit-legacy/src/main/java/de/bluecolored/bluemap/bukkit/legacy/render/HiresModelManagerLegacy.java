package de.bluecolored.bluemap.bukkit.legacy.render;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class HiresModelManagerLegacy {
    private final Logger logger;
    private final HiresModelRendererLegacy renderer;

    public HiresModelManagerLegacy(Logger logger, HiresModelRendererLegacy renderer) {
        this.logger = logger;
        this.renderer = renderer;
    }

    public void render(Object world, Object tile, Object tileMetaConsumer, boolean save) {
        // world: адаптировать под твой World
        // tile: Vector2i или аналог
        // tileMetaConsumer: интерфейс для meta-данных
        // save: сохранять ли prbm
        // TODO: реализовать рендер hires-тайла
    }

    public void unrender(Object tile, Object tileMetaConsumer) {
        // TODO: реализовать удаление hires-тайла и обновление lowres
    }

    private void save(final ArrayTileModelLegacy model, Object tile) {
        // TODO: реализовать сохранение prbm-файла
    }
} 