package rag_document_assistant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {

    @Autowired
    private RestTemplate restTemplate;

    private final String OLLAMA_URL = "http://localhost:11434/api/generate";

    public String askLLM(String question) {

        Map<String, Object> request = new HashMap<>();
        request.put("model", "phi3");
        request.put("prompt", question);
        request.put("stream", false);

        Map response = restTemplate.postForObject(OLLAMA_URL, request, Map.class);

        return response.get("response").toString();
    }
}