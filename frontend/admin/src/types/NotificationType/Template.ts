export interface NotificationTemplate {
  id: string;
  templateCode: string;
  titleTemplate: string;
  contentTemplate: string;
  priority: string;
  createdAt: string;
  updatedAt: string;
}

export interface NotificationTemplateRequest {
  templateCode: string;
  titleTemplate: string;
  contentTemplate: string;
  priority: string;
}
