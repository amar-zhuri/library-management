import api from './api';
import type { AuthResponse, LoginRequest, RegisterRequest, MessageResponse, User } from '../types';

export const authService = {
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', data);
    return response.data;
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/register', data);
    return response.data;
    // await api.post<AuthResponse>('/auth/register', data);
    // return { message: 'Registration successful', success: true };
  },

  async logout(): Promise<MessageResponse> {
    const response = await api.post<MessageResponse>('/auth/logout');
    return response.data;
  },

  async getCurrentUser(): Promise<User> {
    const response = await api.get<User>('/auth/me');
    return response.data;
  },

  async verifyEmail(token: string): Promise<MessageResponse> {
    const response = await api.get<MessageResponse>('/auth/verify', { params: { token } });
    return response.data;
  },

  async resendVerification(email: string): Promise<MessageResponse> {
    const response = await api.post<MessageResponse>('/auth/resend-verification', { email });
    return response.data;
  },

  async forgotPassword(email: string): Promise<MessageResponse> {
    const response = await api.post<MessageResponse>('/auth/forgot-password', { email });
    return response.data;
  },

  async resetPassword(token: string, newPassword: string): Promise<MessageResponse> {
    const response = await api.post<MessageResponse>('/auth/reset-password', {
      token,
      newPassword,
    });
    return response.data;
  },
};
