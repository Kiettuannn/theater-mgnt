package com.theatermgnt.theatermgnt.staff.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.theatermgnt.theatermgnt.staff.entity.Staff;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {
    Optional<Staff> findByAccountId(String accountId);

    List<Staff> findAllByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
}
