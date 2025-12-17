import clsx from 'clsx'

export const Spinner = ({ className }: { className?: string }) => (
  <div className={clsx('h-10 w-10 animate-spin rounded-full border-2 border-primary-200 border-t-primary-600', className)} />
)
