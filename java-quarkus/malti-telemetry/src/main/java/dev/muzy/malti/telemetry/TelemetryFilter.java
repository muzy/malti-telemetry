package dev.muzy.malti.telemetry;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * Single telemetry filter that handles both request and response phases.
 * - On request: capture start time, extract context and consumer
 * - On response: compute duration, build route template, send telemetry asynchronously
 */
@Provider
public class TelemetryFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME_PROPERTY = "malti.start.time";
    private static final String CONTEXT_PROPERTY = "malti.context";
    private static final String CONSUMER_PROPERTY = "malti.consumer";

    @Inject
    TelemetryService telemetryService;

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Record start time
        requestContext.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());

        // Extract and store context
        String context = extractContext(requestContext);
        if (context != null) {
            requestContext.setProperty(CONTEXT_PROPERTY, context);
        }

        // Extract and store consumer
        String consumer = extractConsumer(requestContext);
        requestContext.setProperty(CONSUMER_PROPERTY, consumer);

        Log.debugf("Request started: %s %s, consumer: %s, context: %s",
                requestContext.getMethod(), requestContext.getUriInfo().getPath(), consumer, context);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        try {
            Long start = (Long) requestContext.getProperty(START_TIME_PROPERTY);
            if (start == null) {
                return;
            }

            int durationMs = (int) (System.currentTimeMillis() - start);
            String method = requestContext.getMethod();
            int status = responseContext.getStatus();

            // Build templated route e.g. /users/{userid}
            String endpoint = buildRouteTemplate(requestContext.getUriInfo());

            String context = (String) requestContext.getProperty(CONTEXT_PROPERTY);
            String consumer = (String) requestContext.getProperty(CONSUMER_PROPERTY);

            telemetryService.recordRequest(method, endpoint, status, durationMs, consumer, context);
        } catch (Exception e) {
            Log.debugf("Telemetry recording failed: %s", e.getMessage());
        }
    }

    private String extractContext(ContainerRequestContext requestContext) {
        String context = requestContext.getHeaderString("X-Malti-Context");
        if (context != null && !context.trim().isEmpty()) {
            return context.trim();
        }
        return null;
    }

    private String extractConsumer(ContainerRequestContext requestContext) {
        String v = requestContext.getHeaderString("X-Consumer-Id");
        if (v != null && !v.isEmpty()) return v.trim();
        v = requestContext.getHeaderString("X-User-Id");
        if (v != null && !v.isEmpty()) return v.trim();
        v = requestContext.getHeaderString("Consumer-Id");
        if (v != null && !v.isEmpty()) return v.trim();
        v = requestContext.getHeaderString("User-Id");
        if (v != null && !v.isEmpty()) return v.trim();
        return "";
    }

    /**
     * Build route template from ResourceInfo @Path annotations and matched URIs.
     * This reconstructs the template path like /users/{userid} from the JAX-RS resource annotations.
     */
    private String buildRouteTemplate(UriInfo uriInfo) {
        try {
            // Reconstruct from ResourceInfo annotations (most reliable approach)
            String classPath = null;
            String methodPath = null;
            
            if (resourceInfo != null && resourceInfo.getResourceClass() != null) {
                jakarta.ws.rs.Path cp = resourceInfo.getResourceClass().getAnnotation(jakarta.ws.rs.Path.class);
                if (cp != null) classPath = cp.value();
            }
            
            if (resourceInfo != null && resourceInfo.getResourceMethod() != null) {
                jakarta.ws.rs.Path mp = resourceInfo.getResourceMethod().getAnnotation(jakarta.ws.rs.Path.class);
                if (mp != null) methodPath = mp.value();
            }
            
            // Build the complete template path
            StringBuilder sb = new StringBuilder();
            if (classPath != null && !classPath.isBlank()) {
                if (!classPath.startsWith("/")) sb.append("/");
                sb.append(classPath);
            }
            if (methodPath != null && !methodPath.isBlank()) {
                if (!methodPath.startsWith("/") && sb.length() > 0) sb.append("/");
                if (methodPath.startsWith("/") && sb.toString().endsWith("/")) {
                    methodPath = methodPath.substring(1);
                }
                sb.append(methodPath);
            }
            
            String template = sb.toString();
            if (!template.isEmpty()) {
                return template.startsWith("/") ? template : "/" + template;
            }
            
        } catch (Exception e) {
            Log.debugf("Error building route template: %s", e.getMessage());
        }

        // Fallback to actual path
        String path = uriInfo.getPath();
        return path.startsWith("/") ? path : "/" + path;
    }
}
