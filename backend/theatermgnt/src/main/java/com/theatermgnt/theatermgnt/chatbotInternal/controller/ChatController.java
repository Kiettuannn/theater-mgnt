package com.theatermgnt.theatermgnt.chatbotInternal.controller;

import com.cloudinary.Api;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatMessageResponse;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
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
    public ApiResponse<ChatBotInternalResponse> chat(@RequestBody ChatBotInternalRequest request) {
        return ApiResponse.<ChatBotInternalResponse>builder()
                .result(chatService.chat(request))
                .build();
    }

    @GetMapping("/history")
    public ApiResponse<List<ChatMessageResponse>> getChatHistory(){
        return ApiResponse.<List<ChatMessageResponse>>builder()
                .result(chatService.getChatHistory())
                .build();
    }

    @DeleteMapping("/history")
    public ApiResponse<Void> clearConversation() {
        chatService.clearCurrentUserConversation();
        return ApiResponse.<Void>builder()
                .message("Conversation cleared successfully")
                .build();
    }
}
