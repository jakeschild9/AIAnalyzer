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
    public Client googleGenAiClient(edu.missouristate.aianalyzer.service.config.CloudConfigService cloudConfigService) {
        String projectId = cloudConfigService.getProjectId();

        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException(
                    "Google Cloud Project ID not configured. Please configure in Settings.");
        }

        return Client.builder()
                .vertexAI(true)
                .project(projectId)
                .location("us-central1")
                .build();
    }
}
