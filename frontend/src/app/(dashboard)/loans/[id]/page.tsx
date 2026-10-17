'use client';

import Link from 'next/link';
import { useParams } from 'next/navigation';
import { type ReactNode } from 'react';
import { useQuery } from '@tanstack/react-query';
import { AlertCircle, ArrowLeft, CalendarClock, LoaderCircle, RefreshCw, ShieldX } from 'lucide-react';
import { Badge, type BadgeProps } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { formatAccountNumber, formatDate } from '@/lib/formatters';
import { formatLoanAmount } from '@/lib/loan-formatters';
import { loanKeys, loansService } from '@/services/loans.service';
import { ApiError } from '@/types/api';
import { type LoanAccount, type LoanCurrency } from '@/types/loans';

function statusVariant(statusId: number): BadgeProps['variant'] {
  if (statusId === 300 || statusId === 600) return 'success';
  if ([100, 200, 303, 304].includes(statusId)) return 'warning';
  if ([400, 500, 601].includes(statusId)) return 'danger';
  return 'neutral';
}

export default function LoanDetailPage() {
  const params = useParams<{ id: string }>();
  const loanId = Number(params.id);
  const validId = Number.isSafeInteger(loanId) && loanId > 0;

  const { data: loan, isLoading, error, refetch, isFetching } = useQuery<LoanAccount, ApiError>({
    queryKey: loanKeys.detail(loanId),
    queryFn: ({ signal }) => loansService.getLoan(loanId, signal),
    enabled: validId,
    retry: (count, queryError) => ![403, 404].includes(queryError.status) && count < 2,
  });

  if (!validId || error?.status === 404) {
    return <DetailState icon={<AlertCircle className="h-12 w-12 text-muted-foreground" />} title="Loan not found" message="The requested loan does not exist or is not available in your office hierarchy." />;
  }
  if (error?.status === 403) {
    return <DetailState icon={<ShieldX className="h-12 w-12 text-destructive" />} title="Loans access denied" message="Your role does not have permission to view this loan." />;
  }
  if (error) {
    return <DetailState icon={<AlertCircle className="h-12 w-12 text-destructive" />} title="Unable to load loan" message={error.message} action={<Button variant="outline" size="sm" onClick={() => refetch()}>Try again</Button>} />;
  }
  if (isLoading || !loan) {
    return <DetailState icon={<LoaderCircle className="h-10 w-10 animate-spin text-primary" />} title="Loading loan" message="Retrieving summary, schedule, and transaction history…" />;
  }

  const currency = loan.summary?.currency ?? loan.currency;
  const schedule = loan.repaymentSchedule?.periods ?? [];
  const transactions = loan.transactions ?? [];
  const hasOverdueInformation =
    loan.inArrears ||
    loan.summary?.overdueSinceDate !== undefined ||
    loan.summary?.principalOverdue !== undefined ||
    loan.summary?.totalOverdue !== undefined;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
        <div className="space-y-3">
          <Link href="/loans" className="inline-flex items-center text-xs font-semibold text-muted-foreground hover:text-primary">
            <ArrowLeft className="mr-1.5 h-4 w-4" /> Back to loans
          </Link>
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-2xl font-bold tracking-tight text-foreground">{loan.clientName || loan.group?.name || 'Borrower unavailable'}</h1>
              <Badge variant={statusVariant(loan.status.id)}>{loan.status.value}</Badge>
            </div>
            <p className="mt-1 text-sm text-muted-foreground">
              {loan.loanProductName} · <span className="font-mono">{formatAccountNumber(loan.accountNo)}</span>
            </p>
          </div>
        </div>
        <Button variant="outline" size="sm" onClick={() => refetch()} disabled={isFetching}>
          <RefreshCw className={`mr-2 h-4 w-4 ${isFetching ? 'animate-spin' : ''}`} /> Refresh
        </Button>
      </div>

      <section aria-labelledby="loan-summary-heading" className="space-y-3">
        <h2 id="loan-summary-heading" className="text-sm font-bold uppercase tracking-wide text-muted-foreground">Summary</h2>
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <SummaryCard label="Loan principal" value={formatLoanAmount(loan.principal, currency)} />
          <SummaryCard label="Principal outstanding" value={formatLoanAmount(loan.summary?.principalOutstanding, currency)} />
          <SummaryCard label="Total outstanding" value={formatLoanAmount(loan.summary?.totalOutstanding, currency)} />
          <SummaryCard label="Total repaid" value={formatLoanAmount(loan.summary?.totalRepayment, currency)} />
        </div>
      </section>

      <section aria-labelledby="overdue-heading" className="rounded-xl border border-border bg-card p-5 shadow-sm">
        <div className="flex items-start gap-3">
          <CalendarClock className="mt-0.5 h-5 w-5 text-amber-500" />
          <div className="flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <h2 id="overdue-heading" className="font-bold text-foreground">Overdue information</h2>
              {loan.inArrears && <Badge variant="danger">In arrears</Badge>}
              {loan.inArrears === false && <Badge variant="success">Not in arrears</Badge>}
            </div>
            {!hasOverdueInformation ? (
              <p className="mt-2 text-sm text-muted-foreground">No overdue information was returned by the backend.</p>
            ) : (
              <div className="mt-4 grid gap-4 sm:grid-cols-3">
                <DetailValue label="Overdue since" value={formatDate(loan.summary?.overdueSinceDate)} />
                <DetailValue label="Principal overdue" value={formatLoanAmount(loan.summary?.principalOverdue, currency)} />
                <DetailValue label="Total overdue" value={formatLoanAmount(loan.summary?.totalOverdue, currency)} />
              </div>
            )}
          </div>
        </div>
      </section>

      <DataSection title="Repayment schedule" empty={schedule.length === 0} emptyMessage="No repayment schedule was returned for this loan.">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[1100px] text-left text-sm">
            <thead className="border-b border-border bg-muted/30 text-xs uppercase tracking-wide text-muted-foreground">
              <tr>
                <th className="px-4 py-3">Period</th><th className="px-4 py-3">Due date</th>
                <th className="px-4 py-3 text-right">Principal due</th><th className="px-4 py-3 text-right">Interest due</th>
                <th className="px-4 py-3 text-right">Fees</th><th className="px-4 py-3 text-right">Penalties</th>
                <th className="px-4 py-3 text-right">Paid</th><th className="px-4 py-3 text-right">Outstanding</th>
                <th className="px-4 py-3">Schedule state</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {schedule.map((period, index) => (
                <tr key={`${period.period ?? 'disbursement'}-${index}`} className="hover:bg-muted/20">
                  <td className="px-4 py-3 font-medium">{period.period ?? 'Disbursement'}{period.downPaymentPeriod ? ' (down payment)' : ''}</td>
                  <td className="whitespace-nowrap px-4 py-3">{formatDate(period.dueDate)}</td>
                  <AmountCell value={period.principalDue ?? period.principalDisbursed} currency={currency} />
                  <AmountCell value={period.interestDue} currency={currency} />
                  <AmountCell value={period.feeChargesDue} currency={currency} />
                  <AmountCell value={period.penaltyChargesDue} currency={currency} />
                  <AmountCell value={period.totalPaidForPeriod} currency={currency} />
                  <AmountCell value={period.totalOutstandingForPeriod} currency={currency} />
                  <td className="px-4 py-3">
                    {period.complete ? (
                      <Badge variant="success">Complete</Badge>
                    ) : period.totalOverdue !== undefined && period.totalOverdue > 0 ? (
                      <Badge variant="danger">Backend overdue</Badge>
                    ) : (
                      <Badge variant="neutral">Scheduled</Badge>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </DataSection>

      <DataSection title="Transaction history" empty={transactions.length === 0} emptyMessage="No transactions were returned for this loan.">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[1150px] text-left text-sm">
            <thead className="border-b border-border bg-muted/30 text-xs uppercase tracking-wide text-muted-foreground">
              <tr>
                <th className="px-4 py-3">Date / type</th><th className="px-4 py-3">Reference</th>
                <th className="px-4 py-3 text-right">Amount</th><th className="px-4 py-3 text-right">Principal</th>
                <th className="px-4 py-3 text-right">Interest</th><th className="px-4 py-3 text-right">Fees</th>
                <th className="px-4 py-3 text-right">Penalties</th><th className="px-4 py-3 text-right">Balance after</th>
                <th className="px-4 py-3">State</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {transactions.map((transaction) => {
                const reversed = transaction.manuallyReversed || Boolean(transaction.reversedOnDate);
                const transactionCurrency = transaction.currency ?? currency;
                return (
                  <tr key={transaction.id} className={reversed ? 'bg-destructive/5 text-muted-foreground' : 'hover:bg-muted/20'}>
                    <td className="px-4 py-3"><p className="whitespace-nowrap font-medium">{formatDate(transaction.date)}</p><p className="text-xs text-muted-foreground">{transaction.type.value}</p></td>
                    <td className="px-4 py-3 font-mono text-xs">{transaction.externalId || `#${transaction.id}`}</td>
                    <AmountCell value={transaction.amount} currency={transactionCurrency} />
                    <AmountCell value={transaction.principalPortion} currency={transactionCurrency} />
                    <AmountCell value={transaction.interestPortion} currency={transactionCurrency} />
                    <AmountCell value={transaction.feeChargesPortion} currency={transactionCurrency} />
                    <AmountCell value={transaction.penaltyChargesPortion} currency={transactionCurrency} />
                    <AmountCell value={transaction.outstandingLoanBalance} currency={transactionCurrency} />
                    <td className="px-4 py-3">
                      {reversed ? <div><Badge variant="danger">Reversed</Badge>{transaction.reversedOnDate && <p className="mt-1 text-xs">{formatDate(transaction.reversedOnDate)}</p>}</div> : <Badge variant="success">Posted</Badge>}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </DataSection>
    </div>
  );
}

function AmountCell({ value, currency }: { value?: number; currency?: LoanCurrency }) {
  return <td className="whitespace-nowrap px-4 py-3 text-right font-mono font-medium tabular-nums">{formatLoanAmount(value, currency)}</td>;
}

function SummaryCard({ label, value }: { label: string; value: string }) {
  return <div className="rounded-xl border border-border bg-card p-5 shadow-sm"><p className="text-xs font-semibold text-muted-foreground">{label}</p><p className="mt-2 font-mono text-lg font-bold tabular-nums text-foreground">{value}</p></div>;
}

function DetailValue({ label, value }: { label: string; value: string }) {
  return <div><p className="text-xs font-semibold text-muted-foreground">{label}</p><p className="mt-1 font-mono text-sm font-bold text-foreground">{value}</p></div>;
}

function DataSection({ title, empty, emptyMessage, children }: { title: string; empty: boolean; emptyMessage: string; children: ReactNode }) {
  return <section className="overflow-hidden rounded-xl border border-border bg-card shadow-sm"><div className="border-b border-border px-5 py-4"><h2 className="font-bold text-foreground">{title}</h2></div>{empty ? <p className="px-5 py-12 text-center text-sm text-muted-foreground">{emptyMessage}</p> : children}</section>;
}

function DetailState({ icon, title, message, action }: { icon: ReactNode; title: string; message: string; action?: ReactNode }) {
  return <div className="mx-auto flex min-h-[60vh] max-w-lg flex-col items-center justify-center gap-4 px-6 text-center">{icon}<div><h1 className="text-xl font-bold text-foreground">{title}</h1><p className="mt-1 text-sm text-muted-foreground">{message}</p></div>{action}<Link href="/loans" className="text-sm font-semibold text-primary hover:underline">Return to loans</Link></div>;
}
