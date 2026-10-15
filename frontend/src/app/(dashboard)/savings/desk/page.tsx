'use client';

import React, { useState, Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import { useAuth } from '@/context/auth-context';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { savingsService, savingsKeys } from '@/services/savings.service';
import { ApiError, PaginatedResponse } from '@/types/api';
import { formatUGX, formatAccountNumber, formatDateTime } from '@/lib/formatters';
import { SavingsAccountSummary } from '@/types/savings';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Card, CardContent } from '@/components/ui/Card';
import { Badge } from '@/components/ui/Badge';
import { Modal } from '@/components/ui/Modal';
import { SavingsNav } from '@/components/savings/SavingsNav';
import { toast } from 'sonner';
import {
  ArrowLeftRight,
  ArrowDownLeft,
  ArrowUpRight,
  Search,
  CheckCircle2,
  Printer,
  ShieldAlert,
  Coins,
  Receipt,
  UserCheck,
  RefreshCw,
  Wallet,
} from 'lucide-react';

const MINIMUM_RESERVE_UGX = 5000;

interface CompletedReceipt {
  ref: string;
  type: 'Deposit' | 'Withdrawal';
  accountNo: string;
  memberName: string;
  amount: number;
  newBalance: number;
  teller: string;
  date: string;
  paymentTypeId: number;
  channel: string;
}

function CashDeskTerminal() {
  const searchParams = useSearchParams();
  const initialAccount = searchParams.get('account') || '';
  const initialAction = (searchParams.get('action') as 'deposit' | 'withdraw') || 'deposit';

  const queryClient = useQueryClient();
  const { tillCash, setTillCash, user } = useAuth();
  const [query, setQuery] = useState(initialAccount);
  const [selectedAccount, setSelectedAccount] = useState<SavingsAccountSummary | null>(null);
  const [activeTab, setActiveTab] = useState<'deposit' | 'withdraw'>(initialAction);
  const [amountStr, setAmountStr] = useState('');
  const [depositorName, setDepositorName] = useState('');
  const [note, setNote] = useState('');
  const [receipt, setReceipt] = useState<CompletedReceipt | null>(null);

  // Search accounts query via savingsService with cancellation support
  const { data: accountsData, isLoading: isSearchLoading } = useQuery<PaginatedResponse<SavingsAccountSummary>>({
    queryKey: ['cash-desk-search', query],
    queryFn: ({ signal }) =>
      savingsService.getAccounts(
        {
          limit: 10,
          orderBy: 'id',
          sortOrder: 'DESC',
        },
        signal
      ),
  });

  // If initialAccount query param was passed and not selected yet, find it
  React.useEffect(() => {
    if (initialAccount && accountsData?.pageItems && !selectedAccount) {
      const match = accountsData.pageItems.find(
        (a) => a.accountNo === initialAccount || String(a.id) === initialAccount
      );
      if (match) {
        setSelectedAccount(match);
        setDepositorName(match.clientName);
      }
    }
  }, [initialAccount, accountsData, selectedAccount]);

  const parsedAmount = parseFloat(amountStr) || 0;

  const handleSelectAccount = (acc: SavingsAccountSummary) => {
    setSelectedAccount(acc);
    setAmountStr('');
    setDepositorName(acc.clientName);
  };

  // Transaction Mutation against Fineract via savingsService
  const transactionMutation = useMutation({
    mutationFn: ({
      accountId,
      type,
      amount,
      noteText,
    }: {
      accountId: number;
      type: 'deposit' | 'withdrawal';
      amount: number;
      noteText: string;
    }) => {
      const payload = {
        amount,
        note: noteText || `Counter Cash ${type === 'deposit' ? 'Deposit' : 'Withdrawal'}`,
        paymentTypeId: 1, // Cash
      };
      return type === 'deposit'
        ? savingsService.recordDeposit(accountId, payload)
        : savingsService.recordWithdrawal(accountId, payload);
    },
    onSuccess: (data, variables) => {
      if (!selectedAccount) return;
      const isDeposit = variables.type === 'deposit';

      // Update local till cash
      if (isDeposit) {
        setTillCash((prev) => prev + variables.amount);
      } else {
        setTillCash((prev) => prev - variables.amount);
      }

      const newActualBalance = isDeposit
        ? selectedAccount.accountBalance + variables.amount
        : selectedAccount.accountBalance - variables.amount;

      const newAvailableBalance = isDeposit
        ? selectedAccount.availableBalance + variables.amount
        : selectedAccount.availableBalance - variables.amount;

      // Update selected account state
      setSelectedAccount({
        ...selectedAccount,
        accountBalance: newActualBalance,
        availableBalance: newAvailableBalance,
      });

      const txRef = data?.resourceId ? `TXN-${data.resourceId}` : `TXN-${Date.now().toString().slice(-6)}`;

      setReceipt({
        ref: txRef,
        type: isDeposit ? 'Deposit' : 'Withdrawal',
        accountNo: selectedAccount.accountNo,
        memberName: selectedAccount.clientName,
        amount: variables.amount,
        newBalance: newActualBalance,
        teller: user?.username || 'Cash Desk Teller',
        date: new Date().toISOString(),
        paymentTypeId: 1,
        channel: 'Cash Desk Counter',
      });

      toast.success(
        `${variables.type === 'deposit' ? 'Deposit' : 'Withdrawal'} of ${formatUGX(
          variables.amount
        )} completed successfully.`
      );

      queryClient.invalidateQueries({ queryKey: savingsKeys.lists() });
      queryClient.invalidateQueries({ queryKey: savingsKeys.detail(selectedAccount.id) });
      queryClient.invalidateQueries({ queryKey: ['cash-desk-search'] });
      setAmountStr('');
      setNote('');
    },
    onError: (err: ApiError) => {
      toast.error(err.message);
    },
  });

  const executeTransaction = () => {
    if (!selectedAccount) {
      toast.error('Please select an account first.');
      return;
    }
    if (parsedAmount <= 0) {
      toast.error('Please enter a valid amount.');
      return;
    }

    if (activeTab === 'withdraw') {
      if (parsedAmount > selectedAccount.availableBalance) {
        toast.error(
          `Insufficient available funds. Members must maintain a minimum reserve balance of ${formatUGX(
            MINIMUM_RESERVE_UGX
          )}.`
        );
        return;
      }
      if (parsedAmount > tillCash) {
        toast.error('Insufficient cash in your teller drawer.');
        return;
      }
    }

    transactionMutation.mutate({
      accountId: selectedAccount.id,
      type: activeTab === 'deposit' ? 'deposit' : 'withdrawal',
      amount: parsedAmount,
      noteText: note,
    });
  };

  const accounts = accountsData?.pageItems || [];
  const searchResults = accounts.filter((acc) => {
    if (!query.trim()) return true;
    const q = query.toLowerCase();
    return (
      acc.accountNo.toLowerCase().includes(q) ||
      acc.clientName.toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-6">
      {/* Module Navigation */}
      <SavingsNav />

      {/* Header with Till Float */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-border pb-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <ArrowLeftRight className="w-5 h-5 text-primary" />
            Cash Desk Terminal
          </h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            Process real counter cash deposits and withdrawals with automated dual-balance calculation.
          </p>
        </div>

        {/* Teller Drawer / Float Status */}
        <div className="flex items-center gap-3 bg-card border border-border rounded-xl px-4 py-2.5 shadow-sm">
          <div className="w-9 h-9 rounded-lg bg-emerald-500/10 text-emerald-500 flex items-center justify-center font-bold">
            <Coins className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground block">
              Teller Drawer Cash
            </span>
            <span className="text-base font-bold font-mono text-emerald-500">
              {formatUGX(tillCash)}
            </span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Account Search & Selector */}
        <div className="lg:col-span-4 space-y-4">
          <Card className="bg-card border-border shadow-sm">
            <CardContent className="p-4 space-y-3">
              <label className="text-xs font-semibold text-foreground uppercase tracking-wider block">
                Find Member Account
              </label>
              <div className="relative">
                <Search className="w-4 h-4 text-muted-foreground absolute left-3 top-2.5" />
                <Input
                  type="text"
                  placeholder="Account no or member name..."
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  className="pl-9 text-xs"
                />
              </div>

              <div className="divide-y divide-border max-h-96 overflow-y-auto pt-1">
                {isSearchLoading ? (
                  <div className="py-8 text-center text-xs text-muted-foreground space-y-2">
                    <RefreshCw className="w-4 h-4 animate-spin mx-auto text-primary" />
                    <p>Loading accounts...</p>
                  </div>
                ) : searchResults.length === 0 ? (
                  <div className="py-8 text-center text-xs text-muted-foreground space-y-1">
                    <p className="font-semibold text-foreground">No accounts found</p>
                    <p>Register a member in the system to begin.</p>
                  </div>
                ) : (
                  searchResults.map((acc) => {
                    const isSelected = selectedAccount?.id === acc.id;
                    const isFrozen = !!acc.subStatus?.block;
                    return (
                      <button
                        key={acc.id}
                        type="button"
                        onClick={() => handleSelectAccount(acc)}
                        className={`w-full text-left p-3 rounded-lg transition-all flex items-center justify-between gap-2 ${
                          isSelected
                            ? 'bg-primary/10 border border-primary/30 text-foreground'
                            : 'hover:bg-muted/40 text-muted-foreground'
                        }`}
                      >
                        <div className="truncate">
                          <p className="font-semibold text-xs text-foreground truncate">
                            {acc.clientName}
                          </p>
                          <p className="text-[11px] font-mono text-muted-foreground">
                            {formatAccountNumber(acc.accountNo)}
                          </p>
                        </div>
                        <div className="text-right flex-shrink-0">
                          <p className="text-xs font-bold font-mono text-foreground">
                            {formatUGX(acc.accountBalance)}
                          </p>
                          {isFrozen ? (
                            <Badge variant="destructive" className="text-[9px] py-0 px-1.5">
                              Frozen
                            </Badge>
                          ) : (
                            <span className="text-[10px] text-emerald-500 font-mono">
                              Avail: {formatUGX(acc.availableBalance)}
                            </span>
                          )}
                        </div>
                      </button>
                    );
                  })
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right Column: Transaction Terminal */}
        <div className="lg:col-span-8 space-y-6">
          {selectedAccount ? (
            <>
              {/* Selected Account Overview Card with Dual Balance */}
              <div className="bg-card border border-border rounded-xl p-5 shadow-sm space-y-4">
                <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3 border-b border-border pb-4">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center text-primary font-bold text-base">
                      {selectedAccount.clientName ? selectedAccount.clientName.slice(0, 2).toUpperCase() : 'SA'}
                    </div>
                    <div>
                      <h2 className="text-base font-bold text-foreground">
                        {selectedAccount.clientName}
                      </h2>
                      <p className="text-xs text-muted-foreground font-mono">
                        Account No: {selectedAccount.accountNo} • {selectedAccount.savingsProductName || 'FlexSave'}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    {selectedAccount.subStatus?.block ? (
                      <Badge variant="destructive">Account Frozen</Badge>
                    ) : (
                      <Badge variant="success">Account Active</Badge>
                    )}
                  </div>
                </div>

                {/* Dual-Balance Matrix */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  <div className="p-3.5 bg-muted/20 border border-border rounded-lg space-y-1">
                    <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground block">
                      Actual Balance
                    </span>
                    <span className="text-lg font-bold font-mono text-foreground block">
                      {formatUGX(selectedAccount.accountBalance)}
                    </span>
                    <span className="text-[10px] text-muted-foreground">Ledger book balance</span>
                  </div>

                  <div className="p-3.5 bg-emerald-500/5 border border-emerald-500/20 rounded-lg space-y-1">
                    <span className="text-[10px] uppercase font-bold tracking-wider text-emerald-600 dark:text-emerald-400 block">
                      Available Balance
                    </span>
                    <span className="text-lg font-bold font-mono text-emerald-500 block">
                      {formatUGX(selectedAccount.availableBalance)}
                    </span>
                    <span className="text-[10px] text-muted-foreground">Liquid funds for withdrawal</span>
                  </div>

                  <div className="p-3.5 bg-muted/20 border border-border rounded-lg space-y-1">
                    <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground block">
                      Minimum Reserve
                    </span>
                    <span className="text-lg font-bold font-mono text-amber-500 block">
                      {formatUGX(selectedAccount.minRequiredBalance || MINIMUM_RESERVE_UGX)}
                    </span>
                    <span className="text-[10px] text-muted-foreground">Protected statutory reserve</span>
                  </div>
                </div>

                {selectedAccount.onHoldFunds ? (
                  <div className="p-2.5 bg-amber-500/10 border border-amber-500/20 rounded-lg flex items-center gap-2 text-xs text-amber-600 dark:text-amber-400">
                    <ShieldAlert className="w-4 h-4 flex-shrink-0" />
                    <span>
                      This account has <strong>{formatUGX(selectedAccount.onHoldFunds)}</strong> placed on hold/lien.
                    </span>
                  </div>
                ) : null}
              </div>

              {/* Action Form Card */}
              <div className="bg-card border border-border rounded-xl p-5 shadow-sm space-y-5">
                {/* Tabs: Deposit vs Withdraw */}
                <div className="flex border-b border-border">
                  <button
                    type="button"
                    onClick={() => setActiveTab('deposit')}
                    className={`flex-1 py-3 text-xs font-bold uppercase tracking-wider flex items-center justify-center gap-2 border-b-2 transition-colors ${
                      activeTab === 'deposit'
                        ? 'border-primary text-primary'
                        : 'border-transparent text-muted-foreground hover:text-foreground'
                    }`}
                  >
                    <ArrowUpRight className="w-4 h-4" />
                    Cash Deposit
                  </button>

                  <button
                    type="button"
                    onClick={() => setActiveTab('withdraw')}
                    className={`flex-1 py-3 text-xs font-bold uppercase tracking-wider flex items-center justify-center gap-2 border-b-2 transition-colors ${
                      activeTab === 'withdraw'
                        ? 'border-primary text-primary'
                        : 'border-transparent text-muted-foreground hover:text-foreground'
                    }`}
                  >
                    <ArrowDownLeft className="w-4 h-4" />
                    Cash Withdrawal
                  </button>
                </div>

                <div className="space-y-4">
                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-foreground">
                      Transaction Amount (UGX) <span className="text-primary">*</span>
                    </label>
                    <Input
                      type="number"
                      min="500"
                      step="500"
                      placeholder="e.g. 50000"
                      value={amountStr}
                      onChange={(e) => setAmountStr(e.target.value)}
                      className="font-mono text-base py-2"
                      required
                    />
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div className="space-y-1.5">
                      <label className="text-xs font-semibold text-foreground">
                        {activeTab === 'deposit' ? 'Depositor / Presenter Name' : 'Withdrawal Beneficiary'}
                      </label>
                      <Input
                        type="text"
                        value={depositorName}
                        onChange={(e) => setDepositorName(e.target.value)}
                        placeholder="Person transacting at counter"
                      />
                    </div>

                    <div className="space-y-1.5">
                      <label className="text-xs font-semibold text-foreground">
                        Teller Remarks / Narration
                      </label>
                      <Input
                        type="text"
                        value={note}
                        onChange={(e) => setNote(e.target.value)}
                        placeholder="Optional counter remarks"
                      />
                    </div>
                  </div>

                  {/* Summary preview */}
                  {parsedAmount > 0 && (
                    <div className="p-3 bg-muted/30 border border-border rounded-lg text-xs space-y-1">
                      <div className="flex justify-between text-muted-foreground">
                        <span>New Actual Balance:</span>
                        <span className="font-mono font-bold text-foreground">
                          {formatUGX(
                            activeTab === 'deposit'
                              ? selectedAccount.accountBalance + parsedAmount
                              : selectedAccount.accountBalance - parsedAmount
                          )}
                        </span>
                      </div>
                      <div className="flex justify-between text-muted-foreground">
                        <span>New Available Balance:</span>
                        <span className="font-mono font-bold text-emerald-500">
                          {formatUGX(
                            activeTab === 'deposit'
                              ? selectedAccount.availableBalance + parsedAmount
                              : selectedAccount.availableBalance - parsedAmount
                          )}
                        </span>
                      </div>
                    </div>
                  )}

                  <Button
                    onClick={executeTransaction}
                    disabled={
                      transactionMutation.isPending ||
                      parsedAmount <= 0 ||
                      (activeTab === 'withdraw' &&
                        (parsedAmount > selectedAccount.availableBalance || parsedAmount > tillCash)) ||
                      !!selectedAccount.subStatus?.block
                    }
                    className="w-full bg-primary hover:bg-primary-hover text-white py-2.5 font-bold"
                  >
                    {transactionMutation.isPending
                      ? 'Posting Transaction...'
                      : `Execute Counter ${activeTab === 'deposit' ? 'Deposit' : 'Withdrawal'}`}
                  </Button>
                </div>
              </div>
            </>
          ) : (
            <div className="bg-card border border-border rounded-xl p-12 text-center text-muted-foreground space-y-3">
              <Wallet className="w-10 h-10 mx-auto text-primary/40" />
              <h3 className="text-base font-semibold text-foreground">No Account Selected</h3>
              <p className="text-xs max-w-sm mx-auto">
                Select a member account from the directory on the left or search by account number to execute a transaction.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Printable Receipt Modal */}
      <Modal
        isOpen={!!receipt}
        onClose={() => setReceipt(null)}
        title="Counter Cash Transaction Receipt"
      >
        {receipt && (
          <div className="space-y-4">
            <div id="printable-receipt" className="p-4 bg-muted/20 border border-border rounded-lg space-y-3 text-xs font-mono">
              <div className="text-center border-b border-border pb-3">
                <h4 className="text-sm font-bold uppercase tracking-wider text-foreground">
                  NSIMBI SACCO Core Banking
                </h4>
                <p className="text-[10px] text-muted-foreground">Official Cash Counter Receipt</p>
                <p className="text-[10px] text-muted-foreground mt-0.5">Ref: {receipt.ref}</p>
              </div>

              <div className="space-y-1.5 py-1">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Date & Time:</span>
                  <span className="text-foreground">{formatDateTime(receipt.date)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Transaction Type:</span>
                  <span className="font-bold text-foreground uppercase">{receipt.type}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Account Number:</span>
                  <span className="text-foreground">{receipt.accountNo}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Member Name:</span>
                  <span className="text-foreground">{receipt.memberName}</span>
                </div>
                <div className="flex justify-between pt-1 border-t border-border">
                  <span className="text-muted-foreground font-bold">Amount Transacted:</span>
                  <span className="font-bold text-emerald-500 text-sm">{formatUGX(receipt.amount)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">New Ledger Balance:</span>
                  <span className="font-bold text-foreground">{formatUGX(receipt.newBalance)}</span>
                </div>
                <div className="flex justify-between pt-1 border-t border-border">
                  <span className="text-muted-foreground">Cashier / Teller:</span>
                  <span className="text-foreground">{receipt.teller}</span>
                </div>
              </div>

              <div className="border-t border-border pt-2 text-center text-[10px] text-muted-foreground">
                Thank you for banking with NSIMBI SACCO.
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <Button variant="outline" size="sm" onClick={() => setReceipt(null)}>
                Close
              </Button>
              <Button
                size="sm"
                className="bg-primary hover:bg-primary-hover text-white gap-1.5"
                onClick={() => {
                  window.print();
                }}
              >
                <Printer className="w-3.5 h-3.5" />
                Print Receipt
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}

export default function CashDeskPage() {
  return (
    <Suspense
      fallback={
        <div className="py-24 text-center text-muted-foreground">
          <RefreshCw className="w-6 h-6 animate-spin mx-auto text-primary mb-2" />
          <p className="text-xs">Loading cash desk terminal...</p>
        </div>
      }
    >
      <CashDeskTerminal />
    </Suspense>
  );
}
