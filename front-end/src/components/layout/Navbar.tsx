import { Link, useNavigate } from 'react-router-dom'
import { Button } from '../common/Button'
import { useAuth } from '../../hooks/useAuth'

export const Navbar = () => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <header className="sticky top-0 z-40 flex items-center justify-between border-b border-muted-100 bg-white/80 px-6 py-4 backdrop-blur-md">
      <Link to="/" className="flex items-center gap-2 text-lg font-semibold text-primary-700">
        <span className="rounded-full bg-primary-100 px-3 py-1 text-sm font-bold text-primary-700">LM</span>
        <span>Library Hub</span>
      </Link>
      <div className="flex items-center gap-4 text-sm text-muted-700">
        {user && (
          <>
            <div className="text-right">
              <p className="font-semibold text-muted-900">{user.name}</p>
              <p className="text-xs uppercase tracking-wide text-primary-600">{user.role}</p>
            </div>
            <div className="h-10 w-10 rounded-full bg-primary-100 text-center text-base font-semibold leading-10 text-primary-700">
              {user.name.charAt(0).toUpperCase()}
            </div>
            <Button variant="ghost" size="sm" onClick={handleLogout}>
              Logout
            </Button>
          </>
        )}
      </div>
    </header>
  )
}
