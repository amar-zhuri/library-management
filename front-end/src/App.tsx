import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { ProtectedRoute } from './components/layout/ProtectedRoute'
import { LoginPage } from './pages/auth/LoginPage'
import { RegisterPage } from './pages/auth/RegisterPage'
import { ForgotPasswordPage } from './pages/auth/ForgotPasswordPage'
import { ResetPasswordPage } from './pages/auth/ResetPasswordPage'
import { VerifyEmailPage } from './pages/auth/VerifyEmailPage'
import { DashboardPage } from './pages/dashboard/DashboardPage'
import { BooksPage } from './pages/books/BooksPage'
import { AddBookPage } from './pages/books/AddBookPage'
import { BookDetailPage } from './pages/books/BookDetailPage'
import { EditBookPage } from './pages/books/EditBookPage'
import { SearchPage } from './pages/search/SearchPage'
import { SettingsPage } from './pages/settings/SettingsPage'
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage'
import { UserListPage } from './pages/admin/UserListPage'
import { UserDetailPage } from './pages/admin/UserDetailPage'
import { AdminBooksPage } from './pages/admin/AdminBooksPage'
import { NewsletterPage } from './pages/admin/NewsletterPage'
import { AdminSearchPage } from './pages/admin/AdminSearchPage'
import { AdminAddBookPage } from './pages/admin/AdminAddBookPage'
import { UnsubscribePage } from './pages/notifications/UnsubscribePage'
import { UnsubscribeNewsletterPage } from './pages/notifications/UnsubscribeNewsletterPage'
import { LibraryPage } from './pages/library/LibraryPage'

const App = () => (
  <BrowserRouter>
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />
      <Route path="/unsubscribe" element={<UnsubscribePage />} />
      <Route path="/unsubscribe/newsletter" element={<UnsubscribeNewsletterPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route index element={<DashboardPage />} />
          <Route path="/books" element={<BooksPage />} />
          <Route path="/books/new" element={<AddBookPage />} />
          <Route path="/books/:id" element={<BookDetailPage />} />
          <Route path="/books/:id/edit" element={<EditBookPage />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="/library" element={<LibraryPage />} />
          <Route path="/settings" element={<SettingsPage />} />

          <Route element={<ProtectedRoute adminOnly />}>
            <Route path="/admin" element={<AdminDashboardPage />} />
            <Route path="/admin/users" element={<UserListPage />} />
            <Route path="/admin/users/:id" element={<UserDetailPage />} />
            <Route path="/admin/books" element={<AdminBooksPage />} />
            <Route path="/admin/books/new" element={<AdminAddBookPage />} />
            <Route path="/admin/search" element={<AdminSearchPage />} />
            <Route path="/admin/newsletter" element={<NewsletterPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  </BrowserRouter>
)

export default App
