import { Button } from './Button'

interface PaginationProps {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
}

export const Pagination = ({ page, totalPages, onPageChange }: PaginationProps) => {
  if (totalPages <= 1) return null
  return (
    <div className="mt-4 flex items-center justify-between gap-3">
      <Button variant="ghost" size="sm" disabled={page === 0} onClick={() => onPageChange(page - 1)}>
        Previous
      </Button>
      <p className="text-sm text-muted-600">
        Page {page + 1} of {totalPages}
      </p>
      <Button variant="ghost" size="sm" disabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>
        Next
      </Button>
    </div>
  )
}
