package de.bluecolored.bluemap.bukkit.legacy.render;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Writes an ArrayTileModelLegacy to an OutputStream in the PRBM (BlueMap Hires Model) format.
 * Adapted for legacy needs.
 */
public class PRBMWriterLegacy implements Closeable {

    private static final int FORMAT_VERSION = 1;
    // Header bits from modern BlueMap: indexed (no) _ indices-type (-) _ endianness (little) _ attribute-nr (7 for modern)
    // We will write 7 attributes: position, normal, color, uv, ao, blocklight, sunlight
    private static final int HEADER_BITS = 0b0_0_0_00111; 

    // Attribute types, normalization, cardinality, and encoding constants from modern BlueMap
    private static final int ATTRIBUTE_TYPE_FLOAT = 0;
    private static final int ATTRIBUTE_TYPE_INTEGER = 1 << 7;

    private static final int ATTRIBUTE_NOT_NORMALIZED = 0;
    private static final int ATTRIBUTE_NORMALIZED = 1 << 6;

    private static final int ATTRIBUTE_CARDINALITY_SCALAR = 0;
    private static final int ATTRIBUTE_CARDINALITY_2D_VEC = 1 << 4;
    private static final int ATTRIBUTE_CARDINALITY_3D_VEC = 2 << 4;
    // private static final int ATTRIBUTE_CARDINALITY_4D_VEC = 3 << 4; // Not used here

    // Attribute encodings (simplified for legacy, modern has more)
    private static final int ATTRIBUTE_ENCODING_SIGNED_32BIT_FLOAT = 1;
    private static final int ATTRIBUTE_ENCODING_UNSIGNED_8BIT_INT = 7; // For colors, ao, light
    private static final int ATTRIBUTE_ENCODING_SIGNED_8BIT_INT = 3; // For normals (as per modern BlueMap)

    private final DataOutputStream out;
    private long bytesWrittenSinceLastAttributeHeader; // To help with padding

    public PRBMWriterLegacy(OutputStream out) {
        this.out = new DataOutputStream(out);
        this.bytesWrittenSinceLastAttributeHeader = 0;
    }

    public void write(ArrayTileModelLegacy model) throws IOException {
        out.writeByte(FORMAT_VERSION);      // version - 1 byte
        out.writeByte(HEADER_BITS);         // format info - 1 byte
        
        int numVertices = model.getFaceCount() * 3;
        write3ByteInt(numVertices);         // number of vertices - 3 bytes
        write3ByteInt(0);                   // number of indices (0 for non-indexed) - 3 bytes

        // Order of attributes is important for the client
        writePositionArray(model.getPositionsArray());
        writeNormalArray(model.getPositionsArray()); // Normals are calculated from positions
        writeColorArray(model.getColorsArray());
        writeUvArray(model.getUvsArray());
        writeAoArray(model.getAosArray());
        writeBlocklightArray(model.getBlocklightArray());
        writeSunlightArray(model.getSunlightArray());

        writeMaterialGroups(model.getMaterialGroups());
    }

    private void beginAttribute(String name) throws IOException {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        out.write(nameBytes);
        out.writeByte(0); // Null terminator
        bytesWrittenSinceLastAttributeHeader = nameBytes.length + 1; // name + null terminator
    }

    private void finishAttribute() throws IOException {
        // Add the attribute descriptor byte itself to count
        bytesWrittenSinceLastAttributeHeader += 1; 
        int padding = (int) (bytesWrittenSinceLastAttributeHeader % 4);
        if (padding > 0) {
            padding = 4 - padding;
        }
        for (int i = 0; i < padding; i++) {
            out.writeByte(0);
        }
        bytesWrittenSinceLastAttributeHeader = 0; // Reset for next attribute or data block
    }

    private void writePositionArray(float[] positions) throws IOException {
        beginAttribute("position");
        out.writeByte(
            ATTRIBUTE_TYPE_FLOAT |
            ATTRIBUTE_NOT_NORMALIZED |
            ATTRIBUTE_CARDINALITY_3D_VEC |
            ATTRIBUTE_ENCODING_SIGNED_32BIT_FLOAT
        );
        finishAttribute();
        for (float val : positions) {
            out.writeFloat(val);
        }
    }

    private void writeNormalArray(float[] positions) throws IOException {
        beginAttribute("normal");
        out.writeByte(
            ATTRIBUTE_TYPE_FLOAT |
            ATTRIBUTE_NORMALIZED |
            ATTRIBUTE_CARDINALITY_3D_VEC |
            ATTRIBUTE_ENCODING_SIGNED_8BIT_INT
        );
        finishAttribute();

        // Calculate and write normals per vertex
        // Each face (triangle) has 3 vertices
        for (int i = 0; i < positions.length; i += 9) { // 9 floats per triangle (3 vertices * 3 coords)
            float p1x = positions[i];
            float p1y = positions[i+1];
            float p1z = positions[i+2];
            float p2x = positions[i+3];
            float p2y = positions[i+4];
            float p2z = positions[i+5];
            float p3x = positions[i+6];
            float p3y = positions[i+7];
            float p3z = positions[i+8];

            // Calculate normal for the triangle
            float u1 = p2x - p1x;
            float v1 = p2y - p1y;
            float w1 = p2z - p1z;
            float u2 = p3x - p1x;
            float v2 = p3y - p1y;
            float w2 = p3z - p1z;

            float nx = (v1 * w2) - (w1 * v2);
            float ny = (w1 * u2) - (u1 * w2);
            float nz = (u1 * v2) - (v1 * u2);

            float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (length > 0) {
                nx /= length;
                ny /= length;
                nz /= length;
            }

            // Write the same normal for all 3 vertices of the triangle
            for (int j = 0; j < 3; j++) {
                // Encode as signed byte: value * 127
                out.writeByte((byte) (Math.max(-1f, Math.min(1f, nx)) * 127f));
                out.writeByte((byte) (Math.max(-1f, Math.min(1f, ny)) * 127f));
                out.writeByte((byte) (Math.max(-1f, Math.min(1f, nz)) * 127f));
            }
        }
    }

    private void writeColorArray(float[] colors) throws IOException {
        beginAttribute("color");
        out.writeByte(
            ATTRIBUTE_TYPE_FLOAT |
            ATTRIBUTE_NORMALIZED |
            ATTRIBUTE_CARDINALITY_3D_VEC |
            ATTRIBUTE_ENCODING_UNSIGNED_8BIT_INT
        );
        finishAttribute();
        // Colors are already per-vertex (r,g,b, r,g,b, ...)
        for (float val : colors) {
            out.writeByte((int) (val * 255f));
        }
    }

    private void writeUvArray(float[] uvs) throws IOException {
        beginAttribute("uv");
        out.writeByte(
            ATTRIBUTE_TYPE_FLOAT |
            ATTRIBUTE_NOT_NORMALIZED |
            ATTRIBUTE_CARDINALITY_2D_VEC |
            ATTRIBUTE_ENCODING_SIGNED_32BIT_FLOAT
        );
        finishAttribute();
        for (float val : uvs) {
            out.writeFloat(val);
        }
    }

    private void writeAoArray(float[] aos) throws IOException {
        beginAttribute("ao");
        out.writeByte(
            ATTRIBUTE_TYPE_FLOAT |
            ATTRIBUTE_NORMALIZED |
            ATTRIBUTE_CARDINALITY_SCALAR |
            ATTRIBUTE_ENCODING_UNSIGNED_8BIT_INT
        );
        finishAttribute();
        // AO is per vertex
        for (float val : aos) {
            out.writeByte((int) (val * 255f)); 
        }
    }

    private void writeBlocklightArray(byte[] blocklights) throws IOException {
        beginAttribute("blocklight");
        out.writeByte(
            ATTRIBUTE_TYPE_INTEGER |
            ATTRIBUTE_NOT_NORMALIZED | // Light values are 0-15, not normalized to 0-1
            ATTRIBUTE_CARDINALITY_SCALAR |
            ATTRIBUTE_ENCODING_UNSIGNED_8BIT_INT
        );
        finishAttribute();
        // Blocklight is per face (triangle)
        for (byte val : blocklights) {
            // Repeat for 3 vertices of the triangle
            out.writeByte(val & 0xFF);
            out.writeByte(val & 0xFF);
            out.writeByte(val & 0xFF);
        }
    }

    private void writeSunlightArray(byte[] sunlights) throws IOException {
        beginAttribute("sunlight");
        out.writeByte(
            ATTRIBUTE_TYPE_INTEGER |
            ATTRIBUTE_NOT_NORMALIZED |
            ATTRIBUTE_CARDINALITY_SCALAR |
            ATTRIBUTE_ENCODING_UNSIGNED_8BIT_INT
        );
        finishAttribute();
        // Sunlight is per face (triangle)
        for (byte val : sunlights) {
            // Repeat for 3 vertices of the triangle
            out.writeByte(val & 0xFF);
            out.writeByte(val & 0xFF);
            out.writeByte(val & 0xFF);
        }
    }

    private void writeMaterialGroups(List<ArrayTileModelLegacy.MaterialGroup> groups) throws IOException {
        out.writeByte(groups.size()); // Number of groups - 1 byte
        for (ArrayTileModelLegacy.MaterialGroup group : groups) {
            write2ByteInt(group.materialIndex); // Material-ID - 2 bytes (short)
            write3ByteInt(group.startFaceIndex * 3); // Start vertex-index - 3 bytes
            write3ByteInt(group.faceCount * 3);    // Number of vertices in this group - 3 bytes
        }
    }

    private void write2ByteInt(int value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >> 8) & 0xFF);
    }

    private void write3ByteInt(int value) throws IOException {
        out.writeByte(value & 0xFF);
        out.writeByte((value >> 8) & 0xFF);
        out.writeByte((value >> 16) & 0xFF);
    }

    // private void write4ByteInt(int value) throws IOException { // Not currently used
    //     out.writeInt(value); // DataOutputStream.writeInt is big-endian, PRBM is little-endian
    //     out.writeByte(value & 0xFF);
    //     out.writeByte((value >> 8) & 0xFF);
    //     out.writeByte((value >> 16) & 0xFF);
    //     out.writeByte((value >> 24) & 0xFF);
    // }

    @Override
    public void close() throws IOException {
        out.close();
    }
} 