# Minecraft World Renderer

A web-based 3D renderer for Minecraft worlds using Three.js.

## Features

- 3D rendering of Minecraft blocks
- Texture atlas generation from block textures
- First-person camera controls
- Chunk-based rendering system
- Real-time world loading

## Prerequisites

- Node.js (v14 or higher)
- NPM (v6 or higher)
- Minecraft texture pack (default or custom)

## Setup

1. Clone the repository
2. Navigate to the renderer directory
3. Install dependencies:
```bash
npm install
```

4. Create a `textures/blocks` directory and copy your Minecraft block textures there:
```bash
mkdir -p src/textures/blocks
# Copy your .png texture files to src/textures/blocks/
```

5. Build the project:
```bash
npm run build
```

## Running the Application

1. Start the server:
```bash
npm start
```

2. Open your browser and navigate to `http://localhost:3000`

## Controls

- WASD - Move around
- Mouse - Look around
- Space - Move up
- Shift - Move down
- Click - Lock/unlock mouse pointer

## Development

To run in development mode with hot reloading:
```bash
npm run dev
```

## Project Structure

- `src/server/` - Server-side code
  - `index.ts` - Express server and API endpoints
  - `TextureAtlasManager.ts` - Texture atlas generation
  - `ChunkRenderer.ts` - 3D mesh generation
- `src/client/` - Client-side code
  - `index.html` - Main HTML file
  - `main.ts` - Three.js application
- `src/textures/` - Texture files
  - `blocks/` - Individual block textures
  - `atlas.png` - Generated texture atlas

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

MIT License 