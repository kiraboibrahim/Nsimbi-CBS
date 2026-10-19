import { apiClient } from '@/lib/api';
import { PaginatedResponse } from '@/types/api';
import { LoanAccount } from '@/types/loans';

export type LoanSearchField = 'accountNo' | 'externalId' | 'clientId';

export interface LoanListParams {
  limit?: number;
  offset?: number;
  orderBy?: string;
  sortOrder?: 'ASC' | 'DESC';
  status?: string;
  accountNo?: string;
  externalId?: string;
  clientId?: number;
}

export const loanKeys = {
  all: ['loans'] as const,
  lists: () => [...loanKeys.all, 'list'] as const,
  list: (params: LoanListParams) => [...loanKeys.lists(), params] as const,
  details: () => [...loanKeys.all, 'detail'] as const,
  detail: (id: number) => [...loanKeys.details(), id] as const,
};

export const loansService = {
  async getLoans(params: LoanListParams, signal?: AbortSignal): Promise<PaginatedResponse<LoanAccount>> {
    const response = await apiClient.get<PaginatedResponse<LoanAccount>>('/loans', {
      params: {
        limit: params.limit ?? 15,
        offset: params.offset ?? 0,
        orderBy: params.orderBy ?? 'id',
        sortOrder: params.sortOrder ?? 'DESC',
        associations: 'summary',
        ...(params.status ? { status: params.status } : {}),
        ...(params.accountNo ? { accountNo: params.accountNo } : {}),
        ...(params.externalId ? { externalId: params.externalId } : {}),
        ...(params.clientId !== undefined ? { clientId: params.clientId } : {}),
      },
      signal,
    });
    return response.data;
  },

  async getLoan(id: number, signal?: AbortSignal): Promise<LoanAccount> {
    const response = await apiClient.get<LoanAccount>(`/loans/${id}`, {
      params: { associations: 'repaymentSchedule,transactions' },
      signal,
    });
    return response.data;
  },
};
