package comp3011.assignment1.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class BlockingHttpConcurrencyTest {

    private static final int REQUEST_COUNT = 225;
    
    @LocalServerPort
    private int port;
    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    @Test
    void handles225ConcurrentBlockingRequests() throws Exception {

        ExecutorService executor =
                Executors.newFixedThreadPool(REQUEST_COUNT);

        List<CompletableFuture<Integer>> futures =
                new ArrayList<>();

        long startTime = System.currentTimeMillis();

        // Send 225 transcription requests concurrently.
        for (int i = 0; i < REQUEST_COUNT; i++) {

        	CompletableFuture<Integer> future =
        	        CompletableFuture.supplyAsync(() -> {
        	            try {
        	                String boundary = "TestBoundary";

        	                String body =
        	                        "--" + boundary + "\r\n"
        	                        + "Content-Disposition: form-data; name=\"audio\"; "
        	                        + "filename=\"test.wav\"\r\n"
        	                        + "Content-Type: audio/wav\r\n"
        	                        + "\r\n"
        	                        + "fake audio data\r\n"
        	                        + "--" + boundary + "--\r\n";

        	                HttpRequest request = HttpRequest.newBuilder()
        	                        .uri(URI.create(
        	                                "http://localhost:" + port
        	                                        + "/api/v1/transcribe"))
        	                        .header(
        	                                "Content-Type",
        	                                "multipart/form-data; boundary=" + boundary)
        	                        .POST(HttpRequest.BodyPublishers.ofString(
        	                                body,
        	                                StandardCharsets.UTF_8))
        	                        .build();

        	                HttpResponse<String> response =
        	                        httpClient.send(
        	                                request,
        	                                HttpResponse.BodyHandlers.ofString());

        	                return response.statusCode();

        	            } catch (Exception e) {
        	                throw new RuntimeException(e);
        	            }
        	        }, executor);

            futures.add(future);
        }

        // Wait until all requests finish.
        CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        ).get(30, TimeUnit.SECONDS);

        long elapsedTime =
                System.currentTimeMillis() - startTime;

        // Verify every request completed successfully.
        for (CompletableFuture<Integer> future : futures) {
            assertEquals(200, future.get());
        }

        executor.shutdown();

        System.out.println(
                "225 concurrent blocking requests completed in "
                        + elapsedTime + " ms"
        );

        // Sequential execution would take about 450 seconds.
        assertTrue(elapsedTime < 30000);
    }
}