import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Layout } from '../../components/layout';
import { Input, Button, Alert, Select, Textarea } from '../../components/common';
import { bookService } from '../../services/bookService';
import type { BookRequest, Genre, ReadingStatus } from '../../types';
import { GENRES, READING_STATUSES } from '../../utils/constants';

export function AddBookPage() {
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState<BookRequest>({
    title: '',
    author: '',
    genre: 'FICTION' as Genre,
    status: 'TO_READ' as ReadingStatus,
    description: '',
    isbn: '',
    pageCount: undefined,
    publicationYear: undefined,
    price: undefined,
  });
  
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (field: keyof BookRequest, value: string | number | undefined) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const book = await bookService.createBook(formData);
      navigate(`/books/${book.id}`);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      setError(error.response?.data?.message || 'Failed to create book');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Layout>
      {/* Back link */}
      <Link to="/books" className="text-indigo-600 hover:text-indigo-500 mb-6 inline-block">
        ← Back to Books
      </Link>

      <div className="max-w-2xl">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Add New Book</h1>

        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-8">
          {error && <Alert type="error" message={error} onClose={() => setError('')} />}

          <form onSubmit={handleSubmit}>
            {/* Required Fields */}
            <Input
              id="title"
              label="Title *"
              type="text"
              value={formData.title}
              onChange={(e) => handleChange('title', e.target.value)}
              placeholder="Enter book title"
              required
              maxLength={255}
            />

            <Input
              id="author"
              label="Author *"
              type="text"
              value={formData.author}
              onChange={(e) => handleChange('author', e.target.value)}
              placeholder="Enter author name"
              required
              maxLength={255}
            />

            <Select
              id="genre"
              label="Genre *"
              value={formData.genre}
              onChange={(value) => handleChange('genre', value as Genre)}
              options={GENRES}
              required
            />

            <Select
              id="status"
              label="Reading Status"
              value={formData.status || 'TO_READ'}
              onChange={(value) => handleChange('status', value as ReadingStatus)}
              options={READING_STATUSES}
            />

            {/* Optional Fields */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                id="pageCount"
                label="Page Count"
                type="number"
                value={formData.pageCount || ''}
                onChange={(e) => handleChange('pageCount', e.target.value ? Number(e.target.value) : undefined)}
                placeholder="e.g., 320"
                min={1}
              />

              <Input
                id="publicationYear"
                label="Publication Year"
                type="number"
                value={formData.publicationYear || ''}
                onChange={(e) => handleChange('publicationYear', e.target.value ? Number(e.target.value) : undefined)}
                placeholder="e.g., 2023"
                min={1000}
                max={2100}
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                id="price"
                label="Price ($)"
                type="number"
                value={formData.price || ''}
                onChange={(e) => handleChange('price', e.target.value ? Number(e.target.value) : undefined)}
                placeholder="e.g., 19.99"
                min={0}
                step="0.01"
              />

              <Input
                id="isbn"
                label="ISBN"
                type="text"
                value={formData.isbn || ''}
                onChange={(e) => handleChange('isbn', e.target.value)}
                placeholder="e.g., 978-0-123456-78-9"
                maxLength={20}
              />
            </div>

            <Textarea
              id="description"
              label="Description"
              value={formData.description || ''}
              onChange={(e) => handleChange('description', e.target.value)}
              placeholder="Enter book description..."
              maxLength={2000}
            />

            {/* Actions */}
            <div className="flex justify-end gap-3 mt-6">
              <Button type="button" variant="secondary" onClick={() => navigate('/books')}>
                Cancel
              </Button>
              <Button type="submit" isLoading={isLoading}>
                Add Book
              </Button>
            </div>
          </form>
        </div>
      </div>
    </Layout>
  );
}