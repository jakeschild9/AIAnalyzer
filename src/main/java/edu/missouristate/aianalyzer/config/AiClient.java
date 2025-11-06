package edu.missouristate.aianalyzer.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Lazy
@Configuration
public class AiClient {

    @Bean
    public Client googleGenAiClient(
            @Value("${genai.project:}") String project,
            @Value("${genai.location:us-central1}") String location,
            @Value("${genai.api-key:}") String apiKey // optional fallback (non-Vertex)
    ) {
        if (apiKey != null && !apiKey.isBlank()) {
            // Non-Vertex (API key) mode
            return Client.builder()
                    .apiKey(apiKey)
                    .build();
        }

        if (project == null || project.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing genai.project. For Vertex AI you must set genai.project and genai.location."
            );
        }

        // Vertex AI mode (uses ADC from GOOGLE_APPLICATION_CREDENTIALS)
        return Client.builder()
                .vertexAI(true)
                .project(project)
                .location(location)
                .build();
    }
}
