package comp3011.assignment1.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class BlockingHttpConcurrencyTest {

    private static final int REQUEST_COUNT = 225;

    @Autowired
    private MockMvc mockMvc;

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
                            MockMultipartFile audio =
                                    new MockMultipartFile(
                                            "audio",
                                            "test.wav",
                                            "audio/wav",
                                            "fake audio data".getBytes()
                                    );

                            return mockMvc.perform(
                                            multipart("/api/v1/transcribe")
                                                    .file(audio)
                                    )
                                    .andExpect(status().isOk())
                                    .andReturn()
                                    .getResponse()
                                    .getStatus();

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