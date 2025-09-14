# Malti Telemetry

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Malti Telemetry** is a comprehensive observability solution designed to collect, process, and analyze HTTP request telemetry data from web applications and APIs. The project provides a unified approach to application monitoring with support for multiple frameworks and languages.

## 🏗️ Architecture

Malti Telemetry consists of several key components:

### Core Components
- **Telemetry Collector**: Captures HTTP request data with rich context
- **Batch Sender**: Efficiently sends telemetry data to Malti server with retry logic
- **Buffer Management**: Thread-safe buffering with overflow protection
- **Middleware Integration**: Framework-specific middleware for seamless integration

### Data Flow

```
HTTP Request → Middleware → Telemetry Collector → Buffer → Batch Sender → Malti Server
```

## 📦 Available Implementations

### Python (Starlette) - Production Ready

![PyPI](https://img.shields.io/pypi/v/malti-telemetry) ![Python](https://img.shields.io/pypi/pyversions/malti-telemetry) ![PyPI - Downloads](https://img.shields.io/pypi/dm/malti-telemetry)

The current implementation provides comprehensive support for Python web frameworks:

**Location**: [`python-starlette/`](./python-starlette/)

**Supported Frameworks**:
- **FastAPI**: Native FastAPI middleware with automatic route pattern extraction
- **Starlette**: Base Starlette middleware with full ASGI support
- **Responder**: Compatible with Responder framework
- **Any ASGI Framework**: Generic middleware for custom implementations

**Features**:
- Automatic lifespan management
- Route pattern extraction
- Consumer identification from headers and query parameters
- Context propagation support
- Type-safe implementation with full mypy support

**Requirements**:
- Python 3.11+ (for current implementation)
- Access to a Malti server instance

**Installation (Dev Version)**:
```bash
cd python-starlette
pip install -e .
```

**Installation (Production)**
```bash
pip install malti-telemetry
```

#### Environment Configuration

```bash
# Required
export MALTI_SERVICE_NAME="my-service"
export MALTI_API_KEY="your-api-key"
export MALTI_URL="https://your-malti-server.com"

# Optional
export MALTI_NODE="production-01"
export MALTI_BATCH_SIZE="500"
export MALTI_BATCH_INTERVAL="60"
```

#### Sample Usage

```python
from fastapi import FastAPI
from malti_telemetry.middleware import MaltiMiddleware

app = FastAPI()
app.add_middleware(MaltiMiddleware)

@app.get("/users/{user_id}")
async def get_user(user_id: int):
    return {"user_id": user_id, "name": "John Doe"}
```

### Java (Quarkus) - ⚠️ Experimental

![Experimental](https://img.shields.io/badge/Status-Experimental-orange) ![Not on Maven Central](https://img.shields.io/badge/Maven%20Central-Not%20Published-red)

**🚨 EXPERIMENTAL IMPLEMENTATION** - This is an early-stage implementation for evaluation and testing purposes.

**Location**: [`java-quarkus/`](./java-quarkus/)

**⚠️ Important Notes**:
- **Not published to Maven Central** - requires manual build and installation
- **Experimental status** - API may change without notice
- **Limited testing** - suitable for development and evaluation only
- **No production support** - use at your own risk

**What's Included**:
- **Quarkus Extension**: [`java-quarkus/malti-telemetry/`](./java-quarkus/malti-telemetry/) - Reusable Quarkus extension
- **Demo Application**: [`java-quarkus/telemetry-demo/`](./java-quarkus/telemetry-demo/) - Working example

**Features**:
- Automatic JAX-RS request/response interception
- Route template extraction (e.g., `/users/{userId}`)
- Reactive, non-blocking telemetry collection
- Thread-safe buffer management with overflow protection
- Configurable batching with exponential backoff retry logic
- Consumer identification from multiple header formats
- Type-safe configuration with Quarkus ConfigMapping

**Requirements**:
- Java 21+
- Maven 3.8.1+
- Quarkus 3.26.3+

**Installation** (Manual Build Required):
```bash
# 1. Build the extension locally
cd java-quarkus/malti-telemetry
mvn clean install

# 2. Add to your project's pom.xml
<dependency>
    <groupId>dev.muzy.malti</groupId>
    <artifactId>malti-telemetry-quarkus</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

# 3. Configure in application.properties
malti.api-key=your-api-key
malti.service-name=my-service
malti.url=http://localhost:8000
```

#### Quick Demo

```bash
# Build extension
cd java-quarkus/malti-telemetry && mvn clean install

# Run demo
cd ../telemetry-demo && mvn quarkus:dev

# Test endpoints
curl http://localhost:8080/hello/stats
```

#### Sample Usage

```java
// Zero configuration required - telemetry is automatic!

@Path("/api")
public class UserResource {
    
    @GET
    @Path("/users/{userId}")
    public User getUser(@PathParam("userId") Long userId) {
        // Telemetry automatically captures:
        // - Method: GET
        // - Endpoint: /api/users/{userId}  (templated)
        // - Status: 200
        // - Response time: 45ms
        // - Consumer: from X-Consumer-Id header
        return userService.findById(userId);
    }
}
```

#### Optional Programmatic Access

```java
@Inject
TelemetryService telemetryService;

@GET
@Path("/stats")
public TelemetryStats getStats() {
    return telemetryService.getStats();
}
```

## 📊 Telemetry Data

Malti Telemetry collects comprehensive HTTP request data:

### Standard Fields
- **Service**: Application/service identifier
- **Method**: HTTP method (GET, POST, PUT, DELETE, etc.)
- **Endpoint**: Request endpoint/path
- **Status**: HTTP response status code
- **Response Time**: Request processing time in milliseconds
- **Consumer**: API consumer identifier
- **Node**: Server node identifier
- **Timestamp**: Request timestamp with timezone

## 🔧 Configuration

Configuration varies by implementation. See specific implementation documentation for details.

### Python (Environment Variables)

```bash
# Server Configuration
MALTI_API_KEY="your-api-key"          # Required: Authentication key
MALTI_URL="http://localhost:8080"     # Required: Malti server URL

# Service Identification
MALTI_SERVICE_NAME="my-api"           # Service identifier
MALTI_NODE="web-01"                   # Node/server identifier

# Batching Configuration
MALTI_BATCH_SIZE="500"                # Records per batch
MALTI_BATCH_INTERVAL="60"             # Batch send interval (seconds)
MALTI_MAX_RETRIES="3"                 # Retry attempts
MALTI_RETRY_DELAY="1.0"               # Retry delay (seconds)

# Performance Tuning
MALTI_HTTP_TIMEOUT="30.0"             # HTTP timeout (seconds)
MALTI_MAX_CONNECTIONS="10"            # Max HTTP connections
MALTI_MAX_KEEPALIVE_CONNECTIONS="5"   # Max keep-alive connections

# Data Management
MALTI_OVERFLOW_THRESHOLD_PERCENT="90" # Buffer overflow threshold
MALTI_CLEAN_MODE="true"               # Enable clean mode filtering (prevents bot request logging)
```

### Java Quarkus (application.properties)

```properties
# Server Configuration
malti.api-key=your-api-key
malti.url=http://localhost:8000

# Service Identification
malti.service-name=my-service
malti.node=web-01

# Batching Configuration
malti.batch.size=500
malti.batch.interval-seconds=60
malti.batch.max-retries=3
malti.batch.retry-delay-seconds=5

# Performance Tuning
malti.http.timeout-seconds=15
malti.http.max-connections=10
malti.http.max-keepalive-connections=5

# Data Management
malti.overflow-threshold-percent=90.0
malti.clean-mode=true

# REST Client configuration (automatic)
quarkus.rest-client.malti-api.url=${malti.url}
quarkus.rest-client.malti-api.connect-timeout=${malti.http.timeout-seconds}000
quarkus.rest-client.malti-api.read-timeout=${malti.http.timeout-seconds}000
```

### Consumer Identification

Malti Telemetry automatically identifies API consumers using multiple strategies:

1. **Header-based**: `x-consumer-id`, `x-user-id`, `consumer-id`, `user-id`
2. **Query parameters**: `consumer_id`, `user_id`
3. **Request state**: Framework-specific context (FastAPI state, etc.)
4. **Fallback**: Uses "anonymous" for unidentified consumers

### Clean Mode

Clean mode automatically filters out common bot traffic and irrelevant requests:
- 401 Unauthorized responses
- 404 Not Found responses

## 🤝 Contributing

We welcome contributions to Malti Telemetry! The project is designed to be extensible and community-driven.

### Development Guidelines
- Follow language-specific conventions
- Include comprehensive tests
- Update documentation
- Maintain type safety
- Follow security best practices

### Implementation Templates
Each new integration should include:
- Framework-specific middleware
- Comprehensive test suite
- Configuration documentation
- Usage examples
- Performance benchmarks

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](python-starlette/LICENSE) file for details.
