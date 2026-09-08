package comp3011.assignment1.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import comp3011.assignment1.service.TranscriptionService;

class TranscriptionControllerTest {

    @Test
    void transcribeReturnsTextFromStubService() throws Exception {

        TranscriptionService stubService =
                audio -> "Regression test transcription.";

        TranscriptionController controller =
                new TranscriptionController(stubService);

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "test.webm",
                "audio/webm",
                "fake audio data".getBytes()
        );

        mockMvc.perform(
                multipart("/api/v1/transcribe")
                        .file(audio)
        )
        .andExpect(status().isOk())
        .andExpect(content().string(
                "Regression test transcription."
        ));
    }
}