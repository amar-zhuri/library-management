import api from './api'
import type { BookResponse, PagedResponse } from '../types'

export interface LibraryQueryParams {
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

export const libraryService = {
  list: async (params?: LibraryQueryParams) => {
    const res = await api.get<PagedResponse<BookResponse>>('/library/books', { params })
    return res.data
  },
  claim: async (id: number) => {
    const res = await api.post<BookResponse>(`/library/books/${id}/claim`)
    return res.data
  },
}
