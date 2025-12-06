package com.theatermgnt.theatermgnt.seatType.repository;

import com.theatermgnt.theatermgnt.seatType.entity.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SeatTypeRepository extends JpaRepository<SeatType,String> {
    boolean existsByTypeName(String typeName);
}
