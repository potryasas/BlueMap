package de.bluecolored.bluemap.core.map;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a rectangular bounding box defined by min and max coordinates.
 */
@Data
@AllArgsConstructor
public class Bounds {
    private final double minX;
    private final double minY;
    private final double minZ;
    private final double maxX;
    private final double maxY;
    private final double maxZ;

    /**
     * Checks if a point is inside these bounds.
     *
     * @param x the x coordinate of the point to check
     * @param y the y coordinate of the point to check
     * @param z the z coordinate of the point to check
     * @return true if the point is inside the bounds, false otherwise
     */
    public boolean contains(double x, double y, double z) {
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    /**
     * Checks if another bounds object is fully contained within these bounds.
     *
     * @param other the other bounds to check
     * @return true if the other bounds is fully contained, false otherwise
     */
    public boolean contains(Bounds other) {
        return other.minX >= minX && other.maxX <= maxX &&
               other.minY >= minY && other.maxY <= maxY &&
               other.minZ >= minZ && other.maxZ <= maxZ;
    }

    /**
     * Checks if another bounds object intersects with these bounds.
     *
     * @param other the other bounds to check for intersection
     * @return true if the bounds intersect, false otherwise
     */
    public boolean intersects(Bounds other) {
        return !(other.minX > maxX || other.maxX < minX ||
                 other.minY > maxY || other.maxY < minY ||
                 other.minZ > maxZ || other.maxZ < minZ);
    }

    /**
     * Creates a new bounds with the union of this and another bounds.
     *
     * @param other the other bounds to union with
     * @return a new Bounds representing the union of the two
     */
    public Bounds union(Bounds other) {
        return new Bounds(
            Math.min(minX, other.minX),
            Math.min(minY, other.minY),
            Math.min(minZ, other.minZ),
            Math.max(maxX, other.maxX),
            Math.max(maxY, other.maxY),
            Math.max(maxZ, other.maxZ)
        );
    }

    /**
     * Creates a new bounds with the intersection of this and another bounds.
     *
     * @param other the other bounds to intersect with
     * @return a new Bounds representing the intersection, or null if there is no intersection
     */
    public Bounds intersection(Bounds other) {
        double newMinX = Math.max(minX, other.minX);
        double newMinY = Math.max(minY, other.minY);
        double newMinZ = Math.max(minZ, other.minZ);
        double newMaxX = Math.min(maxX, other.maxX);
        double newMaxY = Math.min(maxY, other.maxY);
        double newMaxZ = Math.min(maxZ, other.maxZ);

        // Check if there is a valid intersection
        if (newMinX > newMaxX || newMinY > newMaxY || newMinZ > newMaxZ) {
            return null; // No intersection
        }

        return new Bounds(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    /**
     * Creates a new bounds with the same dimensions but expanded by the given amount in all directions.
     *
     * @param amount the amount to expand in all directions
     * @return a new Bounds expanded by the specified amount
     */
    public Bounds expand(double amount) {
        return new Bounds(
            minX - amount,
            minY - amount,
            minZ - amount,
            maxX + amount,
            maxY + amount,
            maxZ + amount
        );
    }

    /**
     * Width of the bounds (X-axis).
     *
     * @return the width of the bounds
     */
    public double getWidth() {
        return maxX - minX;
    }

    /**
     * Height of the bounds (Y-axis).
     *
     * @return the height of the bounds
     */
    public double getHeight() {
        return maxY - minY;
    }

    /**
     * Depth of the bounds (Z-axis).
     *
     * @return the depth of the bounds
     */
    public double getDepth() {
        return maxZ - minZ;
    }
}