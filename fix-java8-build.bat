@echo off
setlocal enabledelayedexpansion

:: Set Java 8 path
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.422.5-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

:: Add Git to PATH
set PATH=%PATH%;C:\Program Files\Git\bin

:: Create directories
if not exist "buildtools" mkdir buildtools
if not exist "libs" mkdir libs

:: Download older BuildTools version
echo Downloading BuildTools...
powershell -Command "& {Invoke-WebRequest -Uri 'https://hub.spigotmc.org/jenkins/job/BuildTools/164/artifact/target/BuildTools.jar' -OutFile 'buildtools/BuildTools.jar'}"

:: Build Bukkit 1.5.2-R1.0 with Java 8
echo Building Bukkit 1.5.2-R1.0 with Java 8...
cd buildtools
"%JAVA_HOME%\bin\java" -jar BuildTools.jar --rev 1.5.2-R1.0 --remapped
cd ..

:: Copy the built jar to libs directory
echo Copying Bukkit jar...
if exist "buildtools\Bukkit\target\bukkit-1.5.2-R1.0.jar" (
    copy "buildtools\Bukkit\target\bukkit-1.5.2-R1.0.jar" "libs\"
) else (
    echo ERROR: Bukkit jar was not built successfully
    pause
    exit /b 1
)

:: Update build.gradle to use local jar
echo Updating build.gradle...
(
echo plugins {
echo     id 'java'
echo }
echo.
echo repositories {
echo     mavenCentral()
echo     flatDir {
echo         dirs 'libs'
echo     }
echo }
echo.
echo dependencies {
echo     compileOnly files('libs/bukkit-1.5.2-R1.0.jar')
echo }
echo.
echo java {
echo     sourceCompatibility = JavaVersion.VERSION_1_8
echo     targetCompatibility = JavaVersion.VERSION_1_8
echo }
) > implementations\bukkit-legacy\build.gradle

echo Done! Now you can run your build script.
pause 