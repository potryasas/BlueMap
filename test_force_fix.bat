@echo off
echo ========================================
echo   FORCE TEXTURES.JSON FIX TEST
echo ========================================

echo.
echo 1. Stopping any running servers...
taskkill /F /IM java.exe 2>nul

echo.
echo 2. Clearing old web data...
if exist "c:\Personal\TestServer\plugins\BlueMap\web" (
    rmdir /S /Q "c:\Personal\TestServer\plugins\BlueMap\web"
    echo ✓ Cleared old web data
)

echo.
echo 3. Starting server to force recreation...
cd "c:\Personal\TestServer"
start /B java -Dfile.encoding=UTF-8 -jar server.jar nogui > force_test.log 2>&1

echo Waiting 70 seconds for full startup and file creation...
timeout /t 70 /nobreak > nul

echo.
echo 4. Checking for BlueMap in logs...
type force_test.log | findstr /I "textures.json" | findstr /I /V "debug"

echo.
echo 5. Testing textures.json format...
curl -s "http://localhost:8100/maps/world/textures.json" 2>nul | findstr /C:"\"textures\": [" > nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ SUCCESS! textures.json is now ARRAY format
    echo ✓ Should fix the texture error!
) else (
    echo ✗ Still wrong format or file not found
    echo Raw response:
    curl -s "http://localhost:8100/maps/world/textures.json" 2>nul
)

echo.
echo 6. Opening browser for final test...
start http://localhost:8100

echo.
echo ========================================
echo Check if browser shows map without errors!
echo ========================================
pause

echo Stopping server...
taskkill /F /IM java.exe 2>nul 