package dev.muzy.malti;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import dev.muzy.malti.telemetry.TelemetryService;
import dev.muzy.malti.telemetry.TelemetryBuffer;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Resource for exposing telemetry statistics and buffer information.
 * This demonstrates how to access telemetry statistics as shown in the README.
 */
@Path("/api/telemetry")
@Produces(MediaType.APPLICATION_JSON)
public class TelemetryStatsResource {

    @Inject
    TelemetryService telemetryService;

    /**
     * Get current telemetry buffer statistics
     */
    @GET
    @Path("/stats")
    public Response getStats() {
        try {
            TelemetryBuffer.BufferStats stats = telemetryService.getStats();
            
            double utilizationPercent = stats.maxSize() > 0 ? 
                (stats.currentSize() * 100.0) / stats.maxSize() : 0.0;
            
            return Response.ok(Map.of(
                "bufferStats", Map.of(
                    "currentSize", stats.currentSize(),
                    "maxSize", stats.maxSize(),
                    "totalAdded", stats.totalAdded(),
                    "totalSent", stats.totalSent(),
                    "totalFailed", stats.totalFailed(),
                    "utilizationPercent", utilizationPercent
                ),
                "timestamp", LocalDateTime.now(),
                "service", "telemetry-demo"
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Failed to retrieve telemetry stats",
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
                ))
                .build();
        }
    }

    /**
     * Get telemetry service health status
     */
    @GET
    @Path("/health")
    public Response getHealth() {
        try {
            TelemetryBuffer.BufferStats stats = telemetryService.getStats();
            double utilizationPercent = stats.maxSize() > 0 ? 
                (stats.currentSize() * 100.0) / stats.maxSize() : 0.0;
            boolean isHealthy = utilizationPercent < 90.0;
            
            Map<String, Object> healthData = Map.of(
                "status", isHealthy ? "healthy" : "warning",
                "bufferUtilization", utilizationPercent,
                "maxUtilization", 90.0,
                "currentBufferSize", stats.currentSize(),
                "maxBufferSize", stats.maxSize(),
                "timestamp", LocalDateTime.now()
            );
            
            int statusCode = isHealthy ? Response.Status.OK.getStatusCode() : 207;
            return Response.status(statusCode)
                .entity(healthData)
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of(
                    "status", "unhealthy",
                    "error", "Telemetry service unavailable",
                    "message", e.getMessage(),
                    "timestamp", LocalDateTime.now()
                ))
                .build();
        }
    }

    /**
     * Reset telemetry statistics (for testing purposes)
     * Note: This would typically be a POST or DELETE operation
     */
    @POST
    @Path("/reset")
    public Response resetStats() {
        // Note: The actual TelemetryService might not have a reset method
        // This is just for demonstration purposes
        return Response.ok(Map.of(
            "message", "Telemetry stats reset requested",
            "note", "This is a demo endpoint - actual reset functionality depends on the telemetry service implementation",
            "timestamp", LocalDateTime.now()
        )).build();
    }
}
