'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { SavingsAccountSummary } from '@/types/savings';
import { formatDate } from '@/lib/formatters';
import { Badge } from '@/components/ui/Badge';
import {
  ArrowLeft,
  MoreVertical,
  Lock,
  Unlock,
  ShieldAlert,
  Wallet,
  XCircle,
  Package,
  Calendar,
} from 'lucide-react';
import { toast } from 'sonner';

interface SavingsProfileHeaderProps {
  account: SavingsAccountSummary;
  onToggleFreeze: (isFrozen: boolean) => void;
  isFreezePending: boolean;
  onOpenHoldModal: () => void;
}

export function SavingsProfileHeader({
  account,
  onToggleFreeze,
  isFreezePending,
  onOpenHoldModal,
}: SavingsProfileHeaderProps) {
  const [menuOpen, setMenuOpen] = useState(false);

  const isFrozen = !!account.subStatus?.block;
  const isActive = !!account.status?.active && !isFrozen;
  const isClosed = !!account.status?.closed;

  const getInitials = (name?: string) => {
    if (!name) return 'SA';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  };

  return (
    <div className="p-6 flex flex-col sm:flex-row sm:items-center justify-between gap-6">
      <div className="flex items-start sm:items-center gap-4">
        <Link
          href="/savings"
          className="p-2 rounded-lg border border-border bg-background hover:bg-muted text-muted-foreground hover:text-foreground transition-colors mt-1 sm:mt-0"
          title="Back to Savings Directory"
        >
          <ArrowLeft className="w-4 h-4" />
        </Link>

        <div className="w-14 h-14 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center text-primary font-bold text-lg flex-shrink-0">
          {getInitials(account.clientName)}
        </div>

        <div className="space-y-1">
          <div className="flex items-center gap-3 flex-wrap">
            <h1 className="text-xl sm:text-2xl font-bold text-foreground">
              {account.clientName}
            </h1>
            {isFrozen ? (
              <Badge variant="destructive" className="text-xs px-2.5 py-0.5">
                Frozen
              </Badge>
            ) : isActive ? (
              <Badge variant="success" className="text-xs px-2.5 py-0.5">
                Active
              </Badge>
            ) : isClosed ? (
              <Badge variant="secondary" className="text-xs px-2.5 py-0.5">
                Closed
              </Badge>
            ) : (
              <Badge variant="outline" className="text-xs px-2.5 py-0.5">
                {account.status?.value || 'Pending'}
              </Badge>
            )}
          </div>

          <div className="flex items-center gap-3 text-xs text-muted-foreground flex-wrap">
            <span className="font-mono text-foreground font-semibold">
              Account No: {account.accountNo}
            </span>
            {account.clientType && (
              <span className="uppercase font-medium bg-muted px-2 py-0.5 rounded text-[10px]">
                {account.clientType}
              </span>
            )}
            {account.officeName && (
              <span>• {account.officeName}</span>
            )}
          </div>

          <div className="pt-1 flex items-center gap-4 text-xs text-muted-foreground flex-wrap">
            <span className="flex items-center gap-1 uppercase font-semibold text-foreground/80">
              <Package className="w-3.5 h-3.5 text-primary" />
              {account.savingsProductName || 'FlexSave'}
            </span>
            <span className="flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5" />
              Opened: {formatDate(account.timeline?.activatedOnDate || account.timeline?.submittedOnDate)}
            </span>
          </div>
        </div>
      </div>

      {/* Top Options Dropdown Menu */}
      <div className="relative self-end sm:self-start">
        <button
          onClick={() => setMenuOpen(!menuOpen)}
          className="p-1.5 rounded-lg border border-border bg-background hover:bg-muted text-muted-foreground hover:text-foreground transition-colors"
          aria-label="Account options"
        >
          <MoreVertical className="w-4 h-4" />
        </button>

        {menuOpen && (
          <>
            <div className="fixed inset-0 z-20" onClick={() => setMenuOpen(false)} />
            <div className="absolute right-0 top-full mt-1.5 w-48 rounded-lg border border-border bg-card shadow-xl p-1 z-30 text-xs">
              <button
                onClick={() => {
                  onToggleFreeze(isFrozen);
                  setMenuOpen(false);
                }}
                disabled={isFreezePending || isClosed}
                className="w-full flex items-center gap-2 px-3 py-2 rounded-md hover:bg-muted text-foreground transition-colors font-medium text-left"
              >
                {isFrozen ? (
                  <>
                    <Unlock className="w-3.5 h-3.5 text-emerald-500" />
                    Unfreeze Account
                  </>
                ) : (
                  <>
                    <Lock className="w-3.5 h-3.5 text-amber-500" />
                    Freeze Account
                  </>
                )}
              </button>

              <button
                onClick={() => {
                  onOpenHoldModal();
                  setMenuOpen(false);
                }}
                disabled={isClosed}
                className="w-full flex items-center gap-2 px-3 py-2 rounded-md hover:bg-muted text-foreground transition-colors font-medium text-left"
              >
                <ShieldAlert className="w-3.5 h-3.5 text-blue-500" />
                Place Funds Hold
              </button>

              <Link
                href={`/debit-cards?accountId=${account.id}`}
                className="flex items-center gap-2 px-3 py-2 rounded-md hover:bg-muted text-foreground transition-colors font-medium"
                onClick={() => setMenuOpen(false)}
              >
                <Wallet className="w-3.5 h-3.5 text-purple-500" />
                Debit Card Issuance
              </Link>

              <button
                onClick={() => {
                  toast.info('Account closure requires zero balance and branch manager approval.');
                  setMenuOpen(false);
                }}
                className="w-full flex items-center gap-2 px-3 py-2 rounded-md hover:bg-muted text-destructive transition-colors font-medium text-left"
              >
                <XCircle className="w-3.5 h-3.5" />
                Close Account
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
