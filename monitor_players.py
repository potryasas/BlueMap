import json
import os
from pathlib import Path
import time
from datetime import datetime

# Define paths
TEST_SERVER_DIR = Path(r"c:\Personal\TestServer")
MAPS_DIR = TEST_SERVER_DIR / "web" / "maps"
WORLD_DIR = MAPS_DIR / "world" / "live"
players_file = WORLD_DIR / "players.json"

def format_position(pos):
    if not pos:
        return "Unknown"
    return f"x:{pos.get('x', '?'):.1f}, y:{pos.get('y', '?'):.1f}, z:{pos.get('z', '?'):.1f}"

def format_rotation(rot):
    if not rot:
        return ""
    return f" (pitch:{rot.get('pitch', '?'):.1f}, yaw:{rot.get('yaw', '?'):.1f})"

print(f"Monitoring players.json for changes at: {players_file}")
print("Press Ctrl+C to stop")
print("\nWaiting for player updates...")

last_content = None
last_error_time = 0
ERROR_COOLDOWN = 5  # seconds between error messages

while True:
    try:
        if not players_file.exists():
            current_time = time.time()
            if current_time - last_error_time >= ERROR_COOLDOWN:
                print(f"\nWarning: Players file not found at {players_file}")
                print("Make sure the server is running and BlueMap is configured correctly")
                last_error_time = current_time
            time.sleep(1)
            continue

        with open(players_file, 'r') as f:
            content = f.read()
            if content != last_content:
                data = json.loads(content)
                now = datetime.now().strftime("%H:%M:%S")
                print(f"\nFile updated at: {now}")
                
                player_count = len(data.get("players", []))
                print(f"Players: {player_count}")
                
                if data.get("players"):
                    for player in data["players"]:
                        name = player.get('name', 'Unknown')
                        pos = format_position(player.get('position'))
                        rot = format_rotation(player.get('rotation'))
                        foreign = " (different world)" if player.get('foreign') else ""
                        print(f"- {name}: {pos}{rot}{foreign}")
                
                last_content = content
        
        time.sleep(1)
        
    except json.JSONDecodeError:
        print("\nWarning: Invalid JSON in players file")
        time.sleep(1)
    except Exception as e:
        print(f"\nError reading file: {e}")
        time.sleep(1) 