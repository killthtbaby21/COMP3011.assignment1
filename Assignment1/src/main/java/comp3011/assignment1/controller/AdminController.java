package comp3011.assignment1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.dto.UptimeResponse;
import comp3011.assignment1.service.ServerLifecycleService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final ServerLifecycleService serverLifecycleService;

    public AdminController(
            ServerLifecycleService serverLifecycleService) {

        this.serverLifecycleService = serverLifecycleService;
    }

    @GetMapping("/uptime")
    public ResponseEntity<UptimeResponse> getServerUptime() {

        return ResponseEntity.ok(
                serverLifecycleService.getUptime()
        );
    }
}