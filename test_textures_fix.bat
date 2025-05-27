@echo off
echo ============================================
echo          TESTING TEXTURES.JSON FIX
echo ============================================

echo.
echo 1. Starting Minecraft Server...
cd "c:\Personal\TestServer"
timeout /t 2 /nobreak > nul

echo Starting server in background...
start /B java -Dfile.encoding=UTF-8 -jar server.jar nogui > server_test.log 2>&1

echo Waiting for server startup (45 seconds)...
timeout /t 45 /nobreak > nul

echo.
echo 2. Testing HTTP accessibility...
curl -s -o nul -w "settings.json - Status: %%{http_code}, Size: %%{size_download} bytes\n" "http://localhost:8100/settings.json"
curl -s -o nul -w "world/settings.json - Status: %%{http_code}, Size: %%{size_download} bytes\n" "http://localhost:8100/maps/world/settings.json"
curl -s -o nul -w "world/textures.json - Status: %%{http_code}, Size: %%{size_download} bytes\n" "http://localhost:8100/maps/world/textures.json"

echo.
echo 3. Checking textures.json format...
echo First few lines of textures.json:
curl -s "http://localhost:8100/maps/world/textures.json" | findstr /C:"\"textures\"" | more

echo.
echo 4. Opening browser to test map loading...
start http://localhost:8100

echo.
echo ============================================
echo  Check browser console for any errors!
echo  Should see proper 3D map instead of error
echo ============================================
pause

echo.
echo Stopping server...
taskkill /F /IM java.exe 2>nul
echo Test completed! 