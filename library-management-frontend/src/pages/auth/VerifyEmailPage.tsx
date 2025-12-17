import { useState, useEffect, useRef } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { authService } from '../../services/authService';
import { Alert, Loading } from '../../components/common';

const getTokenFromUrl = () => {
  try {
    const queryToken = new URLSearchParams(window.location.search).get('token');
    const hashPart = window.location.hash.includes('?') ? window.location.hash.split('?')[1] : '';
    const hashToken = hashPart ? new URLSearchParams(hashPart).get('token') : null;
    const pathTokenMatch = window.location.pathname.match(/verify-email\/?([^/?#]+)/)?.[1];
    const raw = queryToken || hashToken || pathTokenMatch || '';
    return raw ? decodeURIComponent(raw) : '';
  } catch {
    return '';
  }
};

export function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const token = getTokenFromUrl() || searchParams.get('token') || '';

  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');
  const [resending, setResending] = useState(false);

    const hasVerified = useRef(false);

  useEffect(() => {
    const verifyEmail = async () => {
         // Prevent double execution
      if (hasVerified.current) {
        return;
      }
      hasVerified.current = true;
      
      
      if (!token) {
        setStatus('error');
        setMessage('Invalid or missing verification token.');
        return;
      }

      try {
        const response = await authService.verifyEmail(token);
        const serverMessage = response.message || 'Email verified successfully! You can now login.';
        const alreadyVerified = serverMessage.toLowerCase().includes('already verified');
        setStatus(alreadyVerified ? 'success' : 'success');
        setMessage(serverMessage);
      } catch (err: unknown) {
        const error = err as { response?: { data?: { message?: string; success?: boolean } } };

        const serverMessage = error.response?.data?.message || 'Failed to verify email. The link may have expired.';
        const looksVerified =
          serverMessage.toLowerCase().includes('already verified') || serverMessage.toLowerCase().includes('verified');
        const successFlag = Boolean(error.response?.data?.success);

        // If backend already marked the email verified, show success even on error responses
        if (successFlag || looksVerified) {
          setStatus('success');
        } else {
          setStatus('error');
        }
        setMessage(serverMessage);
        // If truly invalid, offer retry guidance
        if (!successFlag && !looksVerified) {
          console.error('Verification failed with token:', token);
        }
      }
    };

    verifyEmail();
  }, [token]);

  const handleResend = async () => {
    const storedUserEmail = (() => {
      try {
        const raw = localStorage.getItem('user');
        return raw ? (JSON.parse(raw)?.email as string | undefined) : undefined;
      } catch {
        return undefined;
      }
    })();

    const email = prompt('Enter your email to resend verification', storedUserEmail || '');
    if (!email) return;

    try {
      setResending(true);
      const res = await authService.resendVerification(email);
      setStatus('success');
      setMessage(res.message || 'Verification email sent. Check your inbox.');
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } };
      setStatus('error');
      setMessage(error.response?.data?.message || 'Unable to resend verification email.');
    } finally {
      setResending(false);
    }
  };

  if (status === 'loading') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <Loading />
          <p className="mt-4 text-gray-600">Verifying your email...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900">📚 Library</h1>
          <h2 className="mt-2 text-xl text-gray-600">Email Verification</h2>
        </div>

        <div className="bg-white p-8 rounded-xl shadow-sm border border-gray-200 text-center">
          {status === 'success' ? (
            <>
              <div className="text-6xl mb-4">✅</div>
              <Alert type="success" message={message} />
              <p className="text-gray-600 mt-4">
                Your email has been verified. You can now login to your account.
              </p>
              <Link
                to="/login"
                className="mt-6 inline-block bg-indigo-600 text-white px-6 py-2 rounded-lg font-medium hover:bg-indigo-700"
              >
                Go to Login
              </Link>
            </>
          ) : (
            <>
              <div className="text-6xl mb-4">❌</div>
              <Alert type="error" message={message} />
              <p className="text-gray-600 mt-4">
                Please try again or request a new verification email.
              </p>
              <div className="mt-6 space-y-3">
                <Link
                  to="/login"
                  className="block bg-indigo-600 text-white px-6 py-2 rounded-lg font-medium hover:bg-indigo-700"
                >
                  Go to Login
                </Link>
                <button
                  onClick={handleResend}
                  disabled={resending}
                  className="block w-full bg-white text-indigo-600 border border-indigo-200 px-6 py-2 rounded-lg font-medium hover:bg-indigo-50 disabled:opacity-60"
                >
                  {resending ? 'Sending...' : 'Resend verification email'}
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
