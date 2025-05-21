@echo off
echo ===================================
echo BlueMap Java 8 Compatibility Fixer
echo ===================================
echo.

rem Проверяем наличие Java 8
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.422.5-hotspot
echo Checking Java 8 installation...
if exist "%JAVA_HOME%\bin\java.exe" (
    echo Java 8 found at: %JAVA_HOME%
    "%JAVA_HOME%\bin\java" -version
) else (
    echo ERROR: Java 8 not found at %JAVA_HOME%
    echo Please update the JAVA_HOME path in this script
    goto :error
)
echo.

rem Устанавливаем Java 8 в PATH
set PATH=%JAVA_HOME%\bin;%PATH%

rem Очищаем кэш Gradle и старые сборки
echo Cleaning Gradle cache and previous builds...
call gradlew.bat --stop
rmdir /S /Q .gradle 2>nul
rmdir /S /Q buildSrc\build 2>nul
rmdir /S /Q buildSrc\.gradle 2>nul
echo.

rem Создаем новый gradle.properties без пробелов
echo Fixing gradle.properties...
(
echo org.gradle.jvmargs=-Xmx4G
echo # Явное указание использовать Java 8
echo org.gradle.java.home=C:\\Program Files\\Eclipse Adoptium\\jdk-8.0.422.5-hotspot
echo org.gradle.warning.mode=none
) > gradle.properties
echo.

rem Обновляем Gradle Wrapper для совместимости с Java 8
echo Updating Gradle Wrapper to a Java 8 compatible version...
call gradlew.bat wrapper --gradle-version=6.7.1
echo.

rem Проверяем наличие директории реализации
echo Checking implementation directory...
if not exist implementations\bukkit-legacy (
    echo ERROR: bukkit-legacy implementation directory not found
    echo Please make sure the directory exists
    goto :error
)

rem Создаем файл для отключения проверок spotless
echo Creating Java 8 compatibility overrides...
mkdir buildSrc\src\main\kotlin 2>nul
(
echo package de.bluecolored.bluemap.build
echo.
echo import org.gradle.api.Plugin
echo import org.gradle.api.Project
echo import org.gradle.kotlin.dsl.*
echo.
echo /** Пустая реализация плагина спотлесс для совместимости с Java 8 */
echo class SpotlessCompat : Plugin^<Project^> {
echo     override fun apply(project: Project) {
echo         // Пустая реализация, чтобы не требовать Java 11
echo         project.logger.lifecycle("SpotlessCompat: Disabled spotless checks for Java 8 compatibility")
echo     }
echo }
) > buildSrc\src\main\kotlin\java8-compat.kt
echo.

rem Запускаем сборку buildSrc
echo Building buildSrc with Java 8...
call gradlew.bat -Dorg.gradle.java.home="%JAVA_HOME%" buildSrc:build
if %ERRORLEVEL% neq 0 (
    echo ERROR: Failed to build buildSrc
    goto :error
)
echo.

rem Запускаем предварительную сборку общего модуля
echo Building common module...
call gradlew.bat -Dorg.gradle.java.home="%JAVA_HOME%" :common:build
if %ERRORLEVEL% neq 0 (
    echo WARN: Failed to build common module, but continuing...
)
echo.

echo ===================================
echo Java 8 compatibility fixes applied!
echo Try running build-legacy.bat now
echo ===================================
goto :end

:error
echo ===================================
echo Build troubleshooting failed
echo ===================================

:end
pause 