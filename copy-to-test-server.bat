@echo off
echo Copying BlueMap files to test server...

set TEST_SERVER=c:\Personal\TestServer
set PLUGIN_DIR=%TEST_SERVER%\plugins\BlueMap

if not exist "%PLUGIN_DIR%" mkdir "%PLUGIN_DIR%"

echo Copying output2.json...
copy /Y "output2.json" "%PLUGIN_DIR%\output2.json"

echo Copying plugin files...
copy /Y "implementations\bukkit-legacy\build\libs\*.jar" "%PLUGIN_DIR%\"

echo Copying web files...
if not exist "%PLUGIN_DIR%\web" mkdir "%PLUGIN_DIR%\web"
xcopy /Y /E /I "web\*" "%PLUGIN_DIR%\web\"

echo Done!
pause 