#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Исправляет settings.json файлы для отдельных карт BlueMap
"""

import json
import os

def fix_map_settings():
    """Исправляет настройки карт"""
    
    print("=" * 60)
    print("🗺️ ИСПРАВЛЕНИЕ НАСТРОЕК КАРТ")
    print("=" * 60)
    
    maps_dir = r"c:\Personal\TestServer\plugins\BlueMap\web\maps"
    
    # Правильные настройки для каждой карты
    maps_configs = {
        "world": {
            "name": "world",
            "sorting": 0,
            "startLocation": {
                "x": 0,
                "z": 0
            }
        },
        "world_nether": {
            "name": "world_nether", 
            "sorting": 1,
            "startLocation": {
                "x": 0,
                "z": 0
            }
        },
        "world_the_end": {
            "name": "world_the_end",
            "sorting": 2,
            "startLocation": {
                "x": 0,
                "z": 0
            }
        }
    }
    
    for map_id, config in maps_configs.items():
        map_settings_file = os.path.join(maps_dir, map_id, "settings.json")
        
        if os.path.exists(os.path.dirname(map_settings_file)):
            try:
                # Создаём резервную копию
                if os.path.exists(map_settings_file):
                    backup_file = map_settings_file + ".backup"
                    with open(map_settings_file, 'r', encoding='utf-8') as f:
                        backup_content = f.read()
                    with open(backup_file, 'w', encoding='utf-8') as f:
                        f.write(backup_content)
                    print(f"💾 Создана резервная копия: {map_id}/settings.json.backup")
                
                # Записываем правильные настройки
                with open(map_settings_file, 'w', encoding='utf-8') as f:
                    json.dump(config, f, ensure_ascii=False, indent=2)
                
                print(f"✅ Исправлен файл: {map_id}/settings.json")
                
            except Exception as e:
                print(f"❌ Ошибка исправления {map_id}/settings.json: {e}")
        else:
            print(f"⚠️ Директория не найдена: {os.path.dirname(map_settings_file)}")

def copy_source_map_settings():
    """Копирует settings.json файлы из источника"""
    
    print("\n📋 КОПИРОВАНИЕ ИСХОДНЫХ НАСТРОЕК КАРТ")
    
    source_maps_dir = "bluemap-test/web/maps"
    target_maps_dir = r"c:\Personal\TestServer\plugins\BlueMap\web\maps"
    
    map_dirs = ["world", "world_nether", "world_the_end"]
    
    for map_id in map_dirs:
        source_settings = os.path.join(source_maps_dir, map_id, "settings.json")
        target_settings = os.path.join(target_maps_dir, map_id, "settings.json")
        
        if os.path.exists(source_settings):
            try:
                # Читаем исходный файл
                with open(source_settings, 'r', encoding='utf-8') as f:
                    source_data = json.load(f)
                
                print(f"📊 {map_id}/settings.json:")
                print(f"   Название: {source_data.get('name', 'не указано')}")
                print(f"   Сортировка: {source_data.get('sorting', 'не указана')}")
                
                # Создаём целевую директорию если не существует
                os.makedirs(os.path.dirname(target_settings), exist_ok=True)
                
                # Копируем
                import shutil
                shutil.copy2(source_settings, target_settings)
                print(f"✅ Скопирован {map_id}/settings.json")
                
            except Exception as e:
                print(f"❌ Ошибка копирования {map_id}: {e}")
        else:
            print(f"⚠️ Исходный файл не найден: {source_settings}")

def verify_map_settings():
    """Проверяет настройки карт"""
    
    print("\n🔍 ПРОВЕРКА НАСТРОЕК КАРТ")
    
    maps_dir = r"c:\Personal\TestServer\plugins\BlueMap\web\maps"
    map_dirs = ["world", "world_nether", "world_the_end"]
    
    for map_id in map_dirs:
        settings_file = os.path.join(maps_dir, map_id, "settings.json")
        
        if os.path.exists(settings_file):
            try:
                with open(settings_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                
                print(f"📍 {map_id}:")
                print(f"   - Название: {data.get('name', 'НЕ УКАЗАНО')}")
                print(f"   - Сортировка: {data.get('sorting', 'НЕ УКАЗАНА')}")
                
                if 'startLocation' in data:
                    start_loc = data['startLocation']
                    print(f"   - Стартовая позиция: x={start_loc.get('x', '?')}, z={start_loc.get('z', '?')}")
                else:
                    print("   - ⚠️ Отсутствует startLocation")
                
            except Exception as e:
                print(f"❌ Ошибка чтения {map_id}/settings.json: {e}")
        else:
            print(f"❌ Файл не найден: {map_id}/settings.json")

def main():
    """Главная функция"""
    
    print("🗺️ ИСПРАВЛЕНИЕ НАСТРОЕК КАРТ BlueMap")
    print("=" * 60)
    
    # Попробуем сначала скопировать исходные файлы
    copy_source_map_settings()
    
    # Проверяем результат
    verify_map_settings()
    
    print("\n🎉 НАСТРОЙКИ КАРТ ИСПРАВЛЕНЫ!")
    print("\n🎯 СЛЕДУЮЩИЕ ШАГИ:")
    print("1. 🔄 Перезагрузите страницу BlueMap (Ctrl+F5)")
    print("2. 🔍 Проверьте консоль браузера на ошибки")
    print("3. 🗺️ Ошибка [object Object] должна исчезнуть!")

if __name__ == "__main__":
    main() 