import sharp from 'sharp';
import path from 'path';
import fs from 'fs/promises';

interface TextureInfo {
  name: string;
  x: number;
  y: number;
  width: number;
  height: number;
}

export class TextureAtlasManager {
  private atlasPath: string;
  private texturesDir: string;
  private textureMap: Map<string, TextureInfo>;

  constructor() {
    this.atlasPath = path.join(__dirname, '../textures/atlas.png');
    this.texturesDir = path.join(__dirname, '../textures/blocks');
    this.textureMap = new Map();
  }

  async loadTextures(): Promise<void> {
    try {
      // Read all texture files from the blocks directory
      const files = await fs.readdir(this.texturesDir);
      const textures = await Promise.all(
        files
          .filter(file => file.endsWith('.png'))
          .map(async file => {
            const filePath = path.join(this.texturesDir, file);
            const metadata = await sharp(filePath).metadata();
            return {
              name: path.basename(file, '.png'),
              path: filePath,
              width: metadata.width || 0,
              height: metadata.height || 0
            };
          })
      );

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
        top: this.textureMap.get(texture.name)!.y,
        left: this.textureMap.get(texture.name)!.x
      }));

      await sharp({
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

    } catch (error) {
      console.error('Failed to create texture atlas:', error);
      throw error;
    }
  }

  async getAtlas(): Promise<{ atlas: string; textures: Record<string, TextureInfo> }> {
    // Ensure the atlas exists
    if (!await fs.access(this.atlasPath).then(() => true).catch(() => false)) {
      await this.loadTextures();
    }

    // Convert the texture map to a plain object
    const textures: Record<string, TextureInfo> = {};
    this.textureMap.forEach((info, name) => {
      textures[name] = info;
    });

    return {
      atlas: this.atlasPath,
      textures
    };
  }
} 