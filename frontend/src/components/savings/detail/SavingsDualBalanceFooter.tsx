'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { SavingsAccountSummary } from '@/types/savings';
import { formatUGX } from '@/lib/formatters';
import { Button } from '@/components/ui/Button';
import {
  ArrowUpRight,
  ArrowDownLeft,
  ChevronDown,
  Calendar,
} from 'lucide-react';

interface SavingsDualBalanceFooterProps {
  account: SavingsAccountSummary;
  onOpenDeposit: () => void;
  onOpenWithdraw: () => void;
}

export function SavingsDualBalanceFooter({
  account,
  onOpenDeposit,
  onOpenWithdraw,
}: SavingsDualBalanceFooterProps) {
  const [moreMenuOpen, setMoreMenuOpen] = useState(false);
  const isClosed = !!account.status?.closed;
  const isFrozen = !!account.subStatus?.block;

  return (
    <div className="flex flex-col sm:flex-row justify-between bg-muted/40 p-6 items-stretch sm:items-center rounded-b-xl border-t border-border gap-4">
      <div className="flex items-center gap-8">
        <div>
          <p className="text-2xl font-bold font-mono text-emerald-500">
            {formatUGX(account.availableBalance)}
          </p>
          <p className="text-xs text-muted-foreground uppercase font-semibold tracking-wider">
            Available Balance
          </p>
        </div>

        <div className="border-l border-border pl-8">
          <p className="text-xl font-bold font-mono text-foreground">
            {formatUGX(account.accountBalance)}
          </p>
          <p className="text-xs text-muted-foreground uppercase font-semibold tracking-wider">
            Actual Balance
          </p>
        </div>

        {account.onHoldFunds ? (
          <div className="border-l border-border pl-8 hidden md:block">
            <p className="text-base font-bold font-mono text-amber-500">
              {formatUGX(account.onHoldFunds)}
            </p>
            <p className="text-xs text-muted-foreground uppercase font-semibold tracking-wider">
              Reserved / Holds
            </p>
          </div>
        ) : null}
      </div>

      <div className="flex items-center gap-2 relative">
        <Button
          variant="outline"
          size="sm"
          disabled={isClosed}
          onClick={onOpenDeposit}
          className="border-primary text-primary hover:bg-primary hover:text-white transition-all text-xs font-semibold px-4 gap-1.5"
        >
          <ArrowUpRight className="w-3.5 h-3.5" />
          Deposit
        </Button>

        <Button
          variant="outline"
          size="sm"
          disabled={isClosed || isFrozen || account.availableBalance <= 0}
          onClick={onOpenWithdraw}
          className="border-border text-foreground hover:bg-muted transition-all text-xs font-semibold px-4 gap-1.5"
        >
          <ArrowDownLeft className="w-3.5 h-3.5 text-blue-500" />
          Withdraw
        </Button>

        {/* More Menu */}
        <div className="relative">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setMoreMenuOpen(!moreMenuOpen)}
            className="text-xs font-semibold gap-1 px-3"
          >
            More
            <ChevronDown className="w-3 h-3" />
          </Button>

          {moreMenuOpen && (
            <>
              <div className="fixed inset-0 z-20" onClick={() => setMoreMenuOpen(false)} />
              <div className="absolute right-0 bottom-full mb-1.5 w-48 rounded-lg border border-border bg-card shadow-xl p-1 z-30 text-xs">
                <Link
                  href={`/savings/desk?account=${account.accountNo}&action=deposit`}
                  className="flex items-center gap-2 px-3 py-2 rounded-md hover:bg-muted text-foreground transition-colors font-medium"
                  onClick={() => setMoreMenuOpen(false)}
                >
                  <ArrowUpRight className="w-3.5 h-3.5 text-emerald-500" />
                  Credit this Account
                </Link>

                <Link
                  href={`/savings/desk?account=${account.accountNo}&action=withdraw`}
                  className="flex items-center gap-2 px-3 py-2 rounded-md hover:bg-muted text-foreground transition-colors font-medium"
                  onClick={() => setMoreMenuOpen(false)}
                >
                  <ArrowDownLeft className="w-3.5 h-3.5 text-blue-500" />
                  Debit this Account
                </Link>

                <Link
                  href={`/standing-orders?accountId=${account.id}`}
                  className="flex items-center gap-2 px-3 py-2 rounded-md hover:bg-muted text-foreground transition-colors font-medium"
                  onClick={() => setMoreMenuOpen(false)}
                >
                  <Calendar className="w-3.5 h-3.5 text-purple-500" />
                  Add Standing Order
                </Link>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
