#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Команды для управления маркерами с числовыми ID
"""

import os
import json
from datetime import datetime

def load_markers():
    """Загружает маркеры"""
    markers_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live\markers.json"
    meta_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\markers_meta.json"
    
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
        r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world"
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
