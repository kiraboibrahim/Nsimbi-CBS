'use client';

import * as React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: any[]) {
  return twMerge(clsx(inputs));
}

export interface LabelProps extends React.LabelHTMLAttributes<HTMLLabelElement> {}

const Label = React.forwardRef<HTMLLabelElement, LabelProps>(
  ({ className, ...props }, ref) => (
    <label
      ref={ref}
      className={cn(
        'text-xs font-semibold text-foreground/80 mb-1.5 block tracking-tight peer-disabled:cursor-not-allowed peer-disabled:opacity-70 select-none',
        className
      )}
      {...props}
    />
  )
);
Label.displayName = 'Label';

export { Label };
