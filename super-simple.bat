@echo on
echo Шаг 1: Начало выполнения скрипта
pause

echo Шаг 2: Проверка Java
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.422.5-hotspot"
echo JAVA_HOME установлен: %JAVA_HOME%
pause

echo Шаг 3: Создаю директории
if not exist "implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy" (
  mkdir "implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy"
)
echo Директории созданы
pause

echo Шаг 4: Создаю gradle.properties
echo org.gradle.jvmargs=-Xmx2G > gradle.properties
echo org.gradle.java.home=C:\Program Files\Eclipse Adoptium\jdk-8.0.422.5-hotspot >> gradle.properties 
echo gradle.properties создан
pause

echo Шаг 5: Создаю settings.gradle
echo rootProject.name = 'bluemap-legacy' > settings.gradle
echo include ':bukkit-legacy' >> settings.gradle
echo project(':bukkit-legacy').projectDir = file('implementations/bukkit-legacy') >> settings.gradle
echo settings.gradle создан
pause

echo Шаг 6: Создаю build.gradle
echo apply plugin: 'java' > implementations\bukkit-legacy\build.gradle
echo build.gradle создан
pause

echo Шаг 7: Создаю тестовый класс
echo package de.bluecolored.bluemap.bukkit.legacy; > implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy\DummyClass.java
echo. >> implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy\DummyClass.java 
echo public class DummyClass { >> implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy\DummyClass.java
echo     public static void main(String[] args) { >> implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy\DummyClass.java
echo         System.out.println("Hello from BlueMap Legacy!"); >> implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy\DummyClass.java
echo     } >> implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy\DummyClass.java
echo } >> implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy\DummyClass.java
echo тестовый класс создан
pause

echo Шаг 8: Компиляция
call gradlew.bat -Dorg.gradle.java.home="%JAVA_HOME%" :bukkit-legacy:compileJava
echo Компиляция завершена с кодом: %ERRORLEVEL%
pause

echo Шаг 9: Создание JAR
call gradlew.bat -Dorg.gradle.java.home="%JAVA_HOME%" :bukkit-legacy:jar
echo Создание JAR завершено с кодом: %ERRORLEVEL%
pause

echo Шаг 10: Готово!
echo Нажмите любую клавишу для выхода...
pause 