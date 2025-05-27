package de.bluecolored.bluemap.core.map.hires;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.util.Grid;

import java.util.function.Predicate;

public interface RenderSettings {

    Vector3i DEFAULT_MIN = Vector3i.from(Integer.MIN_VALUE);
    Vector3i DEFAULT_MAX = Vector3i.from(Integer.MAX_VALUE);

    /**
     * The y-level below which "caves" will not be rendered.
     *
     * @return the y-level threshold for cave rendering
     */
    int getRemoveCavesBelowY();

    /**
     * The y-level relative to the ocean-floor heightmap below which caves will not be rendered.
     *
     * @return the relative y-level for cave detection below the ocean floor
     */
    int getCaveDetectionOceanFloor();

    /**
     * If blocklight should be used instead of skylight to detect "caves".
     *
     * @return true if blocklight is used for cave detection, false otherwise
     */
    boolean isCaveDetectionUsesBlockLight();

    /**
     * The minimum position of blocks to render.
     *
     * @return the minimum position {@link Vector3i}
     */
    default Vector3i getMinPos() {
        return DEFAULT_MIN;
    }

    /**
     * The maximum position of blocks to render.
     *
     * @return the maximum position {@link Vector3i}
     */
    default Vector3i getMaxPos() {
        return DEFAULT_MAX;
    }

    /**
     * The (default) ambient light of this world (0-1).
     *
     * @return ambient light value (0-1)
     */
    float getAmbientLight();

    /**
     * The same as the maximum height, but blocks that are above this value are treated as AIR.<br>
     * This leads to the top-faces being rendered instead of them being culled.
     *
     * @return true if edges should be rendered, false otherwise
     */
    default boolean isRenderEdges() {
        return true;
    }

    /**
     * If missing light data should be ignored.
     *
     * @return true if missing light data is ignored, false otherwise
     */
    default boolean isIgnoreMissingLightData() {
        return false;
    }

    /**
     * Checks if the given (x, z) position is within the render boundaries.
     *
     * @param x the x coordinate
     * @param z the z coordinate
     * @return true if inside the render boundaries, false otherwise
     */
    default boolean isInsideRenderBoundaries(int x, int z) {
        Vector3i min = getMinPos();
        Vector3i max = getMaxPos();

        return
                x >= min.getX() &&
                x <= max.getX() &&
                z >= min.getZ() &&
                z <= max.getZ();
    }

    /**
     * Checks if the given (x, y, z) position is within the render boundaries.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return true if inside the render boundaries, false otherwise
     */
    default boolean isInsideRenderBoundaries(int x, int y, int z) {
        Vector3i min = getMinPos();
        Vector3i max = getMaxPos();

        return
                x >= min.getX() &&
                x <= max.getX() &&
                z >= min.getZ() &&
                z <= max.getZ() &&
                y >= min.getY() &&
                y <= max.getY();
    }

    /**
     * Checks if the given cell is within the render boundaries.
     *
     * @param cell the cell to check
     * @param grid the grid instance
     * @param allowPartiallyIncludedCells whether partially included cells are allowed
     * @return true if the cell is inside the render boundaries, false otherwise
     */
    default boolean isInsideRenderBoundaries(Vector2i cell, Grid grid, boolean allowPartiallyIncludedCells) {
        Vector2i cellMin = allowPartiallyIncludedCells ? grid.getCellMin(cell) : grid.getCellMax(cell);
        if (cellMin.getX() > getMaxPos().getX()) return false;
        if (cellMin.getY() > getMaxPos().getZ()) return false;

        Vector2i cellMax = allowPartiallyIncludedCells ? grid.getCellMax(cell) : grid.getCellMin(cell);
        if (cellMax.getX() < getMinPos().getX()) return false;
        return cellMax.getY() >= getMinPos().getZ();
    }

    /**
     * Returns a predicate which is filtering out all cells of a {@link Grid}
     * that are outside the render boundaries.
     *
     * @param grid the grid to use
     * @param allowPartiallyIncludedCells whether partially included cells are allowed
     * @return a predicate that filters cells outside the render boundaries
     */
    default java.util.function.Predicate<Vector2i> getCellRenderBoundariesFilter(Grid grid, boolean allowPartiallyIncludedCells) {
        return cell -> isInsideRenderBoundaries(cell, grid, allowPartiallyIncludedCells);
    }

    /**
     * Whether the hires layer should be saved.
     *
     * @return true if the hires layer should be saved, false otherwise
     */
    boolean isSaveHiresLayer();

    /**
     * Whether only the top layer should be rendered.
     *
     * @return true if only the top layer should be rendered, false otherwise
     */
    boolean isRenderTopOnly();

}