package com.example.tanksolarheaterbe.services;

import com.example.tanksolarheaterbe.dto.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        chatClient = chatClientBuilder.build();
    }

    public String chat(ChatRequest request){
        SystemMessage systemMessage = new SystemMessage("Bạn là chatbot hỗ trợ khách hàng của dự án Sun Tank hỗ trợ khách hàng về bồn nước và máy nước nóng năng lượng mặt trời. Bạn nên trả lời khách hàng một cách tôn trọng và nhiệt tình");

        UserMessage userMessage = new UserMessage(request.message());

        Prompt prompt = new Prompt(systemMessage, userMessage);

        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}
