package com.theatermgnt.theatermgnt.chatbotInternal.dto.request;

import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddDocumentRequest {
    @NotBlank(message = "FILE_ID_REQUIRED")
    String fileId;

    @NotNull(message = "DOCUMENT_TYPE_REQUIRED")
    DocumentType documentType;
    String description;

    @Min(value = 0, message = "PRIORITY_INVALID")
    Integer priority;
    boolean syncImmediately;
}
