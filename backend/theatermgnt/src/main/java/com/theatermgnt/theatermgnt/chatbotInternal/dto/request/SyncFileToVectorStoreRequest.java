package com.theatermgnt.theatermgnt.chatbotInternal.dto.request;

import jakarta.validation.constraints.NotBlank;

import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SyncFileToVectorStoreRequest {
    @NotBlank
    String fileId;

    @NotBlank
    String fileUrl;

    @NotBlank
    String fileName;

    @NonNull
    DocumentType documentType;

    @NotBlank
    String chatbotDocumentId;
}
