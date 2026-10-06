'use client';

import React from 'react';
import { useAuth } from '@/context/auth-context';
import { useTheme } from '@/theme/ThemeContext';
import { formatUGX } from '@/lib/formatters';
import {
  Menu,
  Moon,
  Sun,
  Lock,
  LogOut,
  Building,
  Coins,
} from 'lucide-react';

export function Header({ onOpenMobileMenu }: { onOpenMobileMenu: () => void }) {
  const { user, tillCash, lockSession, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();

  return (
    <header className="sticky top-0 z-30 flex h-16 w-full items-center justify-between border-b border-border bg-card/80 px-4 backdrop-blur-md sm:px-6">
      <div className="flex items-center gap-3">
        <button
          onClick={onOpenMobileMenu}
          className="rounded-lg p-2 text-muted-foreground hover:bg-secondary lg:hidden"
          aria-label="Open menu"
        >
          <Menu className="h-5 w-5" />
        </button>

        {/* Branch */}
        <div className="hidden sm:flex items-center gap-2 rounded-xl border border-border bg-secondary/40 px-3 py-1.5 text-xs">
          <Building className="h-3.5 w-3.5 text-muted-foreground" />
          <span className="font-medium text-foreground">
            {user?.officeName || 'Head Office'}
          </span>
        </div>
      </div>

      <div className="flex items-center gap-2.5 sm:gap-3">
        {/* Cash Drawer */}
        <a
          href="/savings/desk"
          className="flex items-center gap-2 rounded-xl border border-border bg-secondary/30 px-3 py-1.5 hover:border-primary/40 hover:bg-secondary/60 transition-colors"
          title="Open Cash Desk"
        >
          <Coins className="h-3.5 w-3.5 text-primary" />
          <div className="flex items-center gap-1.5 text-xs">
            <span className="text-muted-foreground font-medium hidden md:inline">Till:</span>
            <span className="font-mono font-semibold text-foreground">
              {formatUGX(tillCash)}
            </span>
          </div>
        </a>

        {/* Lock Screen Button */}
        <button
          onClick={lockSession}
          title="Lock screen"
          className="flex h-9 w-9 items-center justify-center rounded-xl border border-border text-muted-foreground hover:bg-secondary hover:text-foreground transition-colors"
        >
          <Lock className="h-4 w-4" />
        </button>

        {/* Dark/Light mode toggle */}
        <button
          onClick={toggleTheme}
          title="Toggle theme"
          className="flex h-9 w-9 items-center justify-center rounded-xl border border-border text-muted-foreground hover:bg-secondary hover:text-foreground transition-colors"
        >
          {theme === 'dark' ? (
            <Sun className="h-4 w-4 text-amber-400" />
          ) : (
            <Moon className="h-4 w-4" />
          )}
        </button>

        {/* User profile */}
        <div className="flex items-center gap-2.5 pl-1 sm:pl-2 border-l border-border">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-primary text-primary-foreground font-bold text-xs">
            {user?.username ? user.username.charAt(0).toUpperCase() : 'U'}
          </div>
          <div className="hidden md:flex flex-col text-left">
            <span className="text-xs font-semibold text-foreground leading-tight">
              {user?.username || 'Staff'}
            </span>
            <span className="text-[11px] text-muted-foreground">
              {user?.roles?.[0]?.name || 'Officer'}
            </span>
          </div>

          <button
            onClick={logout}
            title="Sign out"
            className="flex h-8 w-8 items-center justify-center rounded-lg text-muted-foreground hover:bg-destructive/10 hover:text-destructive transition-colors ml-1"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      </div>
    </header>
  );
}
