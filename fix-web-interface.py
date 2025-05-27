#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Исправляет веб-интерфейс BlueMap
"""

import os
import shutil

def fix_web_interface():
    """Исправляет веб-интерфейс"""
    
    print("=" * 60)
    print("🔧 ИСПРАВЛЕНИЕ ВЕБ-ИНТЕРФЕЙСА BlueMap")
    print("=" * 60)
    
    # Пути
    source_web = "bluemap-test/web"
    target_web = r"c:\Personal\TestServer\plugins\BlueMap\web"
    
    print(f"📁 Источник: {source_web}")
    print(f"📁 Цель: {target_web}")
    
    # Важные веб-файлы для копирования
    important_files = [
        "index.html",
        "assets/index-BIEfirVm.js",
        "assets/index-BgiqB2rB.css",
        "js/app.js",
        "css/style.css"
    ]
    
    copied_count = 0
    
    for file_path in important_files:
        source_file = os.path.join(source_web, file_path)
        target_file = os.path.join(target_web, file_path)
        
        if os.path.exists(source_file):
            try:
                # Создаём папку если не существует
                os.makedirs(os.path.dirname(target_file), exist_ok=True)
                
                # Делаем резервную копию
                if os.path.exists(target_file):
                    backup_file = target_file + ".backup"
                    shutil.copy2(target_file, backup_file)
                    print(f"💾 Резервная копия: {os.path.basename(backup_file)}")
                
                # Копируем новый файл
                shutil.copy2(source_file, target_file)
                print(f"✅ Обновлён: {file_path}")
                copied_count += 1
                
            except Exception as e:
                print(f"❌ Ошибка копирования {file_path}: {e}")
        else:
            print(f"⚠️ Файл не найден: {source_file}")
    
    print(f"\n✅ Обновлено файлов: {copied_count}")
    
    # Проверяем файл BETA.js
    beta_file = "BETA.js"
    if os.path.exists(beta_file):
        print(f"\n🔧 Найден файл BETA.js ({os.path.getsize(beta_file):,} байт)")
        
        target_beta = os.path.join(target_web, "assets", "beta.js")
        
        try:
            os.makedirs(os.path.dirname(target_beta), exist_ok=True)
            shutil.copy2(beta_file, target_beta)
            print(f"✅ BETA.js скопирован как assets/beta.js")
        except Exception as e:
            print(f"❌ Ошибка копирования BETA.js: {e}")

def create_simple_markers_test():
    """Создаёт простой тест маркеров"""
    
    print("\n" + "=" * 60)
    print("🧪 СОЗДАНИЕ ПРОСТОГО ТЕСТА МАРКЕРОВ")
    print("=" * 60)
    
    # Создаём упрощённый файл маркеров для теста
    simple_markers = {
        "test": {
            "label": "ТЕСТ",
            "toggleable": True,
            "defaultHidden": False,
            "markers": {
                "test_marker": {
                    "type": "poi",
                    "label": "Тестовый маркер",
                    "icon": "assets/default.png",
                    "position": {"x": 0, "y": 64, "z": 0},
                    "anchor": {"x": 16, "y": 16},
                    "sorting": 100,
                    "listed": True
                }
            }
        }
    }
    
    test_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\markers_test.json"
    
    try:
        import json
        with open(test_file, 'w', encoding='utf-8') as f:
            json.dump(simple_markers, f, ensure_ascii=False, indent=2)
        
        print(f"✅ Создан тестовый файл: {test_file}")
        print("🔗 Проверьте: http://localhost:8100/maps/world/markers_test.json")
        
    except Exception as e:
        print(f"❌ Ошибка создания теста: {e}")

def main():
    """Главная функция"""
    
    fix_web_interface()
    create_simple_markers_test()
    
    print("\n" + "=" * 60)
    print("🎯 СЛЕДУЮЩИЕ ШАГИ:")
    print("=" * 60)
    print("1. 🔄 Очистите кэш браузера (Ctrl+F5)")
    print("2. 🔍 Откройте консоль браузера (F12)")
    print("3. 🌐 Перезагрузите http://localhost:8100")
    print("4. 📱 Попробуйте другой браузер")
    print("5. 🧪 Проверьте тестовый файл маркеров")
    print("\n💡 ЕСЛИ ПРОБЛЕМА ОСТАЁТСЯ:")
    print("   Возможно, нужен другой файл BETA.js или обновление BlueMap")

if __name__ == "__main__":
    main() 