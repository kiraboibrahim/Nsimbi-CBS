'use client';

import React from 'react';
import Link from 'next/link';

export function LoginForgotPassword() {
  return (
    <div className="w-full max-w-[24rem] pt-4 text-center">
      <Link
        href="/auth/forgot-password"
        className="text-xs uppercase font-semibold tracking-wider text-muted-foreground hover:text-foreground underline underline-offset-4 transition-colors"
        data-test="forgot-password-button"
      >
        Forgot Password ?
      </Link>
    </div>
  );
}
