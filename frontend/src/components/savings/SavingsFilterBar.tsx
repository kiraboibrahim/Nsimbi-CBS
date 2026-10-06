'use client';

import React from 'react';
import { Search, Filter, RefreshCw } from 'lucide-react';

export type SavingsStatusFilter = 'all' | 'active' | 'inactive' | 'frozen' | 'closed';

interface SavingsFilterBarProps {
  search: string;
  onSearchChange: (value: string) => void;
  showFilters: boolean;
  onToggleFilters: () => void;
  statusFilter: SavingsStatusFilter;
  onStatusFilterChange: (status: SavingsStatusFilter) => void;
  onRefresh: () => void;
  isFetching: boolean;
}

const STATUS_OPTIONS: SavingsStatusFilter[] = ['all', 'active', 'inactive', 'frozen', 'closed'];

export function SavingsFilterBar({
  search,
  onSearchChange,
  showFilters,
  onToggleFilters,
  statusFilter,
  onStatusFilterChange,
  onRefresh,
  isFetching,
}: SavingsFilterBarProps) {
  return (
    <>
      {/* Search & Action Controls */}
      <div className="flex flex-col sm:flex-row w-full border-b border-border justify-between items-stretch sm:items-center p-3 px-6 gap-3">
        <div className="relative flex items-center w-full max-w-md">
          <Search className="w-4 h-4 text-muted-foreground absolute left-3 pointer-events-none" />
          <input
            type="text"
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search by account no, member name..."
            className="w-full pl-9 pr-4 py-2 text-sm bg-background border border-border rounded-lg outline-none focus:border-zinc-400 dark:focus:border-zinc-500 focus:ring-1 focus:ring-zinc-500/20 transition-colors text-foreground placeholder:text-muted-foreground/60"
          />
        </div>

        <div className="flex items-center gap-3 justify-end">
          <button
            onClick={onRefresh}
            title="Refresh"
            className="p-2 text-muted-foreground hover:text-foreground rounded-lg border border-border bg-background transition-colors"
          >
            <RefreshCw className={`w-4 h-4 ${isFetching ? 'animate-spin text-primary' : ''}`} />
          </button>

          <button
            onClick={onToggleFilters}
            className="tracking-wider uppercase text-xs font-bold text-primary hover:text-primary-hover flex items-center gap-1.5 py-2 px-3 rounded-lg border border-primary/20 bg-primary/5 transition-colors"
          >
            <Filter className="w-3.5 h-3.5" />
            {showFilters ? 'Hide Filters' : 'Show Filters'}
          </button>
        </div>
      </div>

      {/* Filter Radio Group */}
      {showFilters && (
        <div className="p-3 px-6 bg-muted/20 border-b border-border text-xs flex items-center gap-4 flex-wrap">
          <span className="font-bold text-foreground">Filter By:</span>
          {STATUS_OPTIONS.map((opt) => (
            <label key={opt} className="inline-flex items-center gap-1.5 cursor-pointer">
              <input
                type="radio"
                name="statusFilters"
                checked={statusFilter === opt}
                onChange={() => onStatusFilterChange(opt)}
                className="text-primary accent-primary"
              />
              <span className="capitalize text-muted-foreground hover:text-foreground font-medium">
                {opt}
              </span>
            </label>
          ))}
        </div>
      )}
    </>
  );
}
