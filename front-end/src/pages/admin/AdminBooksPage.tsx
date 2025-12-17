import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { adminService } from '../../services/adminService'
import type { BookResponse, PagedResponse } from '../../types'
import { Button } from '../../components/common/Button'
import { Pagination } from '../../components/common/Pagination'
import { BookCard } from '../../components/books/BookCard'
import { Modal } from '../../components/common/Modal'
import { EmptyState } from '../../components/common/EmptyState'
import { useNavigate } from 'react-router-dom'

export const AdminBooksPage = () => {
  const [data, setData] = useState<PagedResponse<BookResponse> | null>(null)
  const [params, setParams] = useState({ page: 0, size: 6, sortBy: 'createdAt', sortDir: 'desc' })
  const [loading, setLoading] = useState(true)
  const [deleteId, setDeleteId] = useState<number | null>(null)
  const navigate = useNavigate()

  const load = async () => {
    try {
      setLoading(true)
      const res = await adminService.books(params)
      setData(res)
    } catch {
      toast.error('Unable to load books')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params])

  const handleDelete = async () => {
    if (!deleteId) return
    try {
      await adminService.deleteBook(deleteId)
      toast.success('Book removed')
      setDeleteId(null)
      load()
    } catch {
      toast.error('Could not delete book')
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-muted-600">System-wide</p>
          <h1 className="text-2xl font-bold text-muted-900">All Books</h1>
        </div>
        <div className="flex gap-2">
          <Button size="sm" onClick={() => navigate('/admin/books/new')}>
            Add Book
          </Button>
          <Button variant="secondary" size="sm" onClick={() => load()}>
            Refresh
          </Button>
        </div>
      </div>

      {loading && <p className="text-sm text-muted-600">Loading books...</p>}
      {!loading && data && data.content.length === 0 && <EmptyState title="No books found" />}

      <div className="grid gap-4 md:grid-cols-2">
        {data?.content.map((book) => (
          <BookCard key={book.id} book={book} onDelete={() => setDeleteId(book.id)} />
        ))}
      </div>

      {data && <Pagination page={data.page} totalPages={data.totalPages} onPageChange={(page) => setParams((prev) => ({ ...prev, page }))} />}

      <Modal
        title="Delete book"
        description="Remove this book from the system?"
        isOpen={Boolean(deleteId)}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDelete}
        confirmText="Delete"
        confirmTone="danger"
      />
    </div>
  )
}
