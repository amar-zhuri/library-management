import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { adminService } from '../../services/adminService'
import type { NewsletterRequest, NewsletterResponse, NewsletterStats, PagedResponse } from '../../types'
import { Card } from '../../components/common/Card'
import { Input } from '../../components/common/Input'
import { Button } from '../../components/common/Button'
import { Pagination } from '../../components/common/Pagination'
import { formatDate } from '../../utils/helpers'

export const NewsletterPage = () => {
  const [newsletters, setNewsletters] = useState<PagedResponse<NewsletterResponse> | null>(null)
  const [params, setParams] = useState({ page: 0, size: 5 })
  const [loading, setLoading] = useState(true)
  const [sendingId, setSendingId] = useState<number | null>(null)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [stats, setStats] = useState<NewsletterStats | null>(null)
  const {
    register,
    handleSubmit,
    reset,
    formState: { isSubmitting },
  } = useForm<NewsletterRequest>()

  const load = async () => {
    try {
      setLoading(true)
      const res = await adminService.listNewsletters(params)
      setNewsletters(res)
    } catch {
      toast.error('Unable to load newsletters')
    } finally {
      setLoading(false)
    }
  }

  const loadStats = async () => {
    try {
      const res = await adminService.newsletterStats()
      setStats(res)
    } catch {
      setStats(null)
    }
  }

  useEffect(() => {
    load()
    loadStats()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params])

  const onSubmit = async (values: NewsletterRequest) => {
    try {
      if (editingId) {
        await adminService.updateNewsletter(editingId, values)
        toast.success('Newsletter updated')
      } else {
        await adminService.createNewsletter(values)
        toast.success('Newsletter draft created')
      }
      reset()
      setEditingId(null)
      load()
      loadStats()
    } catch {
      toast.error('Unable to save newsletter')
    }
  }

  const handleSend = async (id: number) => {
    try {
      setSendingId(id)
      await adminService.sendNewsletter(id)
      toast.success('Newsletter sent')
      load()
      loadStats()
    } catch {
      toast.error('Unable to send newsletter')
    } finally {
      setSendingId(null)
    }
  }

  const handleEdit = (item: NewsletterResponse) => {
    setEditingId(item.id)
    reset({ subject: item.subject, content: item.content })
  }

  const handleDelete = async (id: number) => {
    const confirm = window.confirm('Delete this newsletter draft?')
    if (!confirm) return
    try {
      await adminService.deleteNewsletter(id)
      toast.success('Newsletter deleted')
      if (editingId === id) {
        setEditingId(null)
        reset()
      }
      load()
      loadStats()
    } catch {
      toast.error('Unable to delete newsletter')
    }
  }

  return (
    <div className="space-y-4">
      <div>
        <p className="text-sm text-muted-600">Engage your readers</p>
        <h1 className="text-2xl font-bold text-muted-900">Newsletter</h1>
      </div>

      {stats && (
        <div className="grid gap-3 md:grid-cols-3">
          <Card>
            <p className="text-xs uppercase tracking-wide text-muted-500">Total</p>
            <p className="text-2xl font-bold text-muted-900">{stats.totalNewsletters ?? 0}</p>
          </Card>
          <Card>
            <p className="text-xs uppercase tracking-wide text-muted-500">Drafts</p>
            <p className="text-2xl font-bold text-muted-900">{stats.drafts ?? 0}</p>
          </Card>
          <Card>
            <p className="text-xs uppercase tracking-wide text-muted-500">Sent</p>
            <p className="text-2xl font-bold text-muted-900">{stats.sent ?? 0}</p>
          </Card>
        </div>
      )}

      <Card className="space-y-3">
        <div className="flex items-center justify-between gap-3">
          <h2 className="text-lg font-semibold text-muted-900">Compose</h2>
          {editingId && (
            <div className="flex items-center gap-2 text-xs text-primary-700">
              <span className="rounded-full bg-primary-50 px-2 py-1 font-semibold">Editing draft #{editingId}</span>
              <Button size="sm" variant="ghost" onClick={() => { reset(); setEditingId(null) }}>
                Cancel
              </Button>
            </div>
          )}
        </div>
        <form className="space-y-3" onSubmit={handleSubmit(onSubmit)}>
          <Input label="Subject" placeholder="Monthly digest" {...register('subject', { required: true })} />
          <label className="flex flex-col gap-2 text-sm font-medium text-muted-700">
            Content
            <textarea
              rows={4}
              className="rounded-xl border border-muted-200 bg-white px-3 py-3 text-sm text-muted-800 focus:border-primary-400 focus:outline-none focus:ring-2 focus:ring-primary-100"
              placeholder="Write your newsletter..."
              {...register('content', { required: true })}
            />
          </label>
          <Button type="submit" loading={isSubmitting}>
            Save draft
          </Button>
        </form>
      </Card>

      <Card className="space-y-3">
            <h2 className="text-lg font-semibold text-muted-900">Drafts</h2>
            {loading && <p className="text-sm text-muted-600">Loading drafts...</p>}
            {!loading && newsletters?.content.length === 0 && <p className="text-sm text-muted-600">No drafts yet.</p>}
            <div className="space-y-3">
              {newsletters?.content.map((item) => (
                <div key={item.id} className="rounded-lg border border-muted-100 bg-white px-3 py-2">
                  <div className="flex items-center justify-between gap-3">
                    <div>
                      <p className="text-sm font-semibold text-muted-900">{item.subject}</p>
                      <p className="text-xs text-muted-600">
                        Status: {item.status} • Updated {formatDate(item.updatedAt)}
                      </p>
                    </div>
                    <div className="flex gap-2">
                      <Button variant="ghost" size="sm" onClick={() => handleEdit(item)}>
                        Edit
                      </Button>
                      <Button variant="danger" size="sm" onClick={() => handleDelete(item.id)}>
                        Delete
                      </Button>
                      <Button size="sm" onClick={() => handleSend(item.id)} loading={sendingId === item.id}>
                        Send
                      </Button>
                    </div>
                  </div>
                  <p className="mt-2 line-clamp-2 text-sm text-muted-700">{item.content}</p>
                </div>
              ))}
            </div>
        {newsletters && (
          <Pagination
            page={newsletters.page}
            totalPages={newsletters.totalPages}
            onPageChange={(page) => setParams((prev) => ({ ...prev, page }))}
          />
        )}
      </Card>
    </div>
  )
}
