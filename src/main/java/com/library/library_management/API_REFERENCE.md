# API Reference

## 1. Authentication Endpoints

### Register
- **Method/URL:** `POST /api/auth/register`
- **Request Body:** `RegisterRequest`
  - `name` (string, 2-100 chars, required)
  - `email` (string, email, required)
  - `password` (string, 6-100 chars, required)
- **Response Body:** `AuthResponse`
  - `token` (string, JWT)
  - `type` (string, e.g., `Bearer`)
  - `expiresIn` (number, milliseconds)
  - `user` (object)
    - `id` (number)
    - `email` (string)
    - `name` (string)
    - `role` (enum `Role`)
- **Auth Required?** No
- **Example**
```json
POST /api/auth/register
{
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "password": "secret123"
}

Response 201
{
  "token": "<jwt>",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "email": "ada@example.com",
    "name": "Ada Lovelace",
    "role": "USER"
  }
}
```

### Login
- **Method/URL:** `POST /api/auth/login`
- **Request Body:** `LoginRequest`
  - `email` (string, email, required)
  - `password` (string, required)
- **Response Body:** `AuthResponse` (same shape as Register response)
- **Auth Required?** No
- **Example**
```json
POST /api/auth/login
{
  "email": "ada@example.com",
  "password": "secret123"
}
```

### Get Current User
- **Method/URL:** `GET /api/auth/me`
- **Request Body:** None
- **Response Body:** `AuthResponse.UserInfo`
  - `id` (number)
  - `email` (string)
  - `name` (string)
  - `role` (enum `Role`)
- **Auth Required?** Yes (Bearer JWT)
- **Example**
```json
GET /api/auth/me
Authorization: Bearer <jwt>

Response 200
{
  "id": 1,
  "email": "ada@example.com",
  "name": "Ada Lovelace",
  "role": "USER"
}
```

### Verify Email
- **Method/URL:** `GET /api/auth/verify?token={token}`
- **Request Body:** None
- **Response Body:** `MessageResponse`
  - `message` (string)
  - `success` (boolean)
- **Auth Required?** No
- **Example**
```json
GET /api/auth/verify?token=abc123

Response 200
{
  "message": "Email verified successfully. You can now login.",
  "success": true
}
```

### Resend Verification Email
- **Method/URL:** `POST /api/auth/resend-verification`
- **Request Body:** `ResendVerificationRequest`
  - `email` (string, email, required)
- **Response Body:** `MessageResponse`
- **Auth Required?** No
- **Example**
```json
POST /api/auth/resend-verification
{
  "email": "ada@example.com"
}
```

### Forgot Password
- **Method/URL:** `POST /api/auth/forgot-password`
- **Request Body:** `ForgotPasswordRequest`
  - `email` (string, email, required)
- **Response Body:** `MessageResponse`
- **Auth Required?** No
- **Example**
```json
POST /api/auth/forgot-password
{
  "email": "ada@example.com"
}
```

### Reset Password
- **Method/URL:** `POST /api/auth/reset-password`
- **Request Body:** `ResetPasswordRequest`
  - `token` (string, required)
  - `newPassword` (string, 6-100 chars, required)
- **Response Body:** `MessageResponse`
- **Auth Required?** No
- **Example**
```json
POST /api/auth/reset-password
{
  "token": "reset-token",
  "newPassword": "newSecret123"
}
```

### Logout
- **Method/URL:** `POST /api/auth/logout`
- **Request Body:** None (use `Authorization` header)
- **Headers:** `Authorization: Bearer <jwt>`
- **Response Body:** `MessageResponse`
  - `message` (string)
  - `success` (boolean)
- **Auth Required?** Yes (Bearer JWT)
- **Notes:** Invalidates the presented JWT by adding it to a blacklist until it naturally expires.
- **Example**
```json
POST /api/auth/logout
Authorization: Bearer <jwt>

Response 200
{
  "message": "Logged out successfully",
  "success": true
}
```

## 2. Book Endpoints

### Create Book
- **Method/URL:** `POST /api/books`
- **Request Body:** `BookRequest`
  - `title` (string, required, max 255)
  - `author` (string, required, max 255)
  - `genre` (enum `Genre`, required)
  - `status` (enum `ReadingStatus`, optional)
  - `price` (number, optional, positive, 2 decimal max)
  - `description` (string, optional, max 2000)
  - `isbn` (string, optional, max 20)
  - `pageCount` (integer, >=1, optional)
  - `publicationYear` (integer, 1000-2100, optional)
- **Response Body:** `BookResponse`
- **Auth Required?** Yes
- **Example**
```json
POST /api/books
Authorization: Bearer <jwt>
{
  "title": "The Hobbit",
  "author": "J.R.R. Tolkien",
  "genre": "FANTASY",
  "status": "TO_READ",
  "price": 12.99,
  "description": "A classic adventure",
  "isbn": "1234567890",
  "pageCount": 310,
  "publicationYear": 1937
}
```

### List Books
- **Method/URL:** `GET /api/books`
- **Query Params:**
  - `genre` (Genre, optional)
  - `status` (ReadingStatus, optional)
  - `search` (string, optional)
  - `page` (int, default 0)
  - `size` (int, default 10)
  - `sortBy` (string, default `createdAt`)
  - `sortDir` (string, default `desc`)
- **Response Body:** `PagedResponse<BookResponse>`
- **Auth Required?** Yes
- **Example**
```json
GET /api/books?genre=FANTASY&status=READING&page=0&size=5
Authorization: Bearer <jwt>

Response 200
{
  "content": [ { "id": 1, "title": "The Hobbit", "author": "J.R.R. Tolkien", "genre": "FANTASY", "status": "READING", "createdAt": "2024-01-01T10:00:00" } ],
  "page": 0,
  "size": 5,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Get Book By ID
- **Method/URL:** `GET /api/books/{id}`
- **Response Body:** `BookResponse`
- **Auth Required?** Yes

### Update Book
- **Method/URL:** `PUT /api/books/{id}`
- **Request Body:** `BookRequest`
- **Response Body:** `BookResponse`
- **Auth Required?** Yes

### Delete Book
- **Method/URL:** `DELETE /api/books/{id}`
- **Response Body:** None (204)
- **Auth Required?** Yes

### Book Statistics
- **Method/URL:** `GET /api/books/stats`
- **Response Body:** Map with counts
  - `total`, `toRead`, `reading`, `completed` (numbers)
- **Auth Required?** Yes
- **Example**
```json
{
  "total": 12,
  "toRead": 5,
  "reading": 3,
  "completed": 4
}
```

### Quick Search
- **Method/URL:** `GET /api/books/quick-search`
- **Query Params:** `q` (string, required), `page` (int, default 0), `size` (int, default 10)
- **Response Body:** `PagedResponse<BookResponse>`
- **Auth Required?** Yes

## 3. Admin Endpoints
_All routes require `ROLE_ADMIN`._

### Get All Users
- **Method/URL:** `GET /api/admin/users`
- **Query Params:** `page` (default 0), `size` (default 10), `sortBy` (default `createdAt`), `sortDir` (default `desc`)
- **Response Body:** `PagedResponse<UserResponse>`

### Get User Details
- **Method/URL:** `GET /api/admin/users/{id}`
- **Response Body:** `UserDetailResponse` (includes list of `BookResponse`)

### Delete User
- **Method/URL:** `DELETE /api/admin/users/{id}`
- **Response Body:** None (204)

### Get All Books (System)
- **Method/URL:** `GET /api/admin/books`
- **Query Params:** pagination/sort same as users
- **Response Body:** `PagedResponse<BookResponse>` (includes `owner` info)

### Delete Any Book
- **Method/URL:** `DELETE /api/admin/books/{id}`
- **Response Body:** None (204)

### Add Book (System-wide)
- **Method/URL:** `POST /api/admin/books`
- **Request Body:** `BookRequest`
- **Response Body:** `BookResponse`

### System Statistics
- **Method/URL:** `GET /api/admin/stats`
- **Response Body:** `StatsResponse`
  - Counts, book/genre/status maps, `topReaders`, `popularBooks`, `topAuthors`

## 4. Search Endpoints

### Search User Books
- **Method/URL:** `GET /api/books/search`
- **Query Params:**
  - `q`, `title`, `author`, `isbn` (strings, optional)
  - `genre` (Genre), `status` (ReadingStatus)
  - `minPrice`, `maxPrice` (decimal)
  - `minYear`, `maxYear` (int)
  - `minPages`, `maxPages` (int)
  - `page` (int, default 0), `size` (int, default 10)
  - `sortBy` (string, default `relevance`), `sortDir` (string, default `desc`)
- **Response Body:** `BookSearchResponse` (books list, pagination fields, `query`, `searchTimeMs`, optional `facets` and `suggestions`)
- **Auth Required?** Yes

### Search Suggestions
- **Method/URL:** `GET /api/books/search/suggestions`
- **Query Params:** `q` (string, required)
- **Response Body:** `SearchService.SearchSuggestions`
  - `titles` (list of strings)
  - `authors` (list of strings)
- **Auth Required?** Yes

### Available Filters
- **Method/URL:** `GET /api/books/search/filters`
- **Response Body:** `SearchService.SearchFilters`
  - `genres` (list of `Genre`)
  - `authors` (list of strings)
  - `statuses` (list of `ReadingStatus`)
  - `minPrice`, `maxPrice` (decimal)
  - `minYear`, `maxYear` (int)
- **Auth Required?** Yes

### Search All Books (Admin)
- **Method/URL:** `GET /api/books/search/all`
- **Query Params:** same as user search but without page filters for pages/years optional; pagination defaults page 0 size 10 sortBy `createdAt` sortDir `desc`
- **Response Body:** `BookSearchResponse`
- **Auth Required?** Admin only

## 5. Recommendation Endpoints

### User Recommendations
- **Method/URL:** `GET /api/recommendations`
- **Response Body:** `RecommendationResponse` (rule-based)
- **Auth Required?** Yes

### AI Recommendations
- **Method/URL:** `GET /api/recommendations/ai`
- **Response Body:** `RecommendationResponse` (AI-powered)
- **Auth Required?** Yes

### By Genre
- **Method/URL:** `GET /api/recommendations/by-genre`
- **Query Params:** `limit` (int, default 5)
- **Response Body:** List of `RecommendedBook` (rule-based)
- **Auth Required?** Yes

### By Author
- **Method/URL:** `GET /api/recommendations/by-author`
- **Query Params:** `limit` (int, default 5)
- **Response Body:** List of `RecommendedBook` (rule-based)
- **Auth Required?** Yes

### Discover (Similar Users)
- **Method/URL:** `GET /api/recommendations/discover`
- **Query Params:** `limit` (int, default 5)
- **Response Body:** List of `RecommendedBook` (rule-based)
- **Auth Required?** Yes

## 6. AI Query Endpoints

### Process Query
- **Method/URL:** `POST /api/ai/query`
- **Request Body:** `AIQueryRequest`
  - `question` (string, 3-500 chars, required)
  - `useLLM` (boolean, optional, default false)
- **Response Body:** `AIQueryResponse`
  - `question`, `answer`, `queryType` (enum `QueryType`), `data` (object), `recognizedQuery` (boolean), `processingMethod` (`RULE_BASED` or `LLM`), `confidence` (number), `executionTimeMs` (number), `suggestions` (list)
- **Auth Required?** Yes

### Query Suggestions
- **Method/URL:** `GET /api/ai/suggestions`
- **Response Body:** Map with `suggestions` (list of strings) and `categories` (list of strings)
- **Auth Required?** No

### System Insights (Admin)
- **Method/URL:** `GET /api/ai/insights`
- **Response Body:** `InsightResponse`
  - `insights` (list of strings), `generatedBy` (`RULE_BASED` or `LLM`), `generatedAtMs` (number)
- **Auth Required?** Admin only

### Quick Stats
- **Method/URL:** `GET /api/ai/quick-stats`
- **Response Body:** Map combining common query results (`bookCount`, `favoriteGenres`, `readingProgress`)
- **Auth Required?** Yes

### User Insights (Rule-Based)
- **Method/URL:** `GET /api/ai/my-insights`
- **Response Body:** `UserInsightsResponse`
  - `insights` (list of strings), `summary` (string), `generatedBy` ("RULE_BASED"), `generationTimeMs` (number)
- **Auth Required?** Yes

### User Insights (AI)
- **Method/URL:** `GET /api/ai/my-insights/ai`
- **Response Body:** `UserInsightsResponse` (generatedBy "AI")
- **Auth Required?** Yes

## 7. Notification & Newsletter Endpoints

### Get Notification Preferences
- **Method/URL:** `GET /api/notifications/preferences`
- **Response Body:** `NotificationPreferencesResponse`
- **Auth Required?** Yes

### Update Notification Preferences
- **Method/URL:** `PUT /api/notifications/preferences`
- **Request Body:** `NotificationPreferencesRequest`
  - `newsletterEnabled`, `newBooksEnabled`, `weeklyDigestEnabled`, `readingRemindersEnabled`, `achievementNotificationsEnabled` (booleans, optional)
- **Response Body:** `NotificationPreferencesResponse`
- **Auth Required?** Yes

### Unsubscribe All
- **Method/URL:** `GET /api/notifications/unsubscribe?token={token}`
- **Response Body:** `MessageResponse`
- **Auth Required?** No (public link)

### Unsubscribe Newsletter
- **Method/URL:** `GET /api/notifications/unsubscribe/newsletter?token={token}`
- **Response Body:** `MessageResponse`
- **Auth Required?** No

### Newsletter (Admin)
All routes require `ROLE_ADMIN` under `/api/admin/newsletter`.
- `POST /api/admin/newsletter` — create draft (`NewsletterRequest` → `NewsletterResponse`)
- `GET /api/admin/newsletter` — list newsletters (query: `page` default 0, `size` default 10) → `PagedResponse<NewsletterResponse>`
- `GET /api/admin/newsletter/{id}` — get by id → `NewsletterResponse`
- `PUT /api/admin/newsletter/{id}` — update draft (`NewsletterRequest`)
- `DELETE /api/admin/newsletter/{id}` — delete draft (204)
- `POST /api/admin/newsletter/{id}/send` — send newsletter → `NewsletterResponse`
- `GET /api/admin/newsletter/stats` — newsletter stats → `NewsletterService.NewsletterStats` (fields per service implementation)

## 8. DTOs

### Request DTOs
- **AIQueryRequest**: `question` (string, @NotBlank, @Size 3-500), `useLLM` (Boolean, default false).
- **BookRequest**: `title` (string, @NotBlank, @Size ≤255), `author` (string, @NotBlank, @Size ≤255), `genre` (Genre, @NotNull), `status` (ReadingStatus), `price` (BigDecimal, @DecimalMin 0.0, @Digits 8.2), `description` (string, @Size ≤2000), `isbn` (string, @Size ≤20), `pageCount` (Integer, @Min 1), `publicationYear` (Integer, @Min 1000, @Max 2100).
- **BookSearchRequest**: `query`, `title`, `author`, `isbn` (strings); `genre` (Genre); `status` (ReadingStatus); `minPrice`, `maxPrice` (BigDecimal); `minYear`, `maxYear`, `minPages`, `maxPages` (Integer); pagination defaults `page=0`, `size=10`; sorting defaults `sortBy="relevance"`, `sortDir="desc"`.
- **ForgotPasswordRequest**: `email` (string, @NotBlank, @Email).
- **LoginRequest**: `email` (string, @NotBlank, @Email), `password` (string, @NotBlank).
- **NewsletterRequest**: `subject` (string, @NotBlank, @Size ≤255), `content` (string, @NotBlank).
- **NotificationPreferencesRequest**: `newsletterEnabled`, `newBooksEnabled`, `weeklyDigestEnabled`, `readingRemindersEnabled`, `achievementNotificationsEnabled` (Boolean).
- **RegisterRequest**: `name` (string, @NotBlank, @Size 2-100), `email` (string, @NotBlank, @Email), `password` (string, @NotBlank, @Size 6-100).
- **ResendVerificationRequest**: `email` (string, @NotBlank, @Email).
- **ResetPasswordRequest**: `token` (string, @NotBlank), `newPassword` (string, @NotBlank, @Size 6-100).

### Response DTOs
- **AIQueryResponse**: `question`, `answer`, `queryType` (QueryType), `data` (object), `recognizedQuery` (boolean), `processingMethod` (string), `confidence` (Double), `executionTimeMs` (long), `suggestions` (list of strings).
- **AuthResponse**: `token` (string), `type` (string), `expiresIn` (long), `user` (`UserInfo`: `id`, `email`, `name`, `role`).
- **BookResponse**: `id`, `title`, `author`, `genre` (Genre), `status` (ReadingStatus), `price` (BigDecimal), `description`, `isbn`, `pageCount`, `publicationYear`, `createdAt`, `updatedAt`, optional `owner` (`OwnerInfo`: `id`, `name`, `email`).
- **BookSearchResponse**: `books` (list of `BookResponse`), `page`, `size`, `totalElements`, `totalPages`, `first`, `last`, `query`, `searchTimeMs`, `facets` (map), `suggestions` (list of strings).
- **ErrorResponse**: `timestamp`, `status`, `error`, `message`, `path`, `validationErrors` (map of field → message).
- **InsightResponse**: `insights` (list of strings), `generatedBy` (string), `generatedAtMs` (long).
- **MessageResponse**: `message` (string), `success` (boolean).
- **NewsletterResponse**: `id`, `subject`, `content`, `status` (NewsletterStatus), `createdByName`, `sentAt`, `recipientCount`, `createdAt`, `updatedAt`.
- **NotificationPreferencesResponse**: `newsletterEnabled`, `newBooksEnabled`, `weeklyDigestEnabled`, `readingRemindersEnabled`, `achievementNotificationsEnabled`.
- **PagedResponse<T>**: `content` (list), `page`, `size`, `totalElements`, `totalPages`, `first`, `last`.
- **RateLimitErrorResponse**: `timestamp`, `status`, `error`, `message`, `retryAfterSeconds`, `path`.
- **RecommendationResponse**: `byGenre`, `byAuthor`, `fromSimilarUsers` (lists of `RecommendedBook`), `message`; `RecommendedBook`: `id`, `title`, `author`, `genre`, `pageCount`, `publicationYear`, `reason`.
- **StatsResponse**: `totalUsers`, `totalBooks`, `booksByGenre` (map), `booksByStatus` (map), `topReaders` (list of `TopReaderDto`), `popularBooks` (list of `PopularBookDto`), `topAuthors` (list of `TopAuthorDto`).
- **UserDetailResponse**: `id`, `email`, `name`, `role`, `createdAt`, `updatedAt`, `books` (list of `BookResponse`).
- **UserInsightsResponse**: `insights` (list of strings), `summary`, `generatedBy`, `generationTimeMs`.
- **UserResponse**: `id`, `email`, `name`, `role`, `bookCount`, `createdAt`, `updatedAt`.

## 9. Enums
- **Genre:** FICTION, NON_FICTION, MYSTERY, SCIENCE_FICTION, FANTASY, ROMANCE, THRILLER, BIOGRAPHY, HISTORY, SCIENCE, SELF_HELP, POETRY, DRAMA, HORROR, ADVENTURE, CHILDREN, YOUNG_ADULT, COMICS, ART, COOKING, TRAVEL, RELIGION, PHILOSOPHY, PSYCHOLOGY, BUSINESS, TECHNOLOGY, OTHER.
- **ReadingStatus:** TO_READ, READING, COMPLETED, ON_HOLD, DROPPED.
- **Role:** USER, ADMIN.
- **TokenType:** EMAIL_VERIFICATION, PASSWORD_RESET.
- **NewsletterStatus:** DRAFT, SCHEDULED, SENDING, SENT, FAILED.
- **QueryType:** USER_BOOK_COUNT, USER_BOOKS_BY_STATUS, USER_BOOKS_BY_GENRE, USER_GENRE_DISTRIBUTION, USER_READING_STATS, USER_LIBRARY_VALUE, USER_RECENT_BOOKS, USER_BOOKS_BY_AUTHOR, TOP_READERS, POPULAR_BOOKS, EXPENSIVE_BOOKS, TOP_AUTHORS, GENRE_DISTRIBUTION, STATUS_DISTRIBUTION, TOTAL_BOOKS, TOTAL_USERS, RECOMMENDATIONS_BY_GENRE, RECOMMENDATIONS_BY_AUTHOR, UNKNOWN.

## 10. Authentication Info
- **Header/Format:** `Authorization: Bearer <jwt>`.
- **Token Expiry:** `jwt.expiration` = 86,400,000 ms (24 hours).
- **Logout/Blacklist:** `POST /api/auth/logout` blacklists the presented JWT until its expiry; `JwtAuthenticationFilter` rejects blacklisted tokens and blacklist entries are cleaned on a schedule.
- **Public Endpoints:** `/api/auth/**`, `/api/health`, `/api/notifications/unsubscribe/**`, Swagger docs.
- **Admin-Only Endpoints:** any under `/api/admin/**`, `/api/ai/insights`, `/api/books/search/all` (via `@PreAuthorize`), newsletter routes.

## 11. Error Response Format
- **Structure:** `ErrorResponse` with `timestamp`, `status`, `error`, `message`, `path`, optional `validationErrors` map (for field errors). Rate-limit errors use `RateLimitErrorResponse` with `retryAfterSeconds`.
- **Common Status Codes:** 400 (Bad Request/validation), 403 (Forbidden/unauthorized access), 404 (Not Found), 500 (Internal Server Error), 429 (Too Many Requests when rate limit triggered).

## 12. Pagination Format
- **Standard Response:** `PagedResponse<T>` or paged fields in `BookSearchResponse` and admin search endpoints (`page`, `size`, `totalElements`, `totalPages`, `first`, `last`, `content` list).
- **Default Page Size:** 10 (controller defaults).
- **Query Params:** `page` (0-based index), `size`, plus optional `sortBy` and `sortDir` where supported.