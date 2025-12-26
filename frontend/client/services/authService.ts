import httpClient from "../configurations/httpClient";
import { API, OAuthConfig } from "../configurations/configuration";
import { setToken, setUserInfo, getToken } from "./localStorageService";
import { AlignHorizontalDistributeCenterIcon } from "lucide-react";

export interface LoginRequest {
  loginIdentifier: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  phoneNumber: string;
  firstName: string;
  lastName: string;
  dob: string;
}

export interface AuthResponse {
  result: {
    token: string;
    authenticated: boolean;
  };
}

export interface GoogleAuthResponse {
  result: {
    token: string;
    user: any;
  };
}

// Login with email and password
export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  try {
    const response = await httpClient.post<AuthResponse>(API.LOGIN, data);
    
    if (response.data.result?.token) {
      setToken(response.data.result.token);
    }
    return response.data;
  } catch (error) {
    console.error("Login failed:", error);
    throw error;
  }
};

// Register new customer
export const register = async (data: RegisterRequest): Promise<AuthResponse> => {
  try {
    const response = await httpClient.post<AuthResponse>(API.REGISTER, data);
    
    if (response.data.result?.token) {
      setToken(response.data.result.token);
    }
    
    return response.data;
  } catch (error) {
    console.error("Registration failed:", error);
    throw error;
  }
};

// Authenticate with Google
export const authenticateWithGoogle = async (code: string): Promise<GoogleAuthResponse> => {
  try {
    const response = await httpClient.post<GoogleAuthResponse>(
      `${API.GOOGLE_AUTH}?code=${code}`
    );

    if (response.data.result?.token) {
      setToken(response.data.result.token);
      setUserInfo(response.data.result.user);
    }

    return response.data;
  } catch (error) {
    console.error("Google authentication failed:", error);
    throw error;
  }
};

// Generate Google OAuth URL
export const getGoogleAuthUrl = (): string => {
  const { clientId, redirectUri, authUri } = OAuthConfig;
  
  return `${authUri}?redirect_uri=${encodeURIComponent(
    redirectUri
  )}&response_type=code&client_id=${clientId}&scope=openid%20email%20profile`;
};

// Create password for Google OAuth users
export const createPassword = async (password: string, confirmPassword: string): Promise<{ result: boolean }> => {
  try {
    const response = await httpClient.post<{ result: boolean }>(
      API.CREATE_PASSWORD,
      { password, confirmPassword },
      {
        headers: {
          Authorization: `Bearer ${getToken()}`,
        },
      }
    );
    
    return response.data;
  } catch (error) {
    console.error("Create password failed:", error);
    throw error;
  }
};
