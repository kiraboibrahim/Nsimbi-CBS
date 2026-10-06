'use client';

import React, { useState, useEffect } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { formatUGX } from '@/lib/formatters';
import { SavingsAccountSummary } from '@/types/savings';

interface SavingsDepositModalProps {
  isOpen: boolean;
  onClose: () => void;
  account: SavingsAccountSummary | null;
  onSubmit: (amount: number, note: string) => void;
  isPending: boolean;
}

export function SavingsDepositModal({
  isOpen,
  onClose,
  account,
  onSubmit,
  isPending,
}: SavingsDepositModalProps) {
  const [amount, setAmount] = useState('');
  const [note, setNote] = useState('');

  useEffect(() => {
    if (!isOpen) {
      setAmount('');
      setNote('');
    }
  }, [isOpen]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const parsed = parseFloat(amount);
    if (isNaN(parsed) || parsed <= 0) return;
    onSubmit(parsed, note);
  };

  if (!account) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Quick Cash Deposit">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="p-3 bg-muted/30 rounded-lg border border-border text-xs space-y-1">
          <p className="font-semibold text-foreground">{account.clientName}</p>
          <p className="font-mono text-muted-foreground">Account: {account.accountNo}</p>
          <div className="flex justify-between pt-1 border-t border-border mt-1">
            <span>Current Balance:</span>
            <span className="font-bold font-mono text-foreground">
              {formatUGX(account.accountBalance)}
            </span>
          </div>
        </div>

        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-foreground">
            Deposit Amount (UGX) <span className="text-primary">*</span>
          </label>
          <Input
            type="number"
            min="500"
            step="500"
            placeholder="e.g. 50000"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
            autoFocus
            className="font-mono"
          />
        </div>

        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-foreground">Transaction Note</label>
          <Input
            type="text"
            placeholder="Optional counter remarks"
            value={note}
            onChange={(e) => setNote(e.target.value)}
          />
        </div>

        <div className="flex justify-end gap-3 pt-3 border-t border-border">
          <Button
            type="button"
            variant="outline"
            onClick={onClose}
            disabled={isPending}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            disabled={isPending}
            className="bg-primary hover:bg-primary-hover text-white"
          >
            {isPending ? 'Processing...' : 'Confirm Deposit'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
