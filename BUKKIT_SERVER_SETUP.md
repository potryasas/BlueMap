# 🚀 Установка Bukkit сервера для BlueMap маркеров

## 🚨 Проблема: Маркеры не отображаются

**Причина**: У вас ванильный Minecraft 1.5.2 сервер, а BlueMap - плагин для Bukkit/Spigot

**Что нужно**: CraftBukkit 1.5.2 или аналогичный сервер с поддержкой плагинов

## 📋 Пошаговое решение

### Шаг 1: Скачайте CraftBukkit 1.5.2

**Вариант А: Использовать готовый архив**
```bash
# Найдите CraftBukkit 1.5.2 на архивных сайтах
# Или используйте BuildTools для старых версий
```

**Вариант Б: Временно протестировать на новой версии**
```bash
# Скачайте современный Spigot для тестирования
java -jar spigot-1.19.4.jar nogui
```

### Шаг 2: Проверьте структуру сервера

Правильная структура должна включать:
```
TestServer/
├── craftbukkit-1.5.2.jar      # JAR файл Bukkit сервера
├── plugins/                    # Папка для плагинов (ОБЯЗАТЕЛЬНО!)
│   └── bluemap-bukkit-legacy-5.7-SNAPSHOT.jar
├── world/                      # Мир
├── server.properties
└── ops.txt
```

### Шаг 3: Замените команду запуска

**Было (ванильный сервер):**
```bash
java -jar minecraft_server.1.5.2.jar nogui
```

**Должно быть (Bukkit сервер):**
```bash
java -jar craftbukkit-1.5.2.jar nogui
```

### Шаг 4: Проверьте логи запуска

**Правильные логи должны содержать:**
```
[INFO] Loading plugins...
[INFO] Loaded plugin: BlueMap Legacy
[INFO] BlueMap Legacy enabling...
[INFO] Marker system initialized
[INFO] Web server started on port 8100
```

**Неправильные логи (ваши текущие):**
```
[INFO] Starting minecraft server version 1.5.2
[INFO] Loading properties
# НЕТ упоминаний о плагинах!
```

## 🔧 Альтернативные решения

### Решение 1: Обновите сервер временно
```bash
# Для быстрого тестирования используйте современный Spigot
wget https://download.getbukkit.org/spigot/spigot-1.19.4.jar
java -jar spigot-1.19.4.jar nogui
```

### Решение 2: Используйте Paper (рекомендуется)
```bash
# Paper лучше совместим с плагинами
wget https://api.papermc.io/v2/projects/paper/versions/1.19.4/builds/latest/downloads/paper-1.19.4-latest.jar
java -jar paper-1.19.4-latest.jar --nogui
```

### Решение 3: Ручная конвертация маркеров

Если Bukkit недоступен, можно вручную преобразовать ваши маркеры:

**Ваш файл:** `_markers_/marker_world.json`
**Нужен файл:** `bluemap-test/web/maps/world/markers.json`

## 📝 Команды для работы с маркерами

После установки Bukkit сервера:

```bash
# Проверить загрузку плагина
/plugins

# Добавить себя в операторы
/op ваш_ник

# Создать категорию маркеров
/bluemap marker addset "Города"

# Добавить маркер
/bluemap marker add "Спавн" set:cities icon:spawn

# Список всех маркеров
/bluemap marker list

# Отладочная информация
/bluemap marker debug
```

## 🎯 Быстрая проверка

### 1. Остановите текущий сервер
### 2. Замените minecraft_server.1.5.2.jar на craftbukkit-1.5.2.jar
### 3. Создайте папку `plugins/` если её нет
### 4. Скопируйте BlueMap JAR в `plugins/`
### 5. Запустите сервер командой `java -jar craftbukkit-1.5.2.jar nogui`
### 6. Проверьте логи - должны быть упоминания о загрузке плагинов

## 🔍 Диагностика

**Если маркеры всё ещё не работают:**

1. Проверьте права доступа: `/op ваш_ник`
2. Проверьте создание файлов: `/bluemap marker debug`
3. Проверьте веб-файлы: `plugins/BlueMap/web/maps/world/markers.json`
4. Перезапустите сервер полностью

## ✅ Ожидаемый результат

После установки Bukkit сервера:
- ✅ Плагины загружаются при старте
- ✅ BlueMap создаёт веб-интерфейс
- ✅ Команды `/bluemap marker` работают
- ✅ Маркеры отображаются на веб-карте
- ✅ Файл `markers.json` автоматически генерируется

---

**Главное**: BlueMap - это **плагин**, он работает только на серверах с поддержкой плагинов (Bukkit/Spigot/Paper), а НЕ на ванильном Minecraft сервере. 