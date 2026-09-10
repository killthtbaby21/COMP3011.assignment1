console.log("recorder.js loaded");
const recordButton = document.getElementById("recordButton");
const stopButton = document.getElementById("stopButton");
const statusText = document.getElementById("status");
const transcriptionText = document.getElementById("transcription");

let mediaRecorder;
let audioChunks = [];

recordButton.addEventListener("click", startRecording);
stopButton.addEventListener("click", stopRecording);

async function startRecording() {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({
            audio: true
        });

        audioChunks = [];

        mediaRecorder = new MediaRecorder(stream, {
            mimeType: "audio/webm"
        });

		mediaRecorder.addEventListener("dataavailable", event => {
		    console.log("Audio data received:", event.data.size, "bytes");

		    if (event.data.size > 0) {
		        audioChunks.push(event.data);
		    }
		});

		mediaRecorder.addEventListener("stop", async () => {
		    console.log("Recorder stopped.");
		    console.log("Audio chunks:", audioChunks.length);

		    const audioBlob = new Blob(audioChunks, {
		        type: "audio/webm"
		    });

		    console.log("Audio blob size:", audioBlob.size, "bytes");

		    stream.getTracks().forEach(track => track.stop());

		    await uploadAudio(audioBlob);
		});

        mediaRecorder.start();

        recordButton.disabled = true;
        stopButton.disabled = false;

        statusText.textContent = "Recording in progress...";

    } catch (error) {
        console.error("Microphone access failed:", error);

        statusText.textContent =
            "Unable to access the microphone. Please allow microphone access.";
    }
}

function stopRecording() {
    if (mediaRecorder && mediaRecorder.state === "recording") {

        mediaRecorder.stop();

        recordButton.disabled = false;
        stopButton.disabled = true;

        statusText.textContent =
            "Recording stopped. Transcribing...";
    }
}

async function uploadAudio(audioBlob) {
	console.log("Uploading audio:", audioBlob.size, "bytes");
    const formData = new FormData();

    formData.append(
        "audio",
        audioBlob,
        "recording.webm"
    );

    try {
        const response = await fetch("/api/v1/transcribe", {
            method: "POST",
            body: formData
        });

        if (!response.ok) {
            throw new Error(
                `Transcription request failed: ${response.status}`
            );
        }

        const transcription = await response.text();

        transcriptionText.textContent = transcription;

        statusText.textContent =
            "Transcription complete. Ready to record again.";

    } catch (error) {
        console.error("Transcription failed:", error);

        statusText.textContent =
            "Transcription failed. Please try again.";
    } finally {
        recordButton.disabled = false;
        stopButton.disabled = true;
    }
}