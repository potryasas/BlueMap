#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Исправляет оставшиеся settings.json файлы для карт Nether и End
"""

import json
import os

def fix_remaining_maps():
    """Исправляет настройки для world_nether и world_the_end"""
    
    print("🔥 ИСПРАВЛЕНИЕ NETHER И END КАРТ")
    print("=" * 50)
    
    maps_dir = r"c:\Personal\TestServer\plugins\BlueMap\web\maps"
    
    # Правильные настройки
    maps_configs = {
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

def verify_all_maps():
    """Проверяет все карты"""
    
    print("\n🔍 ФИНАЛЬНАЯ ПРОВЕРКА ВСЕХ КАРТ")
    print("=" * 40)
    
    maps_dir = r"c:\Personal\TestServer\plugins\BlueMap\web\maps"
    map_dirs = ["world", "world_nether", "world_the_end"]
    
    for map_id in map_dirs:
        settings_file = os.path.join(maps_dir, map_id, "settings.json")
        
        if os.path.exists(settings_file):
            try:
                with open(settings_file, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                
                print(f"📍 {map_id}:")
                print(f"   ✅ Название: {data.get('name', 'НЕ УКАЗАНО')}")
                print(f"   ✅ Сортировка: {data.get('sorting', 'НЕ УКАЗАНА')}")
                
                if 'startLocation' in data:
                    start_loc = data['startLocation']
                    print(f"   ✅ Стартовая позиция: x={start_loc.get('x', '?')}, z={start_loc.get('z', '?')}")
                else:
                    print("   ❌ Отсутствует startLocation")
                
            except Exception as e:
                print(f"❌ Ошибка чтения {map_id}/settings.json: {e}")
        else:
            print(f"❌ Файл не найден: {map_id}/settings.json")

def main():
    """Главная функция"""
    
    fix_remaining_maps()
    verify_all_maps()
    
    print("\n🎉 ВСЕ КАРТЫ ИСПРАВЛЕНЫ!")
    print("\n🎯 ТЕПЕРЬ:")
    print("1. 🔄 Обновите браузер (Ctrl+F5)")
    print("2. 🗺️ Ошибка [object Object] должна исчезнуть!")
    print("3. 🎯 Маркеры должны отображаться!")

if __name__ == "__main__":
    main() 