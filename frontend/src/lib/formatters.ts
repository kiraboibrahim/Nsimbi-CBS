/**
 * Currency, date, and account number formatting utilities for NSIMBI SACCO CBS
 */

export function formatUGX(amount: number | string | null | undefined): string {
  if (amount === null || amount === undefined || amount === '') return 'UGX 0.00';
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  if (isNaN(num)) return 'UGX 0.00';
  return (
    'UGX ' +
    num.toLocaleString('en-UG', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })
  );
}

export function formatNumber(amount: number | string | null | undefined): string {
  if (amount === null || amount === undefined || amount === '') return '0.00';
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  if (isNaN(num)) return '0.00';
  return num.toLocaleString('en-UG', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

export function formatDate(dateVal: string | number[] | null | undefined): string {
  if (!dateVal) return '—';
  try {
    let d: Date;
    if (Array.isArray(dateVal)) {
      d = new Date(dateVal[0], (dateVal[1] || 1) - 1, dateVal[2] || 1);
    } else {
      d = new Date(dateVal);
    }
    if (isNaN(d.getTime())) return String(dateVal);
    return d.toLocaleDateString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  } catch {
    return String(dateVal);
  }
}

export function formatDateTime(dateVal: string | number[] | null | undefined): string {
  if (!dateVal) return '—';
  try {
    let d: Date;
    if (Array.isArray(dateVal)) {
      d = new Date(
        dateVal[0],
        (dateVal[1] || 1) - 1,
        dateVal[2] || 1,
        dateVal[3] || 0,
        dateVal[4] || 0,
        dateVal[5] || 0
      );
    } else {
      d = new Date(dateVal);
    }
    if (isNaN(d.getTime())) return String(dateVal);
    return d.toLocaleString('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return String(dateVal);
  }
}

export function formatAccountNumber(accountNo: string | null | undefined): string {
  if (!accountNo) return '—';
  // format 12-digit string 003500005281 into 0035-0000-5281 for readability
  if (accountNo.length === 12) {
    return `${accountNo.slice(0, 4)}-${accountNo.slice(4, 8)}-${accountNo.slice(8)}`;
  }
  return accountNo;
}
