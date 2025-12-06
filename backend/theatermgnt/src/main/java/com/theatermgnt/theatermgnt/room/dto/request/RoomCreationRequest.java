package com.theatermgnt.theatermgnt.room.dto.request;

import com.theatermgnt.theatermgnt.room.enums.RoomStatus;
import com.theatermgnt.theatermgnt.seat.dto.request.SeatRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.theatermgnt.theatermgnt.common.enums.RoomType;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomCreationRequest {

    @NotNull
    String cinemaId;

    @Size(min = 3, message = "ROOM_NAME_INVALID")
    String name;

    @NotNull
    RoomType roomType;

    RoomStatus status;

    List<SeatRequest> seats;
}
