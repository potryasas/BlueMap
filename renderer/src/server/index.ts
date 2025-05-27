import express from 'express';
import path from 'path';
import { TextureAtlasManager } from './TextureAtlasManager';
import { ChunkRenderer } from './ChunkRenderer';

const app = express();
const port = process.env.PORT || 3000;

// Initialize managers
const textureManager = new TextureAtlasManager();
const chunkRenderer = new ChunkRenderer(textureManager);

// Serve static files from the client directory
app.use(express.static(path.join(__dirname, '../client')));

// API endpoints
app.get('/api/chunk/:x/:y/:z', async (req, res) => {
  try {
    const { x, y, z } = req.params;
    const chunkData = await chunkRenderer.renderChunk(
      parseInt(x),
      parseInt(y),
      parseInt(z)
    );
    res.json(chunkData);
  } catch (error) {
    res.status(500).json({ error: 'Failed to render chunk' });
  }
});

app.get('/api/textures/atlas', async (req, res) => {
  try {
    const atlas = await textureManager.getAtlas();
    res.json(atlas);
  } catch (error) {
    res.status(500).json({ error: 'Failed to get texture atlas' });
  }
});

app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
}); 