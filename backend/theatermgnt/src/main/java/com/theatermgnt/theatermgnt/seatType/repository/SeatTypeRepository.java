package com.theatermgnt.theatermgnt.seatType.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatTypeRepository extends JpaRepository<SeatType,String> {
    boolean existsByTypeName(String typeName);
}
