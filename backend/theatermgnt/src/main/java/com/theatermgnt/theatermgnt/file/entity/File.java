package com.theatermgnt.theatermgnt.file.entity;

import com.theatermgnt.theatermgnt.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "files")
@SQLDelete(sql = "UPDATE files SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class File extends BaseEntity {
    String ownerId;
    String contentType;
    long size;
    String md5Checksum;
    String url;
    String cloudPublicId;
}
