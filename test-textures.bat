@echo off
echo Testing textures.json creation...
echo.

set WEB_DIR=c:\Personal\TestServer\plugins\BlueMap\web

echo === Checking if web directory exists ===
if exist "%WEB_DIR%" (
    echo [OK] Web directory found
    echo.
    
    echo === Checking for maps directory ===
    if exist "%WEB_DIR%\maps" (
        echo [OK] Maps directory found
        dir "%WEB_DIR%\maps" /b
        echo.
        
        echo === Checking for world subdirectories ===
        for /d %%d in ("%WEB_DIR%\maps\*") do (
            echo.
            echo [WORLD] %%~nxd
            if exist "%%d\textures.json" (
                echo [OK] textures.json found in %%~nxd
                echo First few lines:
                type "%%d\textures.json" | head -5
            ) else (
                echo [MISSING] textures.json in %%~nxd
            )
            
            if exist "%%d\world.json" (
                echo [OK] world.json found in %%~nxd
            ) else (
                echo [MISSING] world.json in %%~nxd
            )
        )
    ) else (
        echo [MISSING] Maps directory
    )
) else (
    echo [ERROR] Web directory not found
)

echo.
echo === Testing web server response ===
curl -I http://localhost:8100/ 2>nul
if %errorlevel% == 0 (
    echo [OK] Web server responding
) else (
    echo [ERROR] Web server not responding
)

pause 