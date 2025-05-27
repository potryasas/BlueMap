@echo off
echo Building BlueMap plugin...

REM Run Gradle build
call gradlew.bat clean build

REM Copy the built JAR to the server plugins folder
copy /Y "build\libs\bluemap-legacy.jar" "C:\Personal\TestServer\plugins\bluemap-legacy.jar"

echo Build complete! Please restart your server. 