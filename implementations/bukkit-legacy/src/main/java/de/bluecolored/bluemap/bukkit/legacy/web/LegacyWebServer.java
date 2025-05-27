package de.bluecolored.bluemap.bukkit.legacy.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.bluecolored.bluemap.bukkit.legacy.LegacyBukkitPlugin;
import de.bluecolored.bluemap.bukkit.legacy.config.WebConfig;

public class LegacyWebServer {
    private static final int DEFAULT_PORT = 8100;
    private static final String DEFAULT_BIND_ADDRESS = "0.0.0.0";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private final LegacyBukkitPlugin plugin;
    private final Logger logger;
    private HttpServer server;
    private final WebConfig config;
    
    public LegacyWebServer(LegacyBukkitPlugin plugin, WebConfig config) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.config = config;
    }
    
    public void start() throws IOException {
        if (server != null) {
            logger.warning("Web server is already running!");
            return;
        }
        
        int port = config.getPort();
        String bindAddress = config.getBindAddress();
        
        server = HttpServer.create(new InetSocketAddress(bindAddress, port), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        
        // Register handlers
        server.createContext("/", new WebRootHandler());
        server.createContext("/maps", new MapsHandler());
        server.createContext("/assets", new AssetsHandler());
        server.createContext("/api", new ApiHandler());
        
        server.start();
        logger.info("Web server started on " + bindAddress + ":" + port);
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            logger.info("Web server stopped");
        }
    }
    
    private class WebRootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            Path filePath = plugin.getWebRoot().resolve(path.substring(1));
            serveFile(exchange, filePath);
        }
    }
    
    private class MapsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            Path filePath = plugin.getWebRoot().resolve(path.substring(1));
            serveFile(exchange, filePath);
        }
    }
    
    private class AssetsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            Path filePath = plugin.getWebRoot().resolve(path.substring(1));
            serveFile(exchange, filePath);
        }
    }
    
    private class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            if (path.equals("/api/maps")) {
                handleMapsApi(exchange);
            } else if (path.startsWith("/api/map/")) {
                handleMapApi(exchange, path.substring("/api/map/".length()));
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
        
        private void handleMapsApi(HttpExchange exchange) throws IOException {
            Map<String, Object> response = new HashMap<>();
            response.put("maps", plugin.getLoadedServerWorlds());
            
            String json = GSON.toJson(response);
            sendJsonResponse(exchange, json);
        }
        
        private void handleMapApi(HttpExchange exchange, String mapId) throws IOException {
            // Handle specific map data
            Map<String, Object> response = new HashMap<>();
            // Add map data based on mapId
            
            String json = GSON.toJson(response);
            sendJsonResponse(exchange, json);
        }
    }
    
    private void serveFile(HttpExchange exchange, Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }
        
        String contentType = getContentType(filePath.toString());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        
        File file = filePath.toFile();
        exchange.sendResponseHeaders(200, file.length());
        
        try (OutputStream os = exchange.getResponseBody();
             FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        }
    }
    
    private void sendJsonResponse(HttpExchange exchange, String json) throws IOException {
        byte[] response = json.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
    
    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
} 