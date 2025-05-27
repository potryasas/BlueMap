#!/usr/bin/env python3
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
