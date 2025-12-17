import api from './api'
import type {
  BookRequest,
  BookResponse,
  NewsletterRequest,
  NewsletterResponse,
  NewsletterStats,
  PagedResponse,
  StatsResponse,
  UserDetailResponse,
  UserResponse,
} from '../types'

export interface AdminQueryParams {
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

export const adminService = {
  stats: async () => {
    const res = await api.get<StatsResponse>('/admin/stats')
    return res.data
  },
  users: async (params?: AdminQueryParams) => {
    const res = await api.get<PagedResponse<UserResponse>>('/admin/users', { params })
    return res.data
  },
  userDetail: async (id: number) => {
    const res = await api.get<UserDetailResponse>(`/admin/users/${id}`)
    return res.data
  },
  deleteUser: async (id: number) => {
    await api.delete(`/admin/users/${id}`)
  },
  books: async (params?: AdminQueryParams) => {
    const res = await api.get<PagedResponse<BookResponse>>('/admin/books', { params })
    return res.data
  },
  deleteBook: async (id: number) => {
    await api.delete(`/admin/books/${id}`)
  },
  createBook: async (payload: BookRequest) => {
    const res = await api.post<BookResponse>('/admin/books', payload)
    return res.data
  },
  createNewsletter: async (payload: NewsletterRequest) => {
    const res = await api.post<NewsletterResponse>('/admin/newsletter', payload)
    return res.data
  },
  listNewsletters: async (params?: AdminQueryParams) => {
    const res = await api.get<PagedResponse<NewsletterResponse>>('/admin/newsletter', { params })
    return res.data
  },
  getNewsletter: async (id: number) => {
    const res = await api.get<NewsletterResponse>(`/admin/newsletter/${id}`)
    return res.data
  },
  updateNewsletter: async (id: number, payload: NewsletterRequest) => {
    const res = await api.put<NewsletterResponse>(`/admin/newsletter/${id}`, payload)
    return res.data
  },
  deleteNewsletter: async (id: number) => {
    await api.delete(`/admin/newsletter/${id}`)
  },
  sendNewsletter: async (id: number) => {
    const res = await api.post<NewsletterResponse>(`/admin/newsletter/${id}/send`)
    return res.data
  },
  newsletterStats: async () => {
    const res = await api.get<NewsletterStats>('/admin/newsletter/stats')
    return res.data
  },
}
