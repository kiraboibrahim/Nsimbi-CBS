'use client';

import React from 'react';
import { Button } from '@/components/ui/Button';

interface SavingsPaginationProps {
  page: number;
  totalPages: number;
  totalRecords: number;
  startRecord: number;
  endRecord: number;
  isLoading: boolean;
  onPageChange: (newPage: number) => void;
}

export function SavingsPagination({
  page,
  totalPages,
  totalRecords,
  startRecord,
  endRecord,
  isLoading,
  onPageChange,
}: SavingsPaginationProps) {
  return (
    <div className="p-4 px-6 bg-muted/10 border-t border-border flex flex-col sm:flex-row justify-between items-center text-xs gap-3">
      <div className="uppercase tracking-wider text-muted-foreground font-semibold">
        {totalRecords === 0
          ? '0 Accounts'
          : `${startRecord} - ${endRecord} of ${totalRecords} Accounts`}
      </div>

      <div className="flex items-center gap-1.5">
        <Button
          variant="outline"
          size="sm"
          disabled={page <= 1 || isLoading}
          onClick={() => onPageChange(Math.max(1, page - 1))}
          className="text-xs px-2.5 py-1"
        >
          &larr; Prev
        </Button>

        <span className="px-3 text-muted-foreground font-mono">
          Page {page} of {totalPages}
        </span>

        <Button
          variant="outline"
          size="sm"
          disabled={page >= totalPages || isLoading}
          onClick={() => onPageChange(page + 1)}
          className="text-xs px-2.5 py-1"
        >
          Next &rarr;
        </Button>
      </div>
    </div>
  );
}
