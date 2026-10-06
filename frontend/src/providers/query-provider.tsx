"use client"

import React from "react"
import { QueryClient, QueryClientProvider } from "@tanstack/react-query"

function makeQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30 * 1000, // 30 seconds fresh window to prevent spamming endpoints on rapid navigation
        refetchOnWindowFocus: process.env.NODE_ENV === "production",
        refetchOnReconnect: true,
        retry: (failureCount, error: unknown) => {
          // Do not retry on 429 Throttled or 401/403/404 client errors
          const status = (error as { response?: { status?: number } })?.response?.status;
          if (status === 429 || status === 401 || status === 403 || status === 404) {
            return false;
          }
          return failureCount < 2;
        },
      },
    },
  })
}

let browserQueryClient: QueryClient | undefined = undefined

function getQueryClient() {
  if (typeof window === "undefined") {
    return makeQueryClient()
  } else {
    if (!browserQueryClient) browserQueryClient = makeQueryClient()
    return browserQueryClient
  }
}

export function QueryProvider({ children }: { children: React.ReactNode }) {
  const queryClient = getQueryClient()

  return (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  )
}
