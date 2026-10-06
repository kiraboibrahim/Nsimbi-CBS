export interface ApiFieldError {
  field: string;
  message: string;
  code?: string;
}

export interface ApiErrorPayload {
  message: string;
  status: number;
  code?: string;
  developerMessage?: string;
  fieldErrors?: Record<string, string>;
  timestamp?: string;
}

/**
 * Normalized error representing HTTP, network, or validation failures.
 */
export class ApiError extends Error implements ApiErrorPayload {
  public readonly status: number;
  public readonly code?: string;
  public readonly developerMessage?: string;
  public readonly fieldErrors?: Record<string, string>;
  public readonly timestamp: string;

  constructor(payload: ApiErrorPayload) {
    super(payload.message);
    this.name = 'ApiError';
    this.status = payload.status;
    this.code = payload.code;
    this.developerMessage = payload.developerMessage;
    this.fieldErrors = payload.fieldErrors;
    this.timestamp = payload.timestamp || new Date().toISOString();

    Object.setPrototypeOf(this, ApiError.prototype);
  }
}

/**
 * Paginated response envelope for collection endpoints.
 */
export interface PaginatedResponse<T> {
  totalFilteredRecords: number;
  pageItems: T[];
}

/**
 * Standard query parameters for paginated API requests.
 */
export interface PaginationParams {
  offset?: number;
  limit?: number;
  paged?: boolean;
  orderBy?: string;
  sortOrder?: 'ASC' | 'DESC';
}
