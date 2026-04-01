package rag_document_assistant.controller;

import rag_document_assistant.service.RagService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/rag")
public class RagController {
    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ask")  // Keep this one
    public String ask(@RequestBody Map<String, String> request) {
        return ragService.askQuestion(request.get("question"));
    }
}
