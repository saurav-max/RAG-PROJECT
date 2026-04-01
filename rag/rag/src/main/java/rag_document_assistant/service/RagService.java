package rag_document_assistant.service;

import rag_document_assistant.repository.DocumentChunkRepository;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.List;
import java.util.Set;

@Service
public class RagService {

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository repository;
    private final OllamaService ollamaService;
    private final CacheService cacheService;

    public RagService(EmbeddingService embeddingService,
                      DocumentChunkRepository repository,
                      OllamaService ollamaService,
                      CacheService cacheService) {
        this.embeddingService = embeddingService;
        this.repository = repository;
        this.ollamaService = ollamaService;
        this.cacheService = cacheService;
    }

    public String askQuestion(String question) {

        // 🔥 Step 1: Generate key
        String cacheKey = generateKey(question);

        // 🔥 Step 2: Exact response cache
        String cachedResponse = cacheService.get("resp:" + cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        // 🔥 Step 3: Get embedding (with cache)
        String cachedEmbedding = cacheService.get("emb:" + cacheKey);
        List<Double> questionEmbedding;

        if (cachedEmbedding != null) {
            questionEmbedding = parseEmbedding(cachedEmbedding);
        } else {
            questionEmbedding = embeddingService.getEmbedding(question);
            cacheService.set("emb:" + cacheKey, questionEmbedding.toString(), 60);
        }

        // 🔥 Step 4: Semantic cache check (VERY IMPORTANT)
        String semanticResult = checkSemanticCache(questionEmbedding);
        if (semanticResult != null) {
            return semanticResult;
        }

        // 🔥 Step 5: Hybrid search
        List<String> topChunks = repository.findTopChunksHybrid(
                questionEmbedding.toString(),
                question
        );

        // 🔥 Step 6: Build context
        String context = String.join("\n\n---\n\n", topChunks);

        // 🔥 Step 7: Prompt
        String prompt = """
        You are a helpful AI assistant.

        Use the context below to answer the question accurately.
        Prefer exact information from context when available.

        Context:
        """ + context + """

        Question: """ + question + """

        Answer:
        """;

        // 🔥 Step 8: Call LLM
        String response = ollamaService.askLLM(prompt);

        // 🔥 Step 9: Store caches
        cacheService.set("resp:" + cacheKey, response, 10);
        cacheService.set("emb:" + cacheKey, questionEmbedding.toString(), 60);

        return response;
    }

    // 🔥 Semantic Cache Logic
    private String checkSemanticCache(List<Double> newEmbedding) {

        Set<String> keys = cacheService.getKeys("emb:*");

        if (keys == null) return null;

        for (String key : keys) {

            String storedEmbeddingStr = cacheService.get(key);
            if (storedEmbeddingStr == null) continue;

            List<Double> storedEmbedding = parseEmbedding(storedEmbeddingStr);

            double similarity = cosineSimilarity(newEmbedding, storedEmbedding);

            if (similarity > 0.90) { // threshold
                String responseKey = key.replace("emb:", "resp:");
                return cacheService.get(responseKey);
            }
        }

        return null;
    }

    // 🔥 Cosine Similarity
    private double cosineSimilarity(List<Double> a, List<Double> b) {

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += Math.pow(a.get(i), 2);
            normB += Math.pow(b.get(i), 2);
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // 🔥 Hash key generator
    private String generateKey(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.toLowerCase().trim().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 🔥 Parse embedding string → list
    private List<Double> parseEmbedding(String str) {
        str = str.replace("[", "").replace("]", "");
        String[] parts = str.split(",");
        return java.util.Arrays.stream(parts)
                .map(String::trim)
                .map(Double::parseDouble)
                .toList();
    }
}