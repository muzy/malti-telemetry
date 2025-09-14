package dev.muzy.malti.telemetry;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Wrapper class for telemetry batch requests to match the expected server format.
 * The server expects: {"requests": [array of telemetry records]}
 */
public class TelemetryBatchRequest {
    
    @JsonProperty("requests")
    private List<TelemetryRecord> requests;
    
    public TelemetryBatchRequest() {
    }
    
    public TelemetryBatchRequest(List<TelemetryRecord> requests) {
        this.requests = requests;
    }
    
    public List<TelemetryRecord> getRequests() {
        return requests;
    }
    
    public void setRequests(List<TelemetryRecord> requests) {
        this.requests = requests;
    }
}
