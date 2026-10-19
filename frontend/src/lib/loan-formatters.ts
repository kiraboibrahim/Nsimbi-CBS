import { formatNumber } from '@/lib/formatters';
import { LoanCurrency } from '@/types/loans';

export function formatLoanAmount(amount: number | null | undefined, currency?: LoanCurrency): string {
  if (amount === null || amount === undefined || Number.isNaN(amount)) return '—';
  const prefix = currency?.code || currency?.displaySymbol;
  return prefix ? `${prefix} ${formatNumber(amount)}` : formatNumber(amount);
}
