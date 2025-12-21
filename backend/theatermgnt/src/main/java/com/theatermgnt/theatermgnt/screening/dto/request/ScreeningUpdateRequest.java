package com.theatermgnt.theatermgnt.screening.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScreeningUpdateRequest {
    @NotNull
    String roomId;

    @NotNull
    String movieId;

    @NotNull
    LocalDateTime startTime;

    @NotNull
    LocalDateTime endTime;
}
