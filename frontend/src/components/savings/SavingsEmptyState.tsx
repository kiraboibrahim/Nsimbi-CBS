'use client';

import React from 'react';
import Link from 'next/link';
import { Wallet, Plus, RefreshCw, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/Button';

interface SavingsEmptyStateProps {
  isLoading?: boolean;
  isError?: boolean;
  errorMessage?: string;
  onRetry?: () => void;
  search?: string;
  statusFilter?: string;
  onClearSearch?: () => void;
}

export function SavingsEmptyState({
  isLoading,
  isError,
  errorMessage,
  onRetry,
  search,
  statusFilter = 'all',
  onClearSearch,
}: SavingsEmptyStateProps) {
  if (isLoading) {
    return (
      <div className="py-16 text-center text-muted-foreground space-y-3">
        <RefreshCw className="w-8 h-8 animate-spin mx-auto text-primary" />
        <p className="text-sm font-medium">Loading savings accounts...</p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="py-16 text-center space-y-3 px-4">
        <AlertCircle className="w-10 h-10 text-destructive mx-auto" />
        <p className="text-base font-semibold text-foreground">Unable to load savings accounts</p>
        <p className="text-sm text-muted-foreground max-w-md mx-auto">
          {errorMessage || 'Unable to retrieve savings accounts. Please try again.'}
        </p>
        {onRetry && (
          <Button onClick={onRetry} variant="outline" size="sm" className="mt-2">
            <RefreshCw className="w-3.5 h-3.5 mr-1.5" />
            Retry
          </Button>
        )}
      </div>
    );
  }

  return (
    <div className="py-16 text-center space-y-4 px-4">
      <div className="w-14 h-14 rounded-full bg-primary/10 text-primary flex items-center justify-center mx-auto border border-primary/20">
        <Wallet className="w-7 h-7" />
      </div>
      <div className="space-y-1">
        <h3 className="text-base font-semibold text-foreground">No Savings Accounts Found</h3>
        <p className="text-xs text-muted-foreground max-w-sm mx-auto">
          {search
            ? `No accounts matching "${search}". Try clearing your search query.`
            : statusFilter !== 'all'
            ? `No savings accounts found with status "${statusFilter}".`
            : 'There are currently no savings accounts registered in the core banking system.'}
        </p>
      </div>
      <div className="flex justify-center gap-3 pt-2">
        {search && onClearSearch && (
          <Button variant="outline" size="sm" onClick={onClearSearch}>
            Clear Search
          </Button>
        )}
        <Link href="/members/new">
          <Button size="sm" className="bg-primary hover:bg-primary-hover text-white gap-1.5">
            <Plus className="w-4 h-4" />
            Register Member
          </Button>
        </Link>
      </div>
    </div>
  );
}
