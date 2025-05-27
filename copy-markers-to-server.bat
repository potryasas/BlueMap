@echo off
echo ================================================== 
echo 🚀 КОПИРОВАНИЕ МАРКЕРОВ В ТЕСТОВЫЙ СЕРВЕР
echo ==================================================

REM Создаем структуру папок
mkdir "c:\Personal\TestServer\plugins\BlueMap\web\maps\world" 2>nul
mkdir "c:\Personal\TestServer\plugins\BlueMap\web\assets" 2>nul

echo 📁 Копирую файлы маркеров...

REM Копируем файлы маркеров
copy "bluemap-test\web\maps\world\markers.json" "c:\Personal\TestServer\plugins\BlueMap\web\maps\world\" >nul
if %errorlevel% equ 0 (
    echo ✅ markers.json скопирован
) else (
    echo ❌ Ошибка копирования markers.json
)

copy "bluemap-test\web\maps\world\live-players.json" "c:\Personal\TestServer\plugins\BlueMap\web\maps\world\" >nul
if %errorlevel% equ 0 (
    echo ✅ live-players.json скопирован
) else (
    echo ❌ Ошибка копирования live-players.json
)

copy "bluemap-test\web\maps\world\settings.json" "c:\Personal\TestServer\plugins\BlueMap\web\maps\world\" >nul
if %errorlevel% equ 0 (
    echo ✅ settings.json скопирован
) else (
    echo ❌ Ошибка копирования settings.json
)

echo 🎨 Копирую иконки...

REM Копируем иконки маркеров (PNG файлы)
for %%f in (bluemap-test\web\assets\*.png) do (
    copy "%%f" "c:\Personal\TestServer\plugins\BlueMap\web\assets\" >nul
    if %errorlevel% equ 0 (
        echo ✅ %%~nxf
    ) else (
        echo ❌ Ошибка копирования %%~nxf
    )
)

echo 📋 Копирую веб-файлы BlueMap...

REM Копируем основные веб-файлы
copy "bluemap-test\web\index.html" "c:\Personal\TestServer\plugins\BlueMap\web\" >nul
copy "bluemap-test\web\settings.json" "c:\Personal\TestServer\plugins\BlueMap\web\" >nul
copy "bluemap-test\web\favicon.ico" "c:\Personal\TestServer\plugins\BlueMap\web\" >nul
copy "bluemap-test\web\favicon.png" "c:\Personal\TestServer\plugins\BlueMap\web\" >nul

REM Копируем JavaScript файлы
copy "bluemap-test\web\assets\index-BIEfirVm.js" "c:\Personal\TestServer\plugins\BlueMap\web\assets\" >nul
copy "bluemap-test\web\assets\index-BgiqB2rB.css" "c:\Personal\TestServer\plugins\BlueMap\web\assets\" >nul

echo 🎉 Копирование завершено!

echo ==================================================
echo 📍 СЛЕДУЮЩИЕ ШАГИ:
echo ==================================================
echo 1. 🔧 Установите Bukkit сервер (не ванильный!)
echo 2. 📂 Файлы скопированы в: c:\Personal\TestServer\plugins\BlueMap\web\
echo 3. 🚀 Запустите сервер с плагином BlueMap
echo 4. 🌐 Откройте http://localhost:8100
echo 5. 🗺️ Ваши 118 маркеров должны отображаться!
echo ==================================================

pause 