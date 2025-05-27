import json
import os
from pathlib import Path
import time
import uuid
import glob
import shutil

# Base paths
TEST_SERVER_DIR = Path(r"c:\Personal\TestServer")
MAPS_DIR = TEST_SERVER_DIR / "web" / "maps"
WORLDS = ["world", "world_nether", "world_the_end"]
WORLD_DIR = MAPS_DIR / "world" / "live"

def get_server_players():
    """Get real players from server data"""
    try:
        # Try to read player data files from server
        player_data_dir = TEST_SERVER_DIR / "world" / "playerdata"
        if not player_data_dir.exists():
            return []

        players = []
        # Read usercache.json for player names
        usercache_file = TEST_SERVER_DIR / "usercache.json"
        name_uuid_map = {}
        if usercache_file.exists():
            with open(usercache_file, 'r') as f:
                try:
                    usercache = json.load(f)
                    for entry in usercache:
                        name_uuid_map[entry['uuid']] = entry['name']
                except:
                    pass

        # Read latest.log to get online players and their positions
        log_file = TEST_SERVER_DIR / "logs" / "latest.log"
        online_players = {}
        if log_file.exists():
            with open(log_file, 'r', encoding='utf-8') as f:
                for line in f.readlines():
                    if "logged in with entity id" in line:
                        parts = line.split('[')
                        if len(parts) > 1:
                            player_name = parts[1].split(']')[0]
                            online_players[player_name] = True

        # Read player data files
        for dat_file in glob.glob(str(player_data_dir / "*.dat")):
            try:
                # Get UUID from filename
                uuid_str = os.path.basename(dat_file).replace(".dat", "")
                
                # Get player name from usercache
                player_name = name_uuid_map.get(uuid_str, "Unknown Player")
                
                # Only include if player is online
                if player_name in online_players:
                    # Read last known position from level.dat or other source
                    # For now using example position
                    players.append(create_player_data(
                        player_name,
                        x=100 * len(players),  # Spread players out for visibility
                        y=64,
                        z=100
                    ))
            except Exception as e:
                print(f"Error reading player data {dat_file}: {e}")
                
        return players
    except Exception as e:
        print(f"Error getting server players: {e}")
        return []

def create_player_data(player_name, x, y, z, pitch=0, yaw=0, roll=0):
    return {
        "uuid": str(uuid.uuid4()),
        "name": player_name,
        "foreign": False,
        "position": {
            "x": x,
            "y": y,
            "z": z
        },
        "rotation": {
            "pitch": pitch,
            "yaw": yaw,
            "roll": roll
        }
    }

def setup_player_files():
    # Create base player marker configuration
    player_data = {
        "players": [],
        "updateInterval": 1000,
        "showPlayerMarkers": True,
        "showPlayerBody": True,
        "showPlayerHead": True,
        "showLabelBackground": True,
        "markerSetId": "players",
        "markerSet": {
            "id": "players",
            "label": "Players",
            "toggleable": True,
            "defaultHidden": False,
            "priority": 1000
        }
    }

    # Create player marker files for each world
    for world in WORLDS:
        live_dir = MAPS_DIR / world / "live"
        live_dir.mkdir(parents=True, exist_ok=True)
        
        # Create players.json
        players_path = live_dir / "players.json"
        with open(players_path, 'w') as f:
            json.dump(player_data, f, indent=2)
            print(f"Created players file for {world}")

def update_player_data(world_name, players_data):
    live_dir = MAPS_DIR / world_name / "live"
    players_file = live_dir / "players.json"
    
    try:
        with open(players_file, 'r') as f:
            data = json.load(f)
        
        # Update only the players array while keeping other settings
        data["players"] = players_data
        
        with open(players_file, 'w') as f:
            json.dump(data, f, indent=2)
            print(f"Updated players for {world_name}")
    except Exception as e:
        print(f"Error updating {world_name} players:", e)

def main():
    print("Starting player markers fix...")
    
    # Step 1: Create necessary directories
    ensure_directories()
    
    # Step 2: Create empty players.json with proper structure
    create_empty_players_json()
    
    # Step 3: Update plugin configuration
    update_plugin_config()
    
    print("\nPlayer markers fix completed!")
    print("\nNext steps:")
    print("1. Restart your Minecraft server")
    print("2. Check the web interface at http://localhost:8100")
    print("3. Verify that player markers appear when players are online")

def ensure_directories():
    """Create necessary directories if they don't exist"""
    directories = [
        MAPS_DIR,
        WORLD_DIR
    ]
    
    for directory in directories:
        directory.mkdir(parents=True, exist_ok=True)
        print(f"Ensured directory exists: {directory}")

def create_empty_players_json():
    """Create an empty players.json file with proper structure"""
    players_file = WORLD_DIR / "players.json"
    
    players_data = {
        "players": [],
        "updateInterval": 1000,
        "showPlayerMarkers": True,
        "showPlayerBody": True,
        "showPlayerHead": True,
        "showLabelBackground": True,
        "markerSetId": "players",
        "markerSet": {
            "id": "players",
            "label": "Players",
            "toggleable": True,
            "defaultHidden": False,
            "priority": 1000
        }
    }
    
    with open(players_file, 'w', encoding='utf-8') as f:
        json.dump(players_data, f, indent=2)
    print(f"Created players.json at: {players_file}")

def update_plugin_config():
    """Update plugin configuration to enable player markers"""
    config_content = """# BlueMap Plugin Configuration

# The interval in seconds in which the players will be updated to the map
# Default is 1
player-update-interval: 1

# If enabled, shows player markers on the map
# Default is true
live-player-markers: true

# If enabled, hides invisible/vanished players from the map
# Default is true
hide-invisible: true

# If enabled, hides sneaking players from the map
# Default is false
hide-sneaking: false

# The interval in seconds in which players will be written to the map-storage.
# This is useful if you can't create a live-connection between the server and the webapp
# Default is 3
write-players-interval: 3

# Download the skin from mojang-servers when a player joins your server
# Default is true
skin-download: true
"""
    
    config_file = TEST_SERVER_DIR / "plugins" / "BlueMap" / "plugin.conf"
    config_file.parent.mkdir(parents=True, exist_ok=True)
    
    with open(config_file, 'w', encoding='utf-8') as f:
        f.write(config_content)
    print(f"Updated plugin configuration at: {config_file}")

if __name__ == "__main__":
    main() 