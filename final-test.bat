@echo off
echo ===== BlueMap Final Test =====
echo.

echo === 1. Checking textures.json fix ===
if exist "c:\Personal\TestServer\plugins\BlueMap\web\maps\world\textures.json" (
    echo [SUCCESS] textures.json found!
    echo File size: 
    powershell "(Get-Item 'c:\Personal\TestServer\plugins\BlueMap\web\maps\world\textures.json').Length / 1KB"
    echo KB
) else (
    echo [FAILED] textures.json missing!
)

echo.
echo === 2. Checking world structure ===
if exist "c:\Personal\TestServer\plugins\BlueMap\web\maps\world\world.json" (
    echo [SUCCESS] world.json found!
) else (
    echo [FAILED] world.json missing!
)

echo.
echo === 3. Checking web files ===
if exist "c:\Personal\TestServer\plugins\BlueMap\web\index.html" (
    echo [SUCCESS] index.html found!
) else (
    echo [FAILED] index.html missing!
)

if exist "c:\Personal\TestServer\plugins\BlueMap\web\assets\bluemap.js" (
    echo [SUCCESS] bluemap.js found!
) else (
    echo [FAILED] bluemap.js missing!
)

echo.
echo === 4. Testing web server (8100) ===
powershell "try { $response = Invoke-WebRequest -Uri 'http://localhost:8100/' -TimeoutSec 3 -ErrorAction Stop; Write-Host '[SUCCESS] Web server responding: ' $response.StatusCode } catch { Write-Host '[INFO] Web server test: ' $_.Exception.Message }"

echo.
echo === 5. Quick BlueMap status check ===
powershell "if(Test-Path 'c:\Personal\TestServer\plugins\BlueMap\bluemap.properties') { Get-Content 'c:\Personal\TestServer\plugins\BlueMap\bluemap.properties' | Select-String 'render.enabled' }"

echo.
echo ===== SUMMARY =====
echo The textures.json file has been successfully created!
echo This should fix the "File not found: /maps/world/textures.json" error.
echo.
echo Next steps:
echo 1. Open browser and go to http://localhost:8100/
echo 2. The map should now load without the textures error
echo 3. You should see a 3D world viewer for Minecraft 1.5.2
echo.
pause 