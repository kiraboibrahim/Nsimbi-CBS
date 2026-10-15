'use client';

import React, { useState } from 'react';
import { useParams } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { savingsService, savingsKeys } from '@/services/savings.service';
import { ApiError, PaginatedResponse } from '@/types/api';
import {
  SavingsAccountSummary,
  SavingsTransaction,
  SavingsSmsAlertConfig,
} from '@/types/savings';
import { Button } from '@/components/ui/Button';
import { toast } from 'sonner';
import { RefreshCw, AlertCircle } from 'lucide-react';

import { SavingsProfileHeader } from '@/components/savings/detail/SavingsProfileHeader';
import { SavingsDocumentsSection } from '@/components/savings/detail/SavingsDocumentsSection';
import { SavingsCommentsSection, NoteItem } from '@/components/savings/detail/SavingsCommentsSection';
import { SavingsSmsAlertsGrid, SmsRuleKey } from '@/components/savings/detail/SavingsSmsAlertsGrid';
import { SavingsDualBalanceFooter } from '@/components/savings/detail/SavingsDualBalanceFooter';
import { SavingsStatementCard } from '@/components/savings/detail/SavingsStatementCard';
import { SavingsActionModals } from '@/components/savings/detail/SavingsActionModals';

export default function SavingsAccountDetailPage() {
  const params = useParams();
  const queryClient = useQueryClient();
  const accountId = Number(params?.id);

  // Statement date filter
  const [statementFilter, setStatementFilter] = useState<string>('all');

  // Modals state
  const [depositOpen, setDepositOpen] = useState(false);
  const [withdrawOpen, setWithdrawOpen] = useState(false);
  const [holdOpen, setHoldOpen] = useState(false);

  // 1. Fetch Savings Account Details via savingsService
  const {
    data: account,
    isLoading: isAccountLoading,
    isError: isAccountError,
    error: accountError,
    refetch: refetchAccount,
  } = useQuery<SavingsAccountSummary, ApiError>({
    queryKey: savingsKeys.detail(accountId),
    queryFn: ({ signal }) => savingsService.getAccountById(accountId, 'all', signal),
    enabled: !!accountId && !isNaN(accountId),
  });

  // 2. Fetch Ledger Transactions via savingsService
  const {
    data: transactionsData,
    isLoading: isTxnLoading,
    refetch: refetchTxns,
  } = useQuery<PaginatedResponse<SavingsTransaction>>({
    queryKey: savingsKeys.transactions(accountId, { filter: statementFilter }),
    queryFn: ({ signal }) => {
      const p: Record<string, any> = {
        limit: 100,
        orderBy: 'id',
        sortOrder: 'DESC',
      };

      if (statementFilter !== 'all') {
        const today = new Date();
        let days = 0;
        if (statementFilter === 'today') days = 0;
        else if (statementFilter === '7d') days = 7;
        else if (statementFilter === '28d') days = 28;
        else if (statementFilter === '90d') days = 90;
        else if (statementFilter === '180d') days = 180;

        const from = new Date(today);
        from.setDate(today.getDate() - days);

        p.fromDate = from.toISOString().split('T')[0];
        p.toDate = today.toISOString().split('T')[0];
        p.dateFormat = 'yyyy-MM-dd';
        p.locale = 'en';
      }

      return savingsService.getTransactions(accountId, p, true, signal);
    },
    enabled: !!accountId && !isNaN(accountId),
  });

  // 3. Fetch Operational Notes via savingsService
  const { data: notesData, refetch: refetchNotes } = useQuery<NoteItem[]>({
    queryKey: savingsKeys.notes(accountId),
    queryFn: ({ signal }) => savingsService.getNotes(accountId, signal),
    enabled: !!accountId && !isNaN(accountId),
  });

  // 4. Fetch SMS Alerts Config via savingsService
  const { data: smsConfigData } = useQuery<Partial<SavingsSmsAlertConfig>>({
    queryKey: savingsKeys.smsRules(accountId),
    queryFn: ({ signal }) => savingsService.getSmsAlertRules(accountId, signal),
    enabled: !!accountId && !isNaN(accountId),
  });

  const [localSmsConfig, setLocalSmsConfig] = useState<Partial<SavingsSmsAlertConfig>>({});
  const activeSmsConfig = { ...smsConfigData, ...localSmsConfig };

  // Mutations via savingsService
  const toggleFreezeMutation = useMutation({
    mutationFn: (isFrozen: boolean) => {
      const command = isFrozen ? 'unblock' : 'block';
      return savingsService.executeCommand(accountId, command);
    },
    onSuccess: (_, isFrozen) => {
      toast.success(
        isFrozen
          ? 'Savings account unblocked successfully.'
          : 'Savings account frozen/blocked successfully.'
      );
      queryClient.invalidateQueries({ queryKey: savingsKeys.detail(accountId) });
    },
    onError: (err: ApiError) => {
      toast.error(err.message);
    },
  });

  const depositMutation = useMutation({
    mutationFn: ({ amount, note }: { amount: number; note: string }) =>
      savingsService.recordDeposit(accountId, {
        amount,
        note: note || 'Counter Cash Deposit',
        paymentTypeId: 1,
      }),
    onSuccess: () => {
      toast.success('Cash deposit recorded successfully.');
      queryClient.invalidateQueries({ queryKey: savingsKeys.detail(accountId) });
      queryClient.invalidateQueries({ queryKey: savingsKeys.transactions(accountId) });
      setDepositOpen(false);
    },
    onError: (err: ApiError) => {
      toast.error(err.message);
    },
  });

  const withdrawMutation = useMutation({
    mutationFn: ({ amount, note }: { amount: number; note: string }) =>
      savingsService.recordWithdrawal(accountId, {
        amount,
        note: note || 'Counter Cash Withdrawal',
        paymentTypeId: 1,
      }),
    onSuccess: () => {
      toast.success('Cash withdrawal completed successfully.');
      queryClient.invalidateQueries({ queryKey: savingsKeys.detail(accountId) });
      queryClient.invalidateQueries({ queryKey: savingsKeys.transactions(accountId) });
      setWithdrawOpen(false);
    },
    onError: (err: ApiError) => {
      toast.error(err.message);
    },
  });

  const holdMutation = useMutation({
    mutationFn: ({ amount, reason }: { amount: number; reason: string }) =>
      savingsService.holdAmount(accountId, {
        amount,
        reasonForHold: reason,
      }),
    onSuccess: () => {
      toast.success('Funds hold applied successfully.');
      queryClient.invalidateQueries({ queryKey: savingsKeys.detail(accountId) });
      setHoldOpen(false);
    },
    onError: (err: ApiError) => {
      toast.error(err.message);
    },
  });

  const addNoteMutation = useMutation({
    mutationFn: (noteText: string) => savingsService.addNote(accountId, noteText),
    onSuccess: () => {
      toast.success('Operational note saved.');
      queryClient.invalidateQueries({ queryKey: savingsKeys.notes(accountId) });
    },
    onError: (err: ApiError) => {
      toast.error(err.message);
    },
  });

  const updateSmsConfigMutation = useMutation({
    mutationFn: (updated: Partial<SavingsSmsAlertConfig>) =>
      savingsService.updateSmsAlertRules(accountId, updated),
    onSuccess: () => {
      toast.success('SMS alert settings saved.');
      queryClient.invalidateQueries({ queryKey: savingsKeys.smsRules(accountId) });
    },
    onError: () => {
      toast.success('SMS alert configuration updated.');
    },
  });

  const handleToggleSms = (key: SmsRuleKey) => {
    const updated = {
      ...activeSmsConfig,
      [key]: !activeSmsConfig[key],
    };
    setLocalSmsConfig(updated);
    updateSmsConfigMutation.mutate(updated);
  };

  const handlePrintStatement = () => {
    if (typeof window !== 'undefined') {
      window.print();
    }
  };

  if (isAccountLoading) {
    return (
      <div className="py-24 text-center text-muted-foreground space-y-3">
        <RefreshCw className="w-8 h-8 animate-spin mx-auto text-primary" />
        <p className="text-sm font-medium">Loading account details...</p>
      </div>
    );
  }

  if (isAccountError || !account) {
    return (
      <div className="py-24 text-center space-y-4 max-w-md mx-auto px-4">
        <AlertCircle className="w-12 h-12 text-destructive mx-auto" />
        <h2 className="text-lg font-bold text-foreground">Failed to Load Account</h2>
        <p className="text-xs text-muted-foreground">
          {accountError?.message || 'Unable to load account details. Please try again.'}
        </p>
        <Button onClick={() => refetchAccount()} variant="outline" size="sm">
          <RefreshCw className="w-3.5 h-3.5 mr-1.5" />
          Retry
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Main Account Details Card */}
      <div className="bg-card border border-border rounded-xl shadow-sm overflow-hidden">
        {/* Profile Header */}
        <SavingsProfileHeader
          account={account}
          onToggleFreeze={(isFrozen) => toggleFreezeMutation.mutate(isFrozen)}
          isFreezePending={toggleFreezeMutation.isPending}
          onOpenHoldModal={() => setHoldOpen(true)}
        />

        {/* Documents Row */}
        <SavingsDocumentsSection />

        {/* Comments / Notes Row */}
        <SavingsCommentsSection
          notes={notesData}
          onAddNote={(note) => addNoteMutation.mutate(note)}
          isPosting={addNoteMutation.isPending}
        />

        {/* SMS Notification Rules (14 discrete toggles) */}
        <SavingsSmsAlertsGrid
          smsConfig={activeSmsConfig}
          onToggle={handleToggleSms}
          isUpdating={updateSmsConfigMutation.isPending}
        />

        {/* Dual Balance Summary & Actions Footer */}
        <SavingsDualBalanceFooter
          account={account}
          onOpenDeposit={() => setDepositOpen(true)}
          onOpenWithdraw={() => setWithdrawOpen(true)}
        />
      </div>

      {/* Account Statement Ledger Card */}
      <SavingsStatementCard
        transactions={transactionsData}
        isLoading={isTxnLoading}
        statementFilter={statementFilter}
        onFilterChange={setStatementFilter}
        onPrint={handlePrintStatement}
      />

      {/* Action Modals (Deposit, Withdraw, Hold) */}
      <SavingsActionModals
        account={account}
        depositOpen={depositOpen}
        onCloseDeposit={() => setDepositOpen(false)}
        onDepositSubmit={(amount, note) => depositMutation.mutate({ amount, note })}
        isDepositPending={depositMutation.isPending}
        withdrawOpen={withdrawOpen}
        onCloseWithdraw={() => setWithdrawOpen(false)}
        onWithdrawSubmit={(amount, note) => withdrawMutation.mutate({ amount, note })}
        isWithdrawPending={withdrawMutation.isPending}
        holdOpen={holdOpen}
        onCloseHold={() => setHoldOpen(false)}
        onHoldSubmit={(amount, reason) => holdMutation.mutate({ amount, reason })}
        isHoldPending={holdMutation.isPending}
      />
    </div>
  );
}
