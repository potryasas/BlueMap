# BlueMap Bukkit Legacy Plugin

This is a legacy version of BlueMap plugin for Bukkit 1.5.2 (Minecraft 1.5.2), compatible with Java 8.

## Features

- Web-based map viewer for Minecraft 1.5.2 servers
- Customizable web interface
- Player markers on the map
- Compatible with Java 8 and Bukkit 1.5.2

## Installation

1. Download the latest `bluemap-bukkit-legacy-5.7-SNAPSHOT.jar` from the repository
2. Place the JAR file in your server's `plugins` directory
3. Start or restart your server
4. Configure the plugin by editing the configuration files in `plugins/BlueMap/`

## Commands

- `/bluemap reload` - Reload the plugin configuration
- `/bluemap help` - Show help information

## Configuration

After the first start, configuration files will be generated in the `plugins/BlueMap/` directory:

- `bluemap.properties` - Main configuration file
  - `webserver.port=8100` - Web server port (default: 8100)
  - `webserver.bind=0.0.0.0` - Web server binding address
  - `webserver.enabled=true` - Enable/disable web server

## Web Interface

The web interface is available at `http://your-server-ip:8100/` by default.

When first started, the following directories will be created:
- `plugins/BlueMap/web/` - Web server root directory
- `plugins/BlueMap/maps/` - Map data storage directory

## Troubleshooting

If the web interface doesn't start:
1. Check server logs for error messages
2. Make sure port 8100 (or the one you configured) is not in use by another process
3. Check that you have write permissions to the `plugins/BlueMap/` directory
4. If you changed the port in the configuration file, restart the server

If you don't see the web server startup message:
- After the server has loaded, type `/bluemap reload` to restart the plugin

## Building from Source

To build the plugin from source:

1. Clone the repository
2. Run `./gradlew :bukkit-legacy:shadowJar`
3. The JAR file will be created in `implementations/bukkit-legacy/build/libs/`

## Requirements

- Java 8 or higher
- Bukkit 1.5.2 server

## License

This project is licensed under the MIT License - see the LICENSE file for details. 