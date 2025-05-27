@echo off
echo ===================================
echo BlueMap PNG Tile Test
echo ===================================

echo Creating test PNG tile structure...

REM Create lowres tile directories
mkdir "c:\Personal\TestServer\bluemap\web\maps\world\tiles\1\x0" 2>nul
mkdir "c:\Personal\TestServer\bluemap\web\maps\world\tiles\1\x1" 2>nul
mkdir "c:\Personal\TestServer\bluemap\web\maps\world\tiles\1\x-1" 2>nul

echo Creating simple test PNG tiles...

REM Test if we can access the tile URLs that were failing
echo Testing tile URLs:
echo http://localhost:8100/maps/world/tiles/1/x0/z0.png
echo http://localhost:8100/maps/world/tiles/1/x1/z0.png

REM Create a simple test PNG tile using PowerShell
powershell -Command "Add-Type -AssemblyName System.Drawing; $bmp = New-Object System.Drawing.Bitmap(512,512); $g = [System.Drawing.Graphics]::FromImage($bmp); $g.Clear([System.Drawing.Color]::Blue); $g.DrawString('Test Tile x0,z0', [System.Drawing.Font]::new('Arial', 24), [System.Drawing.Brushes]::White, 100, 250); $g.Dispose(); $bmp.Save('c:\Personal\TestServer\bluemap\web\maps\world\tiles\1\x0\z0.png', [System.Drawing.Imaging.ImageFormat]::Png); $bmp.Dispose(); Write-Host 'Test PNG tile created'"

echo.
echo Test tile created at:
echo c:\Personal\TestServer\bluemap\web\maps\world\tiles\1\x0\z0.png

REM Check if file exists
if exist "c:\Personal\TestServer\bluemap\web\maps\world\tiles\1\x0\z0.png" (
    echo SUCCESS: Test PNG tile exists
    dir "c:\Personal\TestServer\bluemap\web\maps\world\tiles\1\x0\z0.png"
) else (
    echo ERROR: Test PNG tile not created
)

echo.
echo Now restart the server and check if the PNG tile loads in browser:
echo http://localhost:8100/maps/world/tiles/1/x0/z0.png

pause 