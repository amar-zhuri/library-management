import api from './api'
import type { MessageResponse, NotificationPreferencesResponse } from '../types'

export interface NotificationPreferencesPayload {
  newsletterEnabled?: boolean
  newBooksEnabled?: boolean
  weeklyDigestEnabled?: boolean
  readingRemindersEnabled?: boolean
  achievementNotificationsEnabled?: boolean
}

export const notificationService = {
  getPreferences: async () => {
    const res = await api.get<NotificationPreferencesResponse>('/notifications/preferences')
    return res.data
  },
  updatePreferences: async (payload: NotificationPreferencesPayload) => {
    const res = await api.put<NotificationPreferencesResponse>('/notifications/preferences', payload)
    return res.data
  },
  unsubscribeAll: async (token: string) => {
    const res = await api.get<MessageResponse>('/notifications/unsubscribe', { params: { token } })
    return res.data
  },
  unsubscribeNewsletter: async (token: string) => {
    const res = await api.get<MessageResponse>('/notifications/unsubscribe/newsletter', { params: { token } })
    return res.data
  },
}
