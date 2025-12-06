package com.theatermgnt.theatermgnt.room.repository;

import com.theatermgnt.theatermgnt.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room,String> {
    List<Room> findByCinemaId(String cinemaId);
    boolean existsByNameAndCinemaId(String name, String cinemaId);
    boolean existsByNameAndCinemaIdAndIdNot(String name, String cinemaId, String id);
}
