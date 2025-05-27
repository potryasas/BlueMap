#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Тестовый HTTP сервер для BlueMap с серверными файлами
"""

import http.server
import socketserver
import os
import json
import shutil
from pathlib import Path

class BlueMapHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=r"c:\Personal\TestServer\plugins\BlueMap\web", **kwargs)
    
    def do_GET(self):
        print(f"🌐 REQUEST: {self.path}")
        
        # Специальная обработка для live/markers.json
        if self.path.endswith('/live/markers.json'):
            self.send_response(200)
            self.send_header('Content-type', 'application/json; charset=utf-8')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            markers_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live\markers.json"
            try:
                with open(markers_file, 'r', encoding='utf-8') as f:
                    data = f.read()
                self.wfile.write(data.encode('utf-8'))
                print(f"✅ Отправлены live markers: {len(data)} символов")
            except Exception as e:
                print(f"❌ Ошибка загрузки markers: {e}")
                self.wfile.write(b'{}')
            return
        
        # Специальная обработка для live/players.json
        if self.path.endswith('/live/players.json'):
            self.send_response(200)
            self.send_header('Content-type', 'application/json; charset=utf-8')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            players_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live\players.json"
            try:
                with open(players_file, 'r', encoding='utf-8') as f:
                    data = f.read()
                self.wfile.write(data.encode('utf-8'))
                print(f"✅ Отправлены live players: {len(data)} символов")
            except Exception as e:
                print(f"❌ Ошибка загрузки players: {e}")
                self.wfile.write(b'{"players":[]}')
            return
        
        # Обычная обработка статических файлов
        return super().do_GET()

def check_files():
    """Проверяем наличие и содержимое ключевых файлов"""
    
    print("🔍 ПРОВЕРКА ФАЙЛОВ НА СЕРВЕРЕ")
    print("=" * 50)
    
    # Проверяем основные файлы
    files_to_check = [
        r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live\markers.json",
        r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live\players.json",
        r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\settings.json"
    ]
    
    for file_path in files_to_check:
        if os.path.exists(file_path):
            size = os.path.getsize(file_path)
            print(f"✅ {os.path.basename(file_path)}: {size:,} байт")
            
            # Для JSON файлов проверяем содержимое
            if file_path.endswith('.json'):
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        data = json.load(f)
                    
                    if 'markers.json' in file_path:
                        marker_count = sum(len(cat.get('markers', {})) for cat in data.values() if isinstance(cat, dict))
                        print(f"   📍 Маркеров: {marker_count}")
                        
                        # Показываем первые несколько категорий
                        for cat_name in list(data.keys())[:3]:
                            if isinstance(data[cat_name], dict) and 'markers' in data[cat_name]:
                                markers_in_cat = len(data[cat_name]['markers'])
                                print(f"   - {cat_name}: {markers_in_cat} маркеров")
                    
                    elif 'players.json' in file_path:
                        players = data.get('players', [])
                        print(f"   👥 Игроков: {len(players)}")
                    
                    elif 'settings.json' in file_path:
                        print(f"   ⚙️ Настройки: {list(data.keys())}")
                        
                except Exception as e:
                    print(f"   ❌ Ошибка чтения JSON: {e}")
        else:
            print(f"❌ {os.path.basename(file_path)}: НЕ НАЙДЕН")

def main():
    print("🚀 ТЕСТОВЫЙ BLUEMAP СЕРВЕР")
    print("=" * 50)
    
    # Проверяем файлы
    check_files()
    
    print(f"\n🌐 ЗАПУСК HTTP СЕРВЕРА...")
    print(f"📂 Корневая папка: c:\\Personal\\TestServer\\plugins\\BlueMap\\web")
    
    PORT = 8100
    
    try:
        with socketserver.TCPServer(("", PORT), BlueMapHandler) as httpd:
            print(f"✅ Сервер запущен на порту {PORT}")
            print(f"🌐 Основная страница: http://localhost:{PORT}")
            print(f"📍 Live markers: http://localhost:{PORT}/maps/world/live/markers.json")
            print(f"👥 Live players: http://localhost:{PORT}/maps/world/live/players.json")
            print(f"⚙️ Settings: http://localhost:{PORT}/maps/world/settings.json")
            print()
            print("🎯 ДЛЯ ТЕСТИРОВАНИЯ:")
            print("1. Откройте: http://localhost:8100")
            print("2. Должна загрузиться карта BlueMap")
            print("3. Проверьте консоль браузера (F12) на ошибки")
            print("4. Проверьте меню маркеров в интерфейсе")
            print()
            print("❌ Для остановки: Ctrl+C")
            print("=" * 50)
            
            httpd.serve_forever()
            
    except KeyboardInterrupt:
        print("\n🛑 Сервер остановлен")
    except Exception as e:
        print(f"❌ Ошибка запуска сервера: {e}")

if __name__ == "__main__":
    main() 