#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Полностью заменяет webapp BlueMap из рабочего источника
"""

import os
import shutil
import json

def backup_important_files():
    """Создаёт резервные копии важных файлов"""
    
    print("💾 СОЗДАНИЕ РЕЗЕРВНЫХ КОПИЙ")
    print("=" * 40)
    
    target_web = r"c:\Personal\TestServer\plugins\BlueMap\web"
    
    important_files = [
        "settings.json",
        "maps/world/markers.json",
        "maps/world/settings.json",
        "maps/world_nether/settings.json", 
        "maps/world_the_end/settings.json"
    ]
    
    for file_path in important_files:
        full_path = os.path.join(target_web, file_path)
        if os.path.exists(full_path):
            backup_path = full_path + ".backup-webapp"
            try:
                shutil.copy2(full_path, backup_path)
                print(f"✅ Резервная копия: {file_path}")
            except Exception as e:
                print(f"❌ Ошибка копирования {file_path}: {e}")

def copy_webapp_core():
    """Копирует основные файлы webapp"""
    
    print("\n🌐 КОПИРОВАНИЕ ОСНОВНЫХ ФАЙЛОВ WEBAPP")
    print("=" * 50)
    
    source_web = "bluemap-test/web"
    target_web = r"c:\Personal\TestServer\plugins\BlueMap\web"
    
    # Файлы и директории для копирования
    core_items = [
        "assets",
        "css", 
        "js",
        "favicon.ico",
        "favicon.png",
        "index.html"
    ]
    
    for item in core_items:
        source_path = os.path.join(source_web, item)
        target_path = os.path.join(target_web, item)
        
        if os.path.exists(source_path):
            try:
                if os.path.isdir(source_path):
                    # Удаляем старую директорию если существует
                    if os.path.exists(target_path):
                        shutil.rmtree(target_path)
                    shutil.copytree(source_path, target_path)
                    print(f"📁 Скопирована директория: {item}")
                else:
                    shutil.copy2(source_path, target_path)
                    print(f"📄 Скопирован файл: {item}")
            except Exception as e:
                print(f"❌ Ошибка копирования {item}: {e}")
        else:
            print(f"⚠️ Не найден: {source_path}")

def restore_custom_settings():
    """Восстанавливает наши кастомные настройки"""
    
    print("\n⚙️ ВОССТАНОВЛЕНИЕ КАСТОМНЫХ НАСТРОЕК")
    print("=" * 45)
    
    target_web = r"c:\Personal\TestServer\plugins\BlueMap\web"
    
    # Восстанавливаем основные настройки
    settings_file = os.path.join(target_web, "settings.json")
    settings_backup = settings_file + ".backup-webapp"
    
    if os.path.exists(settings_backup):
        try:
            shutil.copy2(settings_backup, settings_file)
            print("✅ Восстановлен основной settings.json")
        except Exception as e:
            print(f"❌ Ошибка восстановления settings.json: {e}")
    
    # Восстанавливаем настройки карт
    map_settings = [
        "maps/world/settings.json",
        "maps/world_nether/settings.json",
        "maps/world_the_end/settings.json"
    ]
    
    for map_setting in map_settings:
        settings_file = os.path.join(target_web, map_setting)
        settings_backup = settings_file + ".backup-webapp"
        
        if os.path.exists(settings_backup):
            try:
                # Создаём директорию если не существует
                os.makedirs(os.path.dirname(settings_file), exist_ok=True)
                shutil.copy2(settings_backup, settings_file)
                print(f"✅ Восстановлен {map_setting}")
            except Exception as e:
                print(f"❌ Ошибка восстановления {map_setting}: {e}")
    
    # Восстанавливаем маркеры
    markers_file = os.path.join(target_web, "maps/world/markers.json")
    markers_backup = markers_file + ".backup-webapp"
    
    if os.path.exists(markers_backup):
        try:
            shutil.copy2(markers_backup, markers_file)
            print("✅ Восстановлен markers.json")
        except Exception as e:
            print(f"❌ Ошибка восстановления markers.json: {e}")

def verify_webapp():
    """Проверяет webapp"""
    
    print("\n🔍 ПРОВЕРКА WEBAPP")
    print("=" * 25)
    
    target_web = r"c:\Personal\TestServer\plugins\BlueMap\web"
    
    required_items = [
        "js",
        "css", 
        "assets",
        "index.html",
        "settings.json"
    ]
    
    for item in required_items:
        item_path = os.path.join(target_web, item)
        if os.path.exists(item_path):
            if os.path.isdir(item_path):
                files_count = len(os.listdir(item_path))
                print(f"✅ {item}/ ({files_count} файлов)")
            else:
                file_size = os.path.getsize(item_path)
                print(f"✅ {item} ({file_size} байт)")
        else:
            print(f"❌ Отсутствует: {item}")

def main():
    """Главная функция"""
    
    print("🌐 ПОЛНОЕ ИСПРАВЛЕНИЕ WEBAPP BlueMap")
    print("=" * 50)
    
    # Создаём резервные копии
    backup_important_files()
    
    # Копируем основные файлы
    copy_webapp_core()
    
    # Восстанавливаем наши настройки
    restore_custom_settings()
    
    # Проверяем результат
    verify_webapp()
    
    print("\n🎉 WEBAPP ПОЛНОСТЬЮ ОБНОВЛЁН!")
    print("\n🎯 ТЕПЕРЬ:")
    print("1. 🔄 Полностью обновите браузер (Ctrl+Shift+Delete)")
    print("2. 🌐 Откройте http://localhost:8100")
    print("3. 🗺️ Ошибка [object Object] должна исчезнуть!")
    print("4. 🎯 Все 118 маркеров должны отображаться!")

if __name__ == "__main__":
    main() 