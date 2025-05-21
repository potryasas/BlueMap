# PowerShell script for building BlueMap Legacy

Write-Host "###########################################" -ForegroundColor Green
Write-Host "#    BlueMap Legacy Builder for PowerShell #" -ForegroundColor Green  
Write-Host "###########################################" -ForegroundColor Green
Write-Host ""

# Check Java 8
Write-Host "[1/8] Checking Java 8..." -ForegroundColor Cyan
$JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-8.0.422.5-hotspot"
if (Test-Path "$JAVA_HOME\bin\java.exe") {
    Write-Host "Java 8 found:" -ForegroundColor Green
    & "$JAVA_HOME\bin\java.exe" -version
}
else {
    Write-Host "ERROR: Java not found at path: $JAVA_HOME" -ForegroundColor Red
    Read-Host "Press ENTER to exit"
    exit 1
}

# Create structure
Write-Host "[2/8] Creating project structure..." -ForegroundColor Cyan
$SRC_DIR = "implementations\bukkit-legacy\src\main\java\de\bluecolored\bluemap\bukkit\legacy"
$RES_DIR = "implementations\bukkit-legacy\src\main\resources"

if (-not (Test-Path $SRC_DIR)) {
    New-Item -Path $SRC_DIR -ItemType Directory -Force | Out-Null
    Write-Host "Created directory: $SRC_DIR" -ForegroundColor Green
}

if (-not (Test-Path $RES_DIR)) {
    New-Item -Path $RES_DIR -ItemType Directory -Force | Out-Null
    Write-Host "Created directory: $RES_DIR" -ForegroundColor Green
}

# Create gradle.properties
Write-Host "[3/8] Creating gradle.properties..." -ForegroundColor Cyan
$gradleProps = @"
org.gradle.jvmargs=-Xmx2G
org.gradle.java.home=C:\\Program Files\\Eclipse Adoptium\\jdk-8.0.422.5-hotspot
"@
[System.IO.File]::WriteAllText("$PWD\gradle.properties", $gradleProps, [System.Text.Encoding]::ASCII)
Write-Host "gradle.properties created" -ForegroundColor Green

# Create settings.gradle
Write-Host "[4/8] Creating settings.gradle..." -ForegroundColor Cyan
$settingsGradle = @"
rootProject.name = 'bluemap-legacy'
include ':bukkit-legacy'
project(':bukkit-legacy').projectDir = file('implementations/bukkit-legacy')
"@
[System.IO.File]::WriteAllText("$PWD\settings.gradle", $settingsGradle, [System.Text.Encoding]::ASCII)
Write-Host "settings.gradle created" -ForegroundColor Green

# Create build.gradle
Write-Host "[5/8] Creating build.gradle..." -ForegroundColor Cyan
$buildGradle = @"
plugins {
    id 'java'
}

repositories {
    mavenCentral()
    maven {
        url 'https://repo.codemc.org/repository/maven-public'
    }
    maven {
        url 'https://libraries.minecraft.net'
    }
}

dependencies {
    compileOnly 'org.bukkit:bukkit:1.5.2-R1.0'
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
"@
[System.IO.File]::WriteAllText("$PWD\implementations\bukkit-legacy\build.gradle", $buildGradle, [System.Text.Encoding]::ASCII)
Write-Host "build.gradle created" -ForegroundColor Green

# Create Java class
Write-Host "[6/8] Creating Java class..." -ForegroundColor Cyan
$javaClass = @"
package de.bluecolored.bluemap.bukkit.legacy;

import org.bukkit.plugin.java.JavaPlugin;

public class DummyClass extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("BlueMap Legacy for Minecraft 1.5.2 started!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BlueMap Legacy stopped!");
    }
}
"@
[System.IO.File]::WriteAllText("$PWD\$SRC_DIR\DummyClass.java", $javaClass, [System.Text.Encoding]::ASCII)
Write-Host "DummyClass.java created" -ForegroundColor Green

# Create plugin.yml
Write-Host "[7/8] Creating plugin.yml..." -ForegroundColor Cyan
$pluginYml = @"
name: BlueMap
version: 1.0.0
main: de.bluecolored.bluemap.bukkit.legacy.DummyClass
author: "Blue"
description: "A 3D map for Minecraft 1.5.2"
"@
[System.IO.File]::WriteAllText("$PWD\$RES_DIR\plugin.yml", $pluginYml, [System.Text.Encoding]::ASCII)
Write-Host "plugin.yml created" -ForegroundColor Green

# Compilation
Write-Host "[8/8] Compiling and creating JAR..." -ForegroundColor Cyan
Write-Host "Stopping Gradle daemon..." -ForegroundColor Yellow
& .\gradlew.bat --stop

Write-Host "Starting compilation..." -ForegroundColor Yellow
& .\gradlew.bat "-Dorg.gradle.java.home=$JAVA_HOME" :bukkit-legacy:compileJava --no-daemon --info | Tee-Object -FilePath "build_powershell.log"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Compilation failed. Code: $LASTEXITCODE" -ForegroundColor Red
    Get-Content "build_powershell.log" | Select-Object -Last 20
    Read-Host "Press ENTER to exit"
    exit 1
}

Write-Host "Creating JAR file..." -ForegroundColor Yellow
& .\gradlew.bat "-Dorg.gradle.java.home=$JAVA_HOME" :bukkit-legacy:jar --no-daemon --info | Tee-Object -Append -FilePath "build_powershell.log"
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: JAR creation failed. Code: $LASTEXITCODE" -ForegroundColor Red
    Get-Content "build_powershell.log" | Select-Object -Last 20
    Read-Host "Press ENTER to exit"
    exit 1
}

Write-Host ""
Write-Host "Build completed successfully!" -ForegroundColor Green
Write-Host "JAR path: implementations\bukkit-legacy\build\libs\" -ForegroundColor Cyan
Write-Host "Checking created files:" -ForegroundColor Cyan
Get-ChildItem "implementations\bukkit-legacy\build\libs\" | ForEach-Object { Write-Host "- $_" -ForegroundColor Yellow }

Read-Host "Press ENTER to exit" 