'use client';

import React from 'react';
import { Button } from '@/components/ui/Button';
import { toast } from 'sonner';

export function SavingsDocumentsSection() {
  return (
    <div className="w-full px-6 py-3.5 bg-muted/20 border-t border-border flex items-center justify-between text-xs">
      <span className="font-semibold text-foreground">Documents</span>
      <div className="flex items-center gap-2">
        <span className="text-muted-foreground">Account Mandate & National ID</span>
        <Button
          variant="outline"
          size="sm"
          className="h-7 text-xs px-2.5"
          onClick={() => toast.info('Mandate document viewer initialized for verified KYC files.')}
        >
          View
        </Button>
      </div>
    </div>
  );
}
