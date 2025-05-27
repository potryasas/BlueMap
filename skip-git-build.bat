@echo off
echo Running Java 8 compatibility fixes...
call .\fix-java8-compatibility.bat core
call .\fix-java8-compatibility.bat api
call .\fix-java8-compatibility.bat common
call .\fix-java8-compatibility.bat implementations

echo Building BlueMap without Git version checks...
SET BLUEMAP_VERSION=0.0.0-dev
call .\gradlew :bukkit-legacy:build -Pbluemap.version=%BLUEMAP_VERSION%

if %errorlevel% neq 0 (
  echo Build failed with error level %errorlevel%
  pause
) else (
  echo Build successful!
  pause
) 