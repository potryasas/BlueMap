#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Интеграция системы живых маркеров в legacy BlueMap
Модификация LegacyBukkitPlugin для поддержки live/markers.json
"""

import os
import re
from pathlib import Path

def modify_legacy_plugin():
    """Модифицирует LegacyBukkitPlugin для поддержки live маркеров"""
    
    plugin_file = "implementations/bukkit-legacy/src/main/java/de/bluecolored/bluemap/bukkit/legacy/LegacyBukkitPlugin.java"
    
    if not os.path.exists(plugin_file):
        print(f"❌ Файл не найден: {plugin_file}")
        return False
    
    try:
        with open(plugin_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Добавляем импорты для работы с live маркерами
        import_addition = """import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;"""
        
        if "import java.nio.file.Files;" not in content:
            # Находим место для вставки импортов
            package_line = content.find("package de.bluecolored.bluemap.bukkit.legacy;")
            if package_line != -1:
                insert_pos = content.find("\n", package_line) + 1
                content = content[:insert_pos] + "\n" + import_addition + "\n" + content[insert_pos:]
                print("✅ Добавлены импорты для live маркеров")
        
        # Добавляем поле для scheduled executor
        executor_field = "    private ScheduledExecutorService liveMarkersExecutor;"
        
        if "liveMarkersExecutor" not in content:
            # Находим место после других полей
            class_start = content.find("public class LegacyBukkitPlugin extends JavaPlugin")
            if class_start != -1:
                brace_pos = content.find("{", class_start)
                insert_pos = content.find("\n", brace_pos) + 1
                content = content[:insert_pos] + "    " + executor_field + "\n" + content[insert_pos:]
                print("✅ Добавлено поле для live маркеров executor")
        
        # Добавляем метод обновления live маркеров
        live_markers_method = '''
    private void updateLiveMarkers() {
        try {
            // Путь к статическому файлу маркеров
            File webDir = new File(getDataFolder(), "web");
            File worldDir = new File(webDir, "maps/world");
            File markersFile = new File(worldDir, "markers.json");
            
            // Путь к live файлу маркеров
            File liveDir = new File(worldDir, "live");
            if (!liveDir.exists()) {
                liveDir.mkdirs();
            }
            File liveMarkersFile = new File(liveDir, "markers.json");
            
            // Копируем статические маркеры в live файл
            if (markersFile.exists()) {
                Files.copy(markersFile.toPath(), liveMarkersFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                getLogger().fine("Updated live markers from static file");
            }
            
        } catch (Exception e) {
            getLogger().warning("Failed to update live markers: " + e.getMessage());
        }
    }
    
    private void startLiveMarkersUpdater() {
        // Создаем executor для обновления live маркеров
        liveMarkersExecutor = Executors.newSingleThreadScheduledExecutor();
        
        // Обновляем live маркеры каждые 30 секунд
        liveMarkersExecutor.scheduleAtFixedRate(this::updateLiveMarkers, 0, 30, TimeUnit.SECONDS);
        
        getLogger().info("Started live markers updater (30 second interval)");
    }
    
    private void stopLiveMarkersUpdater() {
        if (liveMarkersExecutor != null) {
            liveMarkersExecutor.shutdown();
            try {
                if (!liveMarkersExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    liveMarkersExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                liveMarkersExecutor.shutdownNow();
            }
            getLogger().info("Stopped live markers updater");
        }
    }'''
        
        if "updateLiveMarkers()" not in content:
            # Находим конец класса для вставки методов
            last_brace = content.rfind("}")
            content = content[:last_brace] + live_markers_method + "\n" + content[last_brace:]
            print("✅ Добавлены методы для работы с live маркерами")
        
        # Модифицируем onEnable для запуска live маркеров
        onenable_pattern = r"(public void onEnable\(\)\s*\{.*?)(}\s*$)"
        onenable_match = re.search(onenable_pattern, content, re.DOTALL | re.MULTILINE)
        
        if onenable_match and "startLiveMarkersUpdater()" not in content:
            onenable_content = onenable_match.group(1)
            # Добавляем вызов в конец метода onEnable
            new_onenable = onenable_content + "        \n        // Запускаем обновление live маркеров\n        startLiveMarkersUpdater();\n        "
            content = content.replace(onenable_match.group(1), new_onenable)
            print("✅ Добавлен запуск live маркеров в onEnable")
        
        # Модифицируем onDisable для остановки live маркеров
        ondisable_pattern = r"(public void onDisable\(\)\s*\{.*?)(}\s*$)"
        ondisable_match = re.search(ondisable_pattern, content, re.DOTALL | re.MULTILINE)
        
        if ondisable_match and "stopLiveMarkersUpdater()" not in content:
            ondisable_content = ondisable_match.group(1)
            # Добавляем вызов в начало метода onDisable
            lines = ondisable_content.split('\n')
            if len(lines) > 1:
                lines.insert(1, "        // Останавливаем обновление live маркеров")
                lines.insert(2, "        stopLiveMarkersUpdater();")
                new_ondisable = '\n'.join(lines)
                content = content.replace(ondisable_content, new_ondisable)
                print("✅ Добавлена остановка live маркеров в onDisable")
        
        # Сохраняем изменения
        with open(plugin_file, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"✅ Файл {plugin_file} успешно модифицирован")
        return True
        
    except Exception as e:
        print(f"❌ Ошибка модификации файла: {e}")
        return False

def create_live_markers_web_handler():
    """Создает обработчик для live маркеров в веб-сервере"""
    
    handler_code = '''
    /**
     * Обработчик для live/markers.json
     */
    private void handleLiveMarkersRequest(HttpExchange exchange) throws IOException {
        // Устанавливаем CORS заголовки
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        
        try {
            // Читаем файл live маркеров
            File webDir = new File(getDataFolder(), "web");
            File liveMarkersFile = new File(webDir, "maps/world/live/markers.json");
            
            if (liveMarkersFile.exists()) {
                String content = new String(Files.readAllBytes(liveMarkersFile.toPath()), StandardCharsets.UTF_8);
                byte[] response = content.getBytes(StandardCharsets.UTF_8);
                
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
                
                getLogger().fine("Served live markers: " + response.length + " bytes");
            } else {
                // Возвращаем пустой объект если файл не найден
                String emptyResponse = "{}";
                byte[] response = emptyResponse.getBytes(StandardCharsets.UTF_8);
                
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();
                
                getLogger().warning("Live markers file not found, returned empty object");
            }
            
        } catch (Exception e) {
            getLogger().severe("Error serving live markers: " + e.getMessage());
            
            // Возвращаем ошибку
            String errorResponse = "{}";
            byte[] response = errorResponse.getBytes(StandardCharsets.UTF_8);
            
            exchange.sendResponseHeaders(500, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }'''
    
    return handler_code

def modify_bluemap_stub():
    """Модифицирует BlueMapStub для поддержки live маркеров"""
    
    stub_file = "implementations/bukkit-legacy/src/main/java/de/bluecolored/bluemap/bukkit/legacy/java8compat/BlueMapStub.java"
    
    if not os.path.exists(stub_file):
        print(f"❌ Файл не найден: {stub_file}")
        return False
    
    try:
        with open(stub_file, 'r', encoding='utf-8') as f:
            content = f.read()
        
        handler_code = create_live_markers_web_handler()
        
        # Добавляем обработчик live маркеров
        if "handleLiveMarkersRequest" not in content:
            # Находим конец класса для вставки метода
            last_brace = content.rfind("}")
            content = content[:last_brace] + handler_code + "\n" + content[last_brace:]
            print("✅ Добавлен обработчик live маркеров в BlueMapStub")
        
        # Модифицируем создание HTTP сервера для добавления маршрута live маркеров
        server_creation = 'httpServer.createContext("/maps/world/live/markers.json", this::handleLiveMarkersRequest);'
        
        if "/live/markers.json" not in content:
            # Находим место создания контекстов
            context_pattern = r'(httpServer\.createContext\("/", this::handleRequest\);)'
            if re.search(context_pattern, content):
                content = re.sub(context_pattern, r'\1\n                ' + server_creation, content)
                print("✅ Добавлен маршрут для live маркеров")
        
        # Сохраняем изменения
        with open(stub_file, 'w', encoding='utf-8') as f:
            f.write(content)
        
        print(f"✅ Файл {stub_file} успешно модифицирован")
        return True
        
    except Exception as e:
        print(f"❌ Ошибка модификации BlueMapStub: {e}")
        return False

def main():
    """Основная функция интеграции"""
    
    print("🔧 ИНТЕГРАЦИЯ ЖИВЫХ МАРКЕРОВ В LEGACY BLUEMAP")
    print("=" * 55)
    
    # 1. Модифицируем LegacyBukkitPlugin
    print("\n1️⃣ Модификация LegacyBukkitPlugin...")
    if modify_legacy_plugin():
        print("✅ LegacyBukkitPlugin успешно модифицирован")
    else:
        print("❌ Ошибка модификации LegacyBukkitPlugin")
    
    # 2. Модифицируем BlueMapStub
    print("\n2️⃣ Модификация BlueMapStub...")
    if modify_bluemap_stub():
        print("✅ BlueMapStub успешно модифицирован")
    else:
        print("❌ Ошибка модификации BlueMapStub")
    
    print("\n" + "=" * 55)
    print("🎉 ИНТЕГРАЦИЯ ЗАВЕРШЕНА!")
    print("=" * 55)
    
    print("\n📋 СЛЕДУЮЩИЕ ШАГИ:")
    print("1. Запустите сборку: ./gradlew bukkit-legacy:build")
    print("2. Установите новый JAR на сервер")
    print("3. Перезапустите сервер")
    print("4. Проверьте логи на наличие сообщений о live маркерах")
    
    print("\n🔍 ПРОВЕРКА РАБОТЫ:")
    print("- В логах должны появиться сообщения:")
    print("  'Started live markers updater (30 second interval)'")
    print("- URL для live маркеров: http://localhost:8100/maps/world/live/markers.json")
    
    print("\n💡 ПРИНЦИП РАБОТЫ:")
    print("- Каждые 30 секунд статические маркеры копируются в live/markers.json")
    print("- Веб-интерфейс запрашивает live/markers.json каждые 10 секунд")
    print("- Маркеры отображаются как 'живые' но остаются статичными")

if __name__ == "__main__":
    main() 