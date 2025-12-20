package com.theatermgnt.theatermgnt.chatbotInternal.service;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.request.AddDocumentRequest;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatbotDocumentResponse;
import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentStatus;
import com.theatermgnt.theatermgnt.chatbotInternal.mapper.ChatbotDocumentMapper;
import com.theatermgnt.theatermgnt.chatbotInternal.repository.ChatbotDocumentRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.file.entity.FileMgnt;
import com.theatermgnt.theatermgnt.file.repository.FileMgntRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ChatbotConfigService {
    static final List<String> ALLOWED_FILE_TYPES = List.of(
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    ChatbotDocumentRepository chatbotDocumentRepository;
    FileMgntRepository fileMgntRepository;
    VectorStoreService vectorStoreService;
    ChatbotDocumentMapper chatbotDocumentMapper;
    JdbcTemplate jdbcTemplate;

    // Add document to chatbot config
    @Transactional
    public ChatbotDocumentResponse addDocumentToRag(AddDocumentRequest request) {
        // Validate file exists
        FileMgnt file = fileMgntRepository.findById(request.getFileId())
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        // Validate file type
        if(!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        // Check already added
        if(chatbotDocumentRepository.existsByFileId(file.getId())) {
            throw new AppException(ErrorCode.DOCUMENT_ALREADY_EXISTS);
        }

        // Create chatbot document
        String accountId = SecurityContextHolder.getContext().getAuthentication().getName();

        ChatbotDocument chatbotDocument = ChatbotDocument.builder()
                .fileMgnt(file)
                .documentType(request.getDocumentType())
                .documentStatus(DocumentStatus.INACTIVE)
                .description(request.getDescription())
                .priority(request.getPriority())
                .syncedBy(accountId)
                .build();
        chatbotDocument = chatbotDocumentRepository.save(chatbotDocument);

        // Sync immediately if requested
        if(request.isSyncImmediately()){
        }
        return chatbotDocumentMapper.toChatbotDocumentResponse(chatbotDocument);
    }

}
