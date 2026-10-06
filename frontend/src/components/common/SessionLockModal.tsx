'use client';

import React, { useState, useRef, useEffect } from 'react';
import { useAuth } from '@/context/auth-context';
import { Button } from '@/components/ui/Button';
import { Lock, LogOut, Eye, EyeOff, AlertCircle, ArrowUp } from 'lucide-react';

export function SessionLockModal() {
  const { isLocked, user, unlockSession, logout } = useAuth();
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [capsLockActive, setCapsLockActive] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [loading, setLoading] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  // Auto-focus password input whenever modal becomes locked
  useEffect(() => {
    if (isLocked) {
      setPassword('');
      setErrorMsg('');
      setShowPassword(false);
      setCapsLockActive(false);
      const timer = setTimeout(() => {
        inputRef.current?.focus();
      }, 50);
      return () => clearTimeout(timer);
    }
  }, [isLocked]);

  if (!isLocked || !user) return null;

  const userName = user.username || 'Staff User';
  const userOffice = user.officeName || 'Head Office';
  const userInitials = userName
    .split(' ')
    .filter(Boolean)
    .map((n) => n[0])
    .join('')
    .substring(0, 2)
    .toUpperCase();

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    setCapsLockActive(e.getModifierState('CapsLock'));
  };

  const handleKeyUp = (e: React.KeyboardEvent<HTMLInputElement>) => {
    setCapsLockActive(e.getModifierState('CapsLock'));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!password) return;
    setErrorMsg('');
    setLoading(true);
    const success = await unlockSession(password);
    if (!success) {
      setErrorMsg('Incorrect password. Please verify your credentials and try again.');
      inputRef.current?.focus();
    }
    setLoading(false);
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="session-lock-title"
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xl p-4 animate-in fade-in duration-200"
    >
      <div className="w-full max-w-md rounded-2xl border border-border bg-card p-6 sm:p-7 shadow-2xl space-y-5">
        {/* Top Header & Security Status */}
        <div className="text-center space-y-2">
          <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-secondary border border-border text-primary shadow-xs">
            <Lock className="h-6 w-6 text-primary" aria-hidden="true" />
          </div>
          <div>
            <h2 id="session-lock-title" className="text-lg font-bold tracking-tight text-foreground">
              Session Locked
            </h2>
            <p className="text-xs text-muted-foreground mt-1 max-w-xs mx-auto leading-relaxed">
              Paused due to 5 minutes of inactivity. All in-progress transactions and open forms are safely preserved.
            </p>
          </div>
        </div>

        {/* User Identity Card matching Global Design System */}
        <div className="rounded-xl border border-border bg-secondary/40 p-3 flex items-center gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary border border-primary/20 font-bold text-xs select-none">
            {userInitials}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs font-semibold text-foreground truncate">{userName}</p>
            <p className="text-[11px] text-muted-foreground truncate mt-0.5">{userOffice}</p>
          </div>
        </div>

        {/* Authentication Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <div className="flex items-center justify-between">
              <label htmlFor="unlock-password" className="text-xs font-medium text-muted-foreground">
                Enter Password to Resume
              </label>
              {capsLockActive && (
                <span className="inline-flex items-center gap-1 text-[11px] font-medium text-amber-500 bg-amber-500/10 px-2 py-0.5 rounded-md border border-amber-500/20">
                  <ArrowUp className="w-3 h-3" /> Caps Lock is ON
                </span>
              )}
            </div>

            <div className="relative flex items-center">
              <input
                id="unlock-password"
                ref={inputRef}
                type={showPassword ? 'text' : 'password'}
                placeholder="••••••••"

                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  if (errorMsg) setErrorMsg('');
                }}
                onKeyDown={handleKeyDown}
                onKeyUp={handleKeyUp}
                autoComplete="current-password"
                required
                className="flex h-10 w-full rounded-lg border border-border bg-card px-3.5 pr-10 text-sm font-normal text-foreground placeholder:text-muted-foreground/40 outline-none transition-colors focus:border-zinc-400 dark:focus:border-zinc-500 focus:ring-1 focus:ring-zinc-500/20"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                tabIndex={-1}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                className="absolute right-2.5 p-1 rounded-md text-muted-foreground/70 hover:text-foreground transition-colors cursor-pointer"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* Reserved Inline Error Notification */}
          {errorMsg && (
            <div
              role="alert"
              className="flex items-center gap-2 p-2.5 rounded-lg bg-destructive/10 border border-destructive/20 text-destructive text-xs font-medium animate-in fade-in"
            >
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          {/* Primary Action Button */}
          <Button
            type="submit"
            loading={loading}
            disabled={!password.trim() || loading}
            className="w-full h-10 text-xs font-semibold cursor-pointer shadow-sm"
          >
            Unlock Session
          </Button>

          {/* Secondary Action */}
          <div className="flex justify-center pt-1">
            <button
              type="button"
              onClick={logout}
              className="inline-flex items-center gap-1.5 text-xs text-muted-foreground hover:text-destructive transition-colors cursor-pointer py-1 px-2.5 rounded-md"
            >
              <LogOut className="h-3.5 w-3.5" />
              <span>Sign out</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
