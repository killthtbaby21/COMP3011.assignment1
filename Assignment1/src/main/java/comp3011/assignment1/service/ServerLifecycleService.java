package comp3011.assignment1.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import comp3011.assignment1.dto.UptimeResponse;

@Service
public class ServerLifecycleService {

    private final Instant serverStart;
    private final AtomicBoolean shutdownRequested =
            new AtomicBoolean(false);

    private final ConfigurableApplicationContext context;

    public ServerLifecycleService(
            ConfigurableApplicationContext context) {

        this.context = context;
        this.serverStart = Instant.now();
    }

    public UptimeResponse getUptime() {

        Instant now = Instant.now();

        Duration uptime =
                Duration.between(serverStart, now);

        double uptimeSeconds =
                uptime.getSeconds()
                + uptime.getNano() / 1_000_000_000.0;

        return new UptimeResponse(
                serverStart,
                now,
                uptimeSeconds
        );
    }

    public boolean requestShutdown() {
        return shutdownRequested.compareAndSet(
                false,
                true
        );
    }

 // I used ChatGPT to help with the shutdown part because closing the
 // application immediately could stop the HTTP response from being sent.
    public void performShutdown() {
        Thread shutdownThread = new Thread(() -> {
            try {
                Thread.sleep(500);
                context.close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        shutdownThread.start();
    }
}