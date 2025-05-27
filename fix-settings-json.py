#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Исправляет settings.json для BlueMap
"""

import json
import os

def fix_settings_json():
    """Исправляет файл settings.json"""
    
    print("=" * 60)
    print("🔧 ИСПРАВЛЕНИЕ SETTINGS.JSON")
    print("=" * 60)
    
    settings_file = r"c:\Personal\TestServer\plugins\BlueMap\web\settings.json"
    
    # Правильная структура settings.json
    correct_settings = {
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
    
    try:
        # Создаём резервную копию
        if os.path.exists(settings_file):
            backup_file = settings_file + ".backup"
            with open(settings_file, 'r', encoding='utf-8') as f:
                backup_content = f.read()
            with open(backup_file, 'w', encoding='utf-8') as f:
                f.write(backup_content)
            print(f"💾 Создана резервная копия: settings.json.backup")
        
        # Записываем правильный settings.json
        with open(settings_file, 'w', encoding='utf-8') as f:
            json.dump(correct_settings, f, ensure_ascii=False, indent=2)
        
        print(f"✅ Исправлен файл: {settings_file}")
        
        # Проверяем результат
        with open(settings_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        if 'maps' in data and 'world' in data['maps']:
            print("✅ Структура файла правильная")
            print(f"📍 Найдена карта: {data['maps']['world']['name']}")
            return True
        else:
            print("❌ Структура файла всё ещё неправильная")
            return False
            
    except Exception as e:
        print(f"❌ Ошибка исправления settings.json: {e}")
        return False

def copy_source_settings():
    """Копирует settings.json из источника"""
    
    print("\n📋 КОПИРОВАНИЕ ИСХОДНОГО SETTINGS.JSON")
    
    source_settings = "bluemap-test/web/settings.json"
    target_settings = r"c:\Personal\TestServer\plugins\BlueMap\web\settings.json"
    
    if os.path.exists(source_settings):
        try:
            # Читаем исходный файл
            with open(source_settings, 'r', encoding='utf-8') as f:
                source_data = json.load(f)
            
            print(f"📊 Исходный settings.json:")
            print(f"   Версия: {source_data.get('version', 'не указана')}")
            print(f"   Карт: {len(source_data.get('maps', {}))}")
            
            # Копируем
            import shutil
            shutil.copy2(source_settings, target_settings)
            print(f"✅ Скопирован исходный settings.json")
            return True
            
        except Exception as e:
            print(f"❌ Ошибка копирования: {e}")
            return False
    else:
        print(f"⚠️ Исходный файл не найден: {source_settings}")
        return False

def check_maps_structure():
    """Проверяет структуру карт"""
    
    print("\n🗺️ ПРОВЕРКА СТРУКТУРЫ КАРТ")
    
    settings_file = r"c:\Personal\TestServer\plugins\BlueMap\web\settings.json"
    
    try:
        with open(settings_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        if 'maps' not in data:
            print("❌ Отсутствует секция 'maps'")
            return False
        
        maps = data['maps']
        
        if not isinstance(maps, dict):
            print("❌ 'maps' не является объектом")
            return False
        
        print(f"✅ Найдено карт: {len(maps)}")
        
        for map_id, map_data in maps.items():
            print(f"📍 Карта '{map_id}':")
            print(f"   - Название: {map_data.get('name', 'не указано')}")
            print(f"   - Включена: {map_data.get('enabled', False)}")
            print(f"   - Мир: {map_data.get('world', 'не указан')}")
        
        return True
        
    except Exception as e:
        print(f"❌ Ошибка проверки структуры: {e}")
        return False

def main():
    """Главная функция"""
    
    print("🔧 ИСПРАВЛЕНИЕ НАСТРОЕК BlueMap")
    print("=" * 60)
    
    # Попробуем сначала скопировать исходный файл
    if copy_source_settings():
        print("✅ Использован исходный settings.json")
    else:
        # Если не получилось, создаём правильный вручную
        if fix_settings_json():
            print("✅ Создан новый settings.json")
        else:
            print("❌ Не удалось исправить settings.json")
            return
    
    # Проверяем структуру
    if check_maps_structure():
        print("\n🎉 SETTINGS.JSON ИСПРАВЛЕН!")
        print("\n🎯 СЛЕДУЮЩИЕ ШАГИ:")
        print("1. 🔄 Перезагрузите страницу BlueMap (Ctrl+F5)")
        print("2. 🔍 Проверьте консоль браузера на ошибки")
        print("3. 🗺️ Маркеры должны теперь отображаться!")
    else:
        print("\n❌ Проблема с структурой settings.json остаётся")

if __name__ == "__main__":
    main() 