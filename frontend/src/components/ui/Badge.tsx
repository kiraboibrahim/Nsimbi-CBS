'use client';

import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: any[]) {
  return twMerge(clsx(inputs));
}

const badgeVariants = cva(
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 select-none',
  {
    variants: {
      variant: {
        default: 'border-primary/20 bg-primary/10 text-primary',
        brand: 'border-primary/20 bg-primary/10 text-primary',
        secondary: 'border-muted bg-secondary text-muted-foreground',
        neutral: 'border-muted bg-secondary text-muted-foreground',
        destructive: 'border-destructive/20 bg-destructive/10 text-destructive',
        danger: 'border-destructive/20 bg-destructive/10 text-destructive',
        outline: 'border-border bg-transparent text-foreground',
        success: 'border-success/20 bg-success/10 text-success',
        warning: 'border-amber-500/20 bg-amber-500/10 text-amber-500',
        info: 'border-blue-500/20 bg-blue-500/10 text-blue-500',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  }
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {
  size?: 'sm' | 'md';
}

function Badge({ className, variant, size, ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant }), className)} {...props} />
  );
}

export { Badge, badgeVariants };
