package com.theatermgnt.theatermgnt.screeningSeat.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScreeningSeatResponse {
    String id;
    String screeningId;
    String seatId;
    String bookingId;
    String status;
}
