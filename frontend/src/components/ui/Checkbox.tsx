'use client';

import * as React from 'react';
import { Check, Minus } from 'lucide-react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: any[]) {
    return twMerge(clsx(inputs));
}

export interface CheckboxProps
    extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'onChange' | 'checked'> {
    checked?: boolean | 'indeterminate';
    onCheckedChange?: (checked: boolean) => void;
    onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

/**
 * Tri-state checkbox supporting checked, unchecked, and indeterminate states.
 */
export const Checkbox = React.forwardRef<HTMLInputElement, CheckboxProps>(
    ({ className, checked, onCheckedChange, onChange, disabled, ...props }, ref) => {
        const isIndeterminate = checked === 'indeterminate';
        const isChecked = checked === true;

        const internalRef = React.useRef<HTMLInputElement>(null);
        React.useImperativeHandle(ref, () => internalRef.current!);

        React.useEffect(() => {
            if (internalRef.current) {
                internalRef.current.indeterminate = isIndeterminate;
            }
        }, [isIndeterminate]);

        const handleClick = (e: React.MouseEvent) => {
            if (disabled) return;
            e.stopPropagation();
            if (onCheckedChange) {
                onCheckedChange(!isChecked);
            }
        };

        const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
            e.stopPropagation();
            if (onChange) onChange(e);
            if (onCheckedChange) onCheckedChange(e.target.checked);
        };

        return (
            <div
                onClick={handleClick}
                className={cn(
                    'group relative inline-flex items-center justify-center shrink-0 cursor-pointer select-none',
                    disabled && 'cursor-not-allowed opacity-50'
                )}
            >
                <input
                    ref={internalRef}
                    type="checkbox"
                    checked={isChecked}
                    onChange={handleChange}
                    disabled={disabled}
                    className="sr-only peer"
                    {...props}
                />
                <div
                    className={cn(
                        'w-4 h-4 rounded-md border flex items-center justify-center transition-all duration-150 ease-out',
                        'peer-focus-visible:ring-2 peer-focus-visible:ring-primary/40 peer-focus-visible:outline-none',
                        isChecked
                            ? 'bg-primary border-primary text-primary-foreground shadow-xs'
                            : isIndeterminate
                            ? 'bg-primary/15 border-primary text-primary shadow-xs'
                            : 'bg-background border-border/80 group-hover:border-primary/50 group-hover:bg-muted/30',
                        className
                    )}
                >
                    {isChecked && (
                        <Check className="w-3 h-3 stroke-[3] text-primary-foreground animate-in zoom-in-75 duration-100" />
                    )}
                    {isIndeterminate && (
                        <Minus className="w-3 h-3 stroke-[3] text-primary animate-in zoom-in-75 duration-100" />
                    )}
                </div>
            </div>
        );
    }
);

Checkbox.displayName = 'Checkbox';
