'use client';

import React from 'react';
import { AlertTriangle, Trash2 } from 'lucide-react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';

interface DeleteConfirmationModalProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => Promise<void> | void;
    title: string;
    description: string;
    entityName?: string;
    isLoading?: boolean;
}

export function DeleteConfirmationModal({
    isOpen,
    onClose,
    onConfirm,
    title,
    description,
    entityName,
    isLoading = false,
}: DeleteConfirmationModalProps) {
    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            preventClose={isLoading}
            maxWidth="md"
        >
            <div className="space-y-4 pt-0.5">
                {/* Warning Icon & Header */}
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-destructive/10 text-destructive border border-destructive/15 flex items-center justify-center shrink-0">
                        <AlertTriangle className="w-4.5 h-4.5" />
                    </div>
                    <div>
                        <h3 className="text-sm font-semibold text-foreground tracking-tight">{title}</h3>
                        <p className="text-xs text-muted-foreground mt-0.5 leading-normal">{description}</p>
                    </div>
                </div>

                {entityName && (
                    <div className="px-3.5 py-2.5 rounded-xl bg-muted/40 border border-border/60 text-xs flex items-center justify-between gap-2">
                        <span className="text-muted-foreground font-normal shrink-0">Target Record</span>
                        <span className="font-medium text-foreground truncate max-w-[240px]">{entityName}</span>
                    </div>
                )}

                <div className="p-3 rounded-xl bg-destructive/5 border border-destructive/15 text-xs text-destructive/90 flex items-start gap-2.5 leading-relaxed">
                    <Trash2 className="w-4 h-4 shrink-0 mt-0.5 opacity-80" />
                    <p>
                        This action is <span className="font-medium text-destructive">permanent</span> and will completely erase this record from the database. It cannot be reversed.
                    </p>
                </div>

                {/* Footer Buttons */}
                <div className="flex items-center justify-end gap-2 pt-3 border-t border-border/60">
                    <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={onClose}
                        disabled={isLoading}
                        className="font-normal text-muted-foreground hover:text-foreground"
                    >
                        Cancel
                    </Button>
                    <Button
                        type="button"
                        variant="danger"
                        size="sm"
                        onClick={onConfirm}
                        loading={isLoading}
                        icon={<Trash2 className="w-3.5 h-3.5" />}
                        className="bg-destructive hover:bg-destructive/90 text-destructive-foreground font-medium"
                    >
                        Delete Permanently
                    </Button>
                </div>
            </div>
        </Modal>
    );
}
