package com.theatermgnt.theatermgnt.file.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileItemResponse {
    String id;
    String url;
    String originalFileName;
    String contentType;
    long size;
    LocalDateTime uploadDate;
}
