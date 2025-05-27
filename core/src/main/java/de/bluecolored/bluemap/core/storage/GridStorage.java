package de.bluecolored.bluemap.core.storage;

import de.bluecolored.bluemap.core.storage.compression.CompressedInputStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.Stream;

/**
 * A storage storing items on an infinite grid (x,z), each position on the grid can hold one item.
 */
public interface GridStorage {

    /**
     * Returns an {@link OutputStream} that can be used to write an item into this storage at the given position
     * (overwriting any existing item).
     * The OutputStream is expected to be closed by the caller of this method.
     *
     * @param x the x position in the grid
     * @param z the z position in the grid
     * @return an OutputStream to write the item-data
     * @throws IOException if an I/O error occurs
     */
    OutputStream write(int x, int z) throws IOException;

    /**
     * Returns a {@link CompressedInputStream} that can be used to read the item from this storage at the given position
     * or null if there is no item stored.
     * The CompressedInputStream is expected to be closed by the caller of this method.
     *
     * @param x the x position in the grid
     * @param z the z position in the grid
     * @return a CompressedInputStream to read the item-data, or null if nothing is stored
     * @throws IOException if an I/O error occurs
     */
    @Nullable CompressedInputStream read(int x, int z) throws IOException;

    /**
     * Deletes the item from this storage at the given position.
     *
     * @param x the x position in the grid
     * @param z the z position in the grid
     * @throws IOException if an I/O error occurs
     */
    void delete(int x, int z) throws IOException;

    /**
     * Tests if there is an item stored on the given position in this storage.
     *
     * @param x the x position in the grid
     * @param z the z position in the grid
     * @return true if an item exists at the given position, false otherwise
     * @throws IOException if an I/O error occurs
     */
    boolean exists(int x, int z) throws IOException;

    /**
     * Returns a {@link ItemStorage} for the given position.
     *
     * @param x the x position in the grid
     * @param z the z position in the grid
     * @return the ItemStorage for the specified position
     */
    ItemStorage cell(int x, int z);

    /**
     * Returns a stream over all <b>existing</b> items in this storage.
     *
     * @return a stream of existing items (cells) in this storage
     * @throws IOException if an I/O error occurs
     */
    Stream<Cell> stream() throws IOException;

    /**
     * Checks if this storage is closed.
     *
     * @return true if the storage is closed, false otherwise
     */
    boolean isClosed();

    interface Cell extends ItemStorage {

        /**
         * Returns the x position of this item in the grid.
         *
         * @return the x position
         */
        int getX();

        /**
         * Returns the z position of this item in the grid.
         *
         * @return the z position
         */
        int getZ();

    }

    @SuppressWarnings("ClassCanBeRecord")
    @Getter
    @RequiredArgsConstructor
    class GridStorageCell implements Cell {

        private final GridStorage storage;
        private final int x, z;

        @Override
        public OutputStream write() throws IOException {
            return storage.write(x, z);
        }

        @Override
        public CompressedInputStream read() throws IOException {
            return storage.read(x, z);
        }

        @Override
        public void delete() throws IOException {
            storage.delete(x, z);
        }

        @Override
        public boolean exists() throws IOException {
            return storage.exists(x, z);
        }

        @Override
        public boolean isClosed() {
            return storage.isClosed();
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getZ() {
            return z;
        }

    }

}