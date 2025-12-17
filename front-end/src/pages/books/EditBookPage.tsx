import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import { bookService } from '../../services/bookService'
import type { BookRequest, BookResponse } from '../../types'
import { BookForm } from '../../components/books/BookForm'

export const EditBookPage = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [book, setBook] = useState<BookResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    const load = async () => {
      if (!id) return
      try {
        const res = await bookService.get(Number(id))
        setBook(res)
      } catch {
        toast.error('Book not found')
        navigate('/books')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [id, navigate])

  const handleSubmit = async (values: BookRequest) => {
    if (!id) return
    try {
      setSubmitting(true)
      await bookService.update(Number(id), values)
      toast.success('Book updated')
      navigate(`/books/${id}`)
    } catch {
      toast.error('Could not update book')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <p className="text-sm text-muted-600">Loading book...</p>
  if (!book) return null

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-muted-900">Edit Book</h1>
      <BookForm defaultValues={book} onSubmit={handleSubmit} submitting={submitting} />
    </div>
  )
}
