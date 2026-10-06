'use client';

import { AppLayout } from '@/shared/AppLayout';
import { AuthGuard } from '@/components/common/AuthGuard';

export default function DashboardGroupLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AuthGuard>
      <AppLayout>{children}</AppLayout>
    </AuthGuard>
  );
}
