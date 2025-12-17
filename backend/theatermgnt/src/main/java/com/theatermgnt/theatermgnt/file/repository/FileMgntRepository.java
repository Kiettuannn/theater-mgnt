package com.theatermgnt.theatermgnt.file.repository;

import com.theatermgnt.theatermgnt.file.entity.FileMgnt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMgntRepository extends JpaRepository<FileMgnt, String> {
}
