import { useEffect } from "react";
import { MessageCircle, X, Send, Trash2 } from "lucide-react";
import { useAuthStore } from "@/stores/useAuthStore";
import { useChatbot } from "@/hooks/useChatbot";
import { ConfirmDialog } from "@/components/ui/ConfirmDialog";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";

export const ChatbotWidget = () => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  const {
    isOpen,
    isLoading,
    messages,
    inputValue,
    messagesEndRef,
    confirmDialog,
    setInputValue,
    handleSendMessage,
    handleKeyPress,
    handleClearChat,
    toggleChat,
    loadChatHistory,
    scrollToBottom,
    closeConfirmDialog,
  } = useChatbot();

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  useEffect(() => {
    if (isOpen) {
      loadChatHistory();
    }
  }, [isOpen, loadChatHistory]);

  if (!isAuthenticated) {
    return null;
  }

  return (
    <>
      <ConfirmDialog
        isOpen={confirmDialog.isOpen}
        onClose={closeConfirmDialog}
        onConfirm={confirmDialog.onConfirm}
        title={confirmDialog.title}
        description={confirmDialog.description}
        confirmText={confirmDialog.confirmText || "Confirm"}
        cancelText="Huỷ"
        variant={confirmDialog.variant || "destructive"}
      />
      <div className="fixed bottom-6 right-6 z-50">
        {/* Chat Window */}
        {isOpen && (
          <div className="mb-4 w-[380px] h-[500px] bg-white rounded-lg shadow-2xl flex flex-col border border-gray-200">
            {/* Header */}
            <div className="bg-gradient-to-r from-blue-500 to-blue-600 text-white p-4 rounded-t-lg flex justify-between items-center">
              <div className="flex items-center gap-2">
                <MessageCircle size={24} />
                <div>
                  <h3 className="font-semibold">Trợ lý ảo</h3>
                  <p className="text-xs text-blue-100">
                    Luôn sẵn sàng hỗ trợ bạn!
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={handleClearChat}
                  className="hover:bg-blue-400 rounded-full p-1.5 transition-colors cursor-pointer"
                  title="New Chat"
                >
                  <div className="flex items-center gap-2">
                    <Trash2 size={20} />
                    <span>Xoá lịch sử</span>
                  </div>
                </button>
                <button
                  onClick={toggleChat}
                  className="hover:bg-blue-400 rounded-full p-1.5 transition-colors"
                >
                  <X size={20} />
                </button>
              </div>
            </div>

            {/* Messages Container */}
            <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
              {messages.map((message) => (
                <div
                  key={message.id}
                  className={`flex ${
                    message.sender === "user" ? "justify-end" : "justify-start"
                  }`}
                >
                  <div
                    className={`max-w-[70%] rounded-lg p-3 ${
                      message.sender === "user"
                        ? "bg-blue-500 text-white"
                        : "bg-white text-gray-800 shadow-sm border border-gray-200"
                    }`}
                  >
                    <div
                      className={`text-sm prose prose-sm max-w-none ${
                        message.sender === "user"
                          ? "prose-invert"
                          : "prose-slate"
                      }`}
                    >
                      <ReactMarkdown
                        remarkPlugins={[remarkGfm]}
                        components={{
                          p: ({ children }) => (
                            <p className="mb-2 last:mb-0">{children}</p>
                          ),
                          ul: ({ children }) => (
                            <ul className="mb-2 ml-4 list-disc">{children}</ul>
                          ),
                          ol: ({ children }) => (
                            <ol className="mb-2 ml-4 list-decimal">
                              {children}
                            </ol>
                          ),
                          li: ({ children }) => (
                            <li className="mb-1">{children}</li>
                          ),
                          strong: ({ children }) => (
                            <strong className="font-semibold">
                              {children}
                            </strong>
                          ),
                          blockquote: ({ children }) => (
                            <blockquote className="border-l-4 border-blue-400 pl-3 italic my-2">
                              {children}
                            </blockquote>
                          ),
                        }}
                      >
                        {message.text}
                      </ReactMarkdown>
                    </div>
                    <span
                      className={`text-xs mt-1 block ${
                        message.sender === "user"
                          ? "text-blue-50"
                          : "text-gray-400"
                      }`}
                    >
                      {message.timestamp.toLocaleTimeString("vi-VN", {
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </span>
                  </div>
                </div>
              ))}
              {isLoading && (
                <div className="flex justify-start">
                  <div className="max-w-[70%] rounded-lg p-3 bg-white shadow-sm border border-gray-200">
                    <div className="flex gap-1">
                      <span
                        className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                        style={{ animationDelay: "0ms" }}
                      ></span>
                      <span
                        className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                        style={{ animationDelay: "150ms" }}
                      ></span>
                      <span
                        className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                        style={{ animationDelay: "300ms" }}
                      ></span>
                    </div>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>

            {/* Input Area */}
            <div className="p-4 border-t border-gray-200 bg-white rounded-b-lg">
              <div className="flex gap-2">
                <input
                  type="text"
                  value={inputValue}
                  onChange={(e) => setInputValue(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="Nhập tin nhắn..."
                  disabled={isLoading}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm disabled:bg-gray-100 disabled:cursor-not-allowed"
                />
                <button
                  onClick={handleSendMessage}
                  disabled={isLoading}
                  className="bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors flex items-center justify-center disabled:bg-gray-400 disabled:cursor-not-allowed"
                >
                  <Send size={18} />
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Toggle Button */}
        <button
          onClick={toggleChat}
          className={`${
            isOpen ? "hidden" : "flex"
          } w-14 h-14 bg-blue-500 hover:bg-blue-600 text-white rounded-full shadow-lg items-center justify-center transition-all hover:scale-110`}
        >
          <MessageCircle size={24} />
        </button>
      </div>
    </>
  );
};
