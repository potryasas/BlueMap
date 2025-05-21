# BlueMap Port to Minecraft 1.5.2 (Java 8) Plan

## Overview
This document outlines the process for porting BlueMap from its current state (supporting Minecraft 1.16+) to Minecraft 1.5.2 with Java 8 compatibility.

## Current State Analysis
- BlueMap currently supports Minecraft versions 1.16 - 1.21+
- Multiple platform implementations: Spigot, Paper, Forge, Fabric, Sponge, etc.
- Uses modern Java features not available in Java 8
- Depends on libraries and APIs that may not be compatible with Minecraft 1.5.2

## Major Challenges

### 1. Bukkit/Spigot API Changes
- Minecraft 1.5.2 uses a significantly different Bukkit API version
- Many methods and classes present in modern versions don't exist in 1.5.2
- Event handling system differences
- Player and World handling changes

### 2. Java 8 Compatibility
- Remove Java 9+ features like:
  - `var` keyword
  - New collection factory methods
  - Module system
  - New Stream API methods
  - Private methods in interfaces

### 3. World Format Changes
- Minecraft 1.5.2 uses a different world format (Anvil, but earlier version)
- Block IDs and data values are completely different
- Different biome mapping

### 4. Dependency Management
- Update build system to target Java 8
- Find compatible versions of dependencies or replace them
- Remove dependencies on modern Minecraft features

## Implementation Plan

### Phase 1: Project Setup
1. Create a new branch for the 1.5.2 port
2. Modify build configurations to support Java 8
3. Update Gradle scripts to target Minecraft 1.5.2 for Bukkit implementation

### Phase 2: Core Module Adaptation
1. Refactor core module to be compatible with Java 8
2. Adapt rendering engine to work with Minecraft 1.5.2 world format
3. Update block and biome mapping for 1.5.2 compatibility

### Phase 3: Bukkit Implementation
1. Create a specialized implementation for Minecraft 1.5.2
2. Adapt the plugin to use the older Bukkit API
3. Rewrite player and world handling to match 1.5.2 API
4. Update event listeners for compatibility

### Phase 4: Testing and Optimization
1. Test the ported plugin on a Minecraft 1.5.2 server
2. Optimize performance for older server environments
3. Fix compatibility issues and bugs

## Specific Code Changes Required

### Build System
- Update `settings.gradle.kts` and implementation build files to include 1.5.2 support
- Add a dedicated implementation for CraftBukkit 1.5.2
- Set Java compiler compatibility to Java 8

### API Compatibility
- Create adapter classes for handling differences between modern and legacy Bukkit APIs
- Rewrite world loading mechanics to work with the older Anvil format
- Adapt player tracking for 1.5.2 API

### Java 8 Compatibility
- Replace stream operations with traditional loops where needed
- Replace modern Java API calls with Java 8 compatible alternatives
- Refactor code using newer language features

## Conclusion
Porting BlueMap to Minecraft 1.5.2 with Java 8 compatibility is a significant undertaking that will require substantial code changes and adaptation. The differences in the Minecraft API and world format between 1.5.2 and modern versions (1.16+) are substantial, requiring careful refactoring and testing. 