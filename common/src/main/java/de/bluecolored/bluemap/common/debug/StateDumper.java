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
package de.bluecolored.bluemap.common.debug;

import com.google.gson.stream.JsonWriter;
import de.bluecolored.bluemap.core.BlueMap;
import de.bluecolored.bluemap.core.util.Key;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;

public class StateDumper {

    private static final StateDumper GLOBAL = new StateDumper();

    private final Set<Object> instances = Collections.newSetFromMap(new WeakHashMap<>());

    public void dump(Path file) throws IOException {
        JsonWriter writer = new JsonWriter(Files.newBufferedWriter(
                file,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        ));
        writer.setIndent(" ");

        writer.beginObject();

        writer.name("system-info");
        collectSystemInfo(writer);

        Set<Object> alreadyDumped = Collections.newSetFromMap(new IdentityHashMap<>());

        writer.name("threads").beginArray();
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            dumpInstance(thread, writer, alreadyDumped);
        }
        writer.endArray();

        writer.name("dump").beginObject();
        for (Object instance : instances) {
            Class<?> type = instance.getClass();
            writer.name(type.getName());
            dumpInstance(instance, writer, alreadyDumped);
        }
        writer.endObject();

        writer.endObject();

        writer.flush();
        writer.close();
    }

    private void dumpInstance(Object instance, JsonWriter writer, Set<Object> alreadyDumped) throws IOException {

        if (instance == null) {
            writer.nullValue();
            return;
        }

        if (instance instanceof String ||
                instance instanceof Path ||
                instance instanceof UUID ||
                instance instanceof Key
        ) {
            writer.value(instance.toString());
            return;
        }

        if (instance instanceof Number) {
            writer.value((Number) instance);
            return;
        }

        if (instance instanceof Boolean) {
            writer.value((Boolean) instance);
            return;
        }

        if (!alreadyDumped.add(instance)) {
            writer.value("<<" + toIdentityString(instance) + ">>");
            return;
        }

        writer.beginObject();

        Class<?> clazz = instance.getClass();
        writer.name("@type").value(clazz.getName());

        if (instance instanceof Thread) {
            Thread thread = (Thread) instance;
            writer.name("name").value(thread.getName());
            writer.name("state").value(thread.getState().name());
            writer.name("priority").value(thread.getPriority());
            writer.name("daemon").value(thread.isDaemon());
            writer.name("alive").value(thread.isAlive());
            writer.name("interrupted").value(thread.isInterrupted());
            return;
        }

        if (instance instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) instance;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                writer.name(String.valueOf(entry.getKey()));
                dumpInstance(entry.getValue(), writer, alreadyDumped);
            }
            return;
        }

        if (instance instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> collection = (Collection<Object>) instance;
            writer.name("elements").beginArray();
            for (Object element : collection) {
                dumpInstance(element, writer, alreadyDumped);
            }
            writer.endArray();
            return;
        }

        String toString = instance.toString();
        if (!toString.equals(toIdentityString(instance))) {
            writer.name("#toString").value(instance.toString());
        }

        dumpAnnotatedInstance(clazz, instance, writer, alreadyDumped);

        writer.endObject();
    }

    private static String toIdentityString(Object instance) {
        return instance.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(instance));
    }

    private void dumpAnnotatedInstance(Class<?> type, Object instance, JsonWriter writer, Set<Object> alreadyDumped) throws IOException {

        DebugDump typedd = type.getAnnotation(DebugDump.class);
        boolean exclude = typedd != null && typedd.exclude();
        boolean allFields = !exclude && (
                typedd != null ||
                getPackageName(type).startsWith("de.bluecolored.bluemap")
        );

        for (Field field : type.getDeclaredFields()) {
            String key = field.getName();
            Object value;

            try {
                DebugDump dd = field.getAnnotation(DebugDump.class);
                if (dd == null) {
                    if (!allFields) continue;
                    if (Modifier.isStatic(field.getModifiers())) continue;
                    if (Modifier.isTransient(field.getModifiers())) continue;
                } else {
                    if (dd.exclude()) continue;
                }

                if (dd != null) {
                    key = dd.value();
                    if (key.isEmpty()) key = field.getName();
                }

                field.setAccessible(true);
                value = field.get(instance);
            } catch (Exception ex) {
                writer.name("!!" + key).value(ex.toString());
                continue;
            }

            writer.name(key);
            dumpInstance(value, writer, alreadyDumped);
        }

        for (Method method : type.getDeclaredMethods()) {
            String key = method.toGenericString();
            Object value;

            try {
                DebugDump dd = method.getAnnotation(DebugDump.class);
                if (dd == null || dd.exclude()) continue;

                key = dd.value();
                if (key.isEmpty()) key = method.toGenericString();

                method.setAccessible(true);
                value = method.invoke(instance);
            } catch (Exception ex) {
                writer.name("!!" + key).value(ex.toString());
                continue;
            }

            writer.name(key);
            dumpInstance(value, writer, alreadyDumped);
        }

        for (Class<?> iface : type.getInterfaces()) {
            dumpAnnotatedInstance(iface, instance, writer, alreadyDumped);
        }

        Class<?> typeSuperclass = type.getSuperclass();
        if (typeSuperclass != null) {
            dumpAnnotatedInstance(typeSuperclass, instance, writer, alreadyDumped);
        }

    }

    private String getPackageName(Class<?> clazz) {
        String className = clazz.getName();
        int lastDot = className.lastIndexOf('.');
        return lastDot > 0 ? className.substring(0, lastDot) : "";
    }

    private void collectSystemInfo(JsonWriter writer) throws IOException {
        writer.beginObject();

        writer.name("bluemap-version").value(BlueMap.VERSION);
        writer.name("git-hash").value(BlueMap.GIT_HASH);

        String[] properties = new String[]{
                "java.runtime.name",
                "java.runtime.version",
                "java.vm.vendor",
                "java.vm.name",
                "os.name",
                "os.version",
                "user.dir",
                "java.home",
                "file.separator",
                "sun.io.unicode.encoding",
                "java.class.version"
        };
        Map<String, String> propMap = new HashMap<>();
        for (String key : properties) {
            propMap.put(key, System.getProperty(key));
        }
        writer.name("properties");
        dumpInstance(propMap, writer, new HashSet<>());

        writer.name("cores").value(Runtime.getRuntime().availableProcessors());
        writer.name("max-memory").value(Runtime.getRuntime().maxMemory());
        writer.name("total-memory").value(Runtime.getRuntime().totalMemory());
        writer.name("free-memory").value(Runtime.getRuntime().freeMemory());

        writer.name("timestamp").value(System.currentTimeMillis());
        writer.name("time").value(LocalDateTime.now().toString());

        writer.endObject();
    }

    public static StateDumper global() {
        return GLOBAL;
    }

    public synchronized void register(Object instance) {
        GLOBAL.instances.add(instance);
    }

}
