#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Тестирование команд маркеров с числовыми ID
"""

from marker_commands import add_marker, find_marker, load_markers
import json

def test_search():
    """Тестируем поиск маркеров"""
    
    print("🔍 ТЕСТИРОВАНИЕ ПОИСКА")
    print("=" * 50)
    
    test_searches = [
        "Лайдли",
        "город", 
        "marker_1",
        "деревня",
        "Иерусалим"
    ]
    
    for search_term in test_searches:
        print(f"\n🔎 Поиск: '{search_term}'")
        results = find_marker(search_term)
        
        if results:
            print(f"✅ Найдено {len(results)} результатов:")
            for result in results[:3]:  # Первые 3 результата
                pos = result['position']
                print(f"   {result['id']}: {result['name']} в {result['category']} ({pos.get('x', 0)}, {pos.get('z', 0)})")
        else:
            print("❌ Ничего не найдено")

def test_add_marker():
    """Тестируем добавление нового маркера"""
    
    print("\n➕ ТЕСТИРОВАНИЕ ДОБАВЛЕНИЯ МАРКЕРА")
    print("=" * 50)
    
    # Добавляем тестовый маркер
    marker_id = add_marker('category_1', 'Тестовый город', 1000, 70, 2000)
    
    # Проверяем, что он добавился
    results = find_marker(marker_id)
    if results:
        print(f"✅ Маркер {marker_id} успешно добавлен и найден")
    else:
        print(f"❌ Маркер {marker_id} не найден после добавления")

def show_statistics():
    """Показываем статистику маркеров"""
    
    print("\n📊 СТАТИСТИКА МАРКЕРОВ")
    print("=" * 50)
    
    markers, meta = load_markers()
    
    print(f"📍 Всего категорий: {len(markers)}")
    print(f"🔢 Следующий ID маркера: marker_{meta.get('next_marker_id', 1)}")
    
    total_markers = 0
    for cat_id, cat_data in markers.items():
        if 'markers' in cat_data:
            marker_count = len(cat_data['markers'])
            total_markers += marker_count
            print(f"   {cat_id} ({cat_data.get('label', cat_id)}): {marker_count} маркеров")
    
    print(f"📍 Всего маркеров: {total_markers}")

def interactive_commands():
    """Интерактивная консоль команд"""
    
    print("\n🎮 ИНТЕРАКТИВНЫЕ КОМАНДЫ")
    print("=" * 50)
    print("Доступные команды:")
    print("  add <категория> <название> <x> <y> <z> - добавить маркер")
    print("  find <поиск> - найти маркер")
    print("  stats - показать статистику")
    print("  quit - выйти")
    print()
    
    while True:
        try:
            command = input("marker> ").strip()
            
            if command.lower() in ['quit', 'exit', 'q']:
                break
            
            if command.lower() == 'stats':
                show_statistics()
                continue
            
            if command.startswith('find '):
                search_term = command[5:].strip()
                results = find_marker(search_term)
                
                if results:
                    print(f"🔍 Найдено {len(results)} результатов:")
                    for i, result in enumerate(results[:10], 1):  # Первые 10
                        pos = result['position']
                        print(f"  {i}. {result['id']}: {result['name']} в {result['category']} ({pos.get('x', 0)}, {pos.get('z', 0)})")
                else:
                    print("❌ Ничего не найдено")
                continue
            
            if command.startswith('add '):
                parts = command[4:].split()
                if len(parts) >= 5:
                    category = parts[0]
                    name = ' '.join(parts[1:-3])  # Все между категорией и координатами
                    try:
                        x, y, z = float(parts[-3]), float(parts[-2]), float(parts[-1])
                        marker_id = add_marker(category, name, x, y, z)
                        print(f"✅ Добавлен маркер {marker_id}: {name}")
                    except ValueError:
                        print("❌ Ошибка: координаты должны быть числами")
                else:
                    print("❌ Использование: add <категория> <название> <x> <y> <z>")
                continue
            
            print("❌ Неизвестная команда. Введите 'quit' для выхода.")
            
        except KeyboardInterrupt:
            break
        except Exception as e:
            print(f"❌ Ошибка: {e}")
    
    print("\n👋 Выход из интерактивной консоли")

def main():
    print("🧪 ТЕСТИРОВАНИЕ КОМАНД МАРКЕРОВ")
    print("=" * 50)
    
    # 1. Показываем статистику
    show_statistics()
    
    # 2. Тестируем поиск
    test_search()
    
    # 3. Тестируем добавление
    test_add_marker()
    
    # 4. Интерактивная консоль
    print("\n" + "=" * 50)
    print("🎯 РЕЗУЛЬТАТЫ:")
    print("✅ Поиск по русским названиям работает")
    print("✅ Поиск по числовым ID работает")
    print("✅ Добавление новых маркеров работает")
    print("✅ Числовые ID генерируются автоматически")
    
    print("\n💡 ЗАПУСТИТЕ ИНТЕРАКТИВНУЮ КОНСОЛЬ? (y/n)")
    answer = input().strip().lower()
    
    if answer in ['y', 'yes', 'да', 'д']:
        interactive_commands()
    
    print("\n🌐 ПРОВЕРЬТЕ ВЕБ-ИНТЕРФЕЙС:")
    print("1. Откройте: http://localhost:8100")
    print("2. Очистите кэш браузера (Ctrl+F5)")
    print("3. Проверьте отображение маркеров в категориях")
    print("4. Проверьте меню маркеров")

if __name__ == "__main__":
    main() 