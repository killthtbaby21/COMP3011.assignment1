package comp3011.assignment1.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;

import comp3011.assignment1.dto.UptimeResponse;

@Service
public class ServerLifecycleService {

    private final Instant serverStart;

    public ServerLifecycleService() {
        this.serverStart = Instant.now();
    }

    public UptimeResponse getUptime() {
        Instant now = Instant.now();

        Duration uptime = Duration.between(serverStart, now);

        double uptimeSeconds =
                uptime.getSeconds()
                + uptime.getNano() / 1_000_000_000.0;

        return new UptimeResponse(
                serverStart,
                now,
                uptimeSeconds
        );
    }
}