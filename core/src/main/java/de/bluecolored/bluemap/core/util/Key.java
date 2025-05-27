package de.bluecolored.bluemap.core.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a namespaced key for identifying resources.
 */
public class Key implements Keyed {

    private static final ConcurrentHashMap<String, String> STRING_INTERN_POOL = new ConcurrentHashMap<>();

    public static final String MINECRAFT_NAMESPACE = "minecraft";
    public static final String BLUEMAP_NAMESPACE = "bluemap";

    private final String namespace;
    private final String value;
    private final String formatted;

    /**
     * Constructs a Key from a formatted string (e.g., "namespace:value").
     *
     * @param formatted the formatted key string
     */
    public Key(String formatted) {
        String namespace = MINECRAFT_NAMESPACE;
        String value = formatted;
        int namespaceSeparator = formatted.indexOf(':');
        if (namespaceSeparator > 0) {
            namespace = formatted.substring(0, namespaceSeparator);
            value = formatted.substring(namespaceSeparator + 1);
        }

        this.namespace = intern(namespace);
        this.value = intern(value);
        this.formatted = intern(this.namespace + ":" + this.value);
    }

    /**
     * Constructs a Key with the given namespace and value.
     *
     * @param namespace the namespace of the key
     * @param value the value of the key
     */
    public Key(String namespace, String value) {
        this.namespace = intern(namespace);
        this.value = intern(value);
        this.formatted = intern(this.namespace + ":" + this.value);
    }

    /**
     * Returns the namespace of this key.
     *
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the value of this key.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the formatted string representation of this key (e.g., "namespace:value").
     *
     * @return the formatted key string
     */
    public String getFormatted() {
        return formatted;
    }

    @Override
    public Key getKey() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key)) return false;
        Key that = (Key) o;
        if (!that.canEqual(this)) return false;
        return formatted == that.formatted;
    }

    /**
     * Determines whether another object can be considered equal to this Key.
     *
     * @param o the other object
     * @return true if the other object is a Key, false otherwise
     */
    protected boolean canEqual(Object o) {
        return o instanceof Key;
    }

    @Override
    public int hashCode() {
        return formatted.hashCode();
    }

    @Override
    public String toString() {
        return formatted;
    }

    /**
     * Parses a formatted key string into a Key.
     *
     * @param formatted the formatted key string
     * @return the parsed Key
     */
    public static Key parse(String formatted) {
        return new Key(formatted);
    }

    /**
     * Parses a formatted key string into a Key, using the given namespace if none is specified.
     *
     * @param formatted the formatted key string
     * @param defaultNamespace the default namespace to use if not present in the string
     * @return the parsed Key
     */
    public static Key parse(String formatted, String defaultNamespace) {
        String namespace = defaultNamespace;
        String value = formatted;
        int namespaceSeparator = formatted.indexOf(':');
        if (namespaceSeparator > 0) {
            namespace = formatted.substring(0, namespaceSeparator);
            value = formatted.substring(namespaceSeparator + 1);
        }

        return new Key(namespace, value);
    }

    /**
     * Creates a Key in the "minecraft" namespace.
     *
     * @param value the value of the key
     * @return a Key in the "minecraft" namespace
     */
    public static Key minecraft(String value) {
        return new Key(MINECRAFT_NAMESPACE, value);
    }

    /**
     * Creates a Key in the "bluemap" namespace.
     *
     * @param value the value of the key
     * @return a Key in the "bluemap" namespace
     */
    public static Key bluemap(String value) {
        return new Key(BLUEMAP_NAMESPACE, value);
    }

    /**
     * Uses a fast intern pool instead of {@link String#intern()}.
     *
     * @param string the string to intern
     * @return the interned string
     */
    protected static String intern(String string) {
        String interned = STRING_INTERN_POOL.putIfAbsent(string, string);
        return interned != null ? interned : string;
    }

}