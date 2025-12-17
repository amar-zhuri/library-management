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
