package comp3011.assignment1.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import comp3011.assignment1.dto.GlobalStatsResponse;

//I asked ChatGPT how to make the token counters safe when multiple
//requests update them at the same time. It suggested AtomicLong.
//I checked how addAndGet works and used it here instead of a normal long.
@Service
public class StatisticsService {

    private final AtomicLong inputTokens = new AtomicLong(0);
    private final AtomicLong outputTokens = new AtomicLong(0);

    public GlobalStatsResponse getGlobalStats() {
        return new GlobalStatsResponse(
                inputTokens.get(),
                outputTokens.get()
        );
    }

    public void addTokenUsage(long input, long output) {
        inputTokens.addAndGet(input);
        outputTokens.addAndGet(output);
    }
}