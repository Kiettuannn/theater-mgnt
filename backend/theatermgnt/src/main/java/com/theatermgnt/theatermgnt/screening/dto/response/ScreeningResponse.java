package com.theatermgnt.theatermgnt.screening.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScreeningResponse {
    String id;
    String movieName;
    String roomName;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String status;
}
