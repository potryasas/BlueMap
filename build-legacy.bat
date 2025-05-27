@echo off
echo Building BlueMap Legacy Plugin...

REM Clean previous build
if exist "build" rmdir /s /q "build"
if exist "implementations\bukkit-legacy\build" rmdir /s /q "implementations\bukkit-legacy\build"

REM Copy web files from new version
echo Copying web files from new version...
if exist "BlueMapNEW\commonNEW\webapp\dist" (
    xcopy /E /I /Y "BlueMapNEW\commonNEW\webapp\dist" "implementations\bukkit-legacy\src\main\resources\web"
    echo Web files copied successfully.
) else (
    echo Warning: Web files not found in new version.
)

REM Run Gradle build
call gradlew.bat :implementations:bukkit-legacy:clean :implementations:bukkit-legacy:shadowJar

REM Copy built plugin to plugins folder
if exist "implementations\bukkit-legacy\build\libs\bluemap-bukkit-legacy.jar" (
    copy "implementations\bukkit-legacy\build\libs\bluemap-bukkit-legacy.jar" "plugins\BlueMap.jar"
    echo Build successful! Plugin copied to plugins\BlueMap.jar
) else (
    echo Build failed! Check the error messages above.
)

pause 