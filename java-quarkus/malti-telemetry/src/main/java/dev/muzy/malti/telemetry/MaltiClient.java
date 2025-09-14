package dev.muzy.malti.telemetry;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for sending telemetry data to the Malti server.
 * This client is configured to be reactive and non-blocking.
 */
@RegisterRestClient(configKey = "malti-api")
public interface MaltiClient {
    
    /**
     * Send a batch of telemetry records to the Malti server.
     * 
     * @param apiKey The API key for authentication
     * @param batchRequest The telemetry batch request containing the records
     * @return A Uni that completes when the request is sent
     */
    @POST
    @Path("/api/v1/ingest")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Void> sendTelemetryBatch(
        @HeaderParam("X-API-Key") String apiKey,
        TelemetryBatchRequest batchRequest
    );
}
