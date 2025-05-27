#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Исправление формата маркеров для правильного отображения в веб-интерфейсе
+ Числовые ID + Русские названия
"""

import os
import json
import re
from datetime import datetime

def check_current_format():
    """Проверяем текущий формат файла маркеров"""
    
    print("🔍 АНАЛИЗ ТЕКУЩЕГО ФОРМАТА МАРКЕРОВ")
    print("=" * 50)
    
    markers_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live\markers.json"
    
    if not os.path.exists(markers_file):
        print("❌ Файл live/markers.json не найден")
        return None
    
    try:
        with open(markers_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        print(f"📊 Категорий: {len(data)}")
        
        # Анализируем структуру
        for cat_name, cat_data in list(data.items())[:2]:  # Первые 2 категории
            print(f"\n📁 Категория: {cat_name}")
            print(f"   Тип: {type(cat_data)}")
            
            if isinstance(cat_data, dict):
                print(f"   Ключи: {list(cat_data.keys())}")
                
                if 'markers' in cat_data:
                    markers = cat_data['markers']
                    print(f"   Маркеров: {len(markers)}")
                    
                    if len(markers) > 0:
                        first_marker_id = list(markers.keys())[0]
                        first_marker = markers[first_marker_id]
                        print(f"   Первый маркер ID: {first_marker_id}")
                        print(f"   Первый маркер: {list(first_marker.keys()) if isinstance(first_marker, dict) else type(first_marker)}")
                        
                        if isinstance(first_marker, dict):
                            print(f"   Label: {first_marker.get('label', 'НЕТ')}")
                            print(f"   Type: {first_marker.get('type', 'НЕТ')}")
                            print(f"   Position: {first_marker.get('position', 'НЕТ')}")
        
        return data
        
    except Exception as e:
        print(f"❌ Ошибка чтения файла: {e}")
        return None

def fix_markers_format(data):
    """Исправляем формат маркеров для правильного отображения"""
    
    print("\n🔧 ИСПРАВЛЕНИЕ ФОРМАТА МАРКЕРОВ")
    print("=" * 50)
    
    fixed_data = {}
    marker_counter = 1
    
    for cat_name, cat_data in data.items():
        if not isinstance(cat_data, dict) or 'markers' not in cat_data:
            continue
        
        # Создаем правильную структуру категории
        fixed_category = {
            "label": cat_data.get('label', cat_name),
            "toggleable": True,
            "defaultHidden": False,
            "sorting": 0,
            "markers": {}
        }
        
        # Обрабатываем маркеры
        markers = cat_data['markers']
        print(f"📁 Обрабатываю {cat_name}: {len(markers)} маркеров")
        
        for marker_id, marker_data in markers.items():
            if not isinstance(marker_data, dict):
                continue
            
            # Создаем числовой ID если его нет
            if not marker_id.startswith('marker_'):
                new_marker_id = f"marker_{marker_counter}"
                marker_counter += 1
            else:
                new_marker_id = marker_id
                # Извлекаем номер из существующего ID
                match = re.search(r'marker_(\d+)', marker_id)
                if match:
                    num = int(match.group(1))
                    if num >= marker_counter:
                        marker_counter = num + 1
            
            # Исправляем структуру маркера
            fixed_marker = {
                "type": marker_data.get('type', 'poi'),
                "label": marker_data.get('label', f'Маркер {marker_counter}'),
                "position": {
                    "x": float(marker_data.get('position', {}).get('x', 0)),
                    "y": float(marker_data.get('position', {}).get('y', 65)),
                    "z": float(marker_data.get('position', {}).get('z', 0))
                },
                "anchor": {
                    "x": 16,
                    "y": 16
                },
                "sorting": 100,
                "listed": True,
                "minDistance": 10,
                "maxDistance": 100000
            }
            
            # Добавляем иконку
            if 'icon' in marker_data:
                fixed_marker["icon"] = marker_data['icon']
            else:
                # Определяем иконку по категории
                icon_map = {
                    'category_1': 'assets/church.png',    # Города
                    'category_2': 'assets/monument.png',   # Области  
                    'category_3': 'assets/poi.svg',        # Маркеры
                    'category_4': 'assets/village.png',    # Деревни
                    'category_5': 'assets/monument.png',   # Достопримечательности
                    'category_6': 'assets/village.png'     # Посёлки
                }
                fixed_marker["icon"] = icon_map.get(cat_name, 'assets/poi.svg')
            
            # Сохраняем дополнительную информацию для поиска
            fixed_marker["originalName"] = marker_data.get('originalName', marker_data.get('label', ''))
            fixed_marker["categoryName"] = cat_data.get('label', cat_name)
            fixed_marker["originalId"] = marker_data.get('originalId', marker_id)
            
            fixed_category["markers"][new_marker_id] = fixed_marker
        
        fixed_data[cat_name] = fixed_category
    
    print(f"✅ Обработано категорий: {len(fixed_data)}")
    print(f"✅ Следующий ID маркера: marker_{marker_counter}")
    
    return fixed_data, marker_counter

def create_marker_index(data):
    """Создаем индекс для быстрого поиска маркеров"""
    
    print("\n📋 СОЗДАНИЕ ИНДЕКСА МАРКЕРОВ")
    print("=" * 50)
    
    index = {
        "by_id": {},
        "by_name": {},
        "categories": {}
    }
    
    for cat_id, cat_data in data.items():
        if not isinstance(cat_data, dict) or 'markers' not in cat_data:
            continue
        
        cat_name = cat_data.get('label', cat_id)
        category_markers = []
        
        for marker_id, marker_data in cat_data['markers'].items():
            if not isinstance(marker_data, dict):
                continue
            
            marker_name = marker_data.get('label', '')
            
            # Индекс по ID
            index["by_id"][marker_id] = {
                "category_id": cat_id,
                "category_name": cat_name,
                "marker_name": marker_name
            }
            
            # Индекс по имени (для поиска)
            index["by_name"][marker_name.lower()] = {
                "marker_id": marker_id,
                "category_id": cat_id,
                "category_name": cat_name
            }
            
            # Список маркеров категории
            category_markers.append({
                "marker_id": marker_id,
                "marker_name": marker_name
            })
        
        index["categories"][cat_id] = {
            "name": cat_name,
            "marker_count": len(category_markers),
            "markers": category_markers
        }
    
    print(f"✅ Индексировано маркеров: {len(index['by_id'])}")
    print(f"✅ Индексировано имен: {len(index['by_name'])}")
    print(f"✅ Индексировано категорий: {len(index['categories'])}")
    
    return index

def save_fixed_data(data, index, next_marker_id):
    """Сохраняем исправленные данные"""
    
    print("\n💾 СОХРАНЕНИЕ ИСПРАВЛЕННЫХ ДАННЫХ")
    print("=" * 50)
    
    # Пути для сохранения
    paths = [
        "bluemap-test/web/maps/world",
        r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world"
    ]
    
    for base_path in paths:
        live_path = os.path.join(base_path, "live")
        
        # Создаем папку live если нужно
        os.makedirs(live_path, exist_ok=True)
        
        # Сохраняем основной файл маркеров
        markers_file = os.path.join(live_path, "markers.json")
        with open(markers_file, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        
        print(f"✅ Сохранен: {markers_file}")
        
        # Сохраняем индекс
        index_file = os.path.join(base_path, "markers_index.json")
        with open(index_file, 'w', encoding='utf-8') as f:
            json.dump(index, f, ensure_ascii=False, indent=2)
        
        print(f"✅ Сохранен: {index_file}")
        
        # Сохраняем метаданные
        meta_file = os.path.join(base_path, "markers_meta.json")
        meta_data = {
            "next_marker_id": next_marker_id,
            "last_update": datetime.now().isoformat(),
            "total_markers": len(index['by_id']),
            "total_categories": len(index['categories'])
        }
        with open(meta_file, 'w', encoding='utf-8') as f:
            json.dump(meta_data, f, ensure_ascii=False, indent=2)
        
        print(f"✅ Сохранен: {meta_file}")

def create_marker_commands_integration():
    """Создаем интеграцию команд с новым форматом"""
    
    print("\n🎮 СОЗДАНИЕ ИНТЕГРАЦИИ КОМАНД")
    print("=" * 50)
    
    commands_script = '''#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Команды для управления маркерами с числовыми ID
"""

import os
import json
from datetime import datetime

def load_markers():
    """Загружает маркеры"""
    markers_file = r"c:\\Personal\\TestServer\\plugins\\BlueMap\\web\\maps\\world\\live\\markers.json"
    meta_file = r"c:\\Personal\\TestServer\\plugins\\BlueMap\\web\\maps\\world\\markers_meta.json"
    
    if os.path.exists(markers_file):
        with open(markers_file, 'r', encoding='utf-8') as f:
            markers = json.load(f)
    else:
        markers = {}
    
    if os.path.exists(meta_file):
        with open(meta_file, 'r', encoding='utf-8') as f:
            meta = json.load(f)
    else:
        meta = {"next_marker_id": 1}
    
    return markers, meta

def save_markers(markers, meta):
    """Сохраняет маркеры"""
    paths = [
        "bluemap-test/web/maps/world",
        r"c:\\Personal\\TestServer\\plugins\\BlueMap\\web\\maps\\world"
    ]
    
    for base_path in paths:
        live_path = os.path.join(base_path, "live")
        os.makedirs(live_path, exist_ok=True)
        
        # Основной файл
        markers_file = os.path.join(live_path, "markers.json")
        with open(markers_file, 'w', encoding='utf-8') as f:
            json.dump(markers, f, ensure_ascii=False, indent=2)
        
        # Метаданные
        meta_file = os.path.join(base_path, "markers_meta.json")
        meta["last_update"] = datetime.now().isoformat()
        with open(meta_file, 'w', encoding='utf-8') as f:
            json.dump(meta, f, ensure_ascii=False, indent=2)

def add_marker(category_id, name, x, y, z, description=""):
    """Добавляет новый маркер"""
    markers, meta = load_markers()
    
    # Получаем следующий ID
    marker_id = f"marker_{meta['next_marker_id']}"
    meta['next_marker_id'] += 1
    
    # Создаем категорию если нужно
    if category_id not in markers:
        markers[category_id] = {
            "label": f"Категория {category_id}",
            "toggleable": True,
            "defaultHidden": False,
            "sorting": 0,
            "markers": {}
        }
    
    # Добавляем маркер
    markers[category_id]["markers"][marker_id] = {
        "type": "poi",
        "label": name,
        "position": {"x": float(x), "y": float(y), "z": float(z)},
        "anchor": {"x": 16, "y": 16},
        "icon": "assets/poi.svg",
        "sorting": 100,
        "listed": True,
        "minDistance": 10,
        "maxDistance": 100000,
        "originalName": name,
        "categoryName": markers[category_id]["label"],
        "originalId": marker_id
    }
    
    save_markers(markers, meta)
    print(f"✅ Добавлен маркер {marker_id}: {name} в {category_id}")
    return marker_id

def find_marker(search_term):
    """Ищет маркер по имени или ID"""
    markers, _ = load_markers()
    
    found = []
    search_lower = search_term.lower()
    
    for cat_id, cat_data in markers.items():
        if 'markers' not in cat_data:
            continue
        
        for marker_id, marker_data in cat_data['markers'].items():
            marker_name = marker_data.get('label', '').lower()
            
            if (search_lower in marker_name or 
                search_term == marker_id or
                search_lower == marker_name):
                
                found.append({
                    "id": marker_id,
                    "name": marker_data.get('label', ''),
                    "category": cat_id,
                    "position": marker_data.get('position', {})
                })
    
    return found

if __name__ == "__main__":
    print("🎮 КОМАНДЫ МАРКЕРОВ")
    print("Использование:")
    print("add_marker('category_1', 'Новый город', 100, 70, 200)")
    print("find_marker('город')")
'''
    
    with open("marker_commands.py", 'w', encoding='utf-8') as f:
        f.write(commands_script)
    
    print("✅ Создан marker_commands.py")

def main():
    print("🛠️ ИСПРАВЛЕНИЕ ФОРМАТА МАРКЕРОВ")
    print("=" * 50)
    
    # 1. Анализируем текущий формат
    data = check_current_format()
    if not data:
        return
    
    # 2. Исправляем формат
    fixed_data, next_marker_id = fix_markers_format(data)
    
    # 3. Создаем индекс
    index = create_marker_index(fixed_data)
    
    # 4. Сохраняем данные
    save_fixed_data(fixed_data, index, next_marker_id)
    
    # 5. Создаем интеграцию команд
    create_marker_commands_integration()
    
    print("\n" + "=" * 50)
    print("🎉 ИСПРАВЛЕНИЕ ЗАВЕРШЕНО!")
    print("=" * 50)
    
    print("\n📋 ЧТО ИСПРАВЛЕНО:")
    print("✅ Формат маркеров приведен к стандарту BlueMap")
    print("✅ Добавлены числовые ID (marker_1, marker_2, ...)")
    print("✅ Русские названия сохранены")
    print("✅ Создан индекс для поиска")
    print("✅ Интеграция команд обновлена")
    
    print("\n🎮 ИСПОЛЬЗОВАНИЕ КОМАНД:")
    print("from marker_commands import add_marker, find_marker")
    print("add_marker('category_1', 'Новый город', 100, 70, 200)")
    print("find_marker('город')")
    
    print("\n🔄 ПЕРЕЗАПУСТИТЕ ТЕСТОВЫЙ СЕРВЕР:")
    print("python test-server-fix.py")

if __name__ == "__main__":
    main() 