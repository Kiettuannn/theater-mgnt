import axios, { AxiosError, InternalAxiosRequestConfig, AxiosResponse } from "axios";
import { CONFIG } from "./configuration";
import { ApiError, ApiResponse } from "@/lib/errors";

const httpClient = axios.create({
  baseURL: CONFIG.API,
  timeout: 30000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor
httpClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Add auth token from localStorage
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("customer_token");
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response interceptor
httpClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // Check if response has standard format
    if (response.data && typeof response.data.code !== 'undefined') {
      // If code !== 1000, treat as error
      if (response.data.code !== 1000) {
        const apiError = new ApiError(
          response.data.code,
          response.data.message || "Request failed",
          response.data
        );
        return Promise.reject(apiError);
      }
    }
    return response;
  },
  (error: AxiosError<ApiResponse>) => {
    // Handle HTTP errors (4xx, 5xx)
    if (error.response?.data) {
      const data = error.response.data;
      
      // If backend returns standard format even in error response
      if (typeof data.code !== 'undefined') {
        const apiError = new ApiError(
          data.code,
          data.message || "Request failed",
          data
        );
        return Promise.reject(apiError);
      }
    }

    // Handle network errors or other errors
    console.error("API Error:", error);
    return Promise.reject(error);
  }
);

export default httpClient;
