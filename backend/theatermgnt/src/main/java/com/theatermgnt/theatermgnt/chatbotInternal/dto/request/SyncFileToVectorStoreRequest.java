package com.theatermgnt.theatermgnt.chatbotInternal.dto.request;

import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
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
