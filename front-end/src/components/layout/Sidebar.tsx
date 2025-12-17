import { NavLink } from 'react-router-dom'
import clsx from 'clsx'
import { useAuth } from '../../hooks/useAuth'

const navItems = [
  { to: '/', label: 'Dashboard', adminOnly: false },
  { to: '/books', label: 'My Books', adminOnly: false },
  { to: '/books/new', label: 'Add Book', adminOnly: false },
  { to: '/search', label: 'Search', adminOnly: false },
  { to: '/library', label: 'Library', adminOnly: false },
  { to: '/settings', label: 'Settings', adminOnly: false },
]

const adminItems = [
  { to: '/admin', label: 'Admin Dashboard' },
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/books', label: 'All Books' },
  { to: '/admin/books/new', label: 'Add System Book' },
  { to: '/admin/search', label: 'Search All' },
  { to: '/admin/newsletter', label: 'Newsletter' },
]

export const Sidebar = () => {
  const { isAdmin } = useAuth()

  return (
    <aside className="sticky top-[76px] flex h-[calc(100vh-76px)] flex-col gap-4 border-r border-muted-100 bg-white/70 px-4 py-6 backdrop-blur-sm">
      <nav className="space-y-1">
        {[...navItems, ...(isAdmin ? adminItems : [])].map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              clsx(
                'flex items-center gap-2 rounded-xl px-3 py-2 text-sm font-semibold transition',
                isActive ? 'bg-primary-50 text-primary-700 shadow-sm' : 'text-muted-700 hover:bg-muted-50'
              )
            }
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
      <div className="rounded-xl bg-gradient-to-br from-primary-50 via-white to-indigo-100 p-4 text-xs text-muted-700 shadow-inner">
        <p className="font-semibold text-primary-800">Test Accounts</p>
        <p>Admin: admin@library.com / admin123</p>
        <p>User: alice@example.com / password123</p>
      </div>
    </aside>
  )
}
