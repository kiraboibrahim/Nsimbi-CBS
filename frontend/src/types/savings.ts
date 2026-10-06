export interface SavingsAccountStatus {
  id: number;
  code: string;
  value: string;
  submittedAndPendingApproval?: boolean;
  approved?: boolean;
  rejected?: boolean;
  withdrawnByApplicant?: boolean;
  active?: boolean;
  closed?: boolean;
  prematureClosed?: boolean;
  transferInProgress?: boolean;
  transferOnHold?: boolean;
  matured?: boolean;
}

export interface SavingsAccountSubStatus {
  id: number;
  code: string;
  value: string;
  none?: boolean;
  inactive?: boolean;
  dormant?: boolean;
  escheat?: boolean;
  block?: boolean;
  blockCredit?: boolean;
  blockDebit?: boolean;
}

export interface SavingsCurrency {
  code: string;
  name: string;
  decimalPlaces: number;
  displaySymbol: string;
  nameCode?: string;
  displayLabel?: string;
}

export interface SavingsTimeline {
  submittedOnDate?: string | number[];
  submittedByUsername?: string;
  submittedByFirstname?: string;
  submittedByLastname?: string;
  approvedOnDate?: string | number[];
  approvedByUsername?: string;
  activatedOnDate?: string | number[];
  activatedByUsername?: string;
  closedOnDate?: string | number[];
  closedByUsername?: string;
}

export interface SavingsSummary {
  currency?: SavingsCurrency;
  totalDeposits?: number;
  totalWithdrawals?: number;
  totalInterestEarned?: number;
  totalInterestPosted?: number;
  accountBalance: number;
  availableBalance: number;
}

export interface SavingsAccountSummary {
  id: number;
  accountNo: string;
  externalId?: string;
  depositAccountType?: {
    id: number;
    code: string;
    value: string;
  };
  clientId: number;
  clientName: string;
  clientType?: string;
  savingsProductId: number;
  savingsProductName: string;
  fieldOfficerId?: number;
  fieldOfficerName?: string;
  status: SavingsAccountStatus;
  subStatus?: SavingsAccountSubStatus;
  accountBalance: number; // Actual balance
  availableBalance: number; // Actual - Holds - minRequiredBalance
  onHoldFunds?: number;
  minRequiredBalance?: number;
  nominalAnnualInterestRate?: number;
  interestCompoundingPeriodType?: { id: number; code: string; value: string };
  interestPostingPeriodType?: { id: number; code: string; value: string };
  interestCalculationType?: { id: number; code: string; value: string };
  interestCalculationDaysInYearType?: { id: number; code: string; value: string };
  currency: SavingsCurrency;
  timeline?: SavingsTimeline;
  lastActiveTransactionDate?: string | number[];
  officeId?: number;
  officeName?: string;
  smsAlertConfig?: SavingsSmsAlertConfig;
  summary?: SavingsSummary;
  transactions?: SavingsTransaction[];
}

export interface SavingsTransaction {
  id: number;
  transactionType: {
    id: number;
    code: string;
    value: string;
    deposit?: boolean;
    withdrawal?: boolean;
    interestPosting?: boolean;
    feeDeduction?: boolean;
    initiateTransfer?: boolean;
    approveTransfer?: boolean;
    withdrawTransfer?: boolean;
    rejectTransfer?: boolean;
    writtenOff?: boolean;
    overdraftInterest?: boolean;
    withholdTax?: boolean;
    escheat?: boolean;
    amountHold?: boolean;
    amountRelease?: boolean;
  };
  accountId?: number;
  accountNo?: string;
  date: string | number[];
  currency?: SavingsCurrency;
  paymentDetailData?: {
    id?: number;
    paymentType?: { id: number; name: string };
    accountNumber?: string;
    checkNumber?: string;
    routingCode?: string;
    receiptNumber?: string;
    bankNumber?: string;
  };
  amount: number;
  outstandingChargeAmount?: number;
  runningBalance: number;
  reversed: boolean;
  submittedOnDate?: string | number[];
  interestedPostedAsOn?: boolean;
  externalId?: string;
  note?: string;
}

export interface SavingsSmsAlertConfig {
  id?: number;
  savingsAccountId: number;
  notifyOnDeposit: boolean;
  notifyOnWithdrawal: boolean;
  notifyOnEarningInterest: boolean;
  notifyOnEarningDividends: boolean;
  notifyOnOutgoingTransfer: boolean;
  notifyOnIncomingTransfer: boolean;
  notifyOnDirectDebit: boolean;
  notifyOnDirectCredit: boolean;
  notifyOnStandingOrder: boolean;
  notifyOnLoanApplication: boolean;
  notifyOnLoanDisbursement: boolean;
  notifyOnLoanPayment: boolean;
  notifyOnLoanRefund: boolean;
  notifyOnHold: boolean;
}
