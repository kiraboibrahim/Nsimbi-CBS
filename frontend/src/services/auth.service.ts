import { apiClient } from '@/lib/api';

export interface AuthenticationResponse {
  userId: number;
  username: string;
  officeId: number;
  officeName?: string;
  roles?: Array<{ id: number; name: string; description?: string; disabled?: boolean }>;
  permissions?: string[];
  base64EncodedAuthenticationKey: string;
  authenticated: boolean;
  isTwoFactorAuthenticationRequired?: boolean;
}

export interface TwoFactorDeliveryMethod {
  name: string;
  target: string;
}

export interface TwoFactorOtpResponse {
  requestTime: number;
  tokenLiveTimeInSec: number;
  extendedAccessToken: boolean;
  deliveryMethod: TwoFactorDeliveryMethod;
}

export interface TwoFactorValidateResponse {
  token: string;
  validFrom: number;
  validTo: number;
}

export const authService = {
  /**
   * Authenticate user credentials.
   */
  async authenticate(username: string, password: string): Promise<AuthenticationResponse> {
    const res = await apiClient.post<AuthenticationResponse>('/authentication', {
      username,
      password,
    });
    return res.data;
  },

  /**
   * Get available 2FA delivery methods for the authenticated user.
   */
  async getTwoFactorDeliveryMethods(base64Key: string): Promise<TwoFactorDeliveryMethod[]> {
    const res = await apiClient.get<TwoFactorDeliveryMethod[]>('/twofactor', {
      headers: {
        Authorization: `Basic ${base64Key}`,
      },
    });
    return res.data || [];
  },

  /**
   * Request dispatch of a two-factor verification code.
   */
  async requestTwoFactorOtp(
    base64Key: string,
    deliveryMethod: string = 'email',
    extendedToken: boolean = true
  ): Promise<TwoFactorOtpResponse> {
    const res = await apiClient.post<TwoFactorOtpResponse>(
      `/twofactor?deliveryMethod=${deliveryMethod}&extendedToken=${extendedToken}`,
      {},
      {
        headers: {
          Authorization: `Basic ${base64Key}`,
        },
      }
    );
    return res.data;
  },

  /**
   * Validate a two-factor verification code.
   */
  async validateTwoFactorOtp(
    base64Key: string,
    token: string
  ): Promise<TwoFactorValidateResponse> {
    const res = await apiClient.post<TwoFactorValidateResponse>(
      `/twofactor/validate?token=${encodeURIComponent(token.trim())}`,
      {},
      {
        headers: {
          Authorization: `Basic ${base64Key}`,
        },
      }
    );
    return res.data;
  },
};
