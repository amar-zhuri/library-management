import api from './api'
import type { InsightResponse, UserInsightsResponse } from '../types'

export const insightsService = {
  quickStats: async () => {
    const res = await api.get<Record<string, unknown>>('/ai/quick-stats')
    return res.data
  },
  myInsights: async () => {
    const res = await api.get<UserInsightsResponse>('/ai/my-insights')
    return res.data
  },
  myInsightsAI: async () => {
    const res = await api.get<UserInsightsResponse>('/ai/my-insights/ai')
    return res.data
  },
  adminInsights: async () => {
    const res = await api.get<InsightResponse>('/ai/insights')
    return res.data
  },
  adminInsightsAI: async () => {
    const res = await api.get<InsightResponse>('/ai/insights/ai')
    return res.data
  },
}
