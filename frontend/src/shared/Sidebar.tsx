'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { motion, AnimatePresence } from 'framer-motion';
import {
  LayoutDashboard,
  Users,
  Wallet,
  ArrowRightLeft,
  CheckSquare,
  Briefcase,
  Building2,
  Percent,
  ShieldAlert,
  FileBarChart,
  Settings,
  ChevronLeft,
  ChevronRight,
  X,
  LogOut,
} from 'lucide-react';
import { clsx } from 'clsx';
import { useAuth } from '@/context/auth-context';

interface NavItem {
  name: string;
  href: string;
  icon: React.ReactNode;
  badge?: string;
  adminOnly?: boolean;
}

interface NavGroup {
  title: string;
  items: NavItem[];
}

interface SidebarProps {
  mobileOpen?: boolean;
  setMobileOpen?: (open: boolean) => void;
}

export function Sidebar({ mobileOpen = false, setMobileOpen }: SidebarProps) {
  const pathname = usePathname();
  const [collapsed, setCollapsed] = useState(false);
  const { user, logout } = useAuth();

  const userName = user?.username || 'Staff User';
  const userRole = user?.roles?.[0]?.name || 'Officer';

  const userInitials = userName
    .split(' ')
    .filter(Boolean)
    .map((n) => n[0])
    .join('')
    .substring(0, 2)
    .toUpperCase();

  useEffect(() => {
    if (setMobileOpen) {
      setMobileOpen(false);
    }
  }, [pathname, setMobileOpen]);

  const navGroups: NavGroup[] = [
    {
      title: 'MAIN',
      items: [
        { name: 'Dashboard', href: '/', icon: <LayoutDashboard className="w-4 h-4" /> },
        { name: 'Members', href: '/members', icon: <Users className="w-4 h-4" /> },
        { name: 'Savings Accounts', href: '/savings', icon: <Wallet className="w-4 h-4" /> },
        { name: 'Cash Desk', href: '/savings/desk', icon: <ArrowRightLeft className="w-4 h-4" /> },
      ],
    },
    {
      title: 'ADMINISTRATION',
      items: [
        { name: 'Approvals', href: '/members/approvals', icon: <CheckSquare className="w-4 h-4" />, badge: 'Queue' },
        { name: 'Staff & Roles', href: '/admin/users', icon: <Briefcase className="w-4 h-4" /> },
        { name: 'Branches', href: '/admin/offices', icon: <Building2 className="w-4 h-4" /> },
        { name: 'Fees & Rates', href: '/admin/products', icon: <Percent className="w-4 h-4" /> },
      ],
    },
    {
      title: 'SYSTEM & AUDIT',
      items: [
        { name: 'Audit Logs', href: '/audit', icon: <ShieldAlert className="w-4 h-4" /> },
        { name: 'Reports', href: '/reports', icon: <FileBarChart className="w-4 h-4" /> },
        { name: 'Settings', href: '/settings', icon: <Settings className="w-4 h-4" /> },
      ],
    },
  ];

  const isCurrent = (href: string) => {
    if (href === '/') return pathname === '/';
    if (pathname === href) return true;
    if (href === '/savings') {
      return pathname.startsWith('/savings') && !pathname.startsWith('/savings/desk');
    }
    if (href === '/members') {
      return pathname.startsWith('/members') && !pathname.startsWith('/members/approvals');
    }
    return pathname.startsWith(href + '/');
  };

  const renderNavItems = (isMobile = false) => (
    <div className="flex-1 overflow-y-auto px-3 py-4 space-y-6">
      {navGroups.map((group, idx) => (
        <div key={idx} className="space-y-1">
          {(!collapsed || isMobile) && (
            <h4 className="px-3 text-[10px] font-bold text-[var(--text-muted)] tracking-wider uppercase mb-1">
              {group.title}
            </h4>
          )}
          {group.items.map((item) => {
            const isActive = isCurrent(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={() => {
                  if (isMobile && setMobileOpen) setMobileOpen(false);
                }}
                className={clsx(
                  'flex items-center justify-between px-3 py-2 rounded-lg text-xs font-semibold transition-all group cursor-pointer',
                  isActive
                    ? 'bg-[var(--brand-red-light)] text-[var(--brand-red)] font-bold border-l-2 border-[var(--brand-red)]'
                    : 'text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-elevated)]',
                  collapsed && !isMobile && 'justify-center px-0'
                )}
                title={collapsed && !isMobile ? item.name : undefined}
              >
                <div className={clsx('flex items-center gap-3', collapsed && !isMobile && 'gap-0')}>
                  <span
                    className={clsx(
                      isActive
                        ? 'text-[var(--brand-red)]'
                        : 'text-[var(--text-muted)] group-hover:text-[var(--text-primary)]'
                    )}
                  >
                    {item.icon}
                  </span>
                  {(!collapsed || isMobile) && <span>{item.name}</span>}
                </div>
                {(!collapsed || isMobile) && item.badge && (
                  <span className="px-1.5 py-0.5 text-[10px] font-bold rounded-full bg-[var(--brand-red)] text-white">
                    {item.badge}
                  </span>
                )}
              </Link>
            );
          })}
        </div>
      ))}
    </div>
  );

  const renderUserFooter = (isMobile = false) => (
    <div className="p-3 border-t border-border bg-card">
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2.5 min-w-0 flex-1">
          <div className="w-8 h-8 rounded-full bg-[var(--brand-red-light)] text-[var(--brand-red)] border border-[var(--brand-red-border)] font-bold text-xs flex items-center justify-center shrink-0 shadow-xs select-none overflow-hidden">
            {userInitials}
          </div>
          {(!collapsed || isMobile) && (
            <div className="min-w-0 flex-1 overflow-hidden">
              <div className="flex items-center gap-1.5 min-w-0">
                <p className="text-xs font-semibold text-foreground truncate">{userName}</p>
                {userRole && (
                  <span className="text-[9px] font-semibold px-1.5 py-0.5 rounded-full bg-[var(--brand-red-light)] text-[var(--brand-red)] border border-[var(--brand-red-border)] shrink-0">
                    {userRole}
                  </span>
                )}
              </div>
            </div>
          )}
        </div>

        {(!collapsed || isMobile) && (
          <button
            type="button"
            onClick={() => {
              if (isMobile && setMobileOpen) setMobileOpen(false);
              logout();
            }}
            title="Log Out of System"
            className="p-1.5 rounded-lg hover:bg-red-500/10 text-muted-foreground hover:text-red-500 transition-colors cursor-pointer shrink-0"
            aria-label="Log out"
          >
            <LogOut className="w-4 h-4" />
          </button>
        )}
      </div>
    </div>
  );

  return (
    <>
      {/* 1. Desktop Fixed Sidebar */}
      <aside
        className={clsx(
          'h-screen sticky top-0 bg-card border-r border-border hidden lg:flex flex-col transition-all duration-200 z-30 select-none',
          collapsed ? 'w-16' : 'w-64'
        )}
      >
        {/* Brand Header */}
        <div className="flex items-center justify-between border-b border-border px-5 py-4 bg-card">
          {collapsed ? (
            <img
              src="/nsimbi-symbol.png"
              alt="NSIMBI"
              className="h-7 w-auto object-contain shrink-0 mx-auto"
            />
          ) : (
            <Link href="/" className="flex flex-col items-start gap-1 group">
              <img
                src="/Logo.png"
                alt="NSIMBI"
                className="h-4.5 w-auto object-contain brightness-95"
              />
              <span className="text-xs font-black text-muted-foreground/90 tracking-[0.42em] uppercase mt-1">
                CBS
              </span>
            </Link>
          )}
          <button
            type="button"
            onClick={() => setCollapsed(!collapsed)}
            className="p-1.5 rounded-lg hover:bg-muted text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
            aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
          </button>
        </div>

        {renderNavItems(false)}
        {renderUserFooter(false)}
      </aside>

      {/* 2. Mobile Slide-Over Drawer Overlay with Slide-In & Slide-Out Animations */}
      <AnimatePresence>
        {mobileOpen && (
          <div className="lg:hidden fixed inset-0 z-50 flex">
            {/* Backdrop Mask */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.2, ease: 'easeInOut' }}
              className="fixed inset-0 bg-black/60 backdrop-blur-xs"
              onClick={() => setMobileOpen?.(false)}
            />

            {/* Slide-In / Slide-Out Drawer Container */}
            <motion.div
              initial={{ x: '-100%' }}
              animate={{ x: 0 }}
              exit={{ x: '-100%' }}
              transition={{ type: 'spring', damping: 25, stiffness: 280 }}
              className="relative flex flex-col w-72 max-w-[82vw] bg-card border-r border-border h-full shadow-2xl z-50"
            >
              {/* Mobile Header with Close Button */}
              <div className="flex items-center justify-between border-b border-border px-5 py-4 bg-card">
                <Link
                  href="/"
                  onClick={() => setMobileOpen?.(false)}
                  className="flex flex-col items-start gap-1 group"
                >
                  <img
                    src="/Logo.png"
                    alt="NSIMBI"
                    className="h-4.5 w-auto object-contain brightness-95"
                  />
                  <span className="text-xs font-black text-muted-foreground/90 tracking-[0.42em] uppercase mt-1">
                    CBS
                  </span>
                </Link>
                <button
                  type="button"
                  onClick={() => setMobileOpen?.(false)}
                  className="p-1.5 rounded-lg hover:bg-muted text-muted-foreground hover:text-foreground transition-colors cursor-pointer"
                  aria-label="Close sidebar"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {renderNavItems(true)}
              {renderUserFooter(true)}
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </>
  );
}
