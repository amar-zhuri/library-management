import { useAuth } from '../../context/AuthContext';
import { Layout } from '../../components/layout';

export function DashboardPage() {
  const { user } = useAuth();

  return (
    <Layout>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">
          Welcome back, {user?.name}! 👋
        </h1>
        <p className="text-gray-600 mt-2">
          Here's what's happening in your library.
        </p>
      </div>

      {/* Placeholder for stats - will be implemented in Phase 4 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
          <h3 className="text-sm font-medium text-gray-500">Total Books</h3>
          <p className="text-3xl font-bold text-gray-900 mt-1">--</p>
        </div>
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
          <h3 className="text-sm font-medium text-gray-500">Reading</h3>
          <p className="text-3xl font-bold text-gray-900 mt-1">--</p>
        </div>
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
          <h3 className="text-sm font-medium text-gray-500">Completed</h3>
          <p className="text-3xl font-bold text-gray-900 mt-1">--</p>
        </div>
      </div>

      {/* Placeholder message */}
      <div className="bg-indigo-50 p-6 rounded-xl border border-indigo-100">
        <p className="text-indigo-800">
          🚧 Dashboard features coming soon! We'll add recommendations, insights, and more.
        </p>
      </div>
    </Layout>
  );
}