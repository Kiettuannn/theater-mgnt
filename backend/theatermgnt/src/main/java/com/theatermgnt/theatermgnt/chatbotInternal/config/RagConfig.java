package com.theatermgnt.theatermgnt.chatbotInternal.config;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
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
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RagConfig {
    @Value("classpath:/data/staff_handbook.pdf")
    Resource handbookFile;


    @Bean
    ApplicationRunner ragApplicationRunner(VectorStore vectorStore, JdbcTemplate jdbcTemplate) {
        return args -> {
            Integer count = jdbcTemplate.queryForObject(
                    "select count(*) from vector_store", Integer.class);

            if(count != null && count == 0) {
                log.info("Vector store already initialized with {} vectors", count);
                loadDocument(vectorStore, handbookFile);
            }else{
                log.info("Vector store already initialized with {} vectors", count);
            }
        };
    }


    void loadDocument(VectorStore vectorStore, Resource resource) {
        log.info("Loading document {} into Vector Store", resource.getFilename());

        DocumentReader documentReader = new TikaDocumentReader(handbookFile);
        List<Document> documents = documentReader.get();
        TextSplitter textSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = textSplitter.apply(documents);
        vectorStore.add(splitDocuments);
        log.info("Loaded {} documents", resource.getFilename());
    }
}
