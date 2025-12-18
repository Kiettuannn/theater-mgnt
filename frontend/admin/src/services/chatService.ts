import httpClient from "../configurations/httpClient";

export interface ChatRequest {
  query: string;
  // Không cần conversationId và userId nữa - backend tự lấy từ SecurityContext
}

export interface ChatResponse {
  answer: string;
  // Không cần conversationId trong response
}

export interface ChatMessage {
  id: string;
  text: string;
  sender: "user" | "bot";
  timestamp: Date;
}

export const chatService = {
  /**
   * Gửi tin nhắn đến chatbot
   */
  sendMessage: async (request: ChatRequest): Promise<ChatResponse> => {
    const response = await httpClient.post<ChatResponse>("/chat", request);
    return response.data;
  },

  /**
   * Xóa lịch sử chat của user hiện tại
   */
  clearConversation: async (): Promise<void> => {
    await httpClient.delete("/chat/conversation");
  },

  getChatHistory: async (): Promise<ChatMessage[]> => {
    const response = await httpClient.get<ChatMessage[]>("/chat/history");
    
    // Convert timestamp string sang Date object
    return response.data.map((msg, index) => ({
      ...msg,
      id: `history-${index}`,
      timestamp: new Date(msg.timestamp),
    }));
  },
};
