@echo off
echo Checking BlueMap web folder structure...
echo.

set TARGET_DIR=c:\Personal\TestServer\plugins\BlueMap\web

if exist "%TARGET_DIR%" (
    echo === BlueMap Web Directory Content ===
    dir "%TARGET_DIR%" /b
    echo.
    
    echo === Checking for key files ===
    if exist "%TARGET_DIR%\index.html" (
        echo [OK] index.html found
        echo === Index.html content (first 10 lines) ===
        type "%TARGET_DIR%\index.html" | head -10
    ) else (
        echo [MISSING] index.html
    )
    
    if exist "%TARGET_DIR%\settings.json" (
        echo [OK] settings.json found
        echo === Settings.json content ===
        type "%TARGET_DIR%\settings.json"
    ) else (
        echo [MISSING] settings.json
    )
    
    if exist "%TARGET_DIR%\assets" (
        echo [OK] assets directory found
        echo === Assets directory content ===
        dir "%TARGET_DIR%\assets" /b
    ) else (
        echo [MISSING] assets directory
    )
    
    if exist "%TARGET_DIR%\maps" (
        echo [OK] maps directory found
        echo === Maps directory content ===
        dir "%TARGET_DIR%\maps" /b
    ) else (
        echo [MISSING] maps directory
    )
    
) else (
    echo [ERROR] Web directory not found at: %TARGET_DIR%
)

echo.
echo === Checking BlueMap plugin log ===
if exist "c:\Personal\TestServer\logs\latest.log" (
    echo === Last BlueMap log entries ===
    findstr /i "bluemap" "c:\Personal\TestServer\logs\latest.log" | tail -20
) else (
    echo [INFO] Server log not found
)

echo.
echo === Testing web server access ===
curl -I http://localhost:8100/ 2>nul
if %errorlevel% == 0 (
    echo [OK] Web server responding
) else (
    echo [ERROR] Web server not responding on port 8100
)

pause 