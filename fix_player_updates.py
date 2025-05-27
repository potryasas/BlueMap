import json
import os
import logging
import time
from pathlib import Path
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

class PlayerFileHandler(FileSystemEventHandler):
    def __init__(self, bluemap_dir):
        self.bluemap_dir = Path(bluemap_dir)
        self.maps_dir = self.bluemap_dir / "web" / "maps"
        self.dimensions = {
            "world": "overworld",
            "world_nether": "nether", 
            "world_the_end": "end"
        }
        
    def on_modified(self, event):
        if not event.is_directory and event.src_path.endswith("players.json"):
            self.sync_players_across_dimensions()

    def sync_players_across_dimensions(self):
        # Get all players from all dimensions
        all_players = {}
        for world_name, dimension in self.dimensions.items():
            world_dir = self.maps_dir / world_name / "live"
            if not world_dir.exists():
                world_dir.mkdir(parents=True)
                
            players_file = world_dir / "players.json"
            if not players_file.exists():
                self.create_empty_players_file(players_file)
                continue
                
            try:
                with open(players_file) as f:
                    data = json.load(f)
                    if "players" in data:
                        for player in data["players"]:
                            player_uuid = player.get("uuid")
                            if player_uuid:
                                if player_uuid not in all_players:
                                    all_players[player_uuid] = {
                                        "data": player,
                                        "dimension": dimension
                                    }
            except Exception as e:
                logging.error(f"Error reading players from {world_name}: {e}")

        # Update players.json in each dimension
        for world_name, dimension in self.dimensions.items():
            world_dir = self.maps_dir / world_name / "live"
            players_file = world_dir / "players.json"
            
            players_data = {
                "players": [],
                "updateInterval": 1000,
                "showPlayerMarkers": True,
                "showPlayerBody": True,
                "showPlayerHead": True,
                "showLabelBackground": True,
                "markerSetId": "players"
            }
            
            # Add marker set info
            players_data["markerSet"] = {
                "id": "players",
                "label": "Players",
                "toggleable": True,
                "defaultHidden": False,
                "priority": 1000
            }

            # Add players with correct foreign flag
            for player_info in all_players.values():
                player_data = player_info["data"].copy()
                player_data["foreign"] = player_info["dimension"] != dimension
                players_data["players"].append(player_data)

            try:
                with open(players_file, 'w') as f:
                    json.dump(players_data, f, indent=2)
                logging.info(f"Updated players.json for {world_name}")
            except Exception as e:
                logging.error(f"Error writing players.json for {world_name}: {e}")

    def create_empty_players_file(self, file_path):
        empty_data = {
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
        try:
            with open(file_path, 'w') as f:
                json.dump(empty_data, f, indent=2)
            logging.info(f"Created empty players.json at {file_path}")
        except Exception as e:
            logging.error(f"Error creating empty players.json: {e}")

def main():
    bluemap_dir = os.path.dirname(os.path.abspath(__file__))
    event_handler = PlayerFileHandler(bluemap_dir)
    observer = Observer()
    observer.schedule(event_handler, str(Path(bluemap_dir) / "web" / "maps"), recursive=True)
    observer.start()
    
    logging.info("Starting player file monitor")
    
    # Initial sync
    event_handler.sync_players_across_dimensions()
    
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()

if __name__ == "__main__":
    main() 