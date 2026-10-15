'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';

export interface TabItem {
  id: string;
  label: string;
  count?: number | string;
  icon?: React.ReactNode;
  href?: string;
  disabled?: boolean;
}

export interface TabsProps {
  tabs: TabItem[];
  activeTab?: string;
  onChange?: (id: string) => void;
  className?: string;
  size?: 'sm' | 'md';
}

export function Tabs({ tabs, activeTab, onChange, className, size = 'sm' }: TabsProps) {
  const pathname = usePathname();

  const sizeClasses =
    size === 'md'
      ? 'px-4 py-2.5 text-xs sm:text-sm'
      : 'px-3 sm:px-4 py-2 sm:py-2.5 text-xs';

  return (
    <div
      role="tablist"
      className={cn(
        'flex items-center gap-1 border-b border-border overflow-x-auto no-scrollbar scroll-smooth',
        className
      )}
    >
      {tabs.map((tab) => {
        const isActive = activeTab
          ? activeTab === tab.id || (tab.href ? activeTab === tab.href : false)
          : tab.href
            ? pathname === tab.href
            : false;

        const tabClasses = cn(
          'flex items-center gap-2 font-semibold border-b-2 transition-all select-none shrink-0 whitespace-nowrap focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-zinc-400 dark:focus-visible:ring-zinc-500',
          sizeClasses,
          tab.disabled
            ? 'opacity-40 cursor-not-allowed pointer-events-none border-transparent text-muted-foreground'
            : 'cursor-pointer',
          isActive
            ? 'border-primary text-primary bg-primary/10 rounded-t-lg'
            : 'border-transparent text-muted-foreground hover:text-foreground hover:border-border'
        );

        const content = (
          <>
            {tab.icon}
            <span>{tab.label}</span>
            {tab.count !== undefined && (
              <span
                className={cn(
                  'px-1.5 py-0.5 text-[10px] rounded-full font-medium border leading-none',
                  isActive
                    ? 'bg-primary text-primary-foreground border-primary'
                    : 'bg-muted text-muted-foreground border-border'
                )}
              >
                {tab.count}
              </span>
            )}
          </>
        );

        if (tab.href && !tab.disabled) {
          return (
            <Link
              key={tab.id}
              href={tab.href}
              role="tab"
              aria-selected={isActive}
              onClick={() => onChange?.(tab.id)}
              className={tabClasses}
            >
              {content}
            </Link>
          );
        }

        return (
          <button
            key={tab.id}
            type="button"
            role="tab"
            aria-selected={isActive}
            disabled={tab.disabled}
            onClick={() => !tab.disabled && onChange?.(tab.id)}
            className={tabClasses}
          >
            {content}
          </button>
        );
      })}
    </div>
  );
}
