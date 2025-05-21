@echo off
echo Setting up Java environment...
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.422.5-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo Checking Java version...
java -version

echo Current directory:
cd

echo Starting Gradle build...
call gradlew.bat :bukkit-legacy:build --warning-mode=none --info

echo Build completed with exit code: %ERRORLEVEL%
pause 