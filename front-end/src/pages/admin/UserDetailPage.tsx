import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import { adminService } from '../../services/adminService'
import type { UserDetailResponse } from '../../types'
import { Card } from '../../components/common/Card'
import { BookCard } from '../../components/books/BookCard'
import { formatDate } from '../../utils/helpers'

export const UserDetailPage = () => {
  const { id } = useParams()
  const [user, setUser] = useState<UserDetailResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      if (!id) return
      try {
        const res = await adminService.userDetail(Number(id))
        setUser(res)
      } catch {
        toast.error('Unable to load user')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [id])

  if (loading) return <p className="text-sm text-muted-600">Loading user...</p>
  if (!user) return <p className="text-sm text-muted-600">User not found.</p>

  return (
    <div className="space-y-4">
      <Card className="flex items-center justify-between">
        <div>
          <p className="text-xs uppercase tracking-wide text-primary-700">{user.role}</p>
          <h1 className="text-xl font-semibold text-muted-900">{user.name}</h1>
          <p className="text-sm text-muted-700">{user.email}</p>
        </div>
        <div className="text-sm text-muted-600">
          <p>Created {formatDate(user.createdAt)}</p>
          <p>Updated {formatDate(user.updatedAt)}</p>
        </div>
      </Card>

      <div>
        <h2 className="text-lg font-semibold text-muted-900">Books ({user.books.length})</h2>
        <div className="grid gap-4 md:grid-cols-2">
          {user.books.map((book) => (
            <BookCard key={book.id} book={book} />
          ))}
        </div>
      </div>
    </div>
  )
}
