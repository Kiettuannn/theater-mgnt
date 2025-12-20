package com.theatermgnt.theatermgnt.chatbotInternal.dto.request;

import com.theatermgnt.theatermgnt.chatbotInternal.enums.DocumentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddDocumentRequest {
    String fileId;
    DocumentType documentType;
    String description;
    Integer priority;
    boolean syncImmediately;
}
