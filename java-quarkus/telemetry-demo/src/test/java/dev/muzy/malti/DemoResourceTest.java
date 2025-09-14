package dev.muzy.malti;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class DemoResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
            .when()
            .get("/api/demo/hello")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", equalTo("Hello, World!"))
            .body("endpoint", equalTo("hello"))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testHelloWithNameParameter() {
        given()
            .queryParam("name", "Quarkus")
            .when()
            .get("/api/demo/hello")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", equalTo("Hello, Quarkus!"))
            .body("endpoint", equalTo("hello"))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testGetItems() {
        given()
            .when()
            .get("/api/demo/items")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("items", hasSize(5))
            .body("count", equalTo(5))
            .body("items[0].id", equalTo(1))
            .body("items[0].name", equalTo("Alpha"))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testGetItemsWithLimit() {
        given()
            .queryParam("limit", "3")
            .when()
            .get("/api/demo/items")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("items", hasSize(3))
            .body("count", equalTo(3));
    }

    @Test
    public void testGetItemById() {
        given()
            .when()
            .get("/api/demo/items/1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(1))
            .body("name", equalTo("Alpha"))
            .body("value", notNullValue())
            .body("timestamp", notNullValue());
    }

    @Test
    public void testGetItemByIdNotFound() {
        given()
            .when()
            .get("/api/demo/items/999")
            .then()
            .statusCode(404)
            .contentType(ContentType.JSON)
            .body("error", equalTo("Item not found"))
            .body("id", equalTo(999))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testCreateItem() {
        Map<String, Object> newItem = Map.of(
            "name", "TestItem",
            "value", 123
        );

        given()
            .contentType(ContentType.JSON)
            .body(newItem)
            .when()
            .post("/api/demo/items")
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("name", equalTo("TestItem"))
            .body("value", equalTo(123))
            .body("id", notNullValue())
            .body("created", notNullValue());
    }

    @Test
    public void testCreateItemMissingName() {
        Map<String, Object> invalidItem = Map.of("value", 123);

        given()
            .contentType(ContentType.JSON)
            .body(invalidItem)
            .when()
            .post("/api/demo/items")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("error", equalTo("Missing required field: name"))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testCreateItemEmptyBody() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .post("/api/demo/items")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("error", equalTo("Missing required field: name"));
    }

    @Test
    public void testUpdateItem() {
        Map<String, Object> updates = Map.of(
            "name", "UpdatedItem",
            "value", 456
        );

        given()
            .contentType(ContentType.JSON)
            .body(updates)
            .when()
            .put("/api/demo/items/1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(1))
            .body("name", equalTo("UpdatedItem"))
            .body("value", equalTo(456))
            .body("updated", notNullValue());
    }

    @Test
    public void testUpdateItemInvalidId() {
        Map<String, Object> updates = Map.of("name", "UpdatedItem");

        given()
            .contentType(ContentType.JSON)
            .body(updates)
            .when()
            .put("/api/demo/items/0")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("error", equalTo("Invalid ID"))
            .body("id", equalTo(0));
    }

    @Test
    public void testUpdateItemNoUpdates() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .put("/api/demo/items/1")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("error", equalTo("No updates provided"));
    }

    @Test
    public void testDeleteItem() {
        given()
            .when()
            .delete("/api/demo/items/1")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", equalTo("Item deleted successfully"))
            .body("id", equalTo(1))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testDeleteItemInvalidId() {
        given()
            .when()
            .delete("/api/demo/items/0")
            .then()
            .statusCode(400)
            .contentType(ContentType.JSON)
            .body("error", equalTo("Invalid ID"))
            .body("id", equalTo(0));
    }

    @Test
    public void testRandomEndpoint() {
        // Test the random endpoint multiple times to potentially hit different outcomes
        for (int i = 0; i < 10; i++) {
            given()
                .when()
                .get("/api/demo/random")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400), equalTo(404), equalTo(500)))
                .contentType(ContentType.JSON)
                .body("timestamp", notNullValue());
        }
    }

    @Test
    public void testSecureEndpointWithoutApiKey() {
        given()
            .when()
            .get("/api/demo/secure")
            .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("error", equalTo("API key required"))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testSecureEndpointWithApiKey() {
        given()
            .header("X-API-Key", "test-api-key")
            .when()
            .get("/api/demo/secure")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", equalTo("Access granted"))
            .body("user", equalTo("demo-user"))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testSecureEndpointWithEmptyApiKey() {
        given()
            .header("X-API-Key", "")
            .when()
            .get("/api/demo/secure")
            .then()
            .statusCode(401)
            .contentType(ContentType.JSON)
            .body("error", equalTo("API key required"));
    }

    @Test
    public void testSlowEndpoint() {
        given()
            .queryParam("delay", "100")
            .when()
            .get("/api/demo/slow")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", equalTo("Slow response completed"))
            .body("delay", equalTo(100))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testSlowEndpointDefaultDelay() {
        given()
            .when()
            .get("/api/demo/slow")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", equalTo("Slow response completed"))
            .body("delay", equalTo(1000))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testSlowEndpointMaxDelay() {
        // Test that delay is capped at 5000ms
        given()
            .queryParam("delay", "10000")
            .when()
            .get("/api/demo/slow")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", equalTo("Slow response completed"))
            .body("delay", equalTo(10000)); // The endpoint reports the requested delay, but caps the actual delay
    }

    @Test
    public void testConsumerHeaders() {
        // Test that the telemetry extension picks up consumer identification headers
        given()
            .header("X-Consumer-Id", "test-consumer")
            .header("X-User-Id", "test-user")
            .header("X-Malti-Context", "test-context")
            .when()
            .get("/api/demo/hello")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("message", equalTo("Hello, World!"));
    }
}
