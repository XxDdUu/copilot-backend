package com.sky_copilot.ai_copilot.document.service;

import com.sky_copilot.ai_copilot.document.dto.DocumentChunkResponse;
import com.sky_copilot.ai_copilot.document.repository.DocumentChunkRepository; 
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.sky_copilot.ai_copilot.document.mapper.DocumentChunkMapper;
import java.util.Arrays;
import com.sky_copilot.ai_copilot.ai.client.AiClient;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchDocumentService {

    private final DocumentChunkRepository repository;
    private final AiClient aiClient;

    public List<DocumentChunkResponse> search(String question, Integer topK) {

        float[] embedding = aiClient.embed(question);

        String vector = Arrays.toString(embedding);

        return repository.similaritySearch(
                        vector,
                        0.75,
                        topK
                )
                .stream()
                .map(DocumentChunkMapper::toResponse)
                .toList();
    }
    
}