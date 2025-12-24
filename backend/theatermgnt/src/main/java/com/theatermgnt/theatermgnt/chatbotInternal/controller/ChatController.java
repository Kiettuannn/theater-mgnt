package com.theatermgnt.theatermgnt.chatbotInternal.controller;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatMessageResponse;
import org.springframework.web.bind.annotation.*;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.request.ChatBotInternalRequest;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatBotInternalResponse;
import com.theatermgnt.theatermgnt.chatbotInternal.service.ChatService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/chatbot")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    ChatService chatService;

    @PostMapping("/chat")
    public ChatBotInternalResponse chat(@RequestBody ChatBotInternalRequest request) {
        return chatService.chat(request);
    }

    @GetMapping("/history")
    public List<ChatMessageResponse> getChatHistory(){
        return chatService.getChatHistory();
    }

    @DeleteMapping("/conversation")
    public void clearConversation() {
        chatService.clearCurrentUserConversation();
    }
}
