import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { adminService } from '../../services/adminService'
import type { InsightResponse, StatsResponse } from '../../types'
import { Card } from '../../components/common/Card'
import { Badge } from '../../components/common/Badge'
import { toReadable } from '../../utils/helpers'
import { insightsService } from '../../services/insightsService'

export const AdminDashboardPage = () => {
  const [stats, setStats] = useState<StatsResponse | null>(null)
  const [insights, setInsights] = useState<InsightResponse | null>(null)
  const [aiInsights, setAiInsights] = useState<InsightResponse | null>(null)

  useEffect(() => {
    const load = async () => {
      try {
        const [res, adminInsights, adminAiInsights] = await Promise.all([
          adminService.stats(),
          insightsService.adminInsights(),
          insightsService.adminInsightsAI(),
        ])
        setStats(res)
        setInsights(adminInsights)
        setAiInsights(adminAiInsights)
      } catch {
        toast.error('Unable to load admin stats')
      }
    }
    load()
  }, [])

  return (
    <div className="space-y-4">
      <div>
        <p className="text-sm text-muted-600">System overview</p>
        <h1 className="text-2xl font-bold text-muted-900">Admin Dashboard</h1>
      </div>

      {stats && (
        <>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card className="bg-gradient-to-br from-white to-indigo-50">
              <p className="text-sm text-muted-600">Total Users</p>
              <p className="text-3xl font-bold">{stats.totalUsers}</p>
            </Card>
            <Card className="bg-gradient-to-br from-white to-cyan-50">
              <p className="text-sm text-muted-600">Total Books</p>
              <p className="text-3xl font-bold">{stats.totalBooks}</p>
            </Card>
            <Card>
              <p className="text-sm text-muted-600">Top Reader</p>
              <p className="text-lg font-semibold">{stats.topReaders?.[0]?.name ?? stats.topReaders?.[0]?.userName ?? '—'}</p>
            </Card>
            <Card>
              <p className="text-sm text-muted-600">Popular Author</p>
              <p className="text-lg font-semibold">{stats.topAuthors?.[0]?.author ?? '—'}</p>
            </Card>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <Card className="space-y-2">
              <p className="text-sm font-semibold text-muted-800">Books by Genre</p>
              <div className="flex flex-wrap gap-2">
                {Object.entries(stats.booksByGenre || {}).map(([genre, count]) => (
                  <Badge key={genre} tone="primary">
                    {toReadable(genre)} • {count}
                  </Badge>
                ))}
              </div>
            </Card>
            <Card className="space-y-2">
              <p className="text-sm font-semibold text-muted-800">Books by Status</p>
              <div className="flex flex-wrap gap-2">
                {Object.entries(stats.booksByStatus || {}).map(([status, count]) => (
                  <Badge key={status}>{toReadable(status)} • {count as number}</Badge>
                ))}
              </div>
            </Card>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <Card className="space-y-2">
              <p className="text-sm font-semibold text-muted-800">Top Readers</p>
              <div className="space-y-2 text-sm">
                {stats.topReaders?.map((reader) => (
                  <div key={reader.userId} className="flex items-center justify-between rounded-lg border border-muted-100 px-3 py-2">
                    <span>{reader.name ?? reader.userName ?? 'Unknown'}</span>
                    <Badge tone="neutral">{reader.bookCount} books</Badge>
                  </div>
                ))}
              </div>
            </Card>
            <Card className="space-y-2">
              <p className="text-sm font-semibold text-muted-800">Popular Books</p>
              <div className="space-y-2 text-sm">
                {stats.popularBooks?.map((book) => (
                  <div key={book.id ?? `${book.title}-${book.author}`} className="flex items-center justify-between rounded-lg border border-muted-100 px-3 py-2">
                    <span>{book.title}</span>
                    <Badge tone="primary">{(book.readCount ?? book.count) ?? 0} reads</Badge>
                  </div>
                ))}
              </div>
            </Card>
          </div>

          {insights && (
            <Card className="space-y-2">
              <div className="flex items-center justify-between">
                <p className="text-sm font-semibold text-muted-800">System Insights (AI)</p>
                <Badge tone="primary">{insights.generatedBy}</Badge>
              </div>
              <div className="space-y-2 text-sm text-muted-700">
                {insights.insights.map((item, idx) => (
                  <div key={idx} className="rounded-lg bg-muted-50 px-3 py-2">
                    {item}
                  </div>
                ))}
                <p className="text-xs text-muted-500">Generated at {new Date(insights.generatedAtMs).toLocaleString()}</p>
              </div>
            </Card>
          )}

          {aiInsights && (
            <Card className="space-y-2">
              <div className="flex items-center justify-between">
                <p className="text-sm font-semibold text-muted-800">System Insights (LLM)</p>
                <Badge tone="primary">{aiInsights.generatedBy}</Badge>
              </div>
              <div className="space-y-2 text-sm text-muted-700">
                {aiInsights.insights.map((item, idx) => (
                  <div key={idx} className="rounded-lg bg-primary-50 px-3 py-2 text-primary-900">
                    {item}
                  </div>
                ))}
                <p className="text-xs text-muted-500">Generated at {new Date(aiInsights.generatedAtMs).toLocaleString()}</p>
              </div>
            </Card>
          )}
        </>
      )}
    </div>
  )
}
