// ============ Enums ============

export type Genre =
  | 'FICTION'
  | 'NON_FICTION'
  | 'MYSTERY'
  | 'SCIENCE_FICTION'
  | 'FANTASY'
  | 'ROMANCE'
  | 'THRILLER'
  | 'BIOGRAPHY'
  | 'HISTORY'
  | 'SCIENCE'
  | 'SELF_HELP'
  | 'POETRY'
  | 'DRAMA'
  | 'HORROR'
  | 'ADVENTURE'
  | 'CHILDREN'
  | 'YOUNG_ADULT'
  | 'COMICS'
  | 'ART'
  | 'COOKING'
  | 'TRAVEL'
  | 'RELIGION'
  | 'PHILOSOPHY'
  | 'PSYCHOLOGY'
  | 'BUSINESS'
  | 'TECHNOLOGY'
  | 'OTHER';

export type ReadingStatus = 'TO_READ' | 'READING' | 'COMPLETED' | 'ON_HOLD' | 'DROPPED';

export type Role = 'USER' | 'ADMIN';

export type NewsletterStatus = 'DRAFT' | 'SCHEDULED' | 'SENDING' | 'SENT' | 'FAILED';

// ============ Auth Types ============

export interface User {
  id: number;
  email: string;
  name: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  type: string;
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface MessageResponse {
  message: string;
  success: boolean;
}

// ============ Book Types ============

export interface Book {
  id: number;
  title: string;
  author: string;
  genre: Genre;
  status: ReadingStatus;
  price?: number;
  description?: string;
  isbn?: string;
  pageCount?: number;
  publicationYear?: number;
  createdAt: string;
  updatedAt: string;
  owner?: {
    id: number;
    name: string;
    email: string;
  };
}

export interface BookRequest {
  title: string;
  author: string;
  genre: Genre;
  status?: ReadingStatus;
  price?: number;
  description?: string;
  isbn?: string;
  pageCount?: number;
  publicationYear?: number;
}

// ============ Pagination Types ============

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

// ============ Search Types ============

export interface BookSearchResponse {
  books: Book[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  query?: string;
  searchTimeMs: number;
  facets?: Record<string, number>;
  suggestions?: string[];
}

export interface SearchFilters {
  genres: Genre[];
  authors: string[];
  statuses: ReadingStatus[];
  minPrice?: number;
  maxPrice?: number;
  minYear?: number;
  maxYear?: number;
}

// ============ Recommendation Types ============

export interface RecommendedBook {
  id: number;
  title: string;
  author: string;
  genre: Genre;
  pageCount?: number;
  publicationYear?: number;
  reason: string;
}

export interface RecommendationResponse {
  byGenre: RecommendedBook[];
  byAuthor: RecommendedBook[];
  fromSimilarUsers: RecommendedBook[];
  message: string;
}

// ============ Insights Types ============

export interface UserInsightsResponse {
  insights: string[];
  summary: string;
  generatedBy: 'RULE_BASED' | 'AI';
  generationTimeMs: number;
}

// ============ AI Query Types ============

export interface AIQueryRequest {
  question: string;
  useLLM?: boolean;
}

export interface AIQueryResponse {
  question: string;
  answer: string;
  queryType: string;
  data?: unknown;
  recognizedQuery: boolean;
  processingMethod: 'RULE_BASED' | 'LLM';
  confidence: number;
  executionTimeMs: number;
  suggestions?: string[];
}

// ============ Admin Types ============

export interface UserResponse {
  id: number;
  email: string;
  name: string;
  role: Role;
  bookCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface UserDetailResponse {
  id: number;
  email: string;
  name: string;
  role: Role;
  createdAt: string;
  updatedAt: string;
  books: Book[];
}

export interface StatsResponse {
  totalUsers: number;
  totalBooks: number;
  booksByGenre: Record<string, number>;
  booksByStatus: Record<string, number>;
  topReaders: { userId: number; userName: string; bookCount: number }[];
  popularBooks: { title: string; author: string; ownerCount: number }[];
  topAuthors: { author: string; bookCount: number }[];
}

// ============ Notification Types ============

export interface NotificationPreferences {
  newsletterEnabled: boolean;
  newBooksEnabled: boolean;
  weeklyDigestEnabled: boolean;
  readingRemindersEnabled: boolean;
  achievementNotificationsEnabled: boolean;
}

export interface Newsletter {
  id: number;
  subject: string;
  content: string;
  status: NewsletterStatus;
  createdByName?: string;
  sentAt?: string;
  recipientCount?: number;
  createdAt: string;
  updatedAt: string;
}

// ============ Error Types ============

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: Record<string, string>;
}