package com.theatermgnt.theatermgnt.movie.service;

import java.util.List;

import org.springframework.stereotype.Service;

import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.movie.dto.request.CreateAgeRatingRequest;
import com.theatermgnt.theatermgnt.movie.dto.response.AgeRatingResponse;
import com.theatermgnt.theatermgnt.movie.dto.response.MovieSimpleResponse;
import com.theatermgnt.theatermgnt.movie.entity.AgeRating;
import com.theatermgnt.theatermgnt.movie.mapper.AgeRatingMapper;
import com.theatermgnt.theatermgnt.movie.mapper.MovieMapper;
import com.theatermgnt.theatermgnt.movie.repository.AgeRatingRepository;
import com.theatermgnt.theatermgnt.movie.repository.MovieRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AgeRatingService {

    AgeRatingRepository ageRatingRepository;
    AgeRatingMapper ageRatingMapper;
    MovieRepository movieRepository;
    MovieMapper movieMapper;

    // CREATE
    public AgeRatingResponse createAgeRating(CreateAgeRatingRequest request) {
        // Kiểm tra ID đã tồn tại chưa
        if (ageRatingRepository.existsById(request.getId())) {
            throw new AppException(ErrorCode.AGERATING_EXISTED);
        }

        // Kiểm tra Code đã tồn tại chưa
        if (ageRatingRepository.findByCode(request.getCode()).isPresent()) {
            throw new AppException(ErrorCode.AGERATING_CODE_EXISTED);
        }

        AgeRating ageRating = ageRatingMapper.toAgeRating(request);
        AgeRating savedAgeRating = ageRatingRepository.save(ageRating);
        return ageRatingMapper.toAgeRatingResponse(savedAgeRating);
    }

    // READ
    public List<AgeRatingResponse> getAllAgeRatings() {
        List<AgeRating> ageRatings = ageRatingRepository.findAll();
        return ageRatingMapper.toAgeRatingResponseList(ageRatings);
    }

    public AgeRatingResponse getAgeRatingById(String id) {
        AgeRating ageRating =
                ageRatingRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.AGERATING_NOT_EXISTED));
        return ageRatingMapper.toAgeRatingResponse(ageRating);
    }

    public AgeRatingResponse getAgeRatingByCode(String code) {
        AgeRating ageRating = ageRatingRepository
                .findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.AGERATING_NOT_EXISTED));
        return ageRatingMapper.toAgeRatingResponse(ageRating);
    }

    // Get movies using this age rating
    public List<MovieSimpleResponse> getMoviesUsingAgeRating(String id) {
        if (!ageRatingRepository.existsById(id)) {
            throw new AppException(ErrorCode.AGERATING_NOT_EXISTED);
        }
        return movieRepository.findByAgeRatingId(id).stream()
                .map(movieMapper::toMovieSimpleResponse)
                .toList();
    }

    // DELETE
    public void deleteAgeRating(String id) {
        if (!ageRatingRepository.existsById(id)) {
            throw new AppException(ErrorCode.AGERATING_NOT_EXISTED);
        }

        // Check if age rating is being used by any movies
        if (movieRepository.existsByAgeRatingId(id)) {
            throw new AppException(ErrorCode.AGERATING_IN_USE);
        }

        ageRatingRepository.deleteById(id);
    }
}
