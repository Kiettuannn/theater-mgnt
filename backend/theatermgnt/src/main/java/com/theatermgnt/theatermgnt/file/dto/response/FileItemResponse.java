package com.theatermgnt.theatermgnt.file.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileListResponse {
    String id;
    String url;
    String contentType;
    long size;
    LocalDateTime uploadDate;
}
