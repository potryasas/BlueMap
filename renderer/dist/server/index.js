"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const path_1 = __importDefault(require("path"));
const TextureAtlasManager_1 = require("./TextureAtlasManager");
const ChunkRenderer_1 = require("./ChunkRenderer");
const app = (0, express_1.default)();
const port = process.env.PORT || 3000;
// Initialize managers
const textureManager = new TextureAtlasManager_1.TextureAtlasManager();
const chunkRenderer = new ChunkRenderer_1.ChunkRenderer(textureManager);
// Serve static files from the client directory
app.use(express_1.default.static(path_1.default.join(__dirname, '../client')));
// API endpoints
app.get('/api/chunk/:x/:y/:z', async (req, res) => {
    try {
        const { x, y, z } = req.params;
        const chunkData = await chunkRenderer.renderChunk(parseInt(x), parseInt(y), parseInt(z));
        res.json(chunkData);
    }
    catch (error) {
        res.status(500).json({ error: 'Failed to render chunk' });
    }
});
app.get('/api/textures/atlas', async (req, res) => {
    try {
        const atlas = await textureManager.getAtlas();
        res.json(atlas);
    }
    catch (error) {
        res.status(500).json({ error: 'Failed to get texture atlas' });
    }
});
app.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
});
