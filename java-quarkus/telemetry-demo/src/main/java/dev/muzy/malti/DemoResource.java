package dev.muzy.malti;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import dev.muzy.malti.telemetry.TelemetryService;
import dev.muzy.malti.telemetry.TelemetryBuffer;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Demo REST resource showcasing various endpoints that will be tracked by Malti telemetry.
 * This demonstrates different HTTP methods, status codes, and response patterns.
 */
@Path("/api/demo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DemoResource {

    @Inject
    TelemetryService telemetryService;

    private final Random random = new Random();
    private final List<String> sampleData = List.of(
        "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta"
    );

    /**
     * Simple GET endpoint that always returns 200 OK
     */
    @GET
    @Path("/hello")
    public Response hello(@QueryParam("name") String name) {
        String message = name != null ? "Hello, " + name + "!" : "Hello, World!";
        return Response.ok(Map.of(
            "message", message,
            "timestamp", LocalDateTime.now(),
            "endpoint", "hello"
        )).build();
    }

    /**
     * GET endpoint that returns a list of items
     */
    @GET
    @Path("/items")
    public Response getItems(@QueryParam("limit") @DefaultValue("5") int limit) {
        List<Map<String, Object>> items = new ArrayList<>();
        int actualLimit = Math.min(limit, sampleData.size());
        
        for (int i = 0; i < actualLimit; i++) {
            items.add(Map.of(
                "id", i + 1,
                "name", sampleData.get(i),
                "value", random.nextInt(1000)
            ));
        }
        
        return Response.ok(Map.of(
            "items", items,
            "count", items.size(),
            "timestamp", LocalDateTime.now()
        )).build();
    }

    /**
     * GET endpoint that may return 404 for demonstration
     */
    @GET
    @Path("/items/{id}")
    public Response getItem(@PathParam("id") int id) {
        if (id < 1 || id > sampleData.size()) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of(
                    "error", "Item not found",
                    "id", id,
                    "timestamp", LocalDateTime.now()
                ))
                .build();
        }
        
        return Response.ok(Map.of(
            "id", id,
            "name", sampleData.get(id - 1),
            "value", random.nextInt(1000),
            "timestamp", LocalDateTime.now()
        )).build();
    }

    /**
     * POST endpoint for creating items
     */
    @POST
    @Path("/items")
    public Response createItem(Map<String, Object> item) {
        if (item == null || !item.containsKey("name")) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "Missing required field: name",
                    "timestamp", LocalDateTime.now()
                ))
                .build();
        }

        Map<String, Object> createdItem = Map.of(
            "id", random.nextInt(10000),
            "name", item.get("name"),
            "value", item.getOrDefault("value", random.nextInt(1000)),
            "created", LocalDateTime.now()
        );

        return Response.status(Response.Status.CREATED)
            .entity(createdItem)
            .build();
    }

    /**
     * PUT endpoint for updating items
     */
    @PUT
    @Path("/items/{id}")
    public Response updateItem(@PathParam("id") int id, Map<String, Object> updates) {
        if (id < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "Invalid ID",
                    "id", id,
                    "timestamp", LocalDateTime.now()
                ))
                .build();
        }

        if (updates == null || updates.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "No updates provided",
                    "timestamp", LocalDateTime.now()
                ))
                .build();
        }

        Map<String, Object> updatedItem = Map.of(
            "id", id,
            "name", updates.getOrDefault("name", "Updated Item"),
            "value", updates.getOrDefault("value", random.nextInt(1000)),
            "updated", LocalDateTime.now()
        );

        return Response.ok(updatedItem).build();
    }

    /**
     * DELETE endpoint
     */
    @DELETE
    @Path("/items/{id}")
    public Response deleteItem(@PathParam("id") int id) {
        if (id < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "Invalid ID",
                    "id", id,
                    "timestamp", LocalDateTime.now()
                ))
                .build();
        }

        return Response.ok(Map.of(
            "message", "Item deleted successfully",
            "id", id,
            "timestamp", LocalDateTime.now()
        )).build();
    }

    /**
     * Endpoint that simulates random errors for testing different status codes
     */
    @GET
    @Path("/random")
    public Response randomResponse() {
        int outcome = random.nextInt(100);
        
        if (outcome < 70) {
            // 70% success
            return Response.ok(Map.of(
                "status", "success",
                "value", random.nextInt(1000),
                "timestamp", LocalDateTime.now()
            )).build();
        } else if (outcome < 85) {
            // 15% client error
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "Simulated client error",
                    "timestamp", LocalDateTime.now()
                ))
                .build();
        } else if (outcome < 95) {
            // 10% not found
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of(
                    "error", "Simulated not found",
                    "timestamp", LocalDateTime.now()
                ))
                .build();
        } else {
            // 5% server error
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Simulated server error",
                    "timestamp", LocalDateTime.now()
                ))
                .build();
        }
    }

    /**
     * Endpoint that requires authentication (simulated)
     * Returns 401 if no X-API-Key header is provided
     */
    @GET
    @Path("/secure")
    public Response secureEndpoint(@HeaderParam("X-API-Key") String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of(
                    "error", "API key required",
                    "timestamp", LocalDateTime.now()
                ))
                .build();
        }

        return Response.ok(Map.of(
            "message", "Access granted",
            "user", "demo-user",
            "timestamp", LocalDateTime.now()
        )).build();
    }

    /**
     * Endpoint that simulates slow responses
     */
    @GET
    @Path("/slow")
    public Response slowResponse(@QueryParam("delay") @DefaultValue("1000") int delayMs) {
        try {
            Thread.sleep(Math.min(delayMs, 5000)); // Max 5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return Response.ok(Map.of(
            "message", "Slow response completed",
            "delay", delayMs,
            "timestamp", LocalDateTime.now()
        )).build();
    }
}
