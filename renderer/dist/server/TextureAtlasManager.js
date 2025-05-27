"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.TextureAtlasManager = void 0;
const sharp_1 = __importDefault(require("sharp"));
const path_1 = __importDefault(require("path"));
const promises_1 = __importDefault(require("fs/promises"));
class TextureAtlasManager {
    constructor() {
        this.atlasPath = path_1.default.join(__dirname, '../textures/atlas.png');
        this.texturesDir = path_1.default.join(__dirname, '../textures/blocks');
        this.textureMap = new Map();
    }
    async loadTextures() {
        try {
            // Read all texture files from the blocks directory
            const files = await promises_1.default.readdir(this.texturesDir);
            const textures = await Promise.all(files
                .filter(file => file.endsWith('.png'))
                .map(async (file) => {
                const filePath = path_1.default.join(this.texturesDir, file);
                const metadata = await (0, sharp_1.default)(filePath).metadata();
                return {
                    name: path_1.default.basename(file, '.png'),
                    path: filePath,
                    width: metadata.width || 0,
                    height: metadata.height || 0
                };
            }));
            // Calculate atlas dimensions
            const maxWidth = 2048; // Maximum texture atlas width
            let currentX = 0;
            let currentY = 0;
            let rowHeight = 0;
            // Create texture positions in the atlas
            textures.forEach(texture => {
                if (currentX + texture.width > maxWidth) {
                    currentX = 0;
                    currentY += rowHeight;
                    rowHeight = 0;
                }
                this.textureMap.set(texture.name, {
                    name: texture.name,
                    x: currentX,
                    y: currentY,
                    width: texture.width,
                    height: texture.height
                });
                currentX += texture.width;
                rowHeight = Math.max(rowHeight, texture.height);
            });
            // Create the actual atlas image
            const composite = textures.map(texture => ({
                input: texture.path,
                top: this.textureMap.get(texture.name).y,
                left: this.textureMap.get(texture.name).x
            }));
            await (0, sharp_1.default)({
                create: {
                    width: maxWidth,
                    height: currentY + rowHeight,
                    channels: 4,
                    background: { r: 0, g: 0, b: 0, alpha: 0 }
                }
            })
                .composite(composite)
                .png()
                .toFile(this.atlasPath);
        }
        catch (error) {
            console.error('Failed to create texture atlas:', error);
            throw error;
        }
    }
    async getAtlas() {
        // Ensure the atlas exists
        if (!await promises_1.default.access(this.atlasPath).then(() => true).catch(() => false)) {
            await this.loadTextures();
        }
        // Convert the texture map to a plain object
        const textures = {};
        this.textureMap.forEach((info, name) => {
            textures[name] = info;
        });
        return {
            atlas: this.atlasPath,
            textures
        };
    }
}
exports.TextureAtlasManager = TextureAtlasManager;
