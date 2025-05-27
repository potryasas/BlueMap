#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Диагностика проблем с маркерами BlueMap
"""

import os
import json
import time
import requests
from pathlib import Path

def check_file_exists():
    """Проверяет наличие и содержимое файла маркеров"""
    
    print("=" * 60)
    print("📁 ПРОВЕРКА ФАЙЛА МАРКЕРОВ")
    print("=" * 60)
    
    markers_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\markers.json"
    
    if not os.path.exists(markers_file):
        print(f"❌ Файл не найден: {markers_file}")
        return False
    
    file_size = os.path.getsize(markers_file)
    print(f"📊 Размер файла: {file_size:,} байт")
    
    if file_size < 10000:
        print("⚠️ Файл слишком маленький!")
        return False
    
    # Проверяем JSON
    try:
        with open(markers_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        total_markers = 0
        print("📍 Категории маркеров:")
        
        for category_name, category_data in data.items():
            if isinstance(category_data, dict) and 'markers' in category_data:
                marker_count = len(category_data['markers'])
                total_markers += marker_count
                print(f"  - {category_name}: {marker_count} маркеров")
        
        print(f"✅ Всего маркеров: {total_markers}")
        return total_markers > 50
        
    except json.JSONDecodeError as e:
        print(f"❌ Ошибка JSON: {e}")
        return False
    except Exception as e:
        print(f"❌ Ошибка: {e}")
        return False

def check_web_access():
    """Проверяет доступность веб-интерфейса"""
    
    print("\n" + "=" * 60)
    print("🌐 ПРОВЕРКА ВЕБ-ДОСТУПА")
    print("=" * 60)
    
    urls_to_check = [
        "http://localhost:8100",
        "http://localhost:8100/maps/world/markers.json",
        "http://127.0.0.1:8100",
        "http://127.0.0.1:8100/maps/world/markers.json"
    ]
    
    for url in urls_to_check:
        try:
            print(f"🔗 Проверяю: {url}")
            response = requests.get(url, timeout=5)
            
            if response.status_code == 200:
                print(f"✅ Доступен (код: {response.status_code})")
                
                if 'markers.json' in url:
                    try:
                        markers_data = response.json()
                        marker_count = sum(len(cat.get('markers', {})) for cat in markers_data.values() if isinstance(cat, dict))
                        print(f"📍 Маркеров в веб-версии: {marker_count}")
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

def check_server_logs():
    """Проверяет логи сервера"""
    
    print("\n" + "=" * 60)
    print("📄 АНАЛИЗ ЛОГОВ СЕРВЕРА")
    print("=" * 60)
    
    log_file = r"c:\Personal\TestServer\server.log"
    
    if not os.path.exists(log_file):
        print(f"❌ Лог файл не найден: {log_file}")
        return
    
    try:
        with open(log_file, 'r', encoding='utf-8', errors='ignore') as f:
            lines = f.readlines()
        
        # Ищем последние упоминания BlueMap
        bluemap_lines = []
        for i, line in enumerate(lines):
            if 'bluemap' in line.lower():
                bluemap_lines.append((i, line.strip()))
        
        if bluemap_lines:
            print("🔵 Последние упоминания BlueMap:")
            for line_num, line in bluemap_lines[-10:]:  # Последние 10
                print(f"  [{line_num+1}] {line}")
        else:
            print("❌ BlueMap не найден в логах")
        
        # Ищем ошибки
        error_lines = []
        for i, line in enumerate(lines[-100:], len(lines)-100):  # Последние 100 строк
            if any(word in line.lower() for word in ['error', 'exception', 'failed', 'warning']):
                error_lines.append((i, line.strip()))
        
        if error_lines:
            print("\n⚠️ Последние ошибки:")
            for line_num, line in error_lines[-5:]:  # Последние 5 ошибок
                print(f"  [{line_num+1}] {line}")
        
    except Exception as e:
        print(f"❌ Ошибка чтения логов: {e}")

def check_bluemap_config():
    """Проверяет конфигурацию BlueMap"""
    
    print("\n" + "=" * 60)
    print("⚙️ ПРОВЕРКА КОНФИГУРАЦИИ BlueMap")
    print("=" * 60)
    
    config_files = [
        r"c:\Personal\TestServer\plugins\BlueMap\bluemap.properties",
        r"c:\Personal\TestServer\plugins\BlueMap\markers.properties",
        r"c:\Personal\TestServer\plugins\BlueMap\marker-sets.properties"
    ]
    
    for config_file in config_files:
        if os.path.exists(config_file):
            print(f"✅ {os.path.basename(config_file)} найден")
            
            try:
                with open(config_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                if 'marker' in content.lower():
                    print(f"   📍 Содержит настройки маркеров")
                    
            except Exception as e:
                print(f"   ❌ Ошибка чтения: {e}")
        else:
            print(f"❌ {os.path.basename(config_file)} НЕ найден")

def main():
    """Главная функция диагностики"""
    
    print("🔍 ДИАГНОСТИКА ПРОБЛЕМ С МАРКЕРАМИ BlueMap")
    print("=" * 60)
    
    # Проверяем файл маркеров
    file_ok = check_file_exists()
    
    # Проверяем веб-доступ
    check_web_access()
    
    # Проверяем логи
    check_server_logs()
    
    # Проверяем конфигурацию
    check_bluemap_config()
    
    print("\n" + "=" * 60)
    print("🎯 РЕКОМЕНДАЦИИ:")
    print("=" * 60)
    
    if not file_ok:
        print("❌ Файл маркеров повреждён или пуст!")
        print("🔧 Решение: Запустите fix-markers-file.py ещё раз")
    else:
        print("✅ Файл маркеров в порядке")
        print("🔧 Возможные проблемы:")
        print("  1. Сервер не перезапущен после обновления файла")
        print("  2. BlueMap кэширует старую версию")
        print("  3. Проблема с кодировкой UTF-8")
        print("  4. Конфликт с другими плагинами")
    
    print("\n🚀 ПОПРОБУЙТЕ:")
    print("1. Полностью остановите сервер")
    print("2. Удалите файл: plugins/BlueMap/web/maps/world/markers_backup.json")
    print("3. Перезапустите сервер")
    print("4. Выполните в игре: /bluemap reload")
    print("5. Очистите кэш браузера (Ctrl+F5)")

if __name__ == "__main__":
    main() 