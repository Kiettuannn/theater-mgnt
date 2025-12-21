package com.theatermgnt.theatermgnt.screeningSeat.mapper;


import com.theatermgnt.theatermgnt.screeningSeat.dto.request.ScreeningSeatCreationRequest;
import com.theatermgnt.theatermgnt.screeningSeat.dto.request.ScreeningSeatUpdateRequest;
import com.theatermgnt.theatermgnt.screeningSeat.dto.response.ScreeningSeatResponse;
import com.theatermgnt.theatermgnt.screeningSeat.entity.ScreeningSeat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ScreeningSeatMapper {
    @Mapping(target = "screening", ignore = true)
    @Mapping(target = "seat", ignore = true)
    ScreeningSeat toScreeningSeat(ScreeningSeatCreationRequest request);

    @Mapping(target = "screeningId", source = "screening.id")
    @Mapping(target = "seatId", source = "seat.id")
    ScreeningSeatResponse toScreeningSeatResponse(ScreeningSeat screeningSeat);

    void updateScreeningSeat(@MappingTarget ScreeningSeat screeningSeat, ScreeningSeatUpdateRequest request);
}
