import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import toast from 'react-hot-toast'
import { Button } from '../../components/common/Button'
import { Input } from '../../components/common/Input'
import { Card } from '../../components/common/Card'
import { authService } from '../../services/authService'
import { getErrorMessage } from '../../utils/helpers'

interface ForgotFormValues {
  email: string
}

export const ForgotPasswordPage = () => {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ForgotFormValues>()

  const onSubmit = async (values: ForgotFormValues) => {
    try {
      await authService.forgotPassword(values.email)
      toast.success('If an account exists, a reset link has been sent.')
    } catch (error) {
      toast.error(getErrorMessage(error))
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-cyan-50 px-4">
      <Card className="w-full max-w-md">
        <h1 className="text-2xl font-bold text-muted-900">Reset your password</h1>
        <p className="text-sm text-muted-600">Enter your email and we will send you reset instructions.</p>
        <form className="mt-4 space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <Input
            label="Email"
            type="email"
            placeholder="you@example.com"
            {...register('email', { required: 'Email is required' })}
            error={errors.email?.message}
          />
          <Button type="submit" className="w-full" loading={isSubmitting}>
            Send reset link
          </Button>
        </form>
        <div className="mt-4 text-center text-sm">
          <Link className="text-primary-600" to="/login">
            Back to login
          </Link>
        </div>
      </Card>
    </div>
  )
}
