package dev.muzy.malti.telemetry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TelemetryRecordTest {

    @Test
    void testTelemetryRecordCreation() {
        TelemetryRecord record = new TelemetryRecord(
            "test-service",
            "GET",
            "/api/test",
            200,
            150,
            "test-consumer",
            "test-node",
            "test-context"
        );

        assertEquals("test-service", record.getService());
        assertEquals("GET", record.getMethod());
        assertEquals("/api/test", record.getEndpoint());
        assertEquals(200, record.getStatus());
        assertEquals(150, record.getResponseTime());
        assertEquals("test-consumer", record.getConsumer());
        assertEquals("test-node", record.getNode());
        assertEquals("test-context", record.getContext());
        assertNotNull(record.getCreatedAt());
    }

    @Test
    void testDefaultConstructor() {
        TelemetryRecord record = new TelemetryRecord();
        assertNotNull(record.getCreatedAt());
    }

    @Test
    void testToString() {
        TelemetryRecord record = new TelemetryRecord(
            "test-service",
            "POST",
            "/api/users",
            201,
            250,
            "user123",
            "node-1",
            "signup"
        );

        String result = record.toString();
        assertTrue(result.contains("test-service"));
        assertTrue(result.contains("POST"));
        assertTrue(result.contains("/api/users"));
        assertTrue(result.contains("201"));
        assertTrue(result.contains("250"));
        assertTrue(result.contains("user123"));
    }
}
