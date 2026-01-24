package com.theatermgnt.theatermgnt.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.file.entity.FileMgnt;

@Repository
public interface FileMgntRepository extends JpaRepository<FileMgnt, String> {}
