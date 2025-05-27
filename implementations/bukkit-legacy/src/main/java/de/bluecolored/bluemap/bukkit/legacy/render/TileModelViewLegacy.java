package de.bluecolored.bluemap.bukkit.legacy.render;

public class TileModelViewLegacy {
    private final ArrayTileModelLegacy model;
    private final float[] transformMatrix;
    private final float[] tempVertex;

    public TileModelViewLegacy(ArrayTileModelLegacy model) {
        this.model = model;
        this.transformMatrix = new float[16];
        this.tempVertex = new float[3];
        
        // Инициализируем единичную матрицу
        transformMatrix[0] = 1.0f;
        transformMatrix[5] = 1.0f;
        transformMatrix[10] = 1.0f;
        transformMatrix[15] = 1.0f;
    }

    public TileModelViewLegacy initialize() {
        // Сбрасываем трансформацию в единичную матрицу
        transformMatrix[0] = 1.0f;  transformMatrix[4] = 0.0f;  transformMatrix[8] = 0.0f;   transformMatrix[12] = 0.0f;
        transformMatrix[1] = 0.0f;  transformMatrix[5] = 1.0f;  transformMatrix[9] = 0.0f;   transformMatrix[13] = 0.0f;
        transformMatrix[2] = 0.0f;  transformMatrix[6] = 0.0f;  transformMatrix[10] = 1.0f;  transformMatrix[14] = 0.0f;
        transformMatrix[3] = 0.0f;  transformMatrix[7] = 0.0f;  transformMatrix[11] = 0.0f;  transformMatrix[15] = 1.0f;
        return this;
    }

    public void scale(float sx, float sy, float sz) {
        // Умножаем матрицу на матрицу масштабирования
        transformMatrix[0] *= sx;
        transformMatrix[5] *= sy;
        transformMatrix[10] *= sz;
    }

    public void translate(float dx, float dy, float dz) {
        // Умножаем матрицу на матрицу трансляции
        transformMatrix[12] += dx;
        transformMatrix[13] += dy;
        transformMatrix[14] += dz;
    }

    public void addVertex(float x, float y, float z, float u, float v, float r, float g, float b, float a, float ao) {
        // Применяем трансформацию к вершине
        tempVertex[0] = x;
        tempVertex[1] = y;
        tempVertex[2] = z;
        transformVertex(tempVertex);

        // Добавляем трансформированную вершину в модель
        model.addVertex(
            tempVertex[0], tempVertex[1], tempVertex[2],  // position
            u, v,                                         // uv
            r, g, b, a,                                  // color
            ao                                           // ambient occlusion
        );
    }

    private void transformVertex(float[] vertex) {
        float x = vertex[0];
        float y = vertex[1];
        float z = vertex[2];

        vertex[0] = transformMatrix[0] * x + transformMatrix[4] * y + transformMatrix[8] * z + transformMatrix[12];
        vertex[1] = transformMatrix[1] * x + transformMatrix[5] * y + transformMatrix[9] * z + transformMatrix[13];
        vertex[2] = transformMatrix[2] * x + transformMatrix[6] * y + transformMatrix[10] * z + transformMatrix[14];
    }

    public int getStart() {
        return model.getVertexCount();
    }

    public void initialize(int start) {
        // Можно использовать для оптимизации буфера вершин
        // TODO: Реализовать если потребуется
    }
} 