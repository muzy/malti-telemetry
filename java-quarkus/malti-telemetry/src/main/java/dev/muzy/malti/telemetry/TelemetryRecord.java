package dev.muzy.malti.telemetry;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Represents a single telemetry record that tracks HTTP request metrics.
 * This mirrors the Python implementation's TelemetryRecord structure.
 */
public class TelemetryRecord {
    
    @JsonProperty("service")
    private String service;
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("endpoint")
    private String endpoint;
    
    @JsonProperty("status")
    private int status;
    
    @JsonProperty("response_time")
    private int responseTime;
    
    @JsonProperty("consumer")
    private String consumer;
    
    @JsonProperty("node")
    private String node;
    
    @JsonProperty("context")
    private String context;
    
    @JsonProperty("created_at")
    private String createdAt;
    
    public TelemetryRecord() {
        this.createdAt = Instant.now().toString();
    }
    
    public TelemetryRecord(String service, String method, String endpoint, int status, 
                          int responseTime, String consumer, String node, String context) {
        this.service = service;
        this.method = method;
        this.endpoint = endpoint;
        this.status = status;
        this.responseTime = responseTime;
        this.consumer = consumer;
        this.node = node;
        this.context = context;
        this.createdAt = Instant.now().toString();
    }
    
    // Getters and setters
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public int getResponseTime() { return responseTime; }
    public void setResponseTime(int responseTime) { this.responseTime = responseTime; }
    
    public String getConsumer() { return consumer; }
    public void setConsumer(String consumer) { this.consumer = consumer; }
    
    public String getNode() { return node; }
    public void setNode(String node) { this.node = node; }
    
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return String.format("TelemetryRecord{service='%s', method='%s', endpoint='%s', status=%d, responseTime=%d, consumer='%s'}",
                service, method, endpoint, status, responseTime, consumer);
    }
}
