import axios from 'axios'
import type { Genre, ReadingStatus } from '../types'

export const formatCurrency = (value?: number) => {
  if (value === undefined || value === null) return '—'
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value)
}

export const formatDate = (value?: string) => {
  if (!value) return '—'
  return new Intl.DateTimeFormat('en', { month: 'short', day: 'numeric', year: 'numeric' }).format(new Date(value))
}

export const toReadable = (value?: string) => {
  if (!value) return ''
  return value
    .toLowerCase()
    .split('_')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ')
}

export const formatGenre = (genre: Genre) => toReadable(genre)
export const formatStatus = (status: ReadingStatus) => toReadable(status)

export const getErrorMessage = (error: unknown) => {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message || error.response?.data?.error || error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  return 'Something went wrong. Please try again.'
}
