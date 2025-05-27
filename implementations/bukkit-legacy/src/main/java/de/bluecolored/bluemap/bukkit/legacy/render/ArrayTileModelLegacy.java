package de.bluecolored.bluemap.bukkit.legacy.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Holds 3D model data for a single tile, adapted for legacy needs.
 * Each face is a triangle. We store attributes per-face.
 * Modern BlueMap uses attributes per-vertex within a face, this is slightly simplified.
 */
public class ArrayTileModelLegacy {

    // Each "face" here is a triangle. A block cube face is made of two such triangles.
    // Attributes per triangle:
    // Position: 3 vertices * 3 coords (x,y,z) = 9 floats
    // UV: 3 vertices * 2 coords (u,v) = 6 floats
    // MaterialIndex: 1 int (textureId from textures.json)
    // Color: 3 vertices * 3 components (r,g,b) = 9 floats (optional, can be white if texture handles color)
    // AO: 3 vertices * 1 component = 3 floats (optional)
    // Light: 1 sunlight byte, 1 blocklight byte (per triangle)

    private final List<Float> positions = new ArrayList<>();
    private final List<Float> uvs = new ArrayList<>();
    private final List<Integer> materialIndices = new ArrayList<>();
    private final List<Float> colors = new ArrayList<>();
    private final List<Float> aos = new ArrayList<>();
    private final List<Byte> sunlight = new ArrayList<>();
    private final List<Byte> blocklight = new ArrayList<>();
    private int faceCount = 0;

    public ArrayTileModelLegacy() {
        this.faceCount = 0;
    }

    public void addFace(
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            int materialIndex,
            float r, float g, float b,
            float ao1, float ao2, float ao3,
            byte sunLightValue, byte blockLightValue
    ) {
        // Positions
        positions.add(x1); positions.add(y1); positions.add(z1);
        positions.add(x2); positions.add(y2); positions.add(z2);
        positions.add(x3); positions.add(y3); positions.add(z3);
        // UVs
        uvs.add(u1); uvs.add(v1);
        uvs.add(u2); uvs.add(v2);
        uvs.add(u3); uvs.add(v3);
        // Material Index
        materialIndices.add(materialIndex);
        // Colors (repeated for 3 vertices)
        for (int i = 0; i < 3; i++) {
            colors.add(r); colors.add(g); colors.add(b);
        }
        // AO
        aos.add(ao1); aos.add(ao2); aos.add(ao3);
        // Light
        sunlight.add(sunLightValue);
        blocklight.add(blockLightValue);
        faceCount++;
    }

    public void clear() {
        positions.clear();
        uvs.clear();
        materialIndices.clear();
        colors.clear();
        aos.clear();
        sunlight.clear();
        blocklight.clear();
        faceCount = 0;
    }

    public int getFaceCount() {
        return faceCount;
    }

    public float[] getPositionsArray() {
        return toFloatArray(positions);
    }

    public float[] getUvsArray() {
        return toFloatArray(uvs);
    }
    
    public int[] getMaterialIndicesArray() { // Used for sorting/grouping if needed
        return materialIndices.stream().mapToInt(Integer::intValue).toArray();
    }

    public float[] getColorsArray() {
        return toFloatArray(colors);
    }
    
    public float[] getAosArray() {
        return toFloatArray(aos);
    }

    public byte[] getSunlightArray() {
        return toByteArray(sunlight);
    }
    
    public byte[] getBlocklightArray() {
        return toByteArray(blocklight);
    }

    // Helper to convert List<Float> to float[]
    private float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    // Helper to convert List<Byte> to byte[]
    private byte[] toByteArray(List<Byte> list) {
        byte[] array = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
    
    /**
     * Groups faces by material index.
     * Returns a list of MaterialGroup objects.
     * This is important for PRBMWriter.
     */
    public static class MaterialGroup {
        public int materialIndex;
        public int startFaceIndex; // inclusive
        public int faceCount;

        public MaterialGroup(int materialIndex, int startFaceIndex, int faceCount) {
            this.materialIndex = materialIndex;
            this.startFaceIndex = startFaceIndex;
            this.faceCount = faceCount;
        }
    }

    public List<MaterialGroup> getMaterialGroups() {
        List<MaterialGroup> groups = new ArrayList<>();
        if (faceCount == 0) {
            return groups;
        }

        // This simple version assumes faces are added somewhat grouped by material.
        // A more robust version would sort all faces by materialIndex first,
        // then group them. For now, we iterate and create groups as material changes.
        
        int currentMaterial = materialIndices.get(0);
        int currentGroupStartFace = 0;
        int currentGroupFaceCount = 0;

        for (int i = 0; i < faceCount; i++) {
            if (materialIndices.get(i) == currentMaterial) {
                currentGroupFaceCount++;
            } else {
                groups.add(new MaterialGroup(currentMaterial, currentGroupStartFace, currentGroupFaceCount));
                currentMaterial = materialIndices.get(i);
                currentGroupStartFace = i;
                currentGroupFaceCount = 1;
            }
        }
        // Add the last group
        groups.add(new MaterialGroup(currentMaterial, currentGroupStartFace, currentGroupFaceCount));
        
        return groups;
    }

    public void addVertex(float x, float y, float z, float u, float v, float r, float g, float b, float a, float ao) {
        // Позиция (x, y, z)
        positions.add(x);
        positions.add(y);
        positions.add(z);

        // UV координаты (u, v)
        uvs.add(u);
        uvs.add(v);

        // Цвет (r, g, b, a)
        colors.add(r);
        colors.add(g);
        colors.add(b);
        colors.add(a);

        // Ambient Occlusion
        aos.add(ao);

        faceCount++;
    }

    public int getVertexCount() {
        return faceCount;
    }

    // Геттеры для доступа к данным
    public float[] getPositions() {
        float[] result = new float[positions.size()];
        for (int i = 0; i < positions.size(); i++) {
            result[i] = positions.get(i);
        }
        return result;
    }

    public float[] getUVs() {
        float[] result = new float[uvs.size()];
        for (int i = 0; i < uvs.size(); i++) {
            result[i] = uvs.get(i);
        }
        return result;
    }

    public float[] getColors() {
        float[] result = new float[colors.size()];
        for (int i = 0; i < colors.size(); i++) {
            result[i] = colors.get(i);
        }
        return result;
    }

    public float[] getAOs() {
        float[] result = new float[aos.size()];
        for (int i = 0; i < aos.size(); i++) {
            result[i] = aos.get(i);
        }
        return result;
    }

    // Методы для прямого доступа к спискам (если нужно)
    public List<Float> getPositionsList() {
        return positions;
    }

    public List<Float> getUVsList() {
        return uvs;
    }

    public List<Float> getColorsList() {
        return colors;
    }

    public List<Float> getAOsList() {
        return aos;
    }
} 