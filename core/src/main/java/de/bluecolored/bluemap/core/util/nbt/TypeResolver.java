package de.bluecolored.bluemap.core.util.nbt;

import com.google.gson.reflect.TypeToken;
import java.io.IOException;

/**
 * Interface for resolving types dynamically based on NBT data.
 * This is a replacement for the bluenbt TypeResolver to support NBT API migration.
 *
 * @param <T> The base type that this resolver can handle
 * @param <B> The type of the base object used for resolution
 */
public interface TypeResolver<T, B> {
    
    /**
     * Gets the base type token that this resolver handles.
     *
     * @return The base TypeToken
     */
    TypeToken<B> getBaseType();

    /**
     * Resolves the specific type token based on the base object.
     *
     * @param base The base object to resolve from
     * @return The resolved TypeToken
     */
    TypeToken<? extends T> resolve(B base);

    /**
     * Gets all possible types that this resolver can resolve to.
     *
     * @return An Iterable of all possible TypeTokens
     */
    Iterable<TypeToken<? extends T>> getPossibleTypes();

    /**
     * Handles exceptions that occur during parsing.
     *
     * @param parseException The exception that occurred
     * @param base The base object that caused the exception
     * @return A fallback object to use
     */
    T onException(IOException parseException, B base);
} 
