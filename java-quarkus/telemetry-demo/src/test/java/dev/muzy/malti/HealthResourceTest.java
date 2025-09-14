package dev.muzy.malti;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class HealthResourceTest {

    @Test
    public void testHealthEndpoint() {
        given()
            .when()
            .get("/health")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("UP"))
            .body("service", equalTo("telemetry-demo"))
            .body("version", equalTo("1.0.0-SNAPSHOT"))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testReadinessEndpoint() {
        given()
            .when()
            .get("/health/ready")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("READY"))
            .body("service", equalTo("telemetry-demo"))
            .body("timestamp", notNullValue());
    }

    @Test
    public void testLivenessEndpoint() {
        given()
            .when()
            .get("/health/live")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("ALIVE"))
            .body("service", equalTo("telemetry-demo"))
            .body("timestamp", notNullValue());
    }
}
