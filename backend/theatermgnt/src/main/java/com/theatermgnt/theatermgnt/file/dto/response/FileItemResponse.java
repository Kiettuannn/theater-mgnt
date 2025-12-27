package com.theatermgnt.theatermgnt.file.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
