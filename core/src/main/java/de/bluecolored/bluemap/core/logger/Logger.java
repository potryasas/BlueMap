package de.bluecolored.bluemap.core.logger;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

public abstract class Logger implements AutoCloseable {

    @SuppressWarnings("StaticInitializerReferencesSubClass")
    public static final MultiLogger global = new MultiLogger(stdOut());
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                global.close();
            } catch (Exception ignore) {}
        }));
    }

    /**
     * Logs an error with the specified throwable.
     *
     * @param throwable the throwable to log
     */
    public void logError(Throwable throwable) {
        logError(throwable.getMessage(), throwable);
    }

    /**
     * Logs an error message with the associated throwable.
     *
     * @param message the error message
     * @param throwable the throwable to log
     */
    public abstract void logError(String message, Throwable throwable);

    /**
     * Logs a warning message.
     *
     * @param message the warning message
     */
    public abstract void logWarning(String message);

    /**
     * Logs an informational message.
     *
     * @param message the information message
     */
    public abstract void logInfo(String message);

    /**
     * Logs a debug message.
     *
     * @param message the debug message
     */
    public abstract void logDebug(String message);

    /**
     * Only log the error if no message has been logged before with the same key.
     *
     * @param key the unique key for the error message
     * @param message the error message
     * @param throwable the throwable to log
     */
    public abstract void noFloodError(String key, String message, Throwable throwable);

    /**
     * Only log the warning if no message has been logged before with the same key.
     *
     * @param key the unique key for the warning message
     * @param message the warning message
     */
    public abstract void noFloodWarning(String key, String message);

    /**
     * Only log the info if no message has been logged before with the same key.
     *
     * @param key the unique key for the info message
     * @param message the info message
     */
    public abstract void noFloodInfo(String key, String message);

    /**
     * Only log the debug-message if no message has been logged before with the same key.
     *
     * @param key the unique key for the debug message
     * @param message the debug message
     */
    public abstract void noFloodDebug(String key, String message);

    /**
     * Only log the error if no message has been logged before with the same content.
     *
     * @param throwable the throwable to log
     */
    public void noFloodError(Throwable throwable){
        noFloodError(throwable.getMessage(), throwable);
    }

    /**
     * Only log the error if no message has been logged before with the same content.
     *
     * @param message the error message
     * @param throwable the throwable to log
     */
    public void noFloodError(String message, Throwable throwable){
        noFloodError(message, message, throwable);
    }

    /**
     * Only log the warning if no message has been logged before with the same content.
     *
     * @param message the warning message
     */
    public void noFloodWarning(String message){
        noFloodWarning(message, message);
    }

    /**
     * Only log the info if no message has been logged before with the same content.
     *
     * @param message the info message
     */
    public void noFloodInfo(String message){
        noFloodInfo(message, message);
    }

    /**
     * Only log the debug-message if no message has been logged before with the same content.
     *
     * @param message the debug message
     */
    public void noFloodDebug(String message){
        noFloodDebug(message, message);
    }

    @Override
    public void close() throws Exception {}

    /**
     * Clears all entries from the no-flood log.
     */
    public abstract void clearNoFloodLog();

    /**
     * Removes the no-flood log entry for the specified key.
     *
     * @param key the key to remove
     */
    public abstract void removeNoFloodKey(String key);

    /**
     * Removes the no-flood log entry for the specified message.
     *
     * @param message the message to remove
     */
    public void removeNoFloodMessage(String message){
        removeNoFloodKey(message);
    }

    /**
     * Creates a logger that logs to standard output and error streams.
     *
     * @return a Logger instance using standard out and error
     */
    public static Logger stdOut() {
        return new PrintStreamLogger(System.out, System.err);
    }

    /**
     * Creates a logger that logs to standard output and error streams.
     *
     * @param debug whether debug output should be enabled
     * @return a Logger instance using standard out and error
     */
    public static Logger stdOut(boolean debug){
        return new PrintStreamLogger(System.out, System.err, debug);
    }

    /**
     * Creates a logger that logs to the specified file path.
     *
     * @param path the file path for the log file
     * @return a Logger instance that logs to the specified file
     * @throws IOException if an I/O error occurs
     */
    public static Logger file(Path path) throws IOException {
        return file(path, null);
    }

    /**
     * Creates a logger that logs to the specified file path.
     *
     * @param path the file path for the log file
     * @param append whether to append to the file or overwrite
     * @return a Logger instance that logs to the specified file
     * @throws IOException if an I/O error occurs
     */
    public static Logger file(Path path, boolean append) throws IOException {
        return file(path, null, append);
    }

    /**
     * Creates a logger that logs to the specified file path with the given format.
     *
     * @param path the file path for the log file
     * @param format the format for log entries
     * @return a Logger instance that logs to the specified file
     * @throws IOException if an I/O error occurs
     */
    public static Logger file(Path path, String format) throws IOException {
        return file(path, format, true);
    }

    /**
     * Creates a logger that logs to the specified file path with the given format.
     *
     * @param path the file path for the log file
     * @param format the format for log entries, or null to use the default format
     * @param append whether to append to the file or overwrite
     * @return a Logger instance that logs to the specified file
     * @throws IOException if an I/O error occurs
     */
    public static Logger file(Path path, @Nullable String format, boolean append) throws IOException {
        Files.createDirectories(path.getParent());

        FileHandler fileHandler = new FileHandler(path.toString(), append);
        fileHandler.setFormatter(format == null ? new LogFormatter() : new LogFormatter(format));

        java.util.logging.Logger javaLogger = java.util.logging.Logger.getAnonymousLogger();
        javaLogger.setLevel(Level.ALL);
        javaLogger.setUseParentHandlers(false);
        javaLogger.addHandler(fileHandler);

        return new JavaLogger(javaLogger);
    }

    /**
     * Combines multiple logger instances into a single logger.
     *
     * @param logger the loggers to combine
     * @return a combined Logger
     */
    public static Logger combine(Iterable<Logger> logger) {
        return combine(StreamSupport.stream(logger.spliterator(), false)
                .toArray(size -> new Logger[size]));
    }

    /**
     * Combines multiple logger instances into a single logger.
     *
     * @param logger the loggers to combine
     * @return a combined Logger
     */
    public static Logger combine(Logger... logger) {
        if (logger.length == 0) return new VoidLogger();
        if (logger.length == 1) return logger[0];
        return new MultiLogger(logger);
    }

}