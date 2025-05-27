import json
import os
import logging
from pathlib import Path
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

class MarkerFileHandler(FileSystemEventHandler):
    def __init__(self, bluemap_dir):
        self.bluemap_dir = Path(bluemap_dir)
        self.maps_dir = self.bluemap_dir / "web" / "maps"
        self.dimensions = {
            "world": "overworld",
            "world_nether": "nether", 
            "world_the_end": "end"
        }
        
        # Load marker template
        self.marker_template = self.load_marker_template()
        
    def load_marker_template(self):
        template_file = self.bluemap_dir / "marker_template.json"
        if template_file.exists():
            try:
                with open(template_file) as f:
                    return json.load(f)
            except Exception as e:
                logging.error(f"Error loading marker template: {e}")
        
        # Return default template if file doesn't exist
        return {
            "official-markers": {
                "label": "Official Markers",
                "toggleable": True,
                "defaultHidden": False,
                "sorting": 0,
                "markers": {}
            },
            "tier-1-player-markers": {
                "label": "Tier 1 Player Markers",
                "toggleable": True,
                "defaultHidden": False,
                "sorting": 0,
                "markers": {}
            },
            "tier-2-player-markers": {
                "label": "Tier 2 Player Markers",
                "toggleable": True,
                "defaultHidden": False,
                "sorting": 0,
                "markers": {}
            }
        }
        
    def on_modified(self, event):
        if not event.is_directory and event.src_path.endswith("markers.json"):
            self.sync_markers_across_dimensions()

    def sync_markers_across_dimensions(self):
        # Get all markers from all dimensions
        all_markers = {}
        for world_name, dimension in self.dimensions.items():
            world_dir = self.maps_dir / world_name / "live"
            if not world_dir.exists():
                world_dir.mkdir(parents=True)
                
            markers_file = world_dir / "markers.json"
            if not markers_file.exists():
                self.create_empty_markers_file(markers_file)
                continue
                
            try:
                with open(markers_file) as f:
                    data = json.load(f)
                    for category, category_data in data.items():
                        if category not in all_markers:
                            all_markers[category] = {
                                "data": category_data,
                                "markers": {}
                            }
                        
                        if "markers" in category_data:
                            for marker_id, marker in category_data["markers"].items():
                                if marker_id not in all_markers[category]["markers"]:
                                    marker["dimension"] = dimension
                                    all_markers[category]["markers"][marker_id] = marker
            except Exception as e:
                logging.error(f"Error reading markers from {world_name}: {e}")

        # Update markers.json in each dimension
        for world_name, dimension in self.dimensions.items():
            world_dir = self.maps_dir / world_name / "live"
            markers_file = world_dir / "markers.json"
            
            markers_data = {}
            
            # Add categories and their markers
            for category, category_info in all_markers.items():
                category_data = category_info["data"].copy()
                category_data["markers"] = {}
                
                # Add markers that belong to this dimension
                for marker_id, marker in category_info["markers"].items():
                    if marker.get("dimension") == dimension:
                        marker_copy = marker.copy()
                        if "dimension" in marker_copy:
                            del marker_copy["dimension"]
                        category_data["markers"][marker_id] = marker_copy
                
                markers_data[category] = category_data

            try:
                with open(markers_file, 'w') as f:
                    json.dump(markers_data, f, indent=2)
                logging.info(f"Updated markers.json for {world_name}")
            except Exception as e:
                logging.error(f"Error writing markers.json for {world_name}: {e}")

    def create_empty_markers_file(self, file_path):
        try:
            with open(file_path, 'w') as f:
                json.dump(self.marker_template, f, indent=2)
            logging.info(f"Created empty markers.json at {file_path}")
        except Exception as e:
            logging.error(f"Error creating empty markers.json: {e}")

def main():
    bluemap_dir = os.path.dirname(os.path.abspath(__file__))
    event_handler = MarkerFileHandler(bluemap_dir)
    observer = Observer()
    observer.schedule(event_handler, str(Path(bluemap_dir) / "web" / "maps"), recursive=True)
    observer.start()
    
    logging.info("Starting marker file monitor")
    
    # Initial sync
    event_handler.sync_markers_across_dimensions()
    
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
    observer.join()

if __name__ == "__main__":
    main() 