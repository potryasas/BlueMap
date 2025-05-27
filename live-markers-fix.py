#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Исправление системы живых маркеров BlueMap
Создание live/markers.json аналогично live/players.json
"""

import os
import json
import time
import shutil
from pathlib import Path

def create_live_directory():
    """Создает директорию live если её нет"""
    
    live_dirs = [
        r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live",
        "bluemap-test/web/maps/world/live"
    ]
    
    for live_dir in live_dirs:
        if not os.path.exists(live_dir):
            os.makedirs(live_dir, exist_ok=True)
            print(f"✅ Создана директория: {live_dir}")
        else:
            print(f"📁 Директория уже существует: {live_dir}")

def convert_markers_to_live_format():
    """Конвертирует статические маркеры в формат live маркеров"""
    
    # Пути к файлам
    source_file = "bluemap-test/web/maps/world/markers.json"
    target_files = [
        "bluemap-test/web/maps/world/live/markers.json",
        r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live\markers.json"
    ]
    
    if not os.path.exists(source_file):
        print(f"❌ Исходный файл не найден: {source_file}")
        return False
    
    try:
        # Читаем исходный файл маркеров
        with open(source_file, 'r', encoding='utf-8') as f:
            markers_data = json.load(f)
        
        print(f"📖 Загружено {len(markers_data)} категорий маркеров")
        
        # Создаем live/markers.json для каждого пути
        for target_file in target_files:
            # Создаем директорию если нужно
            os.makedirs(os.path.dirname(target_file), exist_ok=True)
            
            # Записываем данные в формате live маркеров
            with open(target_file, 'w', encoding='utf-8') as f:
                json.dump(markers_data, f, ensure_ascii=False, indent=2)
            
            print(f"✅ Создан live файл: {target_file}")
        
        return True
        
    except Exception as e:
        print(f"❌ Ошибка конвертации: {e}")
        return False

def create_live_players_fallback():
    """Создает fallback файл live/players.json если его нет"""
    
    player_files = [
        "bluemap-test/web/maps/world/live/players.json",
        r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\live\players.json"
    ]
    
    fallback_players = {
        "players": []
    }
    
    for player_file in player_files:
        if not os.path.exists(player_file):
            os.makedirs(os.path.dirname(player_file), exist_ok=True)
            
            with open(player_file, 'w', encoding='utf-8') as f:
                json.dump(fallback_players, f, ensure_ascii=False, indent=2)
            
            print(f"✅ Создан live/players.json: {player_file}")
        else:
            print(f"📁 Файл уже существует: {player_file}")

def update_webapp_settings():
    """Обновляет настройки веб-приложения для поддержки live маркеров"""
    
    settings_files = [
        "bluemap-test/web/maps/world/settings.json",
        r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\settings.json"
    ]
    
    for settings_file in settings_files:
        if os.path.exists(settings_file):
            try:
                with open(settings_file, 'r', encoding='utf-8') as f:
                    settings = json.load(f)
                
                # Добавляем поддержку live маркеров
                if 'live' not in settings:
                    settings['live'] = {}
                
                settings['live']['markers'] = True
                settings['live']['players'] = True
                
                with open(settings_file, 'w', encoding='utf-8') as f:
                    json.dump(settings, f, ensure_ascii=False, indent=2)
                
                print(f"✅ Обновлен settings.json: {settings_file}")
                
            except Exception as e:
                print(f"⚠️ Ошибка обновления настроек {settings_file}: {e}")

def create_simple_http_server_test():
    """Создает простой HTTP сервер для тестирования live данных"""
    
    test_script = """#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import http.server
import socketserver
import os
import json

class CustomHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        # Логирование запросов
        print(f"🌐 REQUEST: {self.path}")
        
        # Специальная обработка live данных
        if self.path.endswith('/live/markers.json'):
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # Читаем данные маркеров
            markers_file = "live/markers.json"
            if os.path.exists(markers_file):
                with open(markers_file, 'r', encoding='utf-8') as f:
                    data = f.read()
                self.wfile.write(data.encode('utf-8'))
                print("✅ Отправлены live markers")
            else:
                self.wfile.write(b'{}')
                print("⚠️ Live markers не найдены")
            return
        
        if self.path.endswith('/live/players.json'):
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # Читаем данные игроков
            players_file = "live/players.json"
            if os.path.exists(players_file):
                with open(players_file, 'r', encoding='utf-8') as f:
                    data = f.read()
                self.wfile.write(data.encode('utf-8'))
                print("✅ Отправлены live players")
            else:
                self.wfile.write(b'{"players":[]}')
                print("⚠️ Live players не найдены")
            return
        
        # Обычная обработка
        return super().do_GET()

# Запуск сервера
PORT = 8100
os.chdir("bluemap-test/web/maps/world")

with socketserver.TCPServer(("", PORT), CustomHandler) as httpd:
    print(f"🚀 HTTP сервер запущен на порту {PORT}")
    print(f"🌐 Откройте: http://localhost:{PORT}")
    print("📍 Live markers: http://localhost:8100/live/markers.json")
    print("👥 Live players: http://localhost:8100/live/players.json")
    httpd.serve_forever()
"""
    
    with open("test-live-server.py", 'w', encoding='utf-8') as f:
        f.write(test_script)
    
    print("✅ Создан test-live-server.py для тестирования live данных")

def main():
    """Основная функция исправления"""
    
    print("🔧 ИСПРАВЛЕНИЕ СИСТЕМЫ ЖИВЫХ МАРКЕРОВ")
    print("=" * 50)
    
    # 1. Создаем директории live
    print("\n1️⃣ Создание директорий live...")
    create_live_directory()
    
    # 2. Конвертируем маркеры в live формат
    print("\n2️⃣ Конвертация маркеров в live формат...")
    if convert_markers_to_live_format():
        print("✅ Маркеры успешно конвертированы")
    else:
        print("❌ Ошибка конвертации маркеров")
        return
    
    # 3. Создаем fallback для игроков
    print("\n3️⃣ Создание fallback для live/players.json...")
    create_live_players_fallback()
    
    # 4. Обновляем настройки веб-приложения
    print("\n4️⃣ Обновление настроек веб-приложения...")
    update_webapp_settings()
    
    # 5. Создаем тестовый сервер
    print("\n5️⃣ Создание тестового HTTP сервера...")
    create_simple_http_server_test()
    
    print("\n" + "=" * 50)
    print("🎉 ИСПРАВЛЕНИЕ ЗАВЕРШЕНО!")
    print("=" * 50)
    
    print("\n📋 ИНСТРУКЦИИ ДЛЯ ТЕСТИРОВАНИЯ:")
    print("1. Запустите: python test-live-server.py")
    print("2. Откройте: http://localhost:8100")
    print("3. Проверьте отображение маркеров")
    print("4. Проверьте консоль браузера на ошибки")
    
    print("\n🔍 ПРОВЕРКА LIVE ДАННЫХ:")
    print("- http://localhost:8100/live/markers.json")
    print("- http://localhost:8100/live/players.json")
    
    print("\n💡 ПРИМЕЧАНИЕ:")
    print("Маркеры теперь должны отображаться как 'живые'")
    print("аналогично игрокам, но статично (не двигаются)")

if __name__ == "__main__":
    main() 