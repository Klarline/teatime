import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '@/api/types';
import { authApi } from '@/api/auth.api';

interface AuthState {
  // State
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  
  // Actions
  login: (phone: string, code: string) => Promise<void>;
  logout: () => Promise<void>;
  fetchUser: () => Promise<void>;
  setUser: (user: User) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      // Initial state
      user: null,
      token: null,
      isAuthenticated: false,

      // Login action
      login: async (phone: string, code: string) => {
        try {
          const result = await authApi.login(phone, code);
          if (result.success) {
            set({ 
              token: result.data, 
              isAuthenticated: true 
            });
            
            // Fetch user info after login
            const userResult = await authApi.getCurrentUser();
            if (userResult.success) {
              set({ user: userResult.data });
            }
          }
        } catch (error) {
          console.error('Login failed:', error);
          throw error;
        }
      },

      // Logout action
      logout: async () => {
        try {
          await authApi.logout();
        } catch (error) {
          console.error('Logout error:', error);
        } finally {
          // Clear state regardless of API success
          set({ 
            user: null, 
            token: null, 
            isAuthenticated: false 
          });
        }
      },

      // Fetch current user
      fetchUser: async () => {
        try {
          const result = await authApi.getCurrentUser();
          if (result.success) {
            set({ user: result.data });
          }
        } catch (error) {
          console.error('Fetch user failed:', error);
          // If token is invalid, clear auth state
          set({ 
            user: null, 
            token: null, 
            isAuthenticated: false 
          });
        }
      },

      // Set user manually
      setUser: (user: User) => {
        set({ user });
      },
    }),
    {
      name: 'auth-storage', // localStorage key
      partialize: (state) => ({
        token: state.token,
        isAuthenticated: state.isAuthenticated,
        // Don't persist user - fetch fresh on reload
      }),
    }
  )
);