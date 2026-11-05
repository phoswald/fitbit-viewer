package com.github.phoswald.fitbit.viewer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.api.health.v4.api.DefaultApi;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Runs the generated client against a local stub, to check the URLs which are really sent.
 */
@QuarkusTest
class HealthApiReconcileTest {

    private static final String DAY_1 = """
            {"dailyVo2Max": {"date": {"year": 2026, "month": 3, "day": 1}, "vo2Max": 46.5}}""";

    private final List<String> requests = new ArrayList<>();

    private String responseBody = "{\"dataPoints\": [" + DAY_1 + "]}";

    private HttpServer server;
    private DefaultApi client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", this::handle);
        server.start();
        client = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:" + server.getAddress().getPort()))
                .build(DefaultApi.class);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void reconcile_singlePage_returnsDataPointsAndEncodesFilter() {
        var dataPoints = HealthApiHelpers.reconcile(client, "Bearer token", "users/me/dataTypes/daily-vo2-max", filter());

        assertEquals(List.of("/v4/users/me/dataTypes/daily-vo2-max/dataPoints:reconcile?"
                + "filter=daily_vo2_max.date%20%3E%3D%20%222026-03-01%22%20AND%20daily_vo2_max.date%20%3C%20%222026-04-01%22"), requests);
        assertEquals(1, dataPoints.size());
        assertEquals(46.5, dataPoints.get(0).getDailyVo2Max().getVo2Max().doubleValue());
    }

    @Test
    void reconcile_furtherPages_throws() {
        responseBody = "{\"dataPoints\": [" + DAY_1 + "], \"nextPageToken\": \"token\"}";

        assertThrows(IllegalStateException.class,
                () -> HealthApiHelpers.reconcile(client, "Bearer token", "users/me/dataTypes/daily-vo2-max", filter()));
    }

    private static String filter() {
        return HealthApiHelpers.dateFilter("daily_vo2_max.date", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));
    }

    private void handle(HttpExchange exchange) throws IOException {
        String rawUri = exchange.getRequestURI().getRawPath() + "?" + exchange.getRequestURI().getRawQuery();
        requests.add(rawUri);
        byte[] bytes = responseBody.getBytes(UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
