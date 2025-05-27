package de.bluecolored.bluemap.core.world.mca.region;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.Keyed;
import de.bluecolored.bluemap.core.util.Registry;
import de.bluecolored.bluemap.core.world.Region;
import de.bluecolored.bluemap.core.world.mca.ChunkLoader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a type of region file (e.g., MCA or linear) in a Minecraft world.
 */
public interface RegionType extends Keyed {

    RegionType MCA = new Impl(Key.bluemap("mca"), MCARegion::new, MCARegion::getRegionFileName, MCARegion.FILE_PATTERN);
    RegionType LINEAR = new Impl(Key.bluemap("linear"), LinearRegion::new, LinearRegion::getRegionFileName, LinearRegion.FILE_PATTERN);

    RegionType DEFAULT = MCA;
    Registry<RegionType> REGISTRY = new Registry<>(
            MCA,
            LINEAR
    );

    /**
     * Creates a new {@link Region} from the given chunk loader and region file.
     *
     * @param <T> the chunk type
     * @param chunkLoader the loader for chunks
     * @param regionFile the path to the region file
     * @return the created region
     */
    <T> Region<T> createRegion(ChunkLoader<T> chunkLoader, Path regionFile);

    /**
     * Converts region coordinates into the region file name.
     *
     * @param regionX the region x coordinate
     * @param regionZ the region z coordinate
     * @return the region file name
     */
    String getRegionFileName(int regionX, int regionZ);

    /**
     * Converts the region file name into region coordinates.
     * Returns null if the name does not match the expected format.
     *
     * @param fileName the region file name
     * @return the region coordinates as a Vector2i or null if not recognized
     */
    @Nullable Vector2i getRegionFromFileName(String fileName);

    /**
     * Finds the {@link RegionType} matching the given file name, or null if none matches.
     *
     * @param fileName the region file name
     * @return the matching RegionType, or null if not found
     */
    static @Nullable RegionType forFileName(String fileName) {
        for (RegionType regionType : REGISTRY.values()) {
            if (regionType.getRegionFromFileName(fileName) != null)
                return regionType;
        }
        return null;
    }

    /**
     * Finds the region coordinates for a given file name, considering all registered region types.
     *
     * @param fileName the region file name
     * @return the region coordinates as a Vector2i, or null if not recognized
     */
    static @Nullable Vector2i regionForFileName(String fileName) {
        for (RegionType regionType : REGISTRY.values()) {
            Vector2i pos = regionType.getRegionFromFileName(fileName);
            if (pos != null) return pos;
        }
        return null;
    }

    /**
     * Loads a region from the region folder at given coordinates, using the first matching region type.
     *
     * @param <T> the chunk type
     * @param chunkLoader the loader for chunks
     * @param regionFolder the folder containing region files
     * @param regionX the region x coordinate
     * @param regionZ the region z coordinate
     * @return the loaded region
     */
    static <T> Region<T> loadRegion(ChunkLoader<T> chunkLoader, Path regionFolder, int regionX, int regionZ) {
        for (RegionType regionType : REGISTRY.values()) {
            Path regionFile = regionFolder.resolve(regionType.getRegionFileName(regionX, regionZ));
            if (Files.exists(regionFile)) return regionType.createRegion(chunkLoader, regionFile);
        }
        return DEFAULT.createRegion(chunkLoader, regionFolder.resolve(DEFAULT.getRegionFileName(regionX, regionZ)));
    }

    @RequiredArgsConstructor
    class Impl implements RegionType {

        @Getter private final Key key;
        private final RegionFactory regionFactory;
        private final RegionFileNameFunction regionFileNameFunction;
        private final Pattern regionFileNamePattern;

        /**
         * {@inheritDoc}
         */
        public <T> Region<T> createRegion(ChunkLoader<T> chunkLoader, Path regionFile) {
            return this.regionFactory.create(chunkLoader, regionFile);
        }

        /**
         * {@inheritDoc}
         */
        public String getRegionFileName(int regionX, int regionZ) {
            return regionFileNameFunction.getRegionFileName(regionX, regionZ);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public @Nullable Vector2i getRegionFromFileName(String fileName) {
            Matcher matcher = regionFileNamePattern.matcher(fileName);
            if (!matcher.matches()) return null;

            try {
                int regionX = Integer.parseInt(matcher.group(1));
                int regionZ = Integer.parseInt(matcher.group(2));

                // sanity-check for roughly minecraft max boundaries (-30 000 000 to 30 000 000)
                if (
                        regionX < -100000 || regionX > 100000 ||
                        regionZ < -100000 || regionZ > 100000
                ) {
                    return null;
                }

                return new Vector2i(regionX, regionZ);

            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    /**
     * Factory interface for creating regions.
     */
    @FunctionalInterface
    interface RegionFactory {
        /**
         * Creates a region for the given chunk loader and region file.
         *
         * @param <T> the chunk type
         * @param chunkLoader the chunk loader
         * @param regionFile the path to the region file
         * @return the created region
         */
        <T> Region<T> create(ChunkLoader<T> chunkLoader, Path regionFile);
    }

    /**
     * Interface for creating region file names from coordinates.
     */
    @FunctionalInterface
    interface RegionFileNameFunction {
        /**
         * Creates a region file name from region coordinates.
         *
         * @param regionX the region x coordinate
         * @param regionZ the region z coordinate
         * @return the region file name
         */
        String getRegionFileName(int regionX, int regionZ);
    }

}