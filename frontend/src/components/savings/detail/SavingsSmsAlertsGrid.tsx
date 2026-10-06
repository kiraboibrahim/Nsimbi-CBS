'use client';

import React from 'react';
import { SavingsSmsAlertConfig } from '@/types/savings';
import { Bell, CheckCircle2, XCircle } from 'lucide-react';

interface SavingsSmsAlertsGridProps {
  smsConfig: Partial<SavingsSmsAlertConfig>;
  onToggle: (key: keyof Omit<SavingsSmsAlertConfig, 'id' | 'savingsAccountId'>) => void;
  isUpdating: boolean;
}

export type SmsRuleKey = keyof Omit<SavingsSmsAlertConfig, 'id' | 'savingsAccountId'>;

const SMS_ALERT_ITEMS: { key: SmsRuleKey; label: string }[] = [
  { key: 'notifyOnDeposit', label: 'Cash Deposit' },
  { key: 'notifyOnWithdrawal', label: 'Cash Withdrawal' },
  { key: 'notifyOnEarningInterest', label: 'Interest Posting' },
  { key: 'notifyOnEarningDividends', label: 'Dividends Posting' },
  { key: 'notifyOnOutgoingTransfer', label: 'Outgoing Transfer' },
  { key: 'notifyOnIncomingTransfer', label: 'Incoming Transfer' },
  { key: 'notifyOnDirectDebit', label: 'Direct Debit' },
  { key: 'notifyOnDirectCredit', label: 'Direct Credit' },
  { key: 'notifyOnStandingOrder', label: 'Standing Orders' },
  { key: 'notifyOnLoanApplication', label: 'Loan Application' },
  { key: 'notifyOnLoanDisbursement', label: 'Loan Disbursement' },
  { key: 'notifyOnLoanPayment', label: 'Loan Repayment' },
  { key: 'notifyOnLoanRefund', label: 'Loan Excess Refund' },
  { key: 'notifyOnHold', label: 'Funds Hold / Lien' },
];

export function SavingsSmsAlertsGrid({
  smsConfig,
  onToggle,
  isUpdating,
}: SavingsSmsAlertsGridProps) {
  return (
    <div className="w-full px-6 py-4 border-t border-border bg-card shadow-inner space-y-2.5">
      <div className="flex items-center justify-between">
        <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground flex items-center gap-1.5">
          <Bell className="w-3.5 h-3.5 text-primary" />
          SMS Notification Alert Rules (14 discrete toggles)
        </div>
        <span className="text-[10px] text-muted-foreground">Click any rule to toggle</span>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-2.5 pt-1">
        {SMS_ALERT_ITEMS.map(({ key, label }) => {
          const isEnabled = Boolean(smsConfig[key]);
          return (
            <button
              key={key}
              type="button"
              onClick={() => onToggle(key)}
              disabled={isUpdating}
              className={`flex items-center justify-between p-2 rounded-lg border text-xs font-medium transition-all text-left ${
                isEnabled
                  ? 'border-emerald-500/30 bg-emerald-500/5 text-foreground hover:bg-emerald-500/10'
                  : 'border-border bg-muted/20 text-muted-foreground hover:bg-muted/40'
              }`}
            >
              <span className="truncate pr-1">{label}</span>
              {isEnabled ? (
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-500 flex-shrink-0" />
              ) : (
                <XCircle className="w-3.5 h-3.5 text-muted-foreground/60 flex-shrink-0" />
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
}
