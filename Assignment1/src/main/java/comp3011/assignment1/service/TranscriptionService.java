package comp3011.assignment1.service;

import java.io.IOException;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {

    private static final String API_KEY_ENV = "OPENAI_API_KEY";

    private static final String TRANSCRIPTION_URL =
            "https://api.openai.com/v1/audio/transcriptions";

    private final RestClient restClient;

    public TranscriptionService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String transcribe(MultipartFile audio) throws IOException {

        String apiKey = getApiKey();

        ByteArrayResource audioResource =
                new ByteArrayResource(audio.getBytes()) {
                    @Override
                    public String getFilename() {
                        String filename = audio.getOriginalFilename();
                        return filename != null ? filename : "audio.webm";
                    }
                };

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add("file", audioResource);
        body.add("model", "gpt-4o-mini-transcribe");

        TranscriptionResponse response = restClient.post()
                .uri(TRANSCRIPTION_URL)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(TranscriptionResponse.class);

        if (response == null || response.text() == null) {
            throw new IllegalStateException(
                    "Cloud transcription service returned an empty response."
            );
        }

        return response.text();
    }

    private String getApiKey() {

        String apiKey = System.getenv(API_KEY_ENV);

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OPENAI_API_KEY environment variable is not configured."
            );
        }

        return apiKey;
    }

    private record TranscriptionResponse(String text) {
    }
}