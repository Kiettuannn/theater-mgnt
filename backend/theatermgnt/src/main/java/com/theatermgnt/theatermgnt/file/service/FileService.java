package com.theatermgnt.theatermgnt.file.service;

import com.theatermgnt.theatermgnt.common.exception.AppException;
import com.theatermgnt.theatermgnt.common.exception.ErrorCode;
import com.theatermgnt.theatermgnt.file.dto.response.FileItemResponse;
import com.theatermgnt.theatermgnt.file.dto.response.FileUploadResponse;
import com.theatermgnt.theatermgnt.file.entity.FileMgnt;
import com.theatermgnt.theatermgnt.file.mapper.FileMgntMapper;
import com.theatermgnt.theatermgnt.file.repository.FileMgntRepository;
import com.theatermgnt.theatermgnt.file.repository.FileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {
    FileMgntRepository fileMgntRepository;
    FileRepository fileRepository;
    FileMgntMapper fileMgntMapper;

    public FileUploadResponse uploadFile(MultipartFile file) throws IOException {
        // Store file
        var fileInfo = fileRepository.store(file);

        // Create file management info
        var fileMgnt = fileMgntMapper.toFileMgmt(fileInfo);
        String accountId = SecurityContextHolder.getContext().getAuthentication().getName();
        fileMgnt.setOwnerId(accountId);
        fileMgnt = fileMgntRepository.save(fileMgnt);

        return FileUploadResponse.builder()
                .originalFileName(file.getOriginalFilename())
                .url(fileInfo.getUrl())
                .build();
    }

    public List<FileItemResponse> getAllFiles()  {
        return fileMgntRepository.findAll().stream()
                .map(fileMgntMapper::toFileResponse)
                .toList();
    }
    public FileItemResponse getFileById(String id) {
        FileMgnt file =  fileMgntRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));
        return fileMgntMapper.toFileResponse(file);
    }

    public void deleteFileById(String id) {
        if(!fileMgntRepository.existsById(id)) throw new AppException(ErrorCode.FILE_NOT_FOUND);
        fileMgntRepository.deleteById(id);
    }




}
