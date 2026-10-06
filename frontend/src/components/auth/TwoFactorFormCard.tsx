'use client';

import React, { useState, useRef, useEffect, useCallback } from 'react';
import { useAuth } from '@/context/auth-context';
import { extractErrorMessage } from '@/lib/api';
import { ShieldCheck, Loader2, AlertCircle, RotateCw, ArrowLeft, Clock } from 'lucide-react';

function maskDeliveryTarget(target: string): string {
  if (!target) return 'your registered email';
  if (target.includes('@')) {
    const [local, domain] = target.split('@');
    if (local.length <= 3) {
      return `${local[0]}***@${domain}`;
    }
    const start = local.slice(0, 2);
    const end = local.slice(-2);
    return `${start}***${end}@${domain}`;
  }
  // Phone number masking
  if (target.length >= 7) {
    return `${target.slice(0, 4)}****${target.slice(-3)}`;
  }
  return target;
}

const OTP_LENGTH = 5;
const RESEND_COOLDOWN = 60;

export function TwoFactorFormCard() {
  const { twoFactorState, verifyTwoFactorOtp, resendTwoFactorOtp, cancelTwoFactor } = useAuth();
  const [digits, setDigits] = useState<string[]>(Array(OTP_LENGTH).fill(''));
  const [isVerifying, setIsVerifying] = useState(false);
  const [isResending, setIsResending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [timer, setTimer] = useState<number>(RESEND_COOLDOWN);

  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  // Focus the first input cell on mount
  useEffect(() => {
    inputRefs.current[0]?.focus();
  }, []);

  // Cooldown countdown timer
  useEffect(() => {
    if (timer <= 0) return;
    const interval = setInterval(() => {
      setTimer((prev) => (prev > 0 ? prev - 1 : 0));
    }, 1000);
    return () => clearInterval(interval);
  }, [timer]);

  const submitOtp = useCallback(
    async (codeToVerify: string) => {
      if (codeToVerify.length !== OTP_LENGTH || isVerifying) return;
      setError(null);
      setIsVerifying(true);
      try {
        const result = await verifyTwoFactorOtp(codeToVerify);
        if (!result.success) {
          setError(result.error?.message || 'The security code entered is invalid or has expired.');
          // Select and highlight current inputs for quick retry
          inputRefs.current[OTP_LENGTH - 1]?.focus();
        }
      } catch (err: unknown) {
        setError(extractErrorMessage(err));
      } finally {
        setIsVerifying(false);
      }
    },
    [isVerifying, verifyTwoFactorOtp]
  );

  const handleChange = (index: number, value: string) => {
    // Alphanumeric characters only, convert to uppercase
    const cleaned = value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
    if (!cleaned) {
      const nextDigits = [...digits];
      nextDigits[index] = '';
      setDigits(nextDigits);
      return;
    }

    const char = cleaned.slice(-1);
    const nextDigits = [...digits];
    nextDigits[index] = char;
    setDigits(nextDigits);
    setError(null);

    // Auto-advance to next input
    if (index < OTP_LENGTH - 1) {
      inputRefs.current[index + 1]?.focus();
    } else {
      // Last box entered: check if full
      const fullCode = nextDigits.join('');
      if (fullCode.length === OTP_LENGTH) {
        submitOtp(fullCode);
      }
    }
  };

  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace') {
      if (!digits[index] && index > 0) {
        // Move backwards and clear previous
        const nextDigits = [...digits];
        nextDigits[index - 1] = '';
        setDigits(nextDigits);
        inputRefs.current[index - 1]?.focus();
      } else {
        const nextDigits = [...digits];
        nextDigits[index] = '';
        setDigits(nextDigits);
      }
    } else if (e.key === 'ArrowLeft' && index > 0) {
      inputRefs.current[index - 1]?.focus();
    } else if (e.key === 'ArrowRight' && index < OTP_LENGTH - 1) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
    if (!pasted) return;

    const chars = pasted.slice(0, OTP_LENGTH).split('');
    const nextDigits = Array(OTP_LENGTH).fill('');
    chars.forEach((char, i) => {
      nextDigits[i] = char;
    });
    setDigits(nextDigits);
    setError(null);

    if (chars.length === OTP_LENGTH) {
      inputRefs.current[OTP_LENGTH - 1]?.focus();
      submitOtp(nextDigits.join(''));
    } else {
      inputRefs.current[chars.length]?.focus();
    }
  };

  const handleResend = async () => {
    if (timer > 0 || isResending) return;
    setIsResending(true);
    setError(null);
    try {
      const res = await resendTwoFactorOtp();
      if (res.success) {
        setTimer(RESEND_COOLDOWN);
        setDigits(Array(OTP_LENGTH).fill(''));
        inputRefs.current[0]?.focus();
      } else if (res.error) {
        setError(res.error.message);
      }
    } finally {
      setIsResending(false);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const fullCode = digits.join('');
    submitOtp(fullCode);
  };

  const maskedTarget = maskDeliveryTarget(twoFactorState?.deliveryTarget || '');
  const isComplete = digits.every((d) => d.length > 0);

  return (
    <div
      className="rounded-xl bg-card p-8 border border-border shadow-xl w-full max-w-[24rem] space-y-6 animate-in fade-in-50 duration-200"
      data-test="two-factor-card"
    >
      {/* Icon & Heading */}
      <div className="text-center space-y-2">
        <div className="w-12 h-12 rounded-xl bg-primary/10 border border-primary/20 flex items-center justify-center mx-auto text-primary">
          <ShieldCheck className="w-6 h-6 stroke-[2]" />
        </div>
        <div>
          <h2 className="text-lg font-semibold text-foreground tracking-tight">
            Two-Factor Verification
          </h2>
          <p className="text-xs text-muted-foreground mt-1 leading-relaxed">
            Enter the 5-digit verification code sent to{' '}
            <span className="font-medium text-foreground">{maskedTarget}</span>
          </p>
        </div>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-lg bg-destructive/10 border border-destructive/20 p-3 text-xs text-destructive">
          <AlertCircle className="h-4 w-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {/* 5-Cell OTP Form */}
      <form onSubmit={handleSubmit} className="space-y-5">
        <div className="flex justify-between items-center gap-2.5" onPaste={handlePaste}>
          {digits.map((digit, idx) => (
            <input
              key={idx}
              ref={(el) => {
                inputRefs.current[idx] = el;
              }}
              type="text"
              inputMode="text"
              maxLength={1}
              value={digit}
              onChange={(e) => handleChange(idx, e.target.value)}
              onKeyDown={(e) => handleKeyDown(idx, e)}
              className={`w-12 h-13 text-center text-lg font-mono font-bold uppercase rounded-lg bg-background border transition-all ${
                error
                  ? 'border-destructive text-destructive focus:ring-2 focus:ring-destructive/20 focus:border-destructive'
                  : 'border-border/80 text-foreground focus:outline-none focus:ring-2 focus:ring-zinc-500/20 focus:border-zinc-400 dark:focus:border-zinc-500'
              }`}
              autoComplete="one-time-code"
              aria-label={`Digit ${idx + 1}`}
            />
          ))}
        </div>

        {/* Resend Section with Countdown */}
        <div className="text-center min-h-[1.75rem] flex items-center justify-center">
          {timer > 0 ? (
            <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
              <Clock className="w-3.5 h-3.5" />
              <span>Resend code in {timer}s</span>
            </div>
          ) : (
            <button
              type="button"
              onClick={handleResend}
              disabled={isResending}
              className="text-xs font-medium text-primary hover:text-primary/80 transition-colors flex items-center gap-1.5 disabled:opacity-50"
            >
              <RotateCw className={`w-3.5 h-3.5 ${isResending ? 'animate-spin' : ''}`} />
              <span>{isResending ? 'Sending new code...' : 'Resend verification code'}</span>
            </button>
          )}
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={!isComplete || isVerifying}
          className="w-full h-11 rounded-lg bg-primary hover:bg-primary/90 text-primary-foreground font-semibold text-sm transition-all duration-200 shadow-md hover:shadow-lg flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
          data-test="verify-otp-button"
        >
          {isVerifying ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              <span>Verifying Code...</span>
            </>
          ) : (
            <span>Verify & Sign In</span>
          )}
        </button>

        {/* Cancel / Switch Account */}
        <button
          type="button"
          onClick={cancelTwoFactor}
          disabled={isVerifying}
          className="w-full text-xs text-muted-foreground hover:text-foreground transition-colors flex items-center justify-center gap-1.5 pt-1 disabled:opacity-50"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to sign in</span>
        </button>
      </form>
    </div>
  );
}
