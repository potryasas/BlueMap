package de.bluecolored.bluemap.bukkit.legacy.render;

import org.bukkit.Material;
import java.util.Map;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.io.IOException;

public class ResourceModelRendererLegacy {
    private static final float BLOCK_SCALE = 1f / 16f;

    private static final float[] FACE_NORMALS = {
        0, -1, 0,  // Down (-Y)
        0, 1, 0,   // Up (+Y)
        0, 0, -1,  // North (-Z)
        0, 0, 1,   // South (+Z)
        -1, 0, 0,  // West (-X)
        1, 0, 0    // East (+X)
    };

    private final Map<String, TextureData> textureCache;
    private final Map<Material, float[]> colorMap;
    private final Map<Material, Boolean> transparencyMap;

    private TileModelViewLegacy blockModel;
    private float[] blockColor;
    private float blockColorOpacity;

    private BlockNeighborhoodLegacy neighborhood;

    public ResourceModelRendererLegacy() {
        this.textureCache = new HashMap<>();
        this.colorMap = new HashMap<>();
        this.transparencyMap = new HashMap<>();
    }

    public void loadTextureData(String resourcePath, float[] color, boolean halfTransparent, String base64Texture) {
        try {
            // Декодируем base64 PNG в BufferedImage
            String base64Image = base64Texture.split(",")[1];
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            BufferedImage texture = ImageIO.read(new ByteArrayInputStream(imageBytes));

            // Кэшируем текстуру и цвет
            TextureData textureData = new TextureData(texture, color, halfTransparent);
            textureCache.put(resourcePath, textureData);

            // Парсим Material из resourcePath (Material.XXX)
            String materialName = resourcePath.replace("Material.", "");
            Material material = Material.valueOf(materialName);

            colorMap.put(material, color);
            transparencyMap.put(material, halfTransparent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void render(BlockNeighborhoodLegacy neighborhood, TileModelViewLegacy blockModel) {
        this.neighborhood = neighborhood;
        this.blockModel = blockModel;

        Material material = neighborhood.getType();
        this.blockColor = colorMap.getOrDefault(material, new float[]{1.0f, 1.0f, 1.0f, 1.0f});
        this.blockColorOpacity = this.blockColor[3];

        TextureData textureData = textureCache.get("Material." + material.name());
        if (textureData == null) {
            renderDefaultBlock();
            return;
        }

        renderTexturedBlock(textureData);
    }

    private void renderDefaultBlock() {
        // Создаем грани куба с дефолтным цветом
        float[] vertices = new float[] {
            // Down face
            0, 0, 0,  1, 0, 0,  1, 0, 1,  0, 0, 1,
            // Up face
            0, 1, 0,  0, 1, 1,  1, 1, 1,  1, 1, 0,
            // North face
            0, 0, 0,  0, 1, 0,  1, 1, 0,  1, 0, 0,
            // South face
            0, 0, 1,  1, 0, 1,  1, 1, 1,  0, 1, 1,
            // West face
            0, 0, 0,  0, 0, 1,  0, 1, 1,  0, 1, 0,
            // East face
            1, 0, 0,  1, 1, 0,  1, 1, 1,  1, 0, 1
        };

        float[] uvs = new float[] {
            // Default UVs for each face
            0, 0,  1, 0,  1, 1,  0, 1,
            0, 0,  1, 0,  1, 1,  0, 1,
            0, 0,  1, 0,  1, 1,  0, 1,
            0, 0,  1, 0,  1, 1,  0, 1,
            0, 0,  1, 0,  1, 1,  0, 1,
            0, 0,  1, 0,  1, 1,  0, 1
        };

        // Добавляем все грани в модель
        for (int face = 0; face < 6; face++) {
            int vOffset = face * 12;
            int uvOffset = face * 8;
            
            addFace(
                new float[] {
                    vertices[vOffset],     vertices[vOffset + 1],  vertices[vOffset + 2],
                    vertices[vOffset + 3], vertices[vOffset + 4],  vertices[vOffset + 5],
                    vertices[vOffset + 6], vertices[vOffset + 7],  vertices[vOffset + 8],
                    vertices[vOffset + 9], vertices[vOffset + 10], vertices[vOffset + 11]
                },
                new float[] {
                    uvs[uvOffset],     uvs[uvOffset + 1],
                    uvs[uvOffset + 2], uvs[uvOffset + 3],
                    uvs[uvOffset + 4], uvs[uvOffset + 5],
                    uvs[uvOffset + 6], uvs[uvOffset + 7]
                },
                blockColor,
                1.0f // AO
            );
        }

        // Масштабируем до размера блока
        blockModel.scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE);
    }

    private void renderTexturedBlock(TextureData textureData) {
        // Получаем размеры текстуры
        int textureWidth = textureData.texture.getWidth();
        int textureHeight = textureData.texture.getHeight();

        // UV координаты для каждой грани (нормализованные)
        float[] faceUVs = new float[] {
            0.0f, 0.0f,                           // top-left
            1.0f, 0.0f,                           // top-right
            1.0f, 1.0f,                           // bottom-right
            0.0f, 1.0f                            // bottom-left
        };

        // Создаем все 6 граней куба
        float[][] faceVertices = new float[][] {
            // Down (-Y)
            {0, 0, 0,  1, 0, 0,  1, 0, 1,  0, 0, 1},
            // Up (+Y)
            {0, 1, 0,  0, 1, 1,  1, 1, 1,  1, 1, 0},
            // North (-Z)
            {0, 0, 0,  0, 1, 0,  1, 1, 0,  1, 0, 0},
            // South (+Z)
            {0, 0, 1,  1, 0, 1,  1, 1, 1,  0, 1, 1},
            // West (-X)
            {0, 0, 0,  0, 0, 1,  0, 1, 1,  0, 1, 0},
            // East (+X)
            {1, 0, 0,  1, 1, 0,  1, 1, 1,  1, 0, 1}
        };

        // Для каждой грани
        for (float[] vertices : faceVertices) {
            // Вычисляем ambient occlusion для грани
            float ao = calculateAO(vertices);
            
            // Добавляем грань в модель
            addFace(vertices, faceUVs, textureData.color, ao);
        }

        // Применяем трансформации
        blockModel.scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE);
    }

    private void addFace(float[] vertices, float[] uvs, float[] color, float ao) {
        // Добавляем вершины в TileModelView
        blockModel.addVertex(vertices[0], vertices[1], vertices[2], uvs[0], uvs[1], color[0], color[1], color[2], color[3], ao);
        blockModel.addVertex(vertices[3], vertices[4], vertices[5], uvs[2], uvs[3], color[0], color[1], color[2], color[3], ao);
        blockModel.addVertex(vertices[6], vertices[7], vertices[8], uvs[4], uvs[5], color[0], color[1], color[2], color[3], ao);
        blockModel.addVertex(vertices[9], vertices[10], vertices[11], uvs[6], uvs[7], color[0], color[1], color[2], color[3], ao);
    }

    private float calculateAO(float[] vertices) {
        // Определяем нормаль грани по первым трем вершинам
        float[] normal = calculateNormal(vertices);
        
        // Находим индекс ближайшей стандартной нормали
        int faceIndex = findClosestNormal(normal);
        
        // Получаем координаты блока
        int x = Math.round(vertices[0]);
        int y = Math.round(vertices[1]);
        int z = Math.round(vertices[2]);

        // Проверяем соседние блоки в зависимости от направления грани
        float ao = 1.0f;
        switch (faceIndex) {
            case 0: // Down (-Y)
                ao = getAOForNeighbors(x, y-1, z);
                break;
            case 1: // Up (+Y)
                ao = getAOForNeighbors(x, y+1, z);
                break;
            case 2: // North (-Z)
                ao = getAOForNeighbors(x, y, z-1);
                break;
            case 3: // South (+Z)
                ao = getAOForNeighbors(x, y, z+1);
                break;
            case 4: // West (-X)
                ao = getAOForNeighbors(x-1, y, z);
                break;
            case 5: // East (+X)
                ao = getAOForNeighbors(x+1, y, z);
                break;
        }

        return ao;
    }

    private float[] calculateNormal(float[] vertices) {
        // Вычисляем нормаль по первым трем вершинам
        float[] v1 = new float[]{
            vertices[3] - vertices[0],
            vertices[4] - vertices[1],
            vertices[5] - vertices[2]
        };
        float[] v2 = new float[]{
            vertices[6] - vertices[0],
            vertices[7] - vertices[1],
            vertices[8] - vertices[2]
        };

        // Векторное произведение
        float[] normal = new float[]{
            v1[1] * v2[2] - v1[2] * v2[1],
            v1[2] * v2[0] - v1[0] * v2[2],
            v1[0] * v2[1] - v1[1] * v2[0]
        };

        // Нормализация
        float length = (float) Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
        if (length > 0) {
            normal[0] /= length;
            normal[1] /= length;
            normal[2] /= length;
        }

        return normal;
    }

    private int findClosestNormal(float[] normal) {
        float maxDot = -1;
        int bestIndex = 0;

        for (int i = 0; i < 6; i++) {
            float dot = normal[0] * FACE_NORMALS[i*3] + 
                       normal[1] * FACE_NORMALS[i*3+1] + 
                       normal[2] * FACE_NORMALS[i*3+2];
            if (dot > maxDot) {
                maxDot = dot;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    private float getAOForNeighbors(int x, int y, int z) {
        // Получаем соседние блоки
        BlockNeighborhoodLegacy neighbor = neighborhood.getRelative(x, y, z);
        
        // Базовое значение AO
        float ao = 1.0f;

        // Если блок непрозрачный - уменьшаем AO
        if (neighbor.isOpaque()) {
            ao *= 0.8f;
        }

        // Учитываем уровень освещения
        float lightLevel = Math.max(neighbor.getBlockLight(), neighbor.getSkyLight()) / 15.0f;
        ao *= 0.2f + (lightLevel * 0.8f);

        // Учитываем воду и лаву
        if (neighbor.isWater()) {
            ao *= 0.8f;
        } else if (neighbor.isLava()) {
            ao *= 0.6f;
        }

        return ao;
    }

    private static class TextureData {
        final BufferedImage texture;
        final float[] color;
        final boolean halfTransparent;

        TextureData(BufferedImage texture, float[] color, boolean halfTransparent) {
            this.texture = texture;
            this.color = color;
            this.halfTransparent = halfTransparent;
        }
    }
} 