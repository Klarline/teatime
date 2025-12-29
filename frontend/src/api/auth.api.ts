import api from './index';
import type { ApiResponse, User } from '../types';

export const authApi = {
  // Send verification code
  sendCode: async (phone: string): Promise<ApiResponse<null>> => {
    const params = new URLSearchParams();
    params.append('phone', phone);
    
    const response = await api.post('/user/code', params, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });
    return response.data;
  },

  // Login with phone and code
  login: async (phone: string, code: string): Promise<ApiResponse<string>> => {
    const response = await api.post('/user/login', { phone, code });
    return response.data;
  },

  // Logout
  logout: async (): Promise<ApiResponse<null>> => {
    const response = await api.post('/user/logout');
    return response.data;
  },

  // Get current user
  getCurrentUser: async (): Promise<ApiResponse<User>> => {
    const response = await api.get('/user/me');
    return response.data;
  },

  // Get user by ID
  getUserById: async (id: number): Promise<ApiResponse<User>> => {
    const response = await api.get(`/user/${id}`);
    return response.data;
  },

  // Get user info (public profile)
  getUserInfo: async (id: number): Promise<ApiResponse<User>> => {
    const response = await api.get(`/user/info/${id}`);
    return response.data;
  },

  // Daily check-in
  checkIn: async (): Promise<ApiResponse<number>> => {
    const response = await api.post('/user/checkin');
    return response.data;
  },

  // Get check-in count
  getCheckInCount: async (): Promise<ApiResponse<number>> => {
    const response = await api.get('/user/checkin/count');
    return response.data;
  },
};