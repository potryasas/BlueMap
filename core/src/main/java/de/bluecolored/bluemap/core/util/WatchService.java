package de.bluecolored.bluemap.core.util;

import lombok.experimental.StandardException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A watch service that watches for changes and events.
 * @param <T> The type of the events or changes this WatchService provides
 */
public interface WatchService<T> extends AutoCloseable {

    /**
     * Retrieves and consumes the next batch of events.
     *
     * @return a list of events, or {@code null} if no events are present
     * @throws IOException if an I/O error occurs
     * @throws ClosedException if the watch-service is closed
     */
    @Nullable
    List<T> poll() throws IOException;

    /**
     * Retrieves and consumes the next batch of events,
     * waiting if necessary up to the specified wait time if none are yet present.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return a list of events, or {@code null} if the specified waiting time elapses before an event is present
     * @throws IOException if an I/O error occurs
     * @throws ClosedException if the watch-service is closed, or it is closed while waiting for the next event
     * @throws InterruptedException if interrupted while waiting
     */
    @Nullable
    List<T> poll(long timeout, TimeUnit unit) throws IOException, InterruptedException;

    /**
     * Retrieves and consumes the next batch of events,
     * waiting if necessary until an event becomes available.
     *
     * @return a list of events
     * @throws IOException if an I/O error occurs
     * @throws ClosedException if the watch-service is closed, or it is closed while waiting for the next event
     * @throws InterruptedException if interrupted while waiting
     */
    List<T> take() throws IOException, InterruptedException;

    /**
     * Thrown when the WatchService is closed or gets closed when polling or while waiting for events.
     */
    @StandardException
    class ClosedException extends RuntimeException {}

}