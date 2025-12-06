package com.theatermgnt.theatermgnt.cinema.dto.response;

import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CinemaResponse {
    String id;
    String name;
    String address;
    String city;
    String phoneNumber;
    String managerId;
    LocalDateTime createdAt;
}
