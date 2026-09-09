package comp3011.assignment1.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import comp3011.assignment1.dto.UptimeResponse;
import comp3011.assignment1.service.ServerLifecycleService;
import comp3011.assignment1.exception.GlobalExceptionHandler;

import static org.mockito.Mockito.mock;

class AdminControllerTest {

    private ServerLifecycleService serverLifecycleService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        serverLifecycleService = mock(ServerLifecycleService.class);

        AdminController controller =
                new AdminController(serverLifecycleService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getUptimeReturnsServerUptime() throws Exception {

        Instant serverStart =
                Instant.parse("2026-09-09T10:00:00Z");

        Instant now =
                Instant.parse("2026-09-09T10:01:30Z");

        UptimeResponse response =
                new UptimeResponse(
                        serverStart,
                        now,
                        90.0
                );

        when(serverLifecycleService.getUptime())
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/uptime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utcServerStart")
                        .value("2026-09-09T10:00:00Z"))
                .andExpect(jsonPath("$.utcNow")
                        .value("2026-09-09T10:01:30Z"))
                .andExpect(jsonPath("$.serverUptimeSeconds")
                        .value(90.0));
    }
    
    @Test
    void shutdownReturnsAcceptedWhenRequestIsAccepted() throws Exception {

        when(serverLifecycleService.requestShutdown())
                .thenReturn(true);

        mockMvc.perform(post("/api/v1/admin/shutdown"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message")
                        .value("Graceful shutdown requested."));

        verify(serverLifecycleService).performShutdown();
    }
    
    @Test
    void shutdownReturnsConflictWhenShutdownAlreadyInProgress() throws Exception {

        when(serverLifecycleService.requestShutdown())
                .thenReturn(false);

        mockMvc.perform(post("/api/v1/admin/shutdown"))
                .andExpect(status().isConflict());
    }
    
}