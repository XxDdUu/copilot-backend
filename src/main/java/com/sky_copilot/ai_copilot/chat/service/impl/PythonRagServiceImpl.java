package com.sky_copilot.ai_copilot.chat.service.impl;

import com.sky_copilot.ai_copilot.chat.model.RagRequest;
import com.sky_copilot.ai_copilot.chat.model.RagResponse;
import com.sky_copilot.ai_copilot.chat.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PythonRagServiceImpl implements RagService {

    private final RestTemplate restTemplate;

    @Override
    public String ask(String question) {

        RagRequest request = new RagRequest(question);

        RagResponse response =
                restTemplate.postForObject(
                        "http://localhost:8000/ask",
                        request,
                        RagResponse.class
                );

        return response.getAnswer();
    }
}