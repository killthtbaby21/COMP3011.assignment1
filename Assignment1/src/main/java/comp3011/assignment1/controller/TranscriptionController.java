package comp3011.assignment1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class TranscriptionController {

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribe(
            @RequestParam("audio") MultipartFile audio) {

        if (audio.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Audio file is empty.");
        }

        return ResponseEntity.ok(
                "Received audio file: "
                + audio.getOriginalFilename()
        );
    }
}