package com.theatermgnt.theatermgnt.file.controller;

import com.theatermgnt.theatermgnt.common.dto.response.ApiResponse;
import com.theatermgnt.theatermgnt.file.dto.response.FileItemResponse;
import com.theatermgnt.theatermgnt.file.dto.response.FileUploadResponse;
import com.theatermgnt.theatermgnt.file.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/media")
public class FileController {
    FileService fileService;

    @PostMapping("/upload")
    ApiResponse<FileUploadResponse> uploadMedia(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.<FileUploadResponse>builder()
                .result(fileService.uploadFile(file))
                .build();
    }

    @GetMapping
    ApiResponse<List<FileItemResponse>> getAllFiles() {
        return ApiResponse.<List<FileItemResponse>>builder()
                .result(fileService.getAllFiles())
                .build();
    }

    @GetMapping("/{fileId}")
    ApiResponse<FileItemResponse> getFileById(@PathVariable String fileId) {
        return ApiResponse.<FileItemResponse>builder()
                .result(fileService.getFileById(fileId))
                .build();
    }

    @DeleteMapping("/{fileId}")
    ApiResponse<Void> deleteFileById(@PathVariable String fileId) {
        fileService.deleteFileById(fileId);
        return ApiResponse.<Void>builder()
                .build();
    }
}