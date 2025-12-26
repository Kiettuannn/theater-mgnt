package com.theatermgnt.theatermgnt.chatbotInternal.service;

import com.theatermgnt.theatermgnt.chatbotInternal.dto.response.SourceInfo;
import com.theatermgnt.theatermgnt.chatbotInternal.entity.ChatbotDocument;
import com.theatermgnt.theatermgnt.chatbotInternal.repository.ChatbotDocumentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builder class for constructing AI prompts used in chatbot conversations.
 * Separates prompt templates and formatting logic from business logic.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatPromptBuilder {

    public String buildSystemInstruction(String documentCatalog) {
        return """
                Bạn là một trợ lý quản lý rạp chiếu phim chuyên nghiệp và hữu ích.
                Bạn có khả năng ghi nhớ thông tin trong cuộc hội thoại để trả lời các câu hỏi tiếp theo.

                TÀI LIỆU KHẢ DỤNG:
                %s

                YÊU CẦU KHI TRẢ LỜI:
                1. Nếu câu hỏi liên quan đến quy định/chính sách:
                   - Chỉ trả lời dựa trên thông tin từ tài liệu được cung cấp
                   - ĐỌC KỸ tài liệu và XÁC ĐỊNH MỤC/PHẦN chứa thông tin (nếu có tiêu đề dạng "1. ABC", "2. XYZ")
                   - LUÔN trích dẫn theo format: "Theo **[tên-file.pdf]**, [mục X. TÊN MỤC nếu có], ..."
                   
                   VÍ DỤ TRÍCH DẪN ĐÚNG:
                   ✅ "Theo **policy-01.pdf**, mục 2. DIỆN MẠO VÀ ĐỒNG PHỤC, nhân viên phải..."
                   ✅ "Theo **handbook.pdf**, mục 1. QUY ĐỊNH CHUNG, ca làm việc..."
                   ✅ "Theo **policy-01.pdf**, nhân viên cần..." (nếu không rõ mục)
                   
                   VÍ DỤ TRÍCH DẪN SAI:
                   ❌ "(Nguồn 1, Phần 1)" - TUYỆT ĐỐI KHÔNG viết kiểu này
                   ❌ "Nguồn 3 cho biết..." - TUYỆT ĐỐI KHÔNG viết kiểu này
                   ❌ "Theo tài liệu..." - Phải ghi RÕ tên file
                   
                   - Nếu không tìm thấy: "Xin lỗi, tôi không tìm thấy thông tin này..."
                
                2. Nếu câu hỏi về cuộc hội thoại hiện tại:
                   - Sử dụng thông tin từ lịch sử chat
                
                3. Quy tắc ưu tiên:
                   - POLICY (Chính sách) > HANDBOOK (Sổ tay) > GUIDELINE > FAQ
                   - Priority 1 > Priority 2 > Priority 3
                   - Nếu có mâu thuẫn, ưu tiên nguồn có độ ưu tiên cao và nêu rõ
                
                4. Định dạng Markdown để dễ đọc:
                   - **in đậm** cho thuật ngữ quan trọng
                   - Dấu gạch đầu dòng (-) cho danh sách
                   - Số thứ tự (1., 2., 3.) cho quy trình
                   - > cho lưu ý đặc biệt
                """.formatted(documentCatalog);
    }

    public String buildContextSection(List<Document> documents) {
        String structuredContext = buildStructuredContext(documents);
        return """
                CÁC TÀI LIỆU THAM KHẢO:
                %s
                """.formatted(structuredContext);
    }


    public String buildDocumentCatalog(List<SourceInfo> sources) {
        return sources.stream()
                .map(info -> String.format("- %s (%s) [Priority: %d]",
                        info.getFileName(),
                        info.getDocumentType(),
                        info.getPriority()
                ))
                .collect(Collectors.joining("\n"));
    }


    public String buildStructuredContext(List<Document> documents) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            Map<String, Object> metadata = doc.getMetadata();

            String fileName = metadata.getOrDefault("fileName", "Unknown Document").toString();
            String docType = metadata.getOrDefault("documentType", "POLICY").toString();
            Integer chunkIndex = (Integer) metadata.get("chunkIndex");
            String sectionFullTitle = metadata.get("sectionFullTitle") != null ?
                    metadata.get("sectionFullTitle").toString() : null;

            builder.append(String.format(
                    "\n=== TÀI LIỆU: %s (Loại: %s) ===\n",
                    fileName,
                    docType
            ));
            if (sectionFullTitle != null) {
                builder.append(String.format(
                        "Mục: %s\n",
                        sectionFullTitle
                ));
            }
            builder.append(String.format(
                    "[Đoạn %d]\n\n",
                    chunkIndex != null ? chunkIndex + 1 : 0
            ));
            builder.append(doc.getText()).append("\n");
            builder.append("=====================================\n");
        }
        return builder.toString();
    }
}
