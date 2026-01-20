export type NotificationStatus = "PENDING" | "SENT" | "FAILED";
export type NotificationPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT";
export type NotificationCategory = "BOOKING" | "PROMOTION" | "SYSTEM" | "SECURITY" | "REMINDER";
export type RecipientType = "CUSTOMER" | "STAFF" | "ADMIN";

export interface NotificationLog {
  id: string;
  notificationId: string;
  channelName: string;
  status: string;
  providerResponse: Record<string, any> | null;
  sentAt: string;
}

export interface Notification {
  id: string;
  templateCode: string;
  recipientId: string;
  recipientType: RecipientType;
  category: NotificationCategory;
  priority: NotificationPriority;
  status: NotificationStatus;
  title: string;
  content: string;
  metadata: Record<string, any>;
  isRead: boolean;
  readAt: string | null;
  createdAt: string;
  updatedAt: string;
  logs?: NotificationLog[];
}

export interface NotificationRequest {
  templateCode: string;
  recipientId: string;
  recipientType: RecipientType;
  category: NotificationCategory;
  channels: string[];
  metadata: Record<string, any>;
  priority?: NotificationPriority;
}

// Paginated Response
export interface PaginatedNotificationResponse {
  content: Notification[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
