package com.theatermgnt.theatermgnt.room.dto.response;

import com.theatermgnt.theatermgnt.common.enums.RoomType;
import com.theatermgnt.theatermgnt.seat.dto.response.SeatResponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomResponse {
    String id;
    String cinemaId;
    String cinemaName;
    String name;
    String roomType;
    String status;
    Integer totalSeats;
    List<SeatResponse> seats;
}
