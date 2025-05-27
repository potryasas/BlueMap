#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Проверка маркеров на тестовом сервере
"""

import os
import json
import requests
import time

def check_server_files():
    """Проверяем файлы на сервере"""
    
    print("🔍 ПРОВЕРКА ФАЙЛОВ НА СЕРВЕРЕ")
    print("=" * 50)
    
    markers_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live\markers.json"
    
    if os.path.exists(markers_file):
        size = os.path.getsize(markers_file)
        print(f"✅ live/markers.json найден: {size:,} байт")
        
        try:
            with open(markers_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            
            print(f"📊 Категорий маркеров: {len(data)}")
            
            total_markers = 0
            for cat_name, cat_data in data.items():
                if isinstance(cat_data, dict) and 'markers' in cat_data:
                    marker_count = len(cat_data['markers'])
                    total_markers += marker_count
                    print(f"  📍 {cat_name}: {marker_count} маркеров")
                    
                    # Показываем первый маркер для примера
                    if marker_count > 0:
                        first_marker = list(cat_data['markers'].values())[0]
                        if isinstance(first_marker, dict):
                            print(f"    Пример: {first_marker.get('label', 'Без названия')} в ({first_marker.get('position', {}).get('x', 0)}, {first_marker.get('position', {}).get('z', 0)})")
            
            print(f"📍 Всего маркеров: {total_markers}")
            return True
            
        except Exception as e:
            print(f"❌ Ошибка чтения JSON: {e}")
            return False
    else:
        print("❌ Файл live/markers.json не найден")
        return False

def check_web_server():
    """Проверяем работу веб-сервера"""
    
    print("\n🌐 ПРОВЕРКА ВЕБ-СЕРВЕРА")
    print("=" * 50)
    
    urls = [
        "http://localhost:8100/maps/world/live/markers.json",
        "http://localhost:8100/maps/world/live/players.json",
        "http://localhost:8100/maps/world/settings.json"
    ]
    
    for url in urls:
        try:
            print(f"🔗 Проверяю: {url}")
            response = requests.get(url, timeout=5)
            
            if response.status_code == 200:
                print(f"✅ Доступен (код: {response.status_code})")
                
                if 'markers.json' in url:
                    try:
                        markers_data = response.json()
                        marker_count = sum(len(cat.get('markers', {})) for cat in markers_data.values() if isinstance(cat, dict))
                        print(f"📍 Маркеров в веб-ответе: {marker_count}")
                    except:
                        print("⚠️ Ответ не является JSON")
                        
                elif 'players.json' in url:
                    try:
                        players_data = response.json()
                        player_count = len(players_data.get('players', []))
                        print(f"👥 Игроков в веб-ответе: {player_count}")
                    except:
                        print("⚠️ Ответ не является JSON")
                        
                elif 'settings.json' in url:
                    try:
                        settings_data = response.json()
                        print(f"⚙️ Настройки: {list(settings_data.keys())}")
                    except:
                        print("⚠️ Ответ не является JSON")
            else:
                print(f"❌ Недоступен (код: {response.status_code})")
                
        except requests.exceptions.ConnectionError:
            print(f"❌ Соединение отклонено - сервер не запущен")
        except requests.exceptions.Timeout:
            print(f"❌ Таймаут соединения")
        except Exception as e:
            print(f"❌ Ошибка: {e}")
        
        time.sleep(0.5)

def test_main_page():
    """Проверяем главную страницу"""
    
    print("\n🏠 ПРОВЕРКА ГЛАВНОЙ СТРАНИЦЫ")
    print("=" * 50)
    
    try:
        response = requests.get("http://localhost:8100", timeout=10)
        if response.status_code == 200:
            print("✅ Главная страница доступна")
            
            # Проверяем наличие ключевых элементов в HTML
            content = response.text.lower()
            
            checks = [
                ("bluemap", "BlueMap упоминается"),
                ("canvas", "Canvas элемент найден"),
                ("javascript", "JavaScript загружается"),
                ("maps/world", "Ссылка на карту мира")
            ]
            
            for check, description in checks:
                if check in content:
                    print(f"✅ {description}")
                else:
                    print(f"⚠️ {description} - НЕ НАЙДЕНО")
                    
        else:
            print(f"❌ Главная страница недоступна (код: {response.status_code})")
            
    except Exception as e:
        print(f"❌ Ошибка загрузки главной страницы: {e}")

def main():
    print("🧪 ТЕСТИРОВАНИЕ МАРКЕРОВ НА СЕРВЕРЕ")
    print("=" * 50)
    
    # 1. Проверяем файлы
    files_ok = check_server_files()
    
    # 2. Проверяем веб-сервер
    check_web_server()
    
    # 3. Проверяем главную страницу
    test_main_page()
    
    print("\n" + "=" * 50)
    print("🎯 РЕЗУЛЬТАТЫ ДИАГНОСТИКИ")
    print("=" * 50)
    
    if files_ok:
        print("✅ Файлы маркеров в порядке")
        print("💡 РЕКОМЕНДАЦИИ:")
        print("1. Откройте http://localhost:8100 в браузере")
        print("2. Откройте консоль браузера (F12)")
        print("3. Проверьте сетевые запросы на вкладке Network")
        print("4. Ищите запросы к /maps/world/live/markers.json")
        print("5. Проверьте меню маркеров в интерфейсе карты")
        
        print("\n🔍 ДОПОЛНИТЕЛЬНАЯ ОТЛАДКА:")
        print("- Очистите кэш браузера (Ctrl+F5)")
        print("- Проверьте, что нет ошибок JavaScript в консоли")
        print("- Убедитесь, что запросы к live данным выполняются без ошибок")
        
    else:
        print("❌ Проблемы с файлами маркеров")
        print("🔧 ИСПРАВЛЕНИЕ:")
        print("1. Запустите: python live-markers-fix.py")
        print("2. Перезапустите тестовый сервер")

if __name__ == "__main__":
    main() 