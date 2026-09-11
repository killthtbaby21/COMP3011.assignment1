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
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
/*
 * I used ChatGPT to help me build the first version of this test.
 * I changed it later to send real HTTP requests and added the retry
 * after I had connection errors when testing it.
 */
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

        	                return sendWithRetry(request);

        	            } catch (Exception e) {
        	                throw new RuntimeException(e);
        	            }
        	        }, executor);

            futures.add(future);
        }

        // Wait until all requests finish.
        CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        ).get(60, TimeUnit.SECONDS);

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

     // 225 requests would take about 450 seconds if they were handled one by one.
     // The test should finish well below that when requests are handled concurrently.
        assertTrue(elapsedTime < 60000);
    }

    private int sendWithRetry(HttpRequest request) throws Exception {

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                HttpResponse<String> response =
                        httpClient.send(
                                request,
                                HttpResponse.BodyHandlers.ofString());

                return response.statusCode();

            } catch (ConnectException e) {
                if (attempt == 3) {
                    throw e;
                }

             // Some connections can fail temporarily when all 225 requests start together,
             // so retry the connection a few times before failing the test.
                Thread.sleep(100);
            }
        }

        throw new IllegalStateException(
                "Request could not be completed.");
    }
}