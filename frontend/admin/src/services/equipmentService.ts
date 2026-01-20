import httpClient from "@/configurations/httpClient";
import { CONFIG } from "@/configurations/configuration";

export interface EquipmentCategory {
  id: string;
  name: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Equipment {
  id: string;
  name: string;
  categoryId: string;
  roomId: string;
  serialNumber?: string;
  status: string;
  purchaseDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEquipmentRequest {
  name: string;
  categoryId: string;
  roomId: string;
  serialNumber?: string;
  status: string;
  purchaseDate?: string;
}

export interface UpdateEquipmentRequest {
  name?: string;
  categoryId?: string;
  roomId?: string;
  serialNumber?: string;
  status?: string;
  purchaseDate?: string;
}

export interface CreateCategoryRequest {
  name: string;
  description?: string;
}

export interface UpdateCategoryRequest {
  name?: string;
  description?: string;
}

// Equipment Endpoints
export const getAllEquipment = async () => {
  return await httpClient.get(`${CONFIG.API}/equipments`);
};

export const getEquipmentById = async (equipmentId: string) => {
  return await httpClient.get(`${CONFIG.API}/equipments/${equipmentId}`);
};

export const getEquipmentByRoom = async (roomId: string) => {
  return await httpClient.get(`${CONFIG.API}/equipments/room/${roomId}`);
};

export const getEquipmentByCategory = async (categoryId: string) => {
  return await httpClient.get(`${CONFIG.API}/equipments/category/${categoryId}`);
};

export const getEquipmentByStatus = async (status: string) => {
  return await httpClient.get(`${CONFIG.API}/equipments/status/${status}`);
};

export const createEquipment = async (data: CreateEquipmentRequest) => {
  return await httpClient.post(`${CONFIG.API}/equipments`, data);
};

export const updateEquipment = async (
  equipmentId: string,
  data: UpdateEquipmentRequest
) => {
  return await httpClient.put(`${CONFIG.API}/equipments/${equipmentId}`, data);
};

export const deleteEquipment = async (equipmentId: string) => {
  return await httpClient.delete(`${CONFIG.API}/equipments/${equipmentId}`);
};

// Equipment Category Endpoints
export const getAllCategories = async () => {
  return await httpClient.get(`${CONFIG.API}/equipment-categories`);
};

export const getCategoryById = async (categoryId: string) => {
  return await httpClient.get(`${CONFIG.API}/equipment-categories/${categoryId}`);
};

export const createCategory = async (data: CreateCategoryRequest) => {
  return await httpClient.post(`${CONFIG.API}/equipment-categories`, data);
};

export const updateCategory = async (
  categoryId: string,
  data: UpdateCategoryRequest
) => {
  return await httpClient.put(`${CONFIG.API}/equipment-categories/${categoryId}`, data);
};

export const deleteCategory = async (categoryId: string) => {
  return await httpClient.delete(`${CONFIG.API}/equipment-categories/${categoryId}`);
};
