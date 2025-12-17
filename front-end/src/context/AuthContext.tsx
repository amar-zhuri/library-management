import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import toast from 'react-hot-toast'
import { authService } from '../services/authService'
import type { AuthResponse, AuthUser } from '../types'
import { TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from '../utils/constants'
import { getErrorMessage } from '../utils/helpers'

interface AuthContextValue {
  user: AuthUser | null
  token: string | null
  loading: boolean
  isAuthenticated: boolean
  isAdmin: boolean
  login: (email: string, password: string) => Promise<void>
  register: (name: string, email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

const persistSession = (data: AuthResponse) => {
  localStorage.setItem(TOKEN_STORAGE_KEY, data.token)
  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(data.user))
}

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const cached = localStorage.getItem(USER_STORAGE_KEY)
    return cached ? (JSON.parse(cached) as AuthUser) : null
  })
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_STORAGE_KEY))
  const [loading, setLoading] = useState<boolean>(true)

  const handleAuthSuccess = useCallback((data: AuthResponse) => {
    persistSession(data)
    setUser(data.user)
    setToken(data.token)
  }, [])

  const logout = useCallback(async () => {
    try {
      await authService.logout()
    } catch {
      // ignore logout errors
    } finally {
      localStorage.removeItem(TOKEN_STORAGE_KEY)
      localStorage.removeItem(USER_STORAGE_KEY)
      setUser(null)
      setToken(null)
    }
  }, [])

  const refreshUser = useCallback(async () => {
    if (!token) {
      setLoading(false)
      return
    }
    try {
      const me = await authService.me()
      setUser(me)
    } catch (error) {
      const message = getErrorMessage(error)
      toast.error(message)
      await logout()
    } finally {
      setLoading(false)
    }
  }, [token, logout])

  useEffect(() => {
    refreshUser()
  }, [refreshUser])

  const login = useCallback(async (email: string, password: string) => {
    try {
      setLoading(true)
      const res = await authService.login({ email, password })
      handleAuthSuccess(res)
      toast.success(`Welcome back, ${res.user.name}`)
    } catch (error) {
      toast.error(getErrorMessage(error))
      throw error
    } finally {
      setLoading(false)
    }
  }, [handleAuthSuccess])

  const register = useCallback(async (name: string, email: string, password: string) => {
    try {
      setLoading(true)
      const res = await authService.register({ name, email, password })
      handleAuthSuccess(res)
      toast.success('Account created. Check your email to verify your account.')
    } catch (error) {
      toast.error(getErrorMessage(error))
      throw error
    } finally {
      setLoading(false)
    }
  }, [handleAuthSuccess])

  const value = useMemo(
    () => ({
      user,
      token,
      loading,
      isAuthenticated: Boolean(token),
      isAdmin: user?.role === 'ADMIN',
      login,
      register,
      logout,
      refreshUser,
    }),
    [user, token, loading, login, register, logout, refreshUser]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuthContext = () => {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuthContext must be used within AuthProvider')
  return ctx
}
