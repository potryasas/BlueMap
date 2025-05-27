package de.bluecolored.bluemap.renderapi;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RenderAPI {
    private static final Logger logger = LoggerFactory.getLogger(RenderAPI.class);
    
    private final Javalin app;
    private final ExecutorService renderExecutor;
    private final ConcurrentHashMap<String, WorldRenderer> worldRenderers;
    private final Path webRoot;
    
    public RenderAPI(int port, Path webRoot) {
        this.webRoot = webRoot;
        this.renderExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.worldRenderers = new ConcurrentHashMap<>();
        
        // Initialize HTTP server
        this.app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> cors.add(it -> {
                it.anyHost();
            }));
        }).start(port);
        
        // Register endpoints
        setupEndpoints();
        
        logger.info("RenderAPI started on port {}", port);
    }
    
    private void setupEndpoints() {
        // Health check
        app.get("/health", ctx -> ctx.result("OK"));
        
        // Register world
        app.post("/world/{worldId}", ctx -> {
            String worldId = ctx.pathParam("worldId");
            WorldConfig config = ctx.bodyAsClass(WorldConfig.class);
            
            WorldRenderer renderer = new WorldRenderer(worldId, config, webRoot);
            worldRenderers.put(worldId, renderer);
            
            ctx.result("World registered: " + worldId);
        });
        
        // Update chunks
        app.post("/world/{worldId}/chunks", ctx -> {
            String worldId = ctx.pathParam("worldId");
            ChunkData[] chunks = ctx.bodyAsClass(ChunkData[].class);
            
            WorldRenderer renderer = worldRenderers.get(worldId);
            if (renderer == null) {
                ctx.status(404).result("World not found: " + worldId);
                return;
            }
            
            renderExecutor.submit(() -> {
                try {
                    renderer.updateChunks(chunks);
                } catch (Exception e) {
                    logger.error("Error updating chunks for world " + worldId, e);
                }
            });
            
            ctx.result("Chunks queued for rendering");
        });
        
        // Get render status
        app.get("/world/{worldId}/status", ctx -> {
            String worldId = ctx.pathParam("worldId");
            WorldRenderer renderer = worldRenderers.get(worldId);
            
            if (renderer == null) {
                ctx.status(404).result("World not found: " + worldId);
                return;
            }
            
            ctx.json(renderer.getStatus());
        });
    }
    
    public void shutdown() {
        try {
            renderExecutor.shutdown();
            app.stop();
            logger.info("RenderAPI stopped");
        } catch (Exception e) {
            logger.error("Error shutting down RenderAPI", e);
        }
    }
} 