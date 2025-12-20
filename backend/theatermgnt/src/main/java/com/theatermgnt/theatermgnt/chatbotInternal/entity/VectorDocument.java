package com.theatermgnt.theatermgnt.chatbotInternal.entity;

import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentStatus;
import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentType;
import com.theatermgnt.theatermgnt.common.entity.BaseEntity;
import com.theatermgnt.theatermgnt.file.entity.FileMgnt;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "vector_documents", indexes = {
        @Index(name = "idx_file_id", columnList = "fileId"),
        @Index(name = "idx_chatbot_doc_id", columnList = "chatbotDocumentId")
})
@SQLDelete(sql = "UPDATE vector_documents SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VectorDocument extends BaseEntity {
    String vectorId;
    String fileId;
    String chatbotDocumentId;
    Integer chunkIndex;
}
