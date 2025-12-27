package com.theatermgnt.theatermgnt.chatbotInternal.dto.response;

import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentStatus;
import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentType;
import com.theatermgnt.theatermgnt.file.dto.response.FileItemResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatbotDocumentResponse {
    String id;
    FileItemResponse file;
    DocumentType documentType;
    DocumentStatus documentStatus;
    String description;
    LocalDateTime addedAt;
    LocalDateTime syncedAt;
    String syncedBy;
    Integer priority;
    Integer chunksCount;
    String syncError;

    Integer progressPercentage;
}
