import api from './api'
import type { RecommendationResponse, RecommendedBook } from '../types'

export const recommendationService = {
  quickPicks: async () => {
    const res = await api.get<RecommendationResponse>('/recommendations')
    return res.data
  },
  aiPicks: async () => {
    const res = await api.get<RecommendationResponse>('/recommendations/ai')
    return res.data
  },
  byGenre: async (limit = 5) => {
    const res = await api.get<RecommendedBook[]>('/recommendations/by-genre', { params: { limit } })
    return res.data
  },
  byAuthor: async (limit = 5) => {
    const res = await api.get<RecommendedBook[]>('/recommendations/by-author', { params: { limit } })
    return res.data
  },
  discover: async (limit = 5) => {
    const res = await api.get<RecommendedBook[]>('/recommendations/discover', { params: { limit } })
    return res.data
  },
}
