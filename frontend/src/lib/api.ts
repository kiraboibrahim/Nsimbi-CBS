import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { toast } from 'sonner';
import { ApiError } from '@/types/api';

export const API_URL =
  process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/fineract-provider/api/v1';

export const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Fineract-Platform-TenantId': 'default',
    'Cache-Control': 'no-cache, no-store, must-revalidate',
    Pragma: 'no-cache',
  },
});

/**
 * Normalizes any error (Axios, Network, DOM, Fineract) into an ApiError.
 */
export function toApiError(error: unknown): ApiError {
  if (error instanceof ApiError) {
    return error;
  }

  if (!axios.isAxiosError(error)) {
    if (error instanceof Error && error.message) {
      return new ApiError({
        message: error.message,
        status: 0,
        code: 'GENERIC_ERROR',
      });
    }
    return new ApiError({
      message: 'An unexpected error occurred. Please try again.',
      status: 0,
      code: 'UNKNOWN_ERROR',
    });
  }

  const axiosErr = error as AxiosError<any>;

  if (!axiosErr.response) {
    return new ApiError({
      message: 'Unable to connect. Please check your internet connection and try again.',
      status: 0,
      code: 'NETWORK_UNREACHABLE',
      developerMessage: axiosErr.message,
    });
  }

  const status = axiosErr.response.status;
  const data = axiosErr.response.data;

  const fieldErrors: Record<string, string> = {};
  let defaultMessage = data?.defaultUserMessage || data?.message;
  let code = data?.userMessageGlobalisationCode;

  if (Array.isArray(data?.errors) && data.errors.length > 0) {
    for (const err of data.errors) {
      if (err.parameterName && (err.defaultUserMessage || err.developerMessage)) {
        fieldErrors[err.parameterName] = err.defaultUserMessage || err.developerMessage;
      }
    }
    const first = data.errors[0];
    defaultMessage = defaultMessage || first.defaultUserMessage || first.developerMessage;
    code = code || first.userMessageGlobalisationCode;
  }

  if (typeof data === 'string') {
    defaultMessage = data;
  }

  return new ApiError({
    message: defaultMessage || 'Service is temporarily unavailable. Please try again.',
    status,
    code,
    developerMessage: data?.developerMessage || axiosErr.message,
    fieldErrors: Object.keys(fieldErrors).length > 0 ? fieldErrors : undefined,
  });
}

/**
 * Extracts a safe, user-friendly message from any error.
 */
export function extractErrorMessage(error: unknown): string {
  return toApiError(error).message;
}

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (typeof window !== 'undefined') {
      const auth = localStorage.getItem('nsimbi_cbs_auth');
      if (auth) {
        try {
          const parsed = JSON.parse(auth);
          if (parsed?.base64EncodedAuthenticationKey) {
            config.headers.Authorization = `Basic ${parsed.base64EncodedAuthenticationKey}`;
          } else if (parsed?.token) {
            config.headers.Authorization = `Bearer ${parsed.token}`;
          }
        } catch {
          // ignore corrupted storage
        }
      }

      const tfaToken = localStorage.getItem('nsimbi_cbs_tfa_token');
      if (tfaToken) {
        config.headers['Fineract-Platform-TFA-Token'] = tfaToken;
      }
    }
    return config;
  },
  (error) => Promise.reject(toApiError(error))
);

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const apiError = toApiError(error);
    const status = apiError.status;
    const url: string = error.config?.url || '';

    if ((status === 401 || status === 403) && typeof window !== 'undefined') {
      if (!url.includes('/authentication') && !url.includes('/twofactor')) {
        localStorage.removeItem('nsimbi_cbs_auth');
        localStorage.removeItem('nsimbi_cbs_user');
        localStorage.removeItem('nsimbi_cbs_tfa_token');
        if (!window.location.pathname.startsWith('/login')) {
          window.location.href = '/login?expired=true';
        }
      }
      return Promise.reject(apiError);
    }

    if (!url.includes('/authentication') && !url.includes('/twofactor') && status >= 500) {
      toast.error(apiError.message, {
        id: `server-error-${status}`,
      });
    }

    return Promise.reject(apiError);
  }
);
