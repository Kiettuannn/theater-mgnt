package com.theatermgnt.theatermgnt.chatbotInternal.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SourceInfo {
    String documentName;
    int pageNumber;
    String section;
    float confidenceScore;
    String snippet;
}
