@echo off
title BlueMap Build
color 0A

echo ###########################################
echo #    BlueMap Legacy Builder для CMD      #
echo ###########################################
echo.

REM Проверка Java 8
echo [1/8] Проверка Java 8...
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.422.5-hotspot"
if exist "%JAVA_HOME%\bin\java.exe" (
    "%JAVA_HOME%\bin\java" -version
) else (
    echo ОШИБКА: Java не найдена по указанному пути: %JAVA_HOME%
    goto :error
)

REM Создание структуры
echo [2/8] Создание структуры проекта...
if not exist "implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy" (
    mkdir "implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy"
)
if not exist "implementations\bukkit-legacy\src\main\resources" (
    mkdir "implementations\bukkit-legacy\src\main\resources"
)

REM Создание gradle.properties
echo [3/8] Создание gradle.properties...
(
echo org.gradle.jvmargs=-Xmx2G
echo org.gradle.java.home=C:\Program Files\Eclipse Adoptium\jdk-8.0.422.5-hotspot
)>gradle.properties

REM Создание settings.gradle
echo [4/8] Создание settings.gradle...
(
echo rootProject.name = 'bluemap-legacy'
echo include ':bukkit-legacy'
echo project(':bukkit-legacy').projectDir = file('implementations/bukkit-legacy')
)>settings.gradle

REM Создание build.gradle
echo [5/8] Создание build.gradle...
(
echo plugins {
echo     id 'java'
echo }
echo.
echo repositories {
echo     mavenCentral^(^)
echo }
echo.
echo java {
echo     sourceCompatibility = JavaVersion.VERSION_1_8
echo     targetCompatibility = JavaVersion.VERSION_1_8
echo }
)>"implementations\bukkit-legacy\build.gradle"

REM Создание Java класса
echo [6/8] Создание Java класса...
(
echo package de.bluecolored.bluemap.bukkit.legacy;
echo.
echo public class DummyClass {
echo     public static void main(String[] args) {
echo         System.out.println("Hello from BlueMap Legacy!");
echo     }
echo }
)>"implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy\DummyClass.java"

REM Компиляция
echo [7/8] Компиляция проекта...
call gradlew.bat --stop
call gradlew.bat -Dorg.gradle.java.home="%JAVA_HOME%" :bukkit-legacy:compileJava --no-daemon
if %ERRORLEVEL% neq 0 goto :error

REM Создание JAR
echo [8/8] Создание JAR файла...
call gradlew.bat -Dorg.gradle.java.home="%JAVA_HOME%" :bukkit-legacy:jar --no-daemon
if %ERRORLEVEL% neq 0 goto :error

echo.
echo Сборка успешно завершена!
echo Путь к JAR: implementations\bukkit-legacy\build\libs\
goto :end

:error
echo.
echo ОШИБКА: Сборка завершилась с ошибкой

:end
echo.
echo Нажмите любую клавишу для выхода...
pause > nul 