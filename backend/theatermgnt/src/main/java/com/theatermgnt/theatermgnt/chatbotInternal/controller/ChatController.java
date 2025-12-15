package com.theatermgnt.theatermgnt.chatbotInternal.controller;

import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.request.ChatBotInternalRequest;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatBotInternalResponse;
import com.theatermgnt.theatermgnt.chatbotInternal.service.ChatService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/chat")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    ChatService chatService;

    @PostMapping
    public ChatBotInternalResponse chat(@RequestBody ChatBotInternalRequest request) {
        return chatService.chat(request);
    }

    @DeleteMapping("/conversation")
    public void clearConversation() {
        chatService.clearCurrentUserConversation();
    }
}
