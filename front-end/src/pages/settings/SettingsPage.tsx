import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { notificationService } from '../../services/notificationService'
import type { NotificationPreferencesResponse } from '../../types'
import { Card } from '../../components/common/Card'
import { Button } from '../../components/common/Button'
import { useAuth } from '../../hooks/useAuth'

export const SettingsPage = () => {
  const { user } = useAuth()
  const [prefs, setPrefs] = useState<NotificationPreferencesResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    const load = async () => {
      try {
        const res = await notificationService.getPreferences()
        setPrefs(res)
      } catch {
        toast.error('Unable to load preferences')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  const toggle = (key: keyof NotificationPreferencesResponse) => {
    setPrefs((prev) => (prev ? { ...prev, [key]: !prev[key] } : prev))
  }

  const save = async () => {
    if (!prefs) return
    try {
      setSaving(true)
      const res = await notificationService.updatePreferences(prefs)
      setPrefs(res)
      toast.success('Preferences updated')
    } catch {
      toast.error('Unable to save preferences')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-4">
      <div>
        <p className="text-sm text-muted-600">Control your experience</p>
        <h1 className="text-2xl font-bold text-muted-900">Settings</h1>
      </div>

      <Card className="flex items-center justify-between">
        <div>
          <p className="text-xs uppercase tracking-wide text-primary-700">{user?.role}</p>
          <p className="text-lg font-semibold text-muted-900">{user?.name}</p>
          <p className="text-sm text-muted-700">{user?.email}</p>
        </div>
        <div className="rounded-full bg-primary-100 px-4 py-2 text-sm font-semibold text-primary-700">Verified</div>
      </Card>

      <Card className="space-y-3">
        <h2 className="text-lg font-semibold text-muted-900">Notifications</h2>
        {loading && <p className="text-sm text-muted-600">Loading...</p>}
        {prefs && (
          <div className="space-y-2 text-sm">
            {(['newsletterEnabled', 'newBooksEnabled', 'weeklyDigestEnabled', 'readingRemindersEnabled', 'achievementNotificationsEnabled'] as const).map(
              (key) => (
                <label key={key} className="flex items-center justify-between rounded-lg border border-muted-100 bg-white px-3 py-2">
                  <span className="font-semibold text-muted-800">{key.replace(/([A-Z])/g, ' $1')}</span>
                  <input type="checkbox" checked={prefs[key]} onChange={() => toggle(key)} />
                </label>
              )
            )}
          </div>
        )}
        <Button onClick={save} loading={saving}>
          Save changes
        </Button>
      </Card>
    </div>
  )
}
