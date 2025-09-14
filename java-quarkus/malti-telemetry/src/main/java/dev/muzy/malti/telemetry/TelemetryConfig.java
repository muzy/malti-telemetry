package dev.muzy.malti.telemetry;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.Optional;

/**
 * Configuration properties for Malti telemetry system.
 * This uses Quarkus's type-safe configuration approach.
 */
@ConfigMapping(prefix = "malti")
public interface TelemetryConfig {
    
    /**
     * Service name for telemetry records
     */
    @WithDefault("unknown-service")
    String serviceName();
    
    /**
     * API key for authentication with Malti server
     */
    Optional<String> apiKey();
    
    /**
     * Base URL of the Malti server
     */
    @WithDefault("http://localhost:8000")
    String url();
    
    /**
     * Node identifier for this instance
     */
    @WithDefault("unknown-node")
    String node();
    
    /**
     * Batch configuration
     */
    Batch batch();
    
    /**
     * HTTP client configuration
     */
    Http http();
    
    /**
     * Clean mode - ignore 401/404 responses
     */
    @WithDefault("true")
    boolean cleanMode();
    
    /**
     * Overflow threshold percentage for buffer
     */
    @WithDefault("90.0")
    double overflowThresholdPercent();
    
    interface Batch {
        /**
         * Number of records per batch
         */
        @WithDefault("500")
        int size();
        
        /**
         * Interval in seconds between batch sends
         */
        @WithDefault("60")
        int intervalSeconds();
        
        /**
         * Maximum number of retries for failed requests
         */
        @WithDefault("3")
        int maxRetries();
        
        /**
         * Delay in seconds between retries
         */
        @WithDefault("5")
        int retryDelaySeconds();
    }
    
    interface Http {
        /**
         * HTTP request timeout in seconds
         */
        @WithDefault("15")
        int timeoutSeconds();
        
        /**
         * Maximum number of keepalive connections
         */
        @WithDefault("5")
        int maxKeepaliveConnections();
        
        /**
         * Maximum total connections
         */
        @WithDefault("10")
        int maxConnections();
    }
}
