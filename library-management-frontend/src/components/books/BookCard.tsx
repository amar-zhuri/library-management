import { Link } from 'react-router-dom';
import type { Book } from '../../types';
import { getGenreLabel, getStatusLabel, getStatusColor } from '../../utils/constants';

interface BookCardProps {
  book: Book;
}

export function BookCard({ book }: BookCardProps) {
  return (
    <Link
      to={`/books/${book.id}`}
      className="block bg-white rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-shadow overflow-hidden"
    >
      <div className="p-5">
        {/* Status Badge */}
        <div className="flex items-center justify-between mb-3">
          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(book.status)}`}>
            {getStatusLabel(book.status)}
          </span>
          {book.price && (
            <span className="text-sm font-medium text-gray-600">
              ${book.price.toFixed(2)}
            </span>
          )}
        </div>

        {/* Title & Author */}
        <h3 className="text-lg font-semibold text-gray-900 mb-1 line-clamp-1">
          {book.title}
        </h3>
        <p className="text-sm text-gray-600 mb-3">by {book.author}</p>

        {/* Genre */}
        <span className="inline-block px-2 py-1 bg-indigo-50 text-indigo-700 text-xs rounded-md">
          {getGenreLabel(book.genre)}
        </span>

        {/* Details */}
        <div className="mt-4 pt-4 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500">
          {book.pageCount && (
            <span>{book.pageCount} pages</span>
          )}
          {book.publicationYear && (
            <span>{book.publicationYear}</span>
          )}
        </div>
      </div>
    </Link>
  );
}