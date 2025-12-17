import { Outlet } from 'react-router-dom'
import { Navbar } from './Navbar'
import { Sidebar } from './Sidebar'

export const Layout = () => (
  <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50 text-muted-900">
    <Navbar />
    <div className="mx-auto flex max-w-6xl gap-6 px-4 py-6">
      <Sidebar />
      <main className="flex-1 space-y-6 pb-12">
        <Outlet />
      </main>
    </div>
  </div>
)
