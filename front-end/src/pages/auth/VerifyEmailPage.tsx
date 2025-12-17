import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import toast from 'react-hot-toast'
import { authService } from '../../services/authService'
import { Card } from '../../components/common/Card'
import { Button } from '../../components/common/Button'
import { getErrorMessage } from '../../utils/helpers'

export const VerifyEmailPage = () => {
  const [params] = useSearchParams()
  const token = params.get('token')
  const [message, setMessage] = useState<string>('Verifying your email...')
  const [success, setSuccess] = useState<boolean | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const verify = async () => {
      if (!token) {
        setMessage('Verification token missing.')
        setSuccess(false)
        setLoading(false)
        return
      }
      try {
        const res = await authService.verifyEmail(token)
        setMessage(res.message)
        setSuccess(res.success ?? true)
      } catch (error) {
        setMessage(getErrorMessage(error))
        setSuccess(false)
      } finally {
        setLoading(false)
      }
    }
    verify()
  }, [token])

  const handleResend = async () => {
    const email = prompt('Enter your email to resend verification')
    if (!email) return
    try {
      await authService.resendVerification(email)
      toast.success('Verification email sent.')
    } catch (error) {
      toast.error(getErrorMessage(error))
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-cyan-50 px-4">
      <Card className="w-full max-w-lg text-center">
        <p className="text-sm font-semibold uppercase tracking-wide text-primary-600">Email Verification</p>
        <h1 className="mt-1 text-2xl font-bold text-muted-900">{loading ? 'Checking...' : success ? 'Verified!' : 'Verification failed'}</h1>
        <p className="mt-2 text-sm text-muted-700">{message}</p>
        <div className="mt-6 flex items-center justify-center gap-3">
          <Link to="/login" className="text-primary-600">
            Go to login
          </Link>
          {!success && (
            <Button variant="secondary" size="sm" onClick={handleResend}>
              Resend email
            </Button>
          )}
        </div>
      </Card>
    </div>
  )
}
