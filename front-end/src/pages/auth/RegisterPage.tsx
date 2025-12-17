import { Link, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { Button } from '../../components/common/Button'
import { Input } from '../../components/common/Input'
import { Card } from '../../components/common/Card'
import { useAuth } from '../../hooks/useAuth'

interface RegisterFormValues {
  name: string
  email: string
  password: string
}

export const RegisterPage = () => {
  const { register: registerUser, loading } = useAuth()
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>()

  const onSubmit = async (values: RegisterFormValues) => {
    await registerUser(values.name, values.email, values.password)
    navigate('/')
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-cyan-50 px-4">
      <Card className="w-full max-w-md">
        <div className="mb-4 text-center">
          <p className="text-sm font-semibold uppercase tracking-wide text-primary-600">Join the library</p>
          <h1 className="text-2xl font-bold text-muted-900">Create your account</h1>
          <p className="text-sm text-muted-600">Track books, insights, and recommendations.</p>
        </div>
        <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <Input label="Full Name" placeholder="Ada Lovelace" {...register('name', { required: 'Name is required' })} error={errors.name?.message} />
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
            {...register('password', { required: 'Password is required', minLength: { value: 6, message: 'Minimum 6 characters' } })}
            error={errors.password?.message}
          />
          <Button type="submit" className="w-full" loading={loading}>
            Create account
          </Button>
        </form>
        <div className="mt-4 text-center text-sm text-muted-600">
          Already have an account?{' '}
          <Link className="text-primary-600 hover:text-primary-700" to="/login">
            Login
          </Link>
        </div>
      </Card>
    </div>
  )
}
