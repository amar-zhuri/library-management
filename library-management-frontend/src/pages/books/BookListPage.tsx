import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Layout } from '../../components/layout';
import { Loading, Alert, Pagination, EmptyState } from '../../components/common';
import { BookCard } from '../../components/books';
import { bookService } from '../../services/bookService';
import type { Book } from '../../types';

export function BookListPage() {
  const [books, setBooks] = useState<Book[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchBooks = async (page: number) => {
    setIsLoading(true);
    setError('');
    
    try {
      const response = await bookService.getBooks({ page, size: 12 });
      setBooks(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
      setCurrentPage(page);
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      setError(error.response?.data?.message || 'Failed to load books');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchBooks(0);
  }, []);

  const handlePageChange = (page: number) => {
    fetchBooks(page);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <Layout>
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">My Books</h1>
          <p className="text-gray-600 mt-1">
            {totalElements} {totalElements === 1 ? 'book' : 'books'} in your library
          </p>
        </div>
        <Link
          to="/books/new"
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-indigo-700 flex items-center gap-2"
        >
          <span>+</span> Add Book
        </Link>
      </div>

      {/* Error */}
      {error && <Alert type="error" message={error} onClose={() => setError('')} />}

      {/* Loading */}
      {isLoading ? (
        <Loading />
      ) : books.length === 0 ? (
        /* Empty State */
        <EmptyState
          icon="📚"
          title="No books yet"
          description="Start building your library by adding your first book."
          actionLabel="Add Your First Book"
          actionLink="/books/new"
        />
      ) : (
        /* Book Grid */
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {books.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>

          {/* Pagination */}
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </>
      )}
    </Layout>
  );
}