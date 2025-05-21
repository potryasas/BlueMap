# BlueMap for Minecraft 1.5.2 (Legacy)

This is a special implementation of BlueMap designed to work with Minecraft 1.5.2 and CraftBukkit servers using Java 8.

## Features

- 3D map rendering of Minecraft 1.5.2 worlds
- Block support for all legacy blocks (pre-flattening IDs)
- Player markers on the map
- Legacy Bukkit API compatibility
- Java 8 compatibility

## Installation

1. Download the BlueMap-Legacy jar from the releases page
2. Place the jar in your server's `plugins` folder
3. Start your server once to generate the configuration files
4. Edit the configuration files in `plugins/BlueMap` to your liking
5. Restart your server or use `/bluemap reload`

## Commands

- `/bluemap` - Show the plugin information
- `/bluemap reload` - Reload the plugin configuration
- `/bluemap stop` - Stop all active renders
- `/bluemap render <world>` - Start rendering a world
- `/bluemap help` - Show help information

## Limitations

Due to the age of Minecraft 1.5.2, there are some limitations compared to the modern BlueMap implementations:

1. **Block Mapping**: Some block types in 1.5.2 may not map perfectly to their modern counterparts
2. **Biome Detection**: Biomes are approximated based on block types as 1.5.2 doesn't store biome data the same way
3. **Reduced Performance**: The legacy implementation may be slower due to compatibility layers
4. **Missing Features**: Some features available in newer Minecraft versions may not be available

## Compatibility Notes

- This implementation requires Java 8 or higher
- Only tested with CraftBukkit for Minecraft 1.5.2
- May not be compatible with all Bukkit plugins from that era

## Building from Source

To build this implementation:

```shell
./gradlew :bukkit-legacy:build
```

The built JAR will be in `implementations/bukkit-legacy/build/libs/`. 