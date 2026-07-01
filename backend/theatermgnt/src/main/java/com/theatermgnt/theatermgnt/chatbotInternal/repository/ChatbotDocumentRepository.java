package com.theatermgnt.theatermgnt.chatbotInternal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentStatus;

@Repository
public interface ChatbotDocumentRepository extends JpaRepository<ChatbotDocument, String> {
    boolean existsByFileMgntId(String fileId);

    List<ChatbotDocument> findAllByDocumentStatus(DocumentStatus documentStatus);
}
