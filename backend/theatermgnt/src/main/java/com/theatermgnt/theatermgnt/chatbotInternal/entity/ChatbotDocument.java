package com.theatermgnt.theatermgnt.chatbotInternal.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentStatus;
import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentType;
import com.theatermgnt.theatermgnt.common.entity.BaseEntity;
import com.theatermgnt.theatermgnt.file.entity.FileMgnt;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chatbot_documents")
@SQLDelete(sql = "UPDATE chatbot_documents SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatbotDocument extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "file_id")
    FileMgnt fileMgnt;

    Integer priority;

    LocalDateTime lastSyncedAt;

    String syncedBy;
    Integer chunksCount;
    String syncError;
    String description;

    @Enumerated(EnumType.STRING)
    DocumentType documentType;

    @Enumerated(EnumType.STRING)
    DocumentStatus documentStatus;
}
