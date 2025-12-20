package com.theatermgnt.theatermgnt.chatbotInternal.repository;

import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatbotDocumentRepository extends JpaRepository<ChatbotDocument, String> {
    boolean existsByFileId(String fileId);
}
