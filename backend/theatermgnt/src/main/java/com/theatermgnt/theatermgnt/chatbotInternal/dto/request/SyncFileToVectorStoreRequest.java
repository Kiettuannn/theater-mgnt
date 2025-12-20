package com.theatermgnt.theatermgnt.chatbotInternal.dto.request;

import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SyncFileToVectorStoreRequest {
    String fileId;
    String fileUrl;
    String fileName;
    DocumentType documentType;
    String chatbotDocumentId;
}
