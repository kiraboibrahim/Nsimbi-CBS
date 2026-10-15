'use client';

import React, { useState } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { formatUGX } from '@/lib/formatters';
import { SavingsAccountSummary } from '@/types/savings';

interface SavingsActionModalsProps {
  account: SavingsAccountSummary;
  // Deposit
  depositOpen: boolean;
  onCloseDeposit: () => void;
  onDepositSubmit: (amount: number, note: string) => void;
  isDepositPending: boolean;
  // Withdraw
  withdrawOpen: boolean;
  onCloseWithdraw: () => void;
  onWithdrawSubmit: (amount: number, note: string) => void;
  isWithdrawPending: boolean;
  // Hold
  holdOpen: boolean;
  onCloseHold: () => void;
  onHoldSubmit: (amount: number, reason: string) => void;
  isHoldPending: boolean;
}

export function SavingsActionModals({
  account,
  depositOpen,
  onCloseDeposit,
  onDepositSubmit,
  isDepositPending,
  withdrawOpen,
  onCloseWithdraw,
  onWithdrawSubmit,
  isWithdrawPending,
  holdOpen,
  onCloseHold,
  onHoldSubmit,
  isHoldPending,
}: SavingsActionModalsProps) {
  // Local form states
  const [depositAmount, setDepositAmount] = useState('');
  const [depositNote, setDepositNote] = useState('');

  const [withdrawAmount, setWithdrawAmount] = useState('');
  const [withdrawNote, setWithdrawNote] = useState('');

  const [holdAmount, setHoldAmount] = useState('');
  const [holdReason, setHoldReason] = useState('');

  return (
    <>
      {/* 1. Deposit Modal */}
      <Modal isOpen={depositOpen} onClose={onCloseDeposit} title="Deposit Funds to Account">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const amt = parseFloat(depositAmount);
            if (isNaN(amt) || amt <= 0) return;
            onDepositSubmit(amt, depositNote);
            setDepositAmount('');
            setDepositNote('');
          }}
          className="space-y-4"
        >
          <div className="p-3 bg-muted/30 rounded-lg border border-border text-xs space-y-1">
            <p className="font-semibold text-foreground">{account.clientName}</p>
            <p className="font-mono text-muted-foreground">Account: {account.accountNo}</p>
            <div className="flex justify-between pt-1 border-t border-border mt-1">
              <span>Current Available:</span>
              <span className="font-bold font-mono text-emerald-500">
                {formatUGX(account.availableBalance)}
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
              value={depositAmount}
              onChange={(e) => setDepositAmount(e.target.value)}
              required
              autoFocus
              className="font-mono"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">Transaction Note</label>
            <Input
              type="text"
              placeholder="Counter deposit remarks"
              value={depositNote}
              onChange={(e) => setDepositNote(e.target.value)}
            />
          </div>

          <div className="flex justify-end gap-3 pt-3 border-t border-border">
            <Button
              type="button"
              variant="outline"
              onClick={onCloseDeposit}
              disabled={isDepositPending}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={isDepositPending}
              className="bg-primary hover:bg-primary-hover text-white"
            >
              {isDepositPending ? 'Processing...' : 'Confirm Deposit'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* 2. Withdraw Modal */}
      <Modal isOpen={withdrawOpen} onClose={onCloseWithdraw} title="Withdraw Cash from Account">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const amt = parseFloat(withdrawAmount);
            if (isNaN(amt) || amt <= 0) return;
            onWithdrawSubmit(amt, withdrawNote);
            setWithdrawAmount('');
            setWithdrawNote('');
          }}
          className="space-y-4"
        >
          <div className="p-3 bg-muted/30 rounded-lg border border-border text-xs space-y-1">
            <p className="font-semibold text-foreground">{account.clientName}</p>
            <p className="font-mono text-muted-foreground">Account: {account.accountNo}</p>
            <div className="flex justify-between pt-1 border-t border-border mt-1">
              <span>Max Withdrawable (after UGX 5,000 reserve):</span>
              <span className="font-bold font-mono text-emerald-500">
                {formatUGX(account.availableBalance)}
              </span>
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">
              Withdrawal Amount (UGX) <span className="text-primary">*</span>
            </label>
            <Input
              type="number"
              min="500"
              max={account.availableBalance}
              step="500"
              placeholder="e.g. 20000"
              value={withdrawAmount}
              onChange={(e) => setWithdrawAmount(e.target.value)}
              required
              autoFocus
              className="font-mono"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">Counter Reason / Note</label>
            <Input
              type="text"
              placeholder="Counter cash payout"
              value={withdrawNote}
              onChange={(e) => setWithdrawNote(e.target.value)}
            />
          </div>

          <div className="flex justify-end gap-3 pt-3 border-t border-border">
            <Button
              type="button"
              variant="outline"
              onClick={onCloseWithdraw}
              disabled={isWithdrawPending}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={isWithdrawPending}
              className="bg-primary hover:bg-primary-hover text-white"
            >
              {isWithdrawPending ? 'Processing...' : 'Confirm Withdrawal'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* 3. Funds Hold Modal */}
      <Modal isOpen={holdOpen} onClose={onCloseHold} title="Place Funds Hold on Account">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const amt = parseFloat(holdAmount);
            if (isNaN(amt) || amt <= 0) return;
            onHoldSubmit(amt, holdReason);
            setHoldAmount('');
            setHoldReason('');
          }}
          className="space-y-4"
        >
          <div className="p-3 bg-muted/30 rounded-lg border border-border text-xs space-y-1">
            <p className="font-semibold text-foreground">{account.clientName}</p>
            <p className="font-mono text-muted-foreground">Account: {account.accountNo}</p>
            <div className="flex justify-between pt-1 border-t border-border mt-1">
              <span>Current Available Balance:</span>
              <span className="font-bold font-mono text-emerald-500">
                {formatUGX(account.availableBalance)}
              </span>
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">
              Hold Amount (UGX) <span className="text-primary">*</span>
            </label>
            <Input
              type="number"
              min="1000"
              max={account.availableBalance}
              placeholder="e.g. 50000"
              value={holdAmount}
              onChange={(e) => setHoldAmount(e.target.value)}
              required
              autoFocus
              className="font-mono"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">
              Reason for Hold <span className="text-primary">*</span>
            </label>
            <Input
              type="text"
              placeholder="e.g. Loan collateral guarantee, pending legal verification"
              value={holdReason}
              onChange={(e) => setHoldReason(e.target.value)}
              required
            />
          </div>

          <div className="flex justify-end gap-3 pt-3 border-t border-border">
            <Button
              type="button"
              variant="outline"
              onClick={onCloseHold}
              disabled={isHoldPending}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={isHoldPending}
              className="bg-amber-600 hover:bg-amber-700 text-white"
            >
              {isHoldPending ? 'Applying Hold...' : 'Confirm Hold'}
            </Button>
          </div>
        </form>
      </Modal>
    </>
  );
}
