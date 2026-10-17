export type FineractDate = string | number[];

export interface LoanCurrency {
  code: string;
  name?: string;
  decimalPlaces?: number;
  displaySymbol?: string;
  displayLabel?: string;
}

export interface LoanEnumOption {
  id: number;
  code: string;
  value: string;
}

export interface LoanStatus extends LoanEnumOption {
  pendingApproval?: boolean;
  waitingForDisbursal?: boolean;
  active?: boolean;
  closedObligationsMet?: boolean;
  closedWrittenOff?: boolean;
  closedRescheduled?: boolean;
  overpaid?: boolean;
}

export interface LoanSummary {
  currency?: LoanCurrency;
  principalDisbursed?: number;
  principalPaid?: number;
  principalWrittenOff?: number;
  principalOutstanding?: number;
  principalOverdue?: number;
  interestOutstanding?: number;
  interestOverdue?: number;
  feeChargesOutstanding?: number;
  feeChargesOverdue?: number;
  penaltyChargesOutstanding?: number;
  penaltyChargesOverdue?: number;
  totalRepayment?: number;
  totalOutstanding?: number;
  totalOverdue?: number;
  overdueSinceDate?: FineractDate;
}

export interface LoanSchedulePeriod {
  period?: number;
  fromDate?: FineractDate;
  dueDate?: FineractDate;
  obligationsMetOnDate?: FineractDate;
  complete?: boolean;
  principalDisbursed?: number;
  principalDue?: number;
  principalPaid?: number;
  principalOutstanding?: number;
  principalLoanBalanceOutstanding?: number;
  interestDue?: number;
  interestPaid?: number;
  interestOutstanding?: number;
  feeChargesDue?: number;
  penaltyChargesDue?: number;
  totalDueForPeriod?: number;
  totalPaidForPeriod?: number;
  totalOutstandingForPeriod?: number;
  totalOverdue?: number;
  downPaymentPeriod?: boolean;
}

export interface LoanRepaymentSchedule {
  currency?: LoanCurrency;
  periods?: LoanSchedulePeriod[];
  totalPrincipalDisbursed?: number;
  totalPrincipalExpected?: number;
  totalPrincipalPaid?: number;
  totalInterestCharged?: number;
  totalFeeChargesCharged?: number;
  totalPenaltyChargesCharged?: number;
  totalRepaymentExpected?: number;
  totalRepayment?: number;
  totalOutstanding?: number;
}

export interface LoanTransaction {
  id: number;
  type: LoanEnumOption;
  date: FineractDate;
  currency?: LoanCurrency;
  amount?: number;
  principalPortion?: number;
  interestPortion?: number;
  feeChargesPortion?: number;
  penaltyChargesPortion?: number;
  outstandingLoanBalance?: number;
  externalId?: string;
  manuallyReversed?: boolean;
  reversalExternalId?: string;
  reversedOnDate?: FineractDate;
}

export interface LoanAccount {
  id: number;
  accountNo: string;
  externalId?: string;
  status: LoanStatus;
  clientId?: number;
  clientName?: string;
  group?: { id?: number; name?: string };
  loanProductId: number;
  loanProductName: string;
  loanOfficerName?: string;
  currency: LoanCurrency;
  principal?: number;
  approvedPrincipal?: number;
  numberOfRepayments?: number;
  annualInterestRate?: number;
  inArrears?: boolean;
  summary?: LoanSummary;
  repaymentSchedule?: LoanRepaymentSchedule;
  transactions?: LoanTransaction[];
}
