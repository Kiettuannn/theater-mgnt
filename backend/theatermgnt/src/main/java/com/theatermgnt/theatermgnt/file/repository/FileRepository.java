package com.theatermgnt.theatermgnt.file.repository;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.theatermgnt.theatermgnt.file.dto.request.FileInfo;
import com.theatermgnt.theatermgnt.file.entity.FileMgnt;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class FileRepository {
    Cloudinary cloudinary;

    public FileInfo store(MultipartFile file) throws IOException {

        String fileName = UUID.randomUUID().toString();
        byte[] bytes = file.getBytes();
        

        Map params = ObjectUtils.asMap(
                "public_id", fileName,
                "folder", "theatermgnt_files",
                "resource_type", "auto"
        );
        
        Map uploadResult = cloudinary.uploader().upload(bytes, params);

        return FileInfo.builder()
                .name(fileName)
                .size(file.getSize())
                .contentType(file.getContentType())
                .md5Checksum(DigestUtils.md5DigestAsHex(bytes))
                .url(uploadResult.get("secure_url").toString())
                .originalFileName(file.getOriginalFilename())
                .uploadTime(LocalDateTime.now())
                .build();
    }
}
