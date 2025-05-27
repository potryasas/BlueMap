import json

settings = {
    "port": 8100,
    "bind-address": "0.0.0.0",
    "default-world": "world",
    "single-world-mode": False,
    "useCookies": True,
    "defaultToFlatView": False,
    "resolutionDefault": 1.0,
    "minZoomDistance": 100.0,
    "maxZoomDistance": 100000.0,
    "hiresSliderMax": 500.0,
    "hiresSliderDefault": 100.0,
    "hiresSliderMin": 0.0,
    "lowresSliderMax": 7000.0,
    "lowresSliderDefault": 2000.0,
    "lowresSliderMin": 500.0,
    "mapDataRoot": "maps",
    "liveDataRoot": "maps"
}

settings_path = r"C:\Personal\TestServer\plugins\BlueMap\web\settings.json"
with open(settings_path, 'w') as f:
    json.dump(settings, f, indent=2) 