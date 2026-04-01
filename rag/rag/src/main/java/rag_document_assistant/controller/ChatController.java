package rag_document_assistant.controller;

import rag_document_assistant.service.RagService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/rag")
public class ChatController {
    private final RagService ragService;

    public ChatController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/chat")  // ← CHANGED from "/ask" to "/chat"
    public String ask(@RequestBody Map<String, String> request) {
        return ragService.askQuestion(request.get("question"));
    }
}
