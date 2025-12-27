package com.theatermgnt.theatermgnt.chatbotInternal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.theatermgnt.theatermgnt.chatbotInternal.constant.Sender;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatMessageResponse;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.SourceInfo;
import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import com.theatermgnt.theatermgnt.chatbotInternal.repository.ChatbotDocumentRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.request.ChatBotInternalRequest;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatBotInternalResponse;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import javax.print.Doc;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {
    ChatClient chatClient;
    VectorStore vectorStore;

    JdbcChatMemoryRepository jdbcChatMemoryRepository;
    ChatMemory chatMemory;
    ChatPromptBuilder chatPromptBuilder;

    @Autowired
    ChatbotDocumentRepository chatbotDocumentRepository;

    public ChatService(ChatClient.Builder builder, VectorStore vectorStore,
                       JdbcChatMemoryRepository jdbcChatMemoryRepository,
                       ChatbotDocumentRepository chatbotDocumentRepository,
                       ChatPromptBuilder chatPromptBuilder) {
        this.vectorStore = vectorStore;
        this.jdbcChatMemoryRepository = jdbcChatMemoryRepository;

        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(30)
                .build();
        this.chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.chatbotDocumentRepository = chatbotDocumentRepository;
        this.chatPromptBuilder = chatPromptBuilder;
    }


    public ChatBotInternalResponse chat(ChatBotInternalRequest request) {
        try {
            List<Document> similarDocs = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(request.getQuery())
                            .topK(10)
                            .build());

            if (similarDocs == null || similarDocs.isEmpty()) {
                return ChatBotInternalResponse.builder()
                        .answer("Xin lỗi, tôi không tìm thấy thông tin này trong sổ tay quy định.")
                        .build();
            }


            // Sort by priority
            List<Document> sortedDocs = sortByPriority(similarDocs);

            // LIMIT to top 5
            similarDocs = sortedDocs.stream().limit(5).collect(Collectors.toList());

            // Extract source information first
            List<SourceInfo> sources = extractSourceInfo(similarDocs);

            // Build prompts using PromptBuilder
            String documentCatalog = chatPromptBuilder.buildDocumentCatalog(sources);
            String systemInstruction = chatPromptBuilder.buildSystemInstruction(documentCatalog);
            String contextForAI = chatPromptBuilder.buildContextSection(similarDocs);

            var contextHolder = SecurityContextHolder.getContext();
            String conversationId = contextHolder.getAuthentication().getName(); // AccountId

            String answer = chatClient.prompt()
                    .advisors(advisorSpec -> advisorSpec.param(
                            ChatMemory.CONVERSATION_ID, conversationId
                    ))
                    .system(systemInstruction)
                    .system(sp -> sp.text(contextForAI)) // Add context but not save to memory
                    .user(request.getQuery().trim()) // Save original user query to memory
                    .call()
                    .content();

            return ChatBotInternalResponse.builder()
                    .answer(answer)
                    .sources(sources)
                    .build();

        } catch (Exception e) {
            log.error("Chat error for query='{}'", request.getQuery(), e);
            return ChatBotInternalResponse.builder()
                    .answer("Đã xảy ra lỗi khi xử lý yêu cầu. Vui lòng thử lại sau.")
                    .build();
        }
    }


    public void clearCurrentUserConversation() {
        try {
            var contextHolder = SecurityContextHolder.getContext();
            String accountId = contextHolder.getAuthentication().getName();
            jdbcChatMemoryRepository.deleteByConversationId(accountId);
        } catch (Exception e) {
            log.error("Error clearing current user conversation", e);
        }
    }

    public List<ChatMessageResponse> getChatHistory(){
        var contextHolder = SecurityContextHolder.getContext();
        String conversationId = contextHolder.getAuthentication().getName();

        List<Message> messages= chatMemory.get(conversationId);
        if(messages==null || messages.isEmpty()){
            return Collections.emptyList();
        }
        return messages.stream()
                .map(msg -> ChatMessageResponse.builder()
                        .text(msg.getText())
                        .sender(msg instanceof UserMessage ? Sender.USER : Sender.BOT)
                        .timestamp(LocalDateTime.now())
                        .build()
        ).collect(Collectors.toList());
    }

    // Sort documents by priority and document type
    private List<Document> sortByPriority(List<Document> documents){
        return documents.stream()
                .sorted((d1, d2) -> {
                    String docId1 = d1.getMetadata().get("chatbotDocumentId").toString();
                    String docId2 = d2.getMetadata().get("chatbotDocumentId").toString();

                    ChatbotDocument doc1 = docId1 != null ? chatbotDocumentRepository.findById(docId1).orElse(null) : null;
                    ChatbotDocument doc2 = docId2 != null ?chatbotDocumentRepository.findById(docId2).orElse(null) : null;

                    // Sort by priority (lower number = higher priority)
                    int priority1 = doc1 != null && doc1.getPriority() != null ? doc1.getPriority() : 999;
                    int priority2 = doc2 != null && doc2.getPriority() != null ? doc2.getPriority() : 999;

                    if(priority1 != priority2){
                        return Integer.compare(priority1, priority2);
                    }

                    // Then by document type
                    String type1 = d1.getMetadata().getOrDefault("documentType", "FAQ").toString();
                    String type2 = d2.getMetadata().getOrDefault("documentType", "FAQ").toString();
                    return getDocumentTypeOrder(type1) - getDocumentTypeOrder(type2);
                })
                .toList();
    }
    private int getDocumentTypeOrder(String type) {
        return switch (type) {
            case "POLICY" -> 1;
            case "HANDBOOK" -> 2;
            case "GUIDELINE" -> 3;
            case "FAQ" -> 4;
            default -> 5;
        };
    }

    // Extract source information from documents
    private List<SourceInfo> extractSourceInfo(List<Document> documents) {
        Map<String, SourceInfo> sourceMap = new LinkedHashMap<>();
        
        for (Document doc : documents) {
            Map<String, Object> metadata = doc.getMetadata();
            String fileId = metadata.get("fileId") != null ? metadata.get("fileId").toString() : null;
            
            if (fileId == null) continue;
            
            // Get or create SourceInfo
            SourceInfo sourceInfo = sourceMap.get(fileId);
            if (sourceInfo == null) {
                String chatbotDocId = metadata.get("chatbotDocumentId") != null ? 
                    metadata.get("chatbotDocumentId").toString() : null;
                ChatbotDocument chatbotDoc = chatbotDocId != null ? 
                    chatbotDocumentRepository.findById(chatbotDocId).orElse(null) : null;
                
                String fileName = metadata.get("fileName") != null ? 
                    metadata.get("fileName").toString() : "Unknown";
                String documentType = metadata.get("documentType") != null ? 
                    metadata.get("documentType").toString() : "UNKNOWN";
                Integer priority = chatbotDoc != null && chatbotDoc.getPriority() != null ? 
                    chatbotDoc.getPriority() : 999;
                
                // Get file path from FileMgnt
                String filePath = null;
                if (chatbotDoc != null && chatbotDoc.getFileMgnt() != null) {
                    filePath = chatbotDoc.getFileMgnt().getUrl();
                }
                
                sourceInfo = com.theatermgnt.theatermgnt.chatbotInternal.dto.response.SourceInfo.builder()
                    .fileId(fileId)
                    .fileName(fileName)
                    .filePath(filePath)
                    .documentType(documentType)
                    .priority(priority)
                    .chunkIndices(new HashSet<>())
                    .sectionTitles(new HashSet<>())
                    .build();
                    
                sourceMap.put(fileId, sourceInfo);
            }
            
            // Add chunk index
            Integer chunkIndex = metadata.get("chunkIndex") != null ? 
                (Integer) metadata.get("chunkIndex") : null;
            if (chunkIndex != null) {
                sourceInfo.getChunkIndices().add(chunkIndex + 1); // +1 for human-readable numbering
            }

            // Add section
            String sectionFullTitle = metadata.get("sectionFullTitle") != null ?
                metadata.get("sectionFullTitle").toString() : null;
            if(sectionFullTitle != null){
                sourceInfo.getSectionTitles().add(sectionFullTitle);
            }
        }
        
        return new ArrayList<>(sourceMap.values());
    }

}


