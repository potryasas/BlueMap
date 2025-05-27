import json
import os

settings = {
    "name": "world_nether",
    "skyColor": "#300000",
    "ambientLight": 0.2,
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

settings_path = r"C:\Personal\TestServer\plugins\BlueMap\web\maps\world_nether\settings.json"
with open(settings_path, 'w') as f:
    json.dump(settings, f, indent=2) 