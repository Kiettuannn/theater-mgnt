package com.theatermgnt.theatermgnt.chatbotInternal.mapper;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatbotDocumentResponse;
import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatbotDocumentMapper {
    @Mapping(target = "file", ignore = true)
    ChatbotDocumentResponse toChatbotDocumentResponse(ChatbotDocument chatbotDocument);
}
