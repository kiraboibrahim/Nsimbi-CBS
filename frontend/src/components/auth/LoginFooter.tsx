'use client';

import React from 'react';

export function LoginFooter() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="w-full mt-auto py-4 border-t border-border/40">
      <div className="container max-w-5xl mx-auto px-4 text-xs text-muted-foreground text-center">
        <span>&copy; {currentYear} NSIMBI SACCO. All rights reserved.</span>
      </div>
    </footer>
  );
}
