#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Тестирование исправленного плагина BlueMap с командами маркеров
"""

import os
import json
import subprocess
import time
from pathlib import Path

def check_bluemap_build():
    """Проверяем собран ли BlueMap"""
    
    print("🔍 ПРОВЕРКА СБОРКИ BLUEMAP")
    print("=" * 50)
    
    # Ищем JAR файлы плагина
    jar_locations = [
        "bukkit-legacy/build/libs/",
        "implementations/bukkit-legacy/build/libs/",
        "build/libs/"
    ]
    
    found_jars = []
    for location in jar_locations:
        if os.path.exists(location):
            jar_files = [f for f in os.listdir(location) if f.endswith('.jar') and 'BlueMap' in f]
            for jar in jar_files:
                found_jars.append(os.path.join(location, jar))
    
    if found_jars:
        print("✅ Найдены JAR файлы:")
        for jar in found_jars:
            size = os.path.getsize(jar) / (1024 * 1024)  # MB
            print(f"   {jar} ({size:.1f} MB)")
        return found_jars[0]  # Возвращаем первый найденный
    else:
        print("❌ JAR файлы плагина не найдены")
        return None

def build_bluemap():
    """Собираем BlueMap плагин"""
    
    print("\n🔨 СБОРКА BLUEMAP ПЛАГИНА")
    print("=" * 50)
    
    try:
        # Пытаемся собрать с Gradle
        print("📦 Запускаем gradle build...")
        
        if os.name == 'nt':  # Windows
            result = subprocess.run(["gradlew.bat", "build"], 
                                   capture_output=True, text=True, cwd=".")
        else:  # Linux/Mac
            result = subprocess.run(["./gradlew", "build"], 
                                   capture_output=True, text=True, cwd=".")
        
        if result.returncode == 0:
            print("✅ Сборка успешна")
            return True
        else:
            print(f"❌ Ошибка сборки:")
            print(result.stderr[:500])  # Первые 500 символов ошибки
            return False
            
    except Exception as e:
        print(f"❌ Ошибка запуска сборки: {e}")
        return False

def copy_plugin_to_server(jar_path):
    """Копируем плагин на тестовый сервер"""
    
    print("\n📋 КОПИРОВАНИЕ ПЛАГИНА НА СЕРВЕР")
    print("=" * 50)
    
    server_plugins = r"c:\Personal\TestServer\plugins"
    
    if not os.path.exists(server_plugins):
        print(f"❌ Папка сервера не найдена: {server_plugins}")
        return False
    
    try:
        import shutil
        
        # Удаляем старые версии BlueMap
        for file in os.listdir(server_plugins):
            if file.startswith("BlueMap") and file.endswith(".jar"):
                old_file = os.path.join(server_plugins, file)
                os.remove(old_file)
                print(f"🗑️ Удален старый файл: {file}")
        
        # Копируем новый плагин
        plugin_name = os.path.basename(jar_path)
        target_path = os.path.join(server_plugins, plugin_name)
        
        shutil.copy2(jar_path, target_path)
        
        print(f"✅ Плагин скопирован: {plugin_name}")
        print(f"📍 Путь: {target_path}")
        
        return True
        
    except Exception as e:
        print(f"❌ Ошибка копирования: {e}")
        return False

def test_commands():
    """Создаем файл с тестовыми командами"""
    
    print("\n🎮 СОЗДАНИЕ ТЕСТОВЫХ КОМАНД")
    print("=" * 50)
    
    test_commands = """
# Тестовые команды для BlueMap с исправленными маркерами

# 1. Список маркеров
/bluemap markers list

# 2. Создание категорий
/bluemap markers create cities "Города"
/bluemap markers create villages "Деревни"
/bluemap markers create landmarks "Достопримечательности"

# 3. Добавление маркеров с автоматическими ID (addset)
/bluemap markers addset cities 0 70 0 "Спавн"
/bluemap markers addset cities 100 65 200 "Новый город"
/bluemap markers addset villages -50 70 50 "Деревня у леса"
/bluemap markers addset landmarks 500 80 -300 "Великая башня"

# 4. Добавление маркеров с ручными ID (add)
/bluemap markers add cities custom_1 -200 65 -200 "Особый город"

# 5. Сохранение маркеров
/bluemap markers save

# 6. Перезагрузка маркеров
/bluemap markers reload

# 7. Создание примеров
/bluemap markers examples

# После выполнения команд проверьте:
# - Файл: c:\\Personal\\TestServer\\plugins\\BlueMap\\web\\maps\\world\\live\\markers.json
# - Веб-интерфейс: http://localhost:8100
"""
    
    with open("test_commands.txt", 'w', encoding='utf-8') as f:
        f.write(test_commands)
    
    print("✅ Создан файл test_commands.txt с тестовыми командами")
    print("📋 Скопируйте команды в игру или консоль сервера")

def check_live_markers():
    """Проверяем созданы ли live маркеры"""
    
    print("\n🔍 ПРОВЕРКА LIVE МАРКЕРОВ")
    print("=" * 50)
    
    markers_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live\markers.json"
    
    if os.path.exists(markers_file):
        try:
            with open(markers_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            print(f"✅ Live маркеры найдены: {len(data)} категорий")
            
            total_markers = 0
            for cat_name, cat_data in data.items():
                if isinstance(cat_data, dict) and 'markers' in cat_data:
                    marker_count = len(cat_data['markers'])
                    total_markers += marker_count
                    print(f"   📁 {cat_name}: {marker_count} маркеров")
            
            print(f"📍 Всего маркеров: {total_markers}")
            return True
            
        except Exception as e:
            print(f"❌ Ошибка чтения файла маркеров: {e}")
            return False
    else:
        print("❌ Файл live/markers.json не найден")
        print(f"   Ожидаемый путь: {markers_file}")
        return False

def create_server_start_script():
    """Создаем скрипт для запуска сервера"""
    
    print("\n🚀 СОЗДАНИЕ СКРИПТА ЗАПУСКА СЕРВЕРА")
    print("=" * 50)
    
    server_script = '''@echo off
echo Запуск тестового сервера Minecraft с исправленным BlueMap...
cd /d "c:\\Personal\\TestServer"

echo Проверка Java...
java -version
if %errorlevel% neq 0 (
    echo Java не найдена! Установите Java 17+
    pause
    exit /b 1
)

echo Запуск сервера...
java -Xmx2G -Xms1G -jar server.jar nogui

echo Сервер остановлен.
pause
'''
    
    with open("start_test_server.bat", 'w', encoding='utf-8') as f:
        f.write(server_script)
    
    print("✅ Создан скрипт start_test_server.bat")
    print("🎯 Запустите сервер и протестируйте команды")

def main():
    print("🧪 ТЕСТИРОВАНИЕ ИСПРАВЛЕННОГО ПЛАГИНА BLUEMAP")
    print("=" * 60)
    
    # 1. Проверяем есть ли уже собранный плагин
    jar_path = check_bluemap_build()
    
    # 2. Если нет - собираем
    if not jar_path:
        print("\n📦 Плагин не найден, начинаем сборку...")
        if build_bluemap():
            jar_path = check_bluemap_build()
        
        if not jar_path:
            print("\n❌ КРИТИЧЕСКАЯ ОШИБКА: Не удалось собрать плагин")
            return
    
    # 3. Копируем плагин на сервер
    if copy_plugin_to_server(jar_path):
        print("✅ Плагин готов к тестированию")
    else:
        print("❌ Ошибка установки плагина")
        return
    
    # 4. Создаем тестовые команды
    test_commands()
    
    # 5. Создаем скрипт запуска
    create_server_start_script()
    
    # 6. Проверяем live маркеры (если уже есть)
    check_live_markers()
    
    print("\n" + "=" * 60)
    print("🎉 ПОДГОТОВКА ЗАВЕРШЕНА!")
    print("=" * 60)
    
    print("\n📋 СЛЕДУЮЩИЕ ШАГИ:")
    print("1. Запустите сервер: start_test_server.bat")
    print("2. Зайдите в игру или используйте консоль сервера")
    print("3. Выполните команды из test_commands.txt")
    print("4. Проверьте веб-интерфейс: http://localhost:8100")
    print("5. Проверьте файл live/markers.json")
    
    print("\n🔍 ЧТО ПРОВЕРИТЬ:")
    print("✅ Команды /bluemap markers addset создают маркеры с числовыми ID")
    print("✅ Маркеры сохраняются в live/markers.json автоматически")
    print("✅ Веб-интерфейс показывает маркеры в категориях")
    print("✅ Русские названия отображаются корректно")
    print("✅ Поиск маркеров работает по русским названиям")

if __name__ == "__main__":
    main() 