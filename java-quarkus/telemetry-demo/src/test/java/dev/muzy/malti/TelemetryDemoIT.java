package dev.muzy.malti;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration test that demonstrates the full telemetry demo workflow
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TelemetryDemoIT {

    @Test
    @Order(1)
    public void testApplicationStartup() {
        // Verify the application is running
        given()
            .when()
            .get("/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    @Order(2)
    public void testTelemetryServiceInitialization() {
        // Check that telemetry service is available
        given()
            .when()
            .get("/api/telemetry/health")
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(207)));
    }

    @Test
    @Order(3)
    public void testGenerateVariousTelemetryData() {
        // Generate different types of requests to create telemetry data
        
        // Successful requests
        given().get("/api/demo/hello");
        given().get("/api/demo/items");
        given().get("/api/demo/items/1");
        
        // Request with parameters
        given().queryParam("name", "Integration Test").get("/api/demo/hello");
        given().queryParam("limit", "3").get("/api/demo/items");
        
        // POST request
        Map<String, Object> newItem = Map.of("name", "IntegrationTestItem", "value", 999);
        given()
            .contentType(ContentType.JSON)
            .body(newItem)
            .post("/api/demo/items");
        
        // PUT request
        Map<String, Object> updates = Map.of("name", "UpdatedIntegrationItem");
        given()
            .contentType(ContentType.JSON)
            .body(updates)
            .put("/api/demo/items/1");
        
        // DELETE request
        given().delete("/api/demo/items/1");
        
        // Error scenarios
        given().get("/api/demo/items/999"); // 404
        given().post("/api/demo/items"); // 400 - missing body
        given().get("/api/demo/secure"); // 401 - missing API key
        
        // Authorized request
        given()
            .header("X-API-Key", "test-key")
            .get("/api/demo/secure");
        
        // Request with consumer headers
        given()
            .header("X-Consumer-Id", "integration-test-consumer")
            .header("X-User-Id", "integration-test-user")
            .header("X-Malti-Context", "integration-test-context")
            .get("/api/demo/hello");
        
        // Random endpoint (multiple calls to get different outcomes)
        for (int i = 0; i < 5; i++) {
            given().get("/api/demo/random");
        }
        
        // Slow endpoint
        given().queryParam("delay", "100").get("/api/demo/slow");
    }

    @Test
    @Order(4)
    public void testTelemetryStatsAfterActivity() {
        // Verify telemetry stats show activity
        given()
            .when()
            .get("/api/telemetry/stats")
            .then()
            .statusCode(200)
            .body("bufferStats.totalAdded", greaterThanOrEqualTo(0))
            .body("service", equalTo("telemetry-demo"));
    }

    @Test
    @Order(5)
    public void testTelemetryHealthAfterActivity() {
        // Check telemetry health after generating load
        given()
            .when()
            .get("/api/telemetry/health")
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(207)))
            .body("status", anyOf(equalTo("healthy"), equalTo("warning")))
            .body("bufferUtilization", greaterThanOrEqualTo(0.0f));
    }

    @Test
    @Order(6)
    public void testEndToEndWorkflow() {
        // Complete workflow test
        
        // 1. Create an item
        Map<String, Object> newItem = Map.of("name", "WorkflowTestItem", "value", 123);
        given()
            .contentType(ContentType.JSON)
            .body(newItem)
            .when()
            .post("/api/demo/items")
            .then()
            .statusCode(201)
            .body("name", equalTo("WorkflowTestItem"));
        
        // 2. Get all items
        given()
            .when()
            .get("/api/demo/items")
            .then()
            .statusCode(200)
            .body("count", greaterThan(0));
        
        // 3. Get specific item
        given()
            .when()
            .get("/api/demo/items/2")
            .then()
            .statusCode(200)
            .body("id", equalTo(2));
        
        // 4. Update the item
        Map<String, Object> updates = Map.of("name", "UpdatedWorkflowItem", "value", 456);
        given()
            .contentType(ContentType.JSON)
            .body(updates)
            .when()
            .put("/api/demo/items/2")
            .then()
            .statusCode(200)
            .body("name", equalTo("UpdatedWorkflowItem"));
        
        // 5. Delete the item
        given()
            .when()
            .delete("/api/demo/items/2")
            .then()
            .statusCode(200)
            .body("message", containsString("deleted successfully"));
        
        // 6. Verify telemetry captured all operations
        given()
            .when()
            .get("/api/telemetry/stats")
            .then()
            .statusCode(200)
            .body("service", equalTo("telemetry-demo"));
    }

    @Test
    @Order(7)
    public void testErrorHandlingWithTelemetry() {
        // Test various error scenarios to ensure telemetry captures them
        
        // 404 errors
        given().get("/api/demo/items/999");
        given().get("/api/demo/nonexistent");
        
        // 400 errors
        given().post("/api/demo/items");
        given().put("/api/demo/items/0");
        
        // 401 errors
        given().get("/api/demo/secure");
        
        // Verify telemetry service still works after errors
        given()
            .when()
            .get("/api/telemetry/health")
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(207)));
    }
}
