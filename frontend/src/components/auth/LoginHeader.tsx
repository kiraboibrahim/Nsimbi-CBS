import React from 'react';
import Image from 'next/image';

export function LoginHeader() {
  return (
    <div className="mb-6 flex items-center justify-center">
      <Image
        src="/Logo.png"
        alt="NSIMBI Logo"
        width={160}
        height={42}
        className="h-9 w-auto object-contain dark:brightness-110"
        priority
      />
    </div>
  );
}
