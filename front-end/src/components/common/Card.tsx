import type { HTMLAttributes } from 'react'
import clsx from 'clsx'

export const Card = ({ className, ...props }: HTMLAttributes<HTMLDivElement>) => (
  <div
    className={clsx(
      'rounded-2xl border border-muted-100 bg-white/90 p-5 shadow-card backdrop-blur-sm transition hover:-translate-y-0.5 hover:shadow-lg',
      className
    )}
    {...props}
  />
)
