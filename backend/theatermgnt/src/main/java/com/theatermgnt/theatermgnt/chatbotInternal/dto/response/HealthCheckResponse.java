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
public class HealthCheckResponse {
    boolean isConsistent;
    Integer expectedChunks;
    Integer actualChunks;
    Integer activeDocuments;
    Integer totalDocuments;
    String message;
}
