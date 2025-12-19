package com.theatermgnt.theatermgnt.chatbotInternal.repository;

import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import com.theatermgnt.theatermgnt.chatbotInternal.entity.VectorDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VectorDocumentRepository extends JpaRepository<VectorDocument, String> {
}
