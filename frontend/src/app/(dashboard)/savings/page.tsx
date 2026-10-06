'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { savingsService, savingsKeys } from '@/services/savings.service';
import { SavingsAccountSummary } from '@/types/savings';
import { ApiError, PaginatedResponse } from '@/types/api';
import { toast } from 'sonner';

import { SavingsNav } from '@/components/savings/SavingsNav';
import { SavingsFilterBar, SavingsStatusFilter } from '@/components/savings/SavingsFilterBar';
import { SavingsListItem } from '@/components/savings/SavingsListItem';
import { SavingsPagination } from '@/components/savings/SavingsPagination';
import { SavingsEmptyState } from '@/components/savings/SavingsEmptyState';
import { SavingsDepositModal } from '@/components/savings/SavingsDepositModal';

export default function SavingsDirectoryPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState('');
  const [showFilters, setShowFilters] = useState(true);
  const [statusFilter, setStatusFilter] = useState<SavingsStatusFilter>('all');
  const [page, setPage] = useState(1);
  const pageSize = 15;

  const [openDropdownId, setOpenDropdownId] = useState<number | null>(null);
  const [selectedAccount, setSelectedAccount] = useState<SavingsAccountSummary | null>(null);
  const [depositModalOpen, setDepositModalOpen] = useState(false);

  // Fetch savings accounts via savingsService with cancellation support
  const { data, isLoading, isError, error, refetch, isFetching } = useQuery<
    PaginatedResponse<SavingsAccountSummary>,
    ApiError
  >({
    queryKey: savingsKeys.list({ status: statusFilter, page, pageSize, search }),
    queryFn: ({ signal }) =>
      savingsService.getAccounts(
        {
          status: statusFilter,
          offset: (page - 1) * pageSize,
          limit: pageSize,
          orderBy: 'id',
          sortOrder: 'DESC',
          search: search.trim() || undefined,
        },
        signal
      ),
  });

  // Freeze/Unfreeze account mutation
  const toggleFreezeMutation = useMutation({
    mutationFn: ({ accountId, isFrozen }: { accountId: number; isFrozen: boolean }) => {
      const command = isFrozen ? 'unblock' : 'block';
      return savingsService.executeCommand(accountId, command);
    },
    onSuccess: (_, variables) => {
      toast.success(
        variables.isFrozen
          ? 'Savings account unblocked successfully.'
          : 'Savings account frozen/blocked successfully.'
      );
      queryClient.invalidateQueries({ queryKey: savingsKeys.lists() });
      setOpenDropdownId(null);
    },
    onError: (err: ApiError) => {
      toast.error(err.message);
    },
  });

  // Deposit mutation
  const depositMutation = useMutation({
    mutationFn: ({
      accountId,
      amount,
      note,
    }: {
      accountId: number;
      amount: number;
      note?: string;
    }) =>
      savingsService.recordDeposit(accountId, {
        amount,
        note: note || 'Counter Cash Deposit',
        paymentTypeId: 1,
      }),
    onSuccess: () => {
      toast.success('Deposit completed successfully.');
      queryClient.invalidateQueries({ queryKey: savingsKeys.lists() });
      setDepositModalOpen(false);
      setSelectedAccount(null);
    },
    onError: (err: ApiError) => {
      toast.error(err.message);
    },
  });

  const accounts = data?.pageItems || [];
  const totalRecords = data?.totalFilteredRecords || 0;
  const totalPages = Math.ceil(totalRecords / pageSize) || 1;
  const startRecord = totalRecords === 0 ? 0 : (page - 1) * pageSize + 1;
  const endRecord = Math.min(page * pageSize, totalRecords);

  const filteredAccounts = accounts.filter((acc) => {
    if (!search.trim()) return true;
    const q = search.toLowerCase();
    return (
      acc.accountNo?.toLowerCase().includes(q) ||
      acc.clientName?.toLowerCase().includes(q) ||
      acc.savingsProductName?.toLowerCase().includes(q)
    );
  });

  return (
    <div className="space-y-6">
      {/* Navigation */}
      <SavingsNav />

      {/* Main Container Card */}
      <div className="bg-card border border-border rounded-xl shadow-sm overflow-visible">
        {/* Search & Filters */}
        <SavingsFilterBar
          search={search}
          onSearchChange={setSearch}
          showFilters={showFilters}
          onToggleFilters={() => setShowFilters(!showFilters)}
          statusFilter={statusFilter}
          onStatusFilterChange={(st) => {
            setStatusFilter(st);
            setPage(1);
          }}
          onRefresh={() => refetch()}
          isFetching={isFetching}
        />

        {/* Account Items List */}
        <div className="divide-y divide-border">
          {isLoading || isError || filteredAccounts.length === 0 ? (
            <SavingsEmptyState
              isLoading={isLoading}
              isError={isError}
              errorMessage={error?.message}
              onRetry={() => refetch()}
              search={search}
              statusFilter={statusFilter}
              onClearSearch={() => setSearch('')}
            />
          ) : (
            filteredAccounts.map((acc) => (
              <SavingsListItem
                key={acc.id}
                account={acc}
                isOpenDropdown={openDropdownId === acc.id}
                onToggleDropdown={() =>
                  setOpenDropdownId(openDropdownId === acc.id ? null : acc.id)
                }
                onCloseDropdown={() => setOpenDropdownId(null)}
                onOpenDeposit={() => {
                  setSelectedAccount(acc);
                  setDepositModalOpen(true);
                }}
                onToggleFreeze={(isFrozen) =>
                  toggleFreezeMutation.mutate({
                    accountId: acc.id,
                    isFrozen,
                  })
                }
                isFreezePending={toggleFreezeMutation.isPending}
              />
            ))
          )}
        </div>

        {/* Pagination Footer */}
        <SavingsPagination
          page={page}
          totalPages={totalPages}
          totalRecords={totalRecords}
          startRecord={startRecord}
          endRecord={endRecord}
          isLoading={isLoading}
          onPageChange={setPage}
        />
      </div>

      {/* Quick Deposit Modal */}
      <SavingsDepositModal
        isOpen={depositModalOpen}
        onClose={() => {
          setDepositModalOpen(false);
          setSelectedAccount(null);
        }}
        account={selectedAccount}
        onSubmit={(amount, note) => {
          if (!selectedAccount) return;
          depositMutation.mutate({
            accountId: selectedAccount.id,
            amount,
            note,
          });
        }}
        isPending={depositMutation.isPending}
      />
    </div>
  );
}
