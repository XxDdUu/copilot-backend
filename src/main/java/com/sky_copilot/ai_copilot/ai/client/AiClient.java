package com.sky_copilot.ai_copilot.ai.client;
import com.sky_copilot.ai_copilot.ai.config.AiProperties;
import com.sky_copilot.ai_copilot.document.dto.DocumentIngestRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.sky_copilot.ai_copilot.chat.model.RagRequest;
import com.sky_copilot.ai_copilot.chat.model.RagResponse;
import com.sky_copilot.ai_copilot.ai.dto.EmbeddingRequest;
import com.sky_copilot.ai_copilot.ai.dto.EmbeddingResponse;

@Service
@RequiredArgsConstructor
public class AiClient {

    private final RestTemplate restTemplate;
    private final AiProperties aiProperties;

    public void ingest(DocumentIngestRequest request) {
        restTemplate.postForObject(
                aiProperties.getBaseUrl() + "/api/ingest",
                request,
                Void.class
        );
    }
    public RagResponse ask(RagRequest request) {
        return restTemplate.postForObject(
                aiProperties.getBaseUrl() + "/ask",
                request,
                RagResponse.class
        );
    }
    public float[] embed(String text) {
        EmbeddingResponse response =
            restTemplate.postForObject(
                aiProperties.getBaseUrl() + "/api/embedding",
                new EmbeddingRequest(text),
                EmbeddingResponse.class
            );

        return response.embedding();
    }
}