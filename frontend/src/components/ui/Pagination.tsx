'use client';

import * as React from 'react';
import { Button } from './Button';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export interface PaginationProps {
    currentPage: number;
    totalPages: number;
    totalCount: number;
    pageSize: number;
    onPageChange: (page: number) => void;
    isLoading?: boolean;
}

/**
 * Reusable Pagination component modeled after Swift Disburse design.
 */
export function Pagination({
    currentPage,
    totalPages,
    totalCount,
    pageSize,
    onPageChange,
    isLoading = false,
}: PaginationProps) {
    if (totalPages <= 1 && totalCount <= pageSize) return null;

    const startItem = totalCount === 0 ? 0 : (currentPage - 1) * pageSize + 1;
    const endItem = Math.min(currentPage * pageSize, totalCount);

    return (
        <div className="flex items-center justify-between px-6 py-3.5 border-t border-border bg-muted/20">
            <div className="flex-1 text-xs font-medium text-muted-foreground tabular-nums">
                Showing {startItem}–{endItem} of {totalCount} items
            </div>
            <div className="flex items-center gap-1.5">
                <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => onPageChange(currentPage - 1)}
                    disabled={currentPage === 1 || isLoading}
                    className="h-8 w-8 p-0 rounded-lg border border-transparent hover:border-border transition-all font-semibold"
                >
                    <ChevronLeft className="h-4 w-4" />
                </Button>

                <div className="flex items-center gap-1">
                    {Array.from({ length: Math.min(totalPages, 5) }).map((_, i) => {
                        let pageNum = currentPage;
                        if (currentPage <= 3) {
                            pageNum = i + 1;
                        } else if (currentPage >= totalPages - 2) {
                            pageNum = totalPages - 4 + i;
                        } else {
                            pageNum = currentPage - 2 + i;
                        }

                        if (pageNum <= 0 || pageNum > totalPages) return null;

                        const isSelected = currentPage === pageNum;

                        return (
                            <Button
                                key={pageNum}
                                variant={isSelected ? 'default' : 'ghost'}
                                size="sm"
                                onClick={() => onPageChange(pageNum)}
                                disabled={isLoading}
                                className={`h-8 w-8 p-0 rounded-lg text-xs font-semibold transition-all tabular-nums ${
                                    isSelected
                                        ? 'bg-primary text-primary-foreground font-bold shadow-xs'
                                        : 'hover:bg-accent border border-transparent hover:border-border'
                                }`}
                            >
                                {pageNum}
                            </Button>
                        );
                    })}
                </div>

                <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => onPageChange(currentPage + 1)}
                    disabled={currentPage >= totalPages || isLoading}
                    className="h-8 w-8 p-0 rounded-lg border border-transparent hover:border-border transition-all font-semibold"
                >
                    <ChevronRight className="h-4 w-4" />
                </Button>
            </div>
        </div>
    );
}
