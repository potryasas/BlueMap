package de.bluecolored.bluemap.core.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.bluecolored.bluemap.core.resources.adapter.LocalDateTimeAdapter;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the Minecraft version manifest as provided by Mojang's public API.
 */
@Getter
@SuppressWarnings({"FieldMayBeFinal", "unused"})
public class VersionManifest {

    public static final String DOMAIN = "https://piston-meta.mojang.com/";
    public static final String MANIFEST_URL = DOMAIN + "mc/game/version_manifest.json";

    private static final int CONNECTION_TIMEOUT = 10000;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private static VersionManifest instance;

    private Latest latest;
    private Version[] versions;

    @Getter(AccessLevel.NONE)
    private transient @Nullable Map<String, Version> versionMap;

    @Getter(AccessLevel.NONE)
    private transient boolean sorted;

    /**
     * Returns the cached VersionManifest instance, or fetches it if not loaded.
     *
     * @return the VersionManifest instance
     * @throws IOException if an I/O error occurs when fetching
     */
    public static VersionManifest getOrFetch() throws IOException {
        if (instance == null) return fetch();
        return instance;
    }

    /**
     * Fetches the latest version manifest from the Mojang API.
     *
     * @return the fetched VersionManifest instance
     * @throws IOException if an I/O error occurs
     */
    public static VersionManifest fetch() throws IOException {
        try (
                InputStream in = openInputStream(MANIFEST_URL);
                Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
        ) {
            instance = GSON.fromJson(reader, VersionManifest.class);
        }
        return instance;
    }

    /**
     * Returns an array of versions, ordered newest first.
     *
     * @return an array of versions
     */
    public synchronized Version[] getVersions() {
        if (!sorted) Arrays.sort(versions, Comparator.reverseOrder());
        return versions;
    }

    /**
     * Returns a specific version by its ID, or {@code null} if not found.
     *
     * @param id the version id
     * @return the Version object, or null if not found
     */
    public synchronized @Nullable Version getVersion(String id) {
        if (versionMap == null) {
            versionMap = new HashMap<>();
            for (Version version : versions)
                versionMap.put(version.id, version);
        }

        return versionMap.get(id);
    }

    /**
     * Contains the IDs of the latest release and snapshot versions.
     */
    @Getter
    public static class Latest {
        private String release;
        private String snapshot;
    }

    /**
     * Represents a single Minecraft version entry from the manifest.
     */
    @Getter
    public static class Version implements Comparable<Version> {

        private String id;
        private String type;
        private String url;
        private LocalDateTime time;
        private LocalDateTime releaseTime;

        @Getter(AccessLevel.NONE)
        private transient @Nullable VersionDetail detail;

        /**
         * Fetches the detailed information for this version, using its URL.
         *
         * @return the VersionDetail object
         * @throws IOException if an I/O error occurs while fetching details
         */
        public synchronized VersionDetail fetchDetail() throws IOException {
            if (detail == null) {
                try (
                        InputStream in = openInputStream(url);
                        Reader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                ) {
                    detail = GSON.fromJson(reader, VersionDetail.class);
                }
            }

            return detail;
        }

        @Override
        public int compareTo(@NotNull VersionManifest.Version version) {
            return releaseTime.compareTo(version.releaseTime);
        }

    }

    /**
     * Contains detailed information about a particular version, including downloads.
     */
    @Getter
    public static class VersionDetail {
        private String id;
        private String type;
        private Downloads downloads;
    }

    /**
     * Contains download links for client and server for a version.
     */
    @Getter
    public static class Downloads {
        private Download client;
        private Download server;
    }

    /**
     * Represents a downloadable file, such as a client or server JAR.
     */
    @Getter
    public static class Download {
        private String url;
        private long size;
        private String sha1;

        /**
         * Opens an InputStream to download the file from its URL.
         *
         * @return an InputStream for the download URL
         * @throws IOException if an I/O error occurs
         */
        public InputStream createInputStream() throws IOException {
            return openInputStream(url);
        }

    }

    /**
     * Opens an InputStream for a given URL path, with appropriate timeouts.
     *
     * @param urlPath the URL string
     * @return an InputStream to the URL
     * @throws IOException if an I/O error or invalid URL occurs
     */
    private static InputStream openInputStream(String urlPath) throws IOException {
        try {
            URL downloadUrl = new URI(urlPath).toURL();
            URLConnection connection = downloadUrl.openConnection();
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(CONNECTION_TIMEOUT);
            return connection.getInputStream();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

}