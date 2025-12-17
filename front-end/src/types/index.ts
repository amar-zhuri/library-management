export type Role = 'USER' | 'ADMIN'

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
  | 'OTHER'

export type ReadingStatus = 'TO_READ' | 'READING' | 'COMPLETED' | 'ON_HOLD' | 'DROPPED'

export interface AuthUser {
  id: number
  email: string
  name: string
  role: Role
}

export interface AuthResponse {
  token: string
  type: string
  expiresIn: number
  user: AuthUser
}

export interface MessageResponse {
  message: string
  success?: boolean
}

export interface BookRequest {
  title: string
  author: string
  genre: Genre
  status?: ReadingStatus
  price?: number
  description?: string
  isbn?: string
  pageCount?: number
  publicationYear?: number
}

export interface OwnerInfo {
  id: number
  name: string
  email: string
}

export interface BookResponse extends BookRequest {
  id: number
  createdAt: string
  updatedAt: string
  owner?: OwnerInfo
}

export interface PagedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface BookSearchResponse {
  books: BookResponse[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  query?: string
  searchTimeMs?: number
  facets?: Record<string, unknown>
  suggestions?: string[]
}

export interface SearchFiltersResponse {
  genres: Genre[]
  authors: string[]
  statuses: ReadingStatus[]
  minPrice: number
  maxPrice: number
  minYear: number
  maxYear: number
}

export interface RecommendationResponse {
  byGenre: RecommendedBook[]
  byAuthor: RecommendedBook[]
  fromSimilarUsers: RecommendedBook[]
  message?: string
}

export interface RecommendedBook {
  id: number
  title: string
  author: string
  genre: Genre
  pageCount?: number
  publicationYear?: number
  reason?: string
}

export interface UserInsightsResponse {
  insights: string[]
  summary?: string
  generatedBy: string
  generationTimeMs?: number
}

export interface InsightResponse {
  insights: string[]
  generatedBy: string
  generatedAtMs: number
}

export interface QuickStatsResponse {
  total: number
  toRead: number
  reading: number
  completed: number
}

export interface AIQueryResponse {
  question: string
  answer: string
  queryType: string
  data?: Record<string, unknown>
  recognizedQuery: boolean
  processingMethod: string
  confidence?: number
  executionTimeMs?: number
  suggestions?: string[]
}

export interface StatsResponse {
  totalUsers: number
  totalBooks: number
  booksByGenre: Record<Genre, number>
  booksByStatus: Record<ReadingStatus, number>
  topReaders: TopReaderDto[]
  popularBooks: PopularBookDto[]
  topAuthors: TopAuthorDto[]
}

export interface TopReaderDto {
  userId: number
  name?: string
  userName?: string
  bookCount: number
}

export interface PopularBookDto {
  id?: number
  title: string
  author: string
  readCount?: number
  count?: number
}

export interface TopAuthorDto {
  author: string
  bookCount: number
}

export interface UserResponse {
  id: number
  email: string
  name: string
  role: Role
  bookCount?: number
  createdAt: string
  updatedAt: string
}

export interface UserDetailResponse extends UserResponse {
  books: BookResponse[]
}

export interface NotificationPreferencesResponse {
  newsletterEnabled: boolean
  newBooksEnabled: boolean
  weeklyDigestEnabled: boolean
  readingRemindersEnabled: boolean
  achievementNotificationsEnabled: boolean
}

export interface NewsletterResponse {
  id: number
  subject: string
  content: string
  status: NewsletterStatus
  createdByName?: string
  sentAt?: string
  recipientCount?: number
  createdAt: string
  updatedAt: string
}

export type NewsletterStatus = 'DRAFT' | 'SCHEDULED' | 'SENDING' | 'SENT' | 'FAILED'

export interface NewsletterStats {
  totalNewsletters?: number
  drafts?: number
  sent?: number
  failed?: number
}

export interface NewsletterRequest {
  subject: string
  content: string
}

export interface RateLimitErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  retryAfterSeconds?: number
  path?: string
}
