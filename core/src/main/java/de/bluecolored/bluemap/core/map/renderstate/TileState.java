/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.core.map.renderstate;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.Keyed;
import de.bluecolored.bluemap.core.util.Registry;
import de.bluecolored.bluemap.core.map.Bounds;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static de.bluecolored.bluemap.core.map.renderstate.TileActionResolver.ActionAndNextState.*;
import static de.bluecolored.bluemap.core.map.renderstate.TileActionResolver.BoundsSituation.*;

import java.util.Collection;
import java.util.function.BiFunction;

public interface TileState extends Keyed, TileActionResolver {

    TileState UNKNOWN = new Impl( Key.bluemap("unknown"));

    TileState RENDERED = new Impl(Key.bluemap("rendered"), (changed, bounds) -> {
        if (bounds == INSIDE) {
            return changed ? RENDER_RENDERED : NONE_RENDERED;
        } else if (bounds == EDGE) {
            return RENDER_RENDERED_EDGE;
        } else if (bounds == OUTSIDE) {
            return DELETE_OUT_OF_BOUNDS;
        } else {
            throw new IllegalStateException("Unexpected bounds: " + bounds);
        }
    });
    TileState RENDERED_EDGE = new Impl(Key.bluemap("rendered-edge"), (changed, bounds) -> {
        if (bounds == INSIDE) {
            return RENDER_RENDERED;
        } else if (bounds == EDGE) {
            return changed ? RENDER_RENDERED_EDGE : NONE_RENDERED_EDGE;
        } else if (bounds == OUTSIDE) {
            return DELETE_OUT_OF_BOUNDS;
        } else {
            throw new IllegalStateException("Unexpected bounds: " + bounds);
        }
    });
    TileState OUT_OF_BOUNDS = new Impl(Key.bluemap("out-of-bounds"), (changed, bounds) -> {
        if (bounds == INSIDE) {
            return RENDER_RENDERED;
        } else if (bounds == EDGE) {
            return RENDER_RENDERED_EDGE;
        } else if (bounds == OUTSIDE) {
            return NONE_OUT_OF_BOUNDS;
        } else {
            throw new IllegalStateException("Unexpected bounds: " + bounds);
        }
    });

    TileState NOT_GENERATED = new Impl(Key.bluemap("not-generated"));
    TileState MISSING_LIGHT = new Impl(Key.bluemap("missing-light"));
    TileState LOW_INHABITED_TIME = new Impl(Key.bluemap("low-inhabited-time"));
    TileState CHUNK_ERROR = new Impl(Key.bluemap("chunk-error"));

    TileState RENDER_ERROR = new Impl(Key.bluemap("render-error"), (changed, bounds) -> {
        if (bounds == INSIDE) {
            return RENDER_RENDERED;
        } else if (bounds == EDGE) {
            return RENDER_RENDERED_EDGE;
        } else if (bounds == OUTSIDE) {
            return DELETE_OUT_OF_BOUNDS;
        } else {
            throw new IllegalStateException("Unexpected bounds: " + bounds);
        }
    });

    Registry<TileState> REGISTRY = new Registry<>(
            UNKNOWN,
            RENDERED,
            RENDERED_EDGE,
            OUT_OF_BOUNDS,
            NOT_GENERATED,
            MISSING_LIGHT,
            LOW_INHABITED_TIME,
            CHUNK_ERROR,
            RENDER_ERROR
    );

    TileState refresh(Collection<ChunkInfoRegion> chunksChanged, Bounds tileBounds);

    @Getter
    @RequiredArgsConstructor
    class Impl implements TileState {
        private final Key key;
        private final TileActionResolver resolver;

        public Impl(Key key) {
            this.key = key;
            this.resolver = (changed, bounds) -> {
                if (!changed) return noActionThisNextState();
                if (bounds == INSIDE) {
                    return RENDER_RENDERED;
                } else if (bounds == EDGE) {
                    return RENDER_RENDERED_EDGE;
                } else if (bounds == OUTSIDE) {
                    return DELETE_OUT_OF_BOUNDS;
                } else {
                    throw new IllegalStateException("Unexpected bounds: " + bounds);
                }
            };
        }

        @Override
        public String toString() {
            return key.getFormatted();
        }

        @Override
        public ActionAndNextState findActionAndNextState(
                boolean changed,
                BoundsSituation bounds
        ) {
            return resolver.findActionAndNextState(changed, bounds);
        }

        private ActionAndNextState noActionThisNextState;
        private ActionAndNextState noActionThisNextState() {
            if (noActionThisNextState == null)
                noActionThisNextState = new ActionAndNextState(Action.NONE, this);
            return noActionThisNextState;
        }

        @Override
        public Key getKey() {
            return key;
        }

        @Override
        public TileState refresh(Collection<ChunkInfoRegion> chunksChanged, Bounds tileBounds) {
            return this;
        }
    }

}
