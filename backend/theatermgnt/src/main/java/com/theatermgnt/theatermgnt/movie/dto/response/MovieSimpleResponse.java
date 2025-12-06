package com.theatermgnt.theatermgnt.movie.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.theatermgnt.theatermgnt.common.enums.MovieStatus;
import com.theatermgnt.theatermgnt.movie.entity.Movie;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Response đơn giản cho danh sách phim
 * Chỉ chứa thông tin cần thiết để hiển thị trong list/card view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieSimpleResponse {

    String id;
    String title;
    String posterUrl;
    Integer durationMinutes;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate releaseDate;

    MovieStatus status;
    String ageRatingCode;
    String director;
}
