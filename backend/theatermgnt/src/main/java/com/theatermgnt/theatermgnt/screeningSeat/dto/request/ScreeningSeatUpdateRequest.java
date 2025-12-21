package com.theatermgnt.theatermgnt.screeningSeat.dto.request;

import com.theatermgnt.theatermgnt.screeningSeat.enums.ScreeningSeatStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScreeningSeatUpdateRequest {

    @NotNull
    String bookingId;

    @NotNull
    ScreeningSeatStatus status;
}
