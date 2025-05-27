package de.bluecolored.bluemap.core.util;

import de.bluecolored.bluemap.core.util.stream.OnCloseOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.WatchService;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility methods for file operations and handling streams.
 */
public class FileHelper {

    /**
     * Creates an OutputStream that writes to a ".filepart"-file first and then atomically moves (overwrites) to the final target atomically
     * once the stream gets closed.
     *
     * @param file the target file path
     * @return an OutputStream writing to a temp file and moving to target on close
     * @throws IOException if an I/O error occurs
     */
    public static OutputStream createFilepartOutputStream(final Path file) throws IOException {
        Path folder = file.toAbsolutePath().normalize().getParent();
        final Path partFile = folder.resolve(file.getFileName() + ".filepart");
        FileHelper.createDirectories(folder);
        OutputStream os = Files.newOutputStream(partFile, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        return new OnCloseOutputStream(os, () -> {
            if (!Files.exists(partFile)) return;
            FileHelper.createDirectories(folder);
            FileHelper.atomicMove(partFile, file);
        });
    }

    /**
     * Tries to move the file atomically, but fallbacks to a normal move operation if moving atomically fails.
     *
     * @param from the source path
     * @param to the destination path
     * @throws IOException if an I/O error occurs
     */
    public static void atomicMove(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (FileNotFoundException | NoSuchFileException ignore) {
        } catch (IOException ex) {
            try {
                Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
            } catch (FileNotFoundException | NoSuchFileException ignore) {
            } catch (Throwable t) {
                t.addSuppressed(ex);
                throw t;
            }
        }
    }

    /**
     * Same as {@link Files#createDirectories(Path, FileAttribute[])} but accepts symlinked folders.
     *
     * @param dir the directory path to create
     * @param attrs optional file attributes
     * @return the created directory path
     * @throws IOException if an I/O error occurs
     * @see Files#createDirectories(Path, FileAttribute[])
     */
    public static Path createDirectories(Path dir, FileAttribute<?>... attrs) throws IOException {
        if (Files.isDirectory(dir)) return dir;
        return Files.createDirectories(dir, attrs);
    }

    /**
     * Extracts the entire zip-file into the given target directory.
     *
     * @param zipFile the URL of the zip file
     * @param targetDirectory the target directory to extract into
     * @param options optional copy options
     * @throws IOException if an I/O error occurs
     */
    public static void extractZipFile(URL zipFile, Path targetDirectory, CopyOption... options) throws IOException {
        Path temp = Files.createTempFile(null, ".zip");
        FileHelper.copy(zipFile, temp);
        FileHelper.extractZipFile(temp, targetDirectory, options);
        Files.deleteIfExists(temp);
    }

    /**
     * Extracts the entire zip-file into the given target directory.
     *
     * @param zipFile the path to the zip file
     * @param targetDirectory the target directory to extract into
     * @param options optional copy options
     * @throws IOException if an I/O error occurs
     */
    public static void extractZipFile(Path zipFile, Path targetDirectory, CopyOption... options) throws IOException {
        try (FileSystem webappZipFs = FileSystems.newFileSystem(zipFile, (ClassLoader) null)) {
            CopyingPathVisitor copyAction = new CopyingPathVisitor(targetDirectory, options);
            for (Path root : webappZipFs.getRootDirectories()) {
                Files.walkFileTree(root, copyAction);
            }
        }
    }

    /**
     * Copies from a URL to a target-path.
     *
     * @param source the URL to copy from
     * @param target the path to copy to
     * @throws IOException if an I/O error occurs
     */
    public static void copy(URL source, Path target) throws IOException {
        try (
                InputStream in = source.openStream();
                OutputStream out = Files.newOutputStream(target)
        ) {
            transferTo(in, out);
        }
    }

    /**
     * Java 8 compatible replacement for InputStream.transferTo().
     *
     * @param in the input stream to read from
     * @param out the output stream to write to
     * @return the number of bytes transferred
     * @throws IOException if an I/O error occurs
     */
    public static long transferTo(InputStream in, OutputStream out) throws IOException {
        BufferedInputStream bis = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in);
        BufferedOutputStream bos = out instanceof BufferedOutputStream ? (BufferedOutputStream) out : new BufferedOutputStream(out);

        byte[] buffer = new byte[8192];
        long transferred = 0;
        int read;

        while ((read = bis.read(buffer, 0, buffer.length)) >= 0) {
            bos.write(buffer, 0, read);
            transferred += read;
        }

        bos.flush();
        return transferred;
    }

    /**
     * Uses file-watchers on the path-parent to wait until a specific file or folder exists.
     *
     * @param path the file or folder path to wait for
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return true if the file/folder exists, false if timed out
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if interrupted while waiting
     */
    public static boolean awaitExistence(Path path, long timeout, TimeUnit unit) throws IOException, InterruptedException {
        if (Files.exists(path)) return true;

        long endTime = System.currentTimeMillis() + unit.toMillis(timeout);

        Path parent = path.toAbsolutePath().normalize().getParent();
        if (parent == null) throw new IOException("No parent directory exists that can be watched.");
        if (!awaitExistence(parent, timeout, unit)) return false;

        try (WatchService watchService = parent.getFileSystem().newWatchService()) {
            parent.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            while (!Files.exists(path)) {
                long now = System.currentTimeMillis();
                if (now >= endTime) return false;
                WatchKey key = watchService.poll(endTime - now, TimeUnit.MILLISECONDS);
                if (key != null) key.reset();
            }
            return true;
        }
    }

    /**
     * Adapted version of {@link Files#walk(Path, int, FileVisitOption...)}.
     * This version ignores NoSuchFileException if they occur while iterating the file-tree.
     *
     * @param start the starting file or directory
     * @param maxDepth the maximum number of directory levels to visit
     * @param options options to configure the traversal
     * @return a Stream of paths
     * @throws IOException if an I/O error occurs
     */
    public static Stream<Path> walk(Path start, int maxDepth, FileVisitOption... options) throws IOException {
        FileTreeIterator iterator = new FileTreeIterator(start, maxDepth, options);
        try {
            Spliterator<FileTreeWalker.Event> spliterator =
                    Spliterators.spliteratorUnknownSize(iterator, Spliterator.DISTINCT);
            return StreamSupport.stream(spliterator, false)
                    .onClose(iterator::close)
                    .map(FileTreeWalker.Event::file);
        } catch (Error|RuntimeException e) {
            iterator.close();
            throw e;
        }
    }

    /**
     * Adapted version of {@link Files#walk(Path, FileVisitOption...)} .
     * This version ignores NoSuchFileException if they occur while iterating the file-tree.
     *
     * @param start the starting file or directory
     * @param options options to configure the traversal
     * @return a Stream of paths
     * @throws IOException if an I/O error occurs
     */
    public static Stream<Path> walk(Path start, FileVisitOption... options) throws IOException {
        return walk(start, Integer.MAX_VALUE, options);
    }

}