package com.theatermgnt.theatermgnt.chatbotInternal.controller;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.request.ChatBotInternalRequest;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatController {
    ChatClient chatClient;
    VectorStore vectorStore;

    public ChatController(ChatClient.Builder builder,
                          VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
    }

    @PostMapping
    public String chat(@RequestBody ChatBotInternalRequest request) {

        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.getQuery())
                        .topK(2)
                        .build()
        );
        String context = similarDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        String finalPrompt = """
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
    """.formatted(context, request.getQuery());

        return chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();
    }
}
