'use client';

import React from 'react';
import { useAuth } from '@/context/auth-context';
import { formatUGX, formatDateTime } from '@/lib/formatters';
import {
  Wallet,
  PiggyBank,
  CheckSquare,
  ArrowLeftRight,
  UserPlus,
  Clock,
  TrendingUp,
  ArrowUpRight,
  ArrowDownLeft,
  ChevronRight,
} from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';

export default function DashboardPage() {
  const { user, tillCash } = useAuth();

  const stats = [
    {
      title: 'Cash in Till',
      value: formatUGX(tillCash),
      caption: 'Physical cash available',
      icon: Wallet,
      color: 'text-emerald-500 bg-emerald-500/10',
      actionHref: '/savings/desk',
      actionText: 'Open Cash Desk',
    },
    {
      title: 'Savings Accounts',
      value: '1,428',
      caption: 'Active member accounts',
      icon: PiggyBank,
      color: 'text-blue-500 bg-blue-500/10',
      actionHref: '/savings',
      actionText: 'View Accounts',
    },
    {
      title: 'Fixed Deposits',
      value: 'UGX 845.2M',
      caption: 'Active placements',
      icon: TrendingUp,
      color: 'text-amber-500 bg-amber-500/10',
      actionHref: '/fixed-deposits',
      actionText: 'View Deposits',
    },
    {
      title: 'Pending Approvals',
      value: '7',
      caption: 'Awaiting second approval',
      icon: CheckSquare,
      color: 'text-rose-500 bg-rose-500/10',
      actionHref: '/members/approvals',
      actionText: 'Review Queue',
    },
  ];

  const quickActions = [
    {
      title: 'Cash Desk',
      desc: 'Deposit or withdraw cash for a member',
      icon: ArrowLeftRight,
      href: '/savings/desk',
      primary: true,
    },
    {
      title: 'Register Member',
      desc: 'Add a new individual, group, or institution',
      icon: UserPlus,
      href: '/members/new',
    },
    {
      title: 'Fixed Deposit',
      desc: 'Open a term deposit from cash or savings',
      icon: PiggyBank,
      href: '/fixed-deposits/new',
    },
    {
      title: 'Standing Order',
      desc: 'Set up automatic recurring transfers',
      icon: Clock,
      href: '/standing-orders',
    },
  ];

  const recentTransactions = [
    {
      id: 'TXN-8921',
      accountNo: '0035-0000-5281',
      memberName: 'Sarah Namubiru',
      type: 'Cash Deposit',
      direction: 'in',
      amount: 'UGX 350,000.00',
      date: new Date(Date.now() - 12 * 60 * 1000).toISOString(),
      status: 'Completed',
    },
    {
      id: 'TXN-8920',
      accountNo: '0035-0000-4112',
      memberName: 'Kato Paul & Mary',
      type: 'Cash Withdrawal',
      direction: 'out',
      amount: 'UGX 120,000.00',
      date: new Date(Date.now() - 45 * 60 * 1000).toISOString(),
      status: 'Completed',
    },
    {
      id: 'TXN-8919',
      accountNo: '0035-0000-1099',
      memberName: 'Wakiso Traders Group',
      type: 'Fixed Deposit',
      direction: 'in',
      amount: 'UGX 5,000,000.00',
      date: new Date(Date.now() - 110 * 60 * 1000).toISOString(),
      status: 'Completed',
    },
    {
      id: 'TXN-8918',
      accountNo: '0035-0000-3024',
      memberName: 'Emmanuel Otim',
      type: 'Standing Order',
      direction: 'out',
      amount: 'UGX 50,000.00',
      date: new Date(Date.now() - 180 * 60 * 1000).toISOString(),
      status: 'Completed',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Welcome header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-2 border-b border-border/60">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-foreground">
            Welcome back, {user?.username || 'Staff'}
          </h1>
          <p className="text-sm text-muted-foreground mt-0.5">
            {user?.officeName || 'Head Office'} &bull; {new Date().toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'short', year: 'numeric' })}
          </p>
        </div>

        <div>
          <a href="/savings/desk">
            <Button className="h-10 px-4 gap-2">
              <ArrowLeftRight className="h-4 w-4" />
              Cash Desk
            </Button>
          </a>
        </div>
      </div>

      {/* KPI Stats */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <div
              key={stat.title}
              className="rounded-2xl border border-border bg-card p-5 space-y-3 flex flex-col justify-between"
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-muted-foreground">
                  {stat.title}
                </span>
                <div className={`flex h-8 w-8 items-center justify-center rounded-lg ${stat.color}`}>
                  <Icon className="h-4 w-4" />
                </div>
              </div>

              <div>
                <p className="text-xl font-bold font-mono text-foreground">
                  {stat.value}
                </p>
                <p className="text-xs text-muted-foreground mt-0.5">{stat.caption}</p>
              </div>

              <div className="pt-2 border-t border-border/60">
                <a
                  href={stat.actionHref}
                  className="inline-flex items-center gap-1 text-xs font-semibold text-primary hover:underline"
                >
                  {stat.actionText}
                  <ChevronRight className="h-3 w-3" />
                </a>
              </div>
            </div>
          );
        })}
      </div>

      {/* Quick Actions */}
      <div className="space-y-3">
        <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
          Quick Actions
        </h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {quickActions.map((action) => {
            const Icon = action.icon;
            return (
              <a
                key={action.title}
                href={action.href}
                className={`rounded-2xl border p-5 transition-all flex flex-col justify-between hover:border-border/80 ${
                  action.primary
                    ? 'border-primary/30 bg-primary/5 hover:bg-primary/10'
                    : 'border-border bg-card hover:bg-secondary/40'
                }`}
              >
                <div className="space-y-2.5">
                  <div
                    className={`flex h-10 w-10 items-center justify-center rounded-xl ${
                      action.primary
                        ? 'bg-primary text-primary-foreground'
                        : 'bg-secondary text-foreground'
                    }`}
                  >
                    <Icon className="h-5 w-5" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold text-foreground">
                      {action.title}
                    </h3>
                    <p className="text-xs text-muted-foreground mt-0.5 leading-relaxed">
                      {action.desc}
                    </p>
                  </div>
                </div>

                <div className="mt-4 flex items-center gap-1 text-xs font-semibold text-primary">
                  <span>Open</span>
                  <ChevronRight className="h-3.5 w-3.5" />
                </div>
              </a>
            );
          })}
        </div>
      </div>

      {/* Recent Transactions */}
      <div className="rounded-2xl border border-border bg-card overflow-hidden shadow-xs">
        <div className="flex items-center justify-between border-b border-border p-4 sm:p-5">
          <div>
            <h2 className="text-sm font-bold text-foreground">Recent Transactions</h2>
            <p className="text-xs text-muted-foreground">Latest transactions at this branch</p>
          </div>
          <a href="/savings">
            <Button variant="outline" size="sm">
              View All
            </Button>
          </a>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-border bg-secondary/30 text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
              <tr>
                <th className="py-3 px-4 sm:px-5">Reference</th>
                <th className="py-3 px-4 sm:px-5">Account & Member</th>
                <th className="py-3 px-4 sm:px-5">Type</th>
                <th className="py-3 px-4 sm:px-5 text-right">Amount</th>
                <th className="py-3 px-4 sm:px-5 text-center">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {recentTransactions.map((tx) => (
                <tr key={tx.id} className="hover:bg-secondary/20 transition-colors">
                  <td className="py-3.5 px-4 sm:px-5">
                    <div className="font-mono text-xs font-semibold text-foreground">
                      {tx.id}
                    </div>
                    <div className="text-[11px] text-muted-foreground">
                      {formatDateTime(tx.date)}
                    </div>
                  </td>
                  <td className="py-3.5 px-4 sm:px-5">
                    <div className="font-medium text-foreground">{tx.memberName}</div>
                    <div className="font-mono text-xs text-muted-foreground">
                      {tx.accountNo}
                    </div>
                  </td>
                  <td className="py-3.5 px-4 sm:px-5">
                    <div className="flex items-center gap-2">
                      <div
                        className={`flex h-6 w-6 items-center justify-center rounded-full ${
                          tx.direction === 'in'
                            ? 'bg-emerald-500/10 text-emerald-500'
                            : 'bg-rose-500/10 text-rose-500'
                        }`}
                      >
                        {tx.direction === 'in' ? (
                          <ArrowDownLeft className="h-3.5 w-3.5" />
                        ) : (
                          <ArrowUpRight className="h-3.5 w-3.5" />
                        )}
                      </div>
                      <span className="text-xs font-medium text-foreground">
                        {tx.type}
                      </span>
                    </div>
                  </td>
                  <td className="py-3.5 px-4 sm:px-5 text-right font-mono text-xs font-bold text-foreground">
                    <span
                      className={
                        tx.direction === 'in' ? 'text-emerald-500' : 'text-foreground'
                      }
                    >
                      {tx.direction === 'in' ? '+' : '-'} {tx.amount}
                    </span>
                  </td>
                  <td className="py-3.5 px-4 sm:px-5 text-center">
                    <Badge variant="success" size="sm">
                      {tx.status}
                    </Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
