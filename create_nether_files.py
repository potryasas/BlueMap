import json
import os

# Create directories
nether_live_dir = r"C:\Personal\TestServer\plugins\BlueMap\web\maps\world_nether\live"
os.makedirs(nether_live_dir, exist_ok=True)

# Create markers.json
markers = {"sets": {}}
with open(os.path.join(nether_live_dir, "markers.json"), "w") as f:
    json.dump(markers, f)

# Create players.json
players = {"players": []}
with open(os.path.join(nether_live_dir, "players.json"), "w") as f:
    json.dump(players, f) 