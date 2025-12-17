import api from './api'
import type { AIQueryResponse } from '../types'

export const aiService = {
  ask: async (question: string, useLLM = false) => {
    const res = await api.post<AIQueryResponse>('/ai/query', { question, useLLM })
    return res.data
  },
  suggestions: async () => {
    const res = await api.get<{ suggestions: string[]; categories: string[] }>('/ai/suggestions')
    return res.data
  },
}
