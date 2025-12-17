import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { bookService } from '../../services/bookService'
import type { BookQueryParams } from '../../services/bookService'
import type { BookResponse, PagedResponse } from '../../types'
import { getErrorMessage, toReadable } from '../../utils/helpers'
import { BookCard } from '../../components/books/BookCard'
import { Button } from '../../components/common/Button'
import { Input } from '../../components/common/Input'
import { Modal } from '../../components/common/Modal'
import { EmptyState } from '../../components/common/EmptyState'
import { Pagination } from '../../components/common/Pagination'
import { GENRES, READING_STATUSES } from '../../utils/constants'
import { useAuth } from '../../hooks/useAuth'

export const BooksPage = () => {
  const [data, setData] = useState<PagedResponse<BookResponse> | null>(null)
  const [filters, setFilters] = useState<BookQueryParams>({ page: 0, size: 6, sortBy: 'createdAt', sortDir: 'desc' })
  const [loading, setLoading] = useState(true)
  const [deleteTarget, setDeleteTarget] = useState<BookResponse | null>(null)
  const navigate = useNavigate()
  const { isAdmin } = useAuth()

  const fetchBooks = async () => {
    try {
      setLoading(true)
      const res = await bookService.list(filters)
      setData(res)
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchBooks()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters])

  const handleDelete = async () => {
    if (!deleteTarget) return
    try {
      await bookService.remove(deleteTarget.id)
      toast.success('Book deleted')
      setDeleteTarget(null)
      fetchBooks()
    } catch (error) {
      toast.error(getErrorMessage(error))
    }
  }

  const onFilterChange = (key: keyof BookQueryParams, value: string) => {
    setFilters((prev) => ({ ...prev, [key]: value || undefined, page: 0 }))
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-sm text-muted-600">Your personal library</p>
          <h1 className="text-2xl font-bold text-muted-900">Books</h1>
        </div>
        <Button onClick={() => navigate('/books/new')}>Add Book</Button>
      </div>

      <div className="grid gap-3 md:grid-cols-4">
        <div className="md:col-span-2">
          <Input
            placeholder="Search by title, author, ISBN..."
            value={filters.search ?? ''}
            onChange={(e) => onFilterChange('search', e.target.value)}
          />
        </div>
        <label className="flex flex-col gap-2 text-sm font-medium text-muted-700">
          Genre
          <select
            className="rounded-lg border border-muted-200 bg-white px-3 py-2.5 text-sm text-muted-800 focus:border-primary-400 focus:outline-none focus:ring-2 focus:ring-primary-100"
            value={filters.genre ?? ''}
            onChange={(e) => onFilterChange('genre', e.target.value)}
          >
            <option value="">All</option>
            {GENRES.map((genre) => (
              <option key={genre} value={genre}>
                {toReadable(genre)}
              </option>
            ))}
          </select>
        </label>
        <label className="flex flex-col gap-2 text-sm font-medium text-muted-700">
          Status
          <select
            className="rounded-lg border border-muted-200 bg-white px-3 py-2.5 text-sm text-muted-800 focus:border-primary-400 focus:outline-none focus:ring-2 focus:ring-primary-100"
            value={filters.status ?? ''}
            onChange={(e) => onFilterChange('status', e.target.value)}
          >
            <option value="">All</option>
            {READING_STATUSES.map((status) => (
              <option key={status} value={status}>
                {toReadable(status)}
              </option>
            ))}
          </select>
        </label>
      </div>

      {loading && <div className="text-sm text-muted-600">Loading books...</div>}

      {!loading && data && data.content.length === 0 && (
        <EmptyState
          title="No books yet"
          description="Add your first book to start tracking your reading."
          actionText="Add Book"
          onAction={() => navigate('/books/new')}
        />
      )}

      <div className="grid gap-4 md:grid-cols-2">
        {data?.content.map((book) => (
          <BookCard
            key={book.id}
            book={book}
            onView={(b) => navigate(`/books/${b.id}`)}
            onEdit={(b) => navigate(`/books/${b.id}/edit`)}
            onDelete={isAdmin ? undefined : setDeleteTarget}
          />
        ))}
      </div>

      {data && <Pagination page={data.page} totalPages={data.totalPages} onPageChange={(page) => setFilters((prev) => ({ ...prev, page }))} />}

      <Modal
        title="Delete book"
        description={`Are you sure you want to delete "${deleteTarget?.title}"?`}
        isOpen={Boolean(deleteTarget)}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        confirmText="Delete"
        confirmTone="danger"
      />
    </div>
  )
}
