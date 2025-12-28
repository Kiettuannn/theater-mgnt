import httpClient from "@/configurations/httpClient";
import type { CustomerProfile, CustomerRequest, CustomerUpdateRequest } from "@/types/CustomerType/CustomerProfile";
import { handleApiResponse } from "@/utils/apiResponse";
import type { ApiResponse } from "@/utils/apiResponse";

const BASE_URL = "/customers";

export const getAllCustomers = async (): Promise<CustomerProfile[]> => {
  return handleApiResponse<CustomerProfile[]>(
    httpClient.get<ApiResponse<CustomerProfile[]>>(BASE_URL)
  );
};

export const getCustomerById = async (customerId: string): Promise<CustomerProfile> => {
  return handleApiResponse<CustomerProfile>(
    httpClient.get<ApiResponse<CustomerProfile>>(`${BASE_URL}/${customerId}`)
  );
};

export const createCustomer = async (
  request: CustomerRequest
): Promise<CustomerProfile> => {
  return handleApiResponse<CustomerProfile>(
    httpClient.post<ApiResponse<CustomerProfile>>(BASE_URL, request)
  );
};

export const updateCustomer = async (
  customerId: string,
  request: CustomerUpdateRequest
): Promise<CustomerProfile> => {
  return handleApiResponse<CustomerProfile>(
    httpClient.put<ApiResponse<CustomerProfile>>(`${BASE_URL}/${customerId}`, request)
  );
};

export const deleteCustomer = async (customerId: string): Promise<void> => {
  await httpClient.delete(`${BASE_URL}/${customerId}`);
};

export const searchCustomers = async (keyword: string): Promise<CustomerProfile[]> => {
  return handleApiResponse<CustomerProfile[]>(
    httpClient.get<ApiResponse<CustomerProfile[]>>(`${BASE_URL}/search`, {
      params: { keyword },
    })
  );
};
