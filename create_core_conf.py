# This is the core configuration file of BlueMap.
# All settings are optional and will fall back to their defaults if not specified.

# The folder where bluemap saves rendered map-data and textures
data {
    # The path to the data-folder
    # Default is "bluemap"
    path = "bluemap"
}

# Settings for the integrated web-server
web {
    # Whether the web-server should be enabled
    # Default is true
    enabled = true
    
    # The port that the web-server listens on
    # Default is 8100
    port = 8100
    
    # The ip-address that the web-server binds to
    # Use "0.0.0.0" to bind to all addresses
    # Default is "0.0.0.0"
    bind-address = "0.0.0.0"
    
    # The folder where bluemap saves/expects the web-app files
    # Default is "web"
    webroot = "web"
    
    # Whether bluemap should accept connections from the internet
    # Default is false
    accept-internet = false
}

# Settings for the maps that BlueMap renders
maps {
    # The maps that BlueMap renders
    # Each map represents one dimension/world
    world {
        # The id of this map
        # Default is the folder-name of the world
        id = "world"
        
        # The name of this map that is displayed to the user
        # Default is the id of this map
        name = "world"
        
        # The path to the save-folder of the world that this map renders
        # Default is the current world-folder
        world = "world"
        
        # The position on the map where the camera is centered
        # Default is x:0, z:0
        start-pos {
            x = 0
            z = 0
        }
    }
    
    world_nether {
        id = "world_nether"
        name = "world_nether"
        world = "world_nether"
        start-pos {
            x = 0
            z = 0
        }
    }
    
    world_the_end {
        id = "world_the_end"
        name = "world_the_end"
        world = "world_the_end"
        start-pos {
            x = 0
            z = 0
        }
    }
} 