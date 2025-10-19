package com.theatermgnt.theatermgnt.repository;

import com.theatermgnt.theatermgnt.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, String> {
}
