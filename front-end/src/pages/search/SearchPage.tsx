import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { searchService } from '../../services/searchService'
import type { SearchParams } from '../../services/searchService'
import type { BookResponse, BookSearchResponse, SearchFiltersResponse } from '../../types'
import { Button } from '../../components/common/Button'
import { Input } from '../../components/common/Input'
import { BookCard } from '../../components/books/BookCard'
import { GENRES, READING_STATUSES } from '../../utils/constants'
import { toReadable } from '../../utils/helpers'
import { Pagination } from '../../components/common/Pagination'
import { EmptyState } from '../../components/common/EmptyState'
import { useDebounce } from '../../hooks/useDebounce'
import { bookService } from '../../services/bookService'

export const SearchPage = () => {
  const [params, setParams] = useState<SearchParams>({ page: 0, size: 6, sortBy: 'relevance', sortDir: 'desc' })
  const [results, setResults] = useState<BookSearchResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [suggestions, setSuggestions] = useState<string[]>([])
  const [filters, setFilters] = useState<SearchFiltersResponse | null>(null)
  const [quickResults, setQuickResults] = useState<BookResponse[]>([])
  const debouncedQuery = useDebounce(params.q ?? '', 400)

  const fetchResults = async () => {
    const sanitized: SearchParams = {
      page: params.page ?? 0,
      size: params.size ?? 6,
      sortBy: params.sortBy,
      sortDir: params.sortDir,
    }
    const copyFields: (keyof SearchParams)[] = [
      'q',
      'title',
      'author',
      'isbn',
      'genre',
      'status',
      'minPrice',
      'maxPrice',
      'minYear',
      'maxYear',
      'minPages',
      'maxPages',
    ]
    copyFields.forEach((key) => {
      const value = params[key]
      if (value !== undefined && value !== null && value !== '') {
        ;(sanitized as Record<string, unknown>)[key] = value
      }
    })

    try {
      setLoading(true)
      const res = await searchService.search(sanitized)
      setResults(res)
    } catch (error) {
      toast.error('Search failed')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchResults()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params])

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
    const loadSuggestions = async () => {
      if (!debouncedQuery || debouncedQuery.length < 2) {
        setSuggestions([])
        setQuickResults([])
        return
      }
      try {
        const res = await searchService.getSuggestions(debouncedQuery)
        setSuggestions([...(res.titles ?? []), ...(res.authors ?? [])])
        const quick = await bookService.quickSearch(debouncedQuery, 0, 3)
        setQuickResults(quick.content ?? [])
      } catch {
        setSuggestions([])
        setQuickResults([])
      }
    }
    loadSuggestions()
  }, [debouncedQuery])

  const onChange = (key: keyof SearchParams, value: string | number | undefined) => {
    setParams((prev) => ({ ...prev, [key]: value || undefined, page: 0 }))
  }

  const genreOptions = Array.from(new Set([...(filters?.genres ?? []), ...GENRES]))
  const statusOptions = Array.from(new Set([...(filters?.statuses ?? []), ...READING_STATUSES]))

  const clearFilters = () => {
    setParams({ page: 0, size: 6, sortBy: 'relevance', sortDir: 'desc' })
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3">
        <div>
          <p className="text-sm text-muted-600">Search your collection</p>
          <h1 className="text-2xl font-bold text-muted-900">Smart Search</h1>
        </div>
        <Button variant="secondary" onClick={clearFilters}>
          Clear filters
        </Button>
      </div>

      <div className="grid gap-3 md:grid-cols-4">
        <div className="md:col-span-2 space-y-2">
          <Input
            placeholder="Search by title, author, description or ISBN"
            value={params.q ?? ''}
            onChange={(e) => onChange('q', e.target.value)}
          />
          {suggestions.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {suggestions.slice(0, 6).map((s, idx) => (
                <button
                  key={`${s}-${idx}`}
                  className="rounded-full bg-muted-100 px-3 py-1 text-xs font-semibold text-muted-700 hover:bg-primary-50 hover:text-primary-700"
                  onClick={() => onChange('q', s)}
                >
                  {s}
                </button>
              ))}
            </div>
          )}
        </div>
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
      </div>

      <div className="grid gap-3 md:grid-cols-4">
        <Input
          label="Min Price"
          type="number"
          value={params.minPrice ?? ''}
          onChange={(e) => onChange('minPrice', e.target.value ? Number(e.target.value) : undefined)}
        />
        <Input
          label="Max Price"
          type="number"
          value={params.maxPrice ?? ''}
          onChange={(e) => onChange('maxPrice', e.target.value ? Number(e.target.value) : undefined)}
        />
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
        <Input label="Title" value={params.title ?? ''} onChange={(e) => onChange('title', e.target.value)} />
        <Input
          label="Author"
          value={params.author ?? ''}
          onChange={(e) => onChange('author', e.target.value)}
          helperText={filters?.authors?.length ? `Top: ${filters.authors.slice(0, 3).join(', ')}` : undefined}
        />
        <Input label="ISBN" value={params.isbn ?? ''} onChange={(e) => onChange('isbn', e.target.value)} />
        <div className="grid grid-cols-2 gap-3">
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
      </div>

      {quickResults.length > 0 && (
        <div className="space-y-2 rounded-xl border border-muted-100 bg-white px-3 py-2">
          <div className="flex items-center justify-between text-xs font-semibold uppercase tracking-wide text-primary-700">
            <span>Quick hits</span>
            <span>{quickResults.length}</span>
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            {quickResults.map((book) => (
              <BookCard key={`quick-${book.id}`} book={book} />
            ))}
          </div>
        </div>
      )}

      {loading && <p className="text-sm text-muted-600">Searching...</p>}

      {!loading && results && results.books.length === 0 && (
        <EmptyState title="No results" description="Try adjusting your filters or keywords." />
      )}

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

      {results?.facets && (
        <div className="rounded-xl border border-muted-100 bg-white px-4 py-3 text-sm text-muted-700">
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-primary-700">Facets</p>
          <div className="grid gap-2 md:grid-cols-2">
            {Object.entries(results.facets).map(([key, value]) => (
              <div key={key} className="rounded-lg bg-muted-50 px-3 py-2">
                <p className="text-xs font-semibold text-muted-600">{toReadable(key)}</p>
                <p className="text-sm text-muted-800">
                  {typeof value === 'object' ? JSON.stringify(value) : String(value)}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
