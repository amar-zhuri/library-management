import api from './api';
import type { Book, BookRequest, PagedResponse } from '../types';

export interface GetBooksParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

export const bookService = {
  async getBooks(params: GetBooksParams = {}): Promise<PagedResponse<Book>> {
    const { page = 0, size = 10, sortBy = 'createdAt', sortDir = 'desc' } = params;
    const response = await api.get<PagedResponse<Book>>('/books', {
      params: { page, size, sortBy, sortDir },
    });
    return response.data;
  },

  async getBook(id: number): Promise<Book> {
    const response = await api.get<Book>(`/books/${id}`);
    return response.data;
  },

  async createBook(data: BookRequest): Promise<Book> {
    const response = await api.post<Book>('/books', data);
    return response.data;
  },

  async updateBook(id: number, data: BookRequest): Promise<Book> {
    const response = await api.put<Book>(`/books/${id}`, data);
    return response.data;
  },

  async deleteBook(id: number): Promise<void> {
    await api.delete(`/books/${id}`);
  },

  async updateStatus(id: number, status: string): Promise<Book> {
    const response = await api.patch<Book>(`/books/${id}/status`, null, {
      params: { status },
    });
    return response.data;
  },
};