#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Копирование файлов BlueMap в тестовый сервер
"""

import os
import shutil
from pathlib import Path

def copy_to_testserver():
    """Копирует файлы BlueMap в тестовый сервер"""
    
    print("=" * 60)
    print("🚀 КОПИРОВАНИЕ ФАЙЛОВ В ТЕСТОВЫЙ СЕРВЕР")
    print("=" * 60)
    
    # Пути
    source_dir = "bluemap-test/web"
    target_dir = r"c:\Personal\TestServer\plugins\BlueMap\web"
    
    # Создаём структуру папок
    target_world = os.path.join(target_dir, "maps", "world")
    target_assets = os.path.join(target_dir, "assets")
    
    os.makedirs(target_world, exist_ok=True)
    os.makedirs(target_assets, exist_ok=True)
    
    print(f"📁 Создал папки: {target_world}")
    print(f"📁 Создал папки: {target_assets}")
    
    # Список файлов для копирования
    files_to_copy = [
        # Файлы маркеров
        ("maps/world/markers.json", "maps/world/markers.json", "📍 Маркеры"),
        ("maps/world/live-players.json", "maps/world/live-players.json", "👥 Игроки онлайн"),
        ("maps/world/settings.json", "maps/world/settings.json", "⚙️ Настройки карты"),
        
        # Основные веб-файлы
        ("index.html", "index.html", "🌐 Главная страница"),
        ("settings.json", "settings.json", "⚙️ Настройки BlueMap"),
        ("favicon.ico", "favicon.ico", "🎨 Иконка сайта"),
        ("favicon.png", "favicon.png", "🎨 Иконка PNG"),
        
        # JavaScript и CSS
        ("assets/index-BIEfirVm.js", "assets/index-BIEfirVm.js", "📜 JavaScript"),
        ("assets/index-BgiqB2rB.css", "assets/index-BgiqB2rB.css", "🎨 CSS стили"),
        ("assets/manifest-Ciy5FJKl.webmanifest", "assets/manifest-Ciy5FJKl.webmanifest", "📱 Web манифест"),
    ]
    
    copied_count = 0
    
    # Копируем файлы
    for source_rel, target_rel, description in files_to_copy:
        source_path = os.path.join(source_dir, source_rel)
        target_path = os.path.join(target_dir, target_rel)
        
        if os.path.exists(source_path):
            try:
                # Создаём папку если не существует
                os.makedirs(os.path.dirname(target_path), exist_ok=True)
                
                shutil.copy2(source_path, target_path)
                print(f"✅ {description}: {os.path.basename(source_path)}")
                copied_count += 1
            except Exception as e:
                print(f"❌ Ошибка копирования {source_path}: {e}")
        else:
            print(f"⚠️ Файл не найден: {source_path}")
    
    # Копируем иконки маркеров
    print("\n🎨 Копирую иконки маркеров...")
    
    icons_source = os.path.join(source_dir, "assets")
    icons_target = target_assets
    
    icon_count = 0
    
    if os.path.exists(icons_source):
        for filename in os.listdir(icons_source):
            if filename.endswith('.png'):
                source_icon = os.path.join(icons_source, filename)
                target_icon = os.path.join(icons_target, filename)
                
                try:
                    shutil.copy2(source_icon, target_icon)
                    icon_count += 1
                except Exception as e:
                    print(f"❌ Ошибка копирования иконки {filename}: {e}")
    
    print(f"✅ Скопировано {icon_count} иконок")
    
    print("\n" + "=" * 60)
    print("📊 РЕЗУЛЬТАТ КОПИРОВАНИЯ")
    print("=" * 60)
    print(f"✅ Скопировано файлов: {copied_count}")
    print(f"🎨 Скопировано иконок: {icon_count}")
    print(f"📂 Целевая папка: {target_dir}")
    
    # Проверяем основные файлы
    print("\n🔍 ПРОВЕРКА ФАЙЛОВ:")
    
    check_files = [
        "maps/world/markers.json",
        "maps/world/live-players.json", 
        "index.html",
        "assets/index-BIEfirVm.js"
    ]
    
    for check_file in check_files:
        full_path = os.path.join(target_dir, check_file)
        if os.path.exists(full_path):
            size = os.path.getsize(full_path)
            print(f"✅ {check_file} ({size:,} байт)")
        else:
            print(f"❌ {check_file} НЕ НАЙДЕН!")
    
    print("\n" + "=" * 60)
    print("🎯 СЛЕДУЮЩИЕ ШАГИ:")
    print("=" * 60)
    print("1. 🔧 Установите Bukkit сервер (см. BUKKIT_SERVER_SETUP.md)")
    print("2. 🗺️ У вас должно быть 118 маркеров в файле markers.json")
    print("3. 🚀 Запустите сервер: java -jar craftbukkit-1.5.2.jar nogui")
    print("4. 🌐 Откройте: http://localhost:8100")
    print("5. 🔍 Проверьте логи сервера на ошибки загрузки плагина")
    print("=" * 60)

if __name__ == "__main__":
    copy_to_testserver() 