@echo off
echo ===== BLUEMAP PLUGIN LOADING TEST =====
echo.

echo [1] Checking server.jar...
if exist server.jar (
    echo ✓ server.jar exists
) else (
    echo ✗ server.jar not found!
    pause
    exit /b 1
)

echo.
echo [2] Checking plugins directory...
if exist plugins (
    echo ✓ plugins directory exists
    dir plugins\*.jar
) else (
    echo ✗ plugins directory not found!
    pause
    exit /b 1
)

echo.
echo [3] Checking BlueMap plugin...
if exist "plugins\bluemap-bukkit-legacy-5.7-SNAPSHOT.jar" (
    echo ✓ BlueMap plugin found
    for %%f in ("plugins\bluemap-bukkit-legacy-5.7-SNAPSHOT.jar") do echo File size: %%~zf bytes
) else (
    echo ✗ BlueMap plugin not found!
    pause
    exit /b 1
)

echo.
echo [4] Checking plugin.yml inside JAR...
jar -tf "plugins\bluemap-bukkit-legacy-5.7-SNAPSHOT.jar" | findstr plugin.yml
if %errorlevel% equ 0 (
    echo ✓ plugin.yml found in JAR
) else (
    echo ✗ plugin.yml not found in JAR!
)

echo.
echo [5] Extracting and checking plugin.yml content...
jar -xf "plugins\bluemap-bukkit-legacy-5.7-SNAPSHOT.jar" plugin.yml 2>nul
if exist plugin.yml (
    echo ✓ plugin.yml extracted, content:
    type plugin.yml
    del plugin.yml 2>nul
) else (
    echo ✗ Could not extract plugin.yml
)

echo.
echo [6] Starting server with plugin debug...
echo Starting Minecraft server with BlueMap plugin...
echo Check the output below for plugin loading messages:
echo.

java -jar server.jar nogui 