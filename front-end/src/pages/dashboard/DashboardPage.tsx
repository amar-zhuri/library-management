import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { Card } from '../../components/common/Card'
import { Button } from '../../components/common/Button'
import { recommendationService } from '../../services/recommendationService'
import type { RecommendationResponse, RecommendedBook, UserInsightsResponse } from '../../types'
import { bookService } from '../../services/bookService'
import { getErrorMessage, toReadable } from '../../utils/helpers'
import { Badge } from '../../components/common/Badge'
import { insightsService } from '../../services/insightsService'
import { aiService } from '../../services/aiService'
import { useAuth } from '../../hooks/useAuth'
import { libraryService } from '../../services/libraryService'
import { Spinner } from '../../components/common/Spinner'

const StatCard = ({ label, value }: { label: string; value: number | string }) => (
  <Card className="flex flex-col gap-1 bg-gradient-to-br from-white to-indigo-50">
    <span className="text-sm font-semibold uppercase tracking-wide text-primary-600">{label}</span>
    <span className="text-3xl font-bold text-muted-900">{value}</span>
  </Card>
)

const RecommendationList = ({ title, items }: { title: string; items?: RecommendedBook[] | undefined }) => {
  if (!items || items.length === 0) {
    return (
      <Card>
        <p className="text-sm font-semibold text-muted-700">{title}</p>
        <p className="text-sm text-muted-500">No recommendations yet.</p>
      </Card>
    )
  }
  return (
    <Card className="space-y-3">
      <p className="text-sm font-semibold text-muted-700">{title}</p>
      <div className="space-y-3">
        {items.map((book) => (
          <div key={book.id} className="flex items-start justify-between gap-3 rounded-xl border border-muted-100 bg-white px-3 py-2">
            <div>
              <p className="text-sm font-semibold text-muted-900">{book.title}</p>
              <p className="text-xs text-muted-600">by {book.author}</p>
            </div>
            <Badge tone="primary">{toReadable(book.genre)}</Badge>
          </div>
        ))}
      </div>
    </Card>
  )
}

export const DashboardPage = () => {
  const [stats, setStats] = useState<{ total: number; toRead: number; reading: number; completed: number } | null>(null)
  const [quickPicks, setQuickPicks] = useState<RecommendationResponse | null>(null)
  const [aiPicks, setAiPicks] = useState<RecommendationResponse | null>(null)
  const [byGenre, setByGenre] = useState<RecommendedBook[] | null>(null)
  const [byAuthor, setByAuthor] = useState<RecommendedBook[] | null>(null)
  const [discover, setDiscover] = useState<RecommendedBook[] | null>(null)
  const [insights, setInsights] = useState<UserInsightsResponse | null>(null)
  const [aiInsights, setAiInsights] = useState<UserInsightsResponse | null>(null)
  const [quickStats, setQuickStats] = useState<Record<string, unknown> | null>(null)
  const [suggestedQuestions, setSuggestedQuestions] = useState<string[]>([])
  const [question, setQuestion] = useState('What are my top genres?')
  const [aiAnswer, setAiAnswer] = useState<string>('')
  const [useLLM, setUseLLM] = useState(false)
  const [loading, setLoading] = useState(true)
  const [asking, setAsking] = useState(false)
  const { user } = useAuth()
  const predefinedQuestions = [
    'Show total books by status and genre',
    'What did I finish most recently?',
    'Give me recommendations based on my favorite genre',
    'Summarize my reading progress this month',
  ]
  const aiOnlyQuestions = [
    'What patterns do you see in my reading habits?',
    'Explain trends across my genres and statuses.',
    'Give me a short report on my library as if I were a client.',
  ]

  const formatStatValue = (value: unknown): string => {
    if (value === null || value === undefined) return '—'
    if (Array.isArray(value)) {
      if (value.length === 0) return '—'
      const first = value[0]
      if (typeof first === 'string' || typeof first === 'number' || typeof first === 'boolean') {
        return (value as (string | number | boolean)[]).join(', ')
      }
      if (typeof first === 'object' && first !== null) {
        return (value as Record<string, unknown>[]).map((item) => formatStatValue(item)).join(' | ')
      }
    }
    if (typeof value === 'object') {
      const entries = Object.entries(value as Record<string, unknown>)
      if (entries.length === 0) return '—'
      return entries.map(([key, val]) => `${toReadable(key)}: ${formatStatValue(val)}`).join(', ')
    }
    return String(value)
  }

  useEffect(() => {
    const load = async () => {
      try {
        const [statsRes, quickRes, aiRes, insightRes, aiInsightRes, genreRes, authorRes, discoverRes, quickStatsRes, suggestionRes, sharedRes] = await Promise.all([
          bookService.stats(),
          recommendationService.quickPicks(),
          recommendationService.aiPicks(),
          insightsService.myInsights(),
          insightsService.myInsightsAI(),
          recommendationService.byGenre(),
          recommendationService.byAuthor(),
          recommendationService.discover(),
          insightsService.quickStats(),
          aiService.suggestions(),
          libraryService.list({ size: 8 }),
        ])
        const sharedPicks: RecommendedBook[] = (sharedRes.content ?? []).map((b) => ({
          id: b.id,
          title: b.title,
          author: b.author,
          genre: b.genre,
          pageCount: b.pageCount,
          publicationYear: b.publicationYear,
          reason: 'From shared library',
        }))

        setStats(statsRes)
        setQuickPicks({
          ...quickRes,
          byGenre: quickRes.byGenre?.length ? quickRes.byGenre : sharedPicks,
        })
        setAiPicks(aiRes)
        setInsights(insightRes)
        setAiInsights(aiInsightRes)
        setByGenre(genreRes.length ? genreRes : sharedPicks)
        setByAuthor(authorRes.length ? authorRes : sharedPicks)
        setDiscover(discoverRes.length ? discoverRes : sharedPicks)
        setQuickStats(quickStatsRes)
        setSuggestedQuestions(suggestionRes.suggestions ?? [])
      } catch (error) {
        toast.error(getErrorMessage(error))
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  const handleAsk = async () => {
    if (!question.trim()) return
    try {
      setAsking(true)
      const res = await aiService.ask(question, useLLM)
      setAiAnswer(res.answer)
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setAsking(false)
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-[70vh] items-center justify-center">
        <Card className="flex w-full max-w-2xl flex-col items-center gap-4 bg-gradient-to-br from-primary-50 to-indigo-50 py-10 text-center shadow-lg">
          <Spinner className="h-12 w-12 border-3" />
          <div>
            <p className="text-lg font-semibold text-primary-800">Fetching your dashboard</p>
            <p className="text-sm text-muted-600">Pulling AI insights, stats, and recommendations…</p>
          </div>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-sm text-muted-600">Welcome back,</p>
          <h1 className="text-2xl font-bold text-muted-900">{user?.name}</h1>
        </div>
        <Button variant="secondary" size="sm" onClick={() => window.location.reload()}>
          Refresh
        </Button>
      </div>

      {stats && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard label="Total books" value={stats.total} />
          <StatCard label="To read" value={stats.toRead} />
          <StatCard label="Reading" value={stats.reading} />
          <StatCard label="Completed" value={stats.completed} />
        </div>
      )}

      <div className="grid gap-4 lg:grid-cols-2">
        <RecommendationList title="Quick Picks" items={quickPicks?.byGenre} />
        <RecommendationList title="AI Picks" items={aiPicks?.fromSimilarUsers} />
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <RecommendationList title="By Genre" items={byGenre ?? undefined} />
        <RecommendationList title="By Author" items={byAuthor ?? undefined} />
        <RecommendationList title="Discover" items={discover ?? undefined} />
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card className="space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-muted-900">Insights</h3>
            <Badge tone="primary">{insights?.generatedBy ?? 'RULE_BASED'}</Badge>
          </div>
          <ul className="space-y-2 text-sm text-muted-700">
            {insights?.insights?.map((item, idx) => (
              <li key={idx} className="rounded-lg bg-muted-50 px-3 py-2">
                {item}
              </li>
            ))}
          </ul>
        </Card>
        <Card className="space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-muted-900">AI Insights</h3>
            <Badge tone="primary">{aiInsights?.generatedBy ?? 'AI'}</Badge>
          </div>
          <ul className="space-y-2 text-sm text-muted-700">
            {aiInsights?.insights?.map((item, idx) => (
              <li key={idx} className="rounded-lg bg-primary-50 px-3 py-2 text-primary-900">
                {item}
              </li>
            ))}
          </ul>
        </Card>
        {quickStats && (
          <Card className="space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-muted-900">AI Quick Stats</h3>
              <Badge tone="neutral">Auto</Badge>
            </div>
            <div className="space-y-2 text-sm text-muted-700">
              {Object.entries(quickStats).map(([key, value]) => (
                <div key={key} className="rounded-lg bg-muted-50 px-3 py-2">
                  <span className="font-semibold text-muted-800">{toReadable(key)}:</span> {formatStatValue(value)}
                </div>
              ))}
            </div>
          </Card>
        )}
      </div>

      <Card className="space-y-4">
        <div className="flex items-center justify-between gap-3">
          <div>
            <h3 className="text-lg font-semibold text-muted-900">AI Query Agent</h3>
            <p className="text-sm text-muted-600">Ask natural questions about your library.</p>
          </div>
          <label className="flex items-center gap-2 text-sm text-muted-700">
            <input type="checkbox" checked={useLLM} onChange={(e) => setUseLLM(e.target.checked)} /> Use AI mode
          </label>
        </div>
        <div className="flex flex-col gap-3 md:flex-row">
          <input
            className="flex-1 rounded-xl border border-muted-200 bg-white px-4 py-3 text-sm focus:border-primary-400 focus:outline-none focus:ring-2 focus:ring-primary-100"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder='e.g. "Show my reading statistics"'
          />
          <Button onClick={handleAsk} loading={asking}>
            Ask
          </Button>
        </div>
        <div className="flex flex-wrap gap-2 text-xs">
          {predefinedQuestions.map((q) => (
            <button
              key={q}
              onClick={() => setQuestion(q)}
              className="rounded-full bg-muted-100 px-3 py-1 font-semibold text-muted-700 hover:bg-primary-50 hover:text-primary-700"
            >
              {q}
            </button>
          ))}
        </div>
        <div className="space-y-2 rounded-xl border border-primary-100 bg-primary-50 px-3 py-2 text-xs text-primary-900">
          <div className="flex items-center justify-between">
            <span className="font-semibold uppercase tracking-wide">AI-preferred questions</span>
            <span className="rounded-full bg-white/60 px-2 py-1 text-[10px] font-bold text-primary-800">Enable AI mode</span>
          </div>
          <div className="flex flex-wrap gap-2">
            {aiOnlyQuestions.map((q) => (
              <button
                key={q}
                onClick={() => setQuestion(q)}
                className="rounded-full bg-white px-3 py-1 font-semibold text-primary-800 hover:bg-primary-100"
              >
                {q}
              </button>
            ))}
          </div>
        </div>
        {suggestedQuestions.length > 0 && (
          <div className="flex flex-wrap gap-2 text-xs">
            {suggestedQuestions.slice(0, 6).map((s) => (
              <button
                key={s}
                onClick={() => setQuestion(s)}
                className="rounded-full bg-muted-100 px-3 py-1 font-semibold text-muted-700 hover:bg-primary-50 hover:text-primary-700"
              >
                {s}
              </button>
            ))}
          </div>
        )}
        {aiAnswer && <div className="rounded-xl border border-primary-100 bg-primary-50 px-4 py-3 text-sm text-primary-900">{aiAnswer}</div>}
      </Card>
    </div>
  )
}
