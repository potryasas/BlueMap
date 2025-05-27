import json
import os
import shutil

# Base directory
base_dir = r"C:\Personal\TestServer\plugins\BlueMap\web"
maps_dir = os.path.join(base_dir, "maps")

# World directories
worlds = ["world", "world_nether", "world_the_end"]

# Settings for each world
world_settings = {
    "world": {
        "name": "world",
        "skyColor": "#7dabff",
        "ambientLight": 0.1
    },
    "world_nether": {
        "name": "world_nether",
        "skyColor": "#300000",
        "ambientLight": 0.2
    },
    "world_the_end": {
        "name": "world_the_end",
        "skyColor": "#000000",
        "ambientLight": 0.1
    }
}

# Common settings for all worlds
common_settings = {
    "equirectangular": False,
    "renderEdges": True,
    "renderAll": False,
    "hires": {
        "enabled": True,
        "save": True
    },
    "lowres": {
        "enabled": True,
        "save": True
    }
}

# Web settings
web_settings = {
    "port": 8100,
    "bind-address": "0.0.0.0",
    "webroot": "web",
    "map-data-root": "maps",
    "live-data-root": "maps",
    "default-world": "world",
    "single-world-mode": False,
    "useCookies": True,
    "defaultToFlatView": False
}

# Create directories and files
for world in worlds:
    world_dir = os.path.join(maps_dir, world)
    live_dir = os.path.join(world_dir, "live")
    
    # Create directories
    os.makedirs(live_dir, exist_ok=True)
    
    # Create world settings
    settings = {**world_settings[world], **common_settings}
    with open(os.path.join(world_dir, "settings.json"), "w") as f:
        json.dump(settings, f, indent=2)
    
    # Create empty markers.json
    with open(os.path.join(live_dir, "markers.json"), "w") as f:
        json.dump({"sets": {}}, f, indent=2)
    
    # Create empty players.json
    with open(os.path.join(live_dir, "players.json"), "w") as f:
        json.dump({"players": []}, f, indent=2)
    
    # Copy textures.json if it exists in the source directory
    source_textures = os.path.join(os.path.dirname(os.path.abspath(__file__)), "textures.json")
    if os.path.exists(source_textures):
        shutil.copy2(source_textures, os.path.join(world_dir, "textures.json"))

# Create web settings
with open(os.path.join(base_dir, "settings.json"), "w") as f:
    json.dump(web_settings, f, indent=2)

print("All files have been created successfully!") 