package de.bluecolored.bluemap.core.storage.compression;

import de.bluecolored.bluemap.core.util.stream.DelegateInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream that is aware of the {@link Compression} that its data is compressed with.
 */
public class CompressedInputStream extends DelegateInputStream {

    private final Compression compression;

    /**
     * Creates a new CompressedInputStream with {@link Compression#NONE} from an (uncompressed) {@link InputStream}.
     * This does <b>not</b> compress the provided InputStream.
     *
     * @param in the uncompressed input stream
     */
    public CompressedInputStream(InputStream in) {
        this(in, Compression.NONE);
    }

    /**
     * Creates a new CompressedInputStream from an <b>already compressed</b> {@link InputStream} and the {@link Compression}
     * it is compressed with.
     * This does <b>not</b> compress the provided InputStream.
     *
     * @param in the input stream (already compressed)
     * @param compression the compression type used for the input stream
     */
    public CompressedInputStream(InputStream in, Compression compression) {
        super(in);
        this.compression = compression;
    }

    /**
     * Returns the decompressed {@link InputStream}.
     *
     * @return the decompressed input stream
     * @throws IOException if an I/O error occurs during decompression
     */
    public InputStream decompress() throws IOException {
        return compression.decompress(in);
    }

    /**
     * Returns the {@link Compression} this InputStream's data is compressed with.
     *
     * @return the compression used for this stream
     */
    public Compression getCompression() {
        return compression;
    }

}