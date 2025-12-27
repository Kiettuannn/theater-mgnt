package com.theatermgnt.theatermgnt.chatbotInternal.controller;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.request.AddDocumentRequest;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatbotDocumentResponse;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.HealthCheckResponse;
import com.theatermgnt.theatermgnt.chatbotInternal.service.ChatbotConfigService;
import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chatbot/documents")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ChatbotConfigController {
    ChatbotConfigService chatbotConfigService;

    @PostMapping
    public ApiResponse<ChatbotDocumentResponse> addDocument(@RequestBody @Valid AddDocumentRequest request) {
        return ApiResponse.<ChatbotDocumentResponse>builder()
                .result(chatbotConfigService.addDocumentToRag(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<ChatbotDocumentResponse>> getAllDocuments() {
        return ApiResponse.<List<ChatbotDocumentResponse>>builder()
                .result(chatbotConfigService.getAllRagDocuments())
                .build();
    }

    @GetMapping("/{documentId}")
    public ApiResponse<ChatbotDocumentResponse> getDocumentById(@PathVariable String documentId) {
        return ApiResponse.<ChatbotDocumentResponse>builder()
                .result(chatbotConfigService.getRagDocumentById(documentId))
                .build();
    }

    // Sync document to vector store
    @PostMapping("/{documentId}/sync")
    public ApiResponse<ChatbotDocumentResponse> syncDocument(@PathVariable String documentId) {
        chatbotConfigService.syncDocumentToVector(documentId);
        return ApiResponse.<ChatbotDocumentResponse>builder()
                .message("Syncing document to vector store")
                .build();
    }

    // Resync
    @PostMapping("/{documentId}/resync")
    public ApiResponse<ChatbotDocumentResponse> resyncDocument(@PathVariable String documentId) {
        chatbotConfigService.resyncDocument(documentId);
        return ApiResponse.<ChatbotDocumentResponse>builder()
                .message("Resyncing document to vector store")
                .build();
    }

    // Toggle document active status
    @PostMapping("/{documentId}/toggle")
    public ApiResponse<ChatbotDocumentResponse> toggleDocumentStatus(@PathVariable String documentId) {

        chatbotConfigService.toggleDocumentStatus(documentId);
        return ApiResponse.<ChatbotDocumentResponse>builder()
                .message("Processing toggle document status")
                .build();
    }

    // Delete document from RAG
    @DeleteMapping("/{documentId}")
    public ApiResponse<ChatbotDocumentResponse> deleteDocument(@PathVariable String documentId) {
        chatbotConfigService.removeDocumentFromRag(documentId);
        return ApiResponse.<ChatbotDocumentResponse>builder()
                .message("Document removed from RAG")
                .build();
    }

    // Health check
    @GetMapping("/health")
    public ApiResponse<HealthCheckResponse> healthCheck() {
        return ApiResponse.<HealthCheckResponse>builder()
                .result(chatbotConfigService.checkHealth())
                .build();
    }
}
