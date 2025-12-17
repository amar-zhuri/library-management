import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { Layout } from '../../components/layout';
import { Loading, Alert, Button, Modal } from '../../components/common';
import { bookService } from '../../services/bookService';
import type { Book } from '../../types';
import { getGenreLabel, getStatusLabel, getStatusColor, READING_STATUSES } from '../../utils/constants';

export function BookDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  
  const [book, setBook] = useState<Book | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    const fetchBook = async () => {
      if (!id) return;
      
      try {
        const data = await bookService.getBook(Number(id));
        setBook(data);
      } catch (err: unknown) {
        const error = err as { response?: { data?: { message?: string } } };
        setError(error.response?.data?.message || 'Failed to load book');
      } finally {
        setIsLoading(false);
      }
    };

    fetchBook();
  }, [id]);

  const handleStatusChange = async (newStatus: string) => {
    if (!book) return;
    
    try {
      const updated = await bookService.updateStatus(book.id, newStatus);
      setBook(updated);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      setError(error.response?.data?.message || 'Failed to update status');
    }
  };

  const handleDelete = async () => {
    if (!book) return;
    
    setIsDeleting(true);
    try {
      await bookService.deleteBook(book.id);
      navigate('/books');
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      setError(error.response?.data?.message || 'Failed to delete book');
      setShowDeleteModal(false);
    } finally {
      setIsDeleting(false);
    }
  };

  if (isLoading) {
    return (
      <Layout>
        <Loading />
      </Layout>
    );
  }

  if (error || !book) {
    return (
      <Layout>
        <Alert type="error" message={error || 'Book not found'} />
        <Link to="/books" className="text-indigo-600 hover:text-indigo-500 mt-4 inline-block">
          ← Back to Books
        </Link>
      </Layout>
    );
  }

  return (
    <Layout>
      {/* Back link */}
      <Link to="/books" className="text-indigo-600 hover:text-indigo-500 mb-6 inline-block">
        ← Back to Books
      </Link>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="p-8">
          {/* Header */}
          <div className="flex items-start justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 mb-2">{book.title}</h1>
              <p className="text-xl text-gray-600">by {book.author}</p>
            </div>
            <div className="flex items-center gap-3">
              <Link
                to={`/books/${book.id}/edit`}
                className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
              >
                Edit
              </Link>
              <Button variant="danger" onClick={() => setShowDeleteModal(true)}>
                Delete
              </Button>
            </div>
          </div>

          {/* Status selector */}
          <div className="mb-8">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Reading Status
            </label>
            <div className="flex flex-wrap gap-2">
              {READING_STATUSES.map((status) => (
                <button
                  key={status.value}
                  onClick={() => handleStatusChange(status.value)}
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                    book.status === status.value
                      ? getStatusColor(status.value) + ' ring-2 ring-offset-2 ring-indigo-500'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                >
                  {status.label}
                </button>
              ))}
            </div>
          </div>

          {/* Details grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <div className="bg-gray-50 p-4 rounded-lg">
              <p className="text-sm text-gray-500 mb-1">Genre</p>
              <p className="font-medium text-gray-900">{getGenreLabel(book.genre)}</p>
            </div>
            {book.pageCount && (
              <div className="bg-gray-50 p-4 rounded-lg">
                <p className="text-sm text-gray-500 mb-1">Pages</p>
                <p className="font-medium text-gray-900">{book.pageCount}</p>
              </div>
            )}
            {book.publicationYear && (
              <div className="bg-gray-50 p-4 rounded-lg">
                <p className="text-sm text-gray-500 mb-1">Published</p>
                <p className="font-medium text-gray-900">{book.publicationYear}</p>
              </div>
            )}
            {book.price && (
              <div className="bg-gray-50 p-4 rounded-lg">
                <p className="text-sm text-gray-500 mb-1">Price</p>
                <p className="font-medium text-gray-900">${book.price.toFixed(2)}</p>
              </div>
            )}
          </div>

          {/* ISBN */}
          {book.isbn && (
            <div className="mb-8">
              <p className="text-sm text-gray-500 mb-1">ISBN</p>
              <p className="font-mono text-gray-900">{book.isbn}</p>
            </div>
          )}

          {/* Description */}
          {book.description && (
            <div>
              <p className="text-sm text-gray-500 mb-2">Description</p>
              <p className="text-gray-700 whitespace-pre-wrap">{book.description}</p>
            </div>
          )}

          {/* Timestamps */}
          <div className="mt-8 pt-6 border-t border-gray-200 text-sm text-gray-500">
            <p>Added on {new Date(book.createdAt).toLocaleDateString()}</p>
            {book.updatedAt !== book.createdAt && (
              <p>Last updated {new Date(book.updatedAt).toLocaleDateString()}</p>
            )}
          </div>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={showDeleteModal}
        onClose={() => setShowDeleteModal(false)}
        title="Delete Book"
      >
        <p className="text-gray-600 mb-6">
          Are you sure you want to delete "{book.title}"? This action cannot be undone.
        </p>
        <div className="flex justify-end gap-3">
          <Button variant="secondary" onClick={() => setShowDeleteModal(false)}>
            Cancel
          </Button>
          <Button variant="danger" onClick={handleDelete} isLoading={isDeleting}>
            Delete
          </Button>
        </div>
      </Modal>
    </Layout>
  );
}