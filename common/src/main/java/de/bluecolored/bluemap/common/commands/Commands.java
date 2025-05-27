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
package de.bluecolored.bluemap.common.commands;

import com.flowpowered.math.vector.Vector3d;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.bluecolored.bluemap.common.commands.java8compat.Command;
import de.bluecolored.bluemap.common.commands.java8compat.BlueMapCommands;
import de.bluecolored.bluemap.common.commands.arguments.MapBackedArgumentParser;
import de.bluecolored.bluemap.common.commands.arguments.StringSetArgumentParser;
import de.bluecolored.bluemap.common.commands.commands.*;
import de.bluecolored.bluemap.common.config.storage.StorageConfig;
import de.bluecolored.bluemap.common.plugin.Plugin;
import de.bluecolored.bluemap.common.rendermanager.RenderTask;
import de.bluecolored.bluemap.common.rendermanager.TileUpdateStrategy;
import de.bluecolored.bluemap.common.serverinterface.CommandSource;
import de.bluecolored.bluemap.common.serverinterface.ServerWorld;
import de.bluecolored.bluemap.core.map.BmMap;
import de.bluecolored.bluemap.core.world.World;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static de.bluecolored.bluemap.common.commands.TextFormat.*;
import static net.kyori.adventure.text.Component.text;

public class Commands {

    private static final Cache<String, RenderTask> REF_TO_RENDERTASK = Caffeine.newBuilder()
            .weakValues()
            .build();
    private static final LoadingCache<RenderTask, String> RENDERTASK_TO_REF = Caffeine.newBuilder()
            .weakKeys()
            .build(Commands::safeRandomRef);

    public static Command create(Plugin plugin) {
        BlueMapCommands.BlueMapCommandRegistry.initialize(plugin);
        return BlueMapCommands.BlueMapCommandRegistry.getRootCommand();
    }

    public static String getRefForTask(RenderTask task) {
        return RENDERTASK_TO_REF.get(task);
    }

    public static @Nullable RenderTask getTaskForRef(String ref) {
        return REF_TO_RENDERTASK.getIfPresent(ref);
    }

    public static boolean checkExecutablePreconditions(Plugin plugin, CommandSource context, Object executable) {
        if (!hasUnloadedAnnotation(executable)) {
            return Commands.checkPluginLoaded(plugin, context);
        }

        return true;
    }

    private static boolean hasUnloadedAnnotation(Object executable) {
        try {
            if (executable instanceof Class) {
                return ((Class<?>) executable).isAnnotationPresent(Unloaded.class);
            } else {
                return executable.getClass().isAnnotationPresent(Unloaded.class);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkPluginLoaded(Plugin plugin, CommandSource context){
        if (!plugin.isLoaded()) {
            if (plugin.isLoading()) {
                context.sendMessage(lines(
                        text("⌛ BlueMap is still loading!").color(INFO_COLOR),
                        text("Please try again in a few seconds.").color(BASE_COLOR)
                ));
            } else {
                context.sendMessage(lines(
                        text("❌ BlueMap is not loaded!").color(NEGATIVE_COLOR),
                        format("Check your server-console for errors or warnings and try using %.",
                                command("/bluemap reload").color(HIGHLIGHT_COLOR)
                        ).color(BASE_COLOR)
                ));
            }
            return false;
        }

        return true;
    }

    private static synchronized String safeRandomRef(RenderTask task) {
        String ref = randomRef();
        while (REF_TO_RENDERTASK.asMap().putIfAbsent(ref, task) != null) ref = randomRef();
        return ref;
    }

    private static String randomRef() {
        StringBuilder ref = new StringBuilder(Integer.toString(Math.abs(new Random().nextInt()), 16));
        while (ref.length() < 4) ref.insert(0, "0");
        return ref.subSequence(0, 4).toString();
    }

}
