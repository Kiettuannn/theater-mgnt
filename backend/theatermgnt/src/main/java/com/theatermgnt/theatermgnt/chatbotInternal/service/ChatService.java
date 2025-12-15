package com.theatermgnt.theatermgnt.chatbotInternal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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

    public ChatService(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
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

            String answer = chatClient.prompt().user(finalPrompt).call().content();

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
}


