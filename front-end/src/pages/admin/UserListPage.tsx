import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { adminService } from '../../services/adminService'
import type { PagedResponse, UserResponse } from '../../types'
import { Pagination } from '../../components/common/Pagination'
import { Button } from '../../components/common/Button'
import { Modal } from '../../components/common/Modal'
import { EmptyState } from '../../components/common/EmptyState'

export const UserListPage = () => {
  const [data, setData] = useState<PagedResponse<UserResponse> | null>(null)
  const [params, setParams] = useState({ page: 0, size: 10, sortBy: 'createdAt', sortDir: 'desc' })
  const [loading, setLoading] = useState(true)
  const [deleteId, setDeleteId] = useState<number | null>(null)

  const load = async () => {
    try {
      setLoading(true)
      const res = await adminService.users(params)
      setData(res)
    } catch {
      toast.error('Unable to load users')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params])

  const handleDelete = async () => {
    if (!deleteId) return
    try {
      await adminService.deleteUser(deleteId)
      toast.success('User deleted')
      setDeleteId(null)
      load()
    } catch {
      toast.error('Could not delete user')
    }
  }

  return (
    <div className="space-y-4">
      <div>
        <p className="text-sm text-muted-600">Manage all users</p>
        <h1 className="text-2xl font-bold text-muted-900">Users</h1>
      </div>
      {loading && <p className="text-sm text-muted-600">Loading users...</p>}
      {!loading && data && data.content.length === 0 && <EmptyState title="No users" description="No users found." />}
      <div className="overflow-hidden rounded-xl border border-muted-100 bg-white shadow-sm">
        <table className="w-full border-collapse text-sm">
          <thead className="bg-muted-50 text-left text-muted-700">
            <tr>
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Role</th>
              <th className="px-4 py-3 text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {data?.content.map((user) => (
              <tr key={user.id} className="border-t border-muted-100">
                <td className="px-4 py-3 font-semibold text-muted-900">{user.name}</td>
                <td className="px-4 py-3 text-muted-700">{user.email}</td>
                <td className="px-4 py-3 text-xs font-semibold uppercase tracking-wide text-primary-700">{user.role}</td>
                <td className="px-4 py-3 text-right">
                  <div className="flex justify-end gap-2">
                    <Link to={`/admin/users/${user.id}`} className="text-primary-600 hover:text-primary-700">
                      View
                    </Link>
                    <Button variant="danger" size="sm" onClick={() => setDeleteId(user.id)}>
                      Delete
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {data && <Pagination page={data.page} totalPages={data.totalPages} onPageChange={(page) => setParams((prev) => ({ ...prev, page }))} />}

      <Modal
        title="Delete user"
        description="This will remove the user and their data."
        isOpen={Boolean(deleteId)}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDelete}
        confirmText="Delete user"
        confirmTone="danger"
      />
    </div>
  )
}
