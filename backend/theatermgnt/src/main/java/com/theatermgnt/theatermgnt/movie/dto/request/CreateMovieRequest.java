package com.theatermgnt.theatermgnt.movie.dto.request;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.constraints.*;

import com.theatermgnt.theatermgnt.common.enums.MovieStatus;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateMovieRequest {

    @NotBlank(message = "INVALID_MOVIE_TITLE")
    @Size(max = 255, message = "INVALID_MOVIE_TITLE")
    String title;

    @NotBlank(message = "INVALID_MOVIE_DESCRIPTION")
    @Size(max = 5000, message = "INVALID_MOVIE_DESCRIPTION")
    String description;

    @NotNull(message = "INVALID_MOVIE_DURATION")
    @Min(value = 1, message = "INVALID_MOVIE_DURATION")
    @Max(value = 500, message = "INVALID_MOVIE_DURATION")
    Integer durationMinutes;

    @NotBlank(message = "INVALID_MOVIE_DIRECTOR")
    @Size(max = 255, message = "INVALID_MOVIE_DIRECTOR")
    String director;

    @NotBlank(message = "INVALID_MOVIE_CAST")
    @Size(max = 2000, message = "INVALID_MOVIE_CAST")
    String cast;

    @NotBlank(message = "INVALID_POSTER_URL")
    @Pattern(regexp = "^https?://.*", message = "INVALID_POSTER_URL")
    String posterUrl;

    @NotBlank(message = "INVALID_TRAILER_URL")
    @Pattern(regexp = "^https?://.*", message = "INVALID_TRAILER_URL")
    String trailerUrl;

    @NotNull(message = "INVALID_MOVIE_RELEASE_DATE")
    LocalDate releaseDate;

    @NotNull(message = "INVALID_MOVIE_END_DATE")
    LocalDate endDate;

    @NotBlank(message = "AGERATING_NOT_EXISTED")
    String ageRatingId;

    @NotNull(message = "INVALID_MOVIE_STATUS")
    MovieStatus status;

    @NotNull(message = "INVALID_MOVIE_GENRES")
    @Size(min = 1, max = 10, message = "INVALID_MOVIE_GENRES")
    Set<String> genreIds;
}
