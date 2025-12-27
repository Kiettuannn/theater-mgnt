package com.theatermgnt.theatermgnt.screeningSeat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.screening.entity.Screening;
import com.theatermgnt.theatermgnt.screening.repository.ScreeningRepository;
import com.theatermgnt.theatermgnt.screeningSeat.dto.request.ScreeningSeatCreationRequest;
import com.theatermgnt.theatermgnt.screeningSeat.dto.request.ScreeningSeatUpdateRequest;
import com.theatermgnt.theatermgnt.screeningSeat.dto.response.ScreeningSeatResponse;
import com.theatermgnt.theatermgnt.screeningSeat.entity.ScreeningSeat;
import com.theatermgnt.theatermgnt.screeningSeat.enums.ScreeningSeatStatus;
import com.theatermgnt.theatermgnt.screeningSeat.mapper.ScreeningSeatMapper;
import com.theatermgnt.theatermgnt.screeningSeat.repository.ScreeningSeatRepository;
import com.theatermgnt.theatermgnt.seat.entity.Seat;
import com.theatermgnt.theatermgnt.seat.repository.SeatRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScreeningSeatService {
    SeatRepository seatRepository;
    ScreeningRepository screeningRepository;
    ScreeningSeatRepository screeningSeatRepository;
    ScreeningSeatMapper screeningSeatMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public ScreeningSeatResponse createScreeningSeat(ScreeningSeatCreationRequest request) {
        Screening screening = screeningRepository
                .findById(request.getScreeningId())
                .orElseThrow(() -> new AppException(ErrorCode.SCREENING_NOT_EXISTED));

        Seat seat = seatRepository
                .findById(request.getSeatId())
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_EXISTED));

        if (screeningSeatRepository.existsByScreeningIdAndSeatId(request.getScreeningId(), request.getSeatId()))
            throw new AppException(ErrorCode.SCREENING_SEAT_EXISTED);

        if (!seat.getRoom().getId().equals(screening.getRoom().getId()))
            throw new AppException(ErrorCode.SEAT_NOT_IN_ROOM);

        ScreeningSeat screeningSeat = screeningSeatMapper.toScreeningSeat(request);
        screeningSeat.setSeat(seat);
        screeningSeat.setScreening(screening);
        screeningSeat.setStatus(ScreeningSeatStatus.AVAILABLE);

        return screeningSeatMapper.toScreeningSeatResponse(screeningSeatRepository.save(screeningSeat));
    }

    public List<ScreeningSeatResponse> getScreeningSeatsByScreeningId(String screeningId) {
        return screeningSeatRepository.findByScreeningId(screeningId).stream()
                .map(screeningSeatMapper::toScreeningSeatResponse)
                .toList();
    }

    public List<ScreeningSeatResponse> getScreeningSeatsBySeatId(String seatId) {
        return screeningSeatRepository.findBySeatId(seatId).stream()
                .map(screeningSeatMapper::toScreeningSeatResponse)
                .toList();
    }

    public List<ScreeningSeatResponse> getScreeningSeats() {
        return screeningSeatRepository.findAll().stream()
                .map(screeningSeatMapper::toScreeningSeatResponse)
                .toList();
    }

    public ScreeningSeatResponse getScreeningSeat(String screeningSeatId) {
        ScreeningSeat screeningSeat = screeningSeatRepository
                .findById(screeningSeatId)
                .orElseThrow(() -> new AppException(ErrorCode.SCREENING_SEAT_NOT_EXISTED));
        return screeningSeatMapper.toScreeningSeatResponse(screeningSeat);
    }

    public ScreeningSeatResponse updateScreeningSeat(String screeningSeatId, ScreeningSeatUpdateRequest request) {
        ScreeningSeat screeningSeat = screeningSeatRepository
                .findById(screeningSeatId)
                .orElseThrow(() -> new AppException(ErrorCode.SCREENING_SEAT_NOT_EXISTED));

        if (screeningSeat.getStatus() == ScreeningSeatStatus.SOLD
                && request.getStatus() == ScreeningSeatStatus.AVAILABLE) {
            throw new AppException(ErrorCode.SCREENING_SEAT_INVALID_STATUS_CHANGE);
        }

        screeningSeatMapper.updateScreeningSeat(screeningSeat, request);
        return screeningSeatMapper.toScreeningSeatResponse(screeningSeatRepository.save(screeningSeat));
    }

    public void deleteScreeningSeat(String screeningSeatId) {
        ScreeningSeat ss = screeningSeatRepository
                .findById(screeningSeatId)
                .orElseThrow(() -> new AppException(ErrorCode.SCREENING_SEAT_NOT_EXISTED));
        if (ss.getStatus() == ScreeningSeatStatus.SOLD) throw new AppException(ErrorCode.SCREENING_SEAT_CANNOT_DELETE);
        screeningSeatRepository.deleteById(screeningSeatId);
    }
}
