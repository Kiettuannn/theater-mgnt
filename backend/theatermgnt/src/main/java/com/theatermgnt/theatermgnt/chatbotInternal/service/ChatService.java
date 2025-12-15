package com.theatermgnt.theatermgnt.chatbotInternal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.request.ChatBotInternalRequest;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatBotInternalResponse;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {
    ChatClient chatClient;
    VectorStore vectorStore;
    JdbcChatMemoryRepository jdbcChatMemoryRepository;

    public ChatService(ChatClient.Builder builder, VectorStore vectorStore,
                       JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        this.vectorStore = vectorStore;
        this.jdbcChatMemoryRepository = jdbcChatMemoryRepository;

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(30)
                .build();
        chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }


    public ChatBotInternalResponse chat(ChatBotInternalRequest request) {

        try {
            List<Document> similarDocs = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(request.getQuery())
                            .topK(5)
                            .build());

            if (similarDocs == null || similarDocs.isEmpty()) {
                return ChatBotInternalResponse.builder()
                        .answer("Xin lỗi, tôi không tìm thấy thông tin này trong sổ tay quy định.")
                        .build();
            }


            // Build context
            String context = similarDocs.stream()
                    .map(Document::getText).collect(Collectors.joining("\n\n---\n\n"));


            String finalPrompt =
                    """
				Bạn là một trợ lý quản lý rạp chiếu phim chuyên nghiệp và hữu ích.

				Dưới đây là các quy định và thông tin nội bộ của rạp:
				---------------------
				%s
				---------------------

				YÊU CẦU:
				1. Chỉ trả lời dựa trên thông tin được cung cấp ở trên.
				2. Nếu thông tin không có trong đoạn văn trên, hãy nói: "Xin lỗi, tôi không tìm thấy thông tin này trong sổ tay quy định."
				3. Trả lời ngắn gọn, đúng trọng tâm, văn phong lịch sự.

				Câu hỏi của nhân viên: %s
				"""
                            .formatted(context, request.getQuery().trim());

            var contextHolder = SecurityContextHolder.getContext();
            String conversationId = contextHolder.getAuthentication().getName(); // AccountId

            String answer = chatClient.prompt()
                    .advisors(advisorSpec -> advisorSpec.param(
                            ChatMemory.CONVERSATION_ID, conversationId
                    ))
                    .user(finalPrompt)
                    .call()
                    .content();

            return ChatBotInternalResponse.builder()
                    .answer(answer)
                    .build();

        } catch (Exception e) {
            log.error("Chat error for query='{}'", request.getQuery(), e);
            return ChatBotInternalResponse.builder()
                    .answer("Đã xảy ra lỗi khi xử lý yêu cầu. Vui lòng thử lại sau.")
                    .build();
        }
    }

    /**
     * Xóa toàn bộ lịch sử chat của một conversation
     */
    public void clearConversation(String conversationId) {
        try {
            jdbcChatMemoryRepository.deleteByConversationId(conversationId);
            log.info("Cleared conversation: {}", conversationId);
        } catch (Exception e) {
            log.error("Error clearing conversation: {}", conversationId, e);
        }
    }

    /**
     * Xóa lịch sử chat của user hiện tại (lấy từ SecurityContext)
     */
    public void clearCurrentUserConversation() {
        try {
            var contextHolder = SecurityContextHolder.getContext();
            String accountId = contextHolder.getAuthentication().getName();
            clearConversation(accountId);
        } catch (Exception e) {
            log.error("Error clearing current user conversation", e);
        }
    }
}


