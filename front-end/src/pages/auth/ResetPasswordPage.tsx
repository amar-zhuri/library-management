import { useForm } from 'react-hook-form'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import { Button } from '../../components/common/Button'
import { Input } from '../../components/common/Input'
import { Card } from '../../components/common/Card'
import { authService } from '../../services/authService'
import { getErrorMessage } from '../../utils/helpers'

interface ResetFormValues {
  newPassword: string
}

export const ResetPasswordPage = () => {
  const [params] = useSearchParams()
  const token = params.get('token')
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ResetFormValues>()

  const onSubmit = async (values: ResetFormValues) => {
    if (!token) {
      toast.error('Missing token.')
      return
    }
    try {
      await authService.resetPassword(token, values.newPassword)
      toast.success('Password updated. Please login.')
      navigate('/login')
    } catch (error) {
      toast.error(getErrorMessage(error))
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-cyan-50 px-4">
      <Card className="w-full max-w-md">
        <h1 className="text-2xl font-bold text-muted-900">Choose a new password</h1>
        <form className="mt-4 space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <Input
            label="New Password"
            type="password"
            placeholder="••••••••"
            {...register('newPassword', { required: 'Password is required', minLength: { value: 6, message: 'Minimum 6 characters' } })}
            error={errors.newPassword?.message}
          />
          <Button type="submit" className="w-full" loading={isSubmitting}>
            Update password
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
