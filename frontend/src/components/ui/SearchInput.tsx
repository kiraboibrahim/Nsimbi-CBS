'use client';

import * as React from 'react';
import { Search, X } from 'lucide-react';
import { cn } from '@/lib/utils';

export interface SearchInputProps
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'value' | 'onChange'> {
  value: string;
  onChange: (value: string) => void;
  onClear?: () => void;
  containerClassName?: string;
  iconClassName?: string;
}

export const SearchInput = React.forwardRef<HTMLInputElement, SearchInputProps>(
  (
    {
      value,
      onChange,
      onClear,
      containerClassName,
      iconClassName,
      className,
      placeholder = 'Search...',
      type = 'text',
      ...props
    },
    ref
  ) => {
    const internalRef = React.useRef<HTMLInputElement>(null);
    React.useImperativeHandle(ref, () => internalRef.current!);

    const handleClear = (e: React.MouseEvent) => {
      e.stopPropagation();
      e.preventDefault();
      onChange('');
      if (onClear) onClear();
      if (internalRef.current) {
        internalRef.current.focus();
      }
    };

    return (
      <div className={cn('relative flex items-center shrink-0', containerClassName)}>
        <Search
          className={cn(
            'w-4 h-4 text-muted-foreground absolute left-3 pointer-events-none shrink-0',
            iconClassName
          )}
        />
        <input
          ref={internalRef}
          type={type}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          className={cn(
            'h-9 pl-9 pr-8 w-64 rounded-lg bg-muted border border-border text-foreground text-xs focus:outline-none focus:border-zinc-400 dark:focus:border-zinc-500 focus:ring-1 focus:ring-zinc-500/20 transition-colors',
            className
          )}
          {...props}
        />
        {value ? (
          <button
            type="button"
            onClick={handleClear}
            className="absolute right-2.5 p-0.5 rounded-md text-muted-foreground hover:text-foreground hover:bg-background/60 transition-colors cursor-pointer"
            title="Clear search"
            aria-label="Clear search"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        ) : null}
      </div>
    );
  }
);

SearchInput.displayName = 'SearchInput';
