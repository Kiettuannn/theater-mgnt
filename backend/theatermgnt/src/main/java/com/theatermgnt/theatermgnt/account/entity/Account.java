package com.theatermgnt.theatermgnt.account.entity;

import java.time.Instant;

import com.theatermgnt.theatermgnt.common.entity.BaseEntity;
import jakarta.persistence.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.theatermgnt.theatermgnt.authentication.enums.AccountType;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "accounts")
@SQLDelete(sql = "UPDATE accounts SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Account extends BaseEntity {


    @Column(unique = true)
    String email;

    @Column(unique = true)
    String username;

    String password;


    @Enumerated(EnumType.STRING)
    AccountType accountType;

    Boolean isActive;

}
