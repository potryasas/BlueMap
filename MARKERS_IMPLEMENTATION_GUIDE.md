# BlueMap Legacy Markers - Полная реализация с улучшениями

## 🎯 Что реализовано

### ✅ Основные функции
- **Полная поддержка Unicode** - русские и любые другие языки
- **Проверка дубликатов** - нельзя создать одинаковые маркеры/категории
- **Проверка близости** - маркеры не могут быть ближе 5 блоков друг к другу
- **Автоматическая транслитерация** - русские названия автоматически преобразуются в безопасные ID
- **80+ PNG иконок** - все иконки из папки `_markers_` интегрированы
- **Улучшенное сохранение** - UTF-8 кодировка для правильной работы с Unicode

### 🛠️ Упрощенные команды
```
/bluemap marker add "Название" set:категория [icon:иконка]    - Добавить маркер
/bluemap marker addset "Название категории"                  - Создать категорию  
/bluemap marker list                                         - Список всех маркеров
/bluemap marker examples                                     - Создать примеры
/bluemap marker save                                         - Сохранить
/bluemap marker reload                                       - Перезагрузить
/bluemap marker debug                                        - Отладочная информация
```

### 🏗️ Архитектура системы
- **LegacyMarkerManager.java** - основной менеджер без Gson (совместимость с MC 1.5.2)
- **LegacyMarker.java** - класс маркера
- **LegacyMarkerSet.java** - класс категории маркеров
- **Команды интегрированы** в LegacyBukkitPlugin.java

## 🚨 ВАЖНАЯ ПРОБЛЕМА: Нужен Bukkit сервер!

### Текущая проблема
Ваш текущий сервер - **обычный Minecraft 1.5.2**, а BlueMap это **плагин для Bukkit/Spigot**.

### Что показывают логи
```
2025-05-23 04:48:31 [INFO] Starting minecraft server version 1.5.2
2025-05-23 04:48:31 [INFO] Loading properties
```
☝️ Это ванильный сервер, плагины не загружаются!

### Нужно для работы
1. **CraftBukkit 1.5.2** или аналогичный сервер с поддержкой плагинов
2. **Папка plugins/** должна существовать и читаться сервером
3. **Права администратора** для выполнения команд маркеров

## 🔧 Решение: Установка Bukkit сервера

### Вариант 1: CraftBukkit 1.5.2
```bash
# Скачайте CraftBukkit 1.5.2 с официального сайта или архивов
java -jar craftbukkit-1.5.2.jar nogui
```

### Вариант 2: Spigot (если доступен для 1.5.2)
```bash
java -jar spigot-1.5.2.jar nogui
```

### Проверка правильности сервера
Логи правильного сервера должны содержать:
```
[INFO] Loading plugins...
[INFO] Loaded plugin: BlueMap Legacy
[INFO] BlueMap Legacy enabling...
```

## 🎨 Система иконок (80+ PNG)

### Категории иконок
```
🏠 Здания: house, bighouse, building, castle, church, temple, tower, factory, bank, lighthouse
📍 Места: spawn, portal, anchor, compass, world, pin, poi  
💎 Ресурсы: mine, chest, diamond, ruby, coins, goldstar, silverstar, bronzestar
🚗 Транспорт: minecart, truck, cart, walk
🚩 Флаги: redflag, blueflag, greenflag, yellowflag, pinkflag, purpleflag, orangeflag, pirateflag
🔧 Инструменты: wrench, hammer, gear, key, lock, construction
🌿 Природа: tree, flower, cake, beer, drink, cutlery
⭐ Особые: fire, skull, heart, star, sun, warning, caution, exclamation
➡️ Направления: pointup, pointdown, pointleft, pointright, up, down, left, right
📦 Прочие: camera, bed, door, sign, comment, lightbulb, theater, cup, bookshelf, basket...
```

## 📝 Примеры использования

### Создание категории
```
/bluemap marker addset "Мои дома"
/bluemap marker addset "Важные места" 
/bluemap marker addset "Ресурсы и фермы"
```

### Добавление маркеров
```
# С позицией игрока
/bluemap marker add "Мой замок" set:buildings icon:castle

# С указанием координат (от консоли)
/bluemap marker add "Спавн сервера" set:places icon:goldstar 0 70 0

# Русские названия полностью поддерживаются
/bluemap marker add "Алмазная шахта" set:resources icon:chest
/bluemap marker add "Портал в Ад" set:places icon:portal
```

### Просмотр маркеров
```
/bluemap marker list
```

Покажет:
```
=== Категории маркеров ===
places (Места) - 2 маркеров
    Спавн сервера (0,70,0) [goldstar.png]
    Портал в Ад (100,65,100) [portal.png]

buildings (Постройки) - 1 маркеров  
    Мой замок (150,80,200) [castle.png]
```

## 🔍 Система защиты от дубликатов

### Проверки при создании
1. **ID категории** - автоматически генерируется из названия
2. **Название категории** - не может повторяться  
3. **Название маркера** - не может повторяться в одной категории
4. **Позиция маркера** - не может быть ближе 5 блоков к существующему

### Примеры ошибок
```
§cКатегория 'buildings' уже существует!
§cМаркер с названием 'Мой дом' уже существует в категории 'buildings'!
§cДругой маркер 'Замок' слишком близко (расстояние: 3.2 блока)!
```

## 💾 Файловая система

### Структура данных
```
plugins/BlueMap/
├── markers.properties          # Маркеры (UTF-8)
├── marker-sets.properties      # Категории (UTF-8)  
└── web/
    ├── assets/                 # PNG иконки (автокопирование)
    │   ├── house.png
    │   ├── castle.png
    │   └── ... (80+ иконок)
    └── maps/
        └── world/
            ├── markers.json    # JSON для веб-карты
            └── live-players.json
```

### Формат файлов
**markers.properties (UTF-8):**
```
# BlueMap Legacy Markers - UTF-8 encoding
buildings.moy_dom_1234=Мой дом;150.5;80.0;200.3;house.png
places.spawn_servera_5678=Спавн сервера;0.0;70.0;0.0;goldstar.png
```

**markers.json (автогенерация):**
```json
{
  "buildings": {
    "label": "Постройки",
    "toggleable": true,
    "defaultHidden": false,
    "markers": {
      "moy_dom_1234": {
        "type": "poi",
        "label": "Мой дом",
        "icon": "assets/house.png",
        "position": {"x": 150.5, "y": 80.0, "z": 200.3}
      }
    }
  }
}
```

## 🐛 Отладка проблем

### Команда отладки
```
/bluemap marker debug
```

Покажет:
```
=== BlueMap Marker Manager Debug ===
Plugin data folder: C:\Personal\TestServer\plugins\BlueMap
Web assets folder: C:\Personal\TestServer\plugins\BlueMap\web\assets (exists: true)
Web maps folder: C:\Personal\TestServer\plugins\BlueMap\web\maps (exists: true)
Markers file: C:\Personal\TestServer\plugins\BlueMap\markers.properties (exists: true)
World markers JSON: C:\Personal\TestServer\plugins\BlueMap\web\maps\world\markers.json (exists: true)
Total marker sets: 4
Total markers: 6
Available icons: 80
```

### Частые проблемы

#### 1. Маркеры не видны на карте
- **Проверьте**: Bukkit сервер запущен правильно
- **Проверьте**: Файл `web/maps/world/markers.json` создается
- **Решение**: `/bluemap marker save` + `/bluemap marker debug`

#### 2. Команды не работают
- **Проверьте**: Плагин загружен (`/plugins`)
- **Проверьте**: Есть права `bluemap.markers.*`
- **Решение**: Добавьте себя в ops.txt

#### 3. Русские символы не сохраняются
- **Исправлено**: Используется UTF-8 кодировка
- **Проверьте**: Файлы `.properties` должны быть в UTF-8

#### 4. Иконки не отображаются
- **Проверьте**: Папка `web/assets/` содержит PNG файлы
- **Решение**: Перезапустите сервер для копирования иконок

## 🚀 Быстрый старт

### 1. Установите Bukkit сервер для MC 1.5.2
### 2. Скопируйте `bluemap-bukkit-legacy-5.7-SNAPSHOT.jar` в `plugins/`
### 3. Запустите сервер  
### 4. Дайте себе права оператора: `/op ваш_ник`
### 5. Создайте категорию: `/bluemap marker addset "Мои места"`
### 6. Добавьте маркер: `/bluemap marker add "Дом" set:moi_mesta icon:house`
### 7. Проверьте веб-карту: `http://localhost:8100`

## 🎯 Что улучшено в этой версии

### ✅ Исправления
- **Unicode поддержка** - полная поддержка русского языка
- **Без Gson** - совместимость с Minecraft 1.5.2 
- **Проверка дубликатов** - предотвращение создания одинаковых элементов
- **PNG иконки по умолчанию** - все иконки теперь PNG вместо SVG
- **Улучшенные пути** - правильные пути к веб-ресурсам
- **Транслитерация** - русские символы преобразуются в безопасные ID
- **Близость маркеров** - нельзя создать маркеры слишком близко друг к другу

### 🔧 Технические улучшения  
- UTF-8 кодировка для всех файлов
- Улучшенная обработка ошибок
- Подробные сообщения об ошибках
- Автоматическое создание директорий
- Принудительное обновление веб-карты
- Улучшенное логирование

---

**Версия**: BlueMap Legacy 5.7 с улучшенной системой маркеров  
**Совместимость**: Minecraft 1.5.2 + CraftBukkit/Spigot  
**Дата**: 2025-01-23 