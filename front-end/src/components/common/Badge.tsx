import type { HTMLAttributes } from 'react'
import clsx from 'clsx'

type Tone = 'primary' | 'success' | 'warning' | 'neutral' | 'danger'

interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  tone?: Tone
}

const toneStyles: Record<Tone, string> = {
  primary: 'bg-primary-50 text-primary-700 border-primary-100',
  success: 'bg-emerald-50 text-emerald-700 border-emerald-100',
  warning: 'bg-amber-50 text-amber-700 border-amber-100',
  neutral: 'bg-muted-50 text-muted-700 border-muted-100',
  danger: 'bg-red-50 text-red-600 border-red-100',
}

export const Badge = ({ className, tone = 'neutral', ...props }: BadgeProps) => (
  <span
    className={clsx(
      'inline-flex items-center gap-1 rounded-full border px-3 py-1 text-xs font-semibold uppercase tracking-wide',
      toneStyles[tone],
      className
    )}
    {...props}
  />
)
