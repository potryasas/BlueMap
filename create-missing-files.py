#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Создаёт недостающие файлы для BlueMap
"""

import os
import json
import shutil

def create_world_index():
    """Создаёт index.html для карты мира"""
    
    print("=" * 60)
    print("📄 СОЗДАНИЕ INDEX.HTML ДЛЯ КАРТЫ МИРА")
    print("=" * 60)
    
    world_index_content = '''<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>BlueMap - World</title>
    <script>
        // Перенаправляем на главную страницу с выбранной картой
        window.location.href = '/';
    </script>
</head>
<body>
    <p>Redirecting to main BlueMap interface...</p>
</body>
</html>'''
    
    index_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\index.html"
    
    try:
        with open(index_file, 'w', encoding='utf-8') as f:
            f.write(world_index_content)
        
        print(f"✅ Создан файл: {index_file}")
        return True
        
    except Exception as e:
        print(f"❌ Ошибка создания index.html: {e}")
        return False

def create_live_players():
    """Создаёт файл live-players.json"""
    
    print("\n📊 СОЗДАНИЕ LIVE-PLAYERS.JSON")
    
    live_players_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live-players.json"
    
    live_players_data = {}
    
    try:
        with open(live_players_file, 'w', encoding='utf-8') as f:
            json.dump(live_players_data, f, ensure_ascii=False, indent=2)
        
        print(f"✅ Создан файл: live-players.json")
        return True
        
    except Exception as e:
        print(f"❌ Ошибка создания live-players.json: {e}")
        return False

def fix_main_index():
    """Исправляет главный index.html"""
    
    print("\n🌐 ИСПРАВЛЕНИЕ ГЛАВНОГО INDEX.HTML")
    
    main_index = r"c:\Personal\TestServer\plugins\BlueMap\web\index.html"
    source_index = "bluemap-test/web/index.html"
    
    if os.path.exists(source_index):
        try:
            # Создаём резервную копию
            if os.path.exists(main_index):
                backup_file = main_index + ".backup"
                shutil.copy2(main_index, backup_file)
                print(f"💾 Резервная копия: index.html.backup")
            
            # Копируем новый файл
            shutil.copy2(source_index, main_index)
            print(f"✅ Обновлён главный index.html")
            return True
            
        except Exception as e:
            print(f"❌ Ошибка обновления index.html: {e}")
            return False
    else:
        print(f"⚠️ Исходный файл не найден: {source_index}")
        return False

def check_web_structure():
    """Проверяет структуру веб-файлов"""
    
    print("\n🔍 ПРОВЕРКА СТРУКТУРЫ ВЕБ-ФАЙЛОВ")
    
    web_root = r"c:\Personal\TestServer\plugins\BlueMap\web"
    
    required_files = [
        "index.html",
        "settings.json",
        "maps/world/markers.json",
        "maps/world/settings.json",
        "maps/world/world.json",
        "maps/world/index.html",
        "assets/index-BIEfirVm.js"
    ]
    
    missing_files = []
    
    for file_path in required_files:
        full_path = os.path.join(web_root, file_path)
        if os.path.exists(full_path):
            size = os.path.getsize(full_path)
            print(f"✅ {file_path} ({size:,} байт)")
        else:
            print(f"❌ {file_path} ОТСУТСТВУЕТ")
            missing_files.append(file_path)
    
    return missing_files

def create_settings_json():
    """Создаёт правильный settings.json"""
    
    print("\n⚙️ СОЗДАНИЕ SETTINGS.JSON")
    
    settings_data = {
        "version": 1,
        "maps": {
            "world": {
                "enabled": True,
                "name": "World",
                "world": "world",
                "sorting": 0
            }
        },
        "scripts": [],
        "styles": []
    }
    
    settings_file = r"c:\Personal\TestServer\plugins\BlueMap\web\settings.json"
    
    try:
        with open(settings_file, 'w', encoding='utf-8') as f:
            json.dump(settings_data, f, ensure_ascii=False, indent=2)
        
        print(f"✅ Создан settings.json")
        return True
        
    except Exception as e:
        print(f"❌ Ошибка создания settings.json: {e}")
        return False

def main():
    """Главная функция"""
    
    print("🔧 СОЗДАНИЕ НЕДОСТАЮЩИХ ФАЙЛОВ BlueMap")
    print("=" * 60)
    
    success_count = 0
    
    # Создаём недостающие файлы
    if create_world_index():
        success_count += 1
    
    if create_live_players():
        success_count += 1
    
    if fix_main_index():
        success_count += 1
    
    if create_settings_json():
        success_count += 1
    
    # Проверяем структуру
    missing_files = check_web_structure()
    
    print("\n" + "=" * 60)
    print("📊 РЕЗУЛЬТАТ:")
    print("=" * 60)
    print(f"✅ Создано/обновлено файлов: {success_count}")
    
    if missing_files:
        print(f"⚠️ Остались отсутствующие файлы: {len(missing_files)}")
        for file in missing_files:
            print(f"   - {file}")
    else:
        print("🎉 Все необходимые файлы на месте!")
    
    print("\n🎯 СЛЕДУЮЩИЕ ШАГИ:")
    print("1. 🔄 Перезагрузите http://localhost:8100")
    print("2. 🧹 Очистите кэш браузера (Ctrl+F5)")
    print("3. 🔍 Проверьте консоль браузера (F12) на ошибки")
    print("4. 📱 Попробуйте инкогнито-режим браузера")

if __name__ == "__main__":
    main() 