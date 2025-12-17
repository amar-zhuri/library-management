import type { BookResponse } from '../../types'
import { formatCurrency, formatDate, formatGenre, formatStatus } from '../../utils/helpers'
import { Badge } from '../common/Badge'
import { Button } from '../common/Button'
import { Card } from '../common/Card'

interface BookCardProps {
  book: BookResponse
  onView?: (book: BookResponse) => void
  onEdit?: (book: BookResponse) => void
  onDelete?: (book: BookResponse) => void
  showStatus?: boolean
}

export const BookCard = ({ book, onView, onEdit, onDelete, showStatus = true }: BookCardProps) => (
  <Card className="flex flex-col gap-4">
    <div className="flex items-start justify-between gap-4">
      <div>
        <p className="text-xs uppercase tracking-wide text-muted-500">{formatDate(book.createdAt)}</p>
        <h3 className="text-lg font-semibold text-muted-900">{book.title}</h3>
        <p className="text-sm text-muted-600">by {book.author}</p>
      </div>
      <div className="flex flex-col items-end gap-2">
        <Badge tone="primary">{formatGenre(book.genre)}</Badge>
        {showStatus && book.status && <Badge tone="neutral">{formatStatus(book.status)}</Badge>}
      </div>
    </div>
    {book.description && <p className="text-sm text-muted-700 line-clamp-3">{book.description}</p>}
    <div className="flex flex-wrap items-center gap-3 text-sm text-muted-600">
      {book.pageCount && <span>📖 {book.pageCount} pages</span>}
      {book.publicationYear && <span>📅 {book.publicationYear}</span>}
      {book.isbn && <span>ISBN {book.isbn}</span>}
      {book.price !== undefined && <span className="font-semibold text-primary-700">{formatCurrency(book.price)}</span>}
    </div>
    <div className="flex flex-wrap gap-2">
      {onView && (
        <Button variant="secondary" size="sm" onClick={() => onView(book)}>
          View
        </Button>
      )}
      {onEdit && (
        <Button variant="ghost" size="sm" onClick={() => onEdit(book)}>
          Edit
        </Button>
      )}
      {onDelete && (
        <Button variant="danger" size="sm" onClick={() => onDelete(book)}>
          Delete
        </Button>
      )}
    </div>
  </Card>
)
