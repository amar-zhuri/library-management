import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { bookService } from '../../services/bookService'
import type { BookRequest } from '../../types'
import { BookForm } from '../../components/books/BookForm'

export const AddBookPage = () => {
  const navigate = useNavigate()
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (values: BookRequest) => {
    try {
      setSubmitting(true)
      const book = await bookService.create(values)
      toast.success('Book added')
      navigate(`/books/${book.id}`)
    } catch {
      toast.error('Could not add book')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-muted-900">Add Book</h1>
      <BookForm onSubmit={handleSubmit} submitting={submitting} />
    </div>
  )
}
