package com.backend.medichat.controller;


import com.backend.medichat.dto.ChatRequest;
import com.backend.medichat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // Quan trọng để Angular gọi được
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public ResponseEntity<?> askMediAI(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isEmpty()) {
            return ResponseEntity.badRequest().body("Tin nhắn không được để trống");
        }

        String aiReply = chatService.getChatResponse(request.getMessage());

        // Trả về định dạng JSON để Angular dễ xử lý
        return ResponseEntity.ok(Map.of(
                "role", "assistant",
                "text", aiReply,
                "time", java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
        ));
    }
}