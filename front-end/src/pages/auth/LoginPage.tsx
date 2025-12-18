import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { Button } from '../../components/common/Button'
import { Input } from '../../components/common/Input'
import { Card } from '../../components/common/Card'
import { useAuth } from '../../hooks/useAuth'

interface LoginFormValues {
  email: string
  password: string
}

export const LoginPage = () => {
  const { login, loading } = useAuth()
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<LoginFormValues>()

  const onSubmit = async (values: LoginFormValues) => {
    await login(values.email, values.password)
    navigate('/')
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-cyan-50 px-4">
      <Card className="w-full max-w-md">
        <div className="mb-4 text-center">
          <p className="text-sm font-semibold uppercase tracking-wide text-primary-600">Library Management</p>
          <h1 className="text-2xl font-bold text-muted-900">Welcome back</h1>
          <p className="text-sm text-muted-600">Sign in to your account to manage your books.</p>
        </div>
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <Input
            label="Email"
            type="email"
            placeholder="you@example.com"
            {...register('email', { required: 'Email is required' })}
            error={errors.email?.message}
          />
          <Input
            label="Password"
            type="password"
            placeholder="••••••••"
            {...register('password', { required: 'Password is required' })}
            error={errors.password?.message}
          />
          <Button type="submit" className="w-full" loading={loading}>
            Login
          </Button>
        </form>
        <div className="mt-4 space-y-2 rounded-lg border border-primary-100 bg-primary-50 px-3 py-2 text-sm text-primary-900">
          <p className="font-semibold">Test Accounts</p>
          <div className="grid gap-2 sm:grid-cols-2">
            <button
              type="button"
              className="rounded-md bg-white px-3 py-2 text-left shadow-sm ring-1 ring-primary-100 hover:bg-primary-100"
              onClick={() => {
                setValue('email', 'admin@library.com')
                setValue('password', 'admin123')
              }}
            >
              <div className="text-xs uppercase text-primary-700">Admin</div>
              <div className="font-semibold">admin@library.com</div>
              <div className="text-xs text-primary-700">admin123</div>
            </button>
            <button
              type="button"
              className="rounded-md bg-white px-3 py-2 text-left shadow-sm ring-1 ring-primary-100 hover:bg-primary-100"
              onClick={() => {
                setValue('email', 'alice@example.com')
                setValue('password', 'password123')
              }}
            >
              <div className="text-xs uppercase text-primary-700">User</div>
              <div className="font-semibold">alice@example.com</div>
              <div className="text-xs text-primary-700">password123</div>
            </button>
          </div>
        </div>
        <div className="mt-4 flex items-center justify-between text-sm text-muted-600">
          <Link className="text-primary-600 hover:text-primary-700" to="/forgot-password">
            Forgot password?
          </Link>
          <Link className="text-primary-600 hover:text-primary-700" to="/register">
            Create account
          </Link>
        </div>
      </Card>
    </div>
  )
}
