package dev.muzy.malti.telemetry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class TelemetryBufferTest {

    private TelemetryBuffer buffer;

    @BeforeEach
    void setUp() {
        buffer = new TelemetryBuffer();
    }

    @Test
    void testAddRecord() {
        assertTrue(buffer.isEmpty());
        assertEquals(0, buffer.size());

        TelemetryRecord record = new TelemetryRecord(
            "test-service", "GET", "/test", 200, 100, "consumer", "node", "context"
        );

        buffer.add(record);

        assertFalse(buffer.isEmpty());
        assertEquals(1, buffer.size());
    }

    @Test
    void testGetBatch() {
        // Add some records
        for (int i = 0; i < 5; i++) {
            TelemetryRecord record = new TelemetryRecord(
                "test-service", "GET", "/test" + i, 200, 100, "consumer", "node", "context"
            );
            buffer.add(record);
        }

        assertEquals(5, buffer.size());

        // Get a batch of 3
        List<TelemetryRecord> batch = buffer.getBatch(3);
        assertEquals(3, batch.size());
        assertEquals(2, buffer.size()); // Remaining records

        // Verify the records are correct
        for (int i = 0; i < 3; i++) {
            assertEquals("/test" + i, batch.get(i).getEndpoint());
        }
    }

    @Test
    void testGetBatchLargerThanBuffer() {
        // Add 2 records
        for (int i = 0; i < 2; i++) {
            TelemetryRecord record = new TelemetryRecord(
                "test-service", "GET", "/test" + i, 200, 100, "consumer", "node", "context"
            );
            buffer.add(record);
        }

        // Request batch of 5 (more than available)
        List<TelemetryRecord> batch = buffer.getBatch(5);
        assertEquals(2, batch.size()); // Should only return what's available
        assertEquals(0, buffer.size()); // Buffer should be empty
    }

    @Test
    void testUpdateStats() {
        TelemetryBuffer.BufferStats initialStats = buffer.getStats();
        assertEquals(0, initialStats.totalSent());
        assertEquals(0, initialStats.totalFailed());

        buffer.updateStats(10, 2);

        TelemetryBuffer.BufferStats updatedStats = buffer.getStats();
        assertEquals(10, updatedStats.totalSent());
        assertEquals(2, updatedStats.totalFailed());
    }

    @Test
    void testMaxSize() {
        assertEquals(25000, buffer.getMaxSize());
    }
}
