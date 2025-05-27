package de.bluecolored.bluemap.core.storage;

import java.io.Closeable;
import java.io.IOException;
import java.util.stream.Stream;

public interface Storage extends Closeable {

    /**
     * Does everything necessary to initialize this storage.
     * (E.g. create tables on a database if they don't exist or upgrade older data).
     *
     * @throws IOException if an I/O error occurs during initialization
     */
    void initialize() throws IOException;

    /**
     * Returns the {@link MapStorage} for the given mapId.<br>
     * <br>
     * If this method is invoked multiple times with the same <code>mapId</code>, it is important that the returned MapStorage should at least
     * be equal (<code>equals() == true</code>) to the previously returned storages!
     *
     * @param mapId the id of the map
     * @return the MapStorage for the specified mapId
     */
    MapStorage map(String mapId);

    /**
     * Fetches and returns a stream of all map-id's in this storage.
     *
     * @return a stream of all map ids
     * @throws IOException if an I/O error occurs during retrieval
     */
    Stream<String> mapIds() throws IOException;

    /**
     * Checks if this storage is closed.
     *
     * @return true if the storage is closed, false otherwise
     */
    boolean isClosed();

}