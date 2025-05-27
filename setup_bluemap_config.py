import os
from pathlib import Path

# Define paths
SERVER_DIR = Path(r"c:\Personal\TestServer")
BLUEMAP_CONFIG_DIR = SERVER_DIR / "plugins" / "BlueMap"

def setup_plugin_config():
    """Create BlueMap plugin configuration"""
    config_content = """##                          ##
##         BlueMap          ##
##      Plugin-Config       ##
##                          ##

# If the server should send player-positions to the webapp.
# This only works if the integrated webserver is enabled.
live-player-markers: true

# A list of gamemodes that will prevent a player from appearing on the map.
# Possible values are: survival, creative, spectator, adventure
hidden-game-modes: [
    "spectator"
]

# If this is true, players that are vanished (by a plugin) will be hidden on the map.
hide-vanished: true

# If this is true, players that have an invisibility (potion-)effect will be hidden on the map.
hide-invisible: false

# If this is true, players that are sneaking will be hidden on the map.
hide-sneaking: false

# If this is true, players that are on a different world than the viewed map will not appear on the player-list.
hide-different-world: false

# The interval in seconds that the players will be written to the map-storage.
# This is useful if you can't create a live-connection between the server and the webapp
write-players-interval: 3

# Download the skin from mojang-servers when a player joins your server
skin-download: true

# The amount of players that is needed to pause BlueMap's render-threads.
player-render-limit: -1

# The interval in minutes in which a full map-update will be triggered.
full-update-interval: 1440"""

    # Create config directory if it doesn't exist
    BLUEMAP_CONFIG_DIR.mkdir(parents=True, exist_ok=True)
    
    # Write plugin config
    plugin_conf = BLUEMAP_CONFIG_DIR / "plugin.conf"
    with open(plugin_conf, 'w', encoding='utf-8') as f:
        f.write(config_content)
    print(f"Created BlueMap plugin config at: {plugin_conf}")

if __name__ == "__main__":
    setup_plugin_config() 