package com.theatermgnt.theatermgnt.chatbotInternal.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {
    String text;
    String sender;
    LocalDateTime timestamp;
    List<SourceInfo> sources;
}
