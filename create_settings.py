import json
import os

settings = {
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

settings_path = r"C:\Personal\TestServer\plugins\BlueMap\web\settings.json"
with open(settings_path, 'w') as f:
    json.dump(settings, f, indent=2) 