package com.theatermgnt.theatermgnt.chatbotInternal.config;

import java.util.List;
import java.util.stream.Collectors;

import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentStatus;
import com.theatermgnt.theatermgnt.chatbotInternal.repository.ChatbotDocumentRepository;
import com.theatermgnt.theatermgnt.chatbotInternal.service.ChatbotConfigService;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
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

    @Value("classpath:/data/staff_handbook.pdf")
    Resource handbookFile;

    @Bean
    ApplicationRunner ragApplicationRunner(
            VectorStore vectorStore,
            JdbcTemplate jdbcTemplate,
            ChatbotDocumentRepository chatbotDocumentRepository,
            ChatbotConfigService chatbotConfigService) {
        return args -> {
            Integer count = jdbcTemplate.queryForObject("select count(*) from vector_store", Integer.class);

            if (count == null || count == 0) {
                log.info("Vector store is empty. Initializing from handbook...");

                // Check if there are active documents to sync
                List<ChatbotDocument> activeDocuments =
                        chatbotDocumentRepository.findAllByDocumentStatus(DocumentStatus.ACTIVE);

                if(!activeDocuments.isEmpty()) {
                    log.info("Syncing {} active documents to vector store", activeDocuments.size());
                    for(ChatbotDocument doc : activeDocuments) {
                       try{
                           chatbotConfigService.syncDocumentToVector(doc.getId()).join();
                       }catch(Exception e){
                           log.error("Error while syncing documents to vector store", e);
                       }
                    }
                } else {
                  // Fallback: Load default handbook file
                    log.info("No active documents to vector store");
                    loadDocument(vectorStore, handbookFile);
                }

            }
        };
    }

    void loadDocument(VectorStore vectorStore, Resource resource) {
        log.info("Loading document {} into Vector Store", resource.getFilename());

        DocumentReader documentReader = new TikaDocumentReader(resource);
        List<Document> documents = documentReader.get();

        // Configure TextSplitter
        TextSplitter textSplitter = new TokenTextSplitter(800, 350, 10, 5000, true);
        List<Document> splitDocuments = textSplitter.apply(documents);
        vectorStore.add(splitDocuments);
    }
}
