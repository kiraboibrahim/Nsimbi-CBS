'use client';

import React, { Suspense } from 'react';
import { useAuth } from '@/context/auth-context';
import { LoginHeader } from '@/components/auth/LoginHeader';
import { LoginFormCard } from '@/components/auth/LoginFormCard';
import { TwoFactorFormCard } from '@/components/auth/TwoFactorFormCard';
import { LoginForgotPassword } from '@/components/auth/LoginForgotPassword';
import { LoginFooter } from '@/components/auth/LoginFooter';

export default function LoginPage() {
  const { twoFactorState } = useAuth();

  return (
    <div className="min-h-screen bg-background flex flex-col justify-between selection:bg-primary/20 selection:text-primary">
      <main className="container max-w-md mx-auto px-4 py-12 flex-1 flex flex-col items-center justify-center">
        <div className="w-full max-w-[24rem] flex flex-col items-center">
          {/* Brand Logo directly above form */}
          <LoginHeader />

          {/* Form Card or 2FA Verification Card */}
          <Suspense fallback={<div className="h-64 flex items-center justify-center text-sm text-muted-foreground">Loading credentials...</div>}>
            {twoFactorState ? (
              <TwoFactorFormCard />
            ) : (
              <>
                <LoginFormCard />
                <LoginForgotPassword />
              </>
            )}
          </Suspense>
        </div>
      </main>

      {/* Footer */}
      <LoginFooter />
    </div>
  );
}
