package com.theatermgnt.theatermgnt.chatbotInternal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.theatermgnt.theatermgnt.chatbotInternal.constant.Sender;
import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.ChatMessageResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
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
    ChatMemory chatMemory;

    public ChatService(ChatClient.Builder builder, VectorStore vectorStore,
                       JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        this.vectorStore = vectorStore;
        this.jdbcChatMemoryRepository = jdbcChatMemoryRepository;

        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(30)
                .build();
        this.chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
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

            String systemInstruction =
                    """
				Bạn là một trợ lý quản lý rạp chiếu phim chuyên nghiệp và hữu ích.
				Bạn có khả năng ghi nhớ thông tin trong cuộc hội thoại để trả lời các câu hỏi tiếp theo.

				YÊU CẦU KHI TRẢ LỜI:
				1. Nếu câu hỏi liên quan đến quy định/chính sách rạp chiếu phim:
				   - Chỉ trả lời dựa trên thông tin từ tài liệu được cung cấp
				   - Nếu không tìm thấy thông tin trong tài liệu, hãy nói: "Xin lỗi, tôi không tìm thấy thông tin này trong sổ tay quy định."
				
				2. Nếu câu hỏi liên quan đến cuộc hội thoại hiện tại (ví dụ: "tôi tên gì?", "em nói gì vừa rồi?"):
				   - Sử dụng thông tin từ lịch sử chat để trả lời
				   - Tham khảo các tin nhắn trước đó trong cuộc trò chuyện
				
				3. Định dạng câu trả lời theo Markdown để dễ đọc:
				   - Sử dụng **in đậm** cho các thuật ngữ quan trọng
				   - Sử dụng dấu gạch đầu dòng (-) cho danh sách
				   - Xuống hàng giữa các ý chính
				   - Sử dụng số thứ tự (1., 2., 3.) cho các bước hoặc quy trình
				   - Sử dụng > cho lưu ý đặc biệt
				
				4. Trả lời ngắn gọn, đúng trọng tâm, văn phong lịch sự.
				""";

            String userMessageWithContext =
                    """
				Dưới đây là các quy định và thông tin nội bộ của rạp có thể liên quan:
				---------------------
				%s
				---------------------

				Câu hỏi: %s
				"""
                            .formatted(context, request.getQuery().trim());

            var contextHolder = SecurityContextHolder.getContext();
            String conversationId = contextHolder.getAuthentication().getName(); // AccountId

            String answer = chatClient.prompt()
                    .advisors(advisorSpec -> advisorSpec.param(
                            ChatMemory.CONVERSATION_ID, conversationId
                    ))
                    .system(systemInstruction)
                    .user(userMessageWithContext)
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


    public void clearCurrentUserConversation() {
        try {
            var contextHolder = SecurityContextHolder.getContext();
            String accountId = contextHolder.getAuthentication().getName();
            jdbcChatMemoryRepository.deleteByConversationId(accountId);
        } catch (Exception e) {
            log.error("Error clearing current user conversation", e);
        }
    }

    public List<ChatMessageResponse> getChatHistory(){
        var contextHolder = SecurityContextHolder.getContext();
        String conversationId = contextHolder.getAuthentication().getName();

        List<Message> messages= chatMemory.get(conversationId);
        if(messages==null || messages.isEmpty()){
            return Collections.emptyList();
        }
        return messages.stream()
                .map(msg -> ChatMessageResponse.builder()
                        .text(msg.getText())
                        .sender(msg instanceof UserMessage ? Sender.USER : Sender.BOT)
                        .timestamp(LocalDateTime.now())
                        .build()
        ).collect(Collectors.toList());
    }
}


