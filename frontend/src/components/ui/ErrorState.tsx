'use client';

import React from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';
import { Button } from './Button';

export interface ErrorStateProps {
    title?: string;
    description?: string;
    onRetry?: () => void;
    isRetrying?: boolean;
    compact?: boolean;
    className?: string;
}

/**
 * Reusable ErrorState component for displaying inline API error states with retry functionality.
 */
export function ErrorState({
    title = 'Failed to load data',
    description = 'An unexpected error occurred while fetching information from the server.',
    onRetry,
    isRetrying = false,
    compact = false,
    className = '',
}: ErrorStateProps) {
    if (compact) {
        return (
            <div className={`p-3.5 rounded-xl border border-destructive/20 bg-destructive/5 flex items-center justify-between gap-3 text-xs ${className}`}>
                <div className="flex items-center gap-2.5 min-w-0">
                    <AlertTriangle className="w-4 h-4 text-destructive shrink-0" />
                    <div className="min-w-0">
                        <p className="font-medium text-foreground text-xs truncate">{title}</p>
                        <p className="text-[11px] text-muted-foreground truncate">{description}</p>
                    </div>
                </div>
                {onRetry && (
                    <Button
                        variant="outline"
                        size="xs"
                        onClick={onRetry}
                        disabled={isRetrying}
                        icon={<RefreshCw className={`w-3 h-3 ${isRetrying ? 'animate-spin' : ''}`} />}
                        className="shrink-0"
                    >
                        Retry
                    </Button>
                )}
            </div>
        );
    }

    return (
        <div className={`p-6 rounded-xl border border-border bg-card/60 text-center space-y-3.5 max-w-sm mx-auto my-4 ${className}`}>
            <div className="w-10 h-10 rounded-xl bg-destructive/10 border border-destructive/20 text-destructive flex items-center justify-center mx-auto">
                <AlertTriangle className="w-5 h-5" />
            </div>
            <div className="space-y-1">
                <h3 className="text-xs font-semibold text-foreground tracking-tight">{title}</h3>
                <p className="text-[11px] text-muted-foreground leading-normal max-w-xs mx-auto">
                    {description}
                </p>
            </div>
            {onRetry && (
                <div className="pt-1">
                    <Button
                        variant="outline"
                        size="xs"
                        onClick={onRetry}
                        disabled={isRetrying}
                        icon={<RefreshCw className={`w-3 h-3 ${isRetrying ? 'animate-spin' : ''}`} />}
                    >
                        Try Again
                    </Button>
                </div>
            )}
        </div>
    );
}
