#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Конвертер маркеров для BlueMap
Преобразует маркеры из _markers_/marker_world.json в формат BlueMap
"""

import json
import os
import shutil
from pathlib import Path

def convert_markers():
    """Основная функция конвертации маркеров"""
    
    # Пути к файлам
    source_file = "_markers_/marker_world.json"
    target_dir = "bluemap-test/web/maps/world/"
    target_file = target_dir + "markers.json"
    
    print("🔄 Начинаю конвертацию маркеров...")
    
    # Проверяем существование исходного файла
    if not os.path.exists(source_file):
        print(f"❌ Файл {source_file} не найден!")
        return False
    
    # Создаём целевую директорию
    os.makedirs(target_dir, exist_ok=True)
    
    try:
        # Читаем исходный файл
        with open(source_file, 'r', encoding='utf-8') as f:
            source_data = json.load(f)
        
        print(f"✅ Загружен файл: {source_file}")
        
        # Конвертируем в формат BlueMap
        bluemap_markers = convert_to_bluemap_format(source_data)
        
        # Сохраняем в целевой файл
        with open(target_file, 'w', encoding='utf-8') as f:
            json.dump(bluemap_markers, f, ensure_ascii=False, indent=2)
        
        print(f"✅ Сохранён файл: {target_file}")
        
        # Копируем иконки
        copy_marker_icons()
        
        print("🎉 Конвертация завершена успешно!")
        print(f"📁 Маркеры сохранены в: {target_file}")
        print("🌐 Откройте http://localhost:8100 чтобы увидеть маркеры")
        
        return True
        
    except Exception as e:
        print(f"❌ Ошибка при конвертации: {e}")
        return False

def convert_to_bluemap_format(source_data):
    """Конвертирует маркеры в формат BlueMap"""
    
    bluemap_data = {}
    
    if 'sets' not in source_data:
        print("❌ Неверный формат исходного файла!")
        return {}
    
    total_markers = 0
    
    # Обрабатываем каждую категорию
    for set_id, set_data in source_data['sets'].items():
        
        if 'markers' not in set_data:
            continue
            
        category_markers = {}
        
        # Обрабатываем каждый маркер в категории
        for marker_id, marker_data in set_data['markers'].items():
            
            # Получаем иконку
            icon = marker_data.get('icon', 'default')
            if not icon.endswith('.png'):
                icon += '.png'
            
            # Создаём маркер в формате BlueMap
            bluemap_marker = {
                "type": "poi",
                "label": marker_data.get('label', 'Без названия'),
                "icon": f"assets/{icon}",
                "position": {
                    "x": float(marker_data.get('x', 0)),
                    "y": float(marker_data.get('y', 64)),
                    "z": float(marker_data.get('z', 0))
                },
                "anchor": {"x": 16, "y": 16},
                "sorting": 100,
                "listed": True,
                "minDistance": 10,
                "maxDistance": 100000
            }
            
            # Добавляем маркер
            marker_key = f"{marker_id}_{total_markers}"
            category_markers[marker_key] = bluemap_marker
            total_markers += 1
        
        # Добавляем категорию
        if category_markers:
            bluemap_data[set_id] = {
                "label": set_data.get('label', set_id),
                "toggleable": True,
                "defaultHidden": False,
                "sorting": 0,
                "markers": category_markers
            }
    
    print(f"✅ Конвертировано {total_markers} маркеров в {len(bluemap_data)} категориях")
    
    return bluemap_data

def copy_marker_icons():
    """Копирует иконки в веб-папку"""
    
    source_dir = "_markers_/"
    target_dir = "bluemap-test/web/assets/"
    
    # Создаём папку для иконок
    os.makedirs(target_dir, exist_ok=True)
    
    copied_count = 0
    
    # Копируем все PNG файлы
    for filename in os.listdir(source_dir):
        if filename.endswith('.png'):
            source_path = os.path.join(source_dir, filename)
            target_path = os.path.join(target_dir, filename)
            
            try:
                shutil.copy2(source_path, target_path)
                copied_count += 1
            except Exception as e:
                print(f"⚠️ Ошибка копирования {filename}: {e}")
    
    print(f"✅ Скопировано {copied_count} иконок в {target_dir}")

def create_live_players():
    """Создаёт пустой файл live-players.json"""
    
    target_file = "bluemap-test/web/maps/world/live-players.json"
    live_players_data = {}
    
    try:
        with open(target_file, 'w', encoding='utf-8') as f:
            json.dump(live_players_data, f, ensure_ascii=False, indent=2)
        
        print(f"✅ Создан файл: {target_file}")
        
    except Exception as e:
        print(f"❌ Ошибка создания live-players.json: {e}")

def create_settings_json():
    """Создаёт settings.json для карты мира"""
    
    target_file = "bluemap-test/web/maps/world/settings.json"
    
    settings_data = {
        "name": "world",
        "sorting": 0,
        "startLocation": {
            "x": 0,
            "z": 0
        }
    }
    
    try:
        with open(target_file, 'w', encoding='utf-8') as f:
            json.dump(settings_data, f, ensure_ascii=False, indent=2)
        
        print(f"✅ Создан файл: {target_file}")
        
    except Exception as e:
        print(f"❌ Ошибка создания settings.json: {e}")

def main():
    """Главная функция"""
    
    print("=" * 50)
    print("🗺️  КОНВЕРТЕР МАРКЕРОВ BlueMap")
    print("=" * 50)
    
    # Проверяем структуру файлов
    if not os.path.exists("_markers_"):
        print("❌ Папка _markers_ не найдена!")
        return
    
    if not os.path.exists("bluemap-test"):
        print("❌ Папка bluemap-test не найдена!")
        print("💡 Создайте папку bluemap-test/web/maps/world/ вручную")
        return
    
    # Выполняем конвертацию
    success = convert_markers()
    
    if success:
        # Создаём дополнительные файлы
        create_live_players()
        create_settings_json()
        
        print("\n" + "=" * 50)
        print("🎯 СЛЕДУЮЩИЕ ШАГИ:")
        print("=" * 50)
        print("1. 🔧 Установите Bukkit сервер (см. BUKKIT_SERVER_SETUP.md)")
        print("2. 📂 Скопируйте файлы в plugins/BlueMap/web/")
        print("3. 🚀 Запустите сервер с плагином BlueMap")
        print("4. 🌐 Откройте http://localhost:8100")
        print("=" * 50)
    else:
        print("\n❌ Конвертация не удалась")

if __name__ == "__main__":
    main() 