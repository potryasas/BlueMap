@echo off
echo =================================
echo    BlueMap Server Type Checker
echo =================================
echo.

echo Checking current server setup...
echo.

REM Check if server.log exists
if not exist server.log (
    echo [ERROR] server.log not found!
    echo Make sure you're running this from the server directory.
    pause
    exit /b
)

echo === Server Log Analysis ===
echo.

REM Check for vanilla server
findstr /C:"Starting minecraft server version" server.log > nul
if %errorlevel% == 0 (
    echo [FOUND] Vanilla Minecraft server detected
    echo This is a standard Minecraft server - PLUGINS NOT SUPPORTED!
    echo.
    echo You need CraftBukkit or Spigot for BlueMap to work.
    echo.
)

REM Check for Bukkit/Spigot
findstr /C:"Loading plugins" server.log > nul
if %errorlevel% == 0 (
    echo [FOUND] Bukkit/Spigot server detected - PLUGINS SUPPORTED!
    echo.
) else (
    echo [NOT FOUND] No plugin loading detected
    echo.
)

REM Check for BlueMap plugin loading
findstr /C:"BlueMap" server.log > nul
if %errorlevel% == 0 (
    echo [FOUND] BlueMap references in logs
    echo.
    echo Recent BlueMap log entries:
    findstr /C:"BlueMap" server.log | findstr /V /C:"ERROR" | tail -5
    echo.
) else (
    echo [NOT FOUND] No BlueMap activity in logs
    echo Plugin may not be loaded properly.
    echo.
)

echo === Plugin Directory Check ===
echo.

if exist plugins\ (
    echo [OK] plugins/ directory exists
    
    if exist plugins\bluemap-bukkit-legacy-5.7-SNAPSHOT.jar (
        echo [OK] BlueMap Legacy plugin file found
        dir plugins\bluemap-bukkit-legacy-5.7-SNAPSHOT.jar | findstr bluemap
    ) else (
        echo [WARNING] BlueMap plugin file not found in plugins/
        echo Looking for any BlueMap files...
        dir plugins\*bluemap* 2>nul
        if %errorlevel% neq 0 (
            echo No BlueMap files found in plugins/
        )
    )
    
    echo.
    echo All files in plugins/:
    dir plugins\ /b
    
) else (
    echo [ERROR] plugins/ directory does not exist!
    echo This confirms you're running a vanilla server.
)

echo.
echo === BlueMap Data Check ===
echo.

if exist plugins\BlueMap\ (
    echo [OK] BlueMap data directory exists
    
    if exist plugins\BlueMap\markers.properties (
        echo [OK] Markers file exists
        echo Marker count:
        find /c "=" plugins\BlueMap\markers.properties 2>nul
    ) else (
        echo [INFO] No markers file yet (normal for first run)
    )
    
    if exist plugins\BlueMap\web\ (
        echo [OK] Web directory exists
        if exist plugins\BlueMap\web\assets\ (
            echo [OK] Assets directory exists
            dir plugins\BlueMap\web\assets\ | find "File(s)" 2>nul
        )
        if exist plugins\BlueMap\web\maps\world\markers.json (
            echo [OK] World markers.json exists
        ) else (
            echo [INFO] No markers.json yet
        )
    ) else (
        echo [INFO] Web directory not created yet
    )
    
) else (
    echo [INFO] BlueMap data directory not created yet
    echo This is normal if the plugin hasn't run successfully.
)

echo.
echo === Recommendations ===
echo.

findstr /C:"Starting minecraft server version" server.log > nul
if %errorlevel% == 0 (
    echo ❌ PROBLEM: You're running vanilla Minecraft server
    echo.
    echo SOLUTION: Download and use CraftBukkit 1.5.2 instead:
    echo 1. Download CraftBukkit-1.5.2.jar
    echo 2. Replace your current server.jar
    echo 3. Copy your world/ folder to the new server
    echo 4. Copy plugins/ folder
    echo 5. Run: java -jar CraftBukkit-1.5.2.jar nogui
    echo.
    echo After switching, you should see:
    echo   [INFO] Loading plugins...
    echo   [INFO] Loaded plugin: BlueMap Legacy
)

findstr /C:"Loading plugins" server.log > nul
if %errorlevel% == 0 (
    findstr /C:"BlueMap" server.log > nul
    if %errorlevel% == 0 (
        echo ✅ GOOD: Plugin server with BlueMap activity detected
        echo.
        echo To test markers:
        echo 1. Join the server
        echo 2. Run: /op your_username
        echo 3. Run: /bluemap marker examples
        echo 4. Run: /bluemap marker debug  
        echo 5. Check: http://localhost:8100
    ) else (
        echo ⚠️  WARNING: Plugin server but no BlueMap activity
        echo.
        echo Check if:
        echo 1. BlueMap jar is in plugins/ folder
        echo 2. Java 8 is being used
        echo 3. No errors in server console
    )
)

echo.
echo Press any key to exit...
pause >nul 