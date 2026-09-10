# COMP3011 Assignment 1 - Speech-to-Text Web Application

## Project Overview

This project is a Java Spring Boot web application developed for COMP3011 Assignment 1.

The application allows a user to record audio through the browser and sends the recorded audio to the Java backend using a REST API. The backend then uses the OpenAI speech-to-text service with the `gpt-4o-mini-transcribe` model to convert the audio into text and returns the transcription to the browser.

The application also provides REST API endpoints for server uptime, global token usage statistics, and graceful server shutdown.



## System Design

The application is separated into frontend, controller, service, and data transfer object (DTO) components.

The frontend contains HTML, CSS and JavaScript. JavaScript uses the browser microphone to record audio and sends the recorded file to the backend.

The Spring Boot controllers receive HTTP requests and pass the required work to service classes. The main services include:

- `TranscriptionService` - defines the speech-to-text operation.
- `OpenAITranscriptionService` - sends audio to the OpenAI transcription API in the normal application environment.
- `LocalStubTranscriptionService` - provides a local implementation so the application can be tested without calling the real cloud API.
- `StatisticsService` - stores the global input and output token statistics.
- `ServerLifecycleService` - manages server uptime and graceful shutdown.

The transcription service is separated behind the `TranscriptionService` interface. This allows different implementations to be selected using Spring profiles without changing the controller.

A simplified request flow is:

Browser -> REST Controller -> TranscriptionService -> OpenAI API -> StatisticsService



## Concurrency and Thread Safety

The application may receive multiple HTTP requests at the same time, so shared data must be updated safely.

`StatisticsService` uses `AtomicLong` for both input and output token counters. Atomic operations are used so that concurrent transcription requests can update the statistics without losing updates. This also avoids using a large synchronized block that could unnecessarily block other requests.

A concurrency regression test is included to check this behaviour. The test uses 32 worker threads, with each worker performing 20,000 updates to the same `StatisticsService`. This produces 640,000 concurrent updates in total. The final input and output token counts must both be exactly 640,000.

This test provides assurance that the shared token counters remain correct when they are accessed concurrently.
