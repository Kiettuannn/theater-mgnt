package com.theatermgnt.theatermgnt.chatbotInternal.repository;

import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import com.theatermgnt.theatermgnt.chatbotInternal.entity.VectorDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VectorDocumentRepository extends JpaRepository<VectorDocument, String> {
    List<VectorDocument> findByFileId(String fileId);

    List<VectorDocument> findByChatbotDocumentId(String chatbotDocumentId);

    void deleteByFileId(String fileId);

    void deleteByChatbotDocumentId(String chatbotDocumentId);
}
