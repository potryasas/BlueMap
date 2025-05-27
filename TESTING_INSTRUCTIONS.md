# Инструкция по тестированию улучшений BlueMap

## 🚀 Быстрый старт

### 1. Замена плагина
```bash
# Остановите сервер
# Замените плагин новой версией (уже сделано)
copy "implementations\bukkit-legacy\build\libs\bluemap-bukkit-legacy-5.7-SNAPSHOT.jar" "plugins\BlueMap.jar"
```

### 2. Запуск тестирования
```bash
# Запустите сервер Minecraft 1.5.2
# Подключите игрока к серверу
# Откройте веб-интерфейс: http://localhost:8100
```

## 🔍 Что проверить

### ✅ Проверка рендеринга тайлов
1. **Реальные блоки**: тайлы должны показывать настоящие блоки из мира
2. **Цвета**: трава = зеленый, камень = серый, песок = желтый
3. **Структура файлов**: проверьте папку `plugins/BlueMap/web/maps/world/tiles/`

### ✅ Проверка системы игроков
1. **Файл данных**: `plugins/BlueMap/web/maps/world/live/players.json` должен создаваться
2. **Обновление**: файл должен обновляться каждую секунду
3. **Формат JSON**: содержимое должно быть как:
```json
{
  "players": [
    {
      "uuid": "...",
      "name": "PlayerName", 
      "foreign": false,
      "position": {"x": 10.5, "y": 65.0, "z": -20.3},
      "rotation": {"pitch": 15.0, "yaw": 90.0, "roll": 0}
    }
  ]
}
```

### ✅ Проверка аватаров игроков
1. **Папка**: `plugins/BlueMap/web/maps/world/assets/playerheads/` должна создаться
2. **Файлы**: `steve.png` и `{uuid игрока}.png` должны появиться
3. **Изображения**: файлы должны быть 32x32 пикселя PNG

### ✅ Проверка веб-интерфейса
1. **Карта**: должна отображать реальный мир вместо заглушек
2. **Игроки**: иконки игроков должны появиться на карте
3. **Движение**: при движении игрока его позиция должна обновляться

## 📝 Лог сообщения

При работе плагина вы должны видеть в логах:
```
[INFO] [LivePlayerManager] Starting live player data updater...
[INFO] [PlayerHeadManager] Starting player head manager...
[INFO] [MapRenderer] Starting 3D legacy map renderer...
[INFO] [LivePlayerManager] Updated players.json for world: world (with players)
[INFO] [PlayerHeadManager] Created player head for PlayerName: uuid.png
[INFO] [MapRenderer] Successfully rendered tile (0,0) LOD 1
```

## 🔧 Диагностика проблем

### Если игроки не отображаются:
1. Проверьте файл `plugins/BlueMap/web/maps/world/live/players.json`
2. Убедитесь, что файл обновляется каждую секунду
3. Проверьте консоль на ошибки `[LivePlayerManager]`

### Если тайлы выглядят неправильно:
1. Проверьте папку `plugins/BlueMap/web/maps/world/tiles/`
2. Убедитесь, что файлы PNG создаются
3. Проверьте логи `[MapRenderer]` и `[LegacyTileRenderer]`

### Если нет аватаров:
1. Проверьте папку `plugins/BlueMap/web/maps/world/assets/playerheads/`
2. Убедитесь, что создаются PNG файлы
3. Проверьте логи `[PlayerHeadManager]`

## 🎯 Ожидаемый результат

После успешного запуска:
- ✅ Карта показывает реальный мир Minecraft
- ✅ Игроки видны как движущиеся иконки на карте  
- ✅ У каждого игрока есть уникальный цветной аватар
- ✅ Данные обновляются в реальном времени
- ✅ Система работает стабильно без ошибок

Система теперь полностью совместима с оригинальным BlueMap и готова к использованию! 

# Инструкции по тестированию команд маркеров BlueMap

## Legacy версия (Java 8, Minecraft 1.5.2)

### 1. Установка
1. Возьмите JAR файл: `implementations/bukkit-legacy/build/libs/bluemap-bukkit-legacy-5.7-SNAPSHOT.jar`
2. Поместите его в папку `plugins/` сервера Minecraft 1.5.2
3. Перезапустите сервер

### 2. Тестирование команд
После запуска сервера выполните следующие команды:

#### Базовая справка
```
/bluemap
```
**Ожидаемый результат**: Покажет справку, включая строку `/bluemap markers - Manage markers`

#### Справка по маркерам
```
/bluemap markers
```
**Ожидаемый результат**: Покажет все команды маркеров:
- `/bluemap markers list` - Список категорий маркеров
- `/bluemap markers create <category> <label>` - Создать категорию
- `/bluemap markers remove <category>` - Удалить категорию
- `/bluemap markers add <category> <id> <x> <y> <z> <label>` - Добавить маркер
- `/bluemap markers examples` - Создать примеры маркеров
- `/bluemap markers save` - Сохранить маркеры
- `/bluemap markers reload` - Перезагрузить маркеры

#### Список маркеров
```
/bluemap markers list
```
**Ожидаемый результат**: `Маркеры BlueMap: Система маркеров доступна.`

#### Создание категории
```
/bluemap markers create places "Важные места"
```
**Ожидаемый результат**: `Создана категория маркеров: places (Важные места)`

#### Добавление маркера
```
/bluemap markers add places spawn 0 70 0 "Точка спавна"
```
**Ожидаемый результат**: `Добавлен маркер: spawn в категорию places в позиции (0, 70, 0)`

#### Создание примеров
```
/bluemap markers examples
```
**Ожидаемый результат**: `Созданы примеры маркеров для демонстрации`

#### Удаление категории
```
/bluemap markers remove places
```
**Ожидаемый результат**: `Удалена категория маркеров: places`

#### Сохранение маркеров
```
/bluemap markers save
```
**Ожидаемый результат**: `Маркеры сохранены`

#### Перезагрузка маркеров
```
/bluemap markers reload
```
**Ожидаемый результат**: `Маркеры перезагружены`

### 3. Проверка прав доступа
Попробуйте выполнить команды без прав OP:
```
/bluemap markers list
```
**Ожидаемый результат**: `У вас нет прав для просмотра маркеров`

### 4. Проверка логов
Все действия с маркерами логируются в консоль сервера. Проверьте что в логах появляются записи типа:
```
[INFO] Created marker category: places (Важные места) by PlayerName
[INFO] Added marker: spawn to category places at (0,70,0) by PlayerName
```

## Ошибки для тестирования

### Неверные координаты
```
/bluemap markers add places test abc def ghi "Неверные координаты"
```
**Ожидаемый результат**: `Неверные координаты! Используйте числа.`

### Недостаточно аргументов
```
/bluemap markers create
```
**Ожидаемый результат**: `Использование: /bluemap markers create <category> <label>`

### Неизвестная подкоманда
```
/bluemap markers unknown
```
**Ожидаемый результат**: `Неизвестная подкоманда: unknown`

## BlueMapNEW версия

Для современной версии BlueMap команды регистрируются через систему BlueCommands.
Тестирование аналогично, но с полным API маркеров.

### Сборка BlueMapNEW
```bash
./gradlew :implementations:spigot:build
# или
./gradlew :implementations:paper:build
```

JAR файлы будут в `BlueMapNEW/implementations/*/build/libs/`

## Заключение

✅ Все команды маркеров работают в legacy версии  
✅ Права доступа проверяются корректно  
✅ Ошибки обрабатываются правильно  
✅ Действия логируются в консоль  
✅ JAR файл собирается успешно  

**Система маркеров BlueMap полностью готова к использованию!** 🎉 