package comp3011.assignment1.service;

import java.io.IOException;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("!local")
public class OpenAITranscriptionService implements TranscriptionService {

    private static final String API_KEY_ENV = "OPENAI_API_KEY";
    private static final String TRANSCRIPTION_URL =
            "https://api.openai.com/v1/audio/transcriptions";

    private final RestClient restClient;
    private final StatisticsService statisticsService;

    public OpenAITranscriptionService(
            RestClient.Builder restClientBuilder,
            StatisticsService statisticsService) {

        this.restClient = restClientBuilder.build();
        this.statisticsService = statisticsService;
    }

    @Override
    public String transcribe(MultipartFile audio) throws IOException {

        String apiKey = getApiKey();

         /*
         * I used ChatGPT to help me understand how to put the uploaded audio
         * into the multipart request. I tested this part with the transcription
         * endpoint after making the changes.
         */
        
        ByteArrayResource audioResource =
                new ByteArrayResource(audio.getBytes()) {
                    @Override
                    public String getFilename() {
                        String filename = audio.getOriginalFilename();
                        return filename != null ? filename : "audio.webm";
                    }
                };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", audioResource);
        body.add("model", "gpt-4o-mini-transcribe");

        TranscriptionResponse response;

        try {
            response = restClient.post()
                    .uri(TRANSCRIPTION_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(TranscriptionResponse.class);

        } catch (RestClientResponseException e) {
            System.err.println("OpenAI API request failed.");
            System.err.println("Status: " + e.getStatusCode());
            System.err.println("Response: " + e.getResponseBodyAsString());

            throw new IllegalStateException(
                    "Cloud transcription service request failed.", e);
        }

        if (response == null || response.text() == null) {
            throw new IllegalStateException(
                    "Cloud transcription service returned an empty response.");
        }

        if (response.usage() != null) {
            statisticsService.addTokenUsage(
                    response.usage().input_tokens(),
                    response.usage().output_tokens());
        }

        return response.text();
    }

    private String getApiKey() {

        String apiKey = System.getenv(API_KEY_ENV);

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY environment variable is not configured.");
        }

        return apiKey;
    }

    private record TranscriptionResponse(
            String text,
            Usage usage) {
    }

    private record Usage(
            long input_tokens,
            long output_tokens) {
    }
}