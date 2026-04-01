package rag_document_assistant.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String OLLAMA_EMBED_URL = "http://localhost:11434/api/embeddings";

    public List<Double> getEmbedding(String text) {

        Map<String, Object> request = new HashMap<>();
        request.put("model", "phi3");
        request.put("prompt", text);

        Map response = restTemplate.postForObject(OLLAMA_EMBED_URL, request, Map.class);

        return (List<Double>) response.get("embedding");
    }
}