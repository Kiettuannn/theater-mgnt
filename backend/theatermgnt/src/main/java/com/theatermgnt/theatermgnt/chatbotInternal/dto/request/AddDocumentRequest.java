package com.theatermgnt.theatermgnt.chatbotInternal.dto.request;

import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddDocumentRequest {
    @NotBlank
    String fileId;

    @NonNull
    DocumentType documentType;
    String description;

    @Size(min = 1, max = 100, message = "PRIORITY_INVALID")
    Integer priority;
    boolean syncImmediately;
}
