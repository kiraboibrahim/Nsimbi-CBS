'use client';

import React from 'react';
import Link from 'next/link';
import { SavingsAccountSummary } from '@/types/savings';
import { formatUGX, formatDate } from '@/lib/formatters';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import {
  MoreVertical,
  Wallet,
  ArrowDownLeft,
  Lock,
  Unlock,
  Clock,
  Calendar,
} from 'lucide-react';

interface SavingsListItemProps {
  account: SavingsAccountSummary;
  isOpenDropdown: boolean;
  onToggleDropdown: () => void;
  onCloseDropdown: () => void;
  onOpenDeposit: () => void;
  onToggleFreeze: (isFrozen: boolean) => void;
  isFreezePending: boolean;
}

export function SavingsListItem({
  account,
  isOpenDropdown,
  onToggleDropdown,
  onCloseDropdown,
  onOpenDeposit,
  onToggleFreeze,
  isFreezePending,
}: SavingsListItemProps) {
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
    <div className="p-4 px-6 flex flex-col md:flex-row md:items-center justify-between hover:bg-muted/30 transition-colors gap-4 relative">
      {/* Left: Avatar + Customer info */}
      <div className="flex items-center gap-4 min-w-[260px]">
        <div className="w-12 h-12 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center text-primary font-bold text-base flex-shrink-0">
          {getInitials(account.clientName)}
        </div>
        <div>
          <Link
            href={`/savings/${account.id}`}
            className="font-bold text-foreground hover:text-primary transition-colors text-base block"
          >
            {account.clientName}
          </Link>
          <p className="text-xs text-muted-foreground font-mono mt-0.5">
            {account.accountNo} {account.officeName ? `at ${account.officeName}` : ''}
          </p>
          <div className="flex items-center gap-2 mt-1">
            {isFrozen ? (
              <Badge variant="destructive" className="text-[10px] py-0 px-2 font-semibold">
                Frozen
              </Badge>
            ) : isActive ? (
              <Badge variant="success" className="text-[10px] py-0 px-2 font-semibold">
                Active
              </Badge>
            ) : isClosed ? (
              <Badge variant="secondary" className="text-[10px] py-0 px-2 font-semibold">
                Closed
              </Badge>
            ) : (
              <Badge variant="outline" className="text-[10px] py-0 px-2 font-semibold">
                {account.status?.value || 'Pending'}
              </Badge>
            )}
            {account.clientType && (
              <span className="text-[10px] text-muted-foreground uppercase font-medium bg-muted px-2 py-0.5 rounded">
                {account.clientType}
              </span>
            )}
          </div>
        </div>
      </div>

      {/* Middle / Right: Product & Dates + Dual Balance */}
      <div className="flex items-center gap-6 justify-between md:justify-end flex-grow">
        <div className="hidden lg:block text-left min-w-[140px]">
          <p className="text-sm font-medium text-foreground">
            {account.savingsProductName || 'FlexSave'}
          </p>
          <p className="text-xs text-muted-foreground mt-0.5 flex items-center gap-1">
            <Calendar className="w-3.5 h-3.5" />
            Opened: {formatDate(account.timeline?.activatedOnDate || account.timeline?.submittedOnDate)}
          </p>
        </div>

        <div className="text-left md:text-right min-w-[160px]">
          <p className="text-base font-bold font-mono text-foreground">
            {formatUGX(account.accountBalance)}
          </p>
          <p className="text-xs text-muted-foreground mt-0.5">
            Available:{' '}
            <span className="text-emerald-500 font-semibold font-mono">
              {formatUGX(account.availableBalance)}
            </span>
          </p>
        </div>

        {/* Actions: Deposit Button + Options Dropdown */}
        <div className="flex items-center gap-2 relative">
          <Button
            variant="outline"
            size="sm"
            disabled={isClosed}
            onClick={onOpenDeposit}
            className="border-primary text-primary hover:bg-primary hover:text-white transition-all text-xs font-semibold px-3"
          >
            Deposit
          </Button>

          {/* Dropdown Menu Toggle */}
          <div className="relative">
            <button
              onClick={onToggleDropdown}
              className="p-1.5 rounded-lg border border-border bg-background hover:bg-muted text-muted-foreground hover:text-foreground transition-colors"
              aria-label="Account options"
            >
              <MoreVertical className="w-4 h-4" />
            </button>

            {isOpenDropdown && (
              <>
                <div className="fixed inset-0 z-20" onClick={onCloseDropdown} />
                <div className="absolute right-0 top-full mt-1.5 w-48 rounded-lg border border-border bg-card shadow-xl p-1 z-30 text-xs">
                  <Link
                    href={`/savings/${account.id}`}
                    className="flex items-center gap-2 px-3 py-2 rounded-md hover:bg-muted text-foreground transition-colors font-medium"
                    onClick={onCloseDropdown}
                  >
                    <Wallet className="w-3.5 h-3.5 text-primary" />
                    Account Details
                  </Link>

                  <Link
                    href={`/savings/desk?account=${account.accountNo}&action=withdraw`}
                    className="flex items-center gap-2 px-3 py-2 rounded-md hover:bg-muted text-foreground transition-colors font-medium"
                    onClick={onCloseDropdown}
                  >
                    <ArrowDownLeft className="w-3.5 h-3.5 text-blue-500" />
                    Cash Desk Withdraw
                  </Link>

                  <button
                    onClick={() => onToggleFreeze(isFrozen)}
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

                  <Link
                    href={`/standing-orders?accountId=${account.id}`}
                    className="flex items-center gap-2 px-3 py-2 rounded-md hover:bg-muted text-foreground transition-colors font-medium"
                    onClick={onCloseDropdown}
                  >
                    <Clock className="w-3.5 h-3.5 text-purple-500" />
                    Standing Order
                  </Link>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
