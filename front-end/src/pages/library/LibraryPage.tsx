import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { libraryService } from '../../services/libraryService'
import type { LibraryQueryParams } from '../../services/libraryService'
import type { BookResponse, PagedResponse } from '../../types'
import { BookCard } from '../../components/books/BookCard'
import { Button } from '../../components/common/Button'
import { Pagination } from '../../components/common/Pagination'
import { EmptyState } from '../../components/common/EmptyState'
import { getErrorMessage } from '../../utils/helpers'

export const LibraryPage = () => {
  const [data, setData] = useState<PagedResponse<BookResponse> | null>(null)
  const [params, setParams] = useState<LibraryQueryParams>({ page: 0, size: 6, sortBy: 'createdAt', sortDir: 'desc' })
  const [loading, setLoading] = useState(true)
  const [claimingId, setClaimingId] = useState<number | null>(null)
  const navigate = useNavigate()

  const load = async () => {
    try {
      setLoading(true)
      const res = await libraryService.list(params)
      setData(res)
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params])

  const claim = async (book: BookResponse) => {
    try {
      setClaimingId(book.id)
      await libraryService.claim(book.id)
      toast.success(`Added "${book.title}" to your library`)
      navigate('/books')
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setClaimingId(null)
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-muted-600">Shared by admins</p>
          <h1 className="text-2xl font-bold text-muted-900">Library</h1>
        </div>
        <Button variant="secondary" size="sm" onClick={() => load()}>
          Refresh
        </Button>
      </div>

      {loading && <p className="text-sm text-muted-600">Loading library...</p>}
      {!loading && data && data.content.length === 0 && <EmptyState title="No shared books" description="Admins haven't shared books yet." />}

      <div className="grid gap-4 md:grid-cols-2">
        {data?.content.map((book) => (
          <div key={book.id} className="space-y-2">
            <BookCard book={book} showStatus={false} />
            <div className="flex justify-end">
              <Button size="sm" onClick={() => claim(book)} loading={claimingId === book.id}>
                Add to my library
              </Button>
            </div>
          </div>
        ))}
      </div>

      {data && <Pagination page={data.page} totalPages={data.totalPages} onPageChange={(page) => setParams((prev) => ({ ...prev, page }))} />}
    </div>
  )
}
