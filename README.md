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

### Python (Starlette)
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

**Installation**:
```bash
cd python-starlette
pip install -e .
```

### 🚀 Getting Started

#### Prerequisites
- Python 3.11+ (for current implementation)
- Access to a Malti server instance

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

#### Sample code

```python
from fastapi import FastAPI
from malti_telemetry.middleware import MaltiMiddleware

app = FastAPI()
app.add_middleware(MaltiMiddleware)

@app.get("/users/{user_id}")
async def get_user(user_id: int):
    return {"user_id": user_id, "name": "John Doe"}
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

### Core Configuration
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
