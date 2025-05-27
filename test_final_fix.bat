@echo off
echo ============================================
echo       FINAL BLUEMAP TEXTURES FIX TEST
echo ============================================

echo.
echo 1. Preparing test environment...
cd "c:\Personal\TestServer"

echo Checking plugin file...
if exist "plugins\BlueMap.jar" (
    echo ✓ BlueMap.jar found
) else (
    echo ✗ BlueMap.jar NOT found!
    pause
    exit /b 1
)

echo.
echo 2. Starting Minecraft Server...
echo Starting server and monitoring...
start /B java -Dfile.encoding=UTF-8 -jar server.jar nogui > server_final_test.log 2>&1

echo Waiting for server startup (60 seconds)...
timeout /t 60 /nobreak > nul

echo.
echo 3. Checking server logs for BlueMap...
echo Last 10 lines of server log:
type server_final_test.log | findstr /I "bluemap" | findstr /I /V "debug"
if %ERRORLEVEL% EQU 0 (
    echo ✓ BlueMap plugin detected in logs
) else (
    echo ⚠ BlueMap messages not found, checking all logs...
    type server_final_test.log
)

echo.
echo 4. Testing HTTP accessibility...
curl -s -o nul -w "settings.json - Status: %%{http_code}, Size: %%{size_download} bytes\n" "http://localhost:8100/settings.json"
curl -s -o nul -w "world/settings.json - Status: %%{http_code}, Size: %%{size_download} bytes\n" "http://localhost:8100/maps/world/settings.json"
curl -s -o nul -w "world/textures.json - Status: %%{http_code}, Size: %%{size_download} bytes\n" "http://localhost:8100/maps/world/textures.json"

echo.
echo 5. Checking textures.json format...
echo Testing if textures is array:
curl -s "http://localhost:8100/maps/world/textures.json" | findstr /C:"\"textures\": [" > nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ textures.json has ARRAY format - CORRECT!
) else (
    echo ✗ textures.json format issue
    echo First few lines:
    curl -s "http://localhost:8100/maps/world/textures.json" | findstr /C:"textures" | more
)

echo.
echo 6. Testing a sample tile...
curl -s -o nul -w "x0/z0.json - Status: %%{http_code}, Size: %%{size_download} bytes\n" "http://localhost:8100/maps/world/hires/x0/z0.json"

echo.
echo 7. Opening browser to test map loading...
start http://localhost:8100

echo.
echo ============================================
echo  FINAL TEST RESULTS:
echo  - Check browser for map loading
echo  - Should see 3D map without errors
echo  - Console should be clean of texture errors
echo ============================================
pause

echo.
echo Stopping server...
taskkill /F /IM java.exe 2>nul
echo Final test completed!

echo.
echo === SERVER LOG SUMMARY ===
type server_final_test.log | findstr /I "error\|exception\|warning" | findstr /I "bluemap"
echo === END LOG === 