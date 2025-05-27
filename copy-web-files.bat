@echo off
echo Copying BlueMap web files to test server...

set "TEST_SERVER_PATH=C:\Personal\TestServer\plugins\BlueMap"
set "WEBAPP_ZIP=%~dp0webapp.zip"

REM Copy webapp.zip to plugins folder
echo Copying webapp.zip to plugins folder...
copy /Y "%WEBAPP_ZIP%" "C:\Personal\TestServer\plugins\webapp.zip"

REM Create required directories
mkdir "%TEST_SERVER_PATH%\web" 2>nul
mkdir "%TEST_SERVER_PATH%\web\assets" 2>nul
mkdir "%TEST_SERVER_PATH%\web\assets\css" 2>nul
mkdir "%TEST_SERVER_PATH%\web\assets\js" 2>nul
mkdir "%TEST_SERVER_PATH%\web\assets\images" 2>nul
mkdir "%TEST_SERVER_PATH%\web\assets\images\icons" 2>nul
mkdir "%TEST_SERVER_PATH%\web\maps" 2>nul

REM Create directories for all worlds
mkdir "%TEST_SERVER_PATH%\web\maps\world" 2>nul
mkdir "%TEST_SERVER_PATH%\web\maps\world\live" 2>nul
mkdir "%TEST_SERVER_PATH%\web\maps\world_nether" 2>nul
mkdir "%TEST_SERVER_PATH%\web\maps\world_nether\live" 2>nul
mkdir "%TEST_SERVER_PATH%\web\maps\world_the_end" 2>nul
mkdir "%TEST_SERVER_PATH%\web\maps\world_the_end\live" 2>nul

REM Copy textures.json to all required locations
echo Copying textures.json...
copy /Y "%~dp0textures.json" "%TEST_SERVER_PATH%\web\maps\world\textures.json"
copy /Y "%~dp0textures.json" "%TEST_SERVER_PATH%\web\maps\world_nether\textures.json"
copy /Y "%~dp0textures.json" "%TEST_SERVER_PATH%\web\maps\world_the_end\textures.json"

REM Create settings.json for each world
echo Creating world settings files...

REM For world
(
echo {
echo   "name": "world",
echo   "hires": {
echo     "enabled": true,
echo     "save": true
echo   },
echo   "lowres": {
echo     "enabled": true,
echo     "save": true
echo   },
echo   "skyColor": "#7dabff",
echo   "ambientLight": 0.1,
echo   "equirectangular": false,
echo   "renderEdges": true,
echo   "renderAll": false
echo } 
) > "%TEST_SERVER_PATH%\web\maps\world\settings.json"

REM For nether
(
echo {
echo   "name": "world_nether",
echo   "hires": {
echo     "enabled": true,
echo     "save": true
echo   },
echo   "lowres": {
echo     "enabled": true,
echo     "save": true
echo   },
echo   "skyColor": "#300000",
echo   "ambientLight": 0.2,
echo   "equirectangular": false,
echo   "renderEdges": true,
echo   "renderAll": false
echo }
) > "%TEST_SERVER_PATH%\web\maps\world_nether\settings.json"

REM For end
(
echo {
echo   "name": "world_the_end",
echo   "hires": {
echo     "enabled": true,
echo     "save": true
echo   },
echo   "lowres": {
echo     "enabled": true,
echo     "save": true
echo   },
echo   "skyColor": "#000000",
echo   "ambientLight": 0.1,
echo   "equirectangular": false,
echo   "renderEdges": true,
echo   "renderAll": false
echo }
) > "%TEST_SERVER_PATH%\web\maps\world_the_end\settings.json"

REM Create empty markers.json for each world
echo Creating empty markers files...
echo { "sets": {} } > "%TEST_SERVER_PATH%\web\maps\world\live\markers.json"
echo { "sets": {} } > "%TEST_SERVER_PATH%\web\maps\world_nether\live\markers.json"
echo { "sets": {} } > "%TEST_SERVER_PATH%\web\maps\world_the_end\live\markers.json"

REM Create empty players.json for each world
echo Creating empty players files...
echo { "players": [] } > "%TEST_SERVER_PATH%\web\maps\world\live\players.json"
echo { "players": [] } > "%TEST_SERVER_PATH%\web\maps\world_nether\live\players.json"
echo { "players": [] } > "%TEST_SERVER_PATH%\web\maps\world_the_end\live\players.json"

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
echo   "defaultToFlatView": false,
echo   "resolutionDefault": 1.0,
echo   "minZoomDistance": 50,
echo   "maxZoomDistance": 100000,
echo   "hiresSliderMax": 500,
echo   "hiresSliderDefault": 100,
echo   "hiresSliderMin": 0,
echo   "lowresSliderMax": 7000,
echo   "lowresSliderDefault": 2000,
echo   "lowresSliderMin": 500
echo }
) > "%TEST_SERVER_PATH%\web\settings.json"

REM Create simple index.html
echo Creating index.html...
(
echo ^<!DOCTYPE html^>
echo ^<html lang="en"^>
echo ^<head^>
echo     ^<meta charset="UTF-8"^>
echo     ^<meta name="viewport" content="width=device-width, initial-scale=1.0"^>
echo     ^<title^>BlueMap^</title^>
echo     ^<link rel="stylesheet" href="assets/css/style.css"^>
echo     ^<script src="assets/js/bluemap.js"^>^</script^>
echo ^</head^>
echo ^<body^>
echo     ^<div id="bluemap"^>^</div^>
echo     ^<script^>
echo         document.addEventListener('DOMContentLoaded', function() {
echo             BlueMap.init({
echo                 container: 'bluemap',
echo                 maps: ['world', 'world_nether', 'world_the_end']
echo             });
echo         });
echo     ^</script^>
echo ^</body^>
echo ^</html^>
) > "%TEST_SERVER_PATH%\web\index.html"

echo Done! Files copied to: %TEST_SERVER_PATH%
echo Please restart your server to apply changes.
pause 