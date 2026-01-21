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
  gender: string;
  address?: string;
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

export interface IntrospectResponse {
  result: {
    valid: boolean;
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
    console.log("Registration Response:", response);
    
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
    console.log("Google Auth Response:", response);
    
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

// Introspect token validity
export const introspectToken = async (token: string): Promise<boolean> => {
  try {
    const response = await httpClient.post<IntrospectResponse>("/auth/introspect", { token });
    return !!response.data.result?.valid;
  } catch (error) {
    const status = (error as any)?.response?.status;
    const code = (error as any)?.code ?? (error as any)?.response?.data?.code;
    if (status === 401 || code === 1006) {
      return false;
    }
    console.error("Token introspection failed:", error);
    return false;
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
