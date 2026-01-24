package com.theatermgnt.theatermgnt.chatbotInternal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatbotDocumentResponse;
import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import com.theatermgnt.theatermgnt.file.mapper.FileMgntMapper;

@Mapper(
        componentModel = "spring",
        uses = {FileMgntMapper.class})
public interface ChatbotDocumentMapper {
    @Mapping(source = "fileMgnt", target = "file")
    @Mapping(source = "lastSyncedAt", target = "syncedAt")
    @Mapping(source = "createdAt", target = "addedAt")
    ChatbotDocumentResponse toChatbotDocumentResponse(ChatbotDocument chatbotDocument);
}
