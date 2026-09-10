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



## REST API

The application provides the following REST endpoints:

- `POST /api/v1/transcribe` - uploads an audio file and returns the transcription.
- `GET /api/v1/admin/uptime` - returns the current server uptime.
- `POST /api/v1/admin/shutdown` - starts graceful server shutdown.
- `GET /api/v1/stats/global` - returns the global token usage statistics.
- `POST /api/v1/stats/reset` - resets the global statistics.

API errors are handled by a global exception handler and returned with an appropriate HTTP status and error information.


## Blocking Requests and Concurrency

Speech-to-text is a blocking operation because the server must wait for the transcription service to return a result.

The application relies on Spring Boot's web server to process multiple HTTP requests concurrently. A local transcription service is provided for testing and simulates a blocking cloud request without requiring an external API call.

An integration test sends 225 concurrent HTTP transcription requests to a running Spring Boot server. This test checks that multiple blocking requests can be processed concurrently and that all requests complete successfully.


## Testing

JUnit tests are included for the main backend functionality.

The tests cover:

- administration endpoints and duplicate shutdown handling;
- statistics endpoints;
- transcription controller behaviour;
- thread-safe concurrent statistics updates;
- concurrent blocking HTTP transcription requests.

Tests can be run with:

`.\mvnw.cmd clean test`


## AI Usage

Generative AI was used as a development support tool during this assignment. It was mainly used to help explain Spring Boot concepts, review code, suggest test cases, and assist with debugging errors encountered during development.

The implementation was reviewed, modified and tested during development before being included in the final project.
