import { useForm } from 'react-hook-form'
import { GENRES, READING_STATUSES } from '../../utils/constants'
import type { BookRequest } from '../../types'
import { Button } from '../common/Button'
import { Input } from '../common/Input'
import { Card } from '../common/Card'

interface BookFormProps {
  defaultValues?: Partial<BookRequest>
  onSubmit: (values: BookRequest) => Promise<void> | void
  submitting?: boolean
  title?: string
}

export const BookForm = ({ defaultValues, onSubmit, submitting, title }: BookFormProps) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<BookRequest>({
    defaultValues: {
      title: '',
      author: '',
      genre: 'FICTION',
      status: 'TO_READ',
      ...defaultValues,
    },
  })

  return (
    <Card className="space-y-6">
      {title && <h2 className="text-xl font-semibold text-muted-900">{title}</h2>}
      <form className="grid grid-cols-1 gap-4 md:grid-cols-2" onSubmit={handleSubmit(onSubmit)}>
        <Input label="Title" placeholder="Book title" {...register('title', { required: 'Title is required' })} error={errors.title?.message} />
        <Input label="Author" placeholder="Book author" {...register('author', { required: 'Author is required' })} error={errors.author?.message} />
        <label className="flex flex-col gap-2 text-sm font-medium text-muted-700">
          Genre
          <select
            className="rounded-lg border border-muted-200 bg-white px-3 py-2.5 text-sm text-muted-800 focus:border-primary-400 focus:outline-none focus:ring-2 focus:ring-primary-100"
            {...register('genre', { required: true })}
          >
            {GENRES.map((genre) => (
              <option key={genre} value={genre}>
                {genre.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </label>
        <label className="flex flex-col gap-2 text-sm font-medium text-muted-700">
          Status
          <select
            className="rounded-lg border border-muted-200 bg-white px-3 py-2.5 text-sm text-muted-800 focus:border-primary-400 focus:outline-none focus:ring-2 focus:ring-primary-100"
            {...register('status')}
          >
            {READING_STATUSES.map((status) => (
              <option key={status} value={status}>
                {status.replace(/_/g, ' ')}
              </option>
            ))}
          </select>
        </label>
        <Input
          label="Price"
          type="number"
          step="0.01"
          placeholder="12.99"
          {...register('price', { valueAsNumber: true })}
          error={errors.price?.message}
        />
        <Input label="ISBN" placeholder="978..." {...register('isbn')} error={errors.isbn?.message} />
        <Input
          label="Page Count"
          type="number"
          min={1}
          placeholder="350"
          {...register('pageCount', { valueAsNumber: true })}
          error={errors.pageCount?.message}
        />
        <Input
          label="Publication Year"
          type="number"
          min={1000}
          max={2100}
          placeholder="2024"
          {...register('publicationYear', { valueAsNumber: true })}
          error={errors.publicationYear?.message}
        />
        <label className="md:col-span-2 flex flex-col gap-2 text-sm font-medium text-muted-700">
          Description
          <textarea
            rows={4}
            className="rounded-xl border border-muted-200 bg-white px-3 py-3 text-sm text-muted-800 focus:border-primary-400 focus:outline-none focus:ring-2 focus:ring-primary-100"
            placeholder="What is this book about?"
            {...register('description')}
          />
        </label>
        <div className="md:col-span-2 flex justify-end">
          <Button type="submit" loading={submitting}>
            Save Book
          </Button>
        </div>
      </form>
    </Card>
  )
}
