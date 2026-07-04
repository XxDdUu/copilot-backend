package com.sky_copilot.ai_copilot.chat.service.impl;

import com.sky_copilot.ai_copilot.chat.model.RagRequest;
import com.sky_copilot.ai_copilot.chat.model.RagResponse;
import com.sky_copilot.ai_copilot.chat.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.sky_copilot.ai_copilot.ai.client.AiClient;

@Service
@RequiredArgsConstructor
public class PythonRagServiceImpl implements RagService {

    private final RestTemplate restTemplate;
    private final AiClient aiClient;

    @Override
    public String ask(String question) {
        return aiClient.ask(new RagRequest(question)).getAnswer();
    }
}