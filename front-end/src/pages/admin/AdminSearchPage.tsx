import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { searchService } from '../../services/searchService'
import type { SearchParams } from '../../services/searchService'
import type { BookSearchResponse, SearchFiltersResponse } from '../../types'
import { Input } from '../../components/common/Input'
import { Button } from '../../components/common/Button'
import { BookCard } from '../../components/books/BookCard'
import { Pagination } from '../../components/common/Pagination'
import { EmptyState } from '../../components/common/EmptyState'
import { GENRES, READING_STATUSES } from '../../utils/constants'
import { toReadable } from '../../utils/helpers'

export const AdminSearchPage = () => {
  const [params, setParams] = useState<SearchParams>({ page: 0, size: 8, sortBy: 'createdAt', sortDir: 'desc' })
  const [results, setResults] = useState<BookSearchResponse | null>(null)
  const [filters, setFilters] = useState<SearchFiltersResponse | null>(null)
  const [loading, setLoading] = useState(false)

  const fetchResults = async () => {
    const sanitized: SearchParams = {
      page: params.page ?? 0,
      size: params.size ?? 8,
      sortBy: params.sortBy,
      sortDir: params.sortDir,
    }
    ;(['q', 'title', 'author', 'isbn', 'genre', 'status', 'minYear', 'maxYear', 'minPages', 'maxPages', 'minPrice', 'maxPrice'] as const).forEach(
      (key) => {
        const value = params[key]
        if (value !== undefined && value !== null && value !== '') {
          ;(sanitized as Record<string, unknown>)[key] = value
        }
      }
    )
    try {
      setLoading(true)
      const res = await searchService.searchAll(sanitized)
      const filtered = res.books.filter((book) => {
        if (sanitized.genre && book.genre !== sanitized.genre) return false
        if (sanitized.status && book.status !== sanitized.status) return false
        if (sanitized.author && !book.author.toLowerCase().includes(String(sanitized.author).toLowerCase())) return false
        if (sanitized.title && !book.title.toLowerCase().includes(String(sanitized.title).toLowerCase())) return false
        if (sanitized.isbn && !(book.isbn ?? '').toLowerCase().includes(String(sanitized.isbn).toLowerCase())) return false
        if (sanitized.minYear !== undefined && (book.publicationYear ?? 0) < sanitized.minYear) return false
        if (sanitized.maxYear !== undefined && (book.publicationYear ?? 0) > sanitized.maxYear) return false
        if (sanitized.minPages !== undefined && (book.pageCount ?? 0) < sanitized.minPages) return false
        if (sanitized.maxPages !== undefined && (book.pageCount ?? 0) > sanitized.maxPages) return false
        if (sanitized.minPrice !== undefined && (book.price ?? 0) < sanitized.minPrice) return false
        if (sanitized.maxPrice !== undefined && (book.price ?? 0) > sanitized.maxPrice) return false
        return true
      })

      const size = sanitized.size ?? 8
      const page = sanitized.page ?? 0
      const start = page * size
      const paged = filtered.slice(start, start + size)
      setResults({
        ...res,
        books: paged,
        page,
        size,
        totalElements: filtered.length,
        totalPages: Math.max(1, Math.ceil(filtered.length / size)),
        first: page === 0,
        last: start + size >= filtered.length,
      })
    } catch {
      toast.error('Admin search failed')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    const loadFilters = async () => {
      try {
        const res = await searchService.getFilters()
        setFilters(res)
      } catch {
        setFilters(null)
      }
    }
    loadFilters()
  }, [])

  useEffect(() => {
    fetchResults()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params])

  const onChange = (key: keyof SearchParams, value: string | number | undefined) => {
    setParams((prev) => ({ ...prev, [key]: value || undefined, page: 0 }))
  }

  const genreOptions = Array.from(new Set([...(filters?.genres ?? []), ...GENRES]))
  const statusOptions = Array.from(new Set([...(filters?.statuses ?? []), ...READING_STATUSES]))

  const clear = () => setParams({ page: 0, size: 8, sortBy: 'createdAt', sortDir: 'desc' })

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3">
        <div>
          <p className="text-sm text-muted-600">System-wide search</p>
          <h1 className="text-2xl font-bold text-muted-900">Admin Search</h1>
        </div>
        <Button variant="secondary" onClick={clear}>
          Clear filters
        </Button>
      </div>

      <div className="grid gap-3 md:grid-cols-4">
        <div className="md:col-span-2">
          <Input placeholder="Search across all books" value={params.q ?? ''} onChange={(e) => onChange('q', e.target.value)} />
        </div>
        <Input label="Title" value={params.title ?? ''} onChange={(e) => onChange('title', e.target.value)} />
        <Input label="Author" value={params.author ?? ''} onChange={(e) => onChange('author', e.target.value)} />
        <Input label="ISBN" value={params.isbn ?? ''} onChange={(e) => onChange('isbn', e.target.value)} />
      </div>

      <div className="grid gap-3 md:grid-cols-4">
        <label className="flex flex-col gap-2 text-sm font-medium text-muted-700">
          Genre
          <select
            className="rounded-lg border border-muted-200 bg-white px-3 py-2.5 text-sm text-muted-800 focus:border-primary-400 focus:outline-none focus:ring-2 focus:ring-primary-100"
            value={params.genre ?? ''}
            onChange={(e) => onChange('genre', e.target.value)}
          >
            <option value="">All</option>
            {genreOptions.map((g) => (
              <option key={g} value={g}>
                {toReadable(g)}
              </option>
            ))}
          </select>
        </label>
        <label className="flex flex-col gap-2 text-sm font-medium text-muted-700">
          Status
          <select
            className="rounded-lg border border-muted-200 bg-white px-3 py-2.5 text-sm text-muted-800 focus:border-primary-400 focus:outline-none focus:ring-2 focus:ring-primary-100"
            value={params.status ?? ''}
            onChange={(e) => onChange('status', e.target.value)}
          >
            <option value="">All</option>
            {statusOptions.map((s) => (
              <option key={s} value={s}>
                {toReadable(s)}
              </option>
            ))}
          </select>
        </label>
        <Input
          label="Min Year"
          type="number"
          value={params.minYear ?? ''}
          onChange={(e) => onChange('minYear', e.target.value ? Number(e.target.value) : undefined)}
        />
        <Input
          label="Max Year"
          type="number"
          value={params.maxYear ?? ''}
          onChange={(e) => onChange('maxYear', e.target.value ? Number(e.target.value) : undefined)}
        />
      </div>

      <div className="grid gap-3 md:grid-cols-4">
        <Input
          label="Min Price"
          type="number"
          step="0.01"
          value={params.minPrice ?? ''}
          onChange={(e) => onChange('minPrice', e.target.value ? Number(e.target.value) : undefined)}
        />
        <Input
          label="Max Price"
          type="number"
          step="0.01"
          value={params.maxPrice ?? ''}
          onChange={(e) => onChange('maxPrice', e.target.value ? Number(e.target.value) : undefined)}
        />
        <Input
          label="Min Pages"
          type="number"
          value={params.minPages ?? ''}
          onChange={(e) => onChange('minPages', e.target.value ? Number(e.target.value) : undefined)}
        />
        <Input
          label="Max Pages"
          type="number"
          value={params.maxPages ?? ''}
          onChange={(e) => onChange('maxPages', e.target.value ? Number(e.target.value) : undefined)}
        />
      </div>

      {loading && <p className="text-sm text-muted-600">Searching...</p>}
      {!loading && results && results.books.length === 0 && <EmptyState title="No results" description="No books match your filters." />}

      <div className="grid gap-4 md:grid-cols-2">
        {results?.books.map((book) => (
          <BookCard key={book.id} book={book} />
        ))}
      </div>

      {results && (
        <Pagination
          page={results.page}
          totalPages={results.totalPages}
          onPageChange={(page) => setParams((prev) => ({ ...prev, page }))}
        />
      )}
    </div>
  )
}
