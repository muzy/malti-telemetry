# Malti Telemetry Quarkus Extension

A Quarkus extension for integrating with the Malti telemetry system. This extension automatically captures HTTP request metrics and sends them to a Malti server for analysis and monitoring.

## Features

- Automatic HTTP request/response telemetry collection
- Configurable batching and buffering
- Retry logic with exponential backoff
- Thread-safe buffer management
- Clean mode to ignore certain status codes
- Overflow protection with configurable thresholds

## ⚠️ Important Note

**This extension is NOT available on Maven Central.** You need to build and install it manually before using it in your projects.

## Building the Extension

### Prerequisites
- Java 21 or higher
- Maven 3.8.1 or higher

### Build and Install Locally

1. **Clone or download this repository**
2. **Navigate to the extension directory:**
   ```bash
   cd java-quarkus/malti-telemetry
   ```

3. **Build and install to your local Maven repository:**
   ```bash
   mvn clean install
   ```

   This will:
   - Compile the extension
   - Run all tests
   - Install the artifact to your local `~/.m2/repository`

4. **Verify the installation:**
   ```bash
   ls ~/.m2/repository/dev/muzy/malti/malti-telemetry-quarkus/1.0.0-SNAPSHOT/
   ```

## Installation in Your Project

After building and installing the extension locally, add the dependency to your Quarkus project's `pom.xml`:

```xml
<dependency>
    <groupId>dev.muzy.malti</groupId>
    <artifactId>malti-telemetry-quarkus</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**Note:** The extension will only be available to projects on the same machine where you ran `mvn clean install`.

### For Team Development

If you're working in a team or CI/CD environment, you have several options:

1. **Each developer builds locally:**
   - Every team member runs `mvn clean install` on their machine
   - Simple but requires coordination

2. **Use a local Maven repository server:**
   - Set up Nexus, Artifactory, or similar
   - Deploy the extension to your internal repository
   - Configure your projects to use the internal repository

3. **Include as a Git submodule:**
   - Add this extension as a Git submodule to your project
   - Build as part of your project's build process

## Configuration

Configure the extension in your `application.properties`:

```properties
# Required: API key for authentication
malti.api-key=your-api-key-here

# Optional: Service name (defaults to "unknown-service")
malti.service-name=my-service

# Optional: Malti server URL (defaults to "http://localhost:8000")
malti.url=https://your-malti-server.com

# Optional: Node identifier (defaults to "unknown-node")
malti.node=node-1

# Optional: Clean mode - ignore 401/404 responses (defaults to true)
malti.clean-mode=true

# Optional: Overflow threshold percentage (defaults to 90.0)
malti.overflow-threshold-percent=90.0

# Batch configuration
malti.batch.size=500
malti.batch.interval-seconds=60
malti.batch.max-retries=3
malti.batch.retry-delay-seconds=5

# HTTP client configuration
malti.http.timeout-seconds=15
malti.http.max-keepalive-connections=5
malti.http.max-connections=10
```

## Usage

### Quick Start

1. **Follow the build and installation steps above**
2. **Add the dependency to your Quarkus project**
3. **Configure the extension in your `application.properties`:**
   ```properties
   malti.api-key=your-api-key
   malti.service-name=my-service
   malti.url=http://localhost:8000
   ```
4. **Start your Quarkus application:**
   ```bash
   mvn quarkus:dev
   ```
5. **Verify the extension is loaded** - you should see logs like:
   ```
   INFO  [io.qua.dep.QuarkusAugmentor] Quarkus augmentation completed in 1234ms
   ```

### Automatic Operation

Once configured, the extension automatically:

1. Intercepts all HTTP requests to your Quarkus application
2. Collects metrics including method, endpoint, status code, response time, and consumer information
3. Buffers the telemetry data
4. Periodically sends batches to the configured Malti server
5. Handles retries and error scenarios

### Consumer Identification

The extension looks for consumer identification in the following headers (in order of precedence):

1. `X-Consumer-Id`
2. `X-User-Id`
3. `Consumer-Id`
4. `User-Id`

### Context Information

You can provide additional context by including the `X-Malti-Context` header in your requests.

## Components

- **TelemetryFilter**: JAX-RS filter that captures request/response data
- **TelemetryService**: Main service for processing and sending telemetry
- **TelemetryBuffer**: Thread-safe buffer for storing telemetry records
- **MaltiClient**: REST client for communicating with the Malti server
- **TelemetryConfig**: Type-safe configuration mapping

## Statistics

You can access telemetry statistics by injecting the `TelemetryService`:

```java
@Inject
TelemetryService telemetryService;

public void getStats() {
    TelemetryBuffer.BufferStats stats = telemetryService.getStats();
    // Use stats...
}
```

## Troubleshooting

### Common Issues

**"Could not find artifact dev.muzy.malti:malti-telemetry-quarkus"**
- Make sure you've run `mvn clean install` in the extension directory
- Check that the artifact exists in `~/.m2/repository/dev/muzy/malti/malti-telemetry-quarkus/`
- Ensure you're using the exact groupId and artifactId shown above

**Build fails with compilation errors**
- Ensure you have Java 21 or higher installed
- Check that Maven 3.8.1+ is being used
- Try `mvn clean` first, then `mvn install`

**Extension not being picked up by Quarkus**
- Verify the dependency is in your project's `pom.xml`
- Check that Quarkus can find the extension: `mvn quarkus:list-extensions`
- Look for the extension in your application startup logs

**Tests failing during build**
- This indicates a problem with the extension code
- Check the test output for specific failures
- Ensure all dependencies are properly resolved

## Development

To build the library:

```bash
mvn clean compile
```

To run tests:

```bash
mvn test
```

To install locally:

```bash
mvn clean install
```
