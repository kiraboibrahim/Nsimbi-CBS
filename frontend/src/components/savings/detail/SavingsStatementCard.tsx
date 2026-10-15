'use client';

import React, { useState } from 'react';
import { SavingsTransaction } from '@/types/savings';
import { PaginatedResponse } from '@/types/api';
import { formatUGX, formatDate } from '@/lib/formatters';
import { Button } from '@/components/ui/Button';
import {
  Wallet,
  ChevronDown,
  Printer,
  FileText,
  RefreshCw,
} from 'lucide-react';

interface SavingsStatementCardProps {
  transactions: PaginatedResponse<SavingsTransaction> | undefined;
  isLoading: boolean;
  statementFilter: string;
  onFilterChange: (filter: string) => void;
  onPrint: () => void;
}

const FILTER_OPTIONS = [
  { value: 'all', label: 'All Transactions' },
  { value: 'today', label: 'Today' },
  { value: '7d', label: 'Last 7 days' },
  { value: '28d', label: 'Last 28 days' },
  { value: '90d', label: 'Last 90 days' },
  { value: '180d', label: 'Last 180 days' },
];

export function SavingsStatementCard({
  transactions,
  isLoading,
  statementFilter,
  onFilterChange,
  onPrint,
}: SavingsStatementCardProps) {
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const txList = transactions?.pageItems || [];
  const currentLabel =
    FILTER_OPTIONS.find((o) => o.value === statementFilter)?.label || 'All Transactions';

  return (
    <div className="bg-card border border-border rounded-xl shadow-sm overflow-hidden">
      {/* Statement Header */}
      <div className="py-4 px-6 flex items-center justify-between border-b border-border flex-wrap gap-3">
        <div className="text-sm font-bold uppercase tracking-wider text-foreground flex items-center gap-2">
          <Wallet className="w-4 h-4 text-primary" />
          Account Statement
        </div>

        <div className="flex items-center gap-3">
          {/* Statement Date Range Selector */}
          <div className="relative">
            <button
              onClick={() => setDropdownOpen(!dropdownOpen)}
              className="text-xs font-semibold text-primary hover:text-primary-hover flex items-center gap-1 py-1.5 px-3 rounded-lg border border-primary/20 bg-primary/5 transition-colors"
            >
              <span>{currentLabel}</span>
              <ChevronDown className="w-3 h-3 ml-1" />
            </button>

            {dropdownOpen && (
              <>
                <div
                  className="fixed inset-0 z-20"
                  onClick={() => setDropdownOpen(false)}
                />
                <div className="absolute right-0 top-full mt-1.5 w-44 rounded-lg border border-border bg-card shadow-xl p-1 z-30 text-xs">
                  {FILTER_OPTIONS.map((opt) => (
                    <button
                      key={opt.value}
                      onClick={() => {
                        onFilterChange(opt.value);
                        setDropdownOpen(false);
                      }}
                      className={`w-full text-left px-3 py-1.5 rounded-md transition-colors ${
                        statementFilter === opt.value
                          ? 'bg-primary text-white font-semibold'
                          : 'hover:bg-muted text-foreground'
                      }`}
                    >
                      {opt.label}
                    </button>
                  ))}
                </div>
              </>
            )}
          </div>

          <Button
            variant="outline"
            size="sm"
            onClick={onPrint}
            className="text-xs gap-1.5 h-8 px-3"
          >
            <Printer className="w-3.5 h-3.5" />
            Print Statement
          </Button>
        </div>
      </div>

      {/* Transactions Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-xs text-left">
          <thead className="bg-muted/30 border-b border-border text-muted-foreground uppercase font-semibold">
            <tr>
              <th className="py-3 px-6">Trx ID</th>
              <th className="py-3 px-4">Date</th>
              <th className="py-3 px-4">Description / Type</th>
              <th className="py-3 px-4 text-right">Debit (UGX)</th>
              <th className="py-3 px-4 text-right">Credit (UGX)</th>
              <th className="py-3 px-6 text-right">Running Balance (UGX)</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {isLoading ? (
              <tr>
                <td colSpan={6} className="py-12 text-center text-muted-foreground">
                  <RefreshCw className="w-6 h-6 animate-spin mx-auto text-primary mb-2" />
                  Loading account ledger transactions...
                </td>
              </tr>
            ) : txList.length === 0 ? (
              <tr>
                <td colSpan={6} className="py-12 text-center text-muted-foreground">
                  <FileText className="w-8 h-8 mx-auto text-muted-foreground/40 mb-2" />
                  <p className="font-semibold text-foreground">No Transactions Found</p>
                  <p className="text-[11px] text-muted-foreground mt-0.5">
                    No transactions recorded for the selected date filter.
                  </p>
                </td>
              </tr>
            ) : (
              txList.map((tx) => {
                const isDeposit = tx.transactionType?.deposit;
                const isWithdrawal = tx.transactionType?.withdrawal;

                return (
                  <tr key={tx.id} className="hover:bg-muted/20 transition-colors">
                    <td className="py-3.5 px-6 font-mono text-muted-foreground">
                      #{tx.id}
                    </td>
                    <td className="py-3.5 px-4 font-mono">
                      {formatDate(tx.date || tx.submittedOnDate)}
                    </td>
                    <td className="py-3.5 px-4 font-medium text-foreground">
                      <span className="capitalize">
                        {tx.transactionType?.value || 'Transaction'}
                      </span>
                    </td>
                    <td className="py-3.5 px-4 text-right font-mono font-medium text-rose-500">
                      {isWithdrawal ? formatUGX(tx.amount) : '—'}
                    </td>
                    <td className="py-3.5 px-4 text-right font-mono font-medium text-emerald-500">
                      {isDeposit ? formatUGX(tx.amount) : '—'}
                    </td>
                    <td className="py-3.5 px-6 text-right font-mono font-bold text-foreground">
                      {formatUGX(tx.runningBalance)}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
