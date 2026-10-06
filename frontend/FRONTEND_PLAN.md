# NSIMBI CBS Frontend Implementation Plan

## 1. Executive Summary & Brand Alignment

The **NSIMBI Core Banking System (CBS)** frontend is the primary operating portal for SACCO tellers, loan officers, branch managers, auditors, and administrators. 

### Key Principles:
1. **NSIMBI Brand & Design System**:
   - Strict visual parity with `admin-portal`.
   - Primary Accent: Brand Red (`#ED4747`), Hover (`#D32F2F`), Subtle Tint (`rgba(237, 71, 71, 0.1)`).
   - Dark Mode First: Base background `#0A0A0A`, elevated cards `#161616`, borders `#262626`, foreground `#FAFAFA`.
   - Typography: Clean Google fonts — `Inter` for interface typography and `IBM Plex Mono` for currency figures, account numbers, and transaction IDs.
2. **Simple, Human Language**:
   - Avoid archaic core banking jargon. Replace confusing terms with clear language:
     - *"CIF / Client Master"* $\rightarrow$ **"Member Records"**
     - *"Passbook Savings Account"* $\rightarrow$ **"Savings Account"**
     - *"Hold / Lien Amount"* $\rightarrow$ **"Reserved / Locked Funds"**
     - *"Term Deposit Premature Liquidation"* $\rightarrow$ **"Early Fixed Deposit Withdrawal"**
     - *"Maker-Checker Segregation"* $\rightarrow$ **"Two-Person Review & Approval"**
     - *"Overdraft Facility / Mandatory Reserve"* $\rightarrow$ **"Minimum Required Balance (UGX 5,000)"**
3. **Responsive, Fast, & Error-Resilient**:
   - Immediate feedback with Sonner toast notifications and contextual inline validation.
   - Dual-balance calculation dynamically visible at the cash counter (Available = Actual Balance − Reserved Funds − UGX 5,000 Minimum Reserve).
   - Automatic 5-minute inactivity session lock modal (FR-001 & FR-012).

---

## 2. Technology Stack & Directory Architecture

Built inside `frontend/` with:
- **Framework**: Next.js (App Router) + TypeScript + React 19
- **Styling**: Tailwind CSS v4 (`@tailwindcss/postcss`) with CSS custom properties matching `admin-portal`
- **UI & Icons**: Lucide React icons, Class Variance Authority (`cva`), `clsx`, `tailwind-merge`
- **Notifications**: Sonner toasts
- **State & Server Cache**: `@tanstack/react-query` + Axios with Fineract tenant header (`Fineract-Platform-TenantId: default`)
- **Forms**: React Hook Form + Zod schema validation

---

## 3. Screen-by-Screen Feature Specifications

### 3.1 Authentication & Security Shell (FR-001, FR-012)
- **Login View**: Branded card with NSIMBI logo, username, password, and branch office selector.
- **Global Inactivity Lock**: 5-minute idle lock overlay requiring password to resume.
- **Top Navigation Bar**: Branch Selector, Real-time Teller Float counter (`Till Cash: UGX 14,250,000`), active staff user avatar, and Dark/Light toggle.

### 3.2 Member Master & CIF Directory (Module 2: FR-005 to FR-010)
- **Members Directory (`/members`)**: Search by Name, NIN, Phone, or Member ID across Individuals, Groups, Joint Accounts, and Institutions.
- **Member Registration Wizard (`/members/new`)**: 4-in-1 step-by-step registration with document attachments.
- **Approvals Queue (`/members/approvals`)**: Segregation of Duties vetting and approval.

### 3.3 Savings Accounts & Cash Desk Counter (Module 3: FR-015 to FR-017)
- **Savings Directory (`/savings`)**: Deterministic 12-digit account numbers, Member names, Available vs Actual balance.
- **Cash Desk Counter (`/savings/desk`)**: Dual-balance calculation ($Available = Actual - Holds - UGX\ 5,000$), fast deposit receipt generator, withdrawal transaction limit enforcement.
- **Account Detail & Settings (`/savings/[id]`)**: Holds, freeze toggles, and 14 SMS transaction alert toggles.

### 3.4 Fixed Deposits Subsystem (Module 3: FR-018 to FR-019)
- **Fixed Deposits Directory (`/fixed-deposits`)**: Status filters (`Pending`, `Running`, `Matured`, `Liquidated`).
- **Booking Wizard (`/fixed-deposits/new`)**: Till Cash vs Savings Offset funding with 5k reserve enforcement.
- **Fixed Deposit Detail & Actions (`/fixed-deposits/[id]`)**: Accrued interest preview, term certificate, early withdrawal calculator.

### 3.5 Standing Orders & Debit Cards (Module 3: FR-020 to FR-021)
- **Standing Orders (`/standing-orders`)**: Recurring transfer schedule table, execution history, maker-checker authorization.
- **Debit Card Management (`/debit-cards`)**: Request, approval, and physical card issuance tracking.

### 3.6 Administration & Staff Controls (Module 1: FR-001 to FR-004)
- **Staff Limits & Governance (`/admin/limits`)**: Teller deposit/withdrawal transaction limit configurations.
- **Operating Hours (`/admin/hours`)**: Role branch access schedule configuration.

---

## 4. Phased Implementation Roadmap

| Phase | Focus Area | Deliverables |
|---|---|---|
| **Phase 1** | App Shell, Theme & Auth | Next.js app setup, Fineract Axios client, AuthGuard, Sidebar, Topbar with till counters, Inactivity Lock Modal. |
| **Phase 2** | Member / CIF Engine | Member Directory, 4-in-1 Onboarding Wizard, Maker-Checker Review Queue, Member Profile. |
| **Phase 3** | Savings & Cash Desk | Savings Directory, Cash Desk Counter (Dual Balance + 5k Reserve), Holds/Freezes, 14 SMS Alert Toggles. |
| **Phase 4** | Fixed Deposits | Placements Directory, Booking Form (Till Cash vs Offset), Maturity & Termination Actions. |
| **Phase 5** | Standing Orders & Cards | Recurring Transfers Queue, Debit Card Request & Issuance Tracking. |
| **Phase 6** | Governance & Polish | Staff Limits, Operating Hours, Polish & End-to-End User Testing. |
