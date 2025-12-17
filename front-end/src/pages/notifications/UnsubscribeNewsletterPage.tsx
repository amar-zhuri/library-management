import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Card } from '../../components/common/Card'
import { Button } from '../../components/common/Button'
import { notificationService } from '../../services/notificationService'
import { getErrorMessage } from '../../utils/helpers'

export const UnsubscribeNewsletterPage = () => {
  const [params] = useSearchParams()
  const token = params.get('token')
  const [message, setMessage] = useState('Processing your request...')
  const [success, setSuccess] = useState<boolean | null>(null)

  useEffect(() => {
    const run = async () => {
      if (!token) {
        setMessage('Missing unsubscribe token.')
        setSuccess(false)
        return
      }
      try {
        const res = await notificationService.unsubscribeNewsletter(token)
        setMessage(res.message ?? 'You have been unsubscribed from the newsletter.')
        setSuccess(res.success ?? true)
      } catch (error) {
        setMessage(getErrorMessage(error))
        setSuccess(false)
      }
    }
    run()
  }, [token])

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-cyan-50 px-4">
      <Card className="w-full max-w-lg text-center">
        <p className="text-sm font-semibold uppercase tracking-wide text-primary-600">Newsletter</p>
        <h1 className="mt-2 text-2xl font-bold text-muted-900">{success === null ? 'Working...' : success ? 'Unsubscribed' : 'Request failed'}</h1>
        <p className="mt-2 text-sm text-muted-700">{message}</p>
        <div className="mt-6 flex items-center justify-center gap-3">
          <Link to="/login" className="text-primary-600">
            Back to login
          </Link>
          <Button size="sm" variant="secondary" onClick={() => window.location.reload()}>
            Retry
          </Button>
        </div>
      </Card>
    </div>
  )
}
