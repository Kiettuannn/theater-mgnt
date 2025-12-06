import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';

interface AuthState {
  // State
  token: string | null;
  userId: string | null;
  permissions: string[]; // List of permission codes from JWT scope
  isAuthenticated: boolean;
  
  // Actions
  setAuth: (token: string, userId: string, permissions: string[]) => void;
  clearAuth: () => void;
  hasPermission: (permission: string) => boolean;
  hasAnyPermission: (permissions: string[]) => boolean;
  hasAllPermissions: (permissions: string[]) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      token: null,
      userId: null,
      permissions: [],
      isAuthenticated: false,
      
      // Actions
      setAuth: (token, userId, permissions) => {
        set({
          token,
          userId,
          permissions,
          isAuthenticated: true,
        });
      },
      
      clearAuth: () => {
        set({
          token: null,
          userId: null,
          permissions: [],
          isAuthenticated: false,
        });
      },

      hasPermission: (permission: string) => {
        return get().permissions.includes(permission);
      },

      hasAnyPermission: (permissionsToCheck: string[]) => {
        return permissionsToCheck.some(permission => get().permissions.includes(permission));
      },

      hasAllPermissions: (permissionsToCheck: string[]) => {
        return permissionsToCheck.every(permission => get().permissions.includes(permission));
      },
    }),
    {
      name: 'auth-storage',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        token: state.token,
        userId: state.userId,
        permissions: state.permissions,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

// Selectors for optimized re-renders
export const selectIsAuthenticated = (state: AuthState) => state.isAuthenticated;
export const selectToken = (state: AuthState) => state.token;
export const selectUserId = (state: AuthState) => state.userId;
export const selectPermissions = (state: AuthState) => state.permissions;
