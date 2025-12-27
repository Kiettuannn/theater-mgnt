package com.theatermgnt.theatermgnt.chatbotInternal.config;

import java.util.List;

import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentStatus;
import com.theatermgnt.theatermgnt.chatbotInternal.repository.ChatbotDocumentRepository;
import com.theatermgnt.theatermgnt.chatbotInternal.service.ChatbotConfigService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RagConfig {

    @Bean
    ApplicationRunner ragApplicationRunner(
            JdbcTemplate jdbcTemplate,
            ChatbotDocumentRepository chatbotDocumentRepository,
            ChatbotConfigService chatbotConfigService) {
        return args -> {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vector_store", Integer.class);

            if (count == null || count == 0) {
                log.info("Vector store is empty.");

                // Check if there are active documents to sync
                List<ChatbotDocument> activeDocuments =
                        chatbotDocumentRepository.findAllByDocumentStatus(DocumentStatus.ACTIVE);

                if (!activeDocuments.isEmpty()) {
                    log.info("Found {} active documents. Syncing to vector store...", activeDocuments.size());
                    for (ChatbotDocument doc : activeDocuments) {
                        try {
                            chatbotConfigService.syncDocumentToVector(doc.getId()).join();
                        } catch (Exception e) {
                            log.error("Failed to sync document {}", doc.getId(), e);
                        }
                    }
                } else {
                    log.warn("No active documents found. Please add documents via API: POST /chatbot/documents");
                }
            } else {
                log.info("Vector store already initialized with {} vectors", count);
            }
        };
    }
}
