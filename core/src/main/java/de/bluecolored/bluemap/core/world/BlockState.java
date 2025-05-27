package de.bluecolored.bluemap.core.world;

import de.bluecolored.bluemap.core.util.Key;
import org.jetbrains.annotations.NotNull;
import lombok.Getter;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a BlockState.<br>
 * It is important that {@link #hashCode} and {@link #equals} are implemented correctly, for the caching to work properly.<br>
 * <br>
 * <i>The implementation of this class has to be thread-safe!</i><br>
 */
@Getter
public class BlockState extends Key {

    private static final Pattern BLOCKSTATE_SERIALIZATION_PATTERN = Pattern.compile("^(.+?)(?:\\[(.*)])?$");

    public static final BlockState AIR = new BlockState("minecraft:air");
    public static final BlockState MISSING = new BlockState("minecraft:missing");

    private boolean hashed;
    private int hash;

    private final Map<String, String> properties;
    private final Property[] propertiesArray;

    private final boolean isAir, isWater, isWaterlogged;
    private int liquidLevel = -1, redstonePower = -1;

    private final String id;

    /**
     * Constructs a BlockState with the given block id.
     *
     * @param id the block id
     */
    public BlockState(String id) {
        this(id, Collections.emptyMap());
    }

    /**
     * Constructs a BlockState with the given block id and properties.
     *
     * @param id the block id
     * @param properties the block state properties
     */
    public BlockState(String id, Map<String, String> properties) {
        super(id);

        this.hashed = false;
        this.hash = 0;

        this.properties = properties;

        // build properties array
        List<Property> propertyList = new ArrayList<>();
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                propertyList.add(new Property(entry.getKey(), entry.getValue()));
            }
        }
        Collections.sort(propertyList);
        this.propertiesArray = propertyList.toArray(new Property[propertyList.size()]);

        // special fast-access properties
        this.isAir =
                "minecraft:air".equals(this.getFormatted()) ||
                "minecraft:cave_air".equals(this.getFormatted()) ||
                "minecraft:void_air".equals(this.getFormatted());

        this.isWater = "minecraft:water".equals(this.getFormatted());
        this.isWaterlogged = "true".equals(properties.get("waterlogged"));

        this.id = id;
    }

    /**
     * An immutable map of all properties of this block.<br>
     * <br>
     * For Example:<br>
     * <code>
     * facing = east<br>
     * half = bottom<br>
     * </code>
     *
     * @return an immutable map of all properties of this block
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Checks if this block is air.
     *
     * @return true if this block is air, false otherwise
     */
    public boolean isAir() {
        return isAir;
    }

    /**
     * Checks if this block is water.
     *
     * @return true if this block is water, false otherwise
     */
    public boolean isWater() {
        return isWater;
    }

    /**
     * Checks if this block is waterlogged.
     *
     * @return true if this block is waterlogged, false otherwise
     */
    public boolean isWaterlogged() {
        return isWaterlogged;
    }

    /**
     * Returns the liquid level for this block if present, or 0 if not present or invalid.
     *
     * @return the liquid level (0-15)
     */
    public int getLiquidLevel() {
        if (liquidLevel == -1) {
            try {
                String levelString = properties.get("level");
                liquidLevel = levelString != null ? Integer.parseInt(levelString) : 0;
                if (liquidLevel > 15) liquidLevel = 15;
                if (liquidLevel < 0) liquidLevel = 0;
            } catch (NumberFormatException ex) {
                liquidLevel = 0;
            }
        }
        return liquidLevel;
    }

    /**
     * Returns the redstone power for this block if present, or default value if not present or invalid.
     *
     * @return the redstone power (0-15)
     */
    public int getRedstonePower() {
        if (redstonePower == -1) {
            try {
                String levelString = properties.get("power");
                redstonePower = levelString != null ? Integer.parseInt(levelString) : 0;
                if (redstonePower > 15) redstonePower = 15;
                if (redstonePower < 0) redstonePower = 0;
            } catch (NumberFormatException ex) {
                redstonePower = 15;
            }
        }
        return redstonePower;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BlockState)) return false;
        BlockState b = (BlockState) obj;
        if (!b.canEqual(this)) return false;
        if (getFormatted() != b.getFormatted()) return false;
        return Arrays.equals(propertiesArray, b.propertiesArray);
    }

    @Override
    protected boolean canEqual(Object o) {
        return o instanceof BlockState;
    }

    @Override
    public int hashCode() {
        if (!hashed){
            hash = Objects.hash( getFormatted(), getProperties() );
            hashed = true;
        }
        return hash;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",");
        for (Entry<String, String> e : getProperties().entrySet()){
            sj.add(e.getKey() + "=" + e.getValue());
        }
        return getFormatted() + "[" + sj + "]";
    }

    /**
     * Parses a serialized block state string to a BlockState instance.
     *
     * @param serializedBlockState the serialized string
     * @return the parsed BlockState
     * @throws IllegalArgumentException if parsing fails
     */
    public static BlockState fromString(String serializedBlockState) throws IllegalArgumentException {
        try {
            Matcher m = BLOCKSTATE_SERIALIZATION_PATTERN.matcher(serializedBlockState);

            if (!m.find())
                throw new IllegalArgumentException("'" + serializedBlockState + "' could not be parsed to a BlockState!");

            Map<String, String> pt = new HashMap<>();
            String g2 = m.group(2);
            if (g2 != null && !g2.isEmpty()){
                String[] propertyStrings = g2.trim().split(",");
                for (String s : propertyStrings){
                    String[] kv = s.split("=", 2);
                    pt.put(kv[0], kv[1]);
                }
            }

            String blockId = m.group(1).trim();

            return new BlockState(blockId, pt);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("'" + serializedBlockState + "' could not be parsed to a BlockState!");
        }
    }

    /**
     * Represents a key-value pair property of a block state.
     */
    public static final class Property implements Comparable<Property> {
        private final String key, value;

        /**
         * Constructs a block state property with a given key and value.
         *
         * @param key the property key
         * @param value the property value
         */
        public Property(String key, String value) {
            this.key = intern(key);
            this.value = intern(value);
        }

        @SuppressWarnings("StringEquality")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Property property = (Property) o;
            return key == property.key && value == property.value;
        }

        @Override
        public int hashCode() {
            return key.hashCode() * 31 ^ value.hashCode();
        }

        @Override
        public int compareTo(@NotNull BlockState.Property o) {
            int keyCompare = key.compareTo(o.key);
            return keyCompare != 0 ? keyCompare : value.compareTo(o.value);
        }
    }

    /**
     * Returns a BlockState from the given numeric state value.
     *
     * @param state the numeric state
     * @return the BlockState for the given state
     */
    public static BlockState get(long state) {
        // TODO: Implement state to block mapping
        return AIR;
    }

}