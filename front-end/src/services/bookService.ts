import api from './api'
import type { BookRequest, BookResponse, PagedResponse, QuickStatsResponse } from '../types'

export interface BookQueryParams {
  genre?: string
  status?: string
  search?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

export const bookService = {
  list: async (params?: BookQueryParams) => {
    const res = await api.get<PagedResponse<BookResponse>>('/books', { params })
    return res.data
  },
  get: async (id: number) => {
    const res = await api.get<BookResponse>(`/books/${id}`)
    return res.data
  },
  create: async (payload: BookRequest) => {
    const res = await api.post<BookResponse>('/books', payload)
    return res.data
  },
  update: async (id: number, payload: BookRequest) => {
    const res = await api.put<BookResponse>(`/books/${id}`, payload)
    return res.data
  },
  remove: async (id: number) => {
    await api.delete(`/books/${id}`)
  },
  stats: async () => {
    const res = await api.get<QuickStatsResponse>('/books/stats')
    return res.data
  },
  quickSearch: async (q: string, page = 0, size = 10) => {
    const res = await api.get<PagedResponse<BookResponse>>('/books/quick-search', {
      params: { q, page, size },
    })
    return res.data
  },
}
