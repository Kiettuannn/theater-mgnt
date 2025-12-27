package com.theatermgnt.theatermgnt.chatbotInternal.service;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.request.SyncFileToVectorStoreRequest;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.SectionInfo;
import com.theatermgnt.theatermgnt.chatbotInternal.entity.VectorDocument;
import com.theatermgnt.theatermgnt.chatbotInternal.repository.VectorDocumentRepository;
import com.theatermgnt.theatermgnt.chatbotInternal.util.DocumentSectionTracker;
import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VectorStoreService {
    VectorStore vectorStore;
    VectorDocumentRepository vectorDocumentRepository;

    // Sync file to vector store with metadata
    @Transactional
    public int syncFileToVectorStore(SyncFileToVectorStoreRequest request){
       try{
           // Download file
           Resource resource = downloadFileAsResource(request.getFileUrl());

           // Read document
           DocumentReader documentReader = new TikaDocumentReader(resource);
           List<Document> documents = documentReader.get();

           if(documents.isEmpty()){
               throw new AppException(ErrorCode.DOCUMENT_PARSING_FAILED);
           }

           // Track sections
           String fullText = documents.get(0).getText();
           DocumentSectionTracker sectionTracker = new DocumentSectionTracker(fullText);


           // Split into chunks
           TextSplitter textSplitter = new TokenTextSplitter(800, 350, 10, 5000, true);
           List<Document> splitDocuments = textSplitter.apply(documents);

           // Enrich metadata
           List<Document> enrichedDocuments = new ArrayList<>();
           for(int i = 0; i < splitDocuments.size(); i++){
               Document doc = splitDocuments.get(i);
               Map<String, Object> metadata = doc.getMetadata();

               // Add custom metadata
               metadata.put("fileId", request.getFileId());
               metadata.put("fileName", request.getFileName());
               metadata.put("documentType", request.getDocumentType().name());
               metadata.put("chatbotDocumentId", request.getChatbotDocumentId());
               metadata.put("chunkIndex", i);
               metadata.put("totalChunks", splitDocuments.size());
               metadata.put("syncedAt", LocalDateTime.now().toString());

               if(sectionTracker.hasSections()){
                   try{
                       SectionInfo sectionInfo = sectionTracker.getSectionFromText(doc.getText());

                       if(sectionInfo != null){
                           metadata.put("sectionNumber", sectionInfo.getSectionNumber());
                           metadata.put("sectionTitle", sectionInfo.getSectionTitle());
                           metadata.put("sectionFullTitle", sectionInfo.getFullTitle());
                       }
                   }catch(Exception e){
                       log.warn("Could not map section for chunk {}: {}", i, e.getMessage());
                   }
               }
               enrichedDocuments.add(doc);
           }

           // Add to vector store
           vectorStore.add(enrichedDocuments);

           // Track in repository
           if(vectorDocumentRepository != null){
               for(int i = 0; i < enrichedDocuments.size(); i++){
                   Document doc = enrichedDocuments.get(i);
                   String vectorId = doc.getId();
                   VectorDocument vectorDocument = VectorDocument.builder()
                           .fileId(request.getFileId())
                           .vectorId(vectorId)
                           .chatbotDocumentId(request.getChatbotDocumentId())
                           .chunkIndex(i)
                           .build();
                   vectorDocumentRepository.save(vectorDocument);
               }
           }
            log.info("Synced {} chunks from file {} to vector store", splitDocuments.size(), request.getFileName());
           return splitDocuments.size();
       }catch (AppException e){
              log.error("Error syncing file to vector store: {}", e.getMessage());
              throw new AppException(ErrorCode.FILE_SYNC_TO_VECTOR_STORE_FAILED);
       }
    }

    @Transactional
    public void deleteFileFromVectorStore(String fileId){
        try{
          log.info("Deleting file {} from vector store", fileId);

          // Get vector IDs
          List<VectorDocument> vectorDocuments = vectorDocumentRepository.findByFileId(fileId);
            if(!vectorDocuments.isEmpty()){
                List<String> vectorIds = vectorDocuments.stream()
                        .map(VectorDocument::getVectorId)
                        .toList();

                // Delete from vector store
                vectorStore.delete(vectorIds);

                // Delete tracking records
                vectorDocumentRepository.deleteByFileId(fileId);

                log.info("Removed {} vectors for file {}", vectorIds.size(), fileId);
            }
        }catch (Exception e){
            log.error("Error deleting file from vector store: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_DELETE_FROM_VECTOR_STORE_FAILED);
        }
    }


    private Resource downloadFileAsResource(String fileUrl){
        WebClient webClient = WebClient.create();
        try{
            byte[] bytes = webClient.get()
                    .uri(fileUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            Mono.error(new AppException(ErrorCode.FILE_DOWNLOAD_FAILED)))
                    .bodyToMono(byte[].class)
                    .block();
            if(bytes == null){
                throw new AppException(ErrorCode.FILE_DOWNLOAD_FAILED);
            }
            return new ByteArrayResource(bytes);
        }catch(Exception e){
            log.error("Error downloading file from URL: {}", fileUrl, e);
            throw new AppException(ErrorCode.FILE_DOWNLOAD_FAILED);
        }
    }
}


