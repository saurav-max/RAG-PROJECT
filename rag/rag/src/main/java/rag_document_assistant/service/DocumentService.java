package rag_document_assistant.service;

import rag_document_assistant.repository.DocumentChunkRepository;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DocumentService {

    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository repository;

    public DocumentService(ChunkingService chunkingService,
                           EmbeddingService embeddingService,
                           DocumentChunkRepository repository) {
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.repository = repository;
    }

    public String processDocument(MultipartFile file) throws Exception {

        Tika tika = new Tika();

        // Step 1: Extract text
        String text = tika.parseToString(file.getInputStream());

        // Step 2: Chunk text
        List<String> chunks = chunkingService.chunkText(text, 200);

        // Step 3: Generate embedding + store
        for (String chunk : chunks) {

            List<Double> embedding = embeddingService.getEmbedding(chunk);

            // convert list → string format [0.12, 0.34, ...]
            String embeddingString = embedding.toString();

            repository.insertChunk(chunk, embeddingString);
        }

        return "Saved " + chunks.size() + " chunks to database";
    }
}