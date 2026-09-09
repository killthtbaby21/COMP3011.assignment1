package comp3011.assignment1.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import comp3011.assignment1.service.StatisticsService;

class StatsControllerTest {

    @Test
    void getGlobalStatsReturnsCurrentTokenCounts() throws Exception {

        StatisticsService statisticsService =
                new StatisticsService();

        statisticsService.addTokenUsage(100, 25);

        StatsController controller =
                new StatsController(statisticsService);

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        mockMvc.perform(
                get("/api/v1/global/stats")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.inputTokens").value(100))
        .andExpect(jsonPath("$.outputTokens").value(25));
    }
}