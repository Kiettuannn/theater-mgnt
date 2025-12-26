package com.theatermgnt.theatermgnt.movie.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.movie.dto.request.CreateGenreRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.GenreResponse;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieSimpleResponse;
import com.theatermgnt.theatermgnt.movie.entity.Genre;
import com.theatermgnt.theatermgnt.movie.mapper.GenreMapper;
import com.theatermgnt.theatermgnt.movie.mapper.MovieMapper;
import com.theatermgnt.theatermgnt.movie.repository.GenreRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreService {

    GenreRepository genreRepository;
    GenreMapper genreMapper;
    MovieMapper movieMapper;

    // CREATE
    public GenreResponse createGenre(CreateGenreRequest request) {
        // Kiểm tra ID đã tồn tại
        if (genreRepository.existsById(request.getId())) {
            throw new AppException(ErrorCode.GENRE_EXISTED);
        }

        // Kiểm tra Name đã tồn tại
        if (genreRepository.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.GENRE_NAME_EXISTED);
        }

        // Dùng mapper thay vì manual mapping
        Genre genre = genreMapper.toGenre(request);
        Genre savedGenre = genreRepository.save(genre);
        return genreMapper.toGenreResponse(savedGenre);
    }

    // READ
    public List<GenreResponse> getAllGenres() {
        List<Genre> genres = genreRepository.findAll();
        return genreMapper.toGenreResponseList(genres);
    }

    public GenreResponse getGenreById(String id) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTED));
        return genreMapper.toGenreResponse(genre);
    }

    public GenreResponse getGenreByName(String name) {
        Genre genre = genreRepository.findByName(name).orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTED));
        return genreMapper.toGenreResponse(genre);
    }

    // Get movies using this genre
    public List<MovieSimpleResponse> getMoviesUsingGenre(String id) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTED));
        return genre.getMovies().stream()
                .map(movieMapper::toMovieSimpleResponse)
                .toList();
    }

    // DELETE
    public void deleteGenre(String id) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTED));

        // Check if genre is being used by any movies
        if (genre.getMovies() != null && !genre.getMovies().isEmpty()) {
            throw new AppException(ErrorCode.GENRE_IN_USE);
        }

        genreRepository.deleteById(id);
    }
}
