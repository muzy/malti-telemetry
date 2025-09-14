package dev.muzy.malti.telemetry;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe buffer for storing telemetry records.
 * This mirrors the Python implementation's TelemetryBuffer functionality.
 */
@ApplicationScoped
public class TelemetryBuffer {
    
    private final BlockingQueue<TelemetryRecord> buffer;
    private final int maxSize;
    private final ReentrantLock addLock = new ReentrantLock();
    
    // Statistics
    private final AtomicLong totalAdded = new AtomicLong(0);
    private final AtomicLong totalSent = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    
    public TelemetryBuffer() {
        this.maxSize = 25000; // Default max size from Python implementation
        this.buffer = new LinkedBlockingQueue<>(maxSize);
    }
    
    /**
     * Add a telemetry record to the buffer.
     * If the buffer is full, the oldest record is removed to make space.
     */
    public void add(TelemetryRecord record) {
        // Ensure eviction + insert happens atomically relative to other adders
        addLock.lock();
        try {
            if (buffer.remainingCapacity() == 0) {
                buffer.poll(); // Evict oldest (head)
            }
            buffer.offer(record);
            totalAdded.incrementAndGet();
        } finally {
            addLock.unlock();
        }
    }
    
    /**
     * Get a batch of records and remove them from buffer.
     */
    public List<TelemetryRecord> getBatch(int batchSize) {
        List<TelemetryRecord> batch = new ArrayList<>();
        
        // Drain up to batchSize elements from the buffer
        buffer.drainTo(batch, batchSize);
        
        return batch;
    }
    
    /**
     * Get current buffer size.
     */
    public int size() {
        return buffer.size();
    }
    
    /**
     * Check if buffer is empty.
     */
    public boolean isEmpty() {
        return buffer.isEmpty();
    }
    
    /**
     * Get the maximum size of the buffer.
     */
    public int getMaxSize() {
        return maxSize;
    }
    
    /**
     * Update statistics.
     */
    public void updateStats(int sent, int failed) {
        totalSent.addAndGet(sent);
        totalFailed.addAndGet(failed);
    }
    
    /**
     * Get buffer statistics.
     */
    public BufferStats getStats() {
        return new BufferStats(
            totalAdded.get(),
            totalSent.get(),
            totalFailed.get(),
            buffer.size(),
            maxSize
        );
    }
    
    /**
     * Statistics record for the buffer.
     */
    public record BufferStats(
        long totalAdded,
        long totalSent,
        long totalFailed,
        int currentSize,
        int maxSize
    ) {}
}
