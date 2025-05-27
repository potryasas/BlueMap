import { TextureAtlasManager } from './TextureAtlasManager';

interface BlockFace {
  vertices: number[];
  uvs: number[];
  normals: number[];
  indices: number[];
}

interface Block {
  id: string;
  x: number;
  y: number;
  z: number;
  faces: {
    top?: BlockFace;
    bottom?: BlockFace;
    front?: BlockFace;
    back?: BlockFace;
    left?: BlockFace;
    right?: BlockFace;
  };
}

export class ChunkRenderer {
  private textureManager: TextureAtlasManager;
  private readonly CHUNK_SIZE = 16;

  constructor(textureManager: TextureAtlasManager) {
    this.textureManager = textureManager;
  }

  private createBlockFace(
    x: number,
    y: number,
    z: number,
    direction: 'top' | 'bottom' | 'front' | 'back' | 'left' | 'right',
    textureUV: { x: number; y: number; width: number; height: number }
  ): BlockFace {
    // Define vertices for each face direction
    const vertices = this.getVerticesForFace(x, y, z, direction);
    const uvs = this.getUVsForFace(textureUV);
    const normals = this.getNormalsForFace(direction);
    const indices = [0, 1, 2, 2, 3, 0]; // Triangle indices for the face

    return { vertices, uvs, normals, indices };
  }

  private getVerticesForFace(
    x: number,
    y: number,
    z: number,
    direction: 'top' | 'bottom' | 'front' | 'back' | 'left' | 'right'
  ): number[] {
    switch (direction) {
      case 'top':
        return [
          x, y + 1, z,
          x + 1, y + 1, z,
          x + 1, y + 1, z + 1,
          x, y + 1, z + 1
        ];
      case 'bottom':
        return [
          x, y, z,
          x + 1, y, z,
          x + 1, y, z + 1,
          x, y, z + 1
        ];
      case 'front':
        return [
          x, y, z + 1,
          x + 1, y, z + 1,
          x + 1, y + 1, z + 1,
          x, y + 1, z + 1
        ];
      case 'back':
        return [
          x, y, z,
          x + 1, y, z,
          x + 1, y + 1, z,
          x, y + 1, z
        ];
      case 'left':
        return [
          x, y, z,
          x, y, z + 1,
          x, y + 1, z + 1,
          x, y + 1, z
        ];
      case 'right':
        return [
          x + 1, y, z,
          x + 1, y, z + 1,
          x + 1, y + 1, z + 1,
          x + 1, y + 1, z
        ];
    }
  }

  private getUVsForFace(texture: { x: number; y: number; width: number; height: number }): number[] {
    const { x, y, width, height } = texture;
    return [
      x, y,
      x + width, y,
      x + width, y + height,
      x, y + height
    ];
  }

  private getNormalsForFace(direction: 'top' | 'bottom' | 'front' | 'back' | 'left' | 'right'): number[] {
    switch (direction) {
      case 'top':
        return [0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0];
      case 'bottom':
        return [0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0];
      case 'front':
        return [0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1];
      case 'back':
        return [0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1];
      case 'left':
        return [-1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0];
      case 'right':
        return [1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0];
    }
  }

  async renderChunk(chunkX: number, chunkY: number, chunkZ: number): Promise<{
    vertices: number[];
    uvs: number[];
    normals: number[];
    indices: number[];
  }> {
    const atlas = await this.textureManager.getAtlas();
    
    // Create a test block in the middle of the chunk
    const blocks: Block[] = [{
      id: 'stone',
      x: 8,
      y: 8,
      z: 8,
      faces: {
        top: this.createBlockFace(8, 8, 8, 'top', atlas.textures['stone']),
        bottom: this.createBlockFace(8, 8, 8, 'bottom', atlas.textures['stone']),
        front: this.createBlockFace(8, 8, 8, 'front', atlas.textures['stone']),
        back: this.createBlockFace(8, 8, 8, 'back', atlas.textures['stone']),
        left: this.createBlockFace(8, 8, 8, 'left', atlas.textures['stone']),
        right: this.createBlockFace(8, 8, 8, 'right', atlas.textures['stone'])
      }
    }];

    // Combine all block faces into a single mesh
    const vertices: number[] = [];
    const uvs: number[] = [];
    const normals: number[] = [];
    const indices: number[] = [];
    let indexOffset = 0;

    blocks.forEach(block => {
      Object.entries(block.faces).forEach(([direction, face]) => {
        if (face) {
          vertices.push(...face.vertices);
          uvs.push(...face.uvs);
          normals.push(...face.normals);
          indices.push(...face.indices.map(i => i + indexOffset));
          indexOffset += 4; // Each face has 4 vertices
        }
      });
    });

    return { vertices, uvs, normals, indices };
  }
} 