@echo off
echo ===== TESTING BLUEMAP TILE ACCESS =====
echo.
echo 1. Testing direct tile access via curl...
curl -s -o nul -w "Status: %%{http_code}, Size: %%{size_download} bytes\n" "http://localhost:8100/maps/world/hires/x0/z0.json"
echo.
echo 2. Testing settings.json access...
curl -s -o nul -w "Status: %%{http_code}, Size: %%{size_download} bytes\n" "http://localhost:8100/settings.json"
echo.
echo 3. Testing world settings.json access...
curl -s -o nul -w "Status: %%{http_code}, Size: %%{size_download} bytes\n" "http://localhost:8100/maps/world/settings.json"
echo.
echo 4. Testing textures.json access...
curl -s -o nul -w "Status: %%{http_code}, Size: %%{size_download} bytes\n" "http://localhost:8100/maps/world/textures.json"
echo.
echo 5. Downloading first 200 bytes of z0.json tile...
curl -s -r 0-199 "http://localhost:8100/maps/world/hires/x0/z0.json" | head -c 200
echo.
echo.
echo ===== TEST COMPLETE =====
pause 