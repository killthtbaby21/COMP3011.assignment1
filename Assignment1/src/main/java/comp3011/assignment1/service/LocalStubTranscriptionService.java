package comp3011.assignment1.service;

import java.io.IOException;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("local")
public class LocalStubTranscriptionService implements TranscriptionService {

    @Override
    public String transcribe(MultipartFile audio) throws IOException {

        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("Audio file must not be empty.");
        }

        // Simulate the latency of a real cloud transcription request.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Local transcription was interrupted.", e);
        }

        return "Local placeholder transcription.";
    }
}  