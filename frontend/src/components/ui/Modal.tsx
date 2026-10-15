'use client';

import * as React from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { X } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: any[]) {
    return twMerge(clsx(inputs));
}

interface DialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    children: React.ReactNode;
    preventClose?: boolean;
}

const DialogContext = React.createContext<{ onOpenChange: (open: boolean) => void; preventClose?: boolean } | null>(null);

export function Dialog({ open, children, onOpenChange, preventClose = false }: DialogProps) {
    const handleBackdropClick = (e: React.MouseEvent<HTMLDivElement>) => {
        if (e.target === e.currentTarget && !preventClose) {
            onOpenChange(false);
        }
    };

    return (
        <DialogContext.Provider value={{ onOpenChange, preventClose }}>
            <AnimatePresence>
                {open && (
                    <motion.div
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
                        className="fixed inset-0 z-[110] flex items-start justify-center bg-black/60 backdrop-blur-xs p-3 sm:p-4 overflow-y-auto"
                        onClick={handleBackdropClick}
                    >
                        <div className="relative w-full my-auto pointer-events-none">
                            {children}
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </DialogContext.Provider>
    );
}

export function DialogContent({ children, className, hideCloseButton = false }: { children: React.ReactNode; className?: string; hideCloseButton?: boolean }) {
    const context = React.useContext(DialogContext);

    return (
        <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 12 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 12 }}
            transition={{ duration: 0.22, ease: [0.16, 1, 0.3, 1] }}
            className={cn(
                'relative w-full max-w-lg mx-auto pointer-events-auto bg-card border border-border rounded-2xl shadow-2xl overflow-hidden text-xs text-foreground flex flex-col max-h-[88vh]',
                className
            )}
            onClick={(e) => e.stopPropagation()}
        >
            {context && !context.preventClose && !hideCloseButton && (
                <Button
                    variant="ghost"
                    size="icon"
                    className="absolute right-3.5 top-3.5 z-50 rounded-full bg-muted/50 hover:bg-muted text-muted-foreground transition-colors h-7 w-7 cursor-pointer shrink-0"
                    onClick={() => context.onOpenChange(false)}
                    type="button"
                >
                    <X className="h-3.5 w-3.5" />
                </Button>
            )}
            {children}
        </motion.div>
    );
}

export function DialogHeader({ children, className }: { children: React.ReactNode; className?: string }) {
    return (
        <div className={cn('bg-card px-5 py-4 border-b border-border space-y-1 text-left shrink-0', className)}>
            {children}
        </div>
    );
}

export function DialogTitle({ children, className }: { children: React.ReactNode; className?: string }) {
    return (
        <h2 className={cn('text-sm font-semibold leading-snug tracking-tight text-foreground flex items-center gap-2.5', className)}>
            {children}
        </h2>
    );
}

export function DialogDescription({ children, className }: { children: React.ReactNode; className?: string }) {
    return (
        <p className={cn('text-[11px] text-muted-foreground font-normal leading-relaxed', className)}>
            {children}
        </p>
    );
}

export function DialogFooter({ children, className }: { children: React.ReactNode; className?: string }) {
    return (
        <div className={cn('px-5 py-3 border-t border-border bg-muted/20 flex items-center justify-end gap-2 rounded-b-xl shrink-0', className)}>
            {children}
        </div>
    );
}


export interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    description?: string;
    icon?: React.ReactNode;
    iconClassName?: string;
    children: React.ReactNode;
    maxWidth?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | '4xl' | '5xl' | '6xl' | 'full';
    fullScreen?: boolean;
    footer?: React.ReactNode;
    preventClose?: boolean;
    className?: string;
}

export function Modal({
    isOpen,
    onClose,
    title,
    description,
    icon,
    iconClassName,
    children,
    maxWidth = 'md',
    fullScreen = false,
    footer,
    preventClose = false,
    className,
}: ModalProps) {
    const isFull = fullScreen || maxWidth === 'full';

    const maxWidthStyles = {
        sm: 'max-w-sm',
        md: 'max-w-md',
        lg: 'max-w-lg',
        xl: 'max-w-xl',
        '2xl': 'max-w-2xl',
        '3xl': 'max-w-3xl',
        '4xl': 'max-w-4xl',
        '5xl': 'max-w-5xl',
        '6xl': 'max-w-6xl',
        full: 'w-screen h-screen max-w-none rounded-none m-0 border-0 max-h-none fixed inset-0 z-[130] bg-background flex flex-col overflow-hidden h-screen',
    };

    return (
        <Dialog open={isOpen} onOpenChange={(open) => { if (!open) onClose(); }} preventClose={preventClose}>
            <DialogContent className={cn(isFull ? maxWidthStyles.full : maxWidthStyles[maxWidth], className)} hideCloseButton={isFull}>
                {!isFull && (title || description) && (
                    <DialogHeader>
                        <div className="flex items-start gap-3 pr-6">
                            {icon && (
                                <div className={cn('h-8 w-8 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center text-primary shrink-0 mt-0.5', iconClassName)}>
                                    {icon}
                                </div>
                            )}
                            <div className="space-y-0.5">
                                {title && <DialogTitle>{title}</DialogTitle>}
                                {description && <DialogDescription>{description}</DialogDescription>}
                            </div>
                        </div>
                    </DialogHeader>
                )}

                <div className={isFull ? 'flex-1 flex flex-col min-h-0 overflow-hidden' : 'p-4 sm:p-5 overflow-y-auto flex-1 flex flex-col min-h-0'}>
                    {children}
                </div>

                {!isFull && footer && <DialogFooter>{footer}</DialogFooter>}
            </DialogContent>
        </Dialog>
    );
}
