# Инструкция по тестированию BlueMap Java 8 Port

## Исправление проблемы 404

### Что было исправлено:
- `MapRenderer` теперь **немедленно** создает структуру карт при запуске (без задержки)
- Создаются все необходимые файлы: `settings.json`, директории `hires/lowres`, базовые тайлы
- `WebResourceManager` форсировано создает веб-ресурсы при запуске

### Как тестировать:

1. **Установите плагин**:
   ```bash
   cp implementations/bukkit-legacy/build/libs/bukkit-legacy-5.7-SNAPSHOT.jar /path/to/server/plugins/
   ```

2. **Запустите сервер Bukkit 1.5.2**

3. **Проверьте логи сервера** - должны появиться сообщения:
   ```
   [BlueMapStub] Starting BlueMap...
   [MapRenderer] Creating initial map structures immediately...
   [MapRenderer] Processing world: world
   [MapRenderer] Created basic map structure for world: world
   [BlueMapStub] WEB SERVER RUNNING AT http://localhost:8100
   ```

4. **Проверьте файловую структуру** - должны быть созданы:
   ```
   plugins/BlueMap/
   ├── web/
   │   ├── index.html
   │   ├── css/bluemap.css
   │   ├── js/bluemap.js
   │   ├── settings.json
   │   └── maps/
   │       └── world/
   │           ├── settings.json
   │           ├── index.html
   │           ├── hires/
   │           │   └── x0/
   │           │       └── z0.json
   │           └── lowres/
   ```

5. **Тестируйте веб-интерфейс**:
   - Откройте `http://localhost:8100`
   - **Должно происходить автоматическое перенаправление** на `/maps/world/`
   - **НЕ должно быть ошибки 404**

### Ожидаемый результат:
- ✅ Веб-сервер запускается на порту 8100
- ✅ Автоматическое перенаправление на карту мира
- ✅ Отображается страница мира (даже если карта простая)
- ✅ НЕТ ошибки "404 File not found: /maps/world/"

### Если проблемы все еще есть:

1. **Проверьте права доступа** - плагин должен иметь права создавать файлы в `plugins/BlueMap/`

2. **Проверьте порт** - убедитесь, что порт 8100 не занят другими приложениями

3. **Проверьте логи** на наличие ошибок создания файлов

4. **Ручная команда рендеринга**:
   ```
   /bluemap render
   ```

### Отладка:

Если 404 все еще появляется, проверьте:

1. **Существует ли файл структуры**:
   ```bash
   ls plugins/BlueMap/web/maps/world/
   ```
   Должны быть: `settings.json`, `index.html`, папки `hires`, `lowres`

2. **Логи веб-сервера**:
   Ищите сообщения типа:
   ```
   [BlueMapStub] Requested file: /maps/world/
   [BlueMapStub] File found: /maps/world/index.html
   ```

3. **Проверьте содержимое index.html**:
   ```bash
   cat plugins/BlueMap/web/index.html
   ```
   Должен содержать JavaScript редирект на `/maps/world/`

## Дополнительные команды для тестирования:

- `/bluemap` - информация о плагине
- `/bluemap reload` - перезагрузка (пересоздание файлов)
- `/bluemap render` - принудительный рендеринг

## Ожидаемое поведение:

1. **При первом запуске**: сразу создается полная структура файлов
2. **При открытии браузера**: автоматическое перенаправление на карту мира
3. **НЕТ ошибок 404** для `/maps/world/`

---

**Важно**: Текущая версия создает базовую структуру карт с заглушками. Для полноценного рендеринга реальных данных мира потребуется дополнительная интеграция с BlueMap core. 