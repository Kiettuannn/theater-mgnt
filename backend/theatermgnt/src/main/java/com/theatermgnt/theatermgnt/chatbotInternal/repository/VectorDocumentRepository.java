package com.theatermgnt.theatermgnt.chatbotInternal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.chatbotInternal.entity.VectorDocument;

@Repository
public interface VectorDocumentRepository extends JpaRepository<VectorDocument, String> {
    List<VectorDocument> findByFileId(String fileId);

    void deleteByFileId(String fileId);
}
