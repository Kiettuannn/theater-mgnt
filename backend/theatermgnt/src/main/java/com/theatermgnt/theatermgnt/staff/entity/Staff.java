package com.theatermgnt.theatermgnt.staff.entity;

import com.theatermgnt.theatermgnt.account.entity.Account;
import com.theatermgnt.theatermgnt.authorization.entity.Role;
import com.theatermgnt.theatermgnt.common.entity.BaseEntity;
import com.theatermgnt.theatermgnt.common.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "staffs")
@SQLDelete(sql = "UPDATE staffs SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Staff extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    Account account;

    String cinemaId;

    String firstName;
    String lastName;
    String phoneNumber;
    String jobTitle;
    String address;
    String avatarUrl;
    LocalDate dob;

    @Enumerated(EnumType.STRING)
    Gender gender;

    @ManyToMany
    Set<Role> roles;
}
