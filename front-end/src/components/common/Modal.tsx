import type { ReactNode } from 'react'
import { Button } from './Button'

interface ModalProps {
  title: string
  description?: string
  isOpen: boolean
  onClose: () => void
  onConfirm?: () => void
  confirmText?: string
  confirmTone?: 'primary' | 'danger'
  children?: ReactNode
}

export const Modal = ({
  title,
  description,
  isOpen,
  onClose,
  onConfirm,
  confirmText = 'Confirm',
  confirmTone = 'primary',
  children,
}: ModalProps) => {
  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm px-4">
      <div className="w-full max-w-lg rounded-2xl border border-muted-100 bg-white p-6 shadow-2xl">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h3 className="text-lg font-semibold text-muted-900">{title}</h3>
            {description && <p className="mt-1 text-sm text-muted-600">{description}</p>}
          </div>
          <button onClick={onClose} className="text-muted-400 transition hover:text-muted-600">
            ✕
          </button>
        </div>
        <div className="mt-4 text-sm text-muted-700">{children}</div>
        <div className="mt-6 flex justify-end gap-3">
          <Button variant="ghost" onClick={onClose}>
            Cancel
          </Button>
          {onConfirm && (
            <Button variant={confirmTone === 'danger' ? 'danger' : 'primary'} onClick={onConfirm}>
              {confirmText}
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}
