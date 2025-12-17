interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
  if (totalPages <= 1) return null;

  const pages: (number | string)[] = [];
  
  // Always show first page
  pages.push(0);
  
  // Show pages around current page
  for (let i = Math.max(1, currentPage - 1); i <= Math.min(totalPages - 2, currentPage + 1); i++) {
    if (pages[pages.length - 1] !== i - 1 && pages[pages.length - 1] !== '...') {
      pages.push('...');
    }
    pages.push(i);
  }
  
  // Always show last page
  if (totalPages > 1) {
    if (pages[pages.length - 1] !== totalPages - 2 && pages[pages.length - 1] !== '...') {
      pages.push('...');
    }
    pages.push(totalPages - 1);
  }

  return (
    <div className="flex items-center justify-center gap-2 mt-6">
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
        className="px-3 py-1 rounded border border-gray-300 text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
      >
        Previous
      </button>
      
      {pages.map((page, index) => (
        typeof page === 'number' ? (
          <button
            key={index}
            onClick={() => onPageChange(page)}
            className={`px-3 py-1 rounded text-sm ${
              page === currentPage
                ? 'bg-indigo-600 text-white'
                : 'border border-gray-300 hover:bg-gray-50'
            }`}
          >
            {page + 1}
          </button>
        ) : (
          <span key={index} className="px-2 text-gray-500">
            {page}
          </span>
        )
      ))}
      
      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage >= totalPages - 1}
        className="px-3 py-1 rounded border border-gray-300 text-sm disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
      >
        Next
      </button>
    </div>
  );
}