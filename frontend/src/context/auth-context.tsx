'use client';

import React, { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { toApiError } from '@/lib/api';
import { ApiError } from '@/types/api';
import { authService } from '@/services/auth.service';
import { toast } from 'sonner';

export interface UserRole {
  id: number;
  name: string;
  description?: string;
}

export interface AuthenticatedUser {
  userId: number;
  username: string;
  officeId: number;
  officeName: string;
  roles: UserRole[];
  permissions: string[];
  base64EncodedAuthenticationKey?: string;
}

export interface TwoFactorState {
  pendingUser: AuthenticatedUser;
  base64Key: string;
  deliveryMethod: string;
  deliveryTarget: string;
  tokenLiveTimeInSec: number;
}

export interface LoginResponse {
  success: boolean;
  requires2FA?: boolean;
  error?: ApiError;
}

interface AuthContextType {
  user: AuthenticatedUser | null;
  loading: boolean;
  isLocked: boolean;
  tillCash: number;
  setTillCash: React.Dispatch<React.SetStateAction<number>>;
  twoFactorState: TwoFactorState | null;
  login: (username: string, password: string) => Promise<LoginResponse>;
  verifyTwoFactorOtp: (otp: string) => Promise<{ success: boolean; error?: ApiError }>;
  resendTwoFactorOtp: () => Promise<{ success: boolean; error?: ApiError }>;
  cancelTwoFactor: () => void;
  logout: () => void;
  lockSession: () => void;
  unlockSession: (password: string) => Promise<boolean>;
  hasPermission: (permission: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const INACTIVITY_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes inactivity lock

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [isLocked, setIsLocked] = useState<boolean>(false);
  const [twoFactorState, setTwoFactorState] = useState<TwoFactorState | null>(null);
  const [tillCash, setTillCash] = useState<number>(24500000); // 24.5M UGX default till float
  const router = useRouter();

  useEffect(() => {
    try {
      const storedAuth = localStorage.getItem('nsimbi_cbs_auth');
      const storedUser = localStorage.getItem('nsimbi_cbs_user');
      const lockedState = localStorage.getItem('nsimbi_cbs_locked');

      if (storedAuth && storedUser) {
        setUser(JSON.parse(storedUser));
        if (lockedState === 'true') {
          setIsLocked(true);
        }
      }
    } catch (e) {
      console.error('Error hydrating auth state:', e);
    } finally {
      setLoading(false);
    }
  }, []);

  const lockSession = useCallback(() => {
    if (user && !isLocked) {
      setIsLocked(true);
      localStorage.setItem('nsimbi_cbs_locked', 'true');
    }
  }, [user, isLocked]);

  // Inactivity timer
  useEffect(() => {
    if (!user || isLocked) return;

    let timeoutId: NodeJS.Timeout;

    const resetTimer = () => {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        lockSession();
      }, INACTIVITY_TIMEOUT_MS);
    };

    const activityEvents = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart'];
    activityEvents.forEach((evt) => window.addEventListener(evt, resetTimer, { passive: true }));
    resetTimer();

    return () => {
      clearTimeout(timeoutId);
      activityEvents.forEach((evt) => window.removeEventListener(evt, resetTimer));
    };
  }, [user, isLocked, lockSession]);

  const login = async (username: string, password: string): Promise<LoginResponse> => {
    try {
      setLoading(true);
      const data = await authService.authenticate(username, password);
      const base64Key = data.base64EncodedAuthenticationKey;
      const authenticatedUser: AuthenticatedUser = {
        userId: data.userId,
        username: data.username,
        officeId: data.officeId,
        officeName: data.officeName || 'Head Office',
        roles: data.roles || [],
        permissions: data.permissions || [],
        base64EncodedAuthenticationKey: base64Key,
      };

      if (data.isTwoFactorAuthenticationRequired) {
        // Fetch 2FA delivery methods from Fineract
        const methods = await authService.getTwoFactorDeliveryMethods(base64Key);
        const defaultMethod = methods[0] || { name: 'email', target: 'Email on file' };

        // Dispatch initial OTP token to user's registered delivery method
        const otpReq = await authService.requestTwoFactorOtp(
          base64Key,
          defaultMethod.name,
          true
        );

        const liveTime = otpReq.tokenLiveTimeInSec || 300;

        setTwoFactorState({
          pendingUser: authenticatedUser,
          base64Key,
          deliveryMethod: defaultMethod.name,
          deliveryTarget: defaultMethod.target,
          tokenLiveTimeInSec: liveTime,
        });

        return { success: true, requires2FA: true };
      }

      // 2FA not required for this user
      setUser(authenticatedUser);
      setIsLocked(false);
      localStorage.removeItem('nsimbi_cbs_locked');
      localStorage.setItem('nsimbi_cbs_auth', JSON.stringify({
        base64EncodedAuthenticationKey: base64Key,
      }));
      localStorage.setItem('nsimbi_cbs_user', JSON.stringify(authenticatedUser));

      toast.success(`Welcome back, ${data.username}!`);
      router.push('/');
      return { success: true, requires2FA: false };
    } catch (error) {
      const apiError = toApiError(error);
      return { success: false, error: apiError };
    } finally {
      setLoading(false);
    }
  };

  const verifyTwoFactorOtp = async (otp: string): Promise<{ success: boolean; error?: ApiError }> => {
    if (!twoFactorState) {
      return {
        success: false,
        error: new ApiError({
          message: 'Authentication session expired. Please sign in again.',
          status: 401,
          code: 'SESSION_EXPIRED',
        }),
      };
    }

    try {
      setLoading(true);
      const res = await authService.validateTwoFactorOtp(twoFactorState.base64Key, otp);
      const tfaToken = res.token;
      if (!tfaToken) {
        throw new ApiError({
          message: 'Verification failed: No token returned by server.',
          status: 500,
          code: 'NO_TOKEN_RETURNED',
        });
      }

      // Persist Fineract TFA Token and User Session
      localStorage.setItem('nsimbi_cbs_tfa_token', tfaToken);
      localStorage.setItem(
        'nsimbi_cbs_auth',
        JSON.stringify({
          base64EncodedAuthenticationKey: twoFactorState.base64Key,
        })
      );
      localStorage.setItem('nsimbi_cbs_user', JSON.stringify(twoFactorState.pendingUser));
      localStorage.removeItem('nsimbi_cbs_locked');

      setUser(twoFactorState.pendingUser);
      setTwoFactorState(null);
      setIsLocked(false);

      toast.success(`Welcome back, ${twoFactorState.pendingUser.username}!`);
      router.push('/');
      return { success: true };
    } catch (error) {
      const apiError = toApiError(error);
      return { success: false, error: apiError };
    } finally {
      setLoading(false);
    }
  };

  const resendTwoFactorOtp = async (): Promise<{ success: boolean; error?: ApiError }> => {
    if (!twoFactorState) {
      return {
        success: false,
        error: new ApiError({
          message: 'Authentication session expired. Please sign in again.',
          status: 401,
          code: 'SESSION_EXPIRED',
        }),
      };
    }

    try {
      await authService.requestTwoFactorOtp(
        twoFactorState.base64Key,
        twoFactorState.deliveryMethod,
        true
      );
      toast.success(`A new verification code has been sent.`);
      return { success: true };
    } catch (error) {
      const apiError = toApiError(error);
      return { success: false, error: apiError };
    }
  };

  const cancelTwoFactor = () => {
    setTwoFactorState(null);
  };

  const unlockSession = async (password: string): Promise<boolean> => {
    if (!user) return false;
    try {
      await authService.authenticate(user.username, password);
      setIsLocked(false);
      localStorage.removeItem('nsimbi_cbs_locked');
      toast.success('Session unlocked');
      return true;
    } catch {
      toast.error('Incorrect password. Please verify your credentials.');
      return false;
    }
  };

  const logout = () => {
    setUser(null);
    setTwoFactorState(null);
    setIsLocked(false);
    localStorage.removeItem('nsimbi_cbs_auth');
    localStorage.removeItem('nsimbi_cbs_user');
    localStorage.removeItem('nsimbi_cbs_tfa_token');
    localStorage.removeItem('nsimbi_cbs_locked');
    router.push('/login');
    toast.info('Signed out successfully.');
  };

  const hasPermission = (permission: string): boolean => {
    if (!user) return false;
    if (user.permissions.includes('ALL_FUNCTIONS')) return true;
    return user.permissions.includes(permission);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        isLocked,
        tillCash,
        setTillCash,
        twoFactorState,
        login,
        verifyTwoFactorOtp,
        resendTwoFactorOtp,
        cancelTwoFactor,
        logout,
        lockSession,
        unlockSession,
        hasPermission,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
