# Malti Telemetry Demo

A comprehensive demo application showcasing the Malti Telemetry Quarkus Extension. This demo provides various REST endpoints that demonstrate different HTTP methods, status codes, and scenarios to showcase how the telemetry extension captures and reports request metrics.

## Overview

This demo application is built using the `dev.muzy.malti` namespace and includes:

- **Demo REST Endpoints**: Various endpoints demonstrating CRUD operations, error scenarios, authentication, and performance testing
- **Telemetry Statistics**: Endpoints to view telemetry buffer statistics and health information
- **Health Checks**: Standard health, readiness, and liveness endpoints
- **Comprehensive Tests**: Unit and integration tests covering all functionality

## Prerequisites

Before running this demo, ensure you have:

1. **Java 21 or higher** installed
2. **Maven 3.8.1 or higher** installed
3. **Malti Telemetry Extension** built and installed locally:
   ```bash
   cd ../malti-telemetry
   mvn clean install
   ```

## Quick Start

### 1. Build and Run the Demo

```bash
# Navigate to the demo directory
cd java-quarkus/telemetry-demo

# Run in development mode
mvn quarkus:dev
```

The application will start on `http://localhost:8080`

### 2. Verify the Application

Check that the application is running:

```bash
curl http://localhost:8080/health
```

### 3. Test Basic Functionality

```bash
# Simple hello endpoint
curl http://localhost:8080/api/demo/hello

# Get items
curl http://localhost:8080/api/demo/items

# Check telemetry stats
curl http://localhost:8080/api/telemetry/stats
```

## Available Endpoints

### Demo Endpoints (`/api/demo/`)

#### Basic Operations
- `GET /api/demo/hello` - Simple greeting endpoint
  - Query param: `name` (optional)
- `GET /api/demo/items` - Get list of items
  - Query param: `limit` (default: 5)
- `GET /api/demo/items/{id}` - Get specific item by ID
- `POST /api/demo/items` - Create new item
- `PUT /api/demo/items/{id}` - Update existing item
- `DELETE /api/demo/items/{id}` - Delete item

#### Special Endpoints
- `GET /api/demo/random` - Random response (70% success, 15% 400, 10% 404, 5% 500)
- `GET /api/demo/secure` - Requires `X-API-Key` header (returns 401 without it)
- `GET /api/demo/slow` - Simulates slow response
  - Query param: `delay` in milliseconds (default: 1000, max: 5000)

### Telemetry Endpoints (`/api/telemetry/`)

- `GET /api/telemetry/stats` - Get telemetry buffer statistics
- `GET /api/telemetry/health` - Get telemetry service health
- `POST /api/telemetry/reset` - Reset telemetry statistics (demo only)

### Health Endpoints (`/health/`)

- `GET /health` - Application health status
- `GET /health/ready` - Readiness probe
- `GET /health/live` - Liveness probe

## Testing the Telemetry Features

### Consumer Identification

The telemetry extension captures consumer information from these headers (in order of precedence):

```bash
# Test with consumer headers
curl -H "X-Consumer-Id: my-consumer" \
     -H "X-User-Id: my-user" \
     -H "X-Malti-Context: test-context" \
     http://localhost:8080/api/demo/hello
```

### Different HTTP Methods

```bash
# GET requests
curl http://localhost:8080/api/demo/items

# POST request
curl -X POST http://localhost:8080/api/demo/items \
     -H "Content-Type: application/json" \
     -d '{"name": "TestItem", "value": 123}'

# PUT request
curl -X PUT http://localhost:8080/api/demo/items/1 \
     -H "Content-Type: application/json" \
     -d '{"name": "UpdatedItem", "value": 456}'

# DELETE request
curl -X DELETE http://localhost:8080/api/demo/items/1
```

### Error Scenarios

```bash
# 404 Not Found
curl http://localhost:8080/api/demo/items/999

# 400 Bad Request
curl -X POST http://localhost:8080/api/demo/items \
     -H "Content-Type: application/json" \
     -d '{}'

# 401 Unauthorized
curl http://localhost:8080/api/demo/secure

# 401 Authorized
curl -H "X-API-Key: test-key" http://localhost:8080/api/demo/secure
```

### Performance Testing

```bash
# Normal response
curl http://localhost:8080/api/demo/hello

# Slow response (1 second delay)
curl http://localhost:8080/api/demo/slow

# Custom delay (500ms)
curl "http://localhost:8080/api/demo/slow?delay=500"

# Random outcomes
for i in {1..10}; do curl http://localhost:8080/api/demo/random; done
```

### Viewing Telemetry Statistics

```bash
# Get detailed telemetry statistics
curl http://localhost:8080/api/telemetry/stats | jq

# Check telemetry health
curl http://localhost:8080/api/telemetry/health | jq
```

Example telemetry stats response:
```json
{
  "bufferStats": {
    "currentSize": 15,
    "maxSize": 1000,
    "totalAdded": 127,
    "totalProcessed": 112,
    "totalDropped": 0,
    "overflowCount": 0,
    "utilizationPercent": 1.5
  },
  "timestamp": "2025-09-14T10:30:45.123",
  "service": "telemetry-demo"
}
```

## Running Tests

### Unit Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=DemoResourceTest

# Run with verbose output
mvn test -Dtest=TelemetryStatsResourceTest -X
```

### Integration Tests

```bash
# Run integration tests
mvn verify

# Run specific integration test
mvn test -Dtest=TelemetryDemoIT
```

### Test Coverage

The test suite includes:

- **DemoResourceTest**: Tests all demo endpoints with various scenarios
- **TelemetryStatsResourceTest**: Tests telemetry statistics endpoints
- **HealthResourceTest**: Tests health check endpoints
- **TelemetryDemoIT**: End-to-end integration tests

## Configuration

The demo uses the following telemetry configuration (in `application.properties`):

```properties
# Service identification
malti.service-name=test-service
malti.api-key=${MALTI_API_KEY:test-service-key}
malti.url=${MALTI_URL:http://localhost:8000}
malti.node=${MALTI_NODE:quarkus-demo-node}

# Batch configuration
malti.batch.size=${MALTI_BATCH_SIZE:500}
malti.batch.interval-seconds=${MALTI_BATCH_INTERVAL:10}
malti.batch.max-retries=${MALTI_MAX_RETRIES:3}

# HTTP configuration
malti.http.timeout-seconds=${MALTI_HTTP_TIMEOUT:15}

# Feature flags
malti.clean-mode=${MALTI_CLEAN_MODE:true}
malti.overflow-threshold-percent=${MALTI_OVERFLOW_THRESHOLD_PERCENT:90.0}
```

### Environment Variables

You can override configuration using environment variables:

```bash
export MALTI_API_KEY="your-api-key"
export MALTI_URL="https://your-malti-server.com"
export MALTI_NODE="production-node-1"
export MALTI_BATCH_SIZE="1000"
export MALTI_BATCH_INTERVAL="30"

mvn quarkus:dev
```

## Load Testing

### Using curl for Basic Load Testing

```bash
# Generate multiple requests
for i in {1..100}; do
  curl -s http://localhost:8080/api/demo/hello > /dev/null &
done
wait

# Check telemetry stats after load
curl http://localhost:8080/api/telemetry/stats
```

### Using Apache Bench (ab)

```bash
# Install ab (if not already installed)
# Ubuntu/Debian: sudo apt-get install apache2-utils
# macOS: brew install httpie

# Run load test
ab -n 1000 -c 10 http://localhost:8080/api/demo/hello

# Test different endpoints
ab -n 500 -c 5 http://localhost:8080/api/demo/items
ab -n 200 -c 2 http://localhost:8080/api/demo/random
```

## Monitoring Telemetry Data

### Real-time Monitoring

You can monitor telemetry statistics in real-time:

```bash
# Watch telemetry stats (requires 'watch' command)
watch -n 2 'curl -s http://localhost:8080/api/telemetry/stats | jq .bufferStats'

# Or use a simple loop
while true; do
  echo "=== $(date) ==="
  curl -s http://localhost:8080/api/telemetry/stats | jq .bufferStats.currentSize
  sleep 5
done
```

### Health Monitoring

```bash
# Check health status
curl http://localhost:8080/api/telemetry/health

# Monitor buffer utilization
curl -s http://localhost:8080/api/telemetry/health | jq .bufferUtilization
```

## Troubleshooting

### Common Issues

1. **Extension not found**: Ensure the Malti telemetry extension is built and installed:
   ```bash
   cd ../malti-telemetry && mvn clean install
   ```

2. **Tests failing**: Make sure the application can start properly:
   ```bash
   mvn quarkus:dev
   ```

3. **Telemetry not working**: Check the logs for telemetry-related messages:
   ```bash
   mvn quarkus:dev | grep -i telemetry
   ```

4. **Port conflicts**: Change the port if 8080 is in use:
   ```bash
   mvn quarkus:dev -Dquarkus.http.port=8081
   ```

### Debug Mode

Enable debug logging for telemetry:

```properties
# Add to application.properties
quarkus.log.category."dev.muzy.malti.telemetry".level=DEBUG
```

### Viewing Application Logs

```bash
# Run with verbose logging
mvn quarkus:dev -X

# Filter telemetry logs
mvn quarkus:dev 2>&1 | grep -i telemetry
```

## Development

### Project Structure

```
src/
├── main/java/dev/muzy/malti/
│   ├── DemoResource.java          # Main demo endpoints
│   ├── TelemetryStatsResource.java # Telemetry statistics endpoints
│   └── HealthResource.java        # Health check endpoints
├── test/java/dev/muzy/malti/
│   ├── DemoResourceTest.java      # Unit tests for demo endpoints
│   ├── TelemetryStatsResourceTest.java # Tests for telemetry endpoints
│   ├── HealthResourceTest.java    # Tests for health endpoints
│   └── TelemetryDemoIT.java       # Integration tests
└── main/resources/
    └── application.properties     # Configuration
```

### Adding New Endpoints

1. Add new methods to `DemoResource.java`
2. Create corresponding tests in `DemoResourceTest.java`
3. Update this README with the new endpoint documentation

### Building for Production

```bash
# Build the application
mvn clean package

# Run the built JAR
java -jar target/quarkus-app/quarkus-run.jar

# Or build native executable (requires GraalVM)
mvn clean package -Pnative
./target/telemetry-demo-1.0.0-SNAPSHOT-runner
```

## Next Steps

1. **Deploy to Production**: Configure with real Malti server endpoints
2. **Add Custom Metrics**: Extend with application-specific telemetry
3. **Monitoring Integration**: Connect with monitoring tools like Prometheus/Grafana
4. **Performance Tuning**: Adjust batch sizes and intervals based on load patterns

## Support

For issues with the Malti Telemetry Extension, refer to the main project documentation in `../malti-telemetry/README.md`.