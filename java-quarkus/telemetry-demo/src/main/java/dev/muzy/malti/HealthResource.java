package dev.muzy.malti;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Simple health check resource for the demo application
 */
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    public Response health() {
        return Response.ok(Map.of(
            "status", "UP",
            "service", "telemetry-demo",
            "timestamp", LocalDateTime.now(),
            "version", "1.0.0-SNAPSHOT"
        )).build();
    }

    @GET
    @Path("/ready")
    public Response ready() {
        return Response.ok(Map.of(
            "status", "READY",
            "service", "telemetry-demo",
            "timestamp", LocalDateTime.now()
        )).build();
    }

    @GET
    @Path("/live")
    public Response live() {
        return Response.ok(Map.of(
            "status", "ALIVE",
            "service", "telemetry-demo",
            "timestamp", LocalDateTime.now()
        )).build();
    }
}
