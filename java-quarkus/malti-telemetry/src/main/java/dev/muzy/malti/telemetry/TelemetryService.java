package dev.muzy.malti.telemetry;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main telemetry service that handles batching and sending telemetry data.
 * This mirrors the Python implementation's BatchSender functionality.
 */
@ApplicationScoped
public class TelemetryService {
    
    @Inject
    TelemetryConfig config;
    
    @Inject
    TelemetryBuffer buffer;
    
    @Inject
    @RestClient
    MaltiClient maltiClient;
    
    @Inject
    ObjectMapper objectMapper;
    
    private final AtomicBoolean sending = new AtomicBoolean(false);
    
    /**
     * Record a telemetry record asynchronously.
     * This is the main entry point for adding telemetry data.
     */
    public void recordRequest(String method, String endpoint, int status, 
                            int responseTime, String consumer, String context) {
        
        // Skip recording if in clean mode and status is 401 or 404
        if (shouldIgnoreStatus(status)) {
            return;
        }

        if (config.apiKey().isEmpty()) {
            Log.warn("No API key configured, skipping telemetry record");
            return;
        }

        TelemetryRecord record = new TelemetryRecord(
            config.serviceName(),
            method,
            endpoint,
            status,
            responseTime,
            consumer != null ? consumer : "",
            config.node(),
            context != null ? context : ""
        );
        
        buffer.add(record);
        Log.debugf("Added telemetry record: %s", record);
        
        // Check if we need to send immediately due to overflow threshold
        double currentFillPercentage = (double) buffer.size() / buffer.getMaxSize() * 100.0;
        if (currentFillPercentage >= config.overflowThresholdPercent()) {
            if (config.apiKey().isEmpty()) {
                Log.warn("Buffer overflow threshold reached but no API key configured, skipping telemetry send");
                return;
            }
            Log.debugf("Buffer overflow threshold reached (%.1f%%), triggering immediate send", currentFillPercentage);
            sendBatchAsync().subscribe().with(
                success -> Log.debug("Overflow batch sent successfully"),
                failure -> Log.errorf("Failed to send overflow batch: %s", failure.getMessage())
            );
        }
    }
    
    /**
     * Scheduled method to send batches periodically.
     * Uses Quarkus scheduler with configuration-based interval.
     */
    @Scheduled(every = "${malti.batch.interval-seconds:60}s")
    public void scheduledSend() {
        if (config.apiKey().isEmpty()) {
            Log.warn("No API key configured, skipping scheduled telemetry send");
            return;
        }
        
        if (!buffer.isEmpty()) {
            Log.debug("Scheduled batch send triggered");
            sendBatchAsync().subscribe().with(
                success -> Log.debug("Scheduled batch sent successfully"),
                failure -> Log.errorf("Failed to send scheduled batch: %s", failure.getMessage())
            );
        }
    }
    
    /**
     * Send a batch of telemetry records asynchronously with retry logic.
     */
    public Uni<Void> sendBatchAsync() {
        // Prevent concurrent sends
        if (!sending.compareAndSet(false, true)) {
            Log.debug("Send already in progress, skipping");
            return Uni.createFrom().voidItem();
        }

        List<TelemetryRecord> batch = buffer.getBatch(config.batch().size());

        if (batch.isEmpty()) {
            Log.debug("No records to send");
            sending.set(false);
            return Uni.createFrom().voidItem();
        }

        Log.debugf("Sending batch of %d records", batch.size());

        return sendWithRetry(batch, 0)
            .onItem().invoke(() -> {
                buffer.updateStats(batch.size(), 0);
                Log.debugf("Successfully sent batch of %d records", batch.size());
            })
            .onFailure().invoke(throwable -> {
                buffer.updateStats(0, batch.size());
                Log.errorf("Failed to send batch after all retries: %s", throwable.getMessage());
            })
            .onTermination().invoke(() -> sending.set(false));
    }
    
    /**
     * Send batch with exponential backoff retry logic.
     */
    private Uni<Void> sendWithRetry(List<TelemetryRecord> batch, int attempt) {
        // Create the batch request wrapper
        TelemetryBatchRequest batchRequest = new TelemetryBatchRequest(batch);
        
        // Log the JSON payload being sent
        try {
            String jsonPayload = objectMapper.writeValueAsString(batchRequest);
            Log.infof("Sending telemetry batch JSON payload: %s", jsonPayload);
        } catch (Exception e) {
            Log.warnf("Failed to serialize batch to JSON for logging: %s", e.getMessage());
        }
        
        return maltiClient.sendTelemetryBatch(config.apiKey().get(), batchRequest)
            .onFailure().retry()
            .withBackOff(Duration.ofSeconds(config.batch().retryDelaySeconds()))
            .atMost(config.batch().maxRetries())
            .onFailure().invoke(throwable -> {
                Log.errorf("Failed to send telemetry batch (attempt %d): %s", attempt + 1, throwable.getMessage());
                
                // Log additional error details if available
                if (throwable instanceof jakarta.ws.rs.WebApplicationException) {
                    jakarta.ws.rs.WebApplicationException webEx = (jakarta.ws.rs.WebApplicationException) throwable;
                    try {
                        String responseBody = webEx.getResponse().readEntity(String.class);
                        Log.errorf("Error response body: %s", responseBody);
                        Log.errorf("Error response status: %d", webEx.getResponse().getStatus());
                    } catch (Exception e) {
                        Log.warnf("Could not read error response body: %s", e.getMessage());
                    }
                }
                
                // Log the JSON payload that failed to send for debugging
                try {
                    String failedPayload = objectMapper.writeValueAsString(batchRequest);
                    Log.errorf("Failed payload was: %s", failedPayload);
                } catch (Exception e) {
                    Log.warnf("Could not serialize failed payload for logging: %s", e.getMessage());
                }
            });
    }
    
    /**
     * Check if a status code should be ignored based on clean mode configuration.
     */
    private boolean shouldIgnoreStatus(int status) {
        return config.cleanMode() && (status == 401 || status == 404);
    }
    
    /**
     * Get telemetry system statistics.
     */
    public TelemetryBuffer.BufferStats getStats() {
        return buffer.getStats();
    }
}
