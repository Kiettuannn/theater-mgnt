package com.theatermgnt.theatermgnt.priceConfig.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.theatermgnt.theatermgnt.priceConfig.entity.PriceConfig;

public interface PriceConfigRepository extends JpaRepository<PriceConfig, String> {
    List<PriceConfig> findBySeatTypeId(String seatTypeId);
}
