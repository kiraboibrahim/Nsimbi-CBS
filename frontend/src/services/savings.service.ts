import { apiClient } from '@/lib/api';
import { PaginatedResponse } from '@/types/api';
import {
    SavingsAccountSummary,
    SavingsTransaction,
    SavingsSmsAlertConfig,
} from '@/types/savings';

export interface NoteItem {
    id: number;
    createdByUsername: string;
    createdOn: string | number[];
    note: string;
}

export interface SavingsListParams {
    limit?: number;
    offset?: number;
    orderBy?: string;
    sortOrder?: 'ASC' | 'DESC';
    status?: string;
    search?: string;
}

export interface DepositWithdrawalPayload {
    amount: number;
    note?: string;
    paymentTypeId?: number;
    transactionDate?: string;
}

export interface HoldAmountPayload {
    amount: number;
    reasonForHold: string;
    transactionDate?: string;
}

export interface FineractTransactionResponse {
    officeId?: number;
    clientId?: number;
    savingsId?: number;
    resourceId?: number;
    changes?: Record<string, unknown>;
}

/**
 * Query key factory for the savings module cache.
 */
export const savingsKeys = {
    all: ['savings-accounts'] as const,
    lists: () => [...savingsKeys.all, 'list'] as const,
    list: (params?: Record<string, unknown>) => [...savingsKeys.lists(), params] as const,
    details: () => [...savingsKeys.all, 'detail'] as const,
    detail: (id: number) => [...savingsKeys.details(), id] as const,
    transactions: (id: number, filters?: Record<string, unknown>) =>
        [...savingsKeys.detail(id), 'transactions', filters] as const,
    notes: (id: number) => [...savingsKeys.detail(id), 'notes'] as const,
    smsRules: (id: number) => [...savingsKeys.detail(id), 'sms-rules'] as const,
};

const DEFAULT_SMS_ALERT_CONFIG: Partial<SavingsSmsAlertConfig> = {
    notifyOnDeposit: true,
    notifyOnWithdrawal: true,
    notifyOnEarningInterest: true,
    notifyOnEarningDividends: true,
    notifyOnOutgoingTransfer: true,
    notifyOnIncomingTransfer: true,
    notifyOnDirectDebit: true,
    notifyOnDirectCredit: true,
    notifyOnStandingOrder: true,
    notifyOnLoanApplication: true,
    notifyOnLoanDisbursement: true,
    notifyOnLoanPayment: true,
    notifyOnLoanRefund: true,
    notifyOnHold: true,
};

/**
 * Formats a Date instance to Fineract's "dd MMMM yyyy" format.
 */
export function formatFineractTransactionDate(date: Date = new Date()): string {
    const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December',
    ];
    return `${date.getDate()} ${monthNames[date.getMonth()]} ${date.getFullYear()}`;
}

export const savingsService = {
    /**
     * Fetch paginated savings accounts.
     */
    async getAccounts(
        params?: SavingsListParams,
        signal?: AbortSignal
    ): Promise<PaginatedResponse<SavingsAccountSummary>> {
        const res = await apiClient.get<PaginatedResponse<SavingsAccountSummary>>('/savingsaccounts', {
            params: {
                limit: params?.limit ?? 15,
                offset: params?.offset ?? 0,
                orderBy: params?.orderBy ?? 'id',
                sortOrder: params?.sortOrder ?? 'DESC',
                ...(params?.status ? { status: params.status } : {}),
                ...(params?.search ? { search: params.search } : {}),
            },
            signal,
        });
        return res.data;
    },

    /**
     * Fetch a savings account by ID.
     */
    async getAccountById(
        accountId: number,
        associations: string = 'all',
        signal?: AbortSignal
    ): Promise<SavingsAccountSummary> {
        const res = await apiClient.get<SavingsAccountSummary>(
            `/savingsaccounts/${accountId}?associations=${associations}`,
            { signal }
        );
        return res.data;
    },

    /**
     * Fetch ledger transactions for a savings account.
     */
    async getTransactions(
        accountId: number,
        params?: Record<string, unknown>,
        useSearchEndpoint: boolean = false,
        signal?: AbortSignal
    ): Promise<PaginatedResponse<SavingsTransaction>> {
        const endpoint = useSearchEndpoint
            ? `/savingsaccounts/${accountId}/transactions/search`
            : `/savingsaccounts/${accountId}/transactions`;
        const res = await apiClient.get<PaginatedResponse<SavingsTransaction>>(endpoint, {
            params,
            signal,
        });
        return res.data;
    },

    /**
     * Fetch notes for a savings account.
     */
    async getNotes(accountId: number, signal?: AbortSignal): Promise<NoteItem[]> {
        try {
            const res = await apiClient.get<NoteItem[]>(`/savingsaccounts/${accountId}/notes`, {
                signal,
            });
            return res.data || [];
        } catch {
            return [];
        }
    },

    /**
     * Add a note to a savings account.
     */
    async addNote(accountId: number, noteText: string): Promise<void> {
        await apiClient.post(`/savingsaccounts/${accountId}/notes`, { note: noteText });
    },

    /**
     * Fetch SMS alert rules for a savings account.
     */
    async getSmsAlertRules(
        accountId: number,
        signal?: AbortSignal
    ): Promise<Partial<SavingsSmsAlertConfig>> {
        try {
            const res = await apiClient.get<Partial<SavingsSmsAlertConfig>>(
                `/savingsaccounts/${accountId}/smsalertrules`,
                { signal }
            );
            return res.data || DEFAULT_SMS_ALERT_CONFIG;
        } catch {
            return DEFAULT_SMS_ALERT_CONFIG;
        }
    },

    /**
     * Update SMS alert rules for a savings account.
     */
    async updateSmsAlertRules(
        accountId: number,
        rules: Partial<SavingsSmsAlertConfig>
    ): Promise<void> {
        await apiClient.put(`/savingsaccounts/${accountId}/smsalertrules`, rules);
    },

    /**
     * Execute a command on a savings account (e.g. 'block', 'unblock').
     */
    async executeCommand(accountId: number, command: string): Promise<void> {
        await apiClient.post(`/savingsaccounts/${accountId}?command=${command}`, {});
    },

    /**
     * Record a deposit to a savings account.
     */
    async recordDeposit(
        accountId: number,
        payload: DepositWithdrawalPayload
    ): Promise<FineractTransactionResponse> {
        const formattedDate = payload.transactionDate || formatFineractTransactionDate();
        const res = await apiClient.post<FineractTransactionResponse>(
            `/savingsaccounts/${accountId}/transactions?command=deposit`,
            {
                dateFormat: 'dd MMMM yyyy',
                locale: 'en',
                transactionDate: formattedDate,
                transactionAmount: payload.amount,
                paymentTypeId: payload.paymentTypeId ?? 1,
                note: payload.note || 'Counter Cash Deposit',
            }
        );
        return res.data;
    },

    /**
     * Record a withdrawal from a savings account.
     */
    async recordWithdrawal(
        accountId: number,
        payload: DepositWithdrawalPayload
    ): Promise<FineractTransactionResponse> {
        const formattedDate = payload.transactionDate || formatFineractTransactionDate();
        const res = await apiClient.post<FineractTransactionResponse>(
            `/savingsaccounts/${accountId}/transactions?command=withdrawal`,
            {
                dateFormat: 'dd MMMM yyyy',
                locale: 'en',
                transactionDate: formattedDate,
                transactionAmount: payload.amount,
                paymentTypeId: payload.paymentTypeId ?? 1,
                note: payload.note || 'Counter Cash Withdrawal',
            }
        );
        return res.data;
    },

    /**
     * Place a funds hold on a savings account.
     */
    async holdAmount(
        accountId: number,
        payload: HoldAmountPayload
    ): Promise<FineractTransactionResponse> {
        const formattedDate = payload.transactionDate || new Date().toISOString().split('T')[0];
        const res = await apiClient.post<FineractTransactionResponse>(
            `/savingsaccounts/${accountId}/holdamount`,
            {
                amount: payload.amount,
                reasonForHold: payload.reasonForHold,
                transactionDate: formattedDate,
                dateFormat: 'yyyy-MM-dd',
                locale: 'en',
            }
        );
        return res.data;
    },
};
