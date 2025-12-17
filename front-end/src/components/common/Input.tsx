import type { InputHTMLAttributes, ReactNode } from 'react'
import clsx from 'clsx'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  helperText?: string
  iconLeft?: ReactNode
  iconRight?: ReactNode
}

export const Input = ({ label, error, helperText, className, iconLeft, iconRight, ...props }: InputProps) => {
  return (
    <label className="flex flex-col gap-1 text-sm font-medium text-muted-700">
      {label && <span>{label}</span>}
      <div
        className={clsx(
          'flex items-center gap-2 rounded-lg border bg-white px-3 py-2.5 transition focus-within:border-primary-400 focus-within:ring-2 focus-within:ring-primary-100',
          error ? 'border-red-300 ring-red-100' : 'border-muted-200',
          className
        )}
      >
        {iconLeft && <span className="text-muted-400">{iconLeft}</span>}
        <input
          className="w-full border-none bg-transparent text-muted-900 placeholder:text-muted-400 focus:outline-none"
          {...props}
        />
        {iconRight && <span className="text-muted-400">{iconRight}</span>}
      </div>
      {error ? <span className="text-xs font-medium text-red-500">{error}</span> : helperText && <span className="text-xs text-muted-500">{helperText}</span>}
    </label>
  )
}
