package com.theatermgnt.theatermgnt.file.mapper;

import com.theatermgnt.theatermgnt.file.dto.request.FileInfo;
import com.theatermgnt.theatermgnt.file.dto.response.FileItemResponse;
import com.theatermgnt.theatermgnt.file.entity.FileMgnt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMgntMapper {
    @Mapping(target = "cloudPublicId", source = "name")
    @Mapping(target = "id", ignore = true)
    FileMgnt toFileMgmt(FileInfo fileInfo);

    @Mapping(target = "uploadDate", source = "createdAt")
    FileItemResponse toFileResponse(FileMgnt fileMgmt);
}
