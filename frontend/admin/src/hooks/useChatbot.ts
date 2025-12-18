import { useState, useCallback, useRef } from "react";
import { chatService } from "@/services/chatService";
import { useConfirmDialog } from "./useConfirmDialog";

export interface Message {
  id: string;
  text: string;
  sender: "user" | "bot";
  timestamp: Date;
}

const WELCOME_MESSAGE: Message = {
  id: "1",
  text: "Xin chào! Tôi có thể giúp gì cho bạn?",
  sender: "bot",
  timestamp: new Date(),
};

export function useChatbot() {
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [messages, setMessages] = useState<Message[]>([WELCOME_MESSAGE]);
  const [inputValue, setInputValue] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const { confirmDialog, showConfirmDialog, closeConfirmDialog } =
    useConfirmDialog();

  // Scroll to bottom
  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

  // Load chat history
  const loadChatHistory = useCallback(async () => {
    try {
      const history = await chatService.getChatHistory();

      if (history.length === 0) {
        setMessages([WELCOME_MESSAGE]);
      } else {
        setMessages(history);
      }
    } catch (error) {
      console.error("Error loading chat history:", error);
      setMessages([WELCOME_MESSAGE]);
    }
  }, []);

  // Send message
  const handleSendMessage = useCallback(async () => {
    if (inputValue.trim() === "") return;

    const userMessage: Message = {
      id: Date.now().toString(),
      text: inputValue,
      sender: "user",
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    const currentQuery = inputValue;
    setInputValue("");
    setIsLoading(true);

    try {
      const response = await chatService.sendMessage({
        query: currentQuery,
      });

      const botMessage: Message = {
        id: (Date.now() + 1).toString(),
        text: response.answer,
        sender: "bot",
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, botMessage]);
    } catch (error) {
      console.error("Chat error:", error);
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        text: "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.",
        sender: "bot",
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  }, [inputValue]);

  // Handle key press
  const handleKeyPress = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "Enter" && !isLoading) {
        handleSendMessage();
      }
    },
    [isLoading, handleSendMessage]
  );

  // Clear chat history with confirmation
  const handleClearChat = useCallback(() => {
    showConfirmDialog({
      title: "Xoá lịch sử chat",
      description:
        "Bạn có chắc chắn muốn xoá toàn bộ lịch sử trò chuyện? Hành động này không thể hoàn tác.",
      confirmText: "Xoá",
      variant: "destructive",
      onConfirm: async () => {
        try {
          await chatService.clearConversation();
          setMessages([WELCOME_MESSAGE]);
          closeConfirmDialog();
        } catch (error) {
          console.error("Error clearing conversation:", error);
        }
      },
    });
  }, [showConfirmDialog, closeConfirmDialog]);

  // Toggle chat window
  const toggleChat = useCallback(() => {
    setIsOpen((prev) => !prev);
  }, []);

  return {
    // State
    isOpen,
    isLoading,
    messages,
    inputValue,
    messagesEndRef,
    confirmDialog,

    // Actions
    setInputValue,
    handleSendMessage,
    handleKeyPress,
    handleClearChat,
    toggleChat,
    loadChatHistory,
    scrollToBottom,
    closeConfirmDialog,
  };
}
