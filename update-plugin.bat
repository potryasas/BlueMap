@echo off
echo === BlueMap Plugin Update ===
echo.

echo Copying new plugin...
copy "bukkit-legacy\build\libs\bukkit-legacy-5.7-SNAPSHOT-all.jar" "plugins\BlueMap.jar"

if %ERRORLEVEL% EQU 0 (
    echo Plugin updated successfully!
    echo.
    echo Changes made:
    echo - Fixed marker deletion commands
    echo - Improved JSON structure for web interface
    echo - Enhanced debug command
    echo - Fixed file saving and live markers
    echo - Support for category names in commands
    echo.
    echo To test:
    echo 1. Start server
    echo 2. Use: /bluemap marker addset "Test Category"
    echo 3. Use: /bluemap marker add "Test Marker" set:Test Category icon:house
    echo 4. Use: /bluemap marker debug
    echo 5. Check web interface at http://localhost:8100
    echo.
    pause
) else (
    echo Error updating plugin!
    pause
) 