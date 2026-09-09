package comp3011.assignment1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import comp3011.assignment1.dto.GlobalStatsResponse;

class StatisticsServiceConcurrencyTest {

    @Test
    void concurrentUpdatesDoNotLoseTokenCounts() throws Exception {

        StatisticsService statisticsService = new StatisticsService();

        int workers = 32;
        int updatesPerWorker = 20_000;

        ExecutorService executor = Executors.newFixedThreadPool(workers);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Runnable> tasks = new ArrayList<>();

        // Each worker updates the same counters concurrently.
        for (int i = 0; i < workers; i++) {
            tasks.add(() -> {
                try {
                    startLatch.await();

                    for (int j = 0; j < updatesPerWorker; j++) {
                        statisticsService.addTokenUsage(1, 1);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        for (Runnable task : tasks) {
            executor.submit(task);
        }

        // Release all workers at approximately the same time.
        startLatch.countDown();

        executor.shutdown();

        while (!executor.isTerminated()) {
            Thread.sleep(10);
        }

        GlobalStatsResponse result = statisticsService.getGlobalStats();

        long expected = (long) workers * updatesPerWorker;

        assertEquals(expected, result.inputTokens());
        assertEquals(expected, result.outputTokens());
    }
}