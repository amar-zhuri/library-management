import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Input, Button, Alert } from '../../components/common';
import { authService } from '../../services/authService';
import { useAuth } from '../../context/AuthContext';

export function RegisterPage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();
  const { logout, clearSession } = useAuth();

  // Ensure any existing session is cleared when landing on register
  useEffect(() => {
    clearSession();
    logout().catch(() => {
      // ignore logout errors
    });
  }, [clearSession, logout]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Validation
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    setIsLoading(true);

    try {
      await authService.register({ name, email, password });
      setSuccess('Registration successful! Check your email to verify your account.');

      // Do NOT auto-login; clear any residual auth and send user to login
      clearSession();
      await logout().catch(() => {});
      navigate('/login', { replace: true });
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      setError(error.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">📚 Library</h1>
          <h2 className="mt-2 text-xl text-gray-600">Create your account</h2>
        </div>

        <div className="bg-white p-8 rounded-xl shadow-sm border border-gray-200">
          {error && <Alert type="error" message={error} onClose={() => setError('')} />}
          {success && (
            <Alert type="success" message={success} />
          )}

          {!success && (
            <form onSubmit={handleSubmit}>
              <Input
                id="name"
                label="Full Name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="John Doe"
                required
                minLength={2}
                maxLength={100}
              />

              <Input
                id="email"
                label="Email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
              />

              <Input
                id="password"
                label="Password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
                minLength={6}
              />

              <Input
                id="confirmPassword"
                label="Confirm Password"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="••••••••"
                required
              />

              <Button type="submit" isLoading={isLoading} className="w-full mt-2">
                Create Account
              </Button>
            </form>
          )}

          {success && (
            <div className="text-center">
              <p className="text-gray-600 mb-4">
                Redirecting to login page...
              </p>
            </div>
          )}

          <p className="mt-6 text-center text-sm text-gray-600">
            Already have an account?{' '}
            <Link to="/login" className="text-indigo-600 hover:text-indigo-500 font-medium">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
