package com.example.tanksolarheaterbe.controllers;

import com.example.tanksolarheaterbe.dto.ChatRequest;
import com.example.tanksolarheaterbe.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/")
    public String chat(@RequestBody ChatRequest request) {
        return chatService.chat(request);
    }
}
