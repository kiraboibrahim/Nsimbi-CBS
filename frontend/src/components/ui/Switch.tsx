'use client';

import React from 'react';
import { clsx } from 'clsx';

interface SwitchProps {
  id?: string;
  checked: boolean;
  onChange: (checked: boolean) => void;
  disabled?: boolean;
  label?: string;
  size?: 'sm' | 'md';
  className?: string;
}

export function Switch({ id, checked, onChange, disabled = false, label, size = 'sm', className }: SwitchProps) {
  const isSm = size === 'sm';

  return (
    <label
      htmlFor={id}
      className={clsx(
        'inline-flex items-center gap-2 cursor-pointer select-none',
        disabled && 'opacity-50 cursor-not-allowed',
        className
      )}
    >
      <button
        type="button"
        id={id}
        role="switch"
        aria-checked={checked}
        disabled={disabled}
        onClick={() => !disabled && onChange(!checked)}
        className={clsx(
          'relative inline-flex items-center shrink-0 cursor-pointer rounded-full p-0.5 transition-colors duration-200 ease-in-out focus:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2',
          isSm ? 'h-4.5 w-8' : 'h-6 w-11',
          checked ? 'bg-primary' : 'bg-muted-foreground/30'
        )}
      >
        <span
          className={clsx(
            'pointer-events-none inline-block transform rounded-full bg-white shadow-xs ring-0 transition-transform duration-200 ease-in-out',
            isSm ? 'h-3.5 w-3.5' : 'h-5 w-5',
            checked ? (isSm ? 'translate-x-3.5' : 'translate-x-5') : 'translate-x-0'
          )}
        />
      </button>
      {label && <span className={clsx(isSm ? 'text-[11px] font-medium text-foreground' : 'text-xs font-semibold text-foreground')}>{label}</span>}
    </label>
  );
}
