@echo off
setlocal

:: Set Java 8 path
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.422.5-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

:: Create libs directory if it doesn't exist
if not exist "implementations\bukkit-legacy\libs" mkdir "implementations\bukkit-legacy\libs"

:: Copy Bukkit jar if it exists in the root libs folder
if exist "libs\bukkit-1.5.2-R1.0.jar" (
    copy "libs\bukkit-1.5.2-R1.0.jar" "implementations\bukkit-legacy\libs\"
) else (
    echo ERROR: bukkit-1.5.2-R1.0.jar not found in libs folder!
    pause
    exit /b 1
)

:: Run the build
echo Building with Java 8...
call gradlew.bat -Dorg.gradle.java.home="%JAVA_HOME%" :bukkit-legacy:build --warning-mode=none

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo Build completed successfully!
pause 