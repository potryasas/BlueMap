#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Исправляет файл маркеров в тестовом сервере
"""

import os
import shutil
import json

def fix_markers_file():
    """Исправляет файл маркеров"""
    
    print("=" * 60)
    print("🔧 ИСПРАВЛЕНИЕ ФАЙЛА МАРКЕРОВ")
    print("=" * 60)
    
    # Пути к файлам
    source_file = "bluemap-test/web/maps/world/markers.json"
    target_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\markers.json"
    backup_file = r"c:\Personal\TestServer\plugins\BlueMap\web\maps\world\markers_backup.json"
    
    print(f"📁 Источник: {source_file}")
    print(f"📁 Цель: {target_file}")
    
    # Проверяем существование файлов
    if not os.path.exists(source_file):
        print(f"❌ Файл-источник не найден: {source_file}")
        return False
    
    if not os.path.exists(target_file):
        print(f"❌ Целевой файл не найден: {target_file}")
        return False
    
    # Проверяем размеры файлов
    source_size = os.path.getsize(source_file)
    target_size = os.path.getsize(target_file)
    
    print(f"📊 Размер исходного файла: {source_size:,} байт")
    print(f"📊 Размер текущего файла: {target_size:,} байт")
    
    if source_size <= target_size:
        print("⚠️ Исходный файл не больше текущего!")
        return False
    
    try:
        # Создаём резервную копию
        print("💾 Создаю резервную копию...")
        shutil.copy2(target_file, backup_file)
        print(f"✅ Резервная копия: {backup_file}")
        
        # Копируем новый файл
        print("📋 Копирую новый файл маркеров...")
        shutil.copy2(source_file, target_file)
        
        # Проверяем результат
        new_size = os.path.getsize(target_file)
        print(f"✅ Новый размер файла: {new_size:,} байт")
        
        # Проверяем JSON
        with open(target_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
        
        total_markers = 0
        for category_name, category_data in data.items():
            if 'markers' in category_data:
                marker_count = len(category_data['markers'])
                total_markers += marker_count
                print(f"📍 {category_name}: {marker_count} маркеров")
        
        print(f"🎉 Всего маркеров: {total_markers}")
        
        if total_markers > 100:
            print("✅ ФАЙЛ МАРКЕРОВ УСПЕШНО ОБНОВЛЁН!")
            print("🔄 ПЕРЕЗАПУСТИТЕ СЕРВЕР для применения изменений")
            return True
        else:
            print("⚠️ Маркеров мало, возможно проблема с файлом")
            return False
            
    except Exception as e:
        print(f"❌ Ошибка: {e}")
        
        # Восстанавливаем резервную копию
        if os.path.exists(backup_file):
            print("🔄 Восстанавливаю резервную копию...")
            shutil.copy2(backup_file, target_file)
        
        return False

def check_server_status():
    """Проверяет статус сервера"""
    
    print("\n" + "=" * 60)
    print("🔍 ПРОВЕРКА СТАТУСА СЕРВЕРА")
    print("=" * 60)
    
    # Проверяем логи
    log_file = r"c:\Personal\TestServer\server.log"
    
    if os.path.exists(log_file):
        print("📄 Анализирую логи сервера...")
        
        try:
            with open(log_file, 'r', encoding='utf-8', errors='ignore') as f:
                lines = f.readlines()
                
            # Ищем последние 20 строк
            recent_lines = lines[-20:]
            
            bluemap_found = False
            web_server_found = False
            
            for line in recent_lines:
                if 'BlueMap' in line:
                    bluemap_found = True
                    print(f"🔵 BlueMap: {line.strip()}")
                
                if 'Web server' in line or 'webapp' in line:
                    web_server_found = True
                    print(f"🌐 Web: {line.strip()}")
            
            if bluemap_found:
                print("✅ BlueMap обнаружен в логах")
            else:
                print("❌ BlueMap НЕ найден в последних логах")
            
            if web_server_found:
                print("✅ Веб-сервер запущен")
            else:
                print("❌ Веб-сервер НЕ найден в логах")
                
        except Exception as e:
            print(f"❌ Ошибка чтения логов: {e}")
    else:
        print("❌ Файл логов не найден")

def main():
    """Главная функция"""
    
    success = fix_markers_file()
    
    if success:
        check_server_status()
        
        print("\n" + "=" * 60)
        print("🎯 СЛЕДУЮЩИЕ ШАГИ:")
        print("=" * 60)
        print("1. 🛑 Остановите сервер (если запущен)")
        print("2. 🚀 Перезапустите сервер: java -jar spigot.jar nogui")
        print("3. 🔍 Проверьте логи на ошибки загрузки BlueMap")
        print("4. 🌐 Откройте http://localhost:8100")
        print("5. 🗺️ Проверьте появились ли ваши 118 маркеров!")
        print("=" * 60)
    else:
        print("\n❌ Исправление не удалось. Проверьте пути к файлам.")

if __name__ == "__main__":
    main() 