package de.bluecolored.bluemap.bukkit.legacy.render;

public class Vector3i {
    private final int x;
    private final int y;
    private final int z;

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Vector3i add(Vector3i other) {
        return new Vector3i(x + other.x, y + other.y, z + other.z);
    }

    public Vector3i subtract(Vector3i other) {
        return new Vector3i(x - other.x, y - other.y, z - other.z);
    }

    public Vector3i multiply(int scalar) {
        return new Vector3i(x * scalar, y * scalar, z * scalar);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3i vector3i = (Vector3i) o;
        return x == vector3i.x && y == vector3i.y && z == vector3i.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    @Override
    public String toString() {
        return "Vector3i{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
} 