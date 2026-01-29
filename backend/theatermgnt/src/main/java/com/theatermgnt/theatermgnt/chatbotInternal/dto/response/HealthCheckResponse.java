package com.theatermgnt.theatermgnt.chatbotInternal.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
