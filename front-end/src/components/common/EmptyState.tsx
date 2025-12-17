import type { ReactNode } from 'react'
import { Button } from './Button'

interface EmptyStateProps {
  title: string
  description?: string
  actionText?: string
  onAction?: () => void
  icon?: ReactNode
}

export const EmptyState = ({ title, description, actionText, onAction, icon }: EmptyStateProps) => (
  <div className="flex flex-col items-center justify-center gap-3 rounded-2xl border border-dashed border-muted-200 bg-white/60 px-6 py-10 text-center">
    <div className="text-3xl">{icon ?? '📚'}</div>
    <div>
      <p className="text-lg font-semibold text-muted-900">{title}</p>
      {description && <p className="text-sm text-muted-600">{description}</p>}
    </div>
    {actionText && onAction && (
      <Button size="sm" onClick={onAction}>
        {actionText}
      </Button>
    )}
  </div>
)
