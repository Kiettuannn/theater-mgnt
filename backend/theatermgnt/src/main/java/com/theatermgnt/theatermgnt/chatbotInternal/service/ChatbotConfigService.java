package com.theatermgnt.theatermgnt.chatbotInternal.service;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.request.AddDocumentRequest;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.request.SyncFileToVectorStoreRequest;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatbotDocumentResponse;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.HealthCheckResponse;
import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentStatus;
import com.theatermgnt.theatermgnt.chatbotInternal.mapper.ChatbotDocumentMapper;
import com.theatermgnt.theatermgnt.chatbotInternal.repository.ChatbotDocumentRepository;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.file.entity.FileMgnt;
import com.theatermgnt.theatermgnt.file.mapper.FileMgntMapper;
import com.theatermgnt.theatermgnt.file.repository.FileMgntRepository;
import com.theatermgnt.theatermgnt.staff.repository.StaffRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    StaffRepository staffRepository;

    // Add document to chatbot config
    @Transactional
    public ChatbotDocumentResponse addDocumentToRag(AddDocumentRequest request) {
        log.info("Adding document to RAG - fileId: {}, documentType: {}", request.getFileId(), request.getDocumentType());
        
        // Validate file exists
        FileMgnt file = fileMgntRepository.findById(request.getFileId())
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
        

        // Validate file type
        if(!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
            log.error("Invalid file type: {}", file.getContentType());
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        // Check already added
        if(chatbotDocumentRepository.existsByFileMgntId(file.getId())) {
            log.error("Document already exists for file: {}", file.getId());
            throw new AppException(ErrorCode.DOCUMENT_ALREADY_EXISTS);
        }

        // Create chatbot document
        String accountId = SecurityContextHolder.getContext().getAuthentication().getName();
        String syncedBy = buildSyncedBy(accountId);
        
        log.info("Creating chatbot document - syncedBy: {}", syncedBy);

        ChatbotDocument chatbotDocument = ChatbotDocument.builder()
                .fileMgnt(file)
                .documentType(request.getDocumentType())
                .documentStatus(DocumentStatus.INACTIVE)
                .description(request.getDescription())
                .priority(request.getPriority())
                .syncedBy(syncedBy)
                .build();
        chatbotDocument = chatbotDocumentRepository.save(chatbotDocument);
        
        log.info("Chatbot document created with ID: {}", chatbotDocument.getId());

        // Sync immediately if requested
        if(request.isSyncImmediately()){
            log.info("Syncing document immediately: {}", chatbotDocument.getId());
            syncDocumentToVector(chatbotDocument.getId());
        }
        return chatbotDocumentMapper.toChatbotDocumentResponse(chatbotDocument);
    }


    // Sync document to vector store
    @Async
    @Transactional
    public CompletableFuture<Void> syncDocumentToVector(String documentId) {
        try{
            // Look document
            ChatbotDocument doc = chatbotDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

            // Check if already processing
            if(doc.getDocumentStatus() == DocumentStatus.PROCESSING){
                throw new AppException(ErrorCode.DOCUMENT_ALREADY_PROCESSING);
            }

            // Update status to processing
            doc.setDocumentStatus(DocumentStatus.PROCESSING);
            doc.setSyncError(null);
            chatbotDocumentRepository.save(doc);

            // Sync to vector store
            int syncedChunks = vectorStoreService.syncFileToVectorStore(SyncFileToVectorStoreRequest.builder()
                            .fileId(doc.getFileMgnt().getId())
                            .fileUrl(doc.getFileMgnt().getUrl())
                            .fileName(doc.getFileMgnt().getOriginalFileName())
                            .documentType(doc.getDocumentType())
                            .chatbotDocumentId(doc.getId())
                    .build());

            // Update status to active
            doc.setDocumentStatus(DocumentStatus.ACTIVE);
            doc.setChunksCount(syncedChunks);
            doc.setLastSyncedAt(LocalDateTime.now());
            chatbotDocumentRepository.save(doc);
        }catch(AppException e){
            // Update status to FAILED
            ChatbotDocument doc = chatbotDocumentRepository.findById(documentId).orElse(null);
            if(doc != null){
                doc.setDocumentStatus(DocumentStatus.FAILED);
                doc.setSyncError(e.getMessage());
                chatbotDocumentRepository.save(doc);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    // Resync document: Delete and sync new
    @Async
    @Transactional
    public CompletableFuture<Void> resyncDocument(String documentId) {
        ChatbotDocument doc = chatbotDocumentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        // Remove old vectors
        if(doc.getDocumentStatus() == DocumentStatus.ACTIVE){
            vectorStoreService.deleteFileFromVectorStore(doc.getFileMgnt().getId());
        }

        // Sync new
        return syncDocumentToVector(documentId);
    }

    // Remove document from RAG
    @Transactional
    public void removeDocumentFromRag(String documentId) {
        ChatbotDocument doc = chatbotDocumentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        // Remove vectors if active
        if(doc.getDocumentStatus() == DocumentStatus.ACTIVE){
            vectorStoreService.deleteFileFromVectorStore(doc.getFileMgnt().getId());
        }

        // Remove record
        chatbotDocumentRepository.deleteById(documentId);
    }

    // Toggle document status
    @Transactional
    public void toggleDocumentStatus(String documentId) {
        ChatbotDocument doc = chatbotDocumentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));

        if(doc.getDocumentStatus() == DocumentStatus.ACTIVE){
            // Deactivate
            vectorStoreService.deleteFileFromVectorStore(doc.getFileMgnt().getId());
            doc.setDocumentStatus(DocumentStatus.INACTIVE);
            chatbotDocumentRepository.save(doc);
        } else{
            // Activate
            syncDocumentToVector(documentId);
        }
    }

    // Get all RAG documents
    public List<ChatbotDocumentResponse> getAllRagDocuments() {
        return chatbotDocumentRepository.findAll().stream()
                .map(chatbotDocumentMapper::toChatbotDocumentResponse)
                .toList();
    }

    // Get RAG document by ID
    public ChatbotDocumentResponse getRagDocumentById(String documentId) {
        ChatbotDocument doc = chatbotDocumentRepository.findById(documentId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCUMENT_NOT_FOUND));
        return chatbotDocumentMapper.toChatbotDocumentResponse(doc);
    }

    // Health check
    public HealthCheckResponse checkHealth() {
        List<ChatbotDocument> activeDocs = chatbotDocumentRepository.findAllByDocumentStatus(DocumentStatus.ACTIVE);

        int expectedChunks = activeDocs.stream()
                .mapToInt(doc -> doc.getChunksCount() != null ? doc.getChunksCount() : 0)
                .sum();

        Integer actualChunks = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vector_store",
                Integer.class
        );
        long totalDocs = chatbotDocumentRepository.count();
        boolean isConsistent = expectedChunks == (actualChunks != null ? actualChunks : 0);
        return HealthCheckResponse.builder()
                .isConsistent(isConsistent)
                .expectedChunks(expectedChunks)
                .actualChunks(actualChunks)
                .activeDocuments(activeDocs.size())
                .totalDocuments((int) totalDocs)
                .message(isConsistent ? "System healthy" : "Isconsistency issue detected")
                .build();
    }
    private String buildSyncedBy(String accountId) {
        var staff = staffRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return staff.getFirstName() + " " + staff.getLastName();
    }
}
