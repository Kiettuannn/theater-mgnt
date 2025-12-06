package com.theatermgnt.theatermgnt.staff.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.*;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.authorization.entity.Role;
import com.theatermgnt.theatermgnt.common.enums.Gender;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "staffs")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    Account account;

    String cinemaId;

    String firstName;
    String lastName;
    String jobTitle;
    String address;
    String avatarUrl;
    LocalDate dob;

    @Enumerated(EnumType.STRING)
    Gender gender;

    @ManyToMany
    Set<Role> roles;
}
