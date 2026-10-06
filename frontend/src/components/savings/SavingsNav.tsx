'use client';

import React from 'react';
import { Tabs, TabItem } from '@/components/ui/Tabs';
import { Wallet, ArrowLeftRight, Landmark, FileText, CreditCard } from 'lucide-react';

const TABS: TabItem[] = [
    { id: 'savings-accounts', label: 'Savings Accounts', href: '/savings', icon: <Wallet className="w-4 h-4" /> },
    { id: 'cash-desk', label: 'Cash Desk', href: '/savings/desk', icon: <ArrowLeftRight className="w-4 h-4" /> },
    { id: 'fixed-deposits', label: 'Fixed Deposits', href: '/fixed-deposits', icon: <Landmark className="w-4 h-4" /> },
    { id: 'applications', label: 'Applications', href: '/savings/applications', icon: <FileText className="w-4 h-4" /> },
    { id: 'debit-cards', label: 'Debit Cards Issuance', href: '/debit-cards', icon: <CreditCard className="w-4 h-4" /> },
];

interface SavingsNavProps {
    className?: string;
}

export function SavingsNav({ className }: SavingsNavProps) {
    return <Tabs tabs={TABS} className={className} />;
}
