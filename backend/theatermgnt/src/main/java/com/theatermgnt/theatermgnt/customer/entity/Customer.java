package com.theatermgnt.theatermgnt.customer.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import com.theatermgnt.theatermgnt.account.entity.Account;
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
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    Account account;

    String firstName;
    String lastName;
    String address;
    String avatarUrl;

    @Enumerated(EnumType.STRING)
    Gender gender;

    LocalDate dob;
}
