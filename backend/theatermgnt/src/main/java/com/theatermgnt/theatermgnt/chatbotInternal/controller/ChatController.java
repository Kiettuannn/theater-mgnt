package com.theatermgnt.theatermgnt.chatbotInternal.controller;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.request.ChatBotInternalRequest;
import com.theatermgnt.theatermgnt.chatbotInternal.service.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    ChatService chatService;


    @PostMapping
    public String chat(@RequestBody ChatBotInternalRequest request) {
        return chatService.chat(request);
    }
}
