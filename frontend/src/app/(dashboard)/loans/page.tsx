'use client';

import Link from 'next/link';
import { type FormEvent, type ReactNode, useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { AlertCircle, HandCoins, LoaderCircle, RefreshCw, Search, ShieldX } from 'lucide-react';
import { Badge, type BadgeProps } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Pagination } from '@/components/ui/Pagination';
import { formatAccountNumber, formatDate } from '@/lib/formatters';
import { formatLoanAmount } from '@/lib/loan-formatters';
import { loanKeys, type LoanListParams, type LoanSearchField, loansService } from '@/services/loans.service';
import { ApiError, type PaginatedResponse } from '@/types/api';
import { type LoanAccount } from '@/types/loans';

const PAGE_SIZE = 15;

const STATUS_OPTIONS = [
  { value: '', label: 'All lifecycle statuses' },
  { value: '100', label: 'Submitted and pending approval' },
  { value: '200', label: 'Approved' },
  { value: '300', label: 'Active' },
  { value: '303', label: 'Transfer in progress' },
  { value: '304', label: 'Transfer on hold' },
  { value: '400', label: 'Withdrawn by client' },
  { value: '500', label: 'Rejected' },
  { value: '600', label: 'Closed — obligations met' },
  { value: '601', label: 'Closed — written off' },
  { value: '602', label: 'Closed — rescheduled' },
  { value: '700', label: 'Overpaid' },
];

function statusVariant(statusId: number): BadgeProps['variant'] {
  if (statusId === 300 || statusId === 600) return 'success';
  if ([100, 200, 303, 304].includes(statusId)) return 'warning';
  if ([400, 500, 601].includes(statusId)) return 'danger';
  return 'neutral';
}

export default function LoansPage() {
  const [page, setPage] = useState(1);
  const [status, setStatus] = useState('');
  const [searchField, setSearchField] = useState<LoanSearchField>('accountNo');
  const [searchText, setSearchText] = useState('');
  const [appliedSearch, setAppliedSearch] = useState<{ field: LoanSearchField; value: string } | null>(null);
  const [validationMessage, setValidationMessage] = useState('');

  const queryParams = useMemo<LoanListParams>(() => {
    const params: LoanListParams = {
      limit: PAGE_SIZE,
      offset: (page - 1) * PAGE_SIZE,
      orderBy: 'id',
      sortOrder: 'DESC',
      status: status || undefined,
    };
    if (appliedSearch?.field === 'accountNo') params.accountNo = appliedSearch.value;
    if (appliedSearch?.field === 'externalId') params.externalId = appliedSearch.value;
    if (appliedSearch?.field === 'clientId') params.clientId = Number(appliedSearch.value);
    return params;
  }, [appliedSearch, page, status]);

  const { data, isLoading, isFetching, error, refetch } = useQuery<
    PaginatedResponse<LoanAccount>,
    ApiError
  >({
    queryKey: loanKeys.list(queryParams),
    queryFn: ({ signal }) => loansService.getLoans(queryParams, signal),
    retry: (count, queryError) => queryError.status !== 403 && count < 2,
  });

  const applySearch = (event: FormEvent) => {
    event.preventDefault();
    const value = searchText.trim();
    if (!value) {
      setAppliedSearch(null);
      setValidationMessage('');
      setPage(1);
      return;
    }
    if (searchField === 'clientId' && !/^[1-9]\d*$/.test(value)) {
      setValidationMessage('Client ID must be a positive whole number.');
      return;
    }
    setValidationMessage('');
    setAppliedSearch({ field: searchField, value });
    setPage(1);
  };

  const clearSearch = () => {
    setSearchText('');
    setAppliedSearch(null);
    setValidationMessage('');
    setPage(1);
  };

  const loans = data?.pageItems ?? [];
  const total = data?.totalFilteredRecords ?? 0;
  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-primary">Portfolio</p>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Loans</h1>
          <p className="mt-1 text-sm text-muted-foreground">Read-only loan register scoped by your Fineract access.</p>
        </div>
        <Button variant="outline" size="sm" onClick={() => refetch()} disabled={isFetching}>
          <RefreshCw className={`mr-2 h-4 w-4 ${isFetching ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      <div className="rounded-xl border border-border bg-card shadow-sm">
        <div className="space-y-3 border-b border-border p-4">
          <form onSubmit={applySearch} className="flex flex-col gap-3 lg:flex-row lg:items-start">
            <label className="flex min-w-52 flex-col gap-1 text-xs font-semibold text-muted-foreground">
              Search field
              <select
                value={searchField}
                onChange={(event) => {
                  setSearchField(event.target.value as LoanSearchField);
                  setValidationMessage('');
                }}
                className="h-10 rounded-lg border border-border bg-background px-3 text-sm text-foreground outline-none focus:border-primary"
              >
                <option value="accountNo">Account number</option>
                <option value="externalId">External ID</option>
                <option value="clientId">Client ID</option>
              </select>
            </label>
            <label className="flex flex-1 flex-col gap-1 text-xs font-semibold text-muted-foreground">
              Exact value
              <div className="flex gap-2">
                <input
                  value={searchText}
                  onChange={(event) => setSearchText(event.target.value)}
                  inputMode={searchField === 'clientId' ? 'numeric' : 'text'}
                  placeholder={
                    searchField === 'accountNo'
                      ? 'Enter account number'
                      : searchField === 'externalId'
                        ? 'Enter external ID'
                        : 'Enter client ID'
                  }
                  className="h-10 min-w-0 flex-1 rounded-lg border border-border bg-background px-3 text-sm text-foreground outline-none placeholder:text-muted-foreground focus:border-primary"
                />
                <Button type="submit" className="h-10">
                  <Search className="mr-2 h-4 w-4" /> Search
                </Button>
                {appliedSearch && (
                  <Button type="button" variant="ghost" className="h-10" onClick={clearSearch}>Clear</Button>
                )}
              </div>
              {validationMessage && <span className="text-xs font-medium text-destructive">{validationMessage}</span>}
            </label>
            <label className="flex min-w-64 flex-col gap-1 text-xs font-semibold text-muted-foreground">
              Lifecycle status
              <select
                value={status}
                onChange={(event) => {
                  setStatus(event.target.value);
                  setPage(1);
                }}
                className="h-10 rounded-lg border border-border bg-background px-3 text-sm text-foreground outline-none focus:border-primary"
              >
                {STATUS_OPTIONS.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
              </select>
            </label>
          </form>
          {appliedSearch && (
            <p className="text-xs text-muted-foreground">
              Filtering by {appliedSearch.field === 'accountNo' ? 'account number' : appliedSearch.field === 'externalId' ? 'external ID' : 'client ID'}:{' '}
              <span className="font-mono font-semibold text-foreground">{appliedSearch.value}</span>
            </p>
          )}
        </div>

        {error?.status === 403 ? (
          <StateMessage icon={<ShieldX className="h-10 w-10 text-destructive" />} title="Loans access denied" message="Your role does not have permission to view loans." />
        ) : error ? (
          <StateMessage icon={<AlertCircle className="h-10 w-10 text-destructive" />} title="Unable to load loans" message={error.message} action={<Button variant="outline" size="sm" onClick={() => refetch()}>Try again</Button>} />
        ) : isLoading ? (
          <StateMessage icon={<LoaderCircle className="h-9 w-9 animate-spin text-primary" />} title="Loading loans" message="Retrieving the branch-scoped loan register…" />
        ) : loans.length === 0 ? (
          <StateMessage icon={<HandCoins className="h-10 w-10 text-muted-foreground" />} title="No loans found" message="No loans match the selected server-side filters." />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[1100px] text-left text-sm">
              <thead className="border-b border-border bg-muted/30 text-xs uppercase tracking-wide text-muted-foreground">
                <tr>
                  <th className="px-5 py-3">Borrower / product</th>
                  <th className="px-5 py-3">Account</th>
                  <th className="px-5 py-3">Lifecycle status</th>
                  <th className="px-5 py-3">Overdue</th>
                  <th className="px-5 py-3 text-right">Principal outstanding</th>
                  <th className="px-5 py-3 text-right">Total outstanding</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {loans.map((loan) => {
                  const currency = loan.summary?.currency ?? loan.currency;
                  const overdueDate = loan.summary?.overdueSinceDate;
                  return (
                    <tr key={loan.id} className="transition-colors hover:bg-muted/20">
                      <td className="px-5 py-4">
                        <Link href={`/loans/${loan.id}`} className="font-semibold text-foreground hover:text-primary hover:underline">
                          {loan.clientName || loan.group?.name || 'Borrower unavailable'}
                        </Link>
                        <p className="mt-0.5 text-xs text-muted-foreground">{loan.loanProductName}</p>
                      </td>
                      <td className="px-5 py-4">
                        <p className="font-mono font-semibold text-foreground">{formatAccountNumber(loan.accountNo)}</p>
                        <p className="mt-0.5 text-xs text-muted-foreground">{loan.currency?.code || 'Currency unavailable'}</p>
                      </td>
                      <td className="px-5 py-4"><Badge variant={statusVariant(loan.status.id)}>{loan.status.value}</Badge></td>
                      <td className="px-5 py-4">
                        {loan.inArrears || overdueDate ? (
                          <div>
                            <Badge variant="danger">Overdue</Badge>
                            <p className="mt-1 text-xs text-muted-foreground">Since {formatDate(overdueDate)}</p>
                          </div>
                        ) : loan.inArrears === false ? (
                          <Badge variant="success">Not in arrears</Badge>
                        ) : (
                          <span className="text-muted-foreground">Overdue information unavailable</span>
                        )}
                      </td>
                      <td className="px-5 py-4 text-right font-mono font-semibold tabular-nums">{formatLoanAmount(loan.summary?.principalOutstanding, currency)}</td>
                      <td className="px-5 py-4 text-right font-mono font-semibold tabular-nums">{formatLoanAmount(loan.summary?.totalOutstanding, currency)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {!error && !isLoading && loans.length > 0 && (
          <Pagination currentPage={page} totalPages={totalPages} totalCount={total} pageSize={PAGE_SIZE} onPageChange={setPage} isLoading={isFetching} />
        )}
      </div>
    </div>
  );
}

function StateMessage({ icon, title, message, action }: { icon: ReactNode; title: string; message: string; action?: ReactNode }) {
  return (
    <div className="flex min-h-72 flex-col items-center justify-center gap-3 px-6 py-12 text-center">
      {icon}
      <div>
        <h2 className="font-bold text-foreground">{title}</h2>
        <p className="mt-1 max-w-md text-sm text-muted-foreground">{message}</p>
      </div>
      {action}
    </div>
  );
}
