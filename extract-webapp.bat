@echo off
echo Extracting BlueMap web files...

set "SERVER_PATH=C:\Personal\TestServer"
set "BLUEMAP_PATH=%SERVER_PATH%\plugins\BlueMap"
set "WEBAPP_ZIP=%~dp0webapp.zip"
set "TEXTURES_JSON=%~dp0textures.json"

REM Create required directories
mkdir "%BLUEMAP_PATH%\web" 2>nul
mkdir "%BLUEMAP_PATH%\web\assets" 2>nul
mkdir "%BLUEMAP_PATH%\web\maps" 2>nul
mkdir "%BLUEMAP_PATH%\web\maps\world" 2>nul
mkdir "%BLUEMAP_PATH%\web\maps\world\live" 2>nul
mkdir "%BLUEMAP_PATH%\web\maps\world_nether" 2>nul
mkdir "%BLUEMAP_PATH%\web\maps\world_nether\live" 2>nul
mkdir "%BLUEMAP_PATH%\web\maps\world_the_end" 2>nul
mkdir "%BLUEMAP_PATH%\web\maps\world_the_end\live" 2>nul

REM Extract webapp.zip
echo Extracting webapp.zip...
powershell -command "Expand-Archive -Path '%WEBAPP_ZIP%' -DestinationPath '%BLUEMAP_PATH%\web' -Force"

REM Copy textures.json to each world
echo Copying textures.json...
copy /Y "%TEXTURES_JSON%" "%BLUEMAP_PATH%\web\maps\world\textures.json"
copy /Y "%TEXTURES_JSON%" "%BLUEMAP_PATH%\web\maps\world_nether\textures.json"
copy /Y "%TEXTURES_JSON%" "%BLUEMAP_PATH%\web\maps\world_the_end\textures.json"

REM Create settings.json for each world
echo Creating world settings...

(
echo {
echo   "name": "world",
echo   "skyColor": "#7dabff",
echo   "ambientLight": 0.1,
echo   "equirectangular": false,
echo   "renderEdges": true,
echo   "renderAll": false,
echo   "hires": {
echo     "enabled": true,
echo     "save": true
echo   },
echo   "lowres": {
echo     "enabled": true,
echo     "save": true
echo   }
echo }
) > "%BLUEMAP_PATH%\web\maps\world\settings.json"

(
echo {
echo   "name": "world_nether",
echo   "skyColor": "#300000",
echo   "ambientLight": 0.2,
echo   "equirectangular": false,
echo   "renderEdges": true,
echo   "renderAll": false,
echo   "hires": {
echo     "enabled": true,
echo     "save": true
echo   },
echo   "lowres": {
echo     "enabled": true,
echo     "save": true
echo   }
echo }
) > "%BLUEMAP_PATH%\web\maps\world_nether\settings.json"

(
echo {
echo   "name": "world_the_end",
echo   "skyColor": "#000000",
echo   "ambientLight": 0.1,
echo   "equirectangular": false,
echo   "renderEdges": true,
echo   "renderAll": false,
echo   "hires": {
echo     "enabled": true,
echo     "save": true
echo   },
echo   "lowres": {
echo     "enabled": true,
echo     "save": true
echo   }
echo }
) > "%BLUEMAP_PATH%\web\maps\world_the_end\settings.json"

REM Create web settings.json
echo Creating web settings...
(
echo {
echo   "port": 8100,
echo   "bind-address": "0.0.0.0",
echo   "webroot": "web",
echo   "map-data-root": "maps",
echo   "live-data-root": "maps",
echo   "default-world": "world",
echo   "single-world-mode": false,
echo   "useCookies": true,
echo   "defaultToFlatView": false
echo }
) > "%BLUEMAP_PATH%\web\settings.json"

REM Create empty markers.json for each world
echo Creating empty markers files...
(
echo {
echo   "sets": {}
echo }
) > "%BLUEMAP_PATH%\web\maps\world\live\markers.json"
(
echo {
echo   "sets": {}
echo }
) > "%BLUEMAP_PATH%\web\maps\world_nether\live\markers.json"
(
echo {
echo   "sets": {}
echo }
) > "%BLUEMAP_PATH%\web\maps\world_the_end\live\markers.json"

REM Create empty players.json for each world
echo Creating empty players files...
(
echo {
echo   "players": []
echo }
) > "%BLUEMAP_PATH%\web\maps\world\live\players.json"
(
echo {
echo   "players": []
echo }
) > "%BLUEMAP_PATH%\web\maps\world_nether\live\players.json"
(
echo {
echo   "players": []
echo }
) > "%BLUEMAP_PATH%\web\maps\world_the_end\live\players.json"

echo Done! Files extracted to: %BLUEMAP_PATH%\web
echo Please restart your server to apply changes.
pause 