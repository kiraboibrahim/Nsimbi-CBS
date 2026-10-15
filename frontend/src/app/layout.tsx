import type { Metadata } from 'next';
import { Inter, IBM_Plex_Mono } from 'next/font/google';
import './globals.css';
import { QueryProvider } from '@/providers/query-provider';
import { AuthProvider } from '@/context/auth-context';
import { ThemeProvider } from '@/theme/ThemeContext';
import { Toaster } from 'sonner';
import NextTopLoader from 'nextjs-toploader';

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-sans',
  display: 'swap',
});

const ibmPlexMono = IBM_Plex_Mono({
  subsets: ['latin'],
  weight: ['400', '500', '600', '700'],
  variable: '--font-mono',
  display: 'swap',
});

export const metadata: Metadata = {
  title: {
    default: 'NSIMBI CBS — Core Banking Platform',
    template: '%s | NSIMBI CBS',
  },
  description:
    'Core Banking System for NSIMBI SACCO — Member Master, Savings Accounts, Cash Desk, Fixed Deposits, and Debit Card Operations.',
  icons: {
    icon: '/nsimbi-symbol.png',
    shortcut: '/nsimbi-symbol.png',
    apple: '/nsimbi-symbol.png',
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${inter.variable} ${ibmPlexMono.variable} h-full antialiased dark`}
      suppressHydrationWarning
    >
      <body className="min-h-full flex flex-col bg-background text-foreground font-sans">
        <NextTopLoader
          color="#ED4747"
          height={2.5}
          showSpinner={false}
          crawl={true}
          shadow={false}
        />
        <ThemeProvider>
          <QueryProvider>
            <AuthProvider>
              {children}
              <Toaster
                position="top-right"
                closeButton
                gap={8}
                duration={4500}
                theme="dark"
              />
            </AuthProvider>
          </QueryProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
