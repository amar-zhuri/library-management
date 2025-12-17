import api from './api'
import type { AuthResponse, MessageResponse } from '../types'

interface RegisterPayload {
  name: string
  email: string
  password: string
}

interface LoginPayload {
  email: string
  password: string
}

export const authService = {
  register: async (data: RegisterPayload) => {
    const res = await api.post<AuthResponse>('/auth/register', data)
    return res.data
  },
  login: async (data: LoginPayload) => {
    const res = await api.post<AuthResponse>('/auth/login', data)
    return res.data
  },
  me: async () => {
    const res = await api.get<AuthResponse['user']>('/auth/me')
    return res.data
  },
  logout: async () => {
    const res = await api.post<MessageResponse>('/auth/logout')
    return res.data
  },
  verifyEmail: async (token: string) => {
    const res = await api.get<MessageResponse>('/auth/verify', { params: { token } })
    return res.data
  },
  resendVerification: async (email: string) => {
    const res = await api.post<MessageResponse>('/auth/resend-verification', { email })
    return res.data
  },
  forgotPassword: async (email: string) => {
    const res = await api.post<MessageResponse>('/auth/forgot-password', { email })
    return res.data
  },
  resetPassword: async (token: string, newPassword: string) => {
    const res = await api.post<MessageResponse>('/auth/reset-password', { token, newPassword })
    return res.data
  },
}
