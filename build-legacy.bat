@echo off
setlocal

:: Set Java 8 path
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.422.5-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

:: Clean previous builds
echo Cleaning previous builds...
call gradlew.bat clean

:: Build with Java 8
echo Building with Java 8...
call gradlew.bat -Dorg.gradle.java.home="%JAVA_HOME%" :bukkit-legacy:build --warning-mode=none

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo Build completed successfully!
pause 