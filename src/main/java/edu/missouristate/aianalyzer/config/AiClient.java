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
    public Client googleGenAiClient() {
        return Client.builder()
                .vertexAI(true)
                .build();
    }
}
