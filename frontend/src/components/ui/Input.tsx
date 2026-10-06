'use client';

import * as React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: any[]) {
  return twMerge(clsx(inputs));
}

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  icon?: React.ReactNode;
}

export const renderFormattedLabel = (label: string) => {
  if (label.includes('*')) {
    const parts = label.split('*');
    return (
      <>
        {parts[0]}
        <span className="text-red-500 font-semibold">*</span>
        {parts.slice(1).join('*')}
      </>
    );
  }
  return label;
};

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, icon, className, type, ...props }, ref) => {
    return (
      <div className="w-full">
        {label && (
          <label className="block text-xs font-medium text-muted-foreground/80 mb-1.5 tracking-tight">
            {renderFormattedLabel(label)}
          </label>
        )}
        <div className="relative flex items-center">
          {icon && (
            <div className="absolute left-3 text-muted-foreground/70 pointer-events-none shrink-0">
              {icon}
            </div>
          )}
          <input
            type={type}
            ref={ref}
            className={cn(
              'flex h-9 w-full rounded-lg border border-border bg-card px-3 py-1.5 text-xs font-normal text-foreground shadow-xs transition-colors duration-150',
              'file:border-0 file:bg-transparent file:text-xs file:font-medium file:text-foreground',
              'placeholder:text-muted-foreground/50',
              'outline-none focus:border-zinc-400 dark:focus:border-zinc-500 focus:ring-1 focus:ring-zinc-500/20 focus:bg-background',
              'hover:border-border/80 hover:bg-background',
              'disabled:cursor-not-allowed disabled:opacity-50',
              icon && 'pl-9',
              error && 'border-destructive focus:border-destructive focus:ring-destructive/30',
              className
            )}
            {...props}
          />
        </div>
        {error && <p className="text-[11px] text-destructive mt-1 font-medium">{error}</p>}
      </div>
    );
  }
);
Input.displayName = 'Input';

export { Input };
