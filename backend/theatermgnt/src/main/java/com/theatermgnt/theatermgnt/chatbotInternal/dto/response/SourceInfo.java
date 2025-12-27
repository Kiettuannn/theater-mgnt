package com.theatermgnt.theatermgnt.chatbotInternal.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SourceInfo {
    String fileId;
    String fileName;
    String filePath;
    String documentType;
    Integer priority;
    Set<Integer> chunkIndices;
    Set<String> sectionTitles;
}
