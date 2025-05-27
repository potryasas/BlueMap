import json
import shutil
import os

# Base paths
base_dir = r"C:\Personal\TestServer\plugins\BlueMap\web\maps"
source_textures = r"C:\Users\potryasas\Desktop\BlueMap\textures.json"

# World configurations
worlds = {
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

# Create settings and copy textures for each world
for world_id, world_config in worlds.items():
    world_dir = os.path.join(base_dir, world_id)
    os.makedirs(world_dir, exist_ok=True)
    
    # Create settings.json
    settings = {**world_config, **common_settings}
    settings_path = os.path.join(world_dir, "settings.json")
    with open(settings_path, 'w') as f:
        json.dump(settings, f, indent=2)
    
    # Copy textures.json
    if os.path.exists(source_textures):
        shutil.copy2(source_textures, os.path.join(world_dir, "textures.json"))

print("Files created successfully!") 