import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import { bookService } from '../../services/bookService'
import type { BookResponse } from '../../types'
import { formatCurrency, formatDate, formatGenre, formatStatus } from '../../utils/helpers'
import { Card } from '../../components/common/Card'
import { Button } from '../../components/common/Button'
import { Modal } from '../../components/common/Modal'
import { useAuth } from '../../hooks/useAuth'

export const BookDetailPage = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [book, setBook] = useState<BookResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [confirmDelete, setConfirmDelete] = useState(false)
  const { isAdmin } = useAuth()

  useEffect(() => {
    const load = async () => {
      if (!id) return
      try {
        const res = await bookService.get(Number(id))
        setBook(res)
      } catch (error) {
        toast.error('Book not found')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [id])

  const handleDelete = async () => {
    if (!book) return
    try {
      await bookService.remove(book.id)
      toast.success('Book deleted')
      navigate('/books')
    } catch (error) {
      toast.error('Could not delete book')
    }
  }

  if (loading) {
    return <p className="text-sm text-muted-600">Loading book...</p>
  }

  if (!book) {
    return <p className="text-sm text-muted-600">Book not found.</p>
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-muted-600">{formatDate(book.createdAt)}</p>
          <h1 className="text-2xl font-bold text-muted-900">{book.title}</h1>
          <p className="text-sm text-muted-600">by {book.author}</p>
        </div>
        <div className="flex gap-2">
          <Button variant="secondary" onClick={() => navigate(`/books/${book.id}/edit`)}>
            Edit
          </Button>
          {!isAdmin && (
            <Button variant="danger" onClick={() => setConfirmDelete(true)}>
              Delete
            </Button>
          )}
        </div>
      </div>
      <Card className="space-y-3">
        <div className="flex flex-wrap gap-3 text-sm text-muted-700">
          <span className="rounded-full bg-primary-50 px-3 py-1 font-semibold text-primary-700">{formatGenre(book.genre)}</span>
          {book.status && <span className="rounded-full bg-muted-100 px-3 py-1 font-semibold text-muted-700">{formatStatus(book.status)}</span>}
          {book.price !== undefined && <span className="font-semibold text-primary-700">{formatCurrency(book.price)}</span>}
          {book.publicationYear && <span>Published {book.publicationYear}</span>}
          {book.pageCount && <span>{book.pageCount} pages</span>}
          {book.isbn && <span>ISBN {book.isbn}</span>}
        </div>
        {book.description && <p className="text-sm text-muted-700">{book.description}</p>}
        <p className="text-xs text-muted-500">Last updated {formatDate(book.updatedAt)}</p>
      </Card>

      <Modal
        title="Delete book"
        description="This action cannot be undone."
        isOpen={confirmDelete}
        onClose={() => setConfirmDelete(false)}
        onConfirm={handleDelete}
        confirmText="Delete book"
        confirmTone="danger"
      />
    </div>
  )
}
