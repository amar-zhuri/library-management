import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { BookForm } from '../../components/books/BookForm'
import { adminService } from '../../services/adminService'
import type { BookRequest } from '../../types'

export const AdminAddBookPage = () => {
  const navigate = useNavigate()
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (values: BookRequest) => {
    try {
      setSubmitting(true)
      const book = await adminService.createBook(values)
      toast.success('Book added to system')
      navigate(`/books/${book.id}`)
    } catch (error) {
      console.error(error)
      toast.error('Could not add book')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-muted-900">Add Book (Admin)</h1>
      <BookForm onSubmit={handleSubmit} submitting={submitting} />
    </div>
  )
}
