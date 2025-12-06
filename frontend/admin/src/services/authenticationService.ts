import {
  getToken,
  removeToken,
  setToken
} from "./localStorageService";
import httpClient from "../configurations/httpClient";
import {
  API
} from "../configurations/configuration";
import { useAuthStore } from "@/stores";
import { extractPermissionsFromToken, extractUserIdFromToken, isTokenExpired } from "@/utils/jwtUtils";

export const login = async (loginIdentifier: string, password: string) => {
  const response = await httpClient.post(API.LOGIN, {
    loginIdentifier: loginIdentifier,
    password: password,
  });

  const token = response.data?.result?.token;

  if (token) {
    // Check if token is expired (shouldn't happen right after login, but safe to check)
    if (isTokenExpired(token)) {
      throw new Error('Received expired token from server');
    }

    // Extract userId and permissions from token
    const userId = extractUserIdFromToken(token);
    const permissions = extractPermissionsFromToken(token);

    if (!userId) {
      throw new Error('Invalid token: missing user ID');
    }

    // Save token to localStorage
    setToken(token);

    // Update auth store with minimal required data
    useAuthStore.getState().setAuth(token, userId, permissions);
  }

  return response;
};

export const logOut = () => {
  removeToken();
  // Clear Zustand store
  useAuthStore.getState().clearAuth();
};

export const isAuthenticated = () => {
  return getToken();
};