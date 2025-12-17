import api from './api'
import type { BookResponse, BookSearchResponse, SearchFiltersResponse } from '../types'

export interface SearchParams {
  q?: string
  title?: string
  author?: string
  isbn?: string
  genre?: string
  status?: string
  minPrice?: number
  maxPrice?: number
  minYear?: number
  maxYear?: number
  minPages?: number
  maxPages?: number
  page?: number
  size?: number
  sortBy?: string
  sortDir?: string
}

export const searchService = {
  search: async (params: SearchParams) => {
    const res = await api.get<BookSearchResponse>('/books/search', { params })
    const data = res.data as BookSearchResponse & { content?: BookResponse[] }
    return {
      ...data,
      books: data.books ?? data.content ?? [],
      page: data.page ?? 0,
      size: data.size ?? params.size ?? 10,
      totalElements: data.totalElements ?? data.books?.length ?? data.content?.length ?? 0,
      totalPages: data.totalPages ?? 1,
      first: data.first ?? true,
      last: data.last ?? true,
    }
  },
  searchAll: async (params: SearchParams) => {
    const res = await api.get<BookSearchResponse>('/books/search/all', { params })
    const data = res.data as BookSearchResponse & { content?: BookResponse[] }
    return {
      ...data,
      books: data.books ?? data.content ?? [],
      page: data.page ?? 0,
      size: data.size ?? params.size ?? 10,
      totalElements: data.totalElements ?? data.books?.length ?? data.content?.length ?? 0,
      totalPages: data.totalPages ?? 1,
      first: data.first ?? true,
      last: data.last ?? true,
    }
  },
  getSuggestions: async (q: string) => {
    const res = await api.get<{ titles: string[]; authors: string[] }>('/books/search/suggestions', { params: { q } })
    return res.data
  },
  getFilters: async () => {
    const res = await api.get<SearchFiltersResponse>('/books/search/filters')
    return res.data
  },
}
