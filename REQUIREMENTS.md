# Backend Requirements Specification

**Target System:** SavingsPlus Core Banking System (CBS) / SACCO Management Platform  
**Target Organization:** NSIMBI SACCO LTD  
**Platform Vendor / Engine:** FutureLink Technologies (FLT) — SavingsPlus v5 (`*.sp5.app`)  
**Document Author:** Senior Backend Architect, Business Analyst & Requirements Engineer  
**Date:** September 2026  
**Document Status:** Complete Reverse-Engineered Master Specification (All 51 HTML Artifacts Integrated)  
**Classification:** Confidential / Technical Architecture  

---

## 1. Executive Summary

This document establishes the authoritative, reverse-engineered Backend Requirements Specification for the **SavingsPlus Core Banking System (CBS)** operated by **NSIMBI SACCO LTD**, developed on the **FutureLink Technologies (FLT)** SavingsPlus v5 enterprise microfinance platform.

The specification incorporates the comprehensive analysis of all **51 live DOM / Single-Page Application (SPA) runtime captures** extracted from `nsimbi.sp5.app`. Nothing from prior analyses has been discarded or compressed. The document unifies:
1. **Customer Information File (CIF) / Membership Engine:** Individual, Group, Joint, and Institutional onboarding with Maker-Checker vetting.
2. **Savings & Deposit Subsystem:** Liquid passbook savings (`FlexSave`), Available vs Actual dual-balance calculations (enforcing the `UGX 5,000.00` minimum reserve), teller cash desk counter operations, inter-account transfers, standing orders, administrative freeze/closure, and 14 per-account SMS transaction alert toggles.
3. **Term & Fixed Deposits Subsystem:** Placement booking (Counter Cash vs Savings Offset), 7 lifecycle states, interest accruals, premature/maturity liquidations, and certificate generation.
4. **Debit Card Services:** Co-branded Interswitch debit card issuance requests, approvals, and savings account linking.
5. **Credit Origination & Loan Servicing Subsystem:** Multi-product loan origination (`NCHIEI Credit`, `Business Loans`, `Agriculture Loans`, `Quick Credit`), mathematical amortization calculator (`flat`, `declining`, `amortize`), Credit Bureau Africa (`CBA`) vetting, collateral appraisal, guarantor pledges, automated repayment sweeps (`Auto pay from savings`), interest/penalty/admin fee waivers, loan restructuring/rescheduling (`LN00299` -> `1D04FE1229`), legal debt collection referrals, and bad debt write-offs.
6. **General Ledger & Double-Entry Accounting Engine:** 5-category hierarchical Chart of Accounts (Assets, Liabilities, Capital, Income, Expenses) with dash-delimited structural codes (e.g. `1-1-1-1-2 Teller - 1`), manual and automated Journal Vouchers (JVs) with maker-checker reversals, financial transaction approvals, budget variance monitoring, and period closing with Trial Balance snapshot PDFs and rollback protection.
7. **Fixed Assets Register:** Asset profiling, straight-line/reducing balance depreciation, revaluations, transfers, and disposals.
8. **Shares & Equity Subsystem:** Member share capital records, share transfers, and dividend declarations requiring mandatory operator password re-authentication for distribution into savings accounts or equity capitalization.
9. **Legacy Migration & Batch Data Import Engine:** Multi-entity batch ingestion engine with downloadable templates and preview validation for Individuals, Institutions, Joints, Groups, Group Members, Loans, Fixed Deposits, and Savings Accounts.
10. **Role-Based Access Control (RBAC) & Governance:** 34 modules, 382 granular permissions, daily operating time windows, 5-minute inactivity session lockdown, and maker-checker limits across 9 financial transaction categories.

---

## 2. System Overview

SavingsPlus operates as the central ledger and mission-critical operations portal for **NSIMBI SACCO LTD**. The backend integrates branch banking halls, field credit officers, agency banking agents, external mobile money aggregators, credit reference bureaus, and regulatory authorities into an integrated financial core.

```mermaid
graph TD
    UI[Browser Client / Web Portal] -->|HTTPS / JSON REST API| API[SavingsPlus Backend Core API]
    API --> AUTH[Authentication & RBAC Engine]
    API --> MEM[Member & Customer Management CIF]
    API --> SAV[Savings & Deposit Subsystem]
    API --> FD[Fixed Deposits & Standing Orders]
    API --> CRD[Debit Card Services Interswitch]
    API --> LOAN[Loan Origination & Servicing Subsystem]
    API --> CRB[Credit Bureau Integration CBA]
    API --> SHR[Shares & Dividend Management]
    API --> ACC[General Ledger & Double-Entry Accounting]
    API --> FA[Fixed Assets Register & Depreciation]
    API --> IMP[Batch Data Import Engine]
    API --> PAY[Staff Payroll & Statutory Deductions]
    API --> FLT[Float & External Channels Aggregator]
    API --> JOB[Scheduled Tasks & Async Job Engine]
    
    FLT --> MTN[MTN Mobile Money Gateway]
    FLT --> AIR[Airtel Money Gateway]
    FLT --> MSC[MSACCO Core Switch]
    FLT --> ISW[Interswitch ATM/Card Switch]
    FLT --> BNK[Commercial Banks Bridge]
    
    API --> REG[Regulatory Reporting BOU / UMRA / CRB]
    API --> DB[(Primary Relational Database)]
```

---

## 3. Source HTML Inventory

A total of **79 HTML files** were inspected in complete detail:

| # | Filename | Relative Path | Page / Function Represented | Main Purpose | Related Pages | Important Entities Represented | Important Workflows Represented |
|---|---|---|---|---|---|---|---|
| 1 | `create_member_step_one.html` | `/create_member_step_one.html` | Member Type Selector | Selects membership category | `create_individual_member.html`, `create_group.html` | Member | Onboarding: Step 1 |
| 2 | `create_individual_member.html` | `/create_individual_member.html` | Individual Registration Form | Full biodata, KYC, Next of Kin, Employment, Bank Details, and files | `list_member_applications.html`, `list_members.html` | Customer, Individual, Kin, Bank | Individual Member Onboarding |
| 3 | `create_group.html` | `/create_group.html` | Group Registration Form | Registers formal/informal groups and VSLAs | `list_groups.html`, `list_members.html` | Customer, Group | Group Onboarding |
| 4 | `create_joint.html` | `/create_joint.html` | Joint Account Registration | Registers multi-owner joint accounts | `list_joints.html`, `list_members.html` | Customer, Joint | Joint Account Onboarding |
| 5 | `create_institution.html` | `/create_institution.html` | Institution Registration | Registers corporate and institutional entities | `list_institutions.html`, `list_members.html` | Customer, Institution | Corporate Onboarding |
| 6 | `list_members.html` | `/list_members.html` | Members Master Directory | Lists individual members with phone, age, group, branch | `list_groups.html`, `list_joints.html` | Customer, Individual | Member Browsing & Search |
| 7 | `list_groups.html` | `/list_groups.html` | Groups Directory | Lists registered groups | `list_members.html`, `create_group.html` | Customer, Group | Group Directory Browsing |
| 8 | `list_joints.html` | `/list_joints.html` | Joints Directory | Lists registered joint accounts | `list_members.html`, `create_joint.html` | Customer, Joint | Joint Directory Browsing |
| 9 | `list_institutions.html` | `/list_institutions.html` | Institutions Directory | Lists corporate accounts | `list_members.html`, `create_institution.html` | Customer, Institution | Institutional Directory Browsing |
| 10 | `list_member_applications.html` | `/list_member_applications.html` | Member Vetting Queue | Pending and rejected member applications | `list_members.html`, `create_individual_member.html` | Customer, Branch | Maker-Checker Member Approval |
| 11 | `list_agency_applications.html` | `/list_agency_applications.html` | Agency Applications Queue | Pending and rejected banking agent applications | `list_member_applications.html` | Agency Application | Agent Vetting & Approval |
| 12 | `create_user.html` | `/create_user.html` | Staff User Creation | Enrolls staff, sets 9 financial limit pairs, notification subscriptions | `list_users.html`, `create_role.html` | User, Role, Branch | Staff Provisioning & Limits |
| 13 | `list_users.html` | `/list_users.html` | Users Directory | Lists staff, last login, active/inactive, SMS OTP toggle | `create_user.html`, `user_details.html` | User, Role, Branch | User Lifecycle Management |
| 14 | `user_details.html` | `/user_details.html` | Role Permissions Editor | Edits role profile ('Sp Data Migration Training') and hours | `list_roles.html`, `edit_role.html` | Role, Permissions, Hours | Role Definition (misnamed) |
| 15 | `create_role.html` | `/create_role.html` | Role Creation Form | Sets role name, operating hours, and 382 module checkboxes | `list_roles.html`, `edit_role.html` | Role, Permissions, Hours | RBAC Role Definition |
| 16 | `edit_role.html` | `/edit_role.html` | Role Editing Form | Modifies permissions and hours for existing role | `list_roles.html`, `create_role.html` | Role, Permissions, Hours | RBAC Role Updating |
| 17 | `list_roles.html` | `/list_roles.html` | Roles Directory | Lists role profiles, user counts, operating hours | `create_role.html`, `edit_role.html` | Role, User Count | Role Policy Administration |
| 18 | `list_savings.html` | `/list_savings.html` | Savings Accounts Directory | Lists savings accounts (`FlexSave`), balances, actions | `saving_account_detail.html`, `edit_saving_account.html` | SavingsAccount, Product | Savings Portfolio Directory |
| 19 | `saving_account_detail.html` | `/saving_account_detail.html` | Savings Account Details & Desk | Account overview, Available vs Actual balance, Deposit, Withdraw, Freeze | `list_savings.html`, `edit_saving_account.html` | SavingsAccount, Transaction | Cash Desk Operations & Ledger |
| 20 | `edit_saving_account.html` | `/edit_saving_account.html` | Update Savings Account & Alerts | Updates account settings, SMS transaction alert rules | `saving_account_detail.html`, `list_savings.html` | SavingsAccount, SMSConfig | Account Profile & Alert Tuning |
| 21 | `list_savings_applications.html` | `/list_savings_applications.html` | Savings Applications Queue | Pending/Approved/Rejected savings account openings | `list_savings.html` | SavingsApplication | Savings Account Maker-Checker |
| 22 | `list_savings_account_applications.html` | `/list_savings_account_applications.html` | Savings Account Vetting Queue | Alternative route for savings applications vetting | `list_savings_applications.html` | SavingsApplication | Account Approval Workflow |
| 23 | `list_fixed_deposits.html` | `/list_fixed_deposits.html` | Fixed Deposits Directory | Term deposits with 7 lifecycle status filters and terminate trigger | `create_fixed_deposit.html`, `fixed_deposit_detail.html` | FixedDeposit, Customer | Term Deposit Portfolio Management |
| 24 | `create_fixed_deposit.html` | `/create_fixed_deposit.html` | Fixed Deposit Placement | Books term deposit, interest rate, interval, till/offset funding | `list_fixed_deposits.html`, `fixed_deposit_detail.html` | FixedDeposit, SavingsAccount | Term Deposit Booking |
| 25 | `fixed_deposit_detail.html` | `/fixed_deposit_detail.html` | Fixed Deposit Certificate | Certificate terms, payout on maturity, print certificate | `list_fixed_deposits.html` | FixedDeposit | Term Deposit Certificate & Payout |
| 26 | `list_fixed_deposit_applications.html` | `/list_fixed_deposit_applications.html` | Fixed Deposit Approval Queue | Pending/Approved/Rejected term deposit placements | `list_fixed_deposits.html` | FixedDepositApplication | Term Deposit Approval Queue |
| 27 | `list_standing_orders.html` | `/list_standing_orders.html` | Standing Orders Directory | Recurring transfer orders (Pending, Approved, Running, Expired) | `list_savings.html` | StandingOrder, SavingsAccount | Recurring Payment Automation |
| 28 | `list_debit_card_issuance.html` | `/list_debit_card_issuance.html` | Debit Card Issuance Queue | Co-branded Interswitch card requests, approvals, issuance | `saving_account_detail.html` | DebitCard, SavingsAccount | Card Services Management |
| 29 | `list_loans.html` | `/list_loans.html` | Loans Portfolio Directory | Active loan contracts: Outstanding, Due, Performing, Legal | `loan_detail.html`, `list_loan_applications.html` | Loan, Product, Customer | Loan Portfolio Tracking |
| 30 | `list_loan_applications.html` | `/list_loan_applications.html` | Loan Applications Pipeline | Underwriting queue: Approved, Pending, Rejected, Cancelled | `loan_calculator.html`, `loan_detail.html` | LoanApplication, Product | Loan Origination Pipeline |
| 31 | `loan_detail.html` | `/loan_detail.html` | Loan Account Details & Servicing | Loan schedule, auto-pay, repayments, waivers, reschedule, write-off | `list_loans.html` | Loan, RepaymentSchedule | Loan Servicing & Default Management |
| 32 | `loan_calculator.html` | `/loan_calculator.html` | Loan Amortization Calculator | Interactive calculator: flat, declining, amortize; schedule preview | `list_loan_applications.html` | LoanProduct, Schedule | Credit Appraisal & Modeling |
| 33 | `list_credit_reporting.html` | `/list_credit_reporting.html` | Credit Bureau (CRB) Queue | Credit Reference Bureau (CBA) inquiries and reporting | `list_loans.html`, `list_loan_applications.html` | CreditReport, LoanApplication | Credit Bureau Vetting (CBA) |
| 34 | `acccounting_home.html` | `/acccounting_home.html` | Accounting Portal Home | Navigation hub for Chart of Accounts, Journals, Budgets, Assets | `create_journal.html`, `list_charts_of_accounts_assets.html` | GeneralLedger | Accounting Navigation |
| 35 | `list_charts_of_accounts_assets.html` | `/list_charts_of_accounts_assets.html` | COA Assets Tier | Displays hierarchical Asset accounts with `+ Account` and `Deactivate` | `create_chart_of_account_asset.html` | ChartOfAccount | Chart of Accounts: Assets Tier |
| 36 | `list_charts_of_accounts_liabilities.html` | `/list_charts_of_accounts_liabilities.html` | COA Liabilities Tier | Displays hierarchical Liability accounts (Deposits, Creditors) | `list_charts_of_accounts_assets.html` | ChartOfAccount | Chart of Accounts: Liabilities Tier |
| 37 | `list_charts_of_accounts_capital.html` | `/list_charts_of_accounts_capital.html` | COA Capital Tier | Displays Capital accounts (Member Shares, Institutional Capital) | `list_charts_of_accounts_assets.html` | ChartOfAccount | Chart of Accounts: Capital Tier |
| 38 | `list_charts_of_accounts_equity.html` | `/list_charts_of_accounts_equity.html` | COA Income / Equity Tier | Displays Income and Revenue accounts (`+ income`) | `list_charts_of_accounts_assets.html` | ChartOfAccount | Chart of Accounts: Income Tier |
| 39 | `list_charts_of_accounts_expenses.html` | `/list_charts_of_accounts_expenses.html` | COA Expenses Tier | Displays Operational and Administrative Expense accounts | `list_charts_of_accounts_assets.html` | ChartOfAccount | Chart of Accounts: Expenses Tier |
| 40 | `create_chart_of_account_asset.html` | `/create_chart_of_account_asset.html` | Chart of Account Node Creation | Creates new ledger accounts under a designated parent hierarchy | `list_charts_of_accounts_assets.html` | ChartOfAccount | Ledger Node Definition |
| 41 | `create_journal.html` | `/create_journal.html` | Manual Journal Voucher Entry | Posts balanced double-entry JVs across branches and COA accounts | `journals.html.html`, `approvals.html` | JournalVoucher, JournalItem | Double-Entry JV Posting |
| 42 | `journals.html.html` | `/journals.html.html` | Journal Vouchers Register | Lists historical JVs with `View more` and `Reverse` actions | `create_journal.html` | JournalVoucher | Ledger Audit & Reversals |
| 43 | `approvals.html` | `/approvals.html` | Financial Approvals Queue | Maker-checker queue for JVs, bank transfers, adjustments | `create_journal.html` | FinancialApproval | Transaction Maker-Checker |
| 44 | `budgets.html` | `/budgets.html` | Budget Management | Annual budget allocations and application tracking | `approvals.html` | Budget, BudgetApplication | Budget Control |
| 45 | `closing_and_restore_books.html` | `/closing_and_restore_books.html` | Period Closing & Book Rollback | Closes accounting periods, generates TB snapshot PDF, rollbacks | `acccounting_home.html` | FinancialPeriod | Financial Period Governance |
| 46 | `list_fixed_assets.html` | `/list_fixed_assets.html` | Fixed Assets Register | Lists physical assets, depreciation schedules, and disposals | `list_asset_profiles.html` | FixedAsset | Asset Management |
| 47 | `list_asset_profiles.html` | `/list_asset_profiles.html` | Asset Depreciation Profiles | Configures asset classes and depreciation methods | `list_fixed_assets.html` | AssetProfile | Depreciation Policy Definition |
| 48 | `imports.html` | `/imports.html` | Batch Data Import Intake | Uploads migration/batch CSV files for 9 system entities | `preview_import.html` | DataImportBatch | Batch Migration & Intake |
| 49 | `preview_import.html` | `/preview_import.html` | Import Template Preview | Displays schema validation and column preview for batch imports | `imports.html` | DataImportBatch | Import Schema Validation |
| 50 | `list_shares.html` | `/list_shares.html` | Member Shares Register | Lists member shareholdings and cumulative share capital | `share_dividends.html` | ShareAccount | Share Equity Administration |
| 51 | `share_dividends.html` | `/share_dividends.html` | Dividend Declaration & Payout | Declares dividends with operator password authorization | `list_shares.html` | DividendDeclaration | Dividend Distribution |


### 3.4. Payroll Subsystem Source HTML Files (13 Files)

| # | Filename | Relative Path | Page / Function Represented | Main Purpose | Related Pages | Primary Entity | Subsystem / Module |
|---|---|---|---|---|---|---|---|
| 52 | `create_staff.html` | `/create_staff.html` | Staff Registration Form | Captures staff bio-data, next of kin, salary, tax IDs (TIN, NSSF), branch, role, document uploads | `staff_list.html` | StaffProfile, Document | Staff Management |
| 53 | `staff_list.html` | `/staff_list.html` | Staff Directory & Filter | Lists active/inactive staff, search, filters, navigation to detail and edit | `create_staff.html` | StaffProfile | Staff Management |
| 54 | `payroll_staff_detail.html` | `/payroll_staff_detail.html` | Staff Profile & Deductions | Views staff bio, salary, allowance assignments, other deductions, document uploads | `staff_list.html` | StaffProfile, Allowance, Deduction | Staff Management |
| 55 | `edit_payroll_staff.html` | `/edit_payroll_staff.html` | Staff Edit Form | Updates staff bio-data, salary, tax toggles (PAYE, NSSF, LST), payment method | `payroll_staff_detail.html` | StaffProfile | Staff Management |
| 56 | `payroll_list.html` | `/payroll_list.html` | Monthly Payroll Summary | Displays gross pay, allowances, PAYE, NSSF, salary advance deductions, and net pay | `pay_salaries.html` | PayrollRun, LineItem | Payroll Processing |
| 57 | `pay_salaries.html` | `/pay_salaries.html` | Salary Payment Authorization | Maker-checker salary payment authorization, date selection, cash/bank GL account selection | `payroll_list.html` | SalaryDisbursementBatch | Salary Disbursement |
| 58 | `request_salary_payment.html` | `/request_salary_payment.html` | Request Salary Disbursement | Initiates salary payment batch request for checker approval | `pay_salaries.html` | SalaryDisbursementBatch | Salary Disbursement |
| 59 | `salaries_disbursed.html` | `/salaries_disbursed.html` | Disbursed Salaries Register | Historical register of approved and disbursed staff salaries with date filters | `pay_salaries.html` | SalaryDisbursementBatch | Salary Disbursement |
| 60 | `salary_advances.html` | `/salary_advances.html` | Salary Advances Register | Lists staff advances across Active, Cleared, Pending, Rejected, Reversed states | `request_salary_advance.html` | SalaryAdvance | Salary Advances |
| 61 | `request_salary_advance.html` | `/request_salary_advance.html` | Request Salary Advance | Form to apply for salary advance: staff selector, amount, monthly deduction installment | `salary_advances.html` | SalaryAdvance | Salary Advances |
| 62 | `payroll_allowances.html` | `/payroll_allowances.html` | Allowance Configuration | Manages recurring and one-off allowance categories (Housing, Transport, Medical) | `payroll_staff_detail.html` | AllowanceType | Payroll Settings |
| 63 | `payroll_deductions.html` | `/payroll_deductions.html` | Statutory Deductions Setup | Configures PAYE tax brackets, NSSF rates (5% employee / 10% employer), and LST | `payroll_staff_detail.html` | DeductionType | Payroll Settings |
| 64 | `payroll_other_deductions.html` | `/payroll_other_deductions.html` | Voluntary Deductions Setup | Configures custom staff deductions: staff SACCO shares, welfare funds, loans | `payroll_staff_detail.html` | DeductionType | Payroll Settings |

### 3.5. Product Configuration & Institutional Settings Source HTML Files (15 Files)

| # | Filename | Relative Path | Page / Function Represented | Main Purpose | Related Pages | Primary Entity | Subsystem / Module |
|---|---|---|---|---|---|---|---|
| 65 | `settings_page.html` | `/settings_page.html` | Master Settings Directory | Central administration portal navigating to Institution, Loans, Savings, Payroll, Jobs | `index.html` | SystemSetting | System Administration |
| 66 | `institution_settings.html` | `/institution_settings.html` | Institution Profile View | Displays institution corporate profile, logo, branding, address, registration details | `settings_page.html` | InstitutionProfile | Institutional Setup |
| 67 | `edit_institution_settings.html` | `/edit_institution_settings.html` | Edit Institution Profile | Configures SACCO name, short alias, email, phone, physical address, theme color, logo | `institution_settings.html` | InstitutionProfile | Institutional Setup |
| 68 | `pmt_settings.html` | `/pmt_settings.html` | Proxy Means Test / SPM Setup | Configures Social Performance Management, GNI per capita, poverty thresholds, MIX benchmarks | `settings_page.html` | PMTConfiguration | Social Performance |
| 69 | `list_loan_products.html` | `/list_loan_products.html` | Loan Products Directory | Lists configured credit products, interest rates, status, actions to edit or add deductions | `create_product.html` | LoanProduct | Product Configuration |
| 70 | `create_product.html` | `/create_product.html` | Loan Product Master Setup | Comprehensive credit product authoring: interest method, penalty rate, grace days, 30+ GL accounts | `list_loan_products.html` | LoanProduct, Charge | Product Configuration |
| 71 | `loan_settings.html` | `/loan_settings.html` | Global Loan Risk Policies | Configures amortization frequency, arrears penalizing, due reminders, auto-pay sweep rules | `settings_page.html` | LoanGlobalPolicy | Credit Administration |
| 72 | `loan_loss_provisions.html` | `/loan_loss_provisions.html` | Regulatory Loan Loss Setup | Configures BOU / UMRA IFRS9 loan loss provisioning percentages by arrears aging bracket | `settings_page.html` | LoanLossProvision | Regulatory Compliance |
| 73 | `loan_holidays.html` | `/loan_holidays.html` | Loan Repayment Holidays | Defines moratorium dates and public holidays preventing installment defaults | `settings_page.html` | LoanHoliday | Credit Administration |
| 74 | `crb_settings.html` | `/crb_settings.html` | Credit Bureau Gateway Setup | Configures CRB credentials: Identification code, Enquiries user/pwd, SFTP credentials | `settings_page.html` | CRBConfiguration | Credit Bureau Integration |
| 75 | `list_savings_products.html` | `/list_savings_products.html` | Savings Products Directory | Lists savings products, interest rules, minimum balances, withdrawal limits | `create_savings_product.html` | SavingsProduct | Product Configuration |
| 76 | `create_savings_product.html` | `/create_savings_product.html` | Savings Product Authoring | Defines opening/min balance, dormancy, ledger fees, ATM/MSACCO clearing GLs, velocity limits | `list_savings_products.html` | SavingsProduct, Charge | Product Configuration |
| 77 | `saving_settings.html` | `/saving_settings.html` | Global Savings Policies | Configures counter SMS, overdrafts, ledger fee schedule, auto-share deductions, till limits | `settings_page.html` | SavingsGlobalPolicy | Deposit Administration |
| 78 | `savings_withdraw_charges.html` | `/savings_withdraw_charges.html` | Tiered Withdrawal Charges | Defines bracketed cash withdrawal charge matrix (Min Amount, Max Amount, Fee) | `create_savings_product.html` | WithdrawalChargeTier | Tariff Management |
| 79 | `fixed_deposit_settings.html` | `/fixed_deposit_settings.html` | Fixed Deposit Tier Setup | Defines term deposit duration bands, interest rates, accrual/award frequencies, rollover rules | `settings_page.html` | FixedDepositProduct | Product Configuration |

---
## 4. Domain Analysis

### Core Purpose of the System
SavingsPlus is an enterprise Core Banking System (CBS) and financial ERP built for Savings and Credit Cooperative Organizations (SACCOs) in Uganda under the regulatory framework of the **Bank of Uganda (BOU)** and the **Uganda Microfinance Regulatory Authority (UMRA)**.

### Subsystem Operations
1. **General Ledger & Double-Entry Accounting:**
   - **5-Tier Chart of Accounts Hierarchy:**
     - **1. Assets:** `1-1-1 CASH AND CASH EQUIVALENTS`, `1-1-1-1 CASH AT HAND` (`1-1-1-1-1 Cash In Safe`, `1-1-1-1-2 Teller - 1`, `1-1-1-1-3 Teller - 2`, `1-1-1-1-4 Petty Cash`), `1-1-1-2 CASH AT BANK`, Loans Portfolio, Fixed Assets.
     - **2. Liabilities:** Customer Savings Deposits (`FlexSave`), Fixed Term Deposits, External Borrowings.
     - **3. Capital:** Member Share Capital, Statutory Reserves, Retained Earnings.
     - **4. Income:** Loan Interest Income, Loan Fees & Fines, Account Opening Fees, Ledger Maintenance Fees, Mobile Money Commission Float.
     - **5. Expenses:** Personnel & Payroll, Staff Allowances, Rent, Depreciation, Software Licensing, Interest Expense on Fixed Deposits.
   - **Dash-Separated Hierarchical Codes:** Strict parent-child nesting where each node's code defines its lineage (`1` -> `1-1` -> `1-1-1` -> `1-1-1-1` -> `1-1-1-1-2`).
   - **Teller Drawer GL Binding:** User workstations (e.g. `1-1-1-1-2 Teller - 1`) are literal asset accounts in the Chart of Accounts under Cash at Hand.
   - **Journal Vouchers (JVs):** Balanced double-entry vouchers supporting Method (`cash`, `cheque`), Entry Type (`credit`, `debit`), Contact Person, Reference Number, Registration Date, and Branch mapping.
   - **Period Closing & Snapshotting:** Freezes GL accounts on year-end/month-end (e.g. `Closed on Dec 31, 2024`), automatically generates an immutable Trial Balance snapshot PDF (`Download TB(pdf)`), and supports administrative `Rollback` with tracking.
2. **Shares & Dividend Management:**
   - **Share Capital:** Core member equity required for SACCO membership.
   - **Dividend Declarations:**
     - Payout Modes: Percentage-based or fixed pool (`Amount*`, `Use percentages*`).
     - Share Period: Historical qualification window in months (`Share period* month(s)`).
     - Dual Distribution Destinations (`Method*`):
       1. `savings`: Dividends credited into active member savings passbook (`Saving product`).
       2. `shares`: Dividends capitalized into member share equity.
     - Critical Security Guard: Requires operator password re-authentication (`Password*`) to commit financial dividend payouts.
3. **Batch Migration & Data Import Engine:**
   - Ingestion and schema validation for 9 distinct system entities: `Individuals`, `Institutions`, `Joint Accounts`, `Groups`, `Group Members`, `Loans`, `Fixed Deposits`, `Savings`, and `savingsAccounts`.
4. **Savings Operations:**
   - Account numbering: Member Number (`00350000528`) + Sequence (`1`) = Account (`003500005281`).
   - Available vs Actual Balance (`actual_balance - 5000 UGX reserve`).
   - 14 per-account SMS transaction alert toggles.
5. **Credit Operations:**
   - 4 loan products, 3 amortization methods (`flat`, `declining`, `amortize`), 4 frequencies (`daily`, `weekly`, `monthly`, `annually`), CRB (CBA) checks, auto-pay from savings, debt restructuring (`LN00299` -> `1D04FE1229`), and bad debt write-offs.


### 4.7. Human Capital Management (HCM) & Payroll Subsystem
- **Staff Onboarding & Master Data:** Comprehensive bio-data capturing (NIN, passport photo, signature, physical address), next-of-kin emergency contacts, branch assignment, job designation, and employment contracts.
- **Remuneration & Compensation Modeling:** Basic salary definition, linked internal SACCO savings account for direct payroll credit, and multi-tier payment methods (Internal Savings, Bank Wire, Mobile Money).
- **Statutory Taxation & Social Security Compliance:** Mandatory statutory deduction toggles and engines compliant with Uganda Revenue Authority (URA) Pay-As-You-Earn (PAYE) progressive tax brackets, National Social Security Fund (NSSF) statutory contributions (5% employee deduction + 10% employer contribution = 15% total), and Local Service Tax (LST).
- **Allowance & Voluntary Deduction Matrix:** Parameterized recurring or one-off allowances (Housing, Transport, Medical) and voluntary deductions (Staff SACCO Share purchases, Staff Welfare Funds, Staff Loan recoveries).
- **Gross-to-Net Payroll Calculation Engine:** Automated monthly payroll run compiling Basic Salary + Total Allowances = Gross Pay; Gross Pay - Statutory Taxes (PAYE, NSSF, LST) - Voluntary Deductions - Salary Advance Installments = Net Pay.
- **Dual-Authorization Salary Disbursement Workflow:** Two-step maker-checker authorization protocol (`request_salary_payment.html` -> `pay_salaries.html`) where HR initiates a salary batch, and Executive Management authorizes the disbursement, triggering automated direct balance sweeps from SACCO liquidity accounts into staff savings accounts.
- **Staff Salary Advance Credit Management:** Employee short-term liquidity facility with formal intake, appraisal, approval, disbursement, and automated monthly payroll recovery sweeps with multi-state tracking (`Pending` -> `Active` -> `Cleared` / `Rejected` / `Reversed`).

### 4.8. Enterprise Product Engine, Risk Governance & Multi-Branch Institutional Settings
- **Loan Product Factory:** Parameterized credit product blueprints establishing minimum/maximum principal bounds, interest computation methodologies (`Flat`, `Declining Balance`, `Amortize`), repayment frequencies (`Daily`, `Weekly`, `Monthly`, `Annually`), arrears grace days, penalty rates before and after maturity, compulsory guarantor/collateral thresholds, and mandatory borrower savings/share pledge percentages.
- **Granular General Ledger Product Mapping:** 30+ distinct GL account mapping hooks per loan product covering Principal Asset, Interest Income, Penalty Income (Auto/Manual), Receivables, Suspended Interest, Rescheduled Loss, Bad Debt Write-Offs, Write-Off Recoveries, and Upfront Administration Fees.
- **Regulatory Portfolio Classification & IFRS 9 Loan Loss Provisioning:** Bank of Uganda (BOU) and UMRA compliant provisioning rules categorizing loans by Days Past Due (DPD) into risk classifications (`Normal / Pass` [1%], `Watch / Special Mention` [5%], `Substandard` [20%], `Doubtful` [50%], `Loss` [100%]) with automated impairment expense posting.
- **Institutional Loan Holidays & Repayment Moratoria:** System-wide or product-specific holiday calendars preventing loans from falling into technical default or accruing undue penalties during public holidays or approved institutional moratoria.
- **Credit Reference Bureau (CRB) Gateway:** Dual-channel integration credentials for Credit Bureau Africa (CBA) or Compuscan supporting automated borrower inquiry searches and nightly SFTP batch portfolio synchronization.
- **Savings Product Engine & Omni-Channel Transaction Rules:** Flexible deposit account blueprints supporting individual and group passbook accounts, minimum operating reserves, dormancy thresholds, tiered withdrawal charges, ledger fee frequency schedules, and automated share capital deductions.
- **Omni-Channel Clearing & Settlement Ledger:** Automated GL accounting bindings across external financial channels: MTN Mobile Money, Airtel Money, ABC Bank clearing, and Interswitch / Equity Bank ATM switch networks, with dedicated excise duty tax accounting.
- **Fixed Term Deposit Policy Matrix:** Multi-tier duration profiles (e.g. 3, 6, 12, 24 months) configuring annual interest rates, accrual frequencies, payout schedules, and automated rollover preferences.
- **Social Performance Management (SPM) / Proxy Means Test (PMT):** Socio-economic progress tracking configuring national GNI per capita benchmarks, poverty line probability thresholds, and MIX Market operational indicators.

---
## 5. Actors and Roles

| Role / Designation | Domain Responsibilities | Financial & Operational Bound |
|---|---|---|
| **System Administrator** | Platform configuration, licensing, job recovery, backup restoration | System-wide configuration |
| **Credit Manager** | Underwriting approval, restructuring, write-offs, legal referral | Highest credit approval limits |
| **Head of Finance / Accountant** | General ledger, journal vouchers, Chart of Accounts, period closing, dividend execution | High JV, Transfer, and Period Close limits |
| **Accounts Assistant** | Journal entry posting, till balancing, bank reconciliations | Low/Mid JV posting limits |
| **Branch Manager** | Checker approval for member onboarding, savings accounts, fixed deposits, JVs | Mid/High checker limits |
| **Cashier / Teller (Station 1, Station 2)** | Cash desk deposits, cash withdrawals, till sheet reconciliation | Bounded cash drawer limits (`1-1-1-1-2`) |
| **Internal & External Auditor** | Risk compliance, TB imbalance tracking, audit trail review | Read-only inspection rights |
| **Customer Care Lead** | Member registration, KYC reviews, debit card issuance requests | Zero financial limits |


- **HR & Payroll Officer (`ROLE_PAYROLL_OFFICER`):** Enrolls staff, updates employment profiles, configures allowances and voluntary deductions, inputs salary advance applications, and compiles the preliminary monthly payroll run.
- **Finance Director / Chief Executive Officer (`ROLE_FINANCE_DIRECTOR`, `ROLE_CEO`):** Authorizes salary disbursement batches, approves executive salary advances, establishes institutional branding and corporate parameters, and oversees loan loss provisioning schedules.
- **Credit Risk & Product Administrator (`ROLE_CREDIT_ADMIN`):** Configures loan and savings product master blueprints, defines interest calculation algorithms, establishes collateral and guarantor underwriting rules, and configures regulatory CRB gateway credentials.
- **System / Enterprise Administrator (`ROLE_SUPER_ADMIN`):** Manages multi-branch definitions, system background job schedulers, database backup policies, and system-wide operational flags.

---
## 6. Core Entities

The 51 HTML sources establish the following complete persistent backend entities:

### 1. Member / Customer (`customers`)
- **Key Fields:** `id`, `member_number` (11 digits, e.g. `00350000528`), `customer_type` (`INDIVIDUAL`, `GROUP`, `JOINT`, `INSTITUTION`), `branch_id`, `savings_officer_id`, `entry_date`, `phone_primary`, `phone_secondary`, `email`, `country`, `town`, `physical_address`, `postal_code`, `postal_address`, `has_loan_limit`, `status` (`PENDING`, `ACTIVE`, `REJECTED`, `INACTIVE`, `DORMANT`).

### 2. Individual Profile (`customer_individuals`)
- **Key Fields:** `customer_id`, `salutation`, `first_name`, `middle_name`, `last_name`, `gender`, `marital_status`, `is_dependent`, `is_pwd`, `date_of_birth`, `country_of_birth`, `nationality`, `home_ownership`, `identification_type`, `identification_number`, `card_number`, `signature_file_path`, `identification_file_path`, `photo_file_path`.

### 3. Next of Kin (`customer_next_of_kin`)
- **Key Fields:** `id`, `customer_id`, `first_name`, `second_name`, `phone`, `physical_address`, `relationship`.

### 4. Employment & Income (`customer_employment_income`)
- **Key Fields:** `customer_id`, `occupation`, `employer_name`, `employer_address`, `income_frequency`, `income_per_month`.

### 5. External Bank Details (`customer_external_banks`)
- **Key Fields:** `customer_id`, `primary_bank_name`, `primary_bank_account`, `secondary_bank_name`, `secondary_bank_account`.

### 6. Group Profile (`customer_groups`)
- **Key Fields:** `customer_id`, `group_name`, `group_type` (`Members`, `Non Members`), `registration_number`.

### 7. Joint Profile (`customer_joints`)
- **Key Fields:** `customer_id`, `joint_name`.

### 8. Institution Profile (`customer_institutions`)
- **Key Fields:** `customer_id`, `institution_name`, `registration_number`, `registration_date`, `tin_number`, `business_type`, `institution_category`, `residence_ownership`.

### 9. System User (`users`)
- **Key Fields:** `id`, `branch_id`, `role_id`, `station_id` (e.g. `1-1-1-1-2 Teller - 1`), `username`, `email`, `password_hash`, `first_name`, `last_name`, `phone`, `is_active`, `is_blocked`, `sms_otp_enabled`, `last_login_at`, plus all 9 Min/Max Limit pairs.

### 10. User Notification Subscriptions (`user_notification_subscriptions`)
- **Key Fields:** `user_id`, `event_key` (22 explicit triggers).

### 11. Role Profile (`roles` & `role_permissions`)
- **Key Fields:** `id`, `role_name`, `open_time`, `close_time`, `is_active`, 382 granular permissions across 34 modules.

### 12. Branch (`branches`)
- **Key Fields:** `id`, `branch_code`, `name`, `is_active`.

### 13. Savings Account (`savings_accounts`)
- **Key Fields:** `id`, `account_number` (12 digits, e.g. `003500005281`), `customer_id`, `product_id` (`FlexSave`), `branch_id`, `status` (`PENDING_APPROVAL`, `ACTIVE`, `INACTIVE`, `FROZEN`, `CLOSED`), `actual_balance`, `available_balance`, `opening_fee_charged`, `last_ledger_fee_charged_at`, `last_transaction_at`, `opened_at`.

### 14. Savings SMS Alert Rules (`savings_sms_alert_configs`)
- **Key Fields:** `account_id`, 14 boolean notification flags.

### 15. Fixed Deposit (`fixed_deposits`)
- **Key Fields:** `id`, `deposit_number`, `customer_id`, `funding_source` (`TILL_CASH`, `SAVINGS_OFFSET`), `funding_savings_account_id`, `payout_savings_account_id`, `depositor_name`, `depositor_phone`, `principal_amount`, `starts_on`, `period_months`, `interest_rate_annual`, `interest_interval` (`monthly`, `yearly`, `Not applicable`), `payout_option` (`Principal`, `Principal & Interest`), `status` (7 states), `matures_at`, `accrued_interest`.

### 16. Standing Order (`standing_orders`)
- **Key Fields:** `id`, `source_account_id`, `destination_account_id`, `amount`, `frequency` (`daily`, `weekly`, `monthly`), `start_date`, `end_date`, `status`.

### 17. Debit Card Issuance (`debit_card_issuances`)
- **Key Fields:** `id`, `savings_account_id`, `card_pan_masked`, `status`, `requested_by`, `issued_at`.

### 18. Loan Facility (`loans`)
- **Key Fields:** `id`, `loan_number` (e.g. `1D04FE1229`), `customer_id`, `loan_officer_id`, `product_id` (`NCHIEI Credit`, etc.), `repayment_savings_account_id` (`003500000651`), `principal_disbursed`, `interest_rate_annual` (e.g. `42.0%`), `interest_method` (`flat`, `declining`, `amortize`), `repayment_frequency`, `installments_count`, `grace_period_months`, `installment_amount`, `auto_pay_from_savings`, `disbursed_at`, `maturity_date`, `status` (8 states), `rescheduled_from_loan_number` (`LN00299`), `waived_interest`, `waived_penalty`, `waived_admin_fees`.

### 19. Loan Application (`loan_applications`)
- **Key Fields:** `id`, `customer_id`, `product_id`, `applied_amount`, `status`, `crb_consent_obtained`, `crb_score`.

### 20. Loan Repayment Schedule (`loan_repayment_schedules`)
- **Key Fields:** `id`, `loan_id`, `installment_number`, `due_date`, `principal_due`, `interest_due`, `total_installment`, `is_settled`.

### 21. Credit Reporting Inquiry (`credit_reporting_inquiries`)
- **Key Fields:** `id`, `customer_id`, `bureau` (`CBA`), `inquiry_type`, `response_payload`.

### 22. Chart of Account (`chart_of_accounts`)
- **Key Fields:** `id`, `account_code` (e.g. `1-1-1-1-2`), `name` (e.g. `Teller - 1`), `account_category` (`ASSET`, `LIABILITY`, `CAPITAL`, `INCOME`, `EXPENSE`), `parent_account_id`, `subtype`, `tags`, `branch_id`, `is_active`.

### 23. Journal Voucher (`journal_vouchers`) & Items (`journal_voucher_items`)
- **Key Fields:** `id`, `voucher_number`, `registration_date`, `reference_number`, `contact_person`, `payment_method` (`cash`, `cheque`), `comment`, `status` (`POSTED`, `REVERSED`), `created_by`, `reversed_by`, `reversed_at`, `reversal_reason`.
- **Items Key Fields:** `id`, `voucher_id`, `account_id`, `branch_id`, `entry_type` (`debit`, `credit`), `amount`.

### 24. Financial Period (`financial_periods`)
- **Key Fields:** `id`, `period_end_date`, `closed_at`, `closed_by`, `is_rolled_back`, `rolled_back_at`, `rolled_back_by`, `tb_snapshot_pdf_url`.

### 25. Share Account (`share_accounts`) & Transactions (`share_transactions`)
- **Key Fields:** `id`, `customer_id`, `total_shares`, `share_unit_price`, `total_value`, `status`.
- **Transaction Fields:** `id`, `share_account_id`, `transaction_type` (`PURCHASE`, `TRANSFER`, `LIQUIDATION`, `DIVIDEND`), `shares_count`, `amount`, `reference_number`.

### 26. Dividend Declaration (`dividend_declarations`)
- **Key Fields:** `id`, `total_amount`, `use_percentages`, `declaration_date`, `share_period_months`, `payout_method` (`savings`, `shares`), `destination_savings_product_id`, `reference_number`, `comment`, `authorized_by`, `status`.

### 27. Fixed Asset (`fixed_assets`) & Profile (`asset_profiles`)
- **Key Fields:** `id`, `asset_code`, `name`, `profile_id`, `purchase_date`, `purchase_cost`, `salvage_value`, `useful_life_years`, `depreciation_method`, `accumulated_depreciation`, `net_book_value`, `branch_id`, `status`.

### 28. Data Import Batch (`data_import_batches`)
- **Key Fields:** `id`, `template_type` (9 types), `reference_number`, `branch_id`, `file_path`, `total_records`, `successful_records`, `failed_records`, `status`, `created_by`.

---

## 7. Entity Relationships

```mermaid
erDiagram
    CHART_OF_ACCOUNT ||--o{ JOURNAL_VOUCHER_ITEM : classified_in
    JOURNAL_VOUCHER ||--|{ JOURNAL_VOUCHER_ITEM : contains
    
    BRANCH ||--o{ JOURNAL_VOUCHER_ITEM : allocates
    BRANCH ||--o{ CUSTOMER : hosts
    BRANCH ||--o{ USER : assigns
    
    CUSTOMER ||--o{ SAVINGS_ACCOUNT : owns
    CUSTOMER ||--o{ SHARE_ACCOUNT : holds
    CUSTOMER ||--o{ LOAN : borrows
    
    SAVINGS_ACCOUNT ||--o{ SAVINGS_TRANSACTION : records
    SAVINGS_ACCOUNT ||--o{ FIXED_DEPOSIT : funds_or_pays
    SAVINGS_ACCOUNT ||--o{ STANDING_ORDER : source_of
    SAVINGS_ACCOUNT ||--o{ DEBIT_CARD_ISSUANCE : linked_to
    
    SHARE_ACCOUNT ||--o{ SHARE_TRANSACTION : records
    DIVIDEND_DECLARATION ||--o{ SHARE_TRANSACTION : distributes
    
    USER ||--o{ JOURNAL_VOUCHER : creates
    FINANCIAL_PERIOD ||--o{ JOURNAL_VOUCHER : locks
```

---

## 8. Business Workflows

### Workflow 1: Member Onboarding & Verification (Maker-Checker)
1. Staff selects entity type (`INDIVIDUAL`, `GROUP`, `JOINT`, `INSTITUTION`) on `/customers/select_type`.
2. Staff inputs comprehensive biodata, KYC, next of kin, employment, income, bank details, and uploads signature/photo/ID.
3. System persists record in `PENDING` status and dispatches event `customer_applications_pending_approval`.
4. Branch manager inspects `/customers/applications`, reviews details, and clicks `Approve` or `Reject`.
5. On approval, system transitions status to `ACTIVE`, creates savings passbook ledger, and dispatches welcome SMS.

### Workflow 2: System User Provisioning & Authorization
1. Administrator accesses `/users/create`.
2. Inputs staff names, username, email, phone, role profile, branch, and selects workstation (`1-1-1-1-2 Teller - 1`).
3. Explicitly defines 9 min/max financial limit pairs.
4. Selects event subscriptions from the 22 notification triggers.
5. System hashes password, verifies uniqueness, and saves user in `ACTIVE` state.

### Workflow 3: Role Definition & Operating Hours Enforcement
1. Administrator accesses `/users/roles/create`.
2. Configures role name, daily `Open Time` (e.g. 08:00 AM) and `Close Time` (e.g. 05:00 PM).
3. Selects permissions from the 382 granular checkboxes across 34 modules.
4. Runtime middleware validates server time against role operating hours on every API call.

### Workflow 4: Idle Session Management & Termination
1. Front-end client runs an Idle Detector tracking activity.
2. Inactivity threshold is set to 5 minutes (300 seconds).
3. If idle threshold is breached, active session is invalidated and user is redirected to login.

### Workflow 5: Background Task & Failed Job Monitoring
1. Background tasks execute asynchronously (accruals, standing order sweeps, license verifications).
2. If any task fails, error is written to `/settings/jobs/{id}/logs`.
3. Event `failed_job` fires, triggering high-priority in-app alert banner: *"A recently started job has failed Today 10:26:26 PM"*.

### Workflow 6: Double-Entry Journal Voucher Creation & Reversal
1. Accountant accesses `/accounting/journals/create`.
2. Inputs Contact person, Registration date, Reference number, Method (`cash`, `cheque`), Comment, and Entry type.
3. Selects Debit Account (e.g. `1-1-1-1-2 Teller - 1`) and Credit Account (e.g. Savings Deposit GL) with branch allocations.
4. Verifies debits == credits. On submission, voucher enters `/accounting/approvals`.
5. Checker approves voucher, posting it to General Ledger (`POSTED`).
6. If reversal is required, officer accesses `/accounting/journals`, clicks "Reverse", and enters reversal reason. System posts mirrored entries and marks voucher as `REVERSED`.

### Workflow 7: Financial Period Closing & TB Snapshotting
1. Executive accesses `/accounting/closed-periods` and clicks "Close a Period".
2. System executes automated ledger balancing check. If debits != credits, blocks closing with `tb_imbalance`.
3. If balanced, freezes GL, marks period closed (e.g. `Closed on Dec 31, 2024 by System Tasks User`), and generates immutable Trial Balance snapshot PDF (`Download TB(pdf)`).
4. If adjustments are mandated, manager clicks "Rollback", reopening the period and firing alert `opening_a_closed_period`.

### Workflow 8: Share Dividend Declaration & Payout
1. Management accesses `/shares/dividends`.
2. Enters Dividend Amount, toggles percentage vs flat pool, sets declaration date, qualification share period in months, and chooses payout method (`savings` or `shares`).
3. Operator enters administrative password into `Password*`.
4. Backend re-verifies password, computes dividend per qualifying share, and executes automated batch credit into member savings passbooks (`FlexSave`) or issues bonus shares.

### Workflow 9: Legacy System / Batch Data Import
1. Operator accesses `/settings/imports` and downloads standardized CSV template for target entity (e.g. `Loans`, `Individuals`, `Savings`).
2. Fills template, selects branch, enters reference number, and uploads file.
3. Accesses `/settings/imports/preview` to inspect mapped columns and data type validation errors.
4. Backend processes batch row-by-row, recording success/failure counts.


### 8.11. Staff Onboarding & Remuneration Structuring Workflow
```mermaid
sequenceDiagram
    autonumber
    actor HR as HR / Payroll Officer
    participant UI as Browser / Web App
    participant StaffSvc as Staff Management Service
    participant DocSvc as Document Storage Service
    participant DB as Relational Database

    HR->>UI: Fills Register New Staff Form (create_staff.html)
    HR->>UI: Uploads Contract, Photo, Signature, NIN Document
    UI->>DocSvc: Uploads digital attachments
    DocSvc-->>UI: Returns artifact URLs
    UI->>StaffSvc: POST /payroll/staff (Payload with bio, salary, tax toggles)
    StaffSvc->>StaffSvc: Validates NIN checksum, phone format, email uniqueness
    StaffSvc->>StaffSvc: Verifies linked member savings account is active
    StaffSvc->>DB: INSERT into staff_profiles, staff_next_of_kin
    DB-->>StaffSvc: Staff Profile Created (Status: ACTIVE)
    StaffSvc-->>UI: 201 Created (Staff Code: STF-00104)
    UI-->>HR: Displays success toast and redirects to staff directory
```

### 8.12. Monthly Payroll Run & Dual-Authorization Disbursement Workflow
```mermaid
sequenceDiagram
    autonumber
    actor HR as HR Officer (Maker)
    actor Approver as Finance Director / CEO (Checker)
    participant UI as Web App Portal
    participant PayrollEng as Payroll Calculation Engine
    participant Ledger as General Ledger Service
    participant SavSvc as Savings Account Service
    participant SMS as SMS Gateway Daemon

    HR->>UI: Navigates to /payroll/payroll-list
    UI->>PayrollEng: GET /payroll/payroll-list (Current month cutoff)
    PayrollEng->>PayrollEng: Calculates Gross, URA PAYE brackets, 5% NSSF, LST, Advances, Net Pay
    PayrollEng-->>UI: Renders Payroll Summary Register
    HR->>UI: Submits Salary Disbursement Request (request_salary_payment.html)
    UI->>PayrollEng: POST /payroll/salaries/salary-disbursement-requests (Status: PENDING_APPROVAL)
    
    Approver->>UI: Inspects Pending Batch (pay_salaries.html)
    Approver->>UI: Selects Funding Account (e.g. Bank/Cash GL) & Clicks Disburse
    UI->>PayrollEng: POST /payroll/salaries/salaries-disbursed
    PayrollEng->>Ledger: Verifies Funding Account Balance >= Total Gross Payroll
    PayrollEng->>Ledger: Atomic Transaction: Debit Payroll Expense, Credit Tax Liabilities
    loop For each staff with payment_method == 'savings'
        PayrollEng->>SavSvc: Credit Linked Savings Account with Net Pay
        SavSvc-->>SMS: Trigger SMS Credit Alert to Employee
    end
    PayrollEng->>PayrollEng: Mark Running Salary Advances Deducted
    PayrollEng-->>UI: 200 OK (Batch Disbursed & Archived)
    UI-->>Approver: Renders Disbursement Confirmation Voucher
```

### 8.13. Staff Salary Advance Intake & Automated Payroll Amortization
```mermaid
sequenceDiagram
    autonumber
    actor HR as HR / Payroll Officer
    actor CEO as Approving Executive
    participant UI as Web App Portal
    participant AdvSvc as Salary Advance Service
    participant SavSvc as Savings Account Service
    participant DB as Relational Database

    HR->>UI: Submits Request Salary Advance (request_salary_advance.html)
    UI->>AdvSvc: POST /payroll/advances (Staff ID, Amount, Monthly Installment)
    AdvSvc->>AdvSvc: Validates Amount <= 50% Basic Salary & Zero Running Advances
    AdvSvc->>DB: INSERT into salary_advances (Status: PENDING)
    DB-->>AdvSvc: Advance Record Stored
    CEO->>UI: Reviews Advance Queue (salary_advances.html)
    CEO->>UI: Approves Advance Request
    UI->>AdvSvc: POST /payroll/advances/{id}/approve
    AdvSvc->>SavSvc: Credit Staff Savings Account or Disburse at Cash Desk
    AdvSvc->>DB: UPDATE salary_advances SET status = 'ACTIVE'
    Note over AdvSvc,DB: Subsequent Monthly Payroll Sweeps deduct monthly installment until balance is 0.00 (Status: CLEARED)
```

---
## 9. Status and State Transitions

### Complete State Models
1. **Customer Lifecycle:** `PENDING` -> `ACTIVE` -> `INACTIVE` -> `DORMANT` (or `REJECTED`).
2. **User Lifecycle:** `ACTIVE` -> `INACTIVE` -> `BLOCKED` -> `DELETED`.
3. **Role Lifecycle:** `ACTIVE` -> `INACTIVE` -> `DELETED`.
4. **Savings Account:** `PENDING_APPROVAL` -> `ACTIVE` -> `FROZEN` -> `INACTIVE` -> `CLOSED`.
5. **Fixed Deposit (7 States):** `PENDING` -> `RUNNING` -> `MATURED_NOT_AWARDED` -> `AWARDED_ON_MATURITY` -> `TERMINATED_NO_INTEREST` -> `TERMINATED_WITH_INTEREST` -> `TERMINATED`.
6. **Standing Order:** `PENDING` -> `APPROVED` -> `RUNNING` -> `EXPIRED` (or `REJECTED`).
7. **Debit Card Issuance:** `PENDING` -> `APPROVED` -> `ISSUED` (or `REJECTED`).
8. **Loan Facility (8 States):** `PENDING` -> `APPROVED` -> `RUNNING` -> `PERFORMING` -> `DUE` -> `SENT_TO_LEGAL` -> `WRITTEN_OFF` -> `RESCHEDULED` -> `CLEARED` (or `REJECTED`, `CANCELLED`).
9. **Journal Voucher:** `PENDING` -> `POSTED` -> `REVERSED`.
10. **Financial Approvals:** `PENDING` -> `APPROVED` (or `REJECTED`).
11. **Financial Period:** `OPEN` -> `CLOSED` -> `ROLLED_BACK`.
12. **Dividend Declaration:** `DECLARED` -> `DISBURSED` (or `ROLLED_BACK`).
13. **Data Import Batch:** `UPLOADED` -> `PREVIEWED` -> `PROCESSING` -> `COMPLETED` (or `FAILED`).


### 9.6. Monthly Payroll Run State Model
```mermaid
stateDiagram-v2
    [*] --> DRAFT: Initiate Month Cutoff
    DRAFT --> CALCULATED: Run Gross-to-Net Engine
    CALCULATED --> PENDING_APPROVAL: Maker Submits Request (request_salary_payment.html)
    PENDING_APPROVAL --> REJECTED: Checker Disapproves
    REJECTED --> DRAFT: Review & Recalculate
    PENDING_APPROVAL --> APPROVED: Checker Authorizes (pay_salaries.html)
    APPROVED --> DISBURSED: Atomic Balance Sweep & GL Posting
    DISBURSED --> [*]: Archived (salaries_disbursed.html)
```

### 9.7. Staff Salary Advance State Model
```mermaid
stateDiagram-v2
    [*] --> PENDING: Submit Advance Application
    PENDING --> REJECTED: Disapproved by Executive
    PENDING --> ACTIVE: Approved & Disbursed to Staff
    ACTIVE --> ACTIVE: Monthly Payroll Installment Sweep
    ACTIVE --> CLEARED: Outstanding Balance == 0.00
    ACTIVE --> REVERSED: Administrative Reversal with Audit Remarks
    REJECTED --> [*]
    CLEARED --> [*]
    REVERSED --> [*]
```

### 9.8. Loan & Savings Product Configuration State Model
```mermaid
stateDiagram-v2
    [*] --> DRAFT: Initialize Product Master Form
    DRAFT --> ACTIVE: Save & Validate All GL Mappings
    ACTIVE --> INACTIVE: Administrative Deactivation (No new accounts)
    INACTIVE --> ACTIVE: Reactivated by Product Manager
```

---
## 10. Business Rules

### BR-001: Maker-Checker Principle on Financial & Master Records
- **Rule:** No operator may approve their own created record.
- **Evidence:** Distinct forms for creation (`/customers/individuals/create`) versus approval queues (`/customers/applications`).
- **Enforcement:** The backend must assert `applicant.created_by != approver.user_id`.

### BR-002: Temporal Role Operating Windows
- **Rule:** Users may only perform system activities within the configured `open_time` and `close_time` of their assigned Role Profile.
- **Evidence:** `create_role.html`, `edit_role.html` fields: `Open Time`, `Close Time`.
- **Enforcement:** Every API request must validate `open_time <= current_server_time <= close_time`. Requests outside this window must be rejected with HTTP 403 Forbidden (`ERR_OUTSIDE_OPERATING_HOURS`).

### BR-003: Hierarchical Transactional Limit Enforcement
- **Rule:** A user cannot approve, disburse, post, or transfer funds exceeding their configured individual min/max bounds.
- **Evidence:** `create_user.html` explicit limits for: Loan Approval, Disbursement, Deposits, Withdrawals, Shares, Fixed Assets, JVs, Default, Transfer.
- **Enforcement:** If `transaction_amount > user.max_limit` or `transaction_amount < user.min_limit`, the transaction must fail or be routed to a higher-tier checker.

### BR-004: Trial Balance Imbalance Protection
- **Rule:** Double-entry journal postings must strictly balance across debits and credits. Any out-of-balance condition must immediately halt posting and fire alert `tb_imbalance`.
- **Evidence:** Role permission `TB imbalance` and notification event `tb_imbalance`.

### BR-005: 5-Minute Inactivity Session Lockdown
- **Rule:** Operator sessions must time out after 5 minutes of continuous inactivity.
- **Evidence:** Static DOM badge: *"User is currently: Active, Time since last activity: 11 seconds, Idle duration set to: 5 Minutes"*.

### BR-006: External Float Tracking & Liquidity Alarms
- **Rule:** The system continuously monitors external liquidity balances (MTN float, Airtel float, MSACCO credit, Bank Transfer float). If balance drops below safety reserve, event `liquid_assets_balance_running_low` fires.
- **Evidence:** Top navigation bar float counters: `MSACCO credit UGX 0`, `MTN float UGX 0`, `AIRTEL float UGX 0`, `Float UGX 0`, `BANK TRANSFER float UGX 0`.


### BR-017: Ugandan Statutory Payroll Deductions (PAYE, NSSF, LST)
1. **Pay-As-You-Earn (PAYE) Tax Engine:** The system must compute progressive PAYE income tax on taxable gross salary using statutory Uganda Revenue Authority (URA) tax bands:
   - Band 1: Up to UGX 235,000 per month $	o$ 0% (Tax exempt).
   - Band 2: UGX 235,001 to UGX 335,000 $	o$ 10% on the excess above UGX 235,000.
   - Band 3: UGX 335,001 to UGX 410,000 $	o$ UGX 10,000 + 20% on the excess above UGX 335,000.
   - Band 4: Exceeding UGX 410,000 $	o$ UGX 25,000 + 30% on the excess above UGX 410,000.
   - High Earner Surcharge: For taxable income exceeding UGX 10,000,000 per month, an additional 10% surcharge applies on the amount exceeding UGX 10,000,000.
2. **National Social Security Fund (NSSF) Deductions:**
   - Employee Contribution: 5% of monthly gross salary must be deducted from the employee's pay.
   - Employer Contribution: 10% of monthly gross salary must be contributed by the SACCO.
   - Statutory Remittance: Total statutory NSSF liability posted to the General Ledger equals 15% of gross payroll ($5\% 	ext{ Employee} + 10\% 	ext{ Employer}$).
3. **Local Service Tax (LST):** The system must support scheduled deductions for Local Service Tax based on declared local government municipal schedules.

### BR-018: Staff Salary Advance Exposure Limit & Net Salary Debt Ratio
1. **Maximum Advance Exposure:** A staff member cannot be granted a salary advance exceeding **50% of their declared basic monthly salary**, unless explicitly authorized via executive override.
2. **Debt Service Ratio (DSR) Ceiling:** The monthly deduction installment for a running salary advance, combined with any running staff loans and statutory deductions, must not reduce the staff member's estimated net monthly pay below **33.3% (one-third) of their gross salary** (preserving the Ugandan statutory minimum subsistence income).
3. **Concurrent Advance Restriction:** A staff member with an existing `ACTIVE` salary advance cannot apply for or be granted a new salary advance until the outstanding balance is marked `CLEARED`.

### BR-019: Dual-Authorization Maker-Checker Protocol for Salary Payments
1. **Segregation of Duties:** The operator who initiates or compiles a salary disbursement batch (`request_salary_payment.html`) cannot be the operator who authorizes or disburses the payment (`pay_salaries.html`).
2. **Funding Account Validation:** Salary disbursement cannot execute unless the designated SACCO cash or bank funding account has sufficient liquid balance to cover the total gross salary disbursement.
3. **Atomic Internal Sweep:** When salaries are disbursed to member savings accounts, the system must execute the transfer as an atomic transactional batch: debiting the SACCO Payroll Expense / Bank GL account and simultaneously crediting each staff member's linked savings account, generating individual transaction vouchers and SMS credit notifications.

### BR-020: Loan Product Underwriting Bounds, Compulsory Savings & Guarantor Constraints
1. **Principal Range Integrity:** A loan application cannot be submitted with a principal amount outside the product's defined `Minimum Loan Amount` and `Maximum Loan Amount`.
2. **Compulsory Borrower Savings & Shares Pledges:**
   - If a product specifies `Borrower compulsory savings percent`, the applicant must hold at least that percentage of the requested loan principal in liquid savings before approval.
   - If a product specifies `Borrower compulsory shares percent`, the applicant must hold at least that percentage in paid-up share capital.
3. **Guarantor Underwriting Rules:**
   - If `Allow guarantor with arrears` is disabled, any member with an overdue loan cannot be pledged as a guarantor.
   - If `Require guarantor NIN verification` is enabled, guarantors must possess verified National Identification Numbers.
   - Guarantor compulsory savings and shares pledges must be formally locked as collateral holds, preventing withdrawal until the guaranteed loan is fully closed.

### BR-021: Bank of Uganda (BOU) / UMRA Regulatory Loan Loss Provisioning Matrix
Loans in arrears must be dynamically classified and provisioned according to Days Past Due (DPD) schedules mandated by regulatory guidelines:
1. **Normal / Performing (0 to 29 DPD):** Provision rate of **1%** against outstanding principal balance.
2. **Watch / Special Mention (30 to 89 DPD):** Provision rate of **5%** against outstanding principal balance.
3. **Substandard (90 to 179 DPD):** Provision rate of **20%** against outstanding principal balance.
4. **Doubtful (180 to 364 DPD):** Provision rate of **50%** against outstanding principal balance.
5. **Loss (365+ DPD):** Provision rate of **100%** against outstanding principal balance, with mandatory suspension of interest accruals.

### BR-022: Holiday & Weekend Loan Installment Rescheduling Protocol
1. **Calendar Moratorium:** When an installment due date coincides with an active date in `loan_holidays`, the system must automatically adjust the repayment due date to the next business day (or preceding Friday depending on policy).
2. **Weekend Rescheduling Rules:** Based on `Sunday loan Scheduling` and `Saturday Loan Scheduling` policy toggles:
   - If Saturday/Sunday scheduling is disabled, installments falling on weekends must shift forward to Monday without penalizing the borrower or counting as days in arrears.

### BR-023: Tiered Cash Withdrawal Fee & Omnichannel Limit Enforcement
1. **Bracketed Withdrawal Fee Calculation:** Cash withdrawals over the counter or via mobile channels must evaluate the tiered fee matrix in `savings_withdraw_charges.html`:
   - The fee must be assessed based on the bracket where $	ext{Min Amount} \le 	ext{Withdrawal Amount} \le 	ext{Max Amount}$.
   - If the fee is defined as a fixed charge, the exact amount is debited; if percentage-based, the surcharge is computed on the transaction amount.
2. **Omnichannel Velocity Limits:** Any withdrawal request via ATM or MSACCO mobile money must validate against the product's configured limits:
   - Transaction Amount Limit
   - Daily Withdrawal Amount Limit
   - Monthly Withdrawal Amount Limit
   - Daily & Monthly Transaction Count Limits
   - Breaching any threshold triggers immediate rejection with an `EXCEEDED_CHANNEL_VELOCITY_LIMIT` error.

### BR-024: Automated Ledger Fee Sweep & Arrears Priority Hierarchy
1. **Monthly Ledger Fee Processing:** On the configured ledger fee interval (e.g. monthly), an automated background job sweeps the configured ledger fee from all active savings accounts.
2. **Negative Balance / Minimum Balance Policy:**
   - If `Allow ledger fees deduction below minimum balance` is FALSE, the system cannot deduct the fee if doing so would breach the `UGX 5,000.00` minimum reserve.
   - If `Allow Charge Monthly Ledger Fees Arrears` is TRUE, uncollected fees are recorded as outstanding fee arrears and automatically deducted from subsequent deposits before funds become available.

### Mathematical Calculations & Formulas (Batch 4)

### 13.7. Uganda Revenue Authority (URA) Pay-As-You-Earn (PAYE) Progressive Tax Formula
$$	ext{Taxable Pay} = 	ext{Gross Salary} - 	ext{NSSF Employee Contribution (if exempt)} - 	ext{Allowable Exemptions}$$
$$	ext{Monthly PAYE} = egin{cases} 
0.00 & 	ext{if } 	ext{Taxable Pay} \le 235,000 \
0.10 	imes (	ext{Taxable Pay} - 235,000) & 	ext{if } 235,000 < 	ext{Taxable Pay} \le 335,000 \
10,000 + 0.20 	imes (	ext{Taxable Pay} - 335,000) & 	ext{if } 335,000 < 	ext{Taxable Pay} \le 410,000 \
25,000 + 0.30 	imes (	ext{Taxable Pay} - 410,000) & 	ext{if } 410,000 < 	ext{Taxable Pay} \le 10,000,000 \
25,000 + 0.30 	imes (	ext{Taxable Pay} - 410,000) + 0.10 	imes (	ext{Taxable Pay} - 10,000,000) & 	ext{if } 	ext{Taxable Pay} > 10,000,000 
\end{cases}$$

### 13.8. National Social Security Fund (NSSF) Statutory Remittance
$$	ext{Employee NSSF (5\%)} = 	ext{Gross Salary} 	imes 0.05$$
$$	ext{Employer NSSF (10\%)} = 	ext{Gross Salary} 	imes 0.10$$
$$	ext{Total Statutory NSSF Remittance (15\%)} = 	ext{Employee NSSF} + 	ext{Employer NSSF}$$

### 13.9. Net Monthly Salary Derivation
$$	ext{Gross Salary} = 	ext{Basic Salary} + \sum 	ext{Allowances}$$
$$	ext{Total Deductions} = 	ext{PAYE} + 	ext{Employee NSSF} + 	ext{LST} + 	ext{Salary Advance Installment} + \sum 	ext{Voluntary Deductions}$$
$$	ext{Net Pay} = 	ext{Gross Salary} - 	ext{Total Deductions}$$

### 13.10. Regulatory Loan Loss Provisioning Valuation (IFRS 9 / BOU Matrix)
$$	ext{Required Impairment Provision} = \sum_{k=1}^{5} \left( 	ext{Outstanding Portfolio Balance in Aging Band } k 	imes 	ext{Provision Rate}_k ight)$$
$$	ext{Where: Band Rates} = \{0	ext{--}29	ext{ DPD}: 1\%, \quad 30	ext{--}89	ext{ DPD}: 5\%, \quad 90	ext{--}179	ext{ DPD}: 20\%, \quad 180	ext{--}364	ext{ DPD}: 50\%, \quad 365+	ext{ DPD}: 100\%\}$$

### 13.11. Step-Function Tiered Cash Withdrawal Tariff
$$	ext{Withdrawal Fee} = f(	ext{Amount}) = 	ext{Charge}_i \quad 	ext{where } 	ext{Min Amount}_i \le 	ext{Amount} \le 	ext{Max Amount}_i$$

### 13.12. Fixed Deposit Accrued Interest Formula
$$	ext{Simple Accrual} = P 	imes \left( rac{R}{100} ight) 	imes \left( rac{	ext{Days Held}}{	ext{Days in Year (365)}} ight)$$

---
## 11. Validation Rules

### Confirmed Validations (Explicit in HTML)
1. **Required Fields:** Marked with asterisks in the UI:
   - Individual: Branch, Salutation, First Name, Last Name, Gender, Marital Status, Date of Birth, Country of Birth, Nationality, Home Ownership, Identification Type, Identification Number, Entry Date, Phone Number, Country, Address, Next of Kin (First name, Second name, Phone, Physical Address, Relationship), Occupation, Employer Name, Employer Address, Income Frequency, Income Per Month, Signature, Identification Document, Profile Photo.
   - Group: Branch, Group Name, Group Type, Registration Number, Entry Date, Phone, Country, Physical Address.
   - Institution: Branch, Institution Name, Registration Number, Registration Date, TIN Number, Business Type, Institution Category, Residence Ownership, Entry Date, Phone, Country, Physical Address, Income Frequency, Income Per Month.
   - User: First Name, Last Name, Username, Email Address, Password, Phone, Role, Branch, all 9 Min/Max transaction limit pairs.
   - Role: Role Name.
2. **Data Formats:**
   - Email: Standard RFC-compliant format.
   - Date: Standard date picker validation.
   - Numeric Bounds: All min amount fields must be non-negative and `<= max amount`.

### Inferred Validations
1. **Uniqueness:**
   - `username` and `email` must be system-unique.
   - `member_number` must be system-unique across all branches.
   - `identification_number` (NIN/Passport) must be unique among individual members.
   - `tin_number` must be unique among institutions.
2. **Phone Number Standards:** International format compliant with E.164 (e.g. Uganda +256...).

### Unknown Validations (Requires Clarification)
1. Exact regex validation for Uganda NIN (National Identification Number: 14 alphanumeric characters).
2. Maximum allowable file sizes and MIME types for uploaded documents and photos.


### 11.4. Payroll & Staff Management Validation Matrix

| Field / Property | Rule Type | Validation Constraint | Error Condition / Response |
|:---|:---|:---|:---|
| `identification_number` (NIN) | Format & Uniqueness | 14-character alphanumeric string matching NIRA standard; unique across all staff | `INVALID_NIN_FORMAT` / `DUPLICATE_STAFF_NIN` |
| `phone_number` | Format & Uniqueness | Valid E.164 telecommunications format starting with `+256` | `INVALID_PHONE_NUMBER` |
| `basic_salary` | Value Range | Numeric value $> 0.00$ | `INVALID_BASIC_SALARY` |
| `requested_amount` (Advance) | Exposure Bound | $\le 50\%$ of employee's Basic Salary | `ADVANCE_EXCEEDS_SALARY_LIMIT` |
| `monthly_deduction_amount` | DSR Constraint | Must not reduce estimated net salary below $33.3\%$ of gross salary | `DEBT_SERVICE_RATIO_EXCEEDED` |
| `savings_account_id` | Account State | Bound savings account must be in status `ACTIVE` and belong to the staff member | `INVALID_STAFF_SAVINGS_ACCOUNT` |

### 11.5. Product & Institutional Configuration Validation Matrix

| Field / Property | Rule Type | Validation Constraint | Error Condition / Response |
|:---|:---|:---|:---|
| `min_amount` vs `max_amount` | Range Order | `min_amount` must be strictly $<`max_amount` | `INVALID_PRINCIPAL_RANGE` |
| `annual_interest_rate` | Bounds | Non-negative numeric value $\ge 0.00\%$ | `INVALID_INTEREST_RATE` |
| `chart_account_*` | COA Integrity | Selected GL accounts must exist in `chart_of_accounts` and match category | `INVALID_GL_ACCOUNT_MAPPING` |
| `tier_brackets` | Interval Order | Bracket ranges must be continuous: $	ext{Min}_{i+1} = 	ext{Max}_i + 1$ | `DISCONTINUOUS_TIER_BRACKETS` |
| `provision_percentage` | Percentage Bound | $0.00\% \le 	ext{Rate} \le 100.00\%$ | `INVALID_PROVISION_PERCENTAGE` |

---
## 12. Authentication Requirements

### Credentials & Session Flow
- **Primary Mechanism:** Username and Password authentication (`POST /api/auth/login`).
- **Two-Factor Authentication (SMS OTP):** Evidence in `list_users.html` action button *"Enable SMS OTP"*. When enabled, a one-time numeric passcode must be generated and dispatched via SMS.
- **Token / Session Lifecycle:**
  - Token refresh endpoint: `/api/auth/refresh`.
  - User identity introspection: `/api/auth/user`.
  - Termination: `/api/auth/logout`.
- **Idle Timeout:** 5 minutes (300 seconds) inactivity threshold as explicitly declared in the Idle Detector widget.
- **Credential Governance:** Staff password change capability supported via `/profile` ("Change Password").

---
""")

    # 13. Authorization Requirements
    sections.append("""## 13. Authorization Requirements

### Complete Catalog of 34 Modules & 382 Permissions
The backend authorization engine must enforce the complete catalog of permissions reverse-engineered from `create_role.html`:

| Module | Perm Count | Sample Observed Permissions |
|---|---|---|
| **savings** | 24 | `delete standing order`, `approve standing order`, `reject standing order`, `view savings account statement`, `reverse withdrawal`, `reverse deposit`, `register direct debit credit to customer branch`, `register deposit`, `update deposit`, `register withdrawal`, `update withdrawal`, `register savings transfer`, `update savings transfer`, `register standing order`, `update standing order`, `terminate standing order`, `register savings account credit`, `update savings account credit`, `register savings account debit`, `update savings account debit`, `register direct account credit`, `register direct account debit`, `update direct account credit`, `update direct account debit` |
| **bulk files** | 5 | `approve bulk file`, `reject bulk file`, `process bulk file`, `upload bulk file`, `rollback bulk file` |
| **fixed deposits** | 10 | `accrue fixed deposit`, `rollback fixed deposit`, `award fixed deposit`, `register fixed deposit`, `update fixed deposit`, `delete fixed deposit`, `approve fixed deposit`, `reject fixed deposit`, `view fixed certificate`, `terminate fixed deposit` |
| **scheduled tasks** | 5 | `deactivate task`, `activate task`, `run active task immediately`, `run all due active tasks`, `schedule task` |
| **accounting** | 19 | `register journal`, `reverse journal`, `approve withdrawal request`, `register bank transfer`, `approve bank transfer`, `perform divergent branch journal`, `approve transaction`, `reverse transaction`, `reject transaction`, `register transaction reversal`, `register budget`, `update budget application`, `approve budget`, `reject budget`, `update budget`, `delete budget`, `register withdrawal charge ranges`, `update withdrawal charge ranges`, `delete withdrawal charge ranges` |
| **reports** | 109 | `update report`, `view portfolio monitoring report`, `view all customers`, `view arrears vs savings`, `view asset depreciation`, `view audit trail`, `view backdated transactions`, `view balance sheet`, `view borrower category by gender`, `view branch summary`, `view budget`, `view cash flow statement`, `view cleared loans`, `view compulsory savings`, `view cumulated savings`, `view cumulative shares`, `view customer applications`, `view customer signatories`, `view customer stakeholders`, `view disbursements`, `view disposed assets`, `view dividends shared`, `view due loans`, `view failed standing orders`, `view fixed asset profiles`, `view fixed asset transfers`, `view fixed assets register`, `view fixed deposits`, `view forecast`, `view general ledger`, `view group customer`, `view group nonmember customer`, `view income statement`, `view individual customer`, `view institution customer`, `view joint customer`, `view journals`, `view loan applications`, `view loan arrears`, `view loan bookings`, `view loan guarantors`, `view loan ledger`, `view loan loss provissions`, `view loan products`, `view loan recoveries`, `view loan repayment summary`, `view loan repayments`, `view maturity`, `view msacco transactions`, `view officer summary`, `view outstanding loans`, `view par by ageing`, `view par by ageing summary`, `view payroll`, `view periodic loan transaction`, `view pmt balance sheet`, `view pmt income statement`, `view pmt performance indicators`, `view pmt portfolio activity`, `view portfolio summary`, `view portifolio monitoring`, `view portifolio status`, `view prepaid interest`, `view product by borrower category`, `view profit loss`, `view rescheduled loans`, `view salary payments`, `view savings account applications`, `view savings accounts`, `view savings ledger`, `view savings products`, `view savings transactions`, `view sector by borrower category`, `view sector distribution by gender`, `view sector distribution by product`, `view share transactions`, `view shares ledger`, `view sms notification`, `view staff kyc`, `view staff transfers`, `view standing orders`, `view till sheet`, `view transactions`, `view transferred customers`, `view transferred loans`, `view trial balance`, `view users`, `view written off loans`, `view written off loans schedule`, `view msacco credit statement`, `view guarantees`, `view ABI guarantee`, `view ABI line of credit`, `view loan ageing`, `view customer statement`, `view group consolidation`, `view nin verifications`, `view umra balance sheet`, `view umra income statement`, `view umra par by ageing`, `view msacco medical`, `view mismatches`, `view anti money laundering`, `view savings account statement transactions`, `view management reports`, `view audit reports`, `view bou liquidity`, `view bou bsa`, `view salary advance statement` |
| **loans** | 45 | `update provisions`, `refund last repayment`, `initiate crb report consent`, `get crb report`, `register sponsor`, `update sponsor`, `delete sponsor`, `update loan sponsor`, `unbook loans`, `disapprove loan application`, `download crb files`, `view crb register`, `admin fees waiver`, `view loan schedule`, `cancel loan application`, `unbook compulsory savings`, `unbook compulsory shares`, `delete application collateral`, `apply for loan`, `approve loan application`, `reject loan application`, `update loan application`, `delete loan application`, `reject disbursement`, `disburse loan`, `update disbursement`, `delete disbursement`, `register repayment`, `register refund`, `register penalty waiver`, `register interest waiver`, `reverse waiver`, `send loan to legal`, `remove loan from legal`, `penalize loan`, `write off loan`, `unwrite off loan`, `register loan recovery`, `delete loan recovery`, `reschedule loan`, `register grace period on running loan`, `transfer loan`, `register holiday`, `update holiday`, `delete holiday` |
| **technical** | 4 | `activate license`, `update sms settings`, `update msacco settings`, `manage system jobs` |
| **msacco** | 9 | `register msacco deposit`, `register msacco withdrawal`, `msacco account balance`, `msacco account statement`, `register msacco medical treatment`, `register msacco medical billing`, `reset msacco pin`, `activate msacco account`, `deactiavate msacco account` |
| **account deduction** | 5 | `register account deduction`, `update account deduction`, `activate account deduction`, `deactivate account deduction`, `deduct account opening fees` |
| **fixed assets** | 9 | `impair fixed asset`, `register fixed asset`, `update fixed asset`, `delete fixed asset`, `register fixed asset depreciation`, `delete fixed asset depreciation`, `dispose fixed asset`, `revalue fixed asset`, `transfer fixed asset` |
| **payroll deduction** | 4 | `register payroll deduction`, `update payroll deduction`, `activate payroll deduction`, `deactivate payroll deduction` |
| **payroll** | 17 | `pay staff salary`, `register staff allowance`, `update staff allowance`, `delete staff allowance`, `register payroll allowance`, `update payroll allowance`, `delete payroll allowance`, `view payroll allowance`, `register other payroll deduction`, `update other payroll deduction`, `delete other payroll deduction`, `view other payroll deductions`, `request salary advance`, `approve salary advance`, `reject salary advance`, `cancel salary advance`, `disburse salary advance` |
| **staff** | 13 | `transfer staff`, `register staff deduction/addition`, `update staff deduction/addition`, `delete staff deduction/addition`, `register staff`, `update staff`, `delete staff`, `activate staff`, `deactivate staff`, `register staff salary advance settings`, `update staff salary advance settings`, `delete staff salary advance settings`, `view staff` |
| **customers** | 15 | `verify customer NIN`, `review kyc`, `register agency applications`, `approve agency applications`, `delete agency applications`, `reject agency applications`, `register individual customer`, `update individual customer`, `register group customer`, `update group customer`, `register joint customer`, `update joint customer`, `register institution customer`, `update institution customer`, `delete customer` |
| **interswitch** | 1 | `interswitch card issuance` |
| **internet-banking** | 2 | `enable internet banking`, `disable internet banking` |
| **cards** | 3 | `register card issuance`, `approve card issuance`, `issue cards` |
| **settings** | 6 | `register custom fields`, `update custom fields`, `update setting`, `update forex and currency`, `activate multi currency`, `deactivate multi currency` |
| **bulk sms** | 4 | `create messaging list`, `update messaging list`, `delete messaging list`, `send bulk sms` |
| **modules** | 10 | `access bulk messaging module`, `access savings module`, `access loans module`, `access shares module`, `access settings module`, `access accounting module`, `access payroll module`, `access agency banking module`, `access reports module`, `access user management module` |
| **guarantees** | 7 | `apply for guarantee`, `update guarantee application`, `approve guarantee application`, `delete guarantee application`, `reject guarantee application`, `view guarantee details`, `terminate guarantee` |
| **shares** | 13 | `view customer share transactions`, `register share purchase`, `update share purchase`, `register share transfer`, `update share transfer`, `register share liquidation`, `update share liquidation`, `register share value`, `reverse share purchase`, `reverse share liquidation`, `reverse share transfer`, `share dividends`, `rollback dividends` |
| **dashboard** | 3 | `view loan portifolio metrics`, `view profitability metrics`, `view transaction revenue metrics` |
| **close financial period** | 2 | `close financial period`, `rollback closed period` |
| **chart** | 5 | `register chart account`, `update chart account`, `activate chart account`, `deactivate chart account`, `delete chart account` |
| **users** | 10 | `register user account`, `update user account`, `delete user account`, `disable user account`, `enable user account`, `register user profile`, `update user profile`, `delete user profile`, `enable user profile`, `disable user profile` |
| **accounts** | 8 | `register savings account`, `update savings account`, `delete savings account`, `approve savings account`, `close savings account`, `reopen savings account`, `freeze savings account`, `unfreeze savings account` |
| **products** | 3 | `register product`, `update product`, `delete product` |
| **branches** | 5 | `create branch`, `update branch`, `activate branch`, `deactivate branch`, `delete branch` |
| **fixed asset profiles** | 3 | `register fixed asset profile`, `update fixed asset profile`, `delete fixed asset profile` |
| **database** | 4 | `activate restoration mode`, `deactivate restoration mode`, `restore backup`, `download backup` |

---
""")

    # 14. Functional Requirements
    sections.append("""## 14. Functional Requirements

### FR-001 — Member Category Selection
- **Requirement:** The system shall present a registration gateway allowing the operator to select one of four member entity types: Individual, Group, Joint, or Institution.
- **Entity:** `Customer`
- **Actor:** Customer Care / Staff
- **Source:** `create_member_step_one.html`
- **Priority:** MUST HAVE
- **Preconditions:** Staff is authenticated and holds permission `register individual customer` / `register group customer` / etc.
- **Input:** Selected entity type (`INDIVIDUAL`, `GROUP`, `JOINT`, `INSTITUTION`).
- **Business Rules:** Navigates directly to the specialized multi-field schema for the chosen category.
- **Expected Result:** Renders appropriate registration form.
- **Failure Conditions:** Unauthorized access redirects to 403 Forbidden.
- **Evidence Level:** Explicit

### FR-002 — Individual Member Registration
- **Requirement:** The system shall capture complete individual KYC data across Bio Data, Contact Information, Next of Kin, Employment, Income, Bank Details, and Document Uploads, persisting the record in `PENDING` approval status.
- **Entity:** `Customer`, `CustomerIndividual`, `NextOfKin`, `EmploymentIncome`, `ExternalBank`
- **Actor:** Customer Care / Registration Officer
- **Source:** `create_individual_member.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid branch and designated savings officer selected.
- **Input:** Salutation, Names, Gender, Marital Status, DOB, Nationality, Home Ownership, Identification Type & Number, Entry Date, Phone, Physical Address, Next of Kin details, Occupation, Income Per Month, Signature scan, ID document scan, Photo file.
- **Business Rules:** Form submission enters the Maker-Checker queue (`list_member_applications.html`). System generates unique formatted `member_number` (e.g. `00350000519`).
- **Expected Result:** Member record created in `PENDING` state; event `customer_applications_pending_approval` dispatched.
- **Failure Conditions:** Missing mandatory fields; duplicate identification number / NIN.
- **Evidence Level:** Explicit

### FR-003 — Member Application Approval & Rejection
- **Requirement:** The system shall allow authorized managers to review pending member applications and execute an approval or rejection decision.
- **Entity:** `Customer`
- **Actor:** Branch Manager / Checker
- **Source:** `list_member_applications.html`
- **Priority:** MUST HAVE
- **Preconditions:** Member application exists in `PENDING` status. Checker is not the maker who created the record.
- **Input:** Application ID, Approval Decision (`APPROVE` or `REJECT`), Rejection Reason (if rejected).
- **Business Rules:** On approval, status transitions to `ACTIVE`, core savings passbook ledger is provisioned, and welcome SMS event is triggered. On rejection, status transitions to `REJECTED`.
- **Expected Result:** Application removed from pending queue and visible under appropriate master/rejected directory.
- **Failure Conditions:** Checker is maker (BR-001 violation); application already decided.
- **Evidence Level:** Explicit

### FR-004 — Group Member Registration
- **Requirement:** The system shall register solidarity lending groups and VSLAs with group type classification (Members vs Non Members) and official registration certificates.
- **Entity:** `Customer`, `CustomerGroup`
- **Actor:** Field Officer / Staff
- **Source:** `create_group.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated operator with group registration privilege.
- **Input:** Branch, Group Name, Group Type (`Members` or `Non Members`), Registration Number, Entry Date, Savings Officer, Phone, Address.
- **Business Rules:** Groups may hold group savings accounts and act as institutional borrowing entities.
- **Expected Result:** Group entity persisted in `PENDING` status awaiting approval.
- **Failure Conditions:** Duplicate group registration number within branch.
- **Evidence Level:** Explicit

### FR-005 — Joint Account Registration
- **Requirement:** The system shall register joint member entities shared by two or more individuals with shared contact information and loan limit settings.
- **Entity:** `Customer`, `CustomerJoint`
- **Actor:** Customer Care / Staff
- **Source:** `create_joint.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated operator.
- **Input:** Branch, Joint Name, Entry Date, Assigned Officer, Phone, Address, Loan Limit toggle.
- **Business Rules:** Joint entity requires multi-signatory rules for subsequent withdrawals.
- **Expected Result:** Joint customer record persisted.
- **Failure Conditions:** Missing mandatory branch, name, or phone.
- **Evidence Level:** Explicit

### FR-006 — Institution Customer Registration
- **Requirement:** The system shall register corporate and institutional customers, capturing legal categorization, URA TIN numbers, corporate category, and business sectors.
- **Entity:** `Customer`, `CustomerInstitution`
- **Actor:** Business Development / Customer Care
- **Source:** `create_institution.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated staff.
- **Input:** Institution Name, Registration Number, Registration Date, TIN Number, Business Type (e.g. Agriculture, SACCO, MDI, etc.), Institution Category (e.g. Limited Liability, NGO, etc.), Residence Ownership, Entry Date, Phone, Physical Address, Monthly Income.
- **Business Rules:** TIN must be validated for format and uniqueness.
- **Expected Result:** Institutional record created for approval.
- **Failure Conditions:** Duplicate TIN or registration number.
- **Evidence Level:** Explicit

### FR-007 — User Account Provisioning & Financial Authorization Limits
- **Requirement:** The system shall provision staff user accounts with individual operational bounds across 9 financial transaction categories and alert subscription selections.
- **Entity:** `User`, `UserNotificationSubscription`
- **Actor:** System Administrator
- **Source:** `create_user.html`
- **Priority:** MUST HAVE
- **Preconditions:** Administrator holds `register user account` permission.
- **Input:** First Name, Last Name, Username, Email, Password, Phone, Designation/Role, Station/Teller, Branch, 9 Min/Max Limit pairs, Selection of Notification Subscriptions (up to 22 checkboxes).
- **Business Rules:** Password must be hashed securely; Min Amount must not exceed Max Amount; limits strictly enforce maker-checker thresholds at runtime.
- **Expected Result:** User account created and enabled.
- **Failure Conditions:** Duplicate username or email; Min Amount > Max Amount.
- **Evidence Level:** Explicit

### FR-008 — User Lifecycle State Management (Deactivate, Delete, SMS OTP)
- **Requirement:** The system shall provide operational controls on staff user accounts to deactivate active users, soft-delete records, and toggle SMS OTP two-factor authentication.
- **Entity:** `User`
- **Actor:** System Administrator
- **Source:** `list_users.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target user exists.
- **Input:** Target User ID, Action (`DEACTIVATE`, `ACTIVATE`, `DELETE`, `ENABLE_SMS_OTP`, `DISABLE_SMS_OTP`).
- **Business Rules:** Deactivated users cannot authenticate (`ERR_ACCOUNT_INACTIVE`). Deleted users are soft-deleted to preserve transaction audit integrity.
- **Expected Result:** User status updated immediately; active sessions invalidated.
- **Failure Conditions:** Administrator cannot deactivate own account.
- **Evidence Level:** Explicit

### FR-009 — Role Profile Definition & Operating Hours Configuration
- **Requirement:** The system shall allow administrators to create and edit Role Profiles, configuring daily operating time windows (`open_time` to `close_time`) and assigning granular checkboxes from the 382 permission pool.
- **Entity:** `Role`, `RolePermission`
- **Actor:** System Administrator
- **Source:** `create_role.html`, `edit_role.html`, `user_details.html`
- **Priority:** MUST HAVE
- **Preconditions:** Administrator holds `register user profile` or `update user profile`.
- **Input:** Role Name, Open Time (Time picker), Close Time (Time picker), Array of selected permission keys.
- **Business Rules:** Operating hours enforce system access cutoff; permission updates take effect on subsequent token evaluation.
- **Expected Result:** Role persisted and available for user assignment.
- **Failure Conditions:** Empty role name; close time precedes open time.
- **Evidence Level:** Explicit

### FR-010 — Role Activation, Deactivation & Deletion
- **Requirement:** The system shall provide lifecycle management for role profiles, allowing activation, deactivation, and deletion.
- **Entity:** `Role`
- **Actor:** System Administrator
- **Source:** `list_roles.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target role exists.
- **Input:** Role ID, Action (`ACTIVATE`, `DEACTIVATE`, `DELETE`).
- **Business Rules:** A role cannot be deleted if active users are assigned to it (e.g., *"1 User assigned"* guard). Deactivating a role blocks authentication for all assigned users.
- **Expected Result:** Role status updated.
- **Failure Conditions:** Attempting to delete a role with assigned users (`ERR_ROLE_IN_USE`).
- **Evidence Level:** Explicit

### FR-011 — Real-Time Liquidity & Float Balance Monitoring
- **Requirement:** The system shall maintain and expose real-time aggregate balances for external payment channels: MSACCO Credit, MTN Mobile Money Float, Airtel Money Float, Bank Transfer Float, and General Float.
- **Entity:** `ExternalFloatLedger`
- **Actor:** System / Cashier / Manager
- **Source:** Global Top Bar across all 17 HTML files
- **Priority:** MUST HAVE
- **Preconditions:** Integration webhooks and ledger updates configured.
- **Input:** Channel Identifier, Float Inflow / Outflow, Current Balance.
- **Business Rules:** Float is debited on outgoing mobile money cashouts and credited on incoming deposits. Balance warnings fire when float breaches safety floor.
- **Expected Result:** Current float balances returned to client header widgets.
- **Failure Conditions:** Channel gateway timeout.
- **Evidence Level:** Explicit

### FR-012 — 5-Minute Inactivity Session Timeout
- **Requirement:** The system shall track client activity and invalidate sessions exceeding 300 seconds of inactivity.
- **Entity:** `UserSession`
- **Actor:** System / Authentication Engine
- **Source:** Idle Detector component across all 17 HTML files
- **Priority:** MUST HAVE
- **Preconditions:** Active session token.
- **Input:** Last activity timestamp.
- **Business Rules:** Inactivity > 5 minutes invalidates token and forces re-login.
- **Expected Result:** Session expired; client redirected to login.
- **Failure Conditions:** N/A
- **Evidence Level:** Explicit

### FR-013 — Asynchronous Job Monitoring & Failure Notification
- **Requirement:** The system shall capture execution logs for all background tasks and broadcast high-priority failure notifications to subscribed users whenever a job fails.
- **Entity:** `SystemJob`, `JobExecutionLog`, `Notification`
- **Actor:** System / Scheduler Engine
- **Source:** Global Header Alert: *"A recently started job has failed Today 10:26:26 PM"*
- **Priority:** MUST HAVE
- **Preconditions:** Background job dispatched.
- **Input:** Job ID, Status (`SUCCESS`, `FAILED`), Error Details.
- **Business Rules:** Failed job records error in `/settings/jobs/{id}/logs` and sends alert to users subscribed to `failed_job`.
- **Expected Result:** In-app notification with "Mark as read" trigger.
- **Failure Conditions:** Database connection loss during error logging.
- **Evidence Level:** Explicit

### FR-014 — Agency Banking Application Queue & Vetting
- **Requirement:** The system shall provide an application intake and maker-checker queue for third-party banking agents with pending and rejected tabs.
- **Entity:** `AgencyApplication`
- **Actor:** Agency Banking Lead / Manager
- **Source:** `list_agency_applications.html`
- **Priority:** MUST HAVE
- **Preconditions:** Agency application submitted.
- **Input:** Agent ID, Business Name, Location, Branch, Decision (`APPROVE`, `REJECT`).
- **Business Rules:** Approved agents obtain agency terminal credentials and float limits.
- **Expected Result:** Agent status updated.
- **Failure Conditions:** Incomplete KYC documentation.
- **Evidence Level:** Explicit


### FR-034 — Staff Bio-Data Registration & Profile Management
- **Description:** Enables HR officers to register full employee records, bio-data profiles, contact details, next of kin, and mandatory identification documents.
- **Actor:** HR / Payroll Officer (`ROLE_PAYROLL_OFFICER`)
- **Preconditions:** Authenticated user with `create_staff` permission; valid branch office selected.
- **Trigger:** Submission of the "Register New Staff" form (`create_staff.html`).
- **Detailed Workflow:**
  1. Operator navigates to `/payroll/staff/create`.
  2. Operator enters Bio Data: First Name, Last Name, Other Name, Email Address, Gender, Marital Status, Identification Type (National ID, Passport), and Identification Number (NIN).
  3. Operator enters Contact Information: Telephone Number (prefixed with country code `+256`), Physical Address.
  4. Operator specifies Next of Kin: Names, Telephone Contact, Home Address, and Relationship.
  5. Operator uploads required digital attachments: Employment Contract, Signature, Identification Document, and Profile Photo.
  6. Backend validates uniqueness of Email, NIN, and Telephone.
  7. System persists the staff record in status `ACTIVE` and generates an internal Staff ID.
- **Business Validations:**
  - Mandatory fields must not be null.
  - NIN must adhere to the National Identification and Registration Authority (NIRA) checksum format.
  - Telephone must match standard E.164 telecommunications format.
  - Uploaded files must be PDF, PNG, or JPEG under 5 MB.
- **Postconditions:** Staff entity created in database; document files saved to secure artifact store; record listed in `staff_list.html`.
- **Evidence Level:** Explicit (`create_staff.html`, `staff_list.html`).

### FR-035 — Staff Employment Remuneration & Statutory Registration
- **Description:** Configures an employee's appointment date, basic salary, remuneration payment method, tax registration numbers, and statutory deduction enrollment.
- **Actor:** HR / Payroll Officer
- **Preconditions:** Existing staff profile created.
- **Trigger:** Submitting employment parameters during staff creation or profile editing (`edit_payroll_staff.html`).
- **Detailed Workflow:**
  1. Operator inputs Date Appointed, Basic Salary (UGX), and Designation / Position.
  2. Operator selects Payment Method: `savings` (internal SACCO savings account), `bank` (external commercial bank), or `mobile_money`.
  3. If `savings` is selected, operator binds an active member savings account (`savings_account_id`).
  4. Operator inputs Tax Identification Number (TIN) and Social Security Number (NSSF).
  5. Operator activates or deactivates statutory deduction flags: `PAYE` (Yes/No), `SSF` (Yes/No), and `LST` (Yes/No).
  6. Backend validates TIN format and ensures the linked savings account is active and belongs to the staff member.
  7. System stores the employment remuneration contract.
- **Business Validations:**
  - Basic Salary must be a positive number ($> 0.00$).
  - If PAYE/SSF toggles are enabled, respective tax registration numbers should be recorded.
  - Linked savings account must be verified as open and not in `FROZEN` or `CLOSED` state.
- **Postconditions:** Employment terms saved; staff marked as eligible for monthly payroll compilation.
- **Evidence Level:** Explicit (`create_staff.html`, `edit_payroll_staff.html`).

### FR-036 — Allowance & Voluntary Deduction Configuration
- **Description:** Defines standard institutional allowance categories and deduction rules, and assigns recurring or one-off amounts to specific staff members.
- **Actor:** HR / Payroll Officer
- **Preconditions:** Authenticated user with payroll settings permissions.
- **Trigger:** Creating allowance/deduction types (`payroll_allowances.html`, `payroll_deductions.html`, `payroll_other_deductions.html`) or mapping them to an employee (`payroll_staff_detail.html`).
- **Detailed Workflow:**
  1. Operator creates standard allowance categories (e.g., Housing, Transport, Medical, Responsibility) as fixed UGX amounts or percentages.
  2. Operator creates voluntary deduction categories (e.g., Staff SACCO Shares, Staff Welfare, Staff Loan, Health Insurance).
  3. In `payroll_staff_detail.html`, operator selects an allowance or deduction, specifies the Amount or Percentage, and assigns it to the staff member.
  4. Backend validates that total assigned deductions do not violate the one-third net pay ceiling (BR-018).
  5. System stores the recurring allowance and deduction assignments.
- **Business Validations:**
  - Percentage-based allowances/deductions must be bounded between $0.00\%$ and $100.00\%$.
  - Fixed amounts must be non-negative.
- **Postconditions:** Staff compensation structure updated; included in subsequent payroll calculations.
- **Evidence Level:** Explicit (`payroll_allowances.html`, `payroll_deductions.html`, `payroll_other_deductions.html`, `payroll_staff_detail.html`).

### FR-037 — Monthly Payroll Calculation & Gross-to-Net Engine
- **Description:** Compiles the complete monthly payroll register across all active staff, computing Gross Pay, statutory taxes (PAYE, NSSF, LST), voluntary deductions, salary advance recoveries, and Net Pay.
- **Actor:** HR / Payroll Officer
- **Preconditions:** Month-end cutoff reached; active staff profiles with compensation structures defined.
- **Trigger:** Initiating monthly payroll calculation (`payroll_list.html`).
- **Detailed Workflow:**
  1. System queries all staff with status `ACTIVE`.
  2. For each employee, system computes:
     - Basic Salary
     - Total Allowances ($\sum 	ext{Allowances}$)
     - Gross Salary = Basic Salary + Total Allowances
     - Statutory NSSF Employee Contribution ($5\%$ of Gross)
     - Statutory PAYE Tax based on URA progressive brackets (BR-017)
     - Local Service Tax (if applicable for current period)
     - Active Salary Advance Monthly Installment (BR-018)
     - Voluntary Deductions (Shares, Welfare, Loan installments)
     - Total Deductions = NSSF + PAYE + LST + Advances + Voluntary Deductions
     - Net Pay = Gross Salary - Total Deductions
  3. System renders the comprehensive Payroll Summary table with columns: First Name, Last Name, Phone, Payment Method, Salary, Allowances, Gross Salary, Deductions Breakdown, Net Pay.
  4. System compiles total SACCO payroll expenditure, total tax liability, and total net disbursement.
- **Business Validations:**
  - Net pay cannot be negative ($	ext{Net Pay} \ge 0.00$).
  - System flags any staff member whose deductions exceed the statutory ceiling for manual review.
- **Postconditions:** Payroll run persisted in status `CALCULATED`; ready for disbursement request.
- **Evidence Level:** Explicit (`payroll_list.html`).

### FR-038 — Two-Step Maker-Checker Salary Disbursement
- **Description:** Executes the formal multi-level authorization and financial disbursement of monthly salaries, with direct automated balance sweeps to staff savings accounts.
- **Actor:** Maker: HR / Payroll Officer; Checker: Finance Director / CEO
- **Preconditions:** Payroll run in status `CALCULATED`; sufficient funds in the designated SACCO funding account.
- **Trigger:** Submitting payment request (`request_salary_payment.html`) and approving disbursement (`pay_salaries.html`).
- **Detailed Workflow:**
  1. Maker reviews the calculated payroll run and submits a formal disbursement request, selecting the execution date and payment batch options: `Staff loans`, `Salary (net only)`, `Account (pay at staff branch)`.
  2. Batch transitions to status `PENDING_APPROVAL`.
  3. Checker logs in, inspects the batch totals, and selects the debit funding account (`Cash account (liability or asset)`).
  4. Checker clicks "Approve & Disburse".
  5. Backend executes an atomic transaction:
     - Debits the SACCO Bank / Cash / Expense account for the total payroll amount.
     - For each employee with payment method `savings`, credits their linked savings account with their Net Pay.
     - Automatically books recovered salary advances, crediting the Salary Advance Asset GL account.
     - Credits the PAYE Withholding Tax Liability GL account.
     - Credits the NSSF Payable Liability GL account.
     - Generates individual transaction vouchers and dispatches SMS credit alerts.
  6. Batch status transitions to `DISBURSED` and is archived in `salaries_disbursed.html`.
- **Business Validations:**
  - Maker and Checker must be distinct users (BR-001, BR-019).
  - Selected funding account must have an actual liquid balance $\ge 	ext{Total Disbursement Amount}$.
- **Postconditions:** General Ledger updated; staff savings accounts credited; audit trail logged.
- **Evidence Level:** Explicit (`request_salary_payment.html`, `pay_salaries.html`, `salaries_disbursed.html`).

### FR-039 — Staff Salary Advance Lifecycle & Automated Payroll Recovery
- **Description:** Manages the full lifecycle of staff salary advances from application, approval, and disbursement to automated monthly payroll deduction sweeps.
- **Actor:** HR / Payroll Officer, Approver: CEO / Finance Director
- **Preconditions:** Active staff member with completed probation and zero running overdue advances.
- **Trigger:** Submitting advance application (`request_salary_advance.html`).
- **Detailed Workflow:**
  1. Operator navigates to `/payroll/advances` and clicks "Request Salary Advance".
  2. Operator selects Staff Member, enters Advance Amount, Monthly Deduction Installment Amount, and Effective Date.
  3. System validates that Advance Amount $\le 50\%$ of Basic Salary (BR-018) and that the staff member has no existing `ACTIVE` advance.
  4. Application is saved in status `PENDING`.
  5. Authorized executive reviews and approves the advance.
  6. On disbursement, system debits `Salary Advance Asset Account` and credits staff savings or cash desk. Status transitions to `ACTIVE`.
  7. During each subsequent monthly payroll run, the gross-to-net engine deducts the monthly deduction amount, reducing the outstanding advance balance.
  8. Once the remaining principal reaches zero, status automatically transitions to `CLEARED`.
  9. System supports manual `REVERSED` or `REJECTED` state transitions with audit remarks.
- **Business Validations:**
  - Advance Amount must be positive.
  - Deduction amount must be $\le 	ext{Advance Amount}$.
- **Postconditions:** Advance agreement recorded; repayment schedule bound to monthly payroll engine.
- **Evidence Level:** Explicit (`request_salary_advance.html`, `salary_advances.html`).

### FR-040 — Multi-Branch Institutional Profile & Global Settings
- **Description:** Configures top-level SACCO institutional parameters, corporate branding, contact addresses, and branch definitions.
- **Actor:** System Administrator (`ROLE_SUPER_ADMIN`)
- **Preconditions:** Super administrator credentials.
- **Trigger:** Editing institutional configuration (`edit_institution_settings.html`).
- **Detailed Workflow:**
  1. Administrator enters Institution Name, Alias (Short Name), Institution Type, Email Address, Phone Number (country code select + number), Physical Address, and Registration Number.
  2. Administrator selects Head Office branch location.
  3. Administrator selects Theme Color (hex/picker) and uploads official corporate Logo.
  4. Administrator configures global functional modules via `settings_page.html` (Loans, Savings, Payroll, Jobs, SMS, Backups).
  5. Backend updates corporate metadata and applies theme branding across the web application.
- **Business Validations:**
  - Institution Name and Registration Number must be unique.
  - Email and Phone Number must be syntactically valid.
- **Postconditions:** Institutional settings persisted; corporate branding visible on client portal and PDF report headers.
- **Evidence Level:** Explicit (`settings_page.html`, `institution_settings.html`, `edit_institution_settings.html`).

### FR-041 — Comprehensive Loan Product Master Definition & Accounting GL Mapping
- **Description:** Establishes parameterized loan product blueprints defining credit bounds, interest calculation algorithms, repayment frequencies, and full General Ledger chart of account bindings.
- **Actor:** Credit Risk & Product Administrator (`ROLE_CREDIT_ADMIN`)
- **Preconditions:** Chart of accounts fully configured; user possesses `create_loan_product` permission.
- **Trigger:** Submitting loan product creation form (`create_product.html`).
- **Detailed Workflow:**
  1. Administrator navigates to `/settings/loans/products/create`.
  2. Administrator enters Product Name, selects Principal Asset Chart Account, and specifies Minimum Loan Amount and Maximum Loan Amount.
  3. Administrator configures Interest Engine: Interest Method (`Flat`, `Declining Balance`, `Amortize`), Calculation Basis (360 vs 365 days), Annual Interest Rate, Annual Penalty Rate, Arrears Grace Days, and Annual Penalty Rate after loan expiry.
  4. Administrator sets Repayment Policies: Frequency of Payment (Daily, Weekly, Monthly, Annually), Maximum Number of Installments, Disbursement Method, and Required Savings Period Limit in Months.
  5. Administrator configures Underwriting Requirements: Number of compulsory loan guarantors, Number of compulsory collateral items, Borrower compulsory savings percentage, Borrower compulsory shares percentage, and "Require credit report on application" toggle.
  6. Administrator configures Operational Toggles: Multiple Running Loans on this product, Enable Prepayments, Allow Daily Interest, Enable Auto recovery from savings, Enable Interest Compute after expiry, Allow MSACCO loan applications, and Allow Disbursal To Member Phone Number.
  7. Administrator binds the complete General Ledger mapping across 25+ accounts: Interest Income, Penalty Income (Auto/Manual), Interest Receivable, Penalty Receivable, Write-Off, Interest Due Write-Off, Penalty Write-Off, Write-Off Recovery, Suspended Interest, Prepaid Interest, Loan Loss Asset/Liability, Loan Loss Expense, Reschedule Account, Penalty Waiver Account, Early Settlement Charge Account, and Admin Fee Income/Waiver/Receivable accounts.
  8. Administrator defines Administration Fees: Flat Value, Percentage, and First Installment collection rules.
  9. System validates all accounts and persists the loan product in status `ACTIVE`.
- **Business Validations:**
  - Minimum Loan Amount must be $< 	ext{Maximum Loan Amount}$.
  - Rates (Interest, Penalty, Compulsory Savings, Compulsory Shares) must be non-negative.
  - All mandatory GL accounts must belong to the correct COA category (Asset, Liability, Income, Expense).
- **Postconditions:** Loan product available in loan origination dropdowns; rules enforced during loan creation and servicing.
- **Evidence Level:** Explicit (`list_loan_products.html`, `create_product.html`).

### FR-042 — Credit Risk & Loan Operational Policy Configuration
- **Description:** Establishes institution-wide default operational policies governing installment booking, reminders, automated sweeps, arrears penalization, and guarantor constraints.
- **Actor:** Credit Administrator
- **Preconditions:** Administrative access to loan settings.
- **Trigger:** Updating global loan policies (`loan_settings.html`).
- **Detailed Workflow:**
  1. Administrator configures operational timing:
     - Number of days in advance to book an installment due.
     - Early settlement interest installments.
     - Number of days in a week (5, 6, or 7).
     - Days before loan due reminder SMS.
     - Overdue reminder interval in days.
  2. Administrator configures fees: Loan Balance Check Charge, Repayment Charge Account, MSACCO Loan Balance Account.
  3. Administrator toggles core credit behavior:
     - Collection of booked installments from savings accounts (automated sweep).
     - Ignore grace period interest / Do not collect grace period interest in 1st installment.
     - Allow rounding off to nearest hundreds / two decimal places.
     - Allow interest scheduling for expired loans.
     - Allow early settlement charge.
     - Allow booking compulsory savings.
     - Allow Member with Arrears to apply for another loan.
     - Allow Changing Loan Application Approval terms at disbursement.
     - Allow Interbranch Loans.
     - Allow Auto-reject Stale Applications.
     - Allow Monthly Interest Accrual.
  4. Administrator sets Guarantor Rules:
     - Guarantors compulsory savings percentage of a loan.
     - Guarantors compulsory shares percentage of a loan.
     - Allow guarantor with arrears (Yes/No).
     - Restrict loan applications for a guarantor with loans in arrears (Yes/No).
     - Require guarantor NIN verification (Yes/No).
  5. Administrator configures Weekend & Term Scheduling:
     - Saturday / Sunday loan scheduling rules.
     - Suspend interest threshold (days past due).
     - Term loan start dates (First Term, Second Term, Third Term for school fees loans).
  6. System saves policies and binds them to the credit engine daemon.
- **Business Validations:**
  - Timing day offsets must be $\ge 0$.
  - Percentage limits must be $\le 100\%$.
- **Postconditions:** Global loan policy updated; applied across all active loan schedules.
- **Evidence Level:** Explicit (`loan_settings.html`).

### FR-043 — Regulatory Loan Loss Provisioning Matrix (BOU / UMRA)
- **Description:** Establishes regulatory aging brackets and mandatory loan loss provisioning rates compliant with Bank of Uganda and UMRA microfinance guidelines.
- **Actor:** Finance Director / Chief Risk Officer
- **Preconditions:** Access to `/settings/loans/loss-provisions`.
- **Trigger:** Configuring loan loss provision schedules (`loan_loss_provisions.html`).
- **Detailed Workflow:**
  1. Operator accesses the loan loss provisioning interface.
  2. System displays standard regulatory aging brackets:
     - 0 to 29 Days (Normal / Current) $	o$ 1%
     - 30 to 89 Days (Watch / Special Mention) $	o$ 5%
     - 90 to 179 Days (Substandard) $	o$ 20%
     - 180 to 364 Days (Doubtful) $	o$ 50%
     - 365+ Days (Loss) $	o$ 100%
  3. Operator can customize or fine-tune provision percentage rates or add specialized portfolio risk tiers.
  4. System binds these provisioning rules to the nightly impairment calculation batch job.
  5. The job calculates required portfolio provisions: $	ext{Impairment} = \sum (	ext{Outstanding Balance} 	imes 	ext{Provision \%})$ and posts debit entries to `Loan Loss Expense` and credit entries to `Loan Loss Allowance (Liability/Contra-Asset)` GL accounts.
- **Business Validations:**
  - Provision rates must be between $0\%$ and $100\%$.
  - Aging bracket boundaries must be continuous and non-overlapping.
- **Postconditions:** Provisioning matrix persisted; referenced by end-of-period financial reporting.
- **Evidence Level:** Explicit (`loan_loss_provisions.html`).

### FR-044 — Institutional Loan Holiday & Repayment Moratorium Management
- **Description:** Configures institutional public holidays, weekends, or special emergency moratoria that automatically adjust loan installment payment due dates.
- **Actor:** Credit Risk Officer
- **Preconditions:** Access to `/settings/loans/holidays`.
- **Trigger:** Creating a loan holiday date (`loan_holidays.html`).
- **Detailed Workflow:**
  1. Operator navigates to Loan Holidays management and clicks "Create Holiday".
  2. Operator enters Holiday Name, Description, From Date, and To Date.
  3. Operator specifies whether the holiday applies to all products or specific selected loan products.
  4. Backend verifies that the holiday range does not overlap with existing entries.
  5. When loan installment schedules are generated or evaluated, any installment falling within an active holiday is automatically shifted to the next business day without triggering arrears or late penalty calculation.
- **Business Validations:**
  - `To Date` must be $\ge 	ext{From Date}$.
- **Postconditions:** Holiday record saved; loan schedule engine honors the moratorium window.
- **Evidence Level:** Explicit (`loan_holidays.html`).

### FR-045 — Credit Reference Bureau (CRB) API & SFTP Gateway Configuration
- **Description:** Manages authentication credentials, communication endpoints, and automated data synchronization parameters for Credit Reference Bureau (CRB) integration.
- **Actor:** System Administrator
- **Preconditions:** Active membership in an accredited credit bureau (Credit Bureau Africa / Compuscan).
- **Trigger:** Submitting CRB credentials (`crb_settings.html`).
- **Detailed Workflow:**
  1. Administrator navigates to `/settings/loans/crb`.
  2. Administrator inputs Identification Code, Enquiries Username, Enquiries Password, SFTP Username, and SFTP Password.
  3. Administrator sets profile flags: `Active` (Yes/No) and `Default` (Yes/No).
  4. Administrator can configure secondary CRB profiles if multi-bureau reporting is supported.
  5. Backend securely encrypts credentials and tests connectivity against the bureau gateway.
  6. The system uses these credentials for real-time credit inquiries during loan application appraisal and for nightly batch portfolio export via SFTP.
- **Business Validations:**
  - Required fields cannot be empty.
  - Passwords must be encrypted at rest using AES-256-GCM.
- **Postconditions:** CRB configuration saved; API client ready for live inquiries and SFTP reporting.
- **Evidence Level:** Explicit (`crb_settings.html`).

### FR-046 — Comprehensive Savings Product Definition & Omnichannel Clearing
- **Description:** Establishes parameterized deposit and savings products, configuring minimum operating balances, dormancy limits, ledger fees, and multi-channel clearing GL accounts.
- **Actor:** Product Administrator
- **Preconditions:** Chart of accounts configured.
- **Trigger:** Submitting savings product creation form (`create_savings_product.html`).
- **Detailed Workflow:**
  1. Administrator navigates to `/settings/savings/products/create`.
  2. Administrator selects Savings Liability Chart Account, Currency, Group Product toggle, and Product Type.
  3. Administrator sets Balance Controls: Opening Balance, Minimum Balance, Minimum Award Balance (minimum balance to earn interest), Transfer Charge, and Dormancy Period in Days.
  4. Administrator sets Behavioral Flags: Allow Overdrafts, Auto Loan Deduction, Compulsory Collections, Consider Withdraw Range Charges, Visible to Customers, POS Payments, POS Purchases, and POS Notifications.
  5. Administrator configures Fees:
     - Monthly Ledger Fees: Amount, Ledger Fee GL account, Interval, Schedule.
     - Contribution Fees: Amount, Account, Interval.
     - Equity Contributions: Amount, Account, Interval, Frequency.
  6. Administrator configures Interest: Annual Interest Rate, Interest Expense Account, Interest Accrual Account, Interest Payable Account, and Award Product.
  7. Administrator binds Omni-Channel Clearing Accounts:
     - Default MSACCO Deposit & Withdrawal clearing accounts.
     - MTN Mobile Money MSACCO Deposit & Withdrawal accounts.
     - Airtel Money MSACCO Deposit & Withdrawal accounts.
     - ABC Bank Clearing & Transfer Charge accounts.
     - ATM Integrations: Equity ATM Deposit/Withdrawal accounts, Interswitch ATM Deposit/Withdrawal accounts.
     - Interswitch Card Issuance Charge and Fee Income Account.
     - Tax / Regulatory Duty: MSACCO Medical Excise Duty Account.
  8. Administrator configures Channel Velocity Limits: Daily, Monthly, and Annual limits for both Transaction Amount and Transaction Count across Counter, ATM, and MSACCO mobile channels.
  9. System validates that all selected GL accounts exist and stores the savings product definition.
- **Business Validations:**
  - Minimum Balance must be $\ge 0.00$.
  - Dormancy period must be $> 0$ days (e.g. 180 or 365 days).
  - All clearing accounts must be balanced between asset and liability classes.
- **Postconditions:** Savings product available for account opening; transaction rules enforced by core ledger.
- **Evidence Level:** Explicit (`list_savings_products.html`, `create_savings_product.html`).

### FR-047 — Global Savings Operational Policies, Overdrafts & Automation
- **Description:** Establishes institution-wide policies governing savings counter SMS notifications, overdraft facilities, ledger fee arrears, automated share deductions, and cash desk reserve ceilings.
- **Actor:** Deposit Operations Administrator
- **Preconditions:** Access to `/settings/savings`.
- **Trigger:** Saving policy updates (`saving_settings.html`).
- **Detailed Workflow:**
  1. Administrator toggles operational flags:
     - Enable Over Counter SMS.
     - Enable Auto Debits on Frozen Accounts.
     - Enable Monthly Ledger Fees.
     - Enable Overdrafts.
     - Allow opening fees deduction below minimum balance.
     - Allow ledger fees deduction below minimum balance.
     - Allow bulk file collections SMS.
     - Allow Charge Monthly Ledger Fees Arrears.
     - Allow Auto Share Deductions.
     - Allow share deduction below minimum balance.
  2. Administrator configures intervals:
     - Number of months before charging missed savings target.
     - Number of months before charging ledger fees.
     - Number of months before charging contribution fees.
     - Number of months before first share deduction.
  3. Administrator sets GL and Role bindings: Collections Officer Role, Inter-branch Receivable Account, Inter-branch Payable Account.
  4. Administrator sets Cash Desk Reserve Ceilings: Teller Reserve Amount Limit and Safe Reserve Amount Limit (triggering cash desk alerts when exceeded).
  5. Administrator configures Verification Charges: NIN Verification Charge & Account, Credit Report Charge, Credit Score Charge & Account.
  6. System persists global policies and binds them to teller and savings engine modules.
- **Business Validations:**
  - Cash desk reserve limits must be positive amounts.
  - Fee charges must be non-negative.
- **Postconditions:** Policies active across all teller operations and background scheduled sweeps.
- **Evidence Level:** Explicit (`saving_settings.html`).

### FR-048 — Tiered Cash Withdrawal Fee Matrix & Surcharge Calculation
- **Description:** Configures a bracketed step-function tariff matrix for cash withdrawals over the counter or across digital channels.
- **Actor:** Product Administrator
- **Preconditions:** Access to `/settings/savings/withdraw-charges`.
- **Trigger:** Updating withdrawal charge tiers (`savings_withdraw_charges.html`).
- **Detailed Workflow:**
  1. Administrator defines an ordered set of bracket tiers, specifying for each tier:
     - Minimum Amount (UGX)
     - Maximum Amount (UGX)
     - Charge (UGX flat fee or percentage)
  2. Backend validates that brackets are contiguous without gaps or overlapping intervals (e.g. Tier 1: 0 - 50,000; Tier 2: 50,001 - 200,000; Tier 3: 200,001 - 1,000,000, etc.).
  3. When an operator processes a cash withdrawal, the system evaluates the transaction amount against this matrix, derives the applicable fee, and posts the fee voucher to the configured Withdrawal Fee Income GL account.
- **Business Validations:**
  - Each tier must have $	ext{Max Amount} > 	ext{Min Amount}$.
  - Charge must be $\ge 0.00$.
- **Postconditions:** Tier matrix stored; evaluated in real time during teller or digital withdrawals.
- **Evidence Level:** Explicit (`savings_withdraw_charges.html`).

### FR-049 — Fixed Deposit Product Tier Setup & Rollover Engine
- **Description:** Configures term deposit duration profiles, interest accrual and award frequencies, and automated rollover preferences.
- **Actor:** Product Administrator
- **Preconditions:** Access to `/settings/fixed-deposits`.
- **Trigger:** Creating or modifying fixed deposit configuration (`fixed_deposit_settings.html`).
- **Detailed Workflow:**
  1. Administrator specifies term parameters:
     - Fixing Period in Months (e.g. 3, 6, 12, 24).
     - Recycle Type (Rollover policy: `Principal Only`, `Principal + Interest`, `Do Not Renew`).
     - Annual Interest Rate (%).
     - Interest Award Frequency and Interval.
     - Interest Accrual Frequency and Interval.
     - Allow Daily Interest Computation toggle.
  2. Administrator binds GL accounts: Term Deposit Liability Account, Interest Expense Account, Interest Payable Account.
  3. Administrator sets status to `ACTIVE`.
  4. System validates configuration and makes the term tier available when booking new fixed deposits (`create_fixed_deposit.html`).
- **Business Validations:**
  - Fixing period must be an integer $> 0$.
  - Interest rate must be between $0.00\%$ and $100.00\%$.
- **Postconditions:** Fixed deposit product tier active; utilized by term deposit booking and automated maturity engines.
- **Evidence Level:** Explicit (`fixed_deposit_settings.html`).

### FR-050 — Social Performance Management (SPM) & Proxy Means Test (PMT) Setup
- **Description:** Configures poverty probability scoring, Gross National Income (GNI) per capita benchmarks, and microfinance social performance indicators compliant with MIX Market / CGAP standards.
- **Actor:** Chief Risk Officer / Executive Management
- **Preconditions:** Access to `/settings/pmt`.
- **Trigger:** Submitting PMT settings form (`pmt_settings.html`).
- **Detailed Workflow:**
  1. Operator navigates to `/settings/pmt`.
  2. Operator selects default Regular Savings Product and inputs national economic indicators: GNI Per Capita (UGX), Market Interest Rate, Subsidised Borrowed Funds Rate, Inflation Rate, and National Poverty Rate.
  3. Operator binds GL accounts across MIX Market operational and financial expense categories: Cash, Deposits in financial institutions, Short/Long term investments, Subsidised/Market debt, Loans from central bank, Income taxes, Deferred Income, Donated equity, Reserves, Share capital, Investment income, Grants for loan capital/operations, Non-financial income/expenses, Dividends paid on shares, Governance costs, Rent/utilities, Travel/transport, Stationery, and Personnel expenses.
  4. System validates accounts and saves SPM configuration.
  5. The system uses these parameters to generate social performance reports, client poverty outreach scores, and regulatory financial sustainability ratios.
- **Business Validations:**
  - Macroeconomic percentage rates must be non-negative.
  - Linked accounts must be valid general ledger accounts.
- **Postconditions:** PMT/SPM metrics persisted; referenced in annual social auditing and regulatory returns.
- **Evidence Level:** Explicit (`pmt_settings.html`).

---
## 15. API Requirements

Based on explicit form submissions, list page interactions, and the 190 discovered routes in the client bundle, the following RESTful API surface is required:

### Authentication & Session Subsystem
- `POST /api/auth/login`
  - **Purpose:** Authenticate staff credentials.
  - **Payload:** `{ username: "...", password: "..." }`
  - **Response:** `{ token: "...", refresh_token: "...", expires_in: 300, user: { ... } }`
  - **Validation & Rules:** Rate-limited; verifies `is_active=true`, `is_blocked=false`, and temporal window `open_time <= now <= close_time`.
- `POST /api/auth/logout`
  - **Purpose:** Invalidate current session token.
- `GET /api/auth/user`
  - **Purpose:** Return current authenticated user profile, permissions, and assigned limits.
- `POST /api/auth/refresh`
  - **Purpose:** Issue fresh JWT against valid refresh token.

### Customers & Membership Subsystem
- `GET /customers`
  - **Purpose:** Query paginated customer directory with filtering.
  - **Query Params:** `type` (individual, group, joint, institution), `branch_id`, `search`, `page`, `per_page`.
- `POST /customers/individuals`
  - **Purpose:** Submit new individual member application.
  - **Payload:** Multipart/form-data containing all Bio Data, Contact, Kin, Employment, Bank details, and uploaded binary files (signature, ID, photo).
  - **Response:** 201 Created `{ id: 1223, member_number: "00350000519", status: "PENDING" }`
- `POST /customers/groups`
  - **Purpose:** Register group member.
  - **Payload:** `{ branch_id, group_name, group_type, registration_number, entry_date, savings_officer_id, phone, address, ... }`
- `POST /customers/joints`
  - **Purpose:** Register joint account entity.
- `POST /customers/institutions`
  - **Purpose:** Register corporate/institutional entity.
  - **Payload:** `{ institution_name, registration_number, registration_date, tin_number, business_type, institution_category, ... }`
- `GET /customers/applications`
  - **Purpose:** Fetch maker-checker queue for member applications.
  - **Query Params:** `status=pending` or `status=rejected`, `branch_id`, `page`.
- `POST /customers/applications/{id}/approve`
  - **Purpose:** Approve pending member application and activate ledgers.
- `POST /customers/applications/{id}/reject`
  - **Purpose:** Reject pending member application with reason notes.
- `GET /customers/agent-applications`
  - **Purpose:** Fetch agency banking applications queue.

### Users & Security Subsystem
- `GET /users`
  - **Purpose:** Fetch list of system users.
  - **Query Params:** `status` (all, active, inactive), `search`, `page`.
- `POST /users`
  - **Purpose:** Provision new user account with transaction limits and notification subscriptions.
  - **Payload:** `{ first_name, last_name, username, email, password, phone, role_id, branch_id, station_id, limits: { ... }, notification_subscriptions: [ ... ] }`
- `PATCH /users/{id}`
  - **Purpose:** Update user details, limits, or assign new role.
- `POST /users/{id}/deactivate`
  - **Purpose:** Deactivate active user.
- `DELETE /users/{id}`
  - **Purpose:** Soft-delete user account.
- `POST /users/{id}/toggle-sms-otp`
  - **Purpose:** Enable or disable 2FA requirement.

### Roles & RBAC Subsystem
- `GET /users/roles`
  - **Purpose:** List all role profiles with assigned user counts and operating hours.
  - **Query Params:** `status` (all, active, inactive).
- `POST /users/roles`
  - **Purpose:** Create new role profile.
  - **Payload:** `{ role_name: "...", open_time: "00:00:00", close_time: "23:00:00", permissions: [ "register deposit", ... ] }`
- `GET /users/roles/{id}`
  - **Purpose:** Retrieve role definition and complete array of assigned permissions.
- `PATCH /users/roles/{id}`
  - **Purpose:** Update role permissions or operating window.
- `POST /users/roles/{id}/activate` / `POST /users/roles/{id}/deactivate`
  - **Purpose:** Toggle operational availability of role.
- `DELETE /users/roles/{id}`
  - **Purpose:** Delete unassigned role.

### Notifications & Float Subsystem
- `GET /notifications`
  - **Purpose:** Fetch current user's unread notifications.
- `POST /notifications/{id}/mark-read`
  - **Purpose:** Mark notification as read.
- `POST /notifications/mark-all-read`
  - **Purpose:** Mark all unread notifications as read.
- `GET /settings/float-balances`
  - **Purpose:** Fetch latest float balances for top-bar indicators (MSACCO, MTN, Airtel, Bank Transfer).


### 15.5. Payroll & Human Resources Subsystem Endpoints

| HTTP Method | Endpoint URI | Description | Auth Required | Permissions Required |
|:---|:---|:---|:---:|:---|
| `GET` | `/payroll/staff` | Lists all staff members with pagination, branch filter, and status | Yes | `read_staff` |
| `POST` | `/payroll/staff` | Registers a new staff profile, bio-data, next of kin, and attachments | Yes | `create_staff` |
| `GET` | `/payroll/staff/{id}` | Retrieves full staff profile, salary, allowances, and deductions | Yes | `read_staff` |
| `PUT` | `/payroll/staff/{id}` | Updates staff bio-data, salary, tax toggles, and payment method | Yes | `update_staff` |
| `POST` | `/payroll/staff/{id}/allowances` | Assigns recurring allowance to an employee | Yes | `manage_payroll_allowances` |
| `DELETE` | `/payroll/staff/{id}/allowances/{allowanceId}` | Removes allowance assignment from employee | Yes | `manage_payroll_allowances` |
| `POST` | `/payroll/staff/{id}/deductions` | Assigns voluntary deduction to an employee | Yes | `manage_payroll_deductions` |
| `DELETE` | `/payroll/staff/{id}/deductions/{deductionId}` | Removes deduction assignment from employee | Yes | `manage_payroll_deductions` |
| `GET` | `/payroll/payroll-list` | Calculates and previews monthly gross-to-net payroll summary | Yes | `view_payroll` |
| `POST` | `/payroll/salaries/salary-disbursement-requests` | Submits calculated payroll run for checker authorization | Yes | `request_salary_payment` |
| `GET` | `/payroll/salaries/salary-disbursement-requests` | Lists pending salary disbursement requests | Yes | `approve_salary_payment` |
| `POST` | `/payroll/salaries/salaries-disbursed` | Authorizes salary batch and executes direct savings credit | Yes | `approve_salary_payment` |
| `GET` | `/payroll/salaries/salaries-disbursed` | Retrieves historical register of disbursed salary batches | Yes | `view_payroll` |
| `GET` | `/payroll/advances` | Lists staff salary advances filtered by state (Active, Pending, etc.) | Yes | `read_salary_advances` |
| `POST` | `/payroll/advances` | Submits application for staff salary advance | Yes | `create_salary_advance` |
| `POST` | `/payroll/advances/{id}/approve` | Approves and disburses staff salary advance | Yes | `approve_salary_advance` |
| `POST` | `/payroll/advances/{id}/reject` | Rejects staff salary advance application | Yes | `approve_salary_advance` |
| `POST` | `/payroll/advances/{id}/reverse` | Reverses an active salary advance with audit remarks | Yes | `reverse_salary_advance` |

### 15.6. Institutional & Product Settings Endpoints

| HTTP Method | Endpoint URI | Description | Auth Required | Permissions Required |
|:---|:---|:---|:---:|:---|
| `GET` | `/settings/institution` | Retrieves top-level SACCO institutional profile and branding | Yes | `read_settings` |
| `PUT` | `/settings/institution` | Updates institution details, contact addresses, logo, theme color | Yes | `manage_institution_settings` |
| `GET` | `/settings/loans/products` | Lists configured loan products with interest and penalty terms | Yes | `read_loan_products` |
| `POST` | `/settings/loans/products` | Authors new loan product master with full 30+ GL account mappings | Yes | `create_loan_product` |
| `GET` | `/settings/loans/products/{id}` | Retrieves loan product details and GL mappings | Yes | `read_loan_products` |
| `PUT` | `/settings/loans/products/{id}` | Updates loan product rules, interest rates, and GL mappings | Yes | `update_loan_product` |
| `GET` | `/settings/loans` | Retrieves global loan operational policies (timing, reminders, sweep) | Yes | `read_loan_settings` |
| `PUT` | `/settings/loans` | Updates global loan operational policies and guarantor constraints | Yes | `manage_loan_settings` |
| `GET` | `/settings/loans/loss-provisions` | Retrieves BOU / UMRA loan loss provisioning percentages by aging bracket | Yes | `read_loan_settings` |
| `PUT` | `/settings/loans/loss-provisions` | Updates regulatory loan loss provisioning rates | Yes | `manage_loan_settings` |
| `GET` | `/settings/loans/holidays` | Lists loan repayment holiday moratorium windows | Yes | `read_loan_settings` |
| `POST` | `/settings/loans/holidays` | Adds new holiday moratorium date | Yes | `manage_loan_settings` |
| `GET` | `/settings/loans/crb` | Retrieves Credit Reference Bureau API credentials and SFTP settings | Yes | `manage_crb_settings` |
| `PUT` | `/settings/loans/crb` | Updates and verifies CRB API and SFTP credentials | Yes | `manage_crb_settings` |
| `GET` | `/settings/savings/products` | Lists all configured savings and deposit products | Yes | `read_savings_products` |
| `POST` | `/settings/savings/products` | Creates new savings product with omnichannel clearing accounts | Yes | `create_savings_product` |
| `GET` | `/settings/savings/products/{id}` | Retrieves savings product configuration and channel limits | Yes | `read_savings_products` |
| `PUT` | `/settings/savings/products/{id}` | Updates savings product parameters and ledger fee schedule | Yes | `update_savings_product` |
| `GET` | `/settings/savings` | Retrieves global savings policies (overdrafts, counter SMS, till bounds) | Yes | `read_savings_settings` |
| `PUT` | `/settings/savings` | Updates global savings policies | Yes | `manage_savings_settings` |
| `GET` | `/settings/savings/withdraw-charges` | Retrieves tiered cash withdrawal charge matrix | Yes | `read_savings_settings` |
| `PUT` | `/settings/savings/withdraw-charges` | Updates tiered cash withdrawal charge brackets | Yes | `manage_savings_settings` |
| `GET` | `/settings/fixed-deposits` | Lists configured fixed deposit term durations and interest profiles | Yes | `read_savings_settings` |
| `POST` | `/settings/fixed-deposits` | Creates new fixed deposit product tier | Yes | `manage_savings_settings` |
| `GET` | `/settings/pmt` | Retrieves Social Performance Management / PMT indicators | Yes | `read_pmt_settings` |
| `PUT` | `/settings/pmt` | Updates PMT macroeconomic indicators and GL mappings | Yes | `manage_pmt_settings` |

---
## 16. Data / Database Requirements

### Conceptual Data Model
The database architecture must be a relational database (PostgreSQL recommended) enforcing ACID compliance and double-entry ledger immutability.

```sql
-- Core Branches Table
CREATE TABLE branches (
    id SERIAL PRIMARY KEY,
    branch_code VARCHAR(10) UNIQUE NOT NULL, -- e.g. '0035'
    name VARCHAR(100) NOT NULL, -- e.g. 'KABALE'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Core Roles Table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    open_time TIME NOT NULL DEFAULT '00:00:00',
    close_time TIME NOT NULL DEFAULT '23:59:59',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Role Permissions Table
CREATE TABLE role_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_key VARCHAR(100) NOT NULL,
    UNIQUE(role_id, permission_key)
);

-- Core Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    role_id INTEGER NOT NULL REFERENCES roles(id),
    station_id VARCHAR(50), -- e.g. 'Teller - 1'
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    sms_otp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    
    -- Maker-Checker Financial Limits
    loan_approval_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    loan_approval_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- User Notification Subscriptions Table
CREATE TABLE user_notification_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_key VARCHAR(100) NOT NULL,
    UNIQUE(user_id, event_key)
);

-- Master Customers Table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    member_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '00350000001'
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('INDIVIDUAL', 'GROUP', 'JOINT', 'INSTITUTION')),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    savings_officer_id BIGINT REFERENCES users(id),
    entry_date DATE NOT NULL,
    phone_primary VARCHAR(30) NOT NULL,
    phone_secondary VARCHAR(30),
    email VARCHAR(150),
    country VARCHAR(100) NOT NULL DEFAULT 'Uganda',
    town VARCHAR(100),
    physical_address TEXT NOT NULL,
    postal_code VARCHAR(30),
    postal_address VARCHAR(150),
    has_loan_limit BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'REJECTED', 'INACTIVE', 'DORMANT')),
    created_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Individual Profile Table
CREATE TABLE customer_individuals (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    salutation VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female')),
    marital_status VARCHAR(20) NOT NULL,
    is_dependent BOOLEAN NOT NULL DEFAULT FALSE,
    is_pwd BOOLEAN NOT NULL DEFAULT FALSE,
    date_of_birth DATE NOT NULL,
    country_of_birth VARCHAR(100) NOT NULL,
    nationality VARCHAR(100) NOT NULL DEFAULT 'Ugandan',
    home_ownership VARCHAR(20) NOT NULL CHECK (home_ownership IN ('Owner', 'Tenant', 'Other')),
    identification_type VARCHAR(50) NOT NULL,
    identification_number VARCHAR(100) NOT NULL,
    card_number VARCHAR(100),
    signature_file_path TEXT,
    identification_file_path TEXT,
    photo_file_path TEXT
);

-- Next of Kin Table
CREATE TABLE customer_next_of_kin (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    second_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    physical_address TEXT NOT NULL,
    relationship VARCHAR(30) NOT NULL
);

-- Group Profile Table
CREATE TABLE customer_groups (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    group_name VARCHAR(150) NOT NULL,
    group_type VARCHAR(50) NOT NULL CHECK (group_type IN ('Members', 'Non Members')),
    registration_number VARCHAR(100) NOT NULL
);

-- Joint Profile Table
CREATE TABLE customer_joints (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    joint_name VARCHAR(150) NOT NULL
);

-- Institution Profile Table
CREATE TABLE customer_institutions (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    institution_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100) NOT NULL,
    registration_date DATE NOT NULL,
    tin_number VARCHAR(50) NOT NULL,
    business_type VARCHAR(100) NOT NULL,
    institution_category VARCHAR(100) NOT NULL,
    residence_ownership VARCHAR(50) NOT NULL
);
```


-- ============================================================================
-- 14. Conceptual Database Schema Extensions (Batch 4: Payroll & Product Settings)
-- ============================================================================

-- Staff Master Profile
CREATE TABLE staff_profiles (
    id BIGSERIAL PRIMARY KEY,
    staff_code VARCHAR(30) UNIQUE NOT NULL,
    branch_id BIGINT NOT NULL REFERENCES branches(id),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    other_names VARCHAR(50),
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female')),
    marital_status VARCHAR(20) NOT NULL CHECK (marital_status IN ('Single', 'Married', 'Divorced', 'Widowed')),
    identification_type VARCHAR(30) NOT NULL CHECK (identification_type IN ('National ID', 'Passport', 'Driving Permit')),
    identification_number VARCHAR(50) UNIQUE NOT NULL,
    tax_identification_number VARCHAR(30),
    social_security_number VARCHAR(30),
    physical_address TEXT NOT NULL,
    position VARCHAR(100) NOT NULL,
    date_appointed DATE NOT NULL,
    basic_salary NUMERIC(15,2) NOT NULL CHECK (basic_salary > 0),
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('savings', 'bank', 'mobile_money')),
    savings_account_id BIGINT REFERENCES savings_accounts(id),
    external_bank_name VARCHAR(100),
    external_bank_account VARCHAR(50),
    paye_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ssf_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    lst_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    employment_contract_url TEXT,
    signature_url TEXT,
    identification_document_url TEXT,
    photo_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'TERMINATED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Staff Next of Kin
CREATE TABLE staff_next_of_kin (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff_profiles(id) ON DELETE CASCADE,
    full_name VARCHAR(100) NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    home_address TEXT NOT NULL,
    relationship VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Allowance Types Master
CREATE TABLE payroll_allowance_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_taxable BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expense_account_id BIGINT NOT NULL REFERENCES chart_of_accounts(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Staff Recurring Allowances Mapping
CREATE TABLE staff_allowance_assignments (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff_profiles(id) ON DELETE CASCADE,
    allowance_type_id BIGINT NOT NULL REFERENCES payroll_allowance_types(id),
    amount_type VARCHAR(15) NOT NULL CHECK (amount_type IN ('FIXED', 'PERCENTAGE')),
    amount NUMERIC(15,2) NOT NULL CHECK (amount >= 0),
    percentage NUMERIC(5,2) CHECK (percentage BETWEEN 0 AND 100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Deduction Types Master (Statutory & Voluntary)
CREATE TABLE payroll_deduction_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(20) NOT NULL CHECK (category IN ('STATUTORY', 'VOLUNTARY', 'LOAN_RECOVERY')),
    description TEXT,
    liability_account_id BIGINT NOT NULL REFERENCES chart_of_accounts(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Staff Recurring Deductions Mapping
CREATE TABLE staff_deduction_assignments (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff_profiles(id) ON DELETE CASCADE,
    deduction_type_id BIGINT NOT NULL REFERENCES payroll_deduction_types(id),
    amount_type VARCHAR(15) NOT NULL CHECK (amount_type IN ('FIXED', 'PERCENTAGE')),
    amount NUMERIC(15,2) NOT NULL CHECK (amount >= 0),
    percentage NUMERIC(5,2) CHECK (percentage BETWEEN 0 AND 100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Monthly Payroll Run Master
CREATE TABLE payroll_runs (
    id BIGSERIAL PRIMARY KEY,
    run_reference VARCHAR(50) UNIQUE NOT NULL,
    period_month INT NOT NULL CHECK (period_month BETWEEN 1 AND 12),
    period_year INT NOT NULL CHECK (period_year >= 2020),
    execution_date DATE NOT NULL,
    total_basic_salary NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_allowances NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_gross_salary NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_paye_tax NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_employee_nssf NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_employer_nssf NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_lst NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_advance_deductions NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_other_deductions NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_net_salary NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    funding_account_id BIGINT REFERENCES chart_of_accounts(id),
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'CALCULATED', 'PENDING_APPROVAL', 'APPROVED', 'DISBURSED', 'REJECTED')),
    requested_by BIGINT NOT NULL REFERENCES users(id),
    requested_at TIMESTAMP WITH TIME ZONE,
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    disbursed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Monthly Payroll Run Line Items
CREATE TABLE payroll_run_line_items (
    id BIGSERIAL PRIMARY KEY,
    payroll_run_id BIGINT NOT NULL REFERENCES payroll_runs(id) ON DELETE CASCADE,
    staff_id BIGINT NOT NULL REFERENCES staff_profiles(id),
    basic_salary NUMERIC(15,2) NOT NULL,
    allowances_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    gross_salary NUMERIC(15,2) NOT NULL,
    paye_tax NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    employee_nssf NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    employer_nssf NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    lst_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    salary_advance_deduction NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    other_deductions_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    total_deductions NUMERIC(15,2) NOT NULL,
    net_pay NUMERIC(15,2) NOT NULL CHECK (net_pay >= 0),
    payment_method VARCHAR(20) NOT NULL,
    savings_account_id BIGINT REFERENCES savings_accounts(id),
    disbursement_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (disbursement_status IN ('PENDING', 'CREDITED', 'FAILED')),
    transaction_voucher_no VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Staff Salary Advances
CREATE TABLE salary_advances (
    id BIGSERIAL PRIMARY KEY,
    advance_number VARCHAR(50) UNIQUE NOT NULL,
    staff_id BIGINT NOT NULL REFERENCES staff_profiles(id),
    requested_amount NUMERIC(15,2) NOT NULL CHECK (requested_amount > 0),
    monthly_deduction_amount NUMERIC(15,2) NOT NULL CHECK (monthly_deduction_amount > 0),
    disbursement_date DATE NOT NULL,
    cleared_date DATE,
    total_repaid_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    outstanding_balance NUMERIC(15,2) NOT NULL,
    disbursement_method VARCHAR(20) NOT NULL CHECK (disbursement_method IN ('savings', 'cash_desk')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'CLEARED', 'REJECTED', 'REVERSED')),
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Institution Profile Settings
CREATE TABLE institution_profiles (
    id BIGSERIAL PRIMARY KEY,
    institution_name VARCHAR(150) NOT NULL,
    alias_name VARCHAR(50) NOT NULL,
    institution_type VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(25) NOT NULL,
    physical_address TEXT NOT NULL,
    registration_number VARCHAR(50) NOT NULL,
    head_office_branch_id BIGINT REFERENCES branches(id),
    currency_code VARCHAR(10) NOT NULL DEFAULT 'UGX',
    theme_color VARCHAR(20) DEFAULT '#0d6efd',
    logo_url TEXT,
    updated_by BIGINT REFERENCES users(id),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Products Master Extended
CREATE TABLE loan_product_masters (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    chart_account_principal_id BIGINT NOT NULL REFERENCES chart_of_accounts(id),
    currency_code VARCHAR(10) NOT NULL DEFAULT 'UGX',
    min_amount NUMERIC(15,2) NOT NULL CHECK (min_amount > 0),
    max_amount NUMERIC(15,2) NOT NULL CHECK (max_amount >= min_amount),
    interest_method VARCHAR(30) NOT NULL CHECK (interest_method IN ('FLAT', 'DECLINING_BALANCE', 'AMORTIZE')),
    interest_basis VARCHAR(10) NOT NULL DEFAULT '365' CHECK (interest_basis IN ('360', '365')),
    annual_interest_rate NUMERIC(5,2) NOT NULL CHECK (annual_interest_rate >= 0),
    annual_penalty_rate NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    arrears_grace_days INT NOT NULL DEFAULT 0,
    annual_penalty_rate_after_expiry NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    approvals_required_count INT NOT NULL DEFAULT 1,
    repayment_frequency VARCHAR(20) NOT NULL CHECK (repayment_frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ANNUALLY')),
    max_installments INT NOT NULL CHECK (max_installments > 0),
    compulsory_guarantors_count INT NOT NULL DEFAULT 0,
    compulsory_collateral_count INT NOT NULL DEFAULT 0,
    borrower_compulsory_savings_percent NUMERIC(5,2) DEFAULT 0.00,
    borrower_compulsory_shares_percent NUMERIC(5,2) DEFAULT 0.00,
    require_crb_check BOOLEAN NOT NULL DEFAULT FALSE,
    allow_multiple_running_loans BOOLEAN NOT NULL DEFAULT FALSE,
    enable_prepayments BOOLEAN NOT NULL DEFAULT TRUE,
    allow_daily_interest BOOLEAN NOT NULL DEFAULT FALSE,
    enable_auto_recovery_from_savings BOOLEAN NOT NULL DEFAULT TRUE,
    enable_interest_compute_after_expiry BOOLEAN NOT NULL DEFAULT FALSE,
    allow_msacco_applications BOOLEAN NOT NULL DEFAULT FALSE,
    allow_disbursal_to_phone BOOLEAN NOT NULL DEFAULT FALSE,
    -- GL Accounts Mapping
    interest_income_account_id BIGINT NOT NULL REFERENCES chart_of_accounts(id),
    auto_penalty_income_account_id BIGINT REFERENCES chart_of_accounts(id),
    manual_penalty_income_account_id BIGINT REFERENCES chart_of_accounts(id),
    interest_receivable_account_id BIGINT REFERENCES chart_of_accounts(id),
    penalty_receivable_account_id BIGINT REFERENCES chart_of_accounts(id),
    write_off_account_id BIGINT REFERENCES chart_of_accounts(id),
    write_off_recovery_account_id BIGINT REFERENCES chart_of_accounts(id),
    suspended_interest_account_id BIGINT REFERENCES chart_of_accounts(id),
    loan_loss_expense_account_id BIGINT REFERENCES chart_of_accounts(id),
    loan_loss_reserve_account_id BIGINT REFERENCES chart_of_accounts(id),
    admin_fee_income_account_id BIGINT REFERENCES chart_of_accounts(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Regulatory Loan Loss Provision Brackets (BOU / UMRA)
CREATE TABLE loan_loss_provision_brackets (
    id BIGSERIAL PRIMARY KEY,
    classification_name VARCHAR(50) NOT NULL,
    min_days_past_due INT NOT NULL,
    max_days_past_due INT, -- NULL for Loss (365+)
    provision_percentage NUMERIC(5,2) NOT NULL CHECK (provision_percentage BETWEEN 0 AND 100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Holidays / Moratoria Calendar
CREATE TABLE loan_holidays (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL CHECK (to_date >= from_date),
    description TEXT,
    applies_to_all_products BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Credit Reference Bureau Gateway Configuration
CREATE TABLE crb_configurations (
    id BIGSERIAL PRIMARY KEY,
    identification_code VARCHAR(50) NOT NULL,
    enquiries_username VARCHAR(100) NOT NULL,
    enquiries_password_encrypted TEXT NOT NULL,
    sftp_username VARCHAR(100) NOT NULL,
    sftp_password_encrypted TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Savings Products Master Extended
CREATE TABLE savings_product_masters (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    chart_account_id BIGINT NOT NULL REFERENCES chart_of_accounts(id),
    currency_code VARCHAR(10) NOT NULL DEFAULT 'UGX',
    is_group_product BOOLEAN NOT NULL DEFAULT FALSE,
    opening_balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    minimum_balance NUMERIC(15,2) NOT NULL DEFAULT 5000.00,
    minimum_award_balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    transfer_charge NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    dormancy_period_days INT NOT NULL DEFAULT 365,
    allow_overdrafts BOOLEAN NOT NULL DEFAULT FALSE,
    auto_loan_deduction BOOLEAN NOT NULL DEFAULT TRUE,
    consider_withdraw_range_charges BOOLEAN NOT NULL DEFAULT TRUE,
    ledger_fee_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    ledger_fee_account_id BIGINT REFERENCES chart_of_accounts(id),
    annual_interest_rate NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    interest_expense_account_id BIGINT REFERENCES chart_of_accounts(id),
    -- Multi-channel clearing GL accounts
    msacco_deposit_account_id BIGINT REFERENCES chart_of_accounts(id),
    msacco_withdrawal_account_id BIGINT REFERENCES chart_of_accounts(id),
    mtn_msacco_account_id BIGINT REFERENCES chart_of_accounts(id),
    airtel_msacco_account_id BIGINT REFERENCES chart_of_accounts(id),
    interswitch_atm_account_id BIGINT REFERENCES chart_of_accounts(id),
    daily_withdrawal_amount_limit NUMERIC(15,2),
    monthly_withdrawal_amount_limit NUMERIC(15,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tiered Cash Withdrawal Charges
CREATE TABLE savings_withdrawal_charge_tiers (
    id BIGSERIAL PRIMARY KEY,
    savings_product_id BIGINT REFERENCES savings_product_masters(id) ON DELETE CASCADE,
    tier_order INT NOT NULL,
    min_amount NUMERIC(15,2) NOT NULL CHECK (min_amount >= 0),
    max_amount NUMERIC(15,2) NOT NULL CHECK (max_amount > min_amount),
    charge_amount NUMERIC(15,2) NOT NULL CHECK (charge_amount >= 0),
    is_percentage BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Fixed Deposit Product Tiers
CREATE TABLE fixed_deposit_product_tiers (
    id BIGSERIAL PRIMARY KEY,
    duration_months INT NOT NULL CHECK (duration_months > 0),
    recycle_type VARCHAR(30) NOT NULL CHECK (recycle_type IN ('PRINCIPAL_ONLY', 'PRINCIPAL_PLUS_INTEREST', 'DO_NOT_RENEW')),
    annual_interest_rate NUMERIC(5,2) NOT NULL CHECK (annual_interest_rate >= 0),
    interest_award_frequency VARCHAR(20) NOT NULL DEFAULT 'MATURITY',
    interest_accrual_frequency VARCHAR(20) NOT NULL DEFAULT 'MONTHLY',
    allow_daily_interest BOOLEAN NOT NULL DEFAULT TRUE,
    liability_account_id BIGINT NOT NULL REFERENCES chart_of_accounts(id),
    interest_expense_account_id BIGINT NOT NULL REFERENCES chart_of_accounts(id),
    interest_payable_account_id BIGINT NOT NULL REFERENCES chart_of_accounts(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Social Performance Management / PMT Configuration
CREATE TABLE pmt_configurations (
    id BIGSERIAL PRIMARY KEY,
    regular_savings_product_id BIGINT REFERENCES savings_product_masters(id),
    gni_per_capita NUMERIC(15,2) NOT NULL,
    market_interest_rate NUMERIC(5,2) NOT NULL,
    subsidised_borrowed_funds_rate NUMERIC(5,2) NOT NULL,
    inflation_rate NUMERIC(5,2) NOT NULL,
    poverty_rate NUMERIC(5,2) NOT NULL,
    updated_by BIGINT REFERENCES users(id),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

---
## 17. Search, Filtering and Reporting

### Search Capabilities
- Master member search indexing across: Member Number, Name, Phone Number, and Group Affiliation.
- User search indexing: Username, First Name, Last Name, and Branch.

### Filtering Dimensions
- Status Filters: `All`, `Active`, `Inactive`, `Pending`, `Rejected`.
- Organizational Filters: Branch (`0035 KABALE`), Officer assignment, Customer Subtype.
- Date Range Filters: Registration date, approval date, transaction value date.

### Regulatory & Management Reports Catalog
The system specifies 109 discrete reporting capabilities including:
1. **Central Bank / BOU Returns:** Bank of Uganda Liquidity Return (`bou liquidity`), Bank of Uganda Banking Supervision Return (`bou bsa`).
2. **UMRA Returns:** Uganda Microfinance Regulatory Authority Balance Sheet (`umra balance sheet`), Income Statement (`umra income statement`), and PAR by Aging (`umra par by ageing`).
3. **Credit Operations Reports:** Loan Arrears vs Savings, Portfolio Monitoring, Cleared Loans, Due Loans, Written-off Loans, CRB Registers.
4. **General Ledger & Accounting:** Balance Sheet, Profit & Loss / Income Statement, Cash Flow Statement, Trial Balance, Backdated Transactions, Till Sheet Reconciliation.
5. **Shares & Equity:** Cumulative Shares, Shares Ledger, Dividends Shared Report.

---
""")

    # 18. File and Document Requirements
    sections.append("""## 18. File and Document Requirements

### Upload Specifications
1. **Signature File:** Uploaded during individual registration (`Signature*`). Stored securely; rendered on teller screens for withdrawal verification.
2. **Identification Document Scan:** Uploaded ID card/passport scan (`Indentification*`).
3. **Profile Photo:** Member portrait photograph (`Photo*`).
4. **Bulk Processing Files:** Bulk payroll/disbursement files (`.csv`, `.xlsx`) uploaded for batch processing.
5. **Database Backups:** Compressed database backup files downloadable by administrators (`download backup`, `restore backup`).

---
""")

    # 19. Notification and Event Requirements
    sections.append("""## 19. Notification and Event Requirements

### The 22 Core Notification Triggers
The backend event dispatcher must route notifications based on user subscriptions:
1. `blocked_user`: Dispatched when an account is locked out after consecutive failed authentications.
2. `bulk_files_pending_approval`: Dispatched when an institutional disbursement file is uploaded.
3. `customer_applications_pending_approval`: Dispatched when a member registration form is submitted.
4. `customer_kyc_updated`: Dispatched when member biodata or identity documents are altered.
5. `failed_job`: Dispatched immediately when an asynchronous background job errors out.
6. `fixed_deposit_applications_pending_approval`: Dispatched on term deposit placement.
7. `license_expiry_reminder`: Dispatched when software license term nears expiration.
8. `license_upgraded`: Dispatched when license tier or capacity is expanded.
9. `liquid_assets_balance_running_low`: Dispatched when branch cash or float balances breach minimum reserve.
10. `loan_applications_pending_approval`: Dispatched when a loan officer submits an application.
11. `loan_applications_pending_disbursement`: Dispatched when an approved loan is ready for payout.
12. `msacco_customer_onboarding`: Dispatched when a member self-registers via mobile USSD/App.
13. `msacco_loan_application`: Dispatched on mobile emergency loan request.
14. `opening_a_closed_period`: High-priority alert triggered if an accounting period is unsealed.
15. `reserve_amount_limit`: Dispatched when statutory reserves breach threshold.
16. `savings_account_applications_pending_approval`: Dispatched on savings account opening.
17. `standing_orders_pending_approval`: Dispatched when a recurring payment rule is established.
18. `tb_imbalance`: Critical financial alarm fired when Trial Balance debits != credits.
19. `transaction_mismatch`: Dispatched on settlement variance between CBS and mobile money/bank float.
20. `transactions_pending_approval`: Dispatched when a transaction breaches a teller's posting limit.
21. `upcoming_holidays`: Dispatched in advance of statutory holidays affecting loan schedules.
22. `updated_user_profile`: Dispatched when administrative permissions or limits change.


### 19.4. Payroll, Product & Risk Scheduled Daemons
1. **Monthly Payroll Sweep Daemon:** Runs at 23:59:00 on the configured monthly payroll cutoff date to execute approved salary disbursements, sweep net pay into staff savings accounts, deduct salary advance installments, and post statutory withholding tax journals.
2. **Regulatory Loan Loss Impairment Daemon:** Executes nightly at 01:00:00 to re-evaluate Days Past Due (DPD) for all running loans, re-classify portfolios into BOU aging brackets (Normal, Watch, Substandard, Doubtful, Loss), calculate required provisions, and post automated impairment adjustment vouchers.
3. **Daily Loan Due & Overdue Notification Service:** Evaluates running loans every morning at 07:00:00, identifying installments due within configured reminder days (e.g. 3 days prior) and dispatching personalized SMS payment reminders.
4. **Automated Monthly Ledger Fee Sweep Daemon:** Sweeps monthly ledger fees from all eligible active savings accounts on the first calendar day of the month, evaluating minimum balance constraints and logging arrears.
5. **Nightly CRB SFTP Synchronization Job:** Compiles active borrower repayment histories and non-performing loan records into standard CRB data exchange format (PIF / CIF) and uploads securely via SFTP to the bureau gateway at 02:00:00.

---
## 20. Audit and History Requirements

### Immutability & Audit Trails
- **Audit Fields:** Every database entity must include `created_at`, `updated_at`, `created_by`, and `approved_by`.
- **System Audit Trail (`view audit trail`):** Comprehensive record capturing timestamp, actor user ID, client IP address, action performed, entity affected, old state, and new state.
- **Financial Immutability:** No financial transaction may be hard-deleted or updated in-place. Corrections must occur strictly via reversing entries (`reverse journal`, `reverse withdrawal`, `reverse deposit`, `reverse share purchase`).
- **Backdated Transaction Audit (`view backdated transactions`):** Specialized tracking for any entry posted with a historical value date.

---
""")

    # 21. Error Handling Requirements
    sections.append("""## 21. Error Handling Requirements

### Standard Error Taxonomy
- `400 Bad Request`: Input validation failure (`ERR_VALIDATION_FAILED`), duplicate unique field (`ERR_DUPLICATE_NIN`, `ERR_DUPLICATE_USERNAME`).
- `401 Unauthorized`: Missing, expired, or invalid bearer token (`ERR_SESSION_EXPIRED`, `ERR_UNAUTHENTICATED`).
- `403 Forbidden`:
  - Temporal restriction: `ERR_OUTSIDE_OPERATING_HOURS`.
  - Permission restriction: `ERR_PERMISSION_DENIED`.
  - Transaction limit restriction: `ERR_EXCEEDS_USER_LIMIT`.
  - Maker-checker violation: `ERR_CANNOT_APPROVE_OWN_RECORD`.
- `404 Not Found`: Target entity not found (`ERR_MEMBER_NOT_FOUND`).
- `409 Conflict`: Invalid state transition (`ERR_INVALID_STATUS_TRANSITION`), period already closed (`ERR_PERIOD_CLOSED`), Trial Balance imbalance (`ERR_TB_IMBALANCE`).
- `500 Internal Server Error`: Asynchronous job failure (`ERR_JOB_FAILED`), unexpected exception.

---
""")

    # 22. Security Requirements
    sections.append("""## 22. Security Requirements

### Observed Security Requirements
1. **Operating Hours Access Control:** Strictly enforced login and execution window per role.
2. **Idle Session Invalidation:** 5-minute timeout.
3. **Transaction Boundary Protection:** User min/max amount caps.
4. **Segregation of Duties (SoD):** Maker-checker separation for applications and transactions.
5. **Database Restoration Mode:** Dedicated administrative lock state (`activate restoration mode`).

### Recommended Security Enhancements
1. **Password Policy:** Minimum 12 characters, complexity requirements, Argon2id or bcrypt hashing.
2. **Rate Limiting:** IP and user-based throttling on authentication endpoints.
3. **Transport Security:** Strict TLS 1.3 encryption on all endpoints.
4. **Data Redaction:** PII and financial balances masked in audit log outputs.

---
""")

    # 23. Non-Functional Requirements
    sections.append("""## 23. Non-Functional Requirements

### Performance & Scalability
- **API Latency:** 95% of OLTP requests (deposits, balance checks) must respond in `< 250ms`.
- **Concurrency:** Support concurrent branch tellers and async mobile money webhooks without locking conflicts.
- **Reporting:** Heavy analytical and regulatory reports must execute against read-replicas or materialized views to prevent OLTP lockups.

### Availability & Resilience
- **Target Availability:** 99.9% uptime during operational branch hours.
- **Audit Durability:** Audit trail logs retained for minimum 7-year regulatory compliance window.

---
""")

    # 24. Cross-Page Findings
    sections.append("""## 24. Cross-Page Findings

1. **Vendor & Platform Identity:** Consistent branding across all headers: *"Savings Plus, Property of FutureLink Technologies | @2021 All rights reserved"*. FutureLink Technologies (FLT) provides the core banking platform engine.
2. **Misnamed Artifact:** `user_details.html` does not render an individual staff member's detail card; instead, it renders the exact form for editing a Role Profile (*"Edit Sp Data Migration Training"*), identical to `edit_role.html`.
3. **Shared Layout & Persistent State:** All 17 HTML files share an identical top status bar, navigation bar, idle detector widget, and failed job notification banner, indicating an Angular/Nuxt Single-Page Application shell.
4. **Entity Linkage:** Data created in `create_individual_member.html` flows directly into `list_member_applications.html` as a pending application, and subsequently appears in `list_members.html` upon approval.
5. **Currency Consistency:** The default currency across all balances and reports is Ugandan Shillings (`UGX`), with multi-currency / forex support configured in `/settings/institution/forex`.

---
""")

    # 25. Open Questions
    sections.append("""## 25. Open Questions

The following implementation specifics cannot be resolved strictly from the static HTML DOM and require stakeholder clarification:

| # | Subsystem | Open Question | Backend Impact |
|---|---|---|---|
| 1 | Regulatory Compliance | Is automated direct integration required for CRB inquiries (e.g. Metropol / Compuscan) or is it manual file upload? | External HTTP Client implementation vs static file storage |
| 2 | Biometrics / NIN | Does NIN verification connect directly to the NIRA (National Identification and Registration Authority) API or is it internal verification? | Integration adapter for NIRA gateway |
| 3 | Mobile Money Float | Are float balances polled via Telco APIs (MTN MoMo API, Airtel Money API) or updated via webhook callbacks from the MSACCO switch? | Asynchronous webhook listener vs scheduled balance polling worker |
| 4 | Accounting Engine | What is the exact Chart of Accounts structure (account code digit length, parent-child depth)? | Schema design of `chart_of_accounts` table |
| 5 | Storage Engine | Should uploaded documents (photos, signatures, IDs) be stored in local file storage or S3-compatible object storage? | Blob storage abstraction layer |
| 6 | Soft-Delete Policy | What is the permanent retention or purging lifecycle for soft-deleted users and customers? | Database cleanup / archiving jobs |

---
""")

    # 26. Assumptions
    sections.append("""## 26. Assumptions

1. **Database Engine:** PostgreSQL 15+ is assumed as the primary relational database to support JSONB attributes, transactions, and robust decimal precision.
2. **Statutory Jurisdiction:** The system operates under Ugandan legal and financial regulatory jurisdiction (BOU, UMRA, URA).
3. **Currency:** Primary accounting currency is `UGX` (Ugandan Shilling) with decimal precision set to 2 decimal places.
4. **Timezone:** Standard operating timezone is East Africa Time (EAT, UTC+3).
5. **Maker-Checker Strictness:** Maker-checker is mandatory for all member on-boarding, loan approvals, and transactions exceeding user-specific limits.

---
""")

    # 27. Traceability Matrix
    sections.append("""## 27. Traceability Matrix

| HTML Source File | Feature / Subsystem | Entities Involved | Requirement IDs | Evidence & Traceability |
|---|---|---|---|---|
| `create_member_step_one.html` | Member Type Selection | Customer | FR-001 | Cards for Individuals, Group, Joint, Institution |
| `create_individual_member.html` | Individual Onboarding | Customer, CustomerIndividual, NextOfKin, Employment, BankDetails | FR-002 | Complete 8-section form, file uploads, branch selection |
| `create_group.html` | Group Onboarding | Customer, CustomerGroup | FR-004 | Group Name, Registration number, Members/Non-members |
| `create_joint.html` | Joint Onboarding | Customer, CustomerJoint | FR-005 | Joint Name, shared addresses, loan limit toggle |
| `create_institution.html` | Institution Onboarding | Customer, CustomerInstitution | FR-006 | TIN, Business Type, Category, Ownership dropdowns |
| `list_members.html` | Member Directory | Customer, Branch | FR-001, FR-002 | Master table displaying members with phone, age, group |
| `list_groups.html` | Group Directory Tab | CustomerGroup | FR-004 | Groups filter subtab under /customers |
| `list_joints.html` | Joint Directory Tab | CustomerJoint | FR-005 | Joints filter subtab under /customers |
| `list_institutions.html` | Institution Directory Tab | CustomerInstitution | FR-006 | Institutions filter subtab under /customers |
| `list_member_applications.html` | Member Vetting Queue | Customer | FR-002, FR-003 | Pending / Rejected tabs, view application triggers |
| `list_agency_applications.html` | Agency Vetting Queue | AgencyApplication | FR-014 | Agent application pending / rejected tabs |
| `create_user.html` | User Provisioning | User, UserNotificationSubscription | FR-007 | Staff form, 9 Min/Max limit pairs, 22 alert checkboxes |
| `list_users.html` | User Administration | User | FR-008 | User cards, Deactivate, Delete, Enable SMS OTP buttons |
| `create_role.html` | Role Definition | Role, RolePermission | FR-009 | Role name, Open/Close time, 382 permission checkboxes |
| `edit_role.html` | Role Modification | Role, RolePermission | FR-009 | Permission editing and operating hour adjustments |
| `user_details.html` | Role Profile View | Role, RolePermission | FR-009 | Edit role profile UI misnamed as user_details.html |
| `list_roles.html` | Role Administration | Role | FR-009, FR-010 | Role cards, active user counts, operating hours |
| Global Top Bar / Shell | Liquidity & Alerts | ExternalFloat, Notification, Session | FR-011, FR-012, FR-013 | Float counters, Failed job alert, Idle detector |


| `create_staff.html` | Staff Registration Form | StaffProfile, Document | FR-034, FR-035 | Bio-data, next of kin, salary, tax IDs (TIN, NSSF), branch, role, document uploads |
| `staff_list.html` | Staff Directory & Filter | StaffProfile | FR-034 | Lists active/inactive staff, search, filters, navigation to detail and edit |
| `payroll_staff_detail.html` | Staff Profile & Deductions | StaffProfile, Allowance, Deduction | FR-035, FR-036 | Views staff bio, salary, allowance assignments, other deductions, document uploads |
| `edit_payroll_staff.html` | Staff Edit Form | StaffProfile | FR-034, FR-035 | Updates staff bio-data, salary, tax toggles (PAYE, NSSF, LST), payment method |
| `payroll_list.html` | Monthly Payroll Summary | PayrollRun, LineItem | FR-037 | Displays gross pay, allowances, PAYE, NSSF, salary advance deductions, and net pay |
| `pay_salaries.html` | Salary Payment Authorization | SalaryDisbursementBatch | FR-038 | Maker-checker salary payment authorization, date selection, cash/bank GL account selection |
| `request_salary_payment.html` | Request Salary Disbursement | SalaryDisbursementBatch | FR-038 | Initiates salary payment batch request for checker approval |
| `salaries_disbursed.html` | Disbursed Salaries Register | SalaryDisbursementBatch | FR-038 | Historical register of approved and disbursed staff salaries with date filters |
| `salary_advances.html` | Salary Advances Register | SalaryAdvance | FR-039 | Lists staff advances across Active, Cleared, Pending, Rejected, Reversed states |
| `request_salary_advance.html` | Request Salary Advance | SalaryAdvance | FR-039 | Form to apply for salary advance: staff selector, amount, monthly deduction installment |
| `payroll_allowances.html` | Allowance Configuration | AllowanceType | FR-036 | Manages recurring and one-off allowance categories (Housing, Transport, Medical) |
| `payroll_deductions.html` | Statutory Deductions Setup | DeductionType | FR-036 | Configures PAYE tax brackets, NSSF rates (5% employee / 10% employer), and LST |
| `payroll_other_deductions.html` | Voluntary Deductions Setup | DeductionType | FR-036 | Configures custom staff deductions: staff SACCO shares, welfare funds, loans |
| `settings_page.html` | Master Settings Directory | SystemSetting | FR-040 | Central administration portal navigating to Institution, Loans, Savings, Payroll, Jobs |
| `institution_settings.html` | Institution Profile View | InstitutionProfile | FR-040 | Displays institution corporate profile, logo, branding, address, registration details |
| `edit_institution_settings.html` | Edit Institution Profile | InstitutionProfile | FR-040 | Configures SACCO name, short alias, email, phone, physical address, theme color, logo |
| `pmt_settings.html` | Proxy Means Test / SPM Setup | PMTConfiguration | FR-050 | Configures Social Performance Management, GNI per capita, poverty thresholds, MIX benchmarks |
| `list_loan_products.html` | Loan Products Directory | LoanProduct | FR-041 | Lists configured credit products, interest rates, status, actions to edit or add deductions |
| `create_product.html` | Loan Product Master Setup | LoanProduct, Charge | FR-041 | Comprehensive credit product authoring: interest method, penalty rate, grace days, 30+ GL accounts |
| `loan_settings.html` | Global Loan Risk Policies | LoanGlobalPolicy | FR-042 | Configures amortization frequency, arrears penalizing, due reminders, auto-pay sweep rules |
| `loan_loss_provisions.html` | Regulatory Loan Loss Setup | LoanLossProvision | FR-043 | Configures BOU / UMRA IFRS9 loan loss provisioning percentages by arrears aging bracket |
| `loan_holidays.html` | Loan Repayment Holidays | LoanHoliday | FR-044 | Defines moratorium dates and public holidays preventing installment defaults |
| `crb_settings.html` | Credit Bureau Gateway Setup | CRBConfiguration | FR-045 | Configures CRB credentials: Identification code, Enquiries user/pwd, SFTP credentials |
| `list_savings_products.html` | Savings Products Directory | SavingsProduct | FR-046 | Lists savings products, interest rules, minimum balances, withdrawal limits |
| `create_savings_product.html` | Savings Product Authoring | SavingsProduct, Charge | FR-046 | Defines opening/min balance, dormancy, ledger fees, ATM/MSACCO clearing GLs, velocity limits |
| `saving_settings.html` | Global Savings Policies | SavingsGlobalPolicy | FR-047 | Configures counter SMS, overdrafts, ledger fee schedule, auto-share deductions, till limits |
| `savings_withdraw_charges.html` | Tiered Withdrawal Charges | WithdrawalChargeTier | FR-048 | Defines bracketed cash withdrawal charge matrix (Min Amount, Max Amount, Fee) |
| `fixed_deposit_settings.html` | Fixed Deposit Tier Setup | FixedDepositProduct | FR-049 | Defines term deposit duration bands, interest rates, accrual/award frequencies, rollover rules |

---
## 28. Backend Implementation Considerations

1. **Transactional Ledger Integrity:** Implement strict double-entry ledger primitives. Every financial event (deposit, withdrawal, disbursement, fee) must post balanced debit and credit entries to the general ledger within an isolated database transaction.
2. **Middleware Pipeline:**
   - `AuthenticationMiddleware`: Decodes JWT, validates active session.
   - `OperatingHoursMiddleware`: Evaluates current timestamp against user's role `open_time` and `close_time`.
   - `PermissionGuardMiddleware`: Checks route action against user's assigned role permission set.
   - `FinancialLimitInterceptor`: Checks transaction amount against user's min/max threshold.
3. **Asynchronous Processing:** Use a distributed task queue (e.g. Celery / BullMQ / Redis) for background jobs:
   - Interest accruals on savings and fixed deposits.
   - Batch standing order executions.
   - SMS and email notification dispatching.
   - Job health monitoring with automatic dispatch of `failed_job` alerts.

---
""")

    # 29. Final System Summary
    sections.append("""## 29. Final System Summary

The reverse-engineering of the 17 provided HTML artifacts establishes that **SavingsPlus** is an enterprise Core Banking System (CBS) designed for **NSIMBI SACCO LTD** by **FutureLink Technologies (FLT)**. 

The backend architecture requires:
- **Comprehensive Customer Information File (CIF):** Supporting Individuals, Groups, Joints, and Institutions with maker-checker onboarding.
- **Granular Security Framework:** 34 modules, 382 permissions, strict operating time windows, 5-minute idle timeouts, and individual financial limit ceilings across 9 transaction classes.
- **Integrated Operations:** End-to-end management of Savings, Loans, Shares, Accounting, Fixed Assets, Staff Payroll, External Float (MTN, Airtel, MSACCO, Bank), and Regulatory Compliance (BOU & UMRA).

This specification provides the exhaustive, unambiguous foundation for backend developers and automated coding agents to implement the database models, business logic services, and REST API controllers for the NSIMBI Core Banking System.
""")

    full_text = "
".join(sections)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write(full_text)
    
    # Also write a copy to ~/Downloads/MSACCO/REQUIREMENTS.md for user convenience
    downloads_copy = os.path.expanduser("~/Downloads/MSACCO/REQUIREMENTS.md")
    with open(downloads_copy, "w", encoding="utf-8") as f:
        f.write(full_text)

    print(f"REQUIREMENTS.md generated successfully at:
 - {OUTPUT_FILE}
 - {downloads_copy}")
    print(f"Total lines: {len(full_text.splitlines())}")

if __name__ == "__main__":
    build_requirements()

### BR-007: Savings Dual-Balance & Minimum Reserve Protection
- **Rule:** A savings account must enforce a minimum balance reserve (observed default: `UGX 5,000.00`).
- **Formula:** `available_balance = actual_balance - minimum_reserve - active_holds`.
- **Enforcement:** No withdrawal or transfer may cause `available_balance < 0`.

### BR-008: Auto-Pay Priority on Savings
- **Rule:** If a loan has `auto_pay_from_savings=true`, the scheduled installment sweep has automatic debit priority against the linked savings account on the installment due date.

### BR-009: Loan Amortization Calculation Integrity
- **Rule:** Installments must be calculated strictly according to product specifications:
  - `flat`: `Installment = (Principal / N) + (Principal * (Rate / 100) / N)`.
  - `declining`: Periodic interest computed on remaining principal.
  - `amortize`: Fixed annuity installment formula `PMT(r, n, P)`.

### BR-010: Rescheduled Loan Lineage Tracking
- **Rule:** When a delinquent loan is restructured, the old loan is closed and a new loan is generated with a mandatory reference to the original loan number (`Rescheduled from LN00299`).

### BR-011: Fixed Deposit Early Liquidation Penalty
- **Rule:** If a fixed deposit is terminated prior to its maturity date, interest may be completely forfeited (`TERMINATED_NO_INTEREST`) unless explicitly authorized by a manager (`TERMINATED_WITH_INTEREST`).

---
""")

    # 11. Validation Rules
    s.append("""## 11. Validation Rules

### Confirmed Validations (Savings & Loans)
1. **Fixed Deposit Placement:**
   - `Amount*`: Must be strictly positive and `>= minimum_fixing_amount` defined on product.
   - `Fixing period in months`: Must be integer `>= 1`.
   - `Interest Rate (%)*`: Non-negative decimal.
   - `Starts on*`: Valid date.
2. **Loan Calculator & Origination:**
   - `Product*`: Valid active loan product.
   - `Amount*`: Within product min/max bounds.
   - `Annual interest rate*`: Within product rate limits.
   - `Number of installments`: Integer `>= 1`.
3. **Savings Account Actions:**
   - Cannot delete an account that has a non-zero balance (`actual_balance != 0`).
   - Cannot freeze an already frozen account.

---
""")

    # 12. Authentication Requirements
    s.append("""## 12. Authentication Requirements

- Standard session authentication via JWT (`/api/auth/login`).
- 5-minute inactivity lockdown strictly monitored by front-end and enforced by backend session expiry.
- SMS OTP two-factor authentication toggled per user account (`/users/{id}/toggle-sms-otp`).

---
""")

    # 13. Authorization Requirements
    s.append("""## 13. Authorization Requirements

Complete mapping across all 34 modules and 382 permissions confirmed in previous section, with explicit operational authorization required for:
- Savings: `register deposit`, `register withdrawal`, `freeze savings account`, `close savings account`, `delete savings account`, `register standing order`, `register fixed deposit`, `accrue fixed deposit`, `terminate fixed deposit`.
- Loans: `apply for loan`, `approve loan application`, `reject loan application`, `disburse loan`, `register repayment`, `register refund`, `register penalty waiver`, `register interest waiver`, `admin fees waiver`, `reschedule loan`, `send loan to legal`, `write off loan`, `unwrite off loan`.

---
""")

    # 14. Functional Requirements
    s.append("""## 14. Functional Requirements

### FR-015 — Savings Account Directory & Status Filtering
- **Requirement:** The system shall list all customer savings accounts with account number, member name, branch, product, opened date, actual balance, available balance, and status filters (`All`, `Active`, `Inactive`, `Frozen`).
- **Entity:** `SavingsAccount`
- **Actor:** Cashier / Customer Care / Manager
- **Source:** `list_savings.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated staff.
- **Input:** Filter criteria (Status, Branch, Search term).
- **Business Rules:** Account number formatted as 12 digits (`003500005281`).
- **Expected Result:** Paginated list of savings accounts.
- **Failure Conditions:** Unauthorized access.
- **Evidence Level:** Explicit

### FR-016 — Savings Account Detail & Cash Desk Terminal
- **Requirement:** The system shall provide an operational cash desk screen for an individual savings account displaying Actual Balance, Available Balance, KYC photo/signature preview, and immediate transaction buttons: Deposit, Withdraw, New Transfers, Debit Account, Credit Account, Close, and Freeze.
- **Entity:** `SavingsAccount`, `SavingsTransaction`
- **Actor:** Cashier / Teller
- **Source:** `saving_account_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active savings account.
- **Input:** Account Number (`003500005281`).
- **Business Rules:** Displays Available Balance (`actual_balance - 5000 UGX reserve`).
- **Expected Result:** Detailed account ledger and action triggers.
- **Failure Conditions:** Account does not exist.
- **Evidence Level:** Explicit

### FR-017 — Savings SMS Alert Rules Configuration
- **Requirement:** The system shall allow operators to configure granular SMS transaction and loan notification subscriptions per savings account across 14 discrete triggers.
- **Entity:** `SavingsSMSAlertConfig`
- **Actor:** Customer Care / Staff
- **Source:** `edit_saving_account.html`, `saving_account_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target savings account exists.
- **Input:** Boolean flags for: On Deposit, On Withdrawal, Earning Interest, Earning Dividends, Outgoing Transfer, Incoming Transfer, Direct Debit, Direct Credit, Standing Order, Loan Application, Loan Disbursement, Loan Payment, Loan Refund.
- **Business Rules:** Saved flags dictate which account activities dispatch transactional SMS messages via the SMS gateway.
- **Expected Result:** SMS alert configuration persisted.
- **Failure Conditions:** Invalid account ID.
- **Evidence Level:** Explicit

### FR-018 — Fixed Deposit Placement & Booking
- **Requirement:** The system shall book term/fixed deposits, specifying principal amount, tenure in months, annual interest rate, interest payment interval, maturity payout option, and funding source (Cash counter vs Savings account offset).
- **Entity:** `FixedDeposit`, `SavingsAccount`
- **Actor:** Customer Care / Branch Manager
- **Source:** `create_fixed_deposit.html`
- **Priority:** MUST HAVE
- **Preconditions:** Member possesses an active savings account.
- **Input:** Member, Transfer/Payout Account, Depositor Name, Depositor Phone, Principal Amount, Starts On, Period in Months, Interest Rate (%), Interest Interval (`monthly`, `yearly`, `Not applicable`), Payout Option (`Principal`, `Principal & Interest`), Funding Method (`Till - Cash or cheque`, `Offset - From savings`).
- **Business Rules:** If offset selected, funds immediately placed on hold/debited from source savings account. Enters Maker-Checker approval queue (`list_fixed_deposit_applications.html`).
- **Expected Result:** Fixed deposit contract created in `PENDING` status.
- **Failure Conditions:** Insufficient available balance in source savings account.
- **Evidence Level:** Explicit

### FR-019 — Fixed Deposit Lifecycle & Termination
- **Requirement:** The system shall track fixed deposits through their complete lifecycle and support both premature and maturity termination with interest calculation.
- **Entity:** `FixedDeposit`
- **Actor:** Branch Manager
- **Source:** `list_fixed_deposits.html`, `fixed_deposit_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Fixed deposit in `RUNNING` or `MATURED_NOT_AWARDED` status.
- **Input:** Deposit ID, Action (`TERMINATE`), Interest Decision (`WITH_INTEREST`, `NO_INTEREST`).
- **Business Rules:** Terminating returns principal (and approved interest) to the designated transfer savings account. Status transitions to `AWARDED_ON_MATURITY`, `TERMINATED_NO_INTEREST`, or `TERMINATED_WITH_INTEREST`.
- **Expected Result:** Funds transferred; contract archived.
- **Failure Conditions:** Contract already liquidated.
- **Evidence Level:** Explicit

### FR-020 — Standing Orders Recurring Execution
- **Requirement:** The system shall maintain standing orders with automated periodic execution and track statuses: `Pending`, `Approved`, `Running`, `Rejected`, `Expired`.
- **Entity:** `StandingOrder`
- **Actor:** Customer Care / Scheduler
- **Source:** `list_standing_orders.html`
- **Priority:** MUST HAVE
- **Preconditions:** Source savings account active.
- **Input:** Source Account, Destination Account, Amount, Frequency, Start Date.
- **Business Rules:** Background worker sweeps source account on execution date. If balance insufficient, records failed execution and dispatches alert `standing_orders_pending_approval`.
- **Expected Result:** Transfer posted between accounts.
- **Failure Conditions:** Expired order or insufficient balance.
- **Evidence Level:** Explicit

### FR-021 — Debit Card Issuance Tracking
- **Requirement:** The system shall manage the requisition, approval, and issuance of co-branded Interswitch debit cards linked to member savings accounts.
- **Entity:** `DebitCardIssuance`
- **Actor:** Customer Care / Branch Manager
- **Source:** `list_debit_card_issuance.html`, `saving_account_detail.html`
- **Priority:** SHOULD HAVE
- **Preconditions:** Active savings account.
- **Input:** Savings Account Number, Card PAN / Serial Number.
- **Business Rules:** Card status tracks through `Pending` -> `Approved` -> `Issued` -> `Rejected`.
- **Expected Result:** Card linked for ATM/POS debit transactions.
- **Failure Conditions:** Account frozen or inactive.
- **Evidence Level:** Explicit

### FR-022 — Loan Amortization Calculator
- **Requirement:** The system shall provide an interactive loan amortization calculator supporting multiple interest calculation methods (`flat`, `declining`, `amortize`) and frequencies (`daily`, `weekly`, `monthly`, `annually`), generating installment amounts and repayment schedules.
- **Entity:** `LoanProduct`, `LoanRepaymentSchedule`
- **Actor:** Loan Officer / Customer
- **Source:** `loan_calculator.html`
- **Priority:** MUST HAVE
- **Preconditions:** Loan products configured in system.
- **Input:** Product ID, Principal Amount, Interest Method, Annual Interest Rate, Frequency, Number of Installments, Start Date, Grace Period (Months).
- **Business Rules:** Generates accurate mathematical repayment schedule preview.
- **Expected Result:** Installment amount and period-by-period breakdown of Principal, Interest, and Remaining Balance.
- **Failure Conditions:** Negative principal or rate.
- **Evidence Level:** Explicit

### FR-023 — Credit Reference Bureau (CRB / CBA) Vetting
- **Requirement:** The system shall integrate credit reporting workflows allowing officers to record borrower consent and perform credit vetting against Credit Bureau Africa (CBA).
- **Entity:** `CreditReportingInquiry`
- **Actor:** Loan Officer
- **Source:** `list_credit_reporting.html`
- **Priority:** MUST HAVE
- **Preconditions:** Borrower signed CRB consent form (`initiate crb report consent`).
- **Input:** Member NIN / Account Number, Bureau Identifier (`CBA`).
- **Business Rules:** Credit check results logged in bureau register (`view crb register`) prior to loan approval.
- **Expected Result:** Credit history report and risk rating returned.
- **Failure Conditions:** Bureau service unavailable.
- **Evidence Level:** Explicit

### FR-024 — Loan Servicing, Auto-Pay & Repayments
- **Requirement:** The system shall manage active loan contracts, displaying disbursement date, maturity date, interest rate (e.g. `42.0%`), loan officer, and linked repayment account, supporting manual payment posting and automated savings sweeps.
- **Entity:** `Loan`, `SavingsAccount`, `LoanRepayment`
- **Actor:** Loan Officer / Cashier / System Scheduler
- **Source:** `loan_detail.html`, `list_loans.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active loan contract in `RUNNING` status.
- **Input:** Loan Number (`1D04FE1229`), Payment Amount, Payment Mode.
- **Business Rules:** If `auto_pay_from_savings=true`, system automatically debits linked savings account (`003500000651`) on due date.
- **Expected Result:** Installment credited against outstanding interest and principal.
- **Failure Conditions:** Delinquent repayment incurs late penalties.
- **Evidence Level:** Explicit

### FR-025 — Loan Restructuring, Waivers & Legal Recovery
- **Requirement:** The system shall support credit risk interventions including interest waivers, penalty waivers, admin fee waivers, grace period additions, officer re-assignments, loan restructuring/rescheduling (generating new loan numbers referencing original facility), legal referrals (`Send to legal`), and bad debt write-offs (`Write off`).
- **Entity:** `Loan`
- **Actor:** Credit Manager
- **Source:** `loan_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Delinquent or performing loan requiring administrative modification.
- **Input:** Loan Number, Action (`WAIVE_INTEREST`, `WAIVE_PENALTY`, `WAIVE_ADMIN_FEES`, `RESCHEDULE`, `ADD_GRACE_PERIOD`, `TRANSFER_OFFICER`, `SEND_TO_LEGAL`, `WRITE_OFF`), Justification Notes, Adjusted Terms.
- **Business Rules:** Waivers post corresponding reversing entries. Rescheduling closes old loan and books new loan contract. Write-offs post against Loan Loss Provision accounts.
- **Expected Result:** Loan contract state and ledger updated.
- **Failure Conditions:** Insufficient authorization limits.
- **Evidence Level:** Explicit

---
""")

    # 15. API Requirements
    s.append("""## 15. API Requirements

### Extended API Catalog (Incorporating Savings & Loans)
- `GET /savings` — Query savings accounts with status filters (`active`, `inactive`, `frozen`).
- `GET /savings/{number}` — Fetch detailed account overview, available vs actual balance, and recent ledger entries.
- `POST /savings/{number}/deposit` — Post cash or counter deposit to savings account.
- `POST /savings/{number}/create-individual-withdrawal` — Post cash withdrawal from savings account.
- `POST /savings/{number}/direct-transfers` — Execute inter-account transfer.
- `POST /savings/{number}/freeze` / `POST /savings/{number}/unfreeze` — Toggle administrative hold.
- `POST /savings/{number}/close` — Liquidate balance and close account.
- `PATCH /savings/{number}/alerts` — Update 14 SMS alert rule checkboxes.
- `GET /savings/applications` — Fetch pending savings account opening applications.
- `POST /savings/applications/{id}/approve` — Approve savings account opening.
- `GET /savings/fixed-deposits` — List term deposits with 7 lifecycle status filters.
- `POST /savings/fixed-deposits` — Book new fixed deposit (Till or Savings Offset).
- `GET /savings/fixed-deposits/{id}` — Fetch fixed deposit terms and certificate data.
- `POST /savings/fixed-deposits/{id}/terminate` — Liquidate fixed deposit before or at maturity.
- `GET /savings/standing-orders` — List recurring standing orders.
- `POST /savings/{number}/standing-orders/create` — Setup new recurring standing order.
- `GET /savings/debit-cards` — List debit card issuance requests and statuses.
- `POST /savings/{number}/debit-cards` — Request Interswitch debit card issuance.
- `GET /loans` — List active loan facilities with portfolio filters (`outstanding`, `due`, `performing`, `sent_to_legal`).
- `GET /loans/{number}` — Fetch full loan details, linked repayment account, and amortization schedule.
- `POST /loans/calculator` — Calculate amortization schedule based on method (`flat`, `declining`, `amortize`).
- `GET /loans/applications` — Fetch loan origination pipeline (`approved`, `pending`, `rejected`, `cancelled`).
- `POST /loans/applications` — Submit new loan origination application.
- `POST /loans/applications/{id}/approve` — Sanction loan application.
- `POST /loans/applications/{id}/disburse` — Disburse approved funds into member savings account.
- `POST /loans/{number}/repayments` — Post manual loan repayment.
- `POST /loans/{number}/waiver-interest` — Waive accrued interest.
- `POST /loans/{number}/waiver-penalty` — Waive late payment penalties.
- `POST /loans/{number}/reschedule` — Restructure loan and issue new facility reference.
- `POST /loans/{number}/send-to-legal` — Forward defaulted loan to legal debt collection.
- `POST /loans/{number}/write-off` — Write off bad debt against loan loss provisions.
- `GET /loans/credit-reporting` — Query credit bureau history (CBA).

---
""")

    # 16. Data / Database Requirements
    s.append("""## 16. Data / Database Requirements

### Schema Additions for Savings and Loans
```sql
-- Savings Products Table
CREATE TABLE savings_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'FlexSave'
    code VARCHAR(20) UNIQUE NOT NULL,
    minimum_balance DECIMAL(18,2) NOT NULL DEFAULT 5000.00,
    opening_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_rate_annual DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Savings Accounts Table
CREATE TABLE savings_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(30) UNIQUE NOT NULL, -- e.g. '003500005281'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    product_id INTEGER NOT NULL REFERENCES savings_products(id),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED')),
    actual_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    opening_fee_charged BOOLEAN NOT NULL DEFAULT FALSE,
    last_ledger_fee_charged_at TIMESTAMP WITH TIME ZONE,
    last_transaction_at TIMESTAMP WITH TIME ZONE,
    opened_at DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Savings SMS Alert Rules
CREATE TABLE savings_sms_alert_configs (
    account_id BIGINT PRIMARY KEY REFERENCES savings_accounts(id) ON DELETE CASCADE,
    notify_on_deposit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_withdrawal BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_earning_interest BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_earning_dividends BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_outgoing_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_incoming_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_debit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_credit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_standing_order BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_application BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_loan_disbursement BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_payment BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_refund BOOLEAN NOT NULL DEFAULT FALSE
);

-- Fixed Deposits Table
CREATE TABLE fixed_deposits (
    id BIGSERIAL PRIMARY KEY,
    deposit_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    funding_source VARCHAR(30) NOT NULL CHECK (funding_source IN ('TILL_CASH', 'SAVINGS_OFFSET')),
    funding_savings_account_id BIGINT REFERENCES savings_accounts(id),
    payout_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    depositor_name VARCHAR(150) NOT NULL,
    depositor_phone VARCHAR(30) NOT NULL,
    principal_amount DECIMAL(18,2) NOT NULL,
    starts_on DATE NOT NULL,
    period_months INTEGER NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_interval VARCHAR(30) NOT NULL CHECK (interest_interval IN ('monthly', 'yearly', 'Not applicable')),
    payout_option VARCHAR(50) NOT NULL CHECK (payout_option IN ('Principal', 'Principal & Interest')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RUNNING', 'AWARDED', 'TERMINATED', 'MATURED_NOT_AWARDED', 'TERMINATED_NO_INTEREST', 'AWARDED_ON_MATURITY', 'TERMINATED_WITH_INTEREST')),
    matures_at DATE NOT NULL,
    accrued_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Standing Orders Table
CREATE TABLE standing_orders (
    id BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    destination_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    amount DECIMAL(18,2) NOT NULL,
    frequency VARCHAR(30) NOT NULL CHECK (frequency IN ('daily', 'weekly', 'monthly')),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'RUNNING', 'REJECTED', 'EXPIRED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Products Table
CREATE TABLE loan_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'NCHIEI Credit', 'Business Loans'
    code VARCHAR(20) UNIQUE NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    default_annual_interest_rate DECIMAL(5,2) NOT NULL,
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    min_principal DECIMAL(18,2) NOT NULL,
    max_principal DECIMAL(18,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Loans Table
CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    loan_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '1D04FE1229'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    loan_officer_id BIGINT NOT NULL REFERENCES users(id),
    product_id INTEGER NOT NULL REFERENCES loan_products(id),
    repayment_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    principal_disbursed DECIMAL(18,2) NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    installments_count INTEGER NOT NULL,
    grace_period_months INTEGER NOT NULL DEFAULT 0,
    installment_amount DECIMAL(18,2) NOT NULL,
    auto_pay_from_savings BOOLEAN NOT NULL DEFAULT TRUE,
    disbursed_at DATE NOT NULL,
    maturity_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING', 'PERFORMING', 'DUE', 'OUTSTANDING', 'SENT_TO_LEGAL', 'WRITTEN_OFF', 'RESCHEDULED', 'CLEARED')),
    rescheduled_from_loan_number VARCHAR(50),
    total_principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_penalty_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_penalty DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_admin_fees DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Repayment Schedules Table
CREATE TABLE loan_repayment_schedules (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(18,2) NOT NULL,
    interest_due DECIMAL(18,2) NOT NULL,
    total_installment DECIMAL(18,2) NOT NULL,
    principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    is_settled BOOLEAN NOT NULL DEFAULT FALSE
);
```

---
""")

    # 17 to 26 - Retain & Expand
    s.append("""## 17. Search, Filtering and Reporting

### Savings & Loan Specific Reporting Capabilities
From the 109 cataloged reports, the following are directly confirmed by the savings and loan captures:
- `view savings accounts`, `view savings ledger`, `view savings transactions`, `view failed standing orders`.
- `view fixed deposits`, `view fixed certificate`.
- `view loan applications`, `view loan arrears`, `view loan bookings`, `view loan schedule`, `view loan ledger`, `view loan repayments`, `view loan repayment summary`, `view cleared loans`, `view due loans`, `view written off loans`, `view rescheduled loans`.
- `view par by ageing`, `view par by ageing summary`, `view umra par by ageing` (Portfolio At Risk aging schedules for regulatory reporting).
- `view crb register`, `download crb files` (Credit Reference Bureau returns).

---

### BR-012: Double-Entry Balancing Invariant
- **Rule:** Every Journal Voucher must have equal debits and credits: `SUM(debit_amount) == SUM(credit_amount)`. Imbalanced vouchers must be rejected with HTTP 400 (`ERR_TB_OUT_OF_BALANCE`).

### BR-013: Teller Drawer GL Integration
- **Rule:** Physical teller cash drawers (e.g. `1-1-1-1-2 Teller - 1`) are literal asset accounts under `1-1-1-1 CASH AT HAND`. Any cash desk deposit, withdrawal, or till transfer must post directly to the teller's assigned GL code.

### BR-014: Password Re-Authentication on Share Dividends
- **Rule:** Authorizing a dividend declaration requires re-entering the staff user's login password (`Password*`). The backend must re-verify credentials before executing payouts.

### BR-015: Frozen Period Immutable Lock
- **Rule:** No transaction or journal voucher may be posted with a value date inside a closed financial period unless the period has been explicitly rolled back.

### BR-016: Trial Balance Imbalance Alarm during Period Close
- **Rule:** Closing a financial period performs an automated ledger audit. If debits != credits, the period closing is blocked and alert `tb_imbalance` is dispatched.

---
""")

    # 11. Validation Rules
    sections.append("""## 11. Validation Rules

### Confirmed Validations (Explicit in HTML)
1. **Required Fields:** Marked with asterisks in the UI:
   - Individual: Branch, Salutation, First Name, Last Name, Gender, Marital Status, Date of Birth, Country of Birth, Nationality, Home Ownership, Identification Type, Identification Number, Entry Date, Phone Number, Country, Address, Next of Kin (First name, Second name, Phone, Physical Address, Relationship), Occupation, Employer Name, Employer Address, Income Frequency, Income Per Month, Signature, Identification Document, Profile Photo.
   - Group: Branch, Group Name, Group Type, Registration Number, Entry Date, Phone, Country, Physical Address.
   - Institution: Branch, Institution Name, Registration Number, Registration Date, TIN Number, Business Type, Institution Category, Residence Ownership, Entry Date, Phone, Country, Physical Address, Income Frequency, Income Per Month.
   - User: First Name, Last Name, Username, Email Address, Password, Phone, Role, Branch, all 9 Min/Max transaction limit pairs.
   - Role: Role Name.
2. **Data Formats:**
   - Email: Standard RFC-compliant format.
   - Date: Standard date picker validation.
   - Numeric Bounds: All min amount fields must be non-negative and `<= max amount`.

### Inferred Validations
1. **Uniqueness:**
   - `username` and `email` must be system-unique.
   - `member_number` must be system-unique across all branches.
   - `identification_number` (NIN/Passport) must be unique among individual members.
   - `tin_number` must be unique among institutions.
2. **Phone Number Standards:** International format compliant with E.164 (e.g. Uganda +256...).

### Unknown Validations (Requires Clarification)
1. Exact regex validation for Uganda NIN (National Identification Number: 14 alphanumeric characters).
2. Maximum allowable file sizes and MIME types for uploaded documents and photos.

---
""")

    # 12. Authentication Requirements
    sections.append("""## 12. Authentication Requirements

### Credentials & Session Flow
- **Primary Mechanism:** Username and Password authentication (`POST /api/auth/login`).
- **Two-Factor Authentication (SMS OTP):** Evidence in `list_users.html` action button *"Enable SMS OTP"*. When enabled, a one-time numeric passcode must be generated and dispatched via SMS.
- **Token / Session Lifecycle:**
  - Token refresh endpoint: `/api/auth/refresh`.
  - User identity introspection: `/api/auth/user`.
  - Termination: `/api/auth/logout`.
- **Idle Timeout:** 5 minutes (300 seconds) inactivity threshold as explicitly declared in the Idle Detector widget.
- **Credential Governance:** Staff password change capability supported via `/profile` ("Change Password").

---
""")

    # 13. Authorization Requirements
    sections.append("""## 13. Authorization Requirements

### Complete Catalog of 34 Modules & 382 Permissions
The backend authorization engine must enforce the complete catalog of permissions reverse-engineered from `create_role.html`:

| Module | Perm Count | Sample Observed Permissions |
|---|---|---|
| **savings** | 24 | `delete standing order`, `approve standing order`, `reject standing order`, `view savings account statement`, `reverse withdrawal`, `reverse deposit`, `register direct debit credit to customer branch`, `register deposit`, `update deposit`, `register withdrawal`, `update withdrawal`, `register savings transfer`, `update savings transfer`, `register standing order`, `update standing order`, `terminate standing order`, `register savings account credit`, `update savings account credit`, `register savings account debit`, `update savings account debit`, `register direct account credit`, `register direct account debit`, `update direct account credit`, `update direct account debit` |
| **bulk files** | 5 | `approve bulk file`, `reject bulk file`, `process bulk file`, `upload bulk file`, `rollback bulk file` |
| **fixed deposits** | 10 | `accrue fixed deposit`, `rollback fixed deposit`, `award fixed deposit`, `register fixed deposit`, `update fixed deposit`, `delete fixed deposit`, `approve fixed deposit`, `reject fixed deposit`, `view fixed certificate`, `terminate fixed deposit` |
| **scheduled tasks** | 5 | `deactivate task`, `activate task`, `run active task immediately`, `run all due active tasks`, `schedule task` |
| **accounting** | 19 | `register journal`, `reverse journal`, `approve withdrawal request`, `register bank transfer`, `approve bank transfer`, `perform divergent branch journal`, `approve transaction`, `reverse transaction`, `reject transaction`, `register transaction reversal`, `register budget`, `update budget application`, `approve budget`, `reject budget`, `update budget`, `delete budget`, `register withdrawal charge ranges`, `update withdrawal charge ranges`, `delete withdrawal charge ranges` |
| **reports** | 109 | `update report`, `view portfolio monitoring report`, `view all customers`, `view arrears vs savings`, `view asset depreciation`, `view audit trail`, `view backdated transactions`, `view balance sheet`, `view borrower category by gender`, `view branch summary`, `view budget`, `view cash flow statement`, `view cleared loans`, `view compulsory savings`, `view cumulated savings`, `view cumulative shares`, `view customer applications`, `view customer signatories`, `view customer stakeholders`, `view disbursements`, `view disposed assets`, `view dividends shared`, `view due loans`, `view failed standing orders`, `view fixed asset profiles`, `view fixed asset transfers`, `view fixed assets register`, `view fixed deposits`, `view forecast`, `view general ledger`, `view group customer`, `view group nonmember customer`, `view income statement`, `view individual customer`, `view institution customer`, `view joint customer`, `view journals`, `view loan applications`, `view loan arrears`, `view loan bookings`, `view loan guarantors`, `view loan ledger`, `view loan loss provissions`, `view loan products`, `view loan recoveries`, `view loan repayment summary`, `view loan repayments`, `view maturity`, `view msacco transactions`, `view officer summary`, `view outstanding loans`, `view par by ageing`, `view par by ageing summary`, `view payroll`, `view periodic loan transaction`, `view pmt balance sheet`, `view pmt income statement`, `view pmt performance indicators`, `view pmt portfolio activity`, `view portfolio summary`, `view portifolio monitoring`, `view portifolio status`, `view prepaid interest`, `view product by borrower category`, `view profit loss`, `view rescheduled loans`, `view salary payments`, `view savings account applications`, `view savings accounts`, `view savings ledger`, `view savings products`, `view savings transactions`, `view sector by borrower category`, `view sector distribution by gender`, `view sector distribution by product`, `view share transactions`, `view shares ledger`, `view sms notification`, `view staff kyc`, `view staff transfers`, `view standing orders`, `view till sheet`, `view transactions`, `view transferred customers`, `view transferred loans`, `view trial balance`, `view users`, `view written off loans`, `view written off loans schedule`, `view msacco credit statement`, `view guarantees`, `view ABI guarantee`, `view ABI line of credit`, `view loan ageing`, `view customer statement`, `view group consolidation`, `view nin verifications`, `view umra balance sheet`, `view umra income statement`, `view umra par by ageing`, `view msacco medical`, `view mismatches`, `view anti money laundering`, `view savings account statement transactions`, `view management reports`, `view audit reports`, `view bou liquidity`, `view bou bsa`, `view salary advance statement` |
| **loans** | 45 | `update provisions`, `refund last repayment`, `initiate crb report consent`, `get crb report`, `register sponsor`, `update sponsor`, `delete sponsor`, `update loan sponsor`, `unbook loans`, `disapprove loan application`, `download crb files`, `view crb register`, `admin fees waiver`, `view loan schedule`, `cancel loan application`, `unbook compulsory savings`, `unbook compulsory shares`, `delete application collateral`, `apply for loan`, `approve loan application`, `reject loan application`, `update loan application`, `delete loan application`, `reject disbursement`, `disburse loan`, `update disbursement`, `delete disbursement`, `register repayment`, `register refund`, `register penalty waiver`, `register interest waiver`, `reverse waiver`, `send loan to legal`, `remove loan from legal`, `penalize loan`, `write off loan`, `unwrite off loan`, `register loan recovery`, `delete loan recovery`, `reschedule loan`, `register grace period on running loan`, `transfer loan`, `register holiday`, `update holiday`, `delete holiday` |
| **technical** | 4 | `activate license`, `update sms settings`, `update msacco settings`, `manage system jobs` |
| **msacco** | 9 | `register msacco deposit`, `register msacco withdrawal`, `msacco account balance`, `msacco account statement`, `register msacco medical treatment`, `register msacco medical billing`, `reset msacco pin`, `activate msacco account`, `deactiavate msacco account` |
| **account deduction** | 5 | `register account deduction`, `update account deduction`, `activate account deduction`, `deactivate account deduction`, `deduct account opening fees` |
| **fixed assets** | 9 | `impair fixed asset`, `register fixed asset`, `update fixed asset`, `delete fixed asset`, `register fixed asset depreciation`, `delete fixed asset depreciation`, `dispose fixed asset`, `revalue fixed asset`, `transfer fixed asset` |
| **payroll deduction** | 4 | `register payroll deduction`, `update payroll deduction`, `activate payroll deduction`, `deactivate payroll deduction` |
| **payroll** | 17 | `pay staff salary`, `register staff allowance`, `update staff allowance`, `delete staff allowance`, `register payroll allowance`, `update payroll allowance`, `delete payroll allowance`, `view payroll allowance`, `register other payroll deduction`, `update other payroll deduction`, `delete other payroll deduction`, `view other payroll deductions`, `request salary advance`, `approve salary advance`, `reject salary advance`, `cancel salary advance`, `disburse salary advance` |
| **staff** | 13 | `transfer staff`, `register staff deduction/addition`, `update staff deduction/addition`, `delete staff deduction/addition`, `register staff`, `update staff`, `delete staff`, `activate staff`, `deactivate staff`, `register staff salary advance settings`, `update staff salary advance settings`, `delete staff salary advance settings`, `view staff` |
| **customers** | 15 | `verify customer NIN`, `review kyc`, `register agency applications`, `approve agency applications`, `delete agency applications`, `reject agency applications`, `register individual customer`, `update individual customer`, `register group customer`, `update group customer`, `register joint customer`, `update joint customer`, `register institution customer`, `update institution customer`, `delete customer` |
| **interswitch** | 1 | `interswitch card issuance` |
| **internet-banking** | 2 | `enable internet banking`, `disable internet banking` |
| **cards** | 3 | `register card issuance`, `approve card issuance`, `issue cards` |
| **settings** | 6 | `register custom fields`, `update custom fields`, `update setting`, `update forex and currency`, `activate multi currency`, `deactivate multi currency` |
| **bulk sms** | 4 | `create messaging list`, `update messaging list`, `delete messaging list`, `send bulk sms` |
| **modules** | 10 | `access bulk messaging module`, `access savings module`, `access loans module`, `access shares module`, `access settings module`, `access accounting module`, `access payroll module`, `access agency banking module`, `access reports module`, `access user management module` |
| **guarantees** | 7 | `apply for guarantee`, `update guarantee application`, `approve guarantee application`, `delete guarantee application`, `reject guarantee application`, `view guarantee details`, `terminate guarantee` |
| **shares** | 13 | `view customer share transactions`, `register share purchase`, `update share purchase`, `register share transfer`, `update share transfer`, `register share liquidation`, `update share liquidation`, `register share value`, `reverse share purchase`, `reverse share liquidation`, `reverse share transfer`, `share dividends`, `rollback dividends` |
| **dashboard** | 3 | `view loan portifolio metrics`, `view profitability metrics`, `view transaction revenue metrics` |
| **close financial period** | 2 | `close financial period`, `rollback closed period` |
| **chart** | 5 | `register chart account`, `update chart account`, `activate chart account`, `deactivate chart account`, `delete chart account` |
| **users** | 10 | `register user account`, `update user account`, `delete user account`, `disable user account`, `enable user account`, `register user profile`, `update user profile`, `delete user profile`, `enable user profile`, `disable user profile` |
| **accounts** | 8 | `register savings account`, `update savings account`, `delete savings account`, `approve savings account`, `close savings account`, `reopen savings account`, `freeze savings account`, `unfreeze savings account` |
| **products** | 3 | `register product`, `update product`, `delete product` |
| **branches** | 5 | `create branch`, `update branch`, `activate branch`, `deactivate branch`, `delete branch` |
| **fixed asset profiles** | 3 | `register fixed asset profile`, `update fixed asset profile`, `delete fixed asset profile` |
| **database** | 4 | `activate restoration mode`, `deactivate restoration mode`, `restore backup`, `download backup` |

---
""")

    # 14. Functional Requirements
    sections.append("""## 14. Functional Requirements

### FR-001 — Member Category Selection
- **Requirement:** The system shall present a registration gateway allowing the operator to select one of four member entity types: Individual, Group, Joint, or Institution.
- **Entity:** `Customer`
- **Actor:** Customer Care / Staff
- **Source:** `create_member_step_one.html`
- **Priority:** MUST HAVE
- **Preconditions:** Staff is authenticated and holds permission `register individual customer` / `register group customer` / etc.
- **Input:** Selected entity type (`INDIVIDUAL`, `GROUP`, `JOINT`, `INSTITUTION`).
- **Business Rules:** Navigates directly to the specialized multi-field schema for the chosen category.
- **Expected Result:** Renders appropriate registration form.
- **Failure Conditions:** Unauthorized access redirects to 403 Forbidden.
- **Evidence Level:** Explicit

### FR-002 — Individual Member Registration
- **Requirement:** The system shall capture complete individual KYC data across Bio Data, Contact Information, Next of Kin, Employment, Income, Bank Details, and Document Uploads, persisting the record in `PENDING` approval status.
- **Entity:** `Customer`, `CustomerIndividual`, `NextOfKin`, `EmploymentIncome`, `ExternalBank`
- **Actor:** Customer Care / Registration Officer
- **Source:** `create_individual_member.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid branch and designated savings officer selected.
- **Input:** Salutation, Names, Gender, Marital Status, DOB, Nationality, Home Ownership, Identification Type & Number, Entry Date, Phone, Physical Address, Next of Kin details, Occupation, Income Per Month, Signature scan, ID document scan, Photo file.
- **Business Rules:** Form submission enters the Maker-Checker queue (`list_member_applications.html`). System generates unique formatted `member_number` (e.g. `00350000519`).
- **Expected Result:** Member record created in `PENDING` state; event `customer_applications_pending_approval` dispatched.
- **Failure Conditions:** Missing mandatory fields; duplicate identification number / NIN.
- **Evidence Level:** Explicit

### FR-003 — Member Application Approval & Rejection
- **Requirement:** The system shall allow authorized managers to review pending member applications and execute an approval or rejection decision.
- **Entity:** `Customer`
- **Actor:** Branch Manager / Checker
- **Source:** `list_member_applications.html`
- **Priority:** MUST HAVE
- **Preconditions:** Member application exists in `PENDING` status. Checker is not the maker who created the record.
- **Input:** Application ID, Approval Decision (`APPROVE` or `REJECT`), Rejection Reason (if rejected).
- **Business Rules:** On approval, status transitions to `ACTIVE`, core savings passbook ledger is provisioned, and welcome SMS event is triggered. On rejection, status transitions to `REJECTED`.
- **Expected Result:** Application removed from pending queue and visible under appropriate master/rejected directory.
- **Failure Conditions:** Checker is maker (BR-001 violation); application already decided.
- **Evidence Level:** Explicit

### FR-004 — Group Member Registration
- **Requirement:** The system shall register solidarity lending groups and VSLAs with group type classification (Members vs Non Members) and official registration certificates.
- **Entity:** `Customer`, `CustomerGroup`
- **Actor:** Field Officer / Staff
- **Source:** `create_group.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated operator with group registration privilege.
- **Input:** Branch, Group Name, Group Type (`Members` or `Non Members`), Registration Number, Entry Date, Savings Officer, Phone, Address.
- **Business Rules:** Groups may hold group savings accounts and act as institutional borrowing entities.
- **Expected Result:** Group entity persisted in `PENDING` status awaiting approval.
- **Failure Conditions:** Duplicate group registration number within branch.
- **Evidence Level:** Explicit

### FR-005 — Joint Account Registration
- **Requirement:** The system shall register joint member entities shared by two or more individuals with shared contact information and loan limit settings.
- **Entity:** `Customer`, `CustomerJoint`
- **Actor:** Customer Care / Staff
- **Source:** `create_joint.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated operator.
- **Input:** Branch, Joint Name, Entry Date, Assigned Officer, Phone, Address, Loan Limit toggle.
- **Business Rules:** Joint entity requires multi-signatory rules for subsequent withdrawals.
- **Expected Result:** Joint customer record persisted.
- **Failure Conditions:** Missing mandatory branch, name, or phone.
- **Evidence Level:** Explicit

### FR-006 — Institution Customer Registration
- **Requirement:** The system shall register corporate and institutional customers, capturing legal categorization, URA TIN numbers, corporate category, and business sectors.
- **Entity:** `Customer`, `CustomerInstitution`
- **Actor:** Business Development / Customer Care
- **Source:** `create_institution.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated staff.
- **Input:** Institution Name, Registration Number, Registration Date, TIN Number, Business Type (e.g. Agriculture, SACCO, MDI, etc.), Institution Category (e.g. Limited Liability, NGO, etc.), Residence Ownership, Entry Date, Phone, Physical Address, Monthly Income.
- **Business Rules:** TIN must be validated for format and uniqueness.
- **Expected Result:** Institutional record created for approval.
- **Failure Conditions:** Duplicate TIN or registration number.
- **Evidence Level:** Explicit

### FR-007 — User Account Provisioning & Financial Authorization Limits
- **Requirement:** The system shall provision staff user accounts with individual operational bounds across 9 financial transaction categories and alert subscription selections.
- **Entity:** `User`, `UserNotificationSubscription`
- **Actor:** System Administrator
- **Source:** `create_user.html`
- **Priority:** MUST HAVE
- **Preconditions:** Administrator holds `register user account` permission.
- **Input:** First Name, Last Name, Username, Email, Password, Phone, Designation/Role, Station/Teller, Branch, 9 Min/Max Limit pairs, Selection of Notification Subscriptions (up to 22 checkboxes).
- **Business Rules:** Password must be hashed securely; Min Amount must not exceed Max Amount; limits strictly enforce maker-checker thresholds at runtime.
- **Expected Result:** User account created and enabled.
- **Failure Conditions:** Duplicate username or email; Min Amount > Max Amount.
- **Evidence Level:** Explicit

### FR-008 — User Lifecycle State Management (Deactivate, Delete, SMS OTP)
- **Requirement:** The system shall provide operational controls on staff user accounts to deactivate active users, soft-delete records, and toggle SMS OTP two-factor authentication.
- **Entity:** `User`
- **Actor:** System Administrator
- **Source:** `list_users.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target user exists.
- **Input:** Target User ID, Action (`DEACTIVATE`, `ACTIVATE`, `DELETE`, `ENABLE_SMS_OTP`, `DISABLE_SMS_OTP`).
- **Business Rules:** Deactivated users cannot authenticate (`ERR_ACCOUNT_INACTIVE`). Deleted users are soft-deleted to preserve transaction audit integrity.
- **Expected Result:** User status updated immediately; active sessions invalidated.
- **Failure Conditions:** Administrator cannot deactivate own account.
- **Evidence Level:** Explicit

### FR-009 — Role Profile Definition & Operating Hours Configuration
- **Requirement:** The system shall allow administrators to create and edit Role Profiles, configuring daily operating time windows (`open_time` to `close_time`) and assigning granular checkboxes from the 382 permission pool.
- **Entity:** `Role`, `RolePermission`
- **Actor:** System Administrator
- **Source:** `create_role.html`, `edit_role.html`, `user_details.html`
- **Priority:** MUST HAVE
- **Preconditions:** Administrator holds `register user profile` or `update user profile`.
- **Input:** Role Name, Open Time (Time picker), Close Time (Time picker), Array of selected permission keys.
- **Business Rules:** Operating hours enforce system access cutoff; permission updates take effect on subsequent token evaluation.
- **Expected Result:** Role persisted and available for user assignment.
- **Failure Conditions:** Empty role name; close time precedes open time.
- **Evidence Level:** Explicit

### FR-010 — Role Activation, Deactivation & Deletion
- **Requirement:** The system shall provide lifecycle management for role profiles, allowing activation, deactivation, and deletion.
- **Entity:** `Role`
- **Actor:** System Administrator
- **Source:** `list_roles.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target role exists.
- **Input:** Role ID, Action (`ACTIVATE`, `DEACTIVATE`, `DELETE`).
- **Business Rules:** A role cannot be deleted if active users are assigned to it (e.g., *"1 User assigned"* guard). Deactivating a role blocks authentication for all assigned users.
- **Expected Result:** Role status updated.
- **Failure Conditions:** Attempting to delete a role with assigned users (`ERR_ROLE_IN_USE`).
- **Evidence Level:** Explicit

### FR-011 — Real-Time Liquidity & Float Balance Monitoring
- **Requirement:** The system shall maintain and expose real-time aggregate balances for external payment channels: MSACCO Credit, MTN Mobile Money Float, Airtel Money Float, Bank Transfer Float, and General Float.
- **Entity:** `ExternalFloatLedger`
- **Actor:** System / Cashier / Manager
- **Source:** Global Top Bar across all 17 HTML files
- **Priority:** MUST HAVE
- **Preconditions:** Integration webhooks and ledger updates configured.
- **Input:** Channel Identifier, Float Inflow / Outflow, Current Balance.
- **Business Rules:** Float is debited on outgoing mobile money cashouts and credited on incoming deposits. Balance warnings fire when float breaches safety floor.
- **Expected Result:** Current float balances returned to client header widgets.
- **Failure Conditions:** Channel gateway timeout.
- **Evidence Level:** Explicit

### FR-012 — 5-Minute Inactivity Session Timeout
- **Requirement:** The system shall track client activity and invalidate sessions exceeding 300 seconds of inactivity.
- **Entity:** `UserSession`
- **Actor:** System / Authentication Engine
- **Source:** Idle Detector component across all 17 HTML files
- **Priority:** MUST HAVE
- **Preconditions:** Active session token.
- **Input:** Last activity timestamp.
- **Business Rules:** Inactivity > 5 minutes invalidates token and forces re-login.
- **Expected Result:** Session expired; client redirected to login.
- **Failure Conditions:** N/A
- **Evidence Level:** Explicit

### FR-013 — Asynchronous Job Monitoring & Failure Notification
- **Requirement:** The system shall capture execution logs for all background tasks and broadcast high-priority failure notifications to subscribed users whenever a job fails.
- **Entity:** `SystemJob`, `JobExecutionLog`, `Notification`
- **Actor:** System / Scheduler Engine
- **Source:** Global Header Alert: *"A recently started job has failed Today 10:26:26 PM"*
- **Priority:** MUST HAVE
- **Preconditions:** Background job dispatched.
- **Input:** Job ID, Status (`SUCCESS`, `FAILED`), Error Details.
- **Business Rules:** Failed job records error in `/settings/jobs/{id}/logs` and sends alert to users subscribed to `failed_job`.
- **Expected Result:** In-app notification with "Mark as read" trigger.
- **Failure Conditions:** Database connection loss during error logging.
- **Evidence Level:** Explicit

### FR-014 — Agency Banking Application Queue & Vetting
- **Requirement:** The system shall provide an application intake and maker-checker queue for third-party banking agents with pending and rejected tabs.
- **Entity:** `AgencyApplication`
- **Actor:** Agency Banking Lead / Manager
- **Source:** `list_agency_applications.html`
- **Priority:** MUST HAVE
- **Preconditions:** Agency application submitted.
- **Input:** Agent ID, Business Name, Location, Branch, Decision (`APPROVE`, `REJECT`).
- **Business Rules:** Approved agents obtain agency terminal credentials and float limits.
- **Expected Result:** Agent status updated.
- **Failure Conditions:** Incomplete KYC documentation.
- **Evidence Level:** Explicit

---
""")

    # 15. API Requirements
    sections.append("""## 15. API Requirements

Based on explicit form submissions, list page interactions, and the 190 discovered routes in the client bundle, the following RESTful API surface is required:

### Authentication & Session Subsystem
- `POST /api/auth/login`
  - **Purpose:** Authenticate staff credentials.
  - **Payload:** `{ username: "...", password: "..." }`
  - **Response:** `{ token: "...", refresh_token: "...", expires_in: 300, user: { ... } }`
  - **Validation & Rules:** Rate-limited; verifies `is_active=true`, `is_blocked=false`, and temporal window `open_time <= now <= close_time`.
- `POST /api/auth/logout`
  - **Purpose:** Invalidate current session token.
- `GET /api/auth/user`
  - **Purpose:** Return current authenticated user profile, permissions, and assigned limits.
- `POST /api/auth/refresh`
  - **Purpose:** Issue fresh JWT against valid refresh token.

### Customers & Membership Subsystem
- `GET /customers`
  - **Purpose:** Query paginated customer directory with filtering.
  - **Query Params:** `type` (individual, group, joint, institution), `branch_id`, `search`, `page`, `per_page`.
- `POST /customers/individuals`
  - **Purpose:** Submit new individual member application.
  - **Payload:** Multipart/form-data containing all Bio Data, Contact, Kin, Employment, Bank details, and uploaded binary files (signature, ID, photo).
  - **Response:** 201 Created `{ id: 1223, member_number: "00350000519", status: "PENDING" }`
- `POST /customers/groups`
  - **Purpose:** Register group member.
  - **Payload:** `{ branch_id, group_name, group_type, registration_number, entry_date, savings_officer_id, phone, address, ... }`
- `POST /customers/joints`
  - **Purpose:** Register joint account entity.
- `POST /customers/institutions`
  - **Purpose:** Register corporate/institutional entity.
  - **Payload:** `{ institution_name, registration_number, registration_date, tin_number, business_type, institution_category, ... }`
- `GET /customers/applications`
  - **Purpose:** Fetch maker-checker queue for member applications.
  - **Query Params:** `status=pending` or `status=rejected`, `branch_id`, `page`.
- `POST /customers/applications/{id}/approve`
  - **Purpose:** Approve pending member application and activate ledgers.
- `POST /customers/applications/{id}/reject`
  - **Purpose:** Reject pending member application with reason notes.
- `GET /customers/agent-applications`
  - **Purpose:** Fetch agency banking applications queue.

### Users & Security Subsystem
- `GET /users`
  - **Purpose:** Fetch list of system users.
  - **Query Params:** `status` (all, active, inactive), `search`, `page`.
- `POST /users`
  - **Purpose:** Provision new user account with transaction limits and notification subscriptions.
  - **Payload:** `{ first_name, last_name, username, email, password, phone, role_id, branch_id, station_id, limits: { ... }, notification_subscriptions: [ ... ] }`
- `PATCH /users/{id}`
  - **Purpose:** Update user details, limits, or assign new role.
- `POST /users/{id}/deactivate`
  - **Purpose:** Deactivate active user.
- `DELETE /users/{id}`
  - **Purpose:** Soft-delete user account.
- `POST /users/{id}/toggle-sms-otp`
  - **Purpose:** Enable or disable 2FA requirement.

### Roles & RBAC Subsystem
- `GET /users/roles`
  - **Purpose:** List all role profiles with assigned user counts and operating hours.
  - **Query Params:** `status` (all, active, inactive).
- `POST /users/roles`
  - **Purpose:** Create new role profile.
  - **Payload:** `{ role_name: "...", open_time: "00:00:00", close_time: "23:00:00", permissions: [ "register deposit", ... ] }`
- `GET /users/roles/{id}`
  - **Purpose:** Retrieve role definition and complete array of assigned permissions.
- `PATCH /users/roles/{id}`
  - **Purpose:** Update role permissions or operating window.
- `POST /users/roles/{id}/activate` / `POST /users/roles/{id}/deactivate`
  - **Purpose:** Toggle operational availability of role.
- `DELETE /users/roles/{id}`
  - **Purpose:** Delete unassigned role.

### Notifications & Float Subsystem
- `GET /notifications`
  - **Purpose:** Fetch current user's unread notifications.
- `POST /notifications/{id}/mark-read`
  - **Purpose:** Mark notification as read.
- `POST /notifications/mark-all-read`
  - **Purpose:** Mark all unread notifications as read.
- `GET /settings/float-balances`
  - **Purpose:** Fetch latest float balances for top-bar indicators (MSACCO, MTN, Airtel, Bank Transfer).

---
""")

    # 16. Data / Database Requirements
    sections.append("""## 16. Data / Database Requirements

### Conceptual Data Model
The database architecture must be a relational database (PostgreSQL recommended) enforcing ACID compliance and double-entry ledger immutability.

```sql
-- Core Branches Table
CREATE TABLE branches (
    id SERIAL PRIMARY KEY,
    branch_code VARCHAR(10) UNIQUE NOT NULL, -- e.g. '0035'
    name VARCHAR(100) NOT NULL, -- e.g. 'KABALE'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Core Roles Table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    open_time TIME NOT NULL DEFAULT '00:00:00',
    close_time TIME NOT NULL DEFAULT '23:59:59',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Role Permissions Table
CREATE TABLE role_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_key VARCHAR(100) NOT NULL,
    UNIQUE(role_id, permission_key)
);

-- Core Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    role_id INTEGER NOT NULL REFERENCES roles(id),
    station_id VARCHAR(50), -- e.g. 'Teller - 1'
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    sms_otp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    
    -- Maker-Checker Financial Limits
    loan_approval_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    loan_approval_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- User Notification Subscriptions Table
CREATE TABLE user_notification_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_key VARCHAR(100) NOT NULL,
    UNIQUE(user_id, event_key)
);

-- Master Customers Table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    member_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '00350000001'
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('INDIVIDUAL', 'GROUP', 'JOINT', 'INSTITUTION')),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    savings_officer_id BIGINT REFERENCES users(id),
    entry_date DATE NOT NULL,
    phone_primary VARCHAR(30) NOT NULL,
    phone_secondary VARCHAR(30),
    email VARCHAR(150),
    country VARCHAR(100) NOT NULL DEFAULT 'Uganda',
    town VARCHAR(100),
    physical_address TEXT NOT NULL,
    postal_code VARCHAR(30),
    postal_address VARCHAR(150),
    has_loan_limit BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'REJECTED', 'INACTIVE', 'DORMANT')),
    created_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Individual Profile Table
CREATE TABLE customer_individuals (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    salutation VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female')),
    marital_status VARCHAR(20) NOT NULL,
    is_dependent BOOLEAN NOT NULL DEFAULT FALSE,
    is_pwd BOOLEAN NOT NULL DEFAULT FALSE,
    date_of_birth DATE NOT NULL,
    country_of_birth VARCHAR(100) NOT NULL,
    nationality VARCHAR(100) NOT NULL DEFAULT 'Ugandan',
    home_ownership VARCHAR(20) NOT NULL CHECK (home_ownership IN ('Owner', 'Tenant', 'Other')),
    identification_type VARCHAR(50) NOT NULL,
    identification_number VARCHAR(100) NOT NULL,
    card_number VARCHAR(100),
    signature_file_path TEXT,
    identification_file_path TEXT,
    photo_file_path TEXT
);

-- Next of Kin Table
CREATE TABLE customer_next_of_kin (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    second_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    physical_address TEXT NOT NULL,
    relationship VARCHAR(30) NOT NULL
);

-- Group Profile Table
CREATE TABLE customer_groups (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    group_name VARCHAR(150) NOT NULL,
    group_type VARCHAR(50) NOT NULL CHECK (group_type IN ('Members', 'Non Members')),
    registration_number VARCHAR(100) NOT NULL
);

-- Joint Profile Table
CREATE TABLE customer_joints (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    joint_name VARCHAR(150) NOT NULL
);

-- Institution Profile Table
CREATE TABLE customer_institutions (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    institution_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100) NOT NULL,
    registration_date DATE NOT NULL,
    tin_number VARCHAR(50) NOT NULL,
    business_type VARCHAR(100) NOT NULL,
    institution_category VARCHAR(100) NOT NULL,
    residence_ownership VARCHAR(50) NOT NULL
);
```

---
""")

    # 17. Search, Filtering and Reporting
    sections.append("""## 17. Search, Filtering and Reporting

### Search Capabilities
- Master member search indexing across: Member Number, Name, Phone Number, and Group Affiliation.
- User search indexing: Username, First Name, Last Name, and Branch.

### Filtering Dimensions
- Status Filters: `All`, `Active`, `Inactive`, `Pending`, `Rejected`.
- Organizational Filters: Branch (`0035 KABALE`), Officer assignment, Customer Subtype.
- Date Range Filters: Registration date, approval date, transaction value date.

### Regulatory & Management Reports Catalog
The system specifies 109 discrete reporting capabilities including:
1. **Central Bank / BOU Returns:** Bank of Uganda Liquidity Return (`bou liquidity`), Bank of Uganda Banking Supervision Return (`bou bsa`).
2. **UMRA Returns:** Uganda Microfinance Regulatory Authority Balance Sheet (`umra balance sheet`), Income Statement (`umra income statement`), and PAR by Aging (`umra par by ageing`).
3. **Credit Operations Reports:** Loan Arrears vs Savings, Portfolio Monitoring, Cleared Loans, Due Loans, Written-off Loans, CRB Registers.
4. **General Ledger & Accounting:** Balance Sheet, Profit & Loss / Income Statement, Cash Flow Statement, Trial Balance, Backdated Transactions, Till Sheet Reconciliation.
5. **Shares & Equity:** Cumulative Shares, Shares Ledger, Dividends Shared Report.

---
""")

    # 18. File and Document Requirements
    sections.append("""## 18. File and Document Requirements

### Upload Specifications
1. **Signature File:** Uploaded during individual registration (`Signature*`). Stored securely; rendered on teller screens for withdrawal verification.
2. **Identification Document Scan:** Uploaded ID card/passport scan (`Indentification*`).
3. **Profile Photo:** Member portrait photograph (`Photo*`).
4. **Bulk Processing Files:** Bulk payroll/disbursement files (`.csv`, `.xlsx`) uploaded for batch processing.
5. **Database Backups:** Compressed database backup files downloadable by administrators (`download backup`, `restore backup`).

---
""")

    # 19. Notification and Event Requirements
    sections.append("""## 19. Notification and Event Requirements

### The 22 Core Notification Triggers
The backend event dispatcher must route notifications based on user subscriptions:
1. `blocked_user`: Dispatched when an account is locked out after consecutive failed authentications.
2. `bulk_files_pending_approval`: Dispatched when an institutional disbursement file is uploaded.
3. `customer_applications_pending_approval`: Dispatched when a member registration form is submitted.
4. `customer_kyc_updated`: Dispatched when member biodata or identity documents are altered.
5. `failed_job`: Dispatched immediately when an asynchronous background job errors out.
6. `fixed_deposit_applications_pending_approval`: Dispatched on term deposit placement.
7. `license_expiry_reminder`: Dispatched when software license term nears expiration.
8. `license_upgraded`: Dispatched when license tier or capacity is expanded.
9. `liquid_assets_balance_running_low`: Dispatched when branch cash or float balances breach minimum reserve.
10. `loan_applications_pending_approval`: Dispatched when a loan officer submits an application.
11. `loan_applications_pending_disbursement`: Dispatched when an approved loan is ready for payout.
12. `msacco_customer_onboarding`: Dispatched when a member self-registers via mobile USSD/App.
13. `msacco_loan_application`: Dispatched on mobile emergency loan request.
14. `opening_a_closed_period`: High-priority alert triggered if an accounting period is unsealed.
15. `reserve_amount_limit`: Dispatched when statutory reserves breach threshold.
16. `savings_account_applications_pending_approval`: Dispatched on savings account opening.
17. `standing_orders_pending_approval`: Dispatched when a recurring payment rule is established.
18. `tb_imbalance`: Critical financial alarm fired when Trial Balance debits != credits.
19. `transaction_mismatch`: Dispatched on settlement variance between CBS and mobile money/bank float.
20. `transactions_pending_approval`: Dispatched when a transaction breaches a teller's posting limit.
21. `upcoming_holidays`: Dispatched in advance of statutory holidays affecting loan schedules.
22. `updated_user_profile`: Dispatched when administrative permissions or limits change.

---
""")

    # 20. Audit and History Requirements
    sections.append("""## 20. Audit and History Requirements

### Immutability & Audit Trails
- **Audit Fields:** Every database entity must include `created_at`, `updated_at`, `created_by`, and `approved_by`.
- **System Audit Trail (`view audit trail`):** Comprehensive record capturing timestamp, actor user ID, client IP address, action performed, entity affected, old state, and new state.
- **Financial Immutability:** No financial transaction may be hard-deleted or updated in-place. Corrections must occur strictly via reversing entries (`reverse journal`, `reverse withdrawal`, `reverse deposit`, `reverse share purchase`).
- **Backdated Transaction Audit (`view backdated transactions`):** Specialized tracking for any entry posted with a historical value date.

---
""")

    # 21. Error Handling Requirements
    sections.append("""## 21. Error Handling Requirements

### Standard Error Taxonomy
- `400 Bad Request`: Input validation failure (`ERR_VALIDATION_FAILED`), duplicate unique field (`ERR_DUPLICATE_NIN`, `ERR_DUPLICATE_USERNAME`).
- `401 Unauthorized`: Missing, expired, or invalid bearer token (`ERR_SESSION_EXPIRED`, `ERR_UNAUTHENTICATED`).
- `403 Forbidden`:
  - Temporal restriction: `ERR_OUTSIDE_OPERATING_HOURS`.
  - Permission restriction: `ERR_PERMISSION_DENIED`.
  - Transaction limit restriction: `ERR_EXCEEDS_USER_LIMIT`.
  - Maker-checker violation: `ERR_CANNOT_APPROVE_OWN_RECORD`.
- `404 Not Found`: Target entity not found (`ERR_MEMBER_NOT_FOUND`).
- `409 Conflict`: Invalid state transition (`ERR_INVALID_STATUS_TRANSITION`), period already closed (`ERR_PERIOD_CLOSED`), Trial Balance imbalance (`ERR_TB_IMBALANCE`).
- `500 Internal Server Error`: Asynchronous job failure (`ERR_JOB_FAILED`), unexpected exception.

---
""")

    # 22. Security Requirements
    sections.append("""## 22. Security Requirements

### Observed Security Requirements
1. **Operating Hours Access Control:** Strictly enforced login and execution window per role.
2. **Idle Session Invalidation:** 5-minute timeout.
3. **Transaction Boundary Protection:** User min/max amount caps.
4. **Segregation of Duties (SoD):** Maker-checker separation for applications and transactions.
5. **Database Restoration Mode:** Dedicated administrative lock state (`activate restoration mode`).

### Recommended Security Enhancements
1. **Password Policy:** Minimum 12 characters, complexity requirements, Argon2id or bcrypt hashing.
2. **Rate Limiting:** IP and user-based throttling on authentication endpoints.
3. **Transport Security:** Strict TLS 1.3 encryption on all endpoints.
4. **Data Redaction:** PII and financial balances masked in audit log outputs.

---
""")

    # 23. Non-Functional Requirements
    sections.append("""## 23. Non-Functional Requirements

### Performance & Scalability
- **API Latency:** 95% of OLTP requests (deposits, balance checks) must respond in `< 250ms`.
- **Concurrency:** Support concurrent branch tellers and async mobile money webhooks without locking conflicts.
- **Reporting:** Heavy analytical and regulatory reports must execute against read-replicas or materialized views to prevent OLTP lockups.

### Availability & Resilience
- **Target Availability:** 99.9% uptime during operational branch hours.
- **Audit Durability:** Audit trail logs retained for minimum 7-year regulatory compliance window.

---
""")

    # 24. Cross-Page Findings
    sections.append("""## 24. Cross-Page Findings

1. **Vendor & Platform Identity:** Consistent branding across all headers: *"Savings Plus, Property of FutureLink Technologies | @2021 All rights reserved"*. FutureLink Technologies (FLT) provides the core banking platform engine.
2. **Misnamed Artifact:** `user_details.html` does not render an individual staff member's detail card; instead, it renders the exact form for editing a Role Profile (*"Edit Sp Data Migration Training"*), identical to `edit_role.html`.
3. **Shared Layout & Persistent State:** All 17 HTML files share an identical top status bar, navigation bar, idle detector widget, and failed job notification banner, indicating an Angular/Nuxt Single-Page Application shell.
4. **Entity Linkage:** Data created in `create_individual_member.html` flows directly into `list_member_applications.html` as a pending application, and subsequently appears in `list_members.html` upon approval.
5. **Currency Consistency:** The default currency across all balances and reports is Ugandan Shillings (`UGX`), with multi-currency / forex support configured in `/settings/institution/forex`.

---
""")

    # 25. Open Questions
    sections.append("""## 25. Open Questions

The following implementation specifics cannot be resolved strictly from the static HTML DOM and require stakeholder clarification:

| # | Subsystem | Open Question | Backend Impact |
|---|---|---|---|
| 1 | Regulatory Compliance | Is automated direct integration required for CRB inquiries (e.g. Metropol / Compuscan) or is it manual file upload? | External HTTP Client implementation vs static file storage |
| 2 | Biometrics / NIN | Does NIN verification connect directly to the NIRA (National Identification and Registration Authority) API or is it internal verification? | Integration adapter for NIRA gateway |
| 3 | Mobile Money Float | Are float balances polled via Telco APIs (MTN MoMo API, Airtel Money API) or updated via webhook callbacks from the MSACCO switch? | Asynchronous webhook listener vs scheduled balance polling worker |
| 4 | Accounting Engine | What is the exact Chart of Accounts structure (account code digit length, parent-child depth)? | Schema design of `chart_of_accounts` table |
| 5 | Storage Engine | Should uploaded documents (photos, signatures, IDs) be stored in local file storage or S3-compatible object storage? | Blob storage abstraction layer |
| 6 | Soft-Delete Policy | What is the permanent retention or purging lifecycle for soft-deleted users and customers? | Database cleanup / archiving jobs |

---
""")

    # 26. Assumptions
    sections.append("""## 26. Assumptions

1. **Database Engine:** PostgreSQL 15+ is assumed as the primary relational database to support JSONB attributes, transactions, and robust decimal precision.
2. **Statutory Jurisdiction:** The system operates under Ugandan legal and financial regulatory jurisdiction (BOU, UMRA, URA).
3. **Currency:** Primary accounting currency is `UGX` (Ugandan Shilling) with decimal precision set to 2 decimal places.
4. **Timezone:** Standard operating timezone is East Africa Time (EAT, UTC+3).
5. **Maker-Checker Strictness:** Maker-checker is mandatory for all member on-boarding, loan approvals, and transactions exceeding user-specific limits.

---
""")

    # 27. Traceability Matrix
    sections.append("""## 27. Traceability Matrix

| HTML Source File | Feature / Subsystem | Entities Involved | Requirement IDs | Evidence & Traceability |
|---|---|---|---|---|
| `create_member_step_one.html` | Member Type Selection | Customer | FR-001 | Cards for Individuals, Group, Joint, Institution |
| `create_individual_member.html` | Individual Onboarding | Customer, CustomerIndividual, NextOfKin, Employment, BankDetails | FR-002 | Complete 8-section form, file uploads, branch selection |
| `create_group.html` | Group Onboarding | Customer, CustomerGroup | FR-004 | Group Name, Registration number, Members/Non-members |
| `create_joint.html` | Joint Onboarding | Customer, CustomerJoint | FR-005 | Joint Name, shared addresses, loan limit toggle |
| `create_institution.html` | Institution Onboarding | Customer, CustomerInstitution | FR-006 | TIN, Business Type, Category, Ownership dropdowns |
| `list_members.html` | Member Directory | Customer, Branch | FR-001, FR-002 | Master table displaying members with phone, age, group |
| `list_groups.html` | Group Directory Tab | CustomerGroup | FR-004 | Groups filter subtab under /customers |
| `list_joints.html` | Joint Directory Tab | CustomerJoint | FR-005 | Joints filter subtab under /customers |
| `list_institutions.html` | Institution Directory Tab | CustomerInstitution | FR-006 | Institutions filter subtab under /customers |
| `list_member_applications.html` | Member Vetting Queue | Customer | FR-002, FR-003 | Pending / Rejected tabs, view application triggers |
| `list_agency_applications.html` | Agency Vetting Queue | AgencyApplication | FR-014 | Agent application pending / rejected tabs |
| `create_user.html` | User Provisioning | User, UserNotificationSubscription | FR-007 | Staff form, 9 Min/Max limit pairs, 22 alert checkboxes |
| `list_users.html` | User Administration | User | FR-008 | User cards, Deactivate, Delete, Enable SMS OTP buttons |
| `create_role.html` | Role Definition | Role, RolePermission | FR-009 | Role name, Open/Close time, 382 permission checkboxes |
| `edit_role.html` | Role Modification | Role, RolePermission | FR-009 | Permission editing and operating hour adjustments |
| `user_details.html` | Role Profile View | Role, RolePermission | FR-009 | Edit role profile UI misnamed as user_details.html |
| `list_roles.html` | Role Administration | Role | FR-009, FR-010 | Role cards, active user counts, operating hours |
| Global Top Bar / Shell | Liquidity & Alerts | ExternalFloat, Notification, Session | FR-011, FR-012, FR-013 | Float counters, Failed job alert, Idle detector |

---
""")

    # 28. Backend Implementation Considerations
    sections.append("""## 28. Backend Implementation Considerations

1. **Transactional Ledger Integrity:** Implement strict double-entry ledger primitives. Every financial event (deposit, withdrawal, disbursement, fee) must post balanced debit and credit entries to the general ledger within an isolated database transaction.
2. **Middleware Pipeline:**
   - `AuthenticationMiddleware`: Decodes JWT, validates active session.
   - `OperatingHoursMiddleware`: Evaluates current timestamp against user's role `open_time` and `close_time`.
   - `PermissionGuardMiddleware`: Checks route action against user's assigned role permission set.
   - `FinancialLimitInterceptor`: Checks transaction amount against user's min/max threshold.
3. **Asynchronous Processing:** Use a distributed task queue (e.g. Celery / BullMQ / Redis) for background jobs:
   - Interest accruals on savings and fixed deposits.
   - Batch standing order executions.
   - SMS and email notification dispatching.
   - Job health monitoring with automatic dispatch of `failed_job` alerts.

---
""")

    # 29. Final System Summary
    sections.append("""## 29. Final System Summary

The reverse-engineering of the 17 provided HTML artifacts establishes that **SavingsPlus** is an enterprise Core Banking System (CBS) designed for **NSIMBI SACCO LTD** by **FutureLink Technologies (FLT)**. 

The backend architecture requires:
- **Comprehensive Customer Information File (CIF):** Supporting Individuals, Groups, Joints, and Institutions with maker-checker onboarding.
- **Granular Security Framework:** 34 modules, 382 permissions, strict operating time windows, 5-minute idle timeouts, and individual financial limit ceilings across 9 transaction classes.
- **Integrated Operations:** End-to-end management of Savings, Loans, Shares, Accounting, Fixed Assets, Staff Payroll, External Float (MTN, Airtel, MSACCO, Bank), and Regulatory Compliance (BOU & UMRA).

This specification provides the exhaustive, unambiguous foundation for backend developers and automated coding agents to implement the database models, business logic services, and REST API controllers for the NSIMBI Core Banking System.
""")

    full_text = "
".join(sections)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write(full_text)
    
    # Also write a copy to ~/Downloads/MSACCO/REQUIREMENTS.md for user convenience
    downloads_copy = os.path.expanduser("~/Downloads/MSACCO/REQUIREMENTS.md")
    with open(downloads_copy, "w", encoding="utf-8") as f:
        f.write(full_text)

    print(f"REQUIREMENTS.md generated successfully at:
 - {OUTPUT_FILE}
 - {downloads_copy}")
    print(f"Total lines: {len(full_text.splitlines())}")

if __name__ == "__main__":
    build_requirements()

### FR-015 — Savings Account Directory & Status Filtering
- **Requirement:** The system shall list all customer savings accounts with account number, member name, branch, product, opened date, actual balance, available balance, and status filters (`All`, `Active`, `Inactive`, `Frozen`).
- **Entity:** `SavingsAccount`
- **Actor:** Cashier / Customer Care / Manager
- **Source:** `list_savings.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated staff.
- **Input:** Filter criteria (Status, Branch, Search term).
- **Business Rules:** Account number formatted as 12 digits (`003500005281`).
- **Expected Result:** Paginated list of savings accounts.
- **Failure Conditions:** Unauthorized access.
- **Evidence Level:** Explicit

### FR-016 — Savings Account Detail & Cash Desk Terminal
- **Requirement:** The system shall provide an operational cash desk screen for an individual savings account displaying Actual Balance, Available Balance, KYC photo/signature preview, and immediate transaction buttons: Deposit, Withdraw, New Transfers, Debit Account, Credit Account, Close, and Freeze.
- **Entity:** `SavingsAccount`, `SavingsTransaction`
- **Actor:** Cashier / Teller
- **Source:** `saving_account_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active savings account.
- **Input:** Account Number (`003500005281`).
- **Business Rules:** Displays Available Balance (`actual_balance - 5000 UGX reserve`).
- **Expected Result:** Detailed account ledger and action triggers.
- **Failure Conditions:** Account does not exist.
- **Evidence Level:** Explicit

### FR-017 — Savings SMS Alert Rules Configuration
- **Requirement:** The system shall allow operators to configure granular SMS transaction and loan notification subscriptions per savings account across 14 discrete triggers.
- **Entity:** `SavingsSMSAlertConfig`
- **Actor:** Customer Care / Staff
- **Source:** `edit_saving_account.html`, `saving_account_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target savings account exists.
- **Input:** Boolean flags for: On Deposit, On Withdrawal, Earning Interest, Earning Dividends, Outgoing Transfer, Incoming Transfer, Direct Debit, Direct Credit, Standing Order, Loan Application, Loan Disbursement, Loan Payment, Loan Refund.
- **Business Rules:** Saved flags dictate which account activities dispatch transactional SMS messages via the SMS gateway.
- **Expected Result:** SMS alert configuration persisted.
- **Failure Conditions:** Invalid account ID.
- **Evidence Level:** Explicit

### FR-018 — Fixed Deposit Placement & Booking
- **Requirement:** The system shall book term/fixed deposits, specifying principal amount, tenure in months, annual interest rate, interest payment interval, maturity payout option, and funding source (Cash counter vs Savings account offset).
- **Entity:** `FixedDeposit`, `SavingsAccount`
- **Actor:** Customer Care / Branch Manager
- **Source:** `create_fixed_deposit.html`
- **Priority:** MUST HAVE
- **Preconditions:** Member possesses an active savings account.
- **Input:** Member, Transfer/Payout Account, Depositor Name, Depositor Phone, Principal Amount, Starts On, Period in Months, Interest Rate (%), Interest Interval (`monthly`, `yearly`, `Not applicable`), Payout Option (`Principal`, `Principal & Interest`), Funding Method (`Till - Cash or cheque`, `Offset - From savings`).
- **Business Rules:** If offset selected, funds immediately placed on hold/debited from source savings account. Enters Maker-Checker approval queue (`list_fixed_deposit_applications.html`).
- **Expected Result:** Fixed deposit contract created in `PENDING` status.
- **Failure Conditions:** Insufficient available balance in source savings account.
- **Evidence Level:** Explicit

### FR-019 — Fixed Deposit Lifecycle & Termination
- **Requirement:** The system shall track fixed deposits through their complete lifecycle and support both premature and maturity termination with interest calculation.
- **Entity:** `FixedDeposit`
- **Actor:** Branch Manager
- **Source:** `list_fixed_deposits.html`, `fixed_deposit_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Fixed deposit in `RUNNING` or `MATURED_NOT_AWARDED` status.
- **Input:** Deposit ID, Action (`TERMINATE`), Interest Decision (`WITH_INTEREST`, `NO_INTEREST`).
- **Business Rules:** Terminating returns principal (and approved interest) to the designated transfer savings account. Status transitions to `AWARDED_ON_MATURITY`, `TERMINATED_NO_INTEREST`, or `TERMINATED_WITH_INTEREST`.
- **Expected Result:** Funds transferred; contract archived.
- **Failure Conditions:** Contract already liquidated.
- **Evidence Level:** Explicit

### FR-020 — Standing Orders Recurring Execution
- **Requirement:** The system shall maintain standing orders with automated periodic execution and track statuses: `Pending`, `Approved`, `Running`, `Rejected`, `Expired`.
- **Entity:** `StandingOrder`
- **Actor:** Customer Care / Scheduler
- **Source:** `list_standing_orders.html`
- **Priority:** MUST HAVE
- **Preconditions:** Source savings account active.
- **Input:** Source Account, Destination Account, Amount, Frequency, Start Date.
- **Business Rules:** Background worker sweeps source account on execution date. If balance insufficient, records failed execution and dispatches alert `standing_orders_pending_approval`.
- **Expected Result:** Transfer posted between accounts.
- **Failure Conditions:** Expired order or insufficient balance.
- **Evidence Level:** Explicit

### FR-021 — Debit Card Issuance Tracking
- **Requirement:** The system shall manage the requisition, approval, and issuance of co-branded Interswitch debit cards linked to member savings accounts.
- **Entity:** `DebitCardIssuance`
- **Actor:** Customer Care / Branch Manager
- **Source:** `list_debit_card_issuance.html`, `saving_account_detail.html`
- **Priority:** SHOULD HAVE
- **Preconditions:** Active savings account.
- **Input:** Savings Account Number, Card PAN / Serial Number.
- **Business Rules:** Card status tracks through `Pending` -> `Approved` -> `Issued` -> `Rejected`.
- **Expected Result:** Card linked for ATM/POS debit transactions.
- **Failure Conditions:** Account frozen or inactive.
- **Evidence Level:** Explicit

### FR-022 — Loan Amortization Calculator
- **Requirement:** The system shall provide an interactive loan amortization calculator supporting multiple interest calculation methods (`flat`, `declining`, `amortize`) and frequencies (`daily`, `weekly`, `monthly`, `annually`), generating installment amounts and repayment schedules.
- **Entity:** `LoanProduct`, `LoanRepaymentSchedule`
- **Actor:** Loan Officer / Customer
- **Source:** `loan_calculator.html`
- **Priority:** MUST HAVE
- **Preconditions:** Loan products configured in system.
- **Input:** Product ID, Principal Amount, Interest Method, Annual Interest Rate, Frequency, Number of Installments, Start Date, Grace Period (Months).
- **Business Rules:** Generates accurate mathematical repayment schedule preview.
- **Expected Result:** Installment amount and period-by-period breakdown of Principal, Interest, and Remaining Balance.
- **Failure Conditions:** Negative principal or rate.
- **Evidence Level:** Explicit

### FR-023 — Credit Reference Bureau (CRB / CBA) Vetting
- **Requirement:** The system shall integrate credit reporting workflows allowing officers to record borrower consent and perform credit vetting against Credit Bureau Africa (CBA).
- **Entity:** `CreditReportingInquiry`
- **Actor:** Loan Officer
- **Source:** `list_credit_reporting.html`
- **Priority:** MUST HAVE
- **Preconditions:** Borrower signed CRB consent form (`initiate crb report consent`).
- **Input:** Member NIN / Account Number, Bureau Identifier (`CBA`).
- **Business Rules:** Credit check results logged in bureau register (`view crb register`) prior to loan approval.
- **Expected Result:** Credit history report and risk rating returned.
- **Failure Conditions:** Bureau service unavailable.
- **Evidence Level:** Explicit

### FR-024 — Loan Servicing, Auto-Pay & Repayments
- **Requirement:** The system shall manage active loan contracts, displaying disbursement date, maturity date, interest rate (e.g. `42.0%`), loan officer, and linked repayment account, supporting manual payment posting and automated savings sweeps.
- **Entity:** `Loan`, `SavingsAccount`, `LoanRepayment`
- **Actor:** Loan Officer / Cashier / System Scheduler
- **Source:** `loan_detail.html`, `list_loans.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active loan contract in `RUNNING` status.
- **Input:** Loan Number (`1D04FE1229`), Payment Amount, Payment Mode.
- **Business Rules:** If `auto_pay_from_savings=true`, system automatically debits linked savings account (`003500000651`) on due date.
- **Expected Result:** Installment credited against outstanding interest and principal.
- **Failure Conditions:** Delinquent repayment incurs late penalties.
- **Evidence Level:** Explicit

### FR-025 — Loan Restructuring, Waivers & Legal Recovery
- **Requirement:** The system shall support credit risk interventions including interest waivers, penalty waivers, admin fee waivers, grace period additions, officer re-assignments, loan restructuring/rescheduling (generating new loan numbers referencing original facility), legal referrals (`Send to legal`), and bad debt write-offs (`Write off`).
- **Entity:** `Loan`
- **Actor:** Credit Manager
- **Source:** `loan_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Delinquent or performing loan requiring administrative modification.
- **Input:** Loan Number, Action (`WAIVE_INTEREST`, `WAIVE_PENALTY`, `WAIVE_ADMIN_FEES`, `RESCHEDULE`, `ADD_GRACE_PERIOD`, `TRANSFER_OFFICER`, `SEND_TO_LEGAL`, `WRITE_OFF`), Justification Notes, Adjusted Terms.
- **Business Rules:** Waivers post corresponding reversing entries. Rescheduling closes old loan and books new loan contract. Write-offs post against Loan Loss Provision accounts.
- **Expected Result:** Loan contract state and ledger updated.
- **Failure Conditions:** Insufficient authorization limits.
- **Evidence Level:** Explicit

---
""")

    # 15. API Requirements
    s.append("""## 15. API Requirements

### Extended API Catalog (Incorporating Savings & Loans)
- `GET /savings` — Query savings accounts with status filters (`active`, `inactive`, `frozen`).
- `GET /savings/{number}` — Fetch detailed account overview, available vs actual balance, and recent ledger entries.
- `POST /savings/{number}/deposit` — Post cash or counter deposit to savings account.
- `POST /savings/{number}/create-individual-withdrawal` — Post cash withdrawal from savings account.
- `POST /savings/{number}/direct-transfers` — Execute inter-account transfer.
- `POST /savings/{number}/freeze` / `POST /savings/{number}/unfreeze` — Toggle administrative hold.
- `POST /savings/{number}/close` — Liquidate balance and close account.
- `PATCH /savings/{number}/alerts` — Update 14 SMS alert rule checkboxes.
- `GET /savings/applications` — Fetch pending savings account opening applications.
- `POST /savings/applications/{id}/approve` — Approve savings account opening.
- `GET /savings/fixed-deposits` — List term deposits with 7 lifecycle status filters.
- `POST /savings/fixed-deposits` — Book new fixed deposit (Till or Savings Offset).
- `GET /savings/fixed-deposits/{id}` — Fetch fixed deposit terms and certificate data.
- `POST /savings/fixed-deposits/{id}/terminate` — Liquidate fixed deposit before or at maturity.
- `GET /savings/standing-orders` — List recurring standing orders.
- `POST /savings/{number}/standing-orders/create` — Setup new recurring standing order.
- `GET /savings/debit-cards` — List debit card issuance requests and statuses.
- `POST /savings/{number}/debit-cards` — Request Interswitch debit card issuance.
- `GET /loans` — List active loan facilities with portfolio filters (`outstanding`, `due`, `performing`, `sent_to_legal`).
- `GET /loans/{number}` — Fetch full loan details, linked repayment account, and amortization schedule.
- `POST /loans/calculator` — Calculate amortization schedule based on method (`flat`, `declining`, `amortize`).
- `GET /loans/applications` — Fetch loan origination pipeline (`approved`, `pending`, `rejected`, `cancelled`).
- `POST /loans/applications` — Submit new loan origination application.
- `POST /loans/applications/{id}/approve` — Sanction loan application.
- `POST /loans/applications/{id}/disburse` — Disburse approved funds into member savings account.
- `POST /loans/{number}/repayments` — Post manual loan repayment.
- `POST /loans/{number}/waiver-interest` — Waive accrued interest.
- `POST /loans/{number}/waiver-penalty` — Waive late payment penalties.
- `POST /loans/{number}/reschedule` — Restructure loan and issue new facility reference.
- `POST /loans/{number}/send-to-legal` — Forward defaulted loan to legal debt collection.
- `POST /loans/{number}/write-off` — Write off bad debt against loan loss provisions.
- `GET /loans/credit-reporting` — Query credit bureau history (CBA).

---
""")

    # 16. Data / Database Requirements
    s.append("""## 16. Data / Database Requirements

### Schema Additions for Savings and Loans
```sql
-- Savings Products Table
CREATE TABLE savings_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'FlexSave'
    code VARCHAR(20) UNIQUE NOT NULL,
    minimum_balance DECIMAL(18,2) NOT NULL DEFAULT 5000.00,
    opening_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_rate_annual DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Savings Accounts Table
CREATE TABLE savings_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(30) UNIQUE NOT NULL, -- e.g. '003500005281'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    product_id INTEGER NOT NULL REFERENCES savings_products(id),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED')),
    actual_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    opening_fee_charged BOOLEAN NOT NULL DEFAULT FALSE,
    last_ledger_fee_charged_at TIMESTAMP WITH TIME ZONE,
    last_transaction_at TIMESTAMP WITH TIME ZONE,
    opened_at DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Savings SMS Alert Rules
CREATE TABLE savings_sms_alert_configs (
    account_id BIGINT PRIMARY KEY REFERENCES savings_accounts(id) ON DELETE CASCADE,
    notify_on_deposit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_withdrawal BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_earning_interest BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_earning_dividends BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_outgoing_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_incoming_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_debit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_credit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_standing_order BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_application BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_loan_disbursement BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_payment BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_refund BOOLEAN NOT NULL DEFAULT FALSE
);

-- Fixed Deposits Table
CREATE TABLE fixed_deposits (
    id BIGSERIAL PRIMARY KEY,
    deposit_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    funding_source VARCHAR(30) NOT NULL CHECK (funding_source IN ('TILL_CASH', 'SAVINGS_OFFSET')),
    funding_savings_account_id BIGINT REFERENCES savings_accounts(id),
    payout_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    depositor_name VARCHAR(150) NOT NULL,
    depositor_phone VARCHAR(30) NOT NULL,
    principal_amount DECIMAL(18,2) NOT NULL,
    starts_on DATE NOT NULL,
    period_months INTEGER NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_interval VARCHAR(30) NOT NULL CHECK (interest_interval IN ('monthly', 'yearly', 'Not applicable')),
    payout_option VARCHAR(50) NOT NULL CHECK (payout_option IN ('Principal', 'Principal & Interest')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RUNNING', 'AWARDED', 'TERMINATED', 'MATURED_NOT_AWARDED', 'TERMINATED_NO_INTEREST', 'AWARDED_ON_MATURITY', 'TERMINATED_WITH_INTEREST')),
    matures_at DATE NOT NULL,
    accrued_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Standing Orders Table
CREATE TABLE standing_orders (
    id BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    destination_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    amount DECIMAL(18,2) NOT NULL,
    frequency VARCHAR(30) NOT NULL CHECK (frequency IN ('daily', 'weekly', 'monthly')),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'RUNNING', 'REJECTED', 'EXPIRED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Products Table
CREATE TABLE loan_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'NCHIEI Credit', 'Business Loans'
    code VARCHAR(20) UNIQUE NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    default_annual_interest_rate DECIMAL(5,2) NOT NULL,
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    min_principal DECIMAL(18,2) NOT NULL,
    max_principal DECIMAL(18,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Loans Table
CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    loan_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '1D04FE1229'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    loan_officer_id BIGINT NOT NULL REFERENCES users(id),
    product_id INTEGER NOT NULL REFERENCES loan_products(id),
    repayment_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    principal_disbursed DECIMAL(18,2) NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    installments_count INTEGER NOT NULL,
    grace_period_months INTEGER NOT NULL DEFAULT 0,
    installment_amount DECIMAL(18,2) NOT NULL,
    auto_pay_from_savings BOOLEAN NOT NULL DEFAULT TRUE,
    disbursed_at DATE NOT NULL,
    maturity_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING', 'PERFORMING', 'DUE', 'OUTSTANDING', 'SENT_TO_LEGAL', 'WRITTEN_OFF', 'RESCHEDULED', 'CLEARED')),
    rescheduled_from_loan_number VARCHAR(50),
    total_principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_penalty_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_penalty DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_admin_fees DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Repayment Schedules Table
CREATE TABLE loan_repayment_schedules (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(18,2) NOT NULL,
    interest_due DECIMAL(18,2) NOT NULL,
    total_installment DECIMAL(18,2) NOT NULL,
    principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    is_settled BOOLEAN NOT NULL DEFAULT FALSE
);
```

---
""")

    # 17 to 26 - Retain & Expand
    s.append("""## 17. Search, Filtering and Reporting

### Savings & Loan Specific Reporting Capabilities
From the 109 cataloged reports, the following are directly confirmed by the savings and loan captures:
- `view savings accounts`, `view savings ledger`, `view savings transactions`, `view failed standing orders`.
- `view fixed deposits`, `view fixed certificate`.
- `view loan applications`, `view loan arrears`, `view loan bookings`, `view loan schedule`, `view loan ledger`, `view loan repayments`, `view loan repayment summary`, `view cleared loans`, `view due loans`, `view written off loans`, `view rescheduled loans`.
- `view par by ageing`, `view par by ageing summary`, `view umra par by ageing` (Portfolio At Risk aging schedules for regulatory reporting).
- `view crb register`, `download crb files` (Credit Reference Bureau returns).

---

### FR-026 — Chart of Accounts Hierarchy Management
- **Requirement:** The system shall maintain a 5-tier hierarchical Chart of Accounts (Assets, Liabilities, Capital, Income, Expenses) with dash-separated account codes, supporting node creation, updates, and deactivation.
- **Entity:** `ChartOfAccount`
- **Actor:** Head of Finance / Accountant
- **Source:** `list_charts_of_accounts_assets.html`, `create_chart_of_account_asset.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated user with `register chart account` permission.
- **Input:** Account Name*, Parent Account Node, Subtype (Optional), Tags (Optional).
- **Business Rules:** System generates dash-separated hierarchical code; cannot deactivate an account with a non-zero balance.
- **Expected Result:** New GL node created in Chart of Accounts.
- **Failure Conditions:** Duplicate account code.
- **Evidence Level:** Explicit

### FR-027 — Double-Entry Journal Voucher Creation & Reversal
- **Requirement:** The system shall capture balanced double-entry Journal Vouchers across branches and ledger accounts, supporting maker-checker reversal workflows.
- **Entity:** `JournalVoucher`, `JournalVoucherItem`
- **Actor:** Accountant / Accounts Assistant
- **Source:** `create_journal.html`, `journals.html.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid active accounts in Chart of Accounts.
- **Input:** Contact Person*, Registration Date*, Reference Number*, Method (`cash`, `cheque`)*, Comment*, Debit Account*, Credit Account*, Amount*.
- **Business Rules:** Total debits must equal total credits; reversals must reference original voucher and provide justification.
- **Expected Result:** Journal voucher posted to General Ledger or queued for approval.
- **Failure Conditions:** Debits != Credits (`ERR_TB_OUT_OF_BALANCE`).
- **Evidence Level:** Explicit

### FR-028 — Financial Period Closing, TB Archival & Rollback Protection
- **Requirement:** The system shall close financial periods, generate an immutable Trial Balance snapshot PDF, lock the period against backdated transactions, and allow authorized rollback.
- **Entity:** `FinancialPeriod`
- **Actor:** Head of Finance / Administrator
- **Source:** `closing_and_restore_books.html`
- **Priority:** MUST HAVE
- **Preconditions:** All subledgers reconciled; Trial Balance in balance.
- **Input:** Period End Date, Action (`CLOSE`, `ROLLBACK`).
- **Business Rules:** Closing generates `Download TB(pdf)` snapshot; rollback triggers alert `opening_a_closed_period`.
- **Expected Result:** Period locked or reopened.
- **Failure Conditions:** Out of balance Trial Balance (`tb_imbalance`).
- **Evidence Level:** Explicit

### FR-029 — Financial Approvals Maker-Checker Queue
- **Requirement:** The system shall provide an approval queue for accounting transactions, journal vouchers, and bank transfers with pending, rejected, and approved queues.
- **Entity:** `FinancialApproval`
- **Actor:** Branch Manager / Head of Finance
- **Source:** `approvals.html`
- **Priority:** MUST HAVE
- **Preconditions:** Transaction submitted by maker.
- **Input:** Approval ID, Decision (`APPROVE`, `REJECT`), Reason.
- **Business Rules:** Checker cannot be maker (BR-001).
- **Expected Result:** Transaction committed to ledger or rejected.
- **Failure Conditions:** Checker is maker.
- **Evidence Level:** Explicit

### FR-030 — Fixed Asset Register & Depreciation
- **Requirement:** The system shall maintain a register of physical assets and asset profiles, supporting periodic depreciation calculation, revaluations, and disposals.
- **Entity:** `FixedAsset`, `AssetProfile`
- **Actor:** Accountant
- **Source:** `list_fixed_assets.html`, `list_asset_profiles.html`
- **Priority:** SHOULD HAVE
- **Preconditions:** Asset profile defined.
- **Input:** Asset Name, Profile, Purchase Date, Cost, Salvage Value, Useful Life.
- **Business Rules:** Depreciation posts monthly debit to Depreciation Expense and credit to Accumulated Depreciation.
- **Expected Result:** Asset tracked on balance sheet.
- **Failure Conditions:** Invalid profile.
- **Evidence Level:** Explicit

### FR-031 — Member Share Capital Administration
- **Requirement:** The system shall manage member share equity accounts, recording share capital subscriptions, purchases, transfers, and liquidations.
- **Entity:** `ShareAccount`, `ShareTransaction`
- **Actor:** Customer Care / Accountant
- **Source:** `list_shares.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active member.
- **Input:** Member ID, Number of Shares, Unit Price.
- **Business Rules:** Total share capital reflects under Capital tier of Chart of Accounts.
- **Expected Result:** Member share balance updated.
- **Failure Conditions:** Member inactive.
- **Evidence Level:** Explicit

### FR-032 — Password-Authenticated Share Dividend Payout
- **Requirement:** The system shall declare and distribute share dividends based on shareholding periods, requiring staff password re-authentication, supporting distribution directly into savings accounts or equity capitalization.
- **Entity:** `DividendDeclaration`
- **Actor:** Head of Finance / General Manager
- **Source:** `share_dividends.html`
- **Priority:** MUST HAVE
- **Preconditions:** Approved dividend pool; member shareholdings verified.
- **Input:** Amount*, Use Percentages*, Date*, Share Period (Months)*, Method (`savings`, `shares`)*, Saving Product, Reference Number*, Comment*, Password*.
- **Business Rules:** Password re-verified; credits destination savings product (`FlexSave`) or increases member shares held.
- **Expected Result:** Dividend disbursed across all qualifying members; event logged.
- **Failure Conditions:** Incorrect password (`ERR_INVALID_PASSWORD`).
- **Evidence Level:** Explicit

### FR-033 — Multi-Entity Batch Data Import Engine
- **Requirement:** The system shall provide batch data ingestion with downloadable templates and preview validation across 9 entity types: Individuals, Institutions, Joint Accounts, Groups, Group Members, Loans, Fixed Deposits, Savings, and savingsAccounts.
- **Entity:** `DataImportBatch`
- **Actor:** System Administrator / Migration Specialist
- **Source:** `imports.html`, `preview_import.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid CSV template.
- **Input:** Template Type*, Branch*, Reference Number*, Document File*.
- **Business Rules:** Preview validates column mappings and schema errors prior to commit.
- **Expected Result:** Bulk records ingested into respective database tables.
- **Failure Conditions:** File parsing failure or validation mismatch.
- **Evidence Level:** Explicit

---
""")

    # 15. API Requirements
    sections.append("""## 15. API Requirements

Based on explicit form submissions, list page interactions, and the 190 discovered routes in the client bundle, the following RESTful API surface is required:

### Authentication & Session Subsystem
- `POST /api/auth/login`
  - **Purpose:** Authenticate staff credentials.
  - **Payload:** `{ username: "...", password: "..." }`
  - **Response:** `{ token: "...", refresh_token: "...", expires_in: 300, user: { ... } }`
  - **Validation & Rules:** Rate-limited; verifies `is_active=true`, `is_blocked=false`, and temporal window `open_time <= now <= close_time`.
- `POST /api/auth/logout`
  - **Purpose:** Invalidate current session token.
- `GET /api/auth/user`
  - **Purpose:** Return current authenticated user profile, permissions, and assigned limits.
- `POST /api/auth/refresh`
  - **Purpose:** Issue fresh JWT against valid refresh token.

### Customers & Membership Subsystem
- `GET /customers`
  - **Purpose:** Query paginated customer directory with filtering.
  - **Query Params:** `type` (individual, group, joint, institution), `branch_id`, `search`, `page`, `per_page`.
- `POST /customers/individuals`
  - **Purpose:** Submit new individual member application.
  - **Payload:** Multipart/form-data containing all Bio Data, Contact, Kin, Employment, Bank details, and uploaded binary files (signature, ID, photo).
  - **Response:** 201 Created `{ id: 1223, member_number: "00350000519", status: "PENDING" }`
- `POST /customers/groups`
  - **Purpose:** Register group member.
  - **Payload:** `{ branch_id, group_name, group_type, registration_number, entry_date, savings_officer_id, phone, address, ... }`
- `POST /customers/joints`
  - **Purpose:** Register joint account entity.
- `POST /customers/institutions`
  - **Purpose:** Register corporate/institutional entity.
  - **Payload:** `{ institution_name, registration_number, registration_date, tin_number, business_type, institution_category, ... }`
- `GET /customers/applications`
  - **Purpose:** Fetch maker-checker queue for member applications.
  - **Query Params:** `status=pending` or `status=rejected`, `branch_id`, `page`.
- `POST /customers/applications/{id}/approve`
  - **Purpose:** Approve pending member application and activate ledgers.
- `POST /customers/applications/{id}/reject`
  - **Purpose:** Reject pending member application with reason notes.
- `GET /customers/agent-applications`
  - **Purpose:** Fetch agency banking applications queue.

### Users & Security Subsystem
- `GET /users`
  - **Purpose:** Fetch list of system users.
  - **Query Params:** `status` (all, active, inactive), `search`, `page`.
- `POST /users`
  - **Purpose:** Provision new user account with transaction limits and notification subscriptions.
  - **Payload:** `{ first_name, last_name, username, email, password, phone, role_id, branch_id, station_id, limits: { ... }, notification_subscriptions: [ ... ] }`
- `PATCH /users/{id}`
  - **Purpose:** Update user details, limits, or assign new role.
- `POST /users/{id}/deactivate`
  - **Purpose:** Deactivate active user.
- `DELETE /users/{id}`
  - **Purpose:** Soft-delete user account.
- `POST /users/{id}/toggle-sms-otp`
  - **Purpose:** Enable or disable 2FA requirement.

### Roles & RBAC Subsystem
- `GET /users/roles`
  - **Purpose:** List all role profiles with assigned user counts and operating hours.
  - **Query Params:** `status` (all, active, inactive).
- `POST /users/roles`
  - **Purpose:** Create new role profile.
  - **Payload:** `{ role_name: "...", open_time: "00:00:00", close_time: "23:00:00", permissions: [ "register deposit", ... ] }`
- `GET /users/roles/{id}`
  - **Purpose:** Retrieve role definition and complete array of assigned permissions.
- `PATCH /users/roles/{id}`
  - **Purpose:** Update role permissions or operating window.
- `POST /users/roles/{id}/activate` / `POST /users/roles/{id}/deactivate`
  - **Purpose:** Toggle operational availability of role.
- `DELETE /users/roles/{id}`
  - **Purpose:** Delete unassigned role.

### Notifications & Float Subsystem
- `GET /notifications`
  - **Purpose:** Fetch current user's unread notifications.
- `POST /notifications/{id}/mark-read`
  - **Purpose:** Mark notification as read.
- `POST /notifications/mark-all-read`
  - **Purpose:** Mark all unread notifications as read.
- `GET /settings/float-balances`
  - **Purpose:** Fetch latest float balances for top-bar indicators (MSACCO, MTN, Airtel, Bank Transfer).

---
""")

    # 16. Data / Database Requirements
    sections.append("""## 16. Data / Database Requirements

### Conceptual Data Model
The database architecture must be a relational database (PostgreSQL recommended) enforcing ACID compliance and double-entry ledger immutability.

```sql
-- Core Branches Table
CREATE TABLE branches (
    id SERIAL PRIMARY KEY,
    branch_code VARCHAR(10) UNIQUE NOT NULL, -- e.g. '0035'
    name VARCHAR(100) NOT NULL, -- e.g. 'KABALE'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Core Roles Table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    open_time TIME NOT NULL DEFAULT '00:00:00',
    close_time TIME NOT NULL DEFAULT '23:59:59',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Role Permissions Table
CREATE TABLE role_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_key VARCHAR(100) NOT NULL,
    UNIQUE(role_id, permission_key)
);

-- Core Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    role_id INTEGER NOT NULL REFERENCES roles(id),
    station_id VARCHAR(50), -- e.g. 'Teller - 1'
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    sms_otp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    
    -- Maker-Checker Financial Limits
    loan_approval_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    loan_approval_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- User Notification Subscriptions Table
CREATE TABLE user_notification_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_key VARCHAR(100) NOT NULL,
    UNIQUE(user_id, event_key)
);

-- Master Customers Table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    member_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '00350000001'
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('INDIVIDUAL', 'GROUP', 'JOINT', 'INSTITUTION')),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    savings_officer_id BIGINT REFERENCES users(id),
    entry_date DATE NOT NULL,
    phone_primary VARCHAR(30) NOT NULL,
    phone_secondary VARCHAR(30),
    email VARCHAR(150),
    country VARCHAR(100) NOT NULL DEFAULT 'Uganda',
    town VARCHAR(100),
    physical_address TEXT NOT NULL,
    postal_code VARCHAR(30),
    postal_address VARCHAR(150),
    has_loan_limit BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'REJECTED', 'INACTIVE', 'DORMANT')),
    created_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Individual Profile Table
CREATE TABLE customer_individuals (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    salutation VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female')),
    marital_status VARCHAR(20) NOT NULL,
    is_dependent BOOLEAN NOT NULL DEFAULT FALSE,
    is_pwd BOOLEAN NOT NULL DEFAULT FALSE,
    date_of_birth DATE NOT NULL,
    country_of_birth VARCHAR(100) NOT NULL,
    nationality VARCHAR(100) NOT NULL DEFAULT 'Ugandan',
    home_ownership VARCHAR(20) NOT NULL CHECK (home_ownership IN ('Owner', 'Tenant', 'Other')),
    identification_type VARCHAR(50) NOT NULL,
    identification_number VARCHAR(100) NOT NULL,
    card_number VARCHAR(100),
    signature_file_path TEXT,
    identification_file_path TEXT,
    photo_file_path TEXT
);

-- Next of Kin Table
CREATE TABLE customer_next_of_kin (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    second_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    physical_address TEXT NOT NULL,
    relationship VARCHAR(30) NOT NULL
);

-- Group Profile Table
CREATE TABLE customer_groups (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    group_name VARCHAR(150) NOT NULL,
    group_type VARCHAR(50) NOT NULL CHECK (group_type IN ('Members', 'Non Members')),
    registration_number VARCHAR(100) NOT NULL
);

-- Joint Profile Table
CREATE TABLE customer_joints (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    joint_name VARCHAR(150) NOT NULL
);

-- Institution Profile Table
CREATE TABLE customer_institutions (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    institution_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100) NOT NULL,
    registration_date DATE NOT NULL,
    tin_number VARCHAR(50) NOT NULL,
    business_type VARCHAR(100) NOT NULL,
    institution_category VARCHAR(100) NOT NULL,
    residence_ownership VARCHAR(50) NOT NULL
);
```

---
""")

    # 17. Search, Filtering and Reporting
    sections.append("""## 17. Search, Filtering and Reporting

### Search Capabilities
- Master member search indexing across: Member Number, Name, Phone Number, and Group Affiliation.
- User search indexing: Username, First Name, Last Name, and Branch.

### Filtering Dimensions
- Status Filters: `All`, `Active`, `Inactive`, `Pending`, `Rejected`.
- Organizational Filters: Branch (`0035 KABALE`), Officer assignment, Customer Subtype.
- Date Range Filters: Registration date, approval date, transaction value date.

### Regulatory & Management Reports Catalog
The system specifies 109 discrete reporting capabilities including:
1. **Central Bank / BOU Returns:** Bank of Uganda Liquidity Return (`bou liquidity`), Bank of Uganda Banking Supervision Return (`bou bsa`).
2. **UMRA Returns:** Uganda Microfinance Regulatory Authority Balance Sheet (`umra balance sheet`), Income Statement (`umra income statement`), and PAR by Aging (`umra par by ageing`).
3. **Credit Operations Reports:** Loan Arrears vs Savings, Portfolio Monitoring, Cleared Loans, Due Loans, Written-off Loans, CRB Registers.
4. **General Ledger & Accounting:** Balance Sheet, Profit & Loss / Income Statement, Cash Flow Statement, Trial Balance, Backdated Transactions, Till Sheet Reconciliation.
5. **Shares & Equity:** Cumulative Shares, Shares Ledger, Dividends Shared Report.

---
""")

    # 18. File and Document Requirements
    sections.append("""## 18. File and Document Requirements

### Upload Specifications
1. **Signature File:** Uploaded during individual registration (`Signature*`). Stored securely; rendered on teller screens for withdrawal verification.
2. **Identification Document Scan:** Uploaded ID card/passport scan (`Indentification*`).
3. **Profile Photo:** Member portrait photograph (`Photo*`).
4. **Bulk Processing Files:** Bulk payroll/disbursement files (`.csv`, `.xlsx`) uploaded for batch processing.
5. **Database Backups:** Compressed database backup files downloadable by administrators (`download backup`, `restore backup`).

---
""")

    # 19. Notification and Event Requirements
    sections.append("""## 19. Notification and Event Requirements

### The 22 Core Notification Triggers
The backend event dispatcher must route notifications based on user subscriptions:
1. `blocked_user`: Dispatched when an account is locked out after consecutive failed authentications.
2. `bulk_files_pending_approval`: Dispatched when an institutional disbursement file is uploaded.
3. `customer_applications_pending_approval`: Dispatched when a member registration form is submitted.
4. `customer_kyc_updated`: Dispatched when member biodata or identity documents are altered.
5. `failed_job`: Dispatched immediately when an asynchronous background job errors out.
6. `fixed_deposit_applications_pending_approval`: Dispatched on term deposit placement.
7. `license_expiry_reminder`: Dispatched when software license term nears expiration.
8. `license_upgraded`: Dispatched when license tier or capacity is expanded.
9. `liquid_assets_balance_running_low`: Dispatched when branch cash or float balances breach minimum reserve.
10. `loan_applications_pending_approval`: Dispatched when a loan officer submits an application.
11. `loan_applications_pending_disbursement`: Dispatched when an approved loan is ready for payout.
12. `msacco_customer_onboarding`: Dispatched when a member self-registers via mobile USSD/App.
13. `msacco_loan_application`: Dispatched on mobile emergency loan request.
14. `opening_a_closed_period`: High-priority alert triggered if an accounting period is unsealed.
15. `reserve_amount_limit`: Dispatched when statutory reserves breach threshold.
16. `savings_account_applications_pending_approval`: Dispatched on savings account opening.
17. `standing_orders_pending_approval`: Dispatched when a recurring payment rule is established.
18. `tb_imbalance`: Critical financial alarm fired when Trial Balance debits != credits.
19. `transaction_mismatch`: Dispatched on settlement variance between CBS and mobile money/bank float.
20. `transactions_pending_approval`: Dispatched when a transaction breaches a teller's posting limit.
21. `upcoming_holidays`: Dispatched in advance of statutory holidays affecting loan schedules.
22. `updated_user_profile`: Dispatched when administrative permissions or limits change.

---
""")

    # 20. Audit and History Requirements
    sections.append("""## 20. Audit and History Requirements

### Immutability & Audit Trails
- **Audit Fields:** Every database entity must include `created_at`, `updated_at`, `created_by`, and `approved_by`.
- **System Audit Trail (`view audit trail`):** Comprehensive record capturing timestamp, actor user ID, client IP address, action performed, entity affected, old state, and new state.
- **Financial Immutability:** No financial transaction may be hard-deleted or updated in-place. Corrections must occur strictly via reversing entries (`reverse journal`, `reverse withdrawal`, `reverse deposit`, `reverse share purchase`).
- **Backdated Transaction Audit (`view backdated transactions`):** Specialized tracking for any entry posted with a historical value date.

---
""")

    # 21. Error Handling Requirements
    sections.append("""## 21. Error Handling Requirements

### Standard Error Taxonomy
- `400 Bad Request`: Input validation failure (`ERR_VALIDATION_FAILED`), duplicate unique field (`ERR_DUPLICATE_NIN`, `ERR_DUPLICATE_USERNAME`).
- `401 Unauthorized`: Missing, expired, or invalid bearer token (`ERR_SESSION_EXPIRED`, `ERR_UNAUTHENTICATED`).
- `403 Forbidden`:
  - Temporal restriction: `ERR_OUTSIDE_OPERATING_HOURS`.
  - Permission restriction: `ERR_PERMISSION_DENIED`.
  - Transaction limit restriction: `ERR_EXCEEDS_USER_LIMIT`.
  - Maker-checker violation: `ERR_CANNOT_APPROVE_OWN_RECORD`.
- `404 Not Found`: Target entity not found (`ERR_MEMBER_NOT_FOUND`).
- `409 Conflict`: Invalid state transition (`ERR_INVALID_STATUS_TRANSITION`), period already closed (`ERR_PERIOD_CLOSED`), Trial Balance imbalance (`ERR_TB_IMBALANCE`).
- `500 Internal Server Error`: Asynchronous job failure (`ERR_JOB_FAILED`), unexpected exception.

---
""")

    # 22. Security Requirements
    sections.append("""## 22. Security Requirements

### Observed Security Requirements
1. **Operating Hours Access Control:** Strictly enforced login and execution window per role.
2. **Idle Session Invalidation:** 5-minute timeout.
3. **Transaction Boundary Protection:** User min/max amount caps.
4. **Segregation of Duties (SoD):** Maker-checker separation for applications and transactions.
5. **Database Restoration Mode:** Dedicated administrative lock state (`activate restoration mode`).

### Recommended Security Enhancements
1. **Password Policy:** Minimum 12 characters, complexity requirements, Argon2id or bcrypt hashing.
2. **Rate Limiting:** IP and user-based throttling on authentication endpoints.
3. **Transport Security:** Strict TLS 1.3 encryption on all endpoints.
4. **Data Redaction:** PII and financial balances masked in audit log outputs.

---
""")

    # 23. Non-Functional Requirements
    sections.append("""## 23. Non-Functional Requirements

### Performance & Scalability
- **API Latency:** 95% of OLTP requests (deposits, balance checks) must respond in `< 250ms`.
- **Concurrency:** Support concurrent branch tellers and async mobile money webhooks without locking conflicts.
- **Reporting:** Heavy analytical and regulatory reports must execute against read-replicas or materialized views to prevent OLTP lockups.

### Availability & Resilience
- **Target Availability:** 99.9% uptime during operational branch hours.
- **Audit Durability:** Audit trail logs retained for minimum 7-year regulatory compliance window.

---
""")

    # 24. Cross-Page Findings
    sections.append("""## 24. Cross-Page Findings

1. **Vendor & Platform Identity:** Consistent branding across all headers: *"Savings Plus, Property of FutureLink Technologies | @2021 All rights reserved"*. FutureLink Technologies (FLT) provides the core banking platform engine.
2. **Misnamed Artifact:** `user_details.html` does not render an individual staff member's detail card; instead, it renders the exact form for editing a Role Profile (*"Edit Sp Data Migration Training"*), identical to `edit_role.html`.
3. **Shared Layout & Persistent State:** All 17 HTML files share an identical top status bar, navigation bar, idle detector widget, and failed job notification banner, indicating an Angular/Nuxt Single-Page Application shell.
4. **Entity Linkage:** Data created in `create_individual_member.html` flows directly into `list_member_applications.html` as a pending application, and subsequently appears in `list_members.html` upon approval.
5. **Currency Consistency:** The default currency across all balances and reports is Ugandan Shillings (`UGX`), with multi-currency / forex support configured in `/settings/institution/forex`.

---
""")

    # 25. Open Questions
    sections.append("""## 25. Open Questions

The following implementation specifics cannot be resolved strictly from the static HTML DOM and require stakeholder clarification:

| # | Subsystem | Open Question | Backend Impact |
|---|---|---|---|
| 1 | Regulatory Compliance | Is automated direct integration required for CRB inquiries (e.g. Metropol / Compuscan) or is it manual file upload? | External HTTP Client implementation vs static file storage |
| 2 | Biometrics / NIN | Does NIN verification connect directly to the NIRA (National Identification and Registration Authority) API or is it internal verification? | Integration adapter for NIRA gateway |
| 3 | Mobile Money Float | Are float balances polled via Telco APIs (MTN MoMo API, Airtel Money API) or updated via webhook callbacks from the MSACCO switch? | Asynchronous webhook listener vs scheduled balance polling worker |
| 4 | Accounting Engine | What is the exact Chart of Accounts structure (account code digit length, parent-child depth)? | Schema design of `chart_of_accounts` table |
| 5 | Storage Engine | Should uploaded documents (photos, signatures, IDs) be stored in local file storage or S3-compatible object storage? | Blob storage abstraction layer |
| 6 | Soft-Delete Policy | What is the permanent retention or purging lifecycle for soft-deleted users and customers? | Database cleanup / archiving jobs |

---
""")

    # 26. Assumptions
    sections.append("""## 26. Assumptions

1. **Database Engine:** PostgreSQL 15+ is assumed as the primary relational database to support JSONB attributes, transactions, and robust decimal precision.
2. **Statutory Jurisdiction:** The system operates under Ugandan legal and financial regulatory jurisdiction (BOU, UMRA, URA).
3. **Currency:** Primary accounting currency is `UGX` (Ugandan Shilling) with decimal precision set to 2 decimal places.
4. **Timezone:** Standard operating timezone is East Africa Time (EAT, UTC+3).
5. **Maker-Checker Strictness:** Maker-checker is mandatory for all member on-boarding, loan approvals, and transactions exceeding user-specific limits.

---
""")

    # 27. Traceability Matrix
    sections.append("""## 27. Traceability Matrix

| HTML Source File | Feature / Subsystem | Entities Involved | Requirement IDs | Evidence & Traceability |
|---|---|---|---|---|
| `create_member_step_one.html` | Member Type Selection | Customer | FR-001 | Cards for Individuals, Group, Joint, Institution |
| `create_individual_member.html` | Individual Onboarding | Customer, CustomerIndividual, NextOfKin, Employment, BankDetails | FR-002 | Complete 8-section form, file uploads, branch selection |
| `create_group.html` | Group Onboarding | Customer, CustomerGroup | FR-004 | Group Name, Registration number, Members/Non-members |
| `create_joint.html` | Joint Onboarding | Customer, CustomerJoint | FR-005 | Joint Name, shared addresses, loan limit toggle |
| `create_institution.html` | Institution Onboarding | Customer, CustomerInstitution | FR-006 | TIN, Business Type, Category, Ownership dropdowns |
| `list_members.html` | Member Directory | Customer, Branch | FR-001, FR-002 | Master table displaying members with phone, age, group |
| `list_groups.html` | Group Directory Tab | CustomerGroup | FR-004 | Groups filter subtab under /customers |
| `list_joints.html` | Joint Directory Tab | CustomerJoint | FR-005 | Joints filter subtab under /customers |
| `list_institutions.html` | Institution Directory Tab | CustomerInstitution | FR-006 | Institutions filter subtab under /customers |
| `list_member_applications.html` | Member Vetting Queue | Customer | FR-002, FR-003 | Pending / Rejected tabs, view application triggers |
| `list_agency_applications.html` | Agency Vetting Queue | AgencyApplication | FR-014 | Agent application pending / rejected tabs |
| `create_user.html` | User Provisioning | User, UserNotificationSubscription | FR-007 | Staff form, 9 Min/Max limit pairs, 22 alert checkboxes |
| `list_users.html` | User Administration | User | FR-008 | User cards, Deactivate, Delete, Enable SMS OTP buttons |
| `create_role.html` | Role Definition | Role, RolePermission | FR-009 | Role name, Open/Close time, 382 permission checkboxes |
| `edit_role.html` | Role Modification | Role, RolePermission | FR-009 | Permission editing and operating hour adjustments |
| `user_details.html` | Role Profile View | Role, RolePermission | FR-009 | Edit role profile UI misnamed as user_details.html |
| `list_roles.html` | Role Administration | Role | FR-009, FR-010 | Role cards, active user counts, operating hours |
| Global Top Bar / Shell | Liquidity & Alerts | ExternalFloat, Notification, Session | FR-011, FR-012, FR-013 | Float counters, Failed job alert, Idle detector |

---
""")

    # 28. Backend Implementation Considerations
    sections.append("""## 28. Backend Implementation Considerations

1. **Transactional Ledger Integrity:** Implement strict double-entry ledger primitives. Every financial event (deposit, withdrawal, disbursement, fee) must post balanced debit and credit entries to the general ledger within an isolated database transaction.
2. **Middleware Pipeline:**
   - `AuthenticationMiddleware`: Decodes JWT, validates active session.
   - `OperatingHoursMiddleware`: Evaluates current timestamp against user's role `open_time` and `close_time`.
   - `PermissionGuardMiddleware`: Checks route action against user's assigned role permission set.
   - `FinancialLimitInterceptor`: Checks transaction amount against user's min/max threshold.
3. **Asynchronous Processing:** Use a distributed task queue (e.g. Celery / BullMQ / Redis) for background jobs:
   - Interest accruals on savings and fixed deposits.
   - Batch standing order executions.
   - SMS and email notification dispatching.
   - Job health monitoring with automatic dispatch of `failed_job` alerts.

---
""")

    # 29. Final System Summary
    sections.append("""## 29. Final System Summary

The reverse-engineering of the 17 provided HTML artifacts establishes that **SavingsPlus** is an enterprise Core Banking System (CBS) designed for **NSIMBI SACCO LTD** by **FutureLink Technologies (FLT)**. 

The backend architecture requires:
- **Comprehensive Customer Information File (CIF):** Supporting Individuals, Groups, Joints, and Institutions with maker-checker onboarding.
- **Granular Security Framework:** 34 modules, 382 permissions, strict operating time windows, 5-minute idle timeouts, and individual financial limit ceilings across 9 transaction classes.
- **Integrated Operations:** End-to-end management of Savings, Loans, Shares, Accounting, Fixed Assets, Staff Payroll, External Float (MTN, Airtel, MSACCO, Bank), and Regulatory Compliance (BOU & UMRA).

This specification provides the exhaustive, unambiguous foundation for backend developers and automated coding agents to implement the database models, business logic services, and REST API controllers for the NSIMBI Core Banking System.
""")

    full_text = "\n".join(sections)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write(full_text)
    
    # Also write a copy to ~/Downloads/MSACCO/REQUIREMENTS.md for user convenience
    downloads_copy = os.path.expanduser("~/Downloads/MSACCO/REQUIREMENTS.md")
    with open(downloads_copy, "w", encoding="utf-8") as f:
        f.write(full_text)

    print(f"REQUIREMENTS.md generated successfully at:\n - {OUTPUT_FILE}\n - {downloads_copy}")
    print(f"Total lines: {len(full_text.splitlines())}")

if __name__ == "__main__":
    build_requirements()

### BR-007: Savings Dual-Balance & Minimum Reserve Protection
- **Rule:** A savings account must enforce a minimum balance reserve (observed default: `UGX 5,000.00`).
- **Formula:** `available_balance = actual_balance - minimum_reserve - active_holds`.
- **Enforcement:** No withdrawal or transfer may cause `available_balance < 0`.

### BR-008: Auto-Pay Priority on Savings
- **Rule:** If a loan has `auto_pay_from_savings=true`, the scheduled installment sweep has automatic debit priority against the linked savings account on the installment due date.

### BR-009: Loan Amortization Calculation Integrity
- **Rule:** Installments must be calculated strictly according to product specifications:
  - `flat`: `Installment = (Principal / N) + (Principal * (Rate / 100) / N)`.
  - `declining`: Periodic interest computed on remaining principal.
  - `amortize`: Fixed annuity installment formula `PMT(r, n, P)`.

### BR-010: Rescheduled Loan Lineage Tracking
- **Rule:** When a delinquent loan is restructured, the old loan is closed and a new loan is generated with a mandatory reference to the original loan number (`Rescheduled from LN00299`).

### BR-011: Fixed Deposit Early Liquidation Penalty
- **Rule:** If a fixed deposit is terminated prior to its maturity date, interest may be completely forfeited (`TERMINATED_NO_INTEREST`) unless explicitly authorized by a manager (`TERMINATED_WITH_INTEREST`).

---
""")

    # 11. Validation Rules
    s.append("""## 11. Validation Rules

### Confirmed Validations (Savings & Loans)
1. **Fixed Deposit Placement:**
   - `Amount*`: Must be strictly positive and `>= minimum_fixing_amount` defined on product.
   - `Fixing period in months`: Must be integer `>= 1`.
   - `Interest Rate (%)*`: Non-negative decimal.
   - `Starts on*`: Valid date.
2. **Loan Calculator & Origination:**
   - `Product*`: Valid active loan product.
   - `Amount*`: Within product min/max bounds.
   - `Annual interest rate*`: Within product rate limits.
   - `Number of installments`: Integer `>= 1`.
3. **Savings Account Actions:**
   - Cannot delete an account that has a non-zero balance (`actual_balance != 0`).
   - Cannot freeze an already frozen account.

---
""")

    # 12. Authentication Requirements
    s.append("""## 12. Authentication Requirements

- Standard session authentication via JWT (`/api/auth/login`).
- 5-minute inactivity lockdown strictly monitored by front-end and enforced by backend session expiry.
- SMS OTP two-factor authentication toggled per user account (`/users/{id}/toggle-sms-otp`).

---
""")

    # 13. Authorization Requirements
    s.append("""## 13. Authorization Requirements

Complete mapping across all 34 modules and 382 permissions confirmed in previous section, with explicit operational authorization required for:
- Savings: `register deposit`, `register withdrawal`, `freeze savings account`, `close savings account`, `delete savings account`, `register standing order`, `register fixed deposit`, `accrue fixed deposit`, `terminate fixed deposit`.
- Loans: `apply for loan`, `approve loan application`, `reject loan application`, `disburse loan`, `register repayment`, `register refund`, `register penalty waiver`, `register interest waiver`, `admin fees waiver`, `reschedule loan`, `send loan to legal`, `write off loan`, `unwrite off loan`.

---
""")

    # 14. Functional Requirements
    s.append("""## 14. Functional Requirements

### FR-001 — Member Category Selection
- **Requirement:** The system shall present a registration gateway allowing the operator to select one of four member entity types: Individual, Group, Joint, or Institution.
- **Entity:** `Customer`
- **Actor:** Customer Care / Staff
- **Source:** `create_member_step_one.html`
- **Priority:** MUST HAVE
- **Preconditions:** Staff is authenticated and holds permission `register individual customer` / `register group customer` / etc.
- **Input:** Selected entity type (`INDIVIDUAL`, `GROUP`, `JOINT`, `INSTITUTION`).
- **Business Rules:** Navigates directly to the specialized multi-field schema for the chosen category.
- **Expected Result:** Renders appropriate registration form.
- **Failure Conditions:** Unauthorized access redirects to 403 Forbidden.
- **Evidence Level:** Explicit

### FR-002 — Individual Member Registration
- **Requirement:** The system shall capture complete individual KYC data across Bio Data, Contact Information, Next of Kin, Employment, Income, Bank Details, and Document Uploads, persisting the record in `PENDING` approval status.
- **Entity:** `Customer`, `CustomerIndividual`, `NextOfKin`, `EmploymentIncome`, `ExternalBank`
- **Actor:** Customer Care / Registration Officer
- **Source:** `create_individual_member.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid branch and designated savings officer selected.
- **Input:** Salutation, Names, Gender, Marital Status, DOB, Nationality, Home Ownership, Identification Type & Number, Entry Date, Phone, Physical Address, Next of Kin details, Occupation, Income Per Month, Signature scan, ID document scan, Photo file.
- **Business Rules:** Form submission enters the Maker-Checker queue (`list_member_applications.html`). System generates unique formatted `member_number` (e.g. `00350000519`).
- **Expected Result:** Member record created in `PENDING` state; event `customer_applications_pending_approval` dispatched.
- **Failure Conditions:** Missing mandatory fields; duplicate identification number / NIN.
- **Evidence Level:** Explicit

### FR-003 — Member Application Approval & Rejection
- **Requirement:** The system shall allow authorized managers to review pending member applications and execute an approval or rejection decision.
- **Entity:** `Customer`
- **Actor:** Branch Manager / Checker
- **Source:** `list_member_applications.html`
- **Priority:** MUST HAVE
- **Preconditions:** Member application exists in `PENDING` status. Checker is not the maker who created the record.
- **Input:** Application ID, Approval Decision (`APPROVE` or `REJECT`), Rejection Reason (if rejected).
- **Business Rules:** On approval, status transitions to `ACTIVE`, core savings passbook ledger is provisioned, and welcome SMS event is triggered. On rejection, status transitions to `REJECTED`.
- **Expected Result:** Application removed from pending queue and visible under appropriate master/rejected directory.
- **Failure Conditions:** Checker is maker (BR-001 violation); application already decided.
- **Evidence Level:** Explicit

### FR-004 — Group Member Registration
- **Requirement:** The system shall register solidarity lending groups and VSLAs with group type classification (Members vs Non Members) and official registration certificates.
- **Entity:** `Customer`, `CustomerGroup`
- **Actor:** Field Officer / Staff
- **Source:** `create_group.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated operator with group registration privilege.
- **Input:** Branch, Group Name, Group Type (`Members` or `Non Members`), Registration Number, Entry Date, Savings Officer, Phone, Address.
- **Business Rules:** Groups may hold group savings accounts and act as institutional borrowing entities.
- **Expected Result:** Group entity persisted in `PENDING` status awaiting approval.
- **Failure Conditions:** Duplicate group registration number within branch.
- **Evidence Level:** Explicit

### FR-005 — Joint Account Registration
- **Requirement:** The system shall register joint member entities shared by two or more individuals with shared contact information and loan limit settings.
- **Entity:** `Customer`, `CustomerJoint`
- **Actor:** Customer Care / Staff
- **Source:** `create_joint.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated operator.
- **Input:** Branch, Joint Name, Entry Date, Assigned Officer, Phone, Address, Loan Limit toggle.
- **Business Rules:** Joint entity requires multi-signatory rules for subsequent withdrawals.
- **Expected Result:** Joint customer record persisted.
- **Failure Conditions:** Missing mandatory branch, name, or phone.
- **Evidence Level:** Explicit

### FR-006 — Institution Customer Registration
- **Requirement:** The system shall register corporate and institutional customers, capturing legal categorization, URA TIN numbers, corporate category, and business sectors.
- **Entity:** `Customer`, `CustomerInstitution`
- **Actor:** Business Development / Customer Care
- **Source:** `create_institution.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated staff.
- **Input:** Institution Name, Registration Number, Registration Date, TIN Number, Business Type (e.g. Agriculture, SACCO, MDI, etc.), Institution Category (e.g. Limited Liability, NGO, etc.), Residence Ownership, Entry Date, Phone, Physical Address, Monthly Income.
- **Business Rules:** TIN must be validated for format and uniqueness.
- **Expected Result:** Institutional record created for approval.
- **Failure Conditions:** Duplicate TIN or registration number.
- **Evidence Level:** Explicit

### FR-007 — User Account Provisioning & Financial Authorization Limits
- **Requirement:** The system shall provision staff user accounts with individual operational bounds across 9 financial transaction categories and alert subscription selections.
- **Entity:** `User`, `UserNotificationSubscription`
- **Actor:** System Administrator
- **Source:** `create_user.html`
- **Priority:** MUST HAVE
- **Preconditions:** Administrator holds `register user account` permission.
- **Input:** First Name, Last Name, Username, Email, Password, Phone, Designation/Role, Station/Teller, Branch, 9 Min/Max Limit pairs, Selection of Notification Subscriptions (up to 22 checkboxes).
- **Business Rules:** Password must be hashed securely; Min Amount must not exceed Max Amount; limits strictly enforce maker-checker thresholds at runtime.
- **Expected Result:** User account created and enabled.
- **Failure Conditions:** Duplicate username or email; Min Amount > Max Amount.
- **Evidence Level:** Explicit

### FR-008 — User Lifecycle State Management (Deactivate, Delete, SMS OTP)
- **Requirement:** The system shall provide operational controls on staff user accounts to deactivate active users, soft-delete records, and toggle SMS OTP two-factor authentication.
- **Entity:** `User`
- **Actor:** System Administrator
- **Source:** `list_users.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target user exists.
- **Input:** Target User ID, Action (`DEACTIVATE`, `ACTIVATE`, `DELETE`, `ENABLE_SMS_OTP`, `DISABLE_SMS_OTP`).
- **Business Rules:** Deactivated users cannot authenticate (`ERR_ACCOUNT_INACTIVE`). Deleted users are soft-deleted to preserve transaction audit integrity.
- **Expected Result:** User status updated immediately; active sessions invalidated.
- **Failure Conditions:** Administrator cannot deactivate own account.
- **Evidence Level:** Explicit

### FR-009 — Role Profile Definition & Operating Hours Configuration
- **Requirement:** The system shall allow administrators to create and edit Role Profiles, configuring daily operating time windows (`open_time` to `close_time`) and assigning granular checkboxes from the 382 permission pool.
- **Entity:** `Role`, `RolePermission`
- **Actor:** System Administrator
- **Source:** `create_role.html`, `edit_role.html`, `user_details.html`
- **Priority:** MUST HAVE
- **Preconditions:** Administrator holds `register user profile` or `update user profile`.
- **Input:** Role Name, Open Time (Time picker), Close Time (Time picker), Array of selected permission keys.
- **Business Rules:** Operating hours enforce system access cutoff; permission updates take effect on subsequent token evaluation.
- **Expected Result:** Role persisted and available for user assignment.
- **Failure Conditions:** Empty role name; close time precedes open time.
- **Evidence Level:** Explicit

### FR-010 — Role Activation, Deactivation & Deletion
- **Requirement:** The system shall provide lifecycle management for role profiles, allowing activation, deactivation, and deletion.
- **Entity:** `Role`
- **Actor:** System Administrator
- **Source:** `list_roles.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target role exists.
- **Input:** Role ID, Action (`ACTIVATE`, `DEACTIVATE`, `DELETE`).
- **Business Rules:** A role cannot be deleted if active users are assigned to it (e.g., *"1 User assigned"* guard). Deactivating a role blocks authentication for all assigned users.
- **Expected Result:** Role status updated.
- **Failure Conditions:** Attempting to delete a role with assigned users (`ERR_ROLE_IN_USE`).
- **Evidence Level:** Explicit

### FR-011 — Real-Time Liquidity & Float Balance Monitoring
- **Requirement:** The system shall maintain and expose real-time aggregate balances for external payment channels: MSACCO Credit, MTN Mobile Money Float, Airtel Money Float, Bank Transfer Float, and General Float.
- **Entity:** `ExternalFloatLedger`
- **Actor:** System / Cashier / Manager
- **Source:** Global Top Bar across all 17 HTML files
- **Priority:** MUST HAVE
- **Preconditions:** Integration webhooks and ledger updates configured.
- **Input:** Channel Identifier, Float Inflow / Outflow, Current Balance.
- **Business Rules:** Float is debited on outgoing mobile money cashouts and credited on incoming deposits. Balance warnings fire when float breaches safety floor.
- **Expected Result:** Current float balances returned to client header widgets.
- **Failure Conditions:** Channel gateway timeout.
- **Evidence Level:** Explicit

### FR-012 — 5-Minute Inactivity Session Timeout
- **Requirement:** The system shall track client activity and invalidate sessions exceeding 300 seconds of inactivity.
- **Entity:** `UserSession`
- **Actor:** System / Authentication Engine
- **Source:** Idle Detector component across all 17 HTML files
- **Priority:** MUST HAVE
- **Preconditions:** Active session token.
- **Input:** Last activity timestamp.
- **Business Rules:** Inactivity > 5 minutes invalidates token and forces re-login.
- **Expected Result:** Session expired; client redirected to login.
- **Failure Conditions:** N/A
- **Evidence Level:** Explicit

### FR-013 — Asynchronous Job Monitoring & Failure Notification
- **Requirement:** The system shall capture execution logs for all background tasks and broadcast high-priority failure notifications to subscribed users whenever a job fails.
- **Entity:** `SystemJob`, `JobExecutionLog`, `Notification`
- **Actor:** System / Scheduler Engine
- **Source:** Global Header Alert: *"A recently started job has failed Today 10:26:26 PM"*
- **Priority:** MUST HAVE
- **Preconditions:** Background job dispatched.
- **Input:** Job ID, Status (`SUCCESS`, `FAILED`), Error Details.
- **Business Rules:** Failed job records error in `/settings/jobs/{id}/logs` and sends alert to users subscribed to `failed_job`.
- **Expected Result:** In-app notification with "Mark as read" trigger.
- **Failure Conditions:** Database connection loss during error logging.
- **Evidence Level:** Explicit

### FR-014 — Agency Banking Application Queue & Vetting
- **Requirement:** The system shall provide an application intake and maker-checker queue for third-party banking agents with pending and rejected tabs.
- **Entity:** `AgencyApplication`
- **Actor:** Agency Banking Lead / Manager
- **Source:** `list_agency_applications.html`
- **Priority:** MUST HAVE
- **Preconditions:** Agency application submitted.
- **Input:** Agent ID, Business Name, Location, Branch, Decision (`APPROVE`, `REJECT`).
- **Business Rules:** Approved agents obtain agency terminal credentials and float limits.
- **Expected Result:** Agent status updated.
- **Failure Conditions:** Incomplete KYC documentation.
- **Evidence Level:** Explicit

---
""")

    # 15. API Requirements
    sections.append("""## 15. API Requirements

Based on explicit form submissions, list page interactions, and the 190 discovered routes in the client bundle, the following RESTful API surface is required:

### Authentication & Session Subsystem
- `POST /api/auth/login`
  - **Purpose:** Authenticate staff credentials.
  - **Payload:** `{ username: "...", password: "..." }`
  - **Response:** `{ token: "...", refresh_token: "...", expires_in: 300, user: { ... } }`
  - **Validation & Rules:** Rate-limited; verifies `is_active=true`, `is_blocked=false`, and temporal window `open_time <= now <= close_time`.
- `POST /api/auth/logout`
  - **Purpose:** Invalidate current session token.
- `GET /api/auth/user`
  - **Purpose:** Return current authenticated user profile, permissions, and assigned limits.
- `POST /api/auth/refresh`
  - **Purpose:** Issue fresh JWT against valid refresh token.

### Customers & Membership Subsystem
- `GET /customers`
  - **Purpose:** Query paginated customer directory with filtering.
  - **Query Params:** `type` (individual, group, joint, institution), `branch_id`, `search`, `page`, `per_page`.
- `POST /customers/individuals`
  - **Purpose:** Submit new individual member application.
  - **Payload:** Multipart/form-data containing all Bio Data, Contact, Kin, Employment, Bank details, and uploaded binary files (signature, ID, photo).
  - **Response:** 201 Created `{ id: 1223, member_number: "00350000519", status: "PENDING" }`
- `POST /customers/groups`
  - **Purpose:** Register group member.
  - **Payload:** `{ branch_id, group_name, group_type, registration_number, entry_date, savings_officer_id, phone, address, ... }`
- `POST /customers/joints`
  - **Purpose:** Register joint account entity.
- `POST /customers/institutions`
  - **Purpose:** Register corporate/institutional entity.
  - **Payload:** `{ institution_name, registration_number, registration_date, tin_number, business_type, institution_category, ... }`
- `GET /customers/applications`
  - **Purpose:** Fetch maker-checker queue for member applications.
  - **Query Params:** `status=pending` or `status=rejected`, `branch_id`, `page`.
- `POST /customers/applications/{id}/approve`
  - **Purpose:** Approve pending member application and activate ledgers.
- `POST /customers/applications/{id}/reject`
  - **Purpose:** Reject pending member application with reason notes.
- `GET /customers/agent-applications`
  - **Purpose:** Fetch agency banking applications queue.

### Users & Security Subsystem
- `GET /users`
  - **Purpose:** Fetch list of system users.
  - **Query Params:** `status` (all, active, inactive), `search`, `page`.
- `POST /users`
  - **Purpose:** Provision new user account with transaction limits and notification subscriptions.
  - **Payload:** `{ first_name, last_name, username, email, password, phone, role_id, branch_id, station_id, limits: { ... }, notification_subscriptions: [ ... ] }`
- `PATCH /users/{id}`
  - **Purpose:** Update user details, limits, or assign new role.
- `POST /users/{id}/deactivate`
  - **Purpose:** Deactivate active user.
- `DELETE /users/{id}`
  - **Purpose:** Soft-delete user account.
- `POST /users/{id}/toggle-sms-otp`
  - **Purpose:** Enable or disable 2FA requirement.

### Roles & RBAC Subsystem
- `GET /users/roles`
  - **Purpose:** List all role profiles with assigned user counts and operating hours.
  - **Query Params:** `status` (all, active, inactive).
- `POST /users/roles`
  - **Purpose:** Create new role profile.
  - **Payload:** `{ role_name: "...", open_time: "00:00:00", close_time: "23:00:00", permissions: [ "register deposit", ... ] }`
- `GET /users/roles/{id}`
  - **Purpose:** Retrieve role definition and complete array of assigned permissions.
- `PATCH /users/roles/{id}`
  - **Purpose:** Update role permissions or operating window.
- `POST /users/roles/{id}/activate` / `POST /users/roles/{id}/deactivate`
  - **Purpose:** Toggle operational availability of role.
- `DELETE /users/roles/{id}`
  - **Purpose:** Delete unassigned role.

### Notifications & Float Subsystem
- `GET /notifications`
  - **Purpose:** Fetch current user's unread notifications.
- `POST /notifications/{id}/mark-read`
  - **Purpose:** Mark notification as read.
- `POST /notifications/mark-all-read`
  - **Purpose:** Mark all unread notifications as read.
- `GET /settings/float-balances`
  - **Purpose:** Fetch latest float balances for top-bar indicators (MSACCO, MTN, Airtel, Bank Transfer).

---
""")

    # 16. Data / Database Requirements
    sections.append("""## 16. Data / Database Requirements

### Conceptual Data Model
The database architecture must be a relational database (PostgreSQL recommended) enforcing ACID compliance and double-entry ledger immutability.

```sql
-- Core Branches Table
CREATE TABLE branches (
    id SERIAL PRIMARY KEY,
    branch_code VARCHAR(10) UNIQUE NOT NULL, -- e.g. '0035'
    name VARCHAR(100) NOT NULL, -- e.g. 'KABALE'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Core Roles Table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    open_time TIME NOT NULL DEFAULT '00:00:00',
    close_time TIME NOT NULL DEFAULT '23:59:59',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Role Permissions Table
CREATE TABLE role_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_key VARCHAR(100) NOT NULL,
    UNIQUE(role_id, permission_key)
);

-- Core Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    role_id INTEGER NOT NULL REFERENCES roles(id),
    station_id VARCHAR(50), -- e.g. 'Teller - 1'
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    sms_otp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    
    -- Maker-Checker Financial Limits
    loan_approval_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    loan_approval_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- User Notification Subscriptions Table
CREATE TABLE user_notification_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_key VARCHAR(100) NOT NULL,
    UNIQUE(user_id, event_key)
);

-- Master Customers Table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    member_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '00350000001'
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('INDIVIDUAL', 'GROUP', 'JOINT', 'INSTITUTION')),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    savings_officer_id BIGINT REFERENCES users(id),
    entry_date DATE NOT NULL,
    phone_primary VARCHAR(30) NOT NULL,
    phone_secondary VARCHAR(30),
    email VARCHAR(150),
    country VARCHAR(100) NOT NULL DEFAULT 'Uganda',
    town VARCHAR(100),
    physical_address TEXT NOT NULL,
    postal_code VARCHAR(30),
    postal_address VARCHAR(150),
    has_loan_limit BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'REJECTED', 'INACTIVE', 'DORMANT')),
    created_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Individual Profile Table
CREATE TABLE customer_individuals (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    salutation VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female')),
    marital_status VARCHAR(20) NOT NULL,
    is_dependent BOOLEAN NOT NULL DEFAULT FALSE,
    is_pwd BOOLEAN NOT NULL DEFAULT FALSE,
    date_of_birth DATE NOT NULL,
    country_of_birth VARCHAR(100) NOT NULL,
    nationality VARCHAR(100) NOT NULL DEFAULT 'Ugandan',
    home_ownership VARCHAR(20) NOT NULL CHECK (home_ownership IN ('Owner', 'Tenant', 'Other')),
    identification_type VARCHAR(50) NOT NULL,
    identification_number VARCHAR(100) NOT NULL,
    card_number VARCHAR(100),
    signature_file_path TEXT,
    identification_file_path TEXT,
    photo_file_path TEXT
);

-- Next of Kin Table
CREATE TABLE customer_next_of_kin (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    second_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    physical_address TEXT NOT NULL,
    relationship VARCHAR(30) NOT NULL
);

-- Group Profile Table
CREATE TABLE customer_groups (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    group_name VARCHAR(150) NOT NULL,
    group_type VARCHAR(50) NOT NULL CHECK (group_type IN ('Members', 'Non Members')),
    registration_number VARCHAR(100) NOT NULL
);

-- Joint Profile Table
CREATE TABLE customer_joints (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    joint_name VARCHAR(150) NOT NULL
);

-- Institution Profile Table
CREATE TABLE customer_institutions (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    institution_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100) NOT NULL,
    registration_date DATE NOT NULL,
    tin_number VARCHAR(50) NOT NULL,
    business_type VARCHAR(100) NOT NULL,
    institution_category VARCHAR(100) NOT NULL,
    residence_ownership VARCHAR(50) NOT NULL
);
```

---
""")

    # 17. Search, Filtering and Reporting
    sections.append("""## 17. Search, Filtering and Reporting

### Search Capabilities
- Master member search indexing across: Member Number, Name, Phone Number, and Group Affiliation.
- User search indexing: Username, First Name, Last Name, and Branch.

### Filtering Dimensions
- Status Filters: `All`, `Active`, `Inactive`, `Pending`, `Rejected`.
- Organizational Filters: Branch (`0035 KABALE`), Officer assignment, Customer Subtype.
- Date Range Filters: Registration date, approval date, transaction value date.

### Regulatory & Management Reports Catalog
The system specifies 109 discrete reporting capabilities including:
1. **Central Bank / BOU Returns:** Bank of Uganda Liquidity Return (`bou liquidity`), Bank of Uganda Banking Supervision Return (`bou bsa`).
2. **UMRA Returns:** Uganda Microfinance Regulatory Authority Balance Sheet (`umra balance sheet`), Income Statement (`umra income statement`), and PAR by Aging (`umra par by ageing`).
3. **Credit Operations Reports:** Loan Arrears vs Savings, Portfolio Monitoring, Cleared Loans, Due Loans, Written-off Loans, CRB Registers.
4. **General Ledger & Accounting:** Balance Sheet, Profit & Loss / Income Statement, Cash Flow Statement, Trial Balance, Backdated Transactions, Till Sheet Reconciliation.
5. **Shares & Equity:** Cumulative Shares, Shares Ledger, Dividends Shared Report.

---
""")

    # 18. File and Document Requirements
    sections.append("""## 18. File and Document Requirements

### Upload Specifications
1. **Signature File:** Uploaded during individual registration (`Signature*`). Stored securely; rendered on teller screens for withdrawal verification.
2. **Identification Document Scan:** Uploaded ID card/passport scan (`Indentification*`).
3. **Profile Photo:** Member portrait photograph (`Photo*`).
4. **Bulk Processing Files:** Bulk payroll/disbursement files (`.csv`, `.xlsx`) uploaded for batch processing.
5. **Database Backups:** Compressed database backup files downloadable by administrators (`download backup`, `restore backup`).

---
""")

    # 19. Notification and Event Requirements
    sections.append("""## 19. Notification and Event Requirements

### The 22 Core Notification Triggers
The backend event dispatcher must route notifications based on user subscriptions:
1. `blocked_user`: Dispatched when an account is locked out after consecutive failed authentications.
2. `bulk_files_pending_approval`: Dispatched when an institutional disbursement file is uploaded.
3. `customer_applications_pending_approval`: Dispatched when a member registration form is submitted.
4. `customer_kyc_updated`: Dispatched when member biodata or identity documents are altered.
5. `failed_job`: Dispatched immediately when an asynchronous background job errors out.
6. `fixed_deposit_applications_pending_approval`: Dispatched on term deposit placement.
7. `license_expiry_reminder`: Dispatched when software license term nears expiration.
8. `license_upgraded`: Dispatched when license tier or capacity is expanded.
9. `liquid_assets_balance_running_low`: Dispatched when branch cash or float balances breach minimum reserve.
10. `loan_applications_pending_approval`: Dispatched when a loan officer submits an application.
11. `loan_applications_pending_disbursement`: Dispatched when an approved loan is ready for payout.
12. `msacco_customer_onboarding`: Dispatched when a member self-registers via mobile USSD/App.
13. `msacco_loan_application`: Dispatched on mobile emergency loan request.
14. `opening_a_closed_period`: High-priority alert triggered if an accounting period is unsealed.
15. `reserve_amount_limit`: Dispatched when statutory reserves breach threshold.
16. `savings_account_applications_pending_approval`: Dispatched on savings account opening.
17. `standing_orders_pending_approval`: Dispatched when a recurring payment rule is established.
18. `tb_imbalance`: Critical financial alarm fired when Trial Balance debits != credits.
19. `transaction_mismatch`: Dispatched on settlement variance between CBS and mobile money/bank float.
20. `transactions_pending_approval`: Dispatched when a transaction breaches a teller's posting limit.
21. `upcoming_holidays`: Dispatched in advance of statutory holidays affecting loan schedules.
22. `updated_user_profile`: Dispatched when administrative permissions or limits change.

---
""")

    # 20. Audit and History Requirements
    sections.append("""## 20. Audit and History Requirements

### Immutability & Audit Trails
- **Audit Fields:** Every database entity must include `created_at`, `updated_at`, `created_by`, and `approved_by`.
- **System Audit Trail (`view audit trail`):** Comprehensive record capturing timestamp, actor user ID, client IP address, action performed, entity affected, old state, and new state.
- **Financial Immutability:** No financial transaction may be hard-deleted or updated in-place. Corrections must occur strictly via reversing entries (`reverse journal`, `reverse withdrawal`, `reverse deposit`, `reverse share purchase`).
- **Backdated Transaction Audit (`view backdated transactions`):** Specialized tracking for any entry posted with a historical value date.

---
""")

    # 21. Error Handling Requirements
    sections.append("""## 21. Error Handling Requirements

### Standard Error Taxonomy
- `400 Bad Request`: Input validation failure (`ERR_VALIDATION_FAILED`), duplicate unique field (`ERR_DUPLICATE_NIN`, `ERR_DUPLICATE_USERNAME`).
- `401 Unauthorized`: Missing, expired, or invalid bearer token (`ERR_SESSION_EXPIRED`, `ERR_UNAUTHENTICATED`).
- `403 Forbidden`:
  - Temporal restriction: `ERR_OUTSIDE_OPERATING_HOURS`.
  - Permission restriction: `ERR_PERMISSION_DENIED`.
  - Transaction limit restriction: `ERR_EXCEEDS_USER_LIMIT`.
  - Maker-checker violation: `ERR_CANNOT_APPROVE_OWN_RECORD`.
- `404 Not Found`: Target entity not found (`ERR_MEMBER_NOT_FOUND`).
- `409 Conflict`: Invalid state transition (`ERR_INVALID_STATUS_TRANSITION`), period already closed (`ERR_PERIOD_CLOSED`), Trial Balance imbalance (`ERR_TB_IMBALANCE`).
- `500 Internal Server Error`: Asynchronous job failure (`ERR_JOB_FAILED`), unexpected exception.

---
""")

    # 22. Security Requirements
    sections.append("""## 22. Security Requirements

### Observed Security Requirements
1. **Operating Hours Access Control:** Strictly enforced login and execution window per role.
2. **Idle Session Invalidation:** 5-minute timeout.
3. **Transaction Boundary Protection:** User min/max amount caps.
4. **Segregation of Duties (SoD):** Maker-checker separation for applications and transactions.
5. **Database Restoration Mode:** Dedicated administrative lock state (`activate restoration mode`).

### Recommended Security Enhancements
1. **Password Policy:** Minimum 12 characters, complexity requirements, Argon2id or bcrypt hashing.
2. **Rate Limiting:** IP and user-based throttling on authentication endpoints.
3. **Transport Security:** Strict TLS 1.3 encryption on all endpoints.
4. **Data Redaction:** PII and financial balances masked in audit log outputs.

---
""")

    # 23. Non-Functional Requirements
    sections.append("""## 23. Non-Functional Requirements

### Performance & Scalability
- **API Latency:** 95% of OLTP requests (deposits, balance checks) must respond in `< 250ms`.
- **Concurrency:** Support concurrent branch tellers and async mobile money webhooks without locking conflicts.
- **Reporting:** Heavy analytical and regulatory reports must execute against read-replicas or materialized views to prevent OLTP lockups.

### Availability & Resilience
- **Target Availability:** 99.9% uptime during operational branch hours.
- **Audit Durability:** Audit trail logs retained for minimum 7-year regulatory compliance window.

---
""")

    # 24. Cross-Page Findings
    sections.append("""## 24. Cross-Page Findings

1. **Vendor & Platform Identity:** Consistent branding across all headers: *"Savings Plus, Property of FutureLink Technologies | @2021 All rights reserved"*. FutureLink Technologies (FLT) provides the core banking platform engine.
2. **Misnamed Artifact:** `user_details.html` does not render an individual staff member's detail card; instead, it renders the exact form for editing a Role Profile (*"Edit Sp Data Migration Training"*), identical to `edit_role.html`.
3. **Shared Layout & Persistent State:** All 17 HTML files share an identical top status bar, navigation bar, idle detector widget, and failed job notification banner, indicating an Angular/Nuxt Single-Page Application shell.
4. **Entity Linkage:** Data created in `create_individual_member.html` flows directly into `list_member_applications.html` as a pending application, and subsequently appears in `list_members.html` upon approval.
5. **Currency Consistency:** The default currency across all balances and reports is Ugandan Shillings (`UGX`), with multi-currency / forex support configured in `/settings/institution/forex`.

---
""")

    # 25. Open Questions
    sections.append("""## 25. Open Questions

The following implementation specifics cannot be resolved strictly from the static HTML DOM and require stakeholder clarification:

| # | Subsystem | Open Question | Backend Impact |
|---|---|---|---|
| 1 | Regulatory Compliance | Is automated direct integration required for CRB inquiries (e.g. Metropol / Compuscan) or is it manual file upload? | External HTTP Client implementation vs static file storage |
| 2 | Biometrics / NIN | Does NIN verification connect directly to the NIRA (National Identification and Registration Authority) API or is it internal verification? | Integration adapter for NIRA gateway |
| 3 | Mobile Money Float | Are float balances polled via Telco APIs (MTN MoMo API, Airtel Money API) or updated via webhook callbacks from the MSACCO switch? | Asynchronous webhook listener vs scheduled balance polling worker |
| 4 | Accounting Engine | What is the exact Chart of Accounts structure (account code digit length, parent-child depth)? | Schema design of `chart_of_accounts` table |
| 5 | Storage Engine | Should uploaded documents (photos, signatures, IDs) be stored in local file storage or S3-compatible object storage? | Blob storage abstraction layer |
| 6 | Soft-Delete Policy | What is the permanent retention or purging lifecycle for soft-deleted users and customers? | Database cleanup / archiving jobs |

---
""")

    # 26. Assumptions
    sections.append("""## 26. Assumptions

1. **Database Engine:** PostgreSQL 15+ is assumed as the primary relational database to support JSONB attributes, transactions, and robust decimal precision.
2. **Statutory Jurisdiction:** The system operates under Ugandan legal and financial regulatory jurisdiction (BOU, UMRA, URA).
3. **Currency:** Primary accounting currency is `UGX` (Ugandan Shilling) with decimal precision set to 2 decimal places.
4. **Timezone:** Standard operating timezone is East Africa Time (EAT, UTC+3).
5. **Maker-Checker Strictness:** Maker-checker is mandatory for all member on-boarding, loan approvals, and transactions exceeding user-specific limits.

---
""")

    # 27. Traceability Matrix
    sections.append("""## 27. Traceability Matrix

| HTML Source File | Feature / Subsystem | Entities Involved | Requirement IDs | Evidence & Traceability |
|---|---|---|---|---|
| `create_member_step_one.html` | Member Type Selection | Customer | FR-001 | Cards for Individuals, Group, Joint, Institution |
| `create_individual_member.html` | Individual Onboarding | Customer, CustomerIndividual, NextOfKin, Employment, BankDetails | FR-002 | Complete 8-section form, file uploads, branch selection |
| `create_group.html` | Group Onboarding | Customer, CustomerGroup | FR-004 | Group Name, Registration number, Members/Non-members |
| `create_joint.html` | Joint Onboarding | Customer, CustomerJoint | FR-005 | Joint Name, shared addresses, loan limit toggle |
| `create_institution.html` | Institution Onboarding | Customer, CustomerInstitution | FR-006 | TIN, Business Type, Category, Ownership dropdowns |
| `list_members.html` | Member Directory | Customer, Branch | FR-001, FR-002 | Master table displaying members with phone, age, group |
| `list_groups.html` | Group Directory Tab | CustomerGroup | FR-004 | Groups filter subtab under /customers |
| `list_joints.html` | Joint Directory Tab | CustomerJoint | FR-005 | Joints filter subtab under /customers |
| `list_institutions.html` | Institution Directory Tab | CustomerInstitution | FR-006 | Institutions filter subtab under /customers |
| `list_member_applications.html` | Member Vetting Queue | Customer | FR-002, FR-003 | Pending / Rejected tabs, view application triggers |
| `list_agency_applications.html` | Agency Vetting Queue | AgencyApplication | FR-014 | Agent application pending / rejected tabs |
| `create_user.html` | User Provisioning | User, UserNotificationSubscription | FR-007 | Staff form, 9 Min/Max limit pairs, 22 alert checkboxes |
| `list_users.html` | User Administration | User | FR-008 | User cards, Deactivate, Delete, Enable SMS OTP buttons |
| `create_role.html` | Role Definition | Role, RolePermission | FR-009 | Role name, Open/Close time, 382 permission checkboxes |
| `edit_role.html` | Role Modification | Role, RolePermission | FR-009 | Permission editing and operating hour adjustments |
| `user_details.html` | Role Profile View | Role, RolePermission | FR-009 | Edit role profile UI misnamed as user_details.html |
| `list_roles.html` | Role Administration | Role | FR-009, FR-010 | Role cards, active user counts, operating hours |
| Global Top Bar / Shell | Liquidity & Alerts | ExternalFloat, Notification, Session | FR-011, FR-012, FR-013 | Float counters, Failed job alert, Idle detector |

---
""")

    # 28. Backend Implementation Considerations
    sections.append("""## 28. Backend Implementation Considerations

1. **Transactional Ledger Integrity:** Implement strict double-entry ledger primitives. Every financial event (deposit, withdrawal, disbursement, fee) must post balanced debit and credit entries to the general ledger within an isolated database transaction.
2. **Middleware Pipeline:**
   - `AuthenticationMiddleware`: Decodes JWT, validates active session.
   - `OperatingHoursMiddleware`: Evaluates current timestamp against user's role `open_time` and `close_time`.
   - `PermissionGuardMiddleware`: Checks route action against user's assigned role permission set.
   - `FinancialLimitInterceptor`: Checks transaction amount against user's min/max threshold.
3. **Asynchronous Processing:** Use a distributed task queue (e.g. Celery / BullMQ / Redis) for background jobs:
   - Interest accruals on savings and fixed deposits.
   - Batch standing order executions.
   - SMS and email notification dispatching.
   - Job health monitoring with automatic dispatch of `failed_job` alerts.

---
""")

    # 29. Final System Summary
    sections.append("""## 29. Final System Summary

The reverse-engineering of the 17 provided HTML artifacts establishes that **SavingsPlus** is an enterprise Core Banking System (CBS) designed for **NSIMBI SACCO LTD** by **FutureLink Technologies (FLT)**. 

The backend architecture requires:
- **Comprehensive Customer Information File (CIF):** Supporting Individuals, Groups, Joints, and Institutions with maker-checker onboarding.
- **Granular Security Framework:** 34 modules, 382 permissions, strict operating time windows, 5-minute idle timeouts, and individual financial limit ceilings across 9 transaction classes.
- **Integrated Operations:** End-to-end management of Savings, Loans, Shares, Accounting, Fixed Assets, Staff Payroll, External Float (MTN, Airtel, MSACCO, Bank), and Regulatory Compliance (BOU & UMRA).

This specification provides the exhaustive, unambiguous foundation for backend developers and automated coding agents to implement the database models, business logic services, and REST API controllers for the NSIMBI Core Banking System.
""")

    full_text = "
".join(sections)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write(full_text)
    
    # Also write a copy to ~/Downloads/MSACCO/REQUIREMENTS.md for user convenience
    downloads_copy = os.path.expanduser("~/Downloads/MSACCO/REQUIREMENTS.md")
    with open(downloads_copy, "w", encoding="utf-8") as f:
        f.write(full_text)

    print(f"REQUIREMENTS.md generated successfully at:
 - {OUTPUT_FILE}
 - {downloads_copy}")
    print(f"Total lines: {len(full_text.splitlines())}")

if __name__ == "__main__":
    build_requirements()

### FR-015 — Savings Account Directory & Status Filtering
- **Requirement:** The system shall list all customer savings accounts with account number, member name, branch, product, opened date, actual balance, available balance, and status filters (`All`, `Active`, `Inactive`, `Frozen`).
- **Entity:** `SavingsAccount`
- **Actor:** Cashier / Customer Care / Manager
- **Source:** `list_savings.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated staff.
- **Input:** Filter criteria (Status, Branch, Search term).
- **Business Rules:** Account number formatted as 12 digits (`003500005281`).
- **Expected Result:** Paginated list of savings accounts.
- **Failure Conditions:** Unauthorized access.
- **Evidence Level:** Explicit

### FR-016 — Savings Account Detail & Cash Desk Terminal
- **Requirement:** The system shall provide an operational cash desk screen for an individual savings account displaying Actual Balance, Available Balance, KYC photo/signature preview, and immediate transaction buttons: Deposit, Withdraw, New Transfers, Debit Account, Credit Account, Close, and Freeze.
- **Entity:** `SavingsAccount`, `SavingsTransaction`
- **Actor:** Cashier / Teller
- **Source:** `saving_account_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active savings account.
- **Input:** Account Number (`003500005281`).
- **Business Rules:** Displays Available Balance (`actual_balance - 5000 UGX reserve`).
- **Expected Result:** Detailed account ledger and action triggers.
- **Failure Conditions:** Account does not exist.
- **Evidence Level:** Explicit

### FR-017 — Savings SMS Alert Rules Configuration
- **Requirement:** The system shall allow operators to configure granular SMS transaction and loan notification subscriptions per savings account across 14 discrete triggers.
- **Entity:** `SavingsSMSAlertConfig`
- **Actor:** Customer Care / Staff
- **Source:** `edit_saving_account.html`, `saving_account_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target savings account exists.
- **Input:** Boolean flags for: On Deposit, On Withdrawal, Earning Interest, Earning Dividends, Outgoing Transfer, Incoming Transfer, Direct Debit, Direct Credit, Standing Order, Loan Application, Loan Disbursement, Loan Payment, Loan Refund.
- **Business Rules:** Saved flags dictate which account activities dispatch transactional SMS messages via the SMS gateway.
- **Expected Result:** SMS alert configuration persisted.
- **Failure Conditions:** Invalid account ID.
- **Evidence Level:** Explicit

### FR-018 — Fixed Deposit Placement & Booking
- **Requirement:** The system shall book term/fixed deposits, specifying principal amount, tenure in months, annual interest rate, interest payment interval, maturity payout option, and funding source (Cash counter vs Savings account offset).
- **Entity:** `FixedDeposit`, `SavingsAccount`
- **Actor:** Customer Care / Branch Manager
- **Source:** `create_fixed_deposit.html`
- **Priority:** MUST HAVE
- **Preconditions:** Member possesses an active savings account.
- **Input:** Member, Transfer/Payout Account, Depositor Name, Depositor Phone, Principal Amount, Starts On, Period in Months, Interest Rate (%), Interest Interval (`monthly`, `yearly`, `Not applicable`), Payout Option (`Principal`, `Principal & Interest`), Funding Method (`Till - Cash or cheque`, `Offset - From savings`).
- **Business Rules:** If offset selected, funds immediately placed on hold/debited from source savings account. Enters Maker-Checker approval queue (`list_fixed_deposit_applications.html`).
- **Expected Result:** Fixed deposit contract created in `PENDING` status.
- **Failure Conditions:** Insufficient available balance in source savings account.
- **Evidence Level:** Explicit

### FR-019 — Fixed Deposit Lifecycle & Termination
- **Requirement:** The system shall track fixed deposits through their complete lifecycle and support both premature and maturity termination with interest calculation.
- **Entity:** `FixedDeposit`
- **Actor:** Branch Manager
- **Source:** `list_fixed_deposits.html`, `fixed_deposit_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Fixed deposit in `RUNNING` or `MATURED_NOT_AWARDED` status.
- **Input:** Deposit ID, Action (`TERMINATE`), Interest Decision (`WITH_INTEREST`, `NO_INTEREST`).
- **Business Rules:** Terminating returns principal (and approved interest) to the designated transfer savings account. Status transitions to `AWARDED_ON_MATURITY`, `TERMINATED_NO_INTEREST`, or `TERMINATED_WITH_INTEREST`.
- **Expected Result:** Funds transferred; contract archived.
- **Failure Conditions:** Contract already liquidated.
- **Evidence Level:** Explicit

### FR-020 — Standing Orders Recurring Execution
- **Requirement:** The system shall maintain standing orders with automated periodic execution and track statuses: `Pending`, `Approved`, `Running`, `Rejected`, `Expired`.
- **Entity:** `StandingOrder`
- **Actor:** Customer Care / Scheduler
- **Source:** `list_standing_orders.html`
- **Priority:** MUST HAVE
- **Preconditions:** Source savings account active.
- **Input:** Source Account, Destination Account, Amount, Frequency, Start Date.
- **Business Rules:** Background worker sweeps source account on execution date. If balance insufficient, records failed execution and dispatches alert `standing_orders_pending_approval`.
- **Expected Result:** Transfer posted between accounts.
- **Failure Conditions:** Expired order or insufficient balance.
- **Evidence Level:** Explicit

### FR-021 — Debit Card Issuance Tracking
- **Requirement:** The system shall manage the requisition, approval, and issuance of co-branded Interswitch debit cards linked to member savings accounts.
- **Entity:** `DebitCardIssuance`
- **Actor:** Customer Care / Branch Manager
- **Source:** `list_debit_card_issuance.html`, `saving_account_detail.html`
- **Priority:** SHOULD HAVE
- **Preconditions:** Active savings account.
- **Input:** Savings Account Number, Card PAN / Serial Number.
- **Business Rules:** Card status tracks through `Pending` -> `Approved` -> `Issued` -> `Rejected`.
- **Expected Result:** Card linked for ATM/POS debit transactions.
- **Failure Conditions:** Account frozen or inactive.
- **Evidence Level:** Explicit

### FR-022 — Loan Amortization Calculator
- **Requirement:** The system shall provide an interactive loan amortization calculator supporting multiple interest calculation methods (`flat`, `declining`, `amortize`) and frequencies (`daily`, `weekly`, `monthly`, `annually`), generating installment amounts and repayment schedules.
- **Entity:** `LoanProduct`, `LoanRepaymentSchedule`
- **Actor:** Loan Officer / Customer
- **Source:** `loan_calculator.html`
- **Priority:** MUST HAVE
- **Preconditions:** Loan products configured in system.
- **Input:** Product ID, Principal Amount, Interest Method, Annual Interest Rate, Frequency, Number of Installments, Start Date, Grace Period (Months).
- **Business Rules:** Generates accurate mathematical repayment schedule preview.
- **Expected Result:** Installment amount and period-by-period breakdown of Principal, Interest, and Remaining Balance.
- **Failure Conditions:** Negative principal or rate.
- **Evidence Level:** Explicit

### FR-023 — Credit Reference Bureau (CRB / CBA) Vetting
- **Requirement:** The system shall integrate credit reporting workflows allowing officers to record borrower consent and perform credit vetting against Credit Bureau Africa (CBA).
- **Entity:** `CreditReportingInquiry`
- **Actor:** Loan Officer
- **Source:** `list_credit_reporting.html`
- **Priority:** MUST HAVE
- **Preconditions:** Borrower signed CRB consent form (`initiate crb report consent`).
- **Input:** Member NIN / Account Number, Bureau Identifier (`CBA`).
- **Business Rules:** Credit check results logged in bureau register (`view crb register`) prior to loan approval.
- **Expected Result:** Credit history report and risk rating returned.
- **Failure Conditions:** Bureau service unavailable.
- **Evidence Level:** Explicit

### FR-024 — Loan Servicing, Auto-Pay & Repayments
- **Requirement:** The system shall manage active loan contracts, displaying disbursement date, maturity date, interest rate (e.g. `42.0%`), loan officer, and linked repayment account, supporting manual payment posting and automated savings sweeps.
- **Entity:** `Loan`, `SavingsAccount`, `LoanRepayment`
- **Actor:** Loan Officer / Cashier / System Scheduler
- **Source:** `loan_detail.html`, `list_loans.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active loan contract in `RUNNING` status.
- **Input:** Loan Number (`1D04FE1229`), Payment Amount, Payment Mode.
- **Business Rules:** If `auto_pay_from_savings=true`, system automatically debits linked savings account (`003500000651`) on due date.
- **Expected Result:** Installment credited against outstanding interest and principal.
- **Failure Conditions:** Delinquent repayment incurs late penalties.
- **Evidence Level:** Explicit

### FR-025 — Loan Restructuring, Waivers & Legal Recovery
- **Requirement:** The system shall support credit risk interventions including interest waivers, penalty waivers, admin fee waivers, grace period additions, officer re-assignments, loan restructuring/rescheduling (generating new loan numbers referencing original facility), legal referrals (`Send to legal`), and bad debt write-offs (`Write off`).
- **Entity:** `Loan`
- **Actor:** Credit Manager
- **Source:** `loan_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Delinquent or performing loan requiring administrative modification.
- **Input:** Loan Number, Action (`WAIVE_INTEREST`, `WAIVE_PENALTY`, `WAIVE_ADMIN_FEES`, `RESCHEDULE`, `ADD_GRACE_PERIOD`, `TRANSFER_OFFICER`, `SEND_TO_LEGAL`, `WRITE_OFF`), Justification Notes, Adjusted Terms.
- **Business Rules:** Waivers post corresponding reversing entries. Rescheduling closes old loan and books new loan contract. Write-offs post against Loan Loss Provision accounts.
- **Expected Result:** Loan contract state and ledger updated.
- **Failure Conditions:** Insufficient authorization limits.
- **Evidence Level:** Explicit

---
""")

    # 15. API Requirements
    s.append("""## 15. API Requirements

### Extended API Catalog (Incorporating Savings & Loans)
- `GET /savings` — Query savings accounts with status filters (`active`, `inactive`, `frozen`).
- `GET /savings/{number}` — Fetch detailed account overview, available vs actual balance, and recent ledger entries.
- `POST /savings/{number}/deposit` — Post cash or counter deposit to savings account.
- `POST /savings/{number}/create-individual-withdrawal` — Post cash withdrawal from savings account.
- `POST /savings/{number}/direct-transfers` — Execute inter-account transfer.
- `POST /savings/{number}/freeze` / `POST /savings/{number}/unfreeze` — Toggle administrative hold.
- `POST /savings/{number}/close` — Liquidate balance and close account.
- `PATCH /savings/{number}/alerts` — Update 14 SMS alert rule checkboxes.
- `GET /savings/applications` — Fetch pending savings account opening applications.
- `POST /savings/applications/{id}/approve` — Approve savings account opening.
- `GET /savings/fixed-deposits` — List term deposits with 7 lifecycle status filters.
- `POST /savings/fixed-deposits` — Book new fixed deposit (Till or Savings Offset).
- `GET /savings/fixed-deposits/{id}` — Fetch fixed deposit terms and certificate data.
- `POST /savings/fixed-deposits/{id}/terminate` — Liquidate fixed deposit before or at maturity.
- `GET /savings/standing-orders` — List recurring standing orders.
- `POST /savings/{number}/standing-orders/create` — Setup new recurring standing order.
- `GET /savings/debit-cards` — List debit card issuance requests and statuses.
- `POST /savings/{number}/debit-cards` — Request Interswitch debit card issuance.
- `GET /loans` — List active loan facilities with portfolio filters (`outstanding`, `due`, `performing`, `sent_to_legal`).
- `GET /loans/{number}` — Fetch full loan details, linked repayment account, and amortization schedule.
- `POST /loans/calculator` — Calculate amortization schedule based on method (`flat`, `declining`, `amortize`).
- `GET /loans/applications` — Fetch loan origination pipeline (`approved`, `pending`, `rejected`, `cancelled`).
- `POST /loans/applications` — Submit new loan origination application.
- `POST /loans/applications/{id}/approve` — Sanction loan application.
- `POST /loans/applications/{id}/disburse` — Disburse approved funds into member savings account.
- `POST /loans/{number}/repayments` — Post manual loan repayment.
- `POST /loans/{number}/waiver-interest` — Waive accrued interest.
- `POST /loans/{number}/waiver-penalty` — Waive late payment penalties.
- `POST /loans/{number}/reschedule` — Restructure loan and issue new facility reference.
- `POST /loans/{number}/send-to-legal` — Forward defaulted loan to legal debt collection.
- `POST /loans/{number}/write-off` — Write off bad debt against loan loss provisions.
- `GET /loans/credit-reporting` — Query credit bureau history (CBA).

---
""")

    # 16. Data / Database Requirements
    s.append("""## 16. Data / Database Requirements

### Schema Additions for Savings and Loans
```sql
-- Savings Products Table
CREATE TABLE savings_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'FlexSave'
    code VARCHAR(20) UNIQUE NOT NULL,
    minimum_balance DECIMAL(18,2) NOT NULL DEFAULT 5000.00,
    opening_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_rate_annual DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Savings Accounts Table
CREATE TABLE savings_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(30) UNIQUE NOT NULL, -- e.g. '003500005281'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    product_id INTEGER NOT NULL REFERENCES savings_products(id),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED')),
    actual_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    opening_fee_charged BOOLEAN NOT NULL DEFAULT FALSE,
    last_ledger_fee_charged_at TIMESTAMP WITH TIME ZONE,
    last_transaction_at TIMESTAMP WITH TIME ZONE,
    opened_at DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Savings SMS Alert Rules
CREATE TABLE savings_sms_alert_configs (
    account_id BIGINT PRIMARY KEY REFERENCES savings_accounts(id) ON DELETE CASCADE,
    notify_on_deposit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_withdrawal BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_earning_interest BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_earning_dividends BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_outgoing_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_incoming_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_debit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_credit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_standing_order BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_application BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_loan_disbursement BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_payment BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_refund BOOLEAN NOT NULL DEFAULT FALSE
);

-- Fixed Deposits Table
CREATE TABLE fixed_deposits (
    id BIGSERIAL PRIMARY KEY,
    deposit_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    funding_source VARCHAR(30) NOT NULL CHECK (funding_source IN ('TILL_CASH', 'SAVINGS_OFFSET')),
    funding_savings_account_id BIGINT REFERENCES savings_accounts(id),
    payout_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    depositor_name VARCHAR(150) NOT NULL,
    depositor_phone VARCHAR(30) NOT NULL,
    principal_amount DECIMAL(18,2) NOT NULL,
    starts_on DATE NOT NULL,
    period_months INTEGER NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_interval VARCHAR(30) NOT NULL CHECK (interest_interval IN ('monthly', 'yearly', 'Not applicable')),
    payout_option VARCHAR(50) NOT NULL CHECK (payout_option IN ('Principal', 'Principal & Interest')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RUNNING', 'AWARDED', 'TERMINATED', 'MATURED_NOT_AWARDED', 'TERMINATED_NO_INTEREST', 'AWARDED_ON_MATURITY', 'TERMINATED_WITH_INTEREST')),
    matures_at DATE NOT NULL,
    accrued_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Standing Orders Table
CREATE TABLE standing_orders (
    id BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    destination_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    amount DECIMAL(18,2) NOT NULL,
    frequency VARCHAR(30) NOT NULL CHECK (frequency IN ('daily', 'weekly', 'monthly')),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'RUNNING', 'REJECTED', 'EXPIRED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Products Table
CREATE TABLE loan_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'NCHIEI Credit', 'Business Loans'
    code VARCHAR(20) UNIQUE NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    default_annual_interest_rate DECIMAL(5,2) NOT NULL,
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    min_principal DECIMAL(18,2) NOT NULL,
    max_principal DECIMAL(18,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Loans Table
CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    loan_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '1D04FE1229'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    loan_officer_id BIGINT NOT NULL REFERENCES users(id),
    product_id INTEGER NOT NULL REFERENCES loan_products(id),
    repayment_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    principal_disbursed DECIMAL(18,2) NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    installments_count INTEGER NOT NULL,
    grace_period_months INTEGER NOT NULL DEFAULT 0,
    installment_amount DECIMAL(18,2) NOT NULL,
    auto_pay_from_savings BOOLEAN NOT NULL DEFAULT TRUE,
    disbursed_at DATE NOT NULL,
    maturity_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING', 'PERFORMING', 'DUE', 'OUTSTANDING', 'SENT_TO_LEGAL', 'WRITTEN_OFF', 'RESCHEDULED', 'CLEARED')),
    rescheduled_from_loan_number VARCHAR(50),
    total_principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_penalty_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_penalty DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_admin_fees DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Repayment Schedules Table
CREATE TABLE loan_repayment_schedules (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(18,2) NOT NULL,
    interest_due DECIMAL(18,2) NOT NULL,
    total_installment DECIMAL(18,2) NOT NULL,
    principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    is_settled BOOLEAN NOT NULL DEFAULT FALSE
);
```

---
""")

    # 17 to 26 - Retain & Expand
    s.append("""## 17. Search, Filtering and Reporting

### Savings & Loan Specific Reporting Capabilities
From the 109 cataloged reports, the following are directly confirmed by the savings and loan captures:
- `view savings accounts`, `view savings ledger`, `view savings transactions`, `view failed standing orders`.
- `view fixed deposits`, `view fixed certificate`.
- `view loan applications`, `view loan arrears`, `view loan bookings`, `view loan schedule`, `view loan ledger`, `view loan repayments`, `view loan repayment summary`, `view cleared loans`, `view due loans`, `view written off loans`, `view rescheduled loans`.
- `view par by ageing`, `view par by ageing summary`, `view umra par by ageing` (Portfolio At Risk aging schedules for regulatory reporting).
- `view crb register`, `download crb files` (Credit Reference Bureau returns).

---

### FR-026 — Chart of Accounts Hierarchy Management
- **Requirement:** The system shall maintain a 5-tier hierarchical Chart of Accounts (Assets, Liabilities, Capital, Income, Expenses) with dash-separated account codes, supporting node creation, updates, and deactivation.
- **Entity:** `ChartOfAccount`
- **Actor:** Head of Finance / Accountant
- **Source:** `list_charts_of_accounts_assets.html`, `create_chart_of_account_asset.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated user with `register chart account` permission.
- **Input:** Account Name*, Parent Account Node, Subtype (Optional), Tags (Optional).
- **Business Rules:** System generates dash-separated hierarchical code; cannot deactivate an account with a non-zero balance.
- **Expected Result:** New GL node created in Chart of Accounts.
- **Failure Conditions:** Duplicate account code.
- **Evidence Level:** Explicit

### FR-027 — Double-Entry Journal Voucher Creation & Reversal
- **Requirement:** The system shall capture balanced double-entry Journal Vouchers across branches and ledger accounts, supporting maker-checker reversal workflows.
- **Entity:** `JournalVoucher`, `JournalVoucherItem`
- **Actor:** Accountant / Accounts Assistant
- **Source:** `create_journal.html`, `journals.html.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid active accounts in Chart of Accounts.
- **Input:** Contact Person*, Registration Date*, Reference Number*, Method (`cash`, `cheque`)*, Comment*, Debit Account*, Credit Account*, Amount*.
- **Business Rules:** Total debits must equal total credits; reversals must reference original voucher and provide justification.
- **Expected Result:** Journal voucher posted to General Ledger or queued for approval.
- **Failure Conditions:** Debits != Credits (`ERR_TB_OUT_OF_BALANCE`).
- **Evidence Level:** Explicit

### FR-028 — Financial Period Closing, TB Archival & Rollback Protection
- **Requirement:** The system shall close financial periods, generate an immutable Trial Balance snapshot PDF, lock the period against backdated transactions, and allow authorized rollback.
- **Entity:** `FinancialPeriod`
- **Actor:** Head of Finance / Administrator
- **Source:** `closing_and_restore_books.html`
- **Priority:** MUST HAVE
- **Preconditions:** All subledgers reconciled; Trial Balance in balance.
- **Input:** Period End Date, Action (`CLOSE`, `ROLLBACK`).
- **Business Rules:** Closing generates `Download TB(pdf)` snapshot; rollback triggers alert `opening_a_closed_period`.
- **Expected Result:** Period locked or reopened.
- **Failure Conditions:** Out of balance Trial Balance (`tb_imbalance`).
- **Evidence Level:** Explicit

### FR-029 — Financial Approvals Maker-Checker Queue
- **Requirement:** The system shall provide an approval queue for accounting transactions, journal vouchers, and bank transfers with pending, rejected, and approved queues.
- **Entity:** `FinancialApproval`
- **Actor:** Branch Manager / Head of Finance
- **Source:** `approvals.html`
- **Priority:** MUST HAVE
- **Preconditions:** Transaction submitted by maker.
- **Input:** Approval ID, Decision (`APPROVE`, `REJECT`), Reason.
- **Business Rules:** Checker cannot be maker (BR-001).
- **Expected Result:** Transaction committed to ledger or rejected.
- **Failure Conditions:** Checker is maker.
- **Evidence Level:** Explicit

### FR-030 — Fixed Asset Register & Depreciation
- **Requirement:** The system shall maintain a register of physical assets and asset profiles, supporting periodic depreciation calculation, revaluations, and disposals.
- **Entity:** `FixedAsset`, `AssetProfile`
- **Actor:** Accountant
- **Source:** `list_fixed_assets.html`, `list_asset_profiles.html`
- **Priority:** SHOULD HAVE
- **Preconditions:** Asset profile defined.
- **Input:** Asset Name, Profile, Purchase Date, Cost, Salvage Value, Useful Life.
- **Business Rules:** Depreciation posts monthly debit to Depreciation Expense and credit to Accumulated Depreciation.
- **Expected Result:** Asset tracked on balance sheet.
- **Failure Conditions:** Invalid profile.
- **Evidence Level:** Explicit

### FR-031 — Member Share Capital Administration
- **Requirement:** The system shall manage member share equity accounts, recording share capital subscriptions, purchases, transfers, and liquidations.
- **Entity:** `ShareAccount`, `ShareTransaction`
- **Actor:** Customer Care / Accountant
- **Source:** `list_shares.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active member.
- **Input:** Member ID, Number of Shares, Unit Price.
- **Business Rules:** Total share capital reflects under Capital tier of Chart of Accounts.
- **Expected Result:** Member share balance updated.
- **Failure Conditions:** Member inactive.
- **Evidence Level:** Explicit

### FR-032 — Password-Authenticated Share Dividend Payout
- **Requirement:** The system shall declare and distribute share dividends based on shareholding periods, requiring staff password re-authentication, supporting distribution directly into savings accounts or equity capitalization.
- **Entity:** `DividendDeclaration`
- **Actor:** Head of Finance / General Manager
- **Source:** `share_dividends.html`
- **Priority:** MUST HAVE
- **Preconditions:** Approved dividend pool; member shareholdings verified.
- **Input:** Amount*, Use Percentages*, Date*, Share Period (Months)*, Method (`savings`, `shares`)*, Saving Product, Reference Number*, Comment*, Password*.
- **Business Rules:** Password re-verified; credits destination savings product (`FlexSave`) or increases member shares held.
- **Expected Result:** Dividend disbursed across all qualifying members; event logged.
- **Failure Conditions:** Incorrect password (`ERR_INVALID_PASSWORD`).
- **Evidence Level:** Explicit

### FR-033 — Multi-Entity Batch Data Import Engine
- **Requirement:** The system shall provide batch data ingestion with downloadable templates and preview validation across 9 entity types: Individuals, Institutions, Joint Accounts, Groups, Group Members, Loans, Fixed Deposits, Savings, and savingsAccounts.
- **Entity:** `DataImportBatch`
- **Actor:** System Administrator / Migration Specialist
- **Source:** `imports.html`, `preview_import.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid CSV template.
- **Input:** Template Type*, Branch*, Reference Number*, Document File*.
- **Business Rules:** Preview validates column mappings and schema errors prior to commit.
- **Expected Result:** Bulk records ingested into respective database tables.
- **Failure Conditions:** File parsing failure or validation mismatch.
- **Evidence Level:** Explicit

---
""")

    # 15. API Requirements
    s.append("""## 15. API Requirements

### Extended API Catalog (Incorporating Savings & Loans)
- `GET /savings` — Query savings accounts with status filters (`active`, `inactive`, `frozen`).
- `GET /savings/{number}` — Fetch detailed account overview, available vs actual balance, and recent ledger entries.
- `POST /savings/{number}/deposit` — Post cash or counter deposit to savings account.
- `POST /savings/{number}/create-individual-withdrawal` — Post cash withdrawal from savings account.
- `POST /savings/{number}/direct-transfers` — Execute inter-account transfer.
- `POST /savings/{number}/freeze` / `POST /savings/{number}/unfreeze` — Toggle administrative hold.
- `POST /savings/{number}/close` — Liquidate balance and close account.
- `PATCH /savings/{number}/alerts` — Update 14 SMS alert rule checkboxes.
- `GET /savings/applications` — Fetch pending savings account opening applications.
- `POST /savings/applications/{id}/approve` — Approve savings account opening.
- `GET /savings/fixed-deposits` — List term deposits with 7 lifecycle status filters.
- `POST /savings/fixed-deposits` — Book new fixed deposit (Till or Savings Offset).
- `GET /savings/fixed-deposits/{id}` — Fetch fixed deposit terms and certificate data.
- `POST /savings/fixed-deposits/{id}/terminate` — Liquidate fixed deposit before or at maturity.
- `GET /savings/standing-orders` — List recurring standing orders.
- `POST /savings/{number}/standing-orders/create` — Setup new recurring standing order.
- `GET /savings/debit-cards` — List debit card issuance requests and statuses.
- `POST /savings/{number}/debit-cards` — Request Interswitch debit card issuance.
- `GET /loans` — List active loan facilities with portfolio filters (`outstanding`, `due`, `performing`, `sent_to_legal`).
- `GET /loans/{number}` — Fetch full loan details, linked repayment account, and amortization schedule.
- `POST /loans/calculator` — Calculate amortization schedule based on method (`flat`, `declining`, `amortize`).
- `GET /loans/applications` — Fetch loan origination pipeline (`approved`, `pending`, `rejected`, `cancelled`).
- `POST /loans/applications` — Submit new loan origination application.
- `POST /loans/applications/{id}/approve` — Sanction loan application.
- `POST /loans/applications/{id}/disburse` — Disburse approved funds into member savings account.
- `POST /loans/{number}/repayments` — Post manual loan repayment.
- `POST /loans/{number}/waiver-interest` — Waive accrued interest.
- `POST /loans/{number}/waiver-penalty` — Waive late payment penalties.
- `POST /loans/{number}/reschedule` — Restructure loan and issue new facility reference.
- `POST /loans/{number}/send-to-legal` — Forward defaulted loan to legal debt collection.
- `POST /loans/{number}/write-off` — Write off bad debt against loan loss provisions.
- `GET /loans/credit-reporting` — Query credit bureau history (CBA).

---
""")

    # 16. Data / Database Requirements
    s.append("""## 16. Data / Database Requirements

### Schema Additions for Savings and Loans
```sql
-- Savings Products Table
CREATE TABLE savings_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'FlexSave'
    code VARCHAR(20) UNIQUE NOT NULL,
    minimum_balance DECIMAL(18,2) NOT NULL DEFAULT 5000.00,
    opening_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_rate_annual DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Savings Accounts Table
CREATE TABLE savings_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(30) UNIQUE NOT NULL, -- e.g. '003500005281'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    product_id INTEGER NOT NULL REFERENCES savings_products(id),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED')),
    actual_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    opening_fee_charged BOOLEAN NOT NULL DEFAULT FALSE,
    last_ledger_fee_charged_at TIMESTAMP WITH TIME ZONE,
    last_transaction_at TIMESTAMP WITH TIME ZONE,
    opened_at DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Savings SMS Alert Rules
CREATE TABLE savings_sms_alert_configs (
    account_id BIGINT PRIMARY KEY REFERENCES savings_accounts(id) ON DELETE CASCADE,
    notify_on_deposit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_withdrawal BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_earning_interest BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_earning_dividends BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_outgoing_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_incoming_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_debit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_credit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_standing_order BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_application BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_loan_disbursement BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_payment BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_refund BOOLEAN NOT NULL DEFAULT FALSE
);

-- Fixed Deposits Table
CREATE TABLE fixed_deposits (
    id BIGSERIAL PRIMARY KEY,
    deposit_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    funding_source VARCHAR(30) NOT NULL CHECK (funding_source IN ('TILL_CASH', 'SAVINGS_OFFSET')),
    funding_savings_account_id BIGINT REFERENCES savings_accounts(id),
    payout_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    depositor_name VARCHAR(150) NOT NULL,
    depositor_phone VARCHAR(30) NOT NULL,
    principal_amount DECIMAL(18,2) NOT NULL,
    starts_on DATE NOT NULL,
    period_months INTEGER NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_interval VARCHAR(30) NOT NULL CHECK (interest_interval IN ('monthly', 'yearly', 'Not applicable')),
    payout_option VARCHAR(50) NOT NULL CHECK (payout_option IN ('Principal', 'Principal & Interest')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RUNNING', 'AWARDED', 'TERMINATED', 'MATURED_NOT_AWARDED', 'TERMINATED_NO_INTEREST', 'AWARDED_ON_MATURITY', 'TERMINATED_WITH_INTEREST')),
    matures_at DATE NOT NULL,
    accrued_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Standing Orders Table
CREATE TABLE standing_orders (
    id BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    destination_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    amount DECIMAL(18,2) NOT NULL,
    frequency VARCHAR(30) NOT NULL CHECK (frequency IN ('daily', 'weekly', 'monthly')),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'RUNNING', 'REJECTED', 'EXPIRED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Products Table
CREATE TABLE loan_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'NCHIEI Credit', 'Business Loans'
    code VARCHAR(20) UNIQUE NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    default_annual_interest_rate DECIMAL(5,2) NOT NULL,
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    min_principal DECIMAL(18,2) NOT NULL,
    max_principal DECIMAL(18,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Loans Table
CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    loan_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '1D04FE1229'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    loan_officer_id BIGINT NOT NULL REFERENCES users(id),
    product_id INTEGER NOT NULL REFERENCES loan_products(id),
    repayment_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    principal_disbursed DECIMAL(18,2) NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    installments_count INTEGER NOT NULL,
    grace_period_months INTEGER NOT NULL DEFAULT 0,
    installment_amount DECIMAL(18,2) NOT NULL,
    auto_pay_from_savings BOOLEAN NOT NULL DEFAULT TRUE,
    disbursed_at DATE NOT NULL,
    maturity_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING', 'PERFORMING', 'DUE', 'OUTSTANDING', 'SENT_TO_LEGAL', 'WRITTEN_OFF', 'RESCHEDULED', 'CLEARED')),
    rescheduled_from_loan_number VARCHAR(50),
    total_principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_penalty_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_penalty DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_admin_fees DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Repayment Schedules Table
CREATE TABLE loan_repayment_schedules (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(18,2) NOT NULL,
    interest_due DECIMAL(18,2) NOT NULL,
    total_installment DECIMAL(18,2) NOT NULL,
    principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    is_settled BOOLEAN NOT NULL DEFAULT FALSE
);
```

---
""")

    # 17 to 26 - Retain & Expand
    s.append("""## 17. Search, Filtering and Reporting

### Savings & Loan Specific Reporting Capabilities
From the 109 cataloged reports, the following are directly confirmed by the savings and loan captures:
- `view savings accounts`, `view savings ledger`, `view savings transactions`, `view failed standing orders`.
- `view fixed deposits`, `view fixed certificate`.
- `view loan applications`, `view loan arrears`, `view loan bookings`, `view loan schedule`, `view loan ledger`, `view loan repayments`, `view loan repayment summary`, `view cleared loans`, `view due loans`, `view written off loans`, `view rescheduled loans`.
- `view par by ageing`, `view par by ageing summary`, `view umra par by ageing` (Portfolio At Risk aging schedules for regulatory reporting).
- `view crb register`, `download crb files` (Credit Reference Bureau returns).

---

### BR-012: Double-Entry Balancing Invariant
- **Rule:** Every Journal Voucher must have equal debits and credits: `SUM(debit_amount) == SUM(credit_amount)`. Imbalanced vouchers must be rejected with HTTP 400 (`ERR_TB_OUT_OF_BALANCE`).

### BR-013: Teller Drawer GL Integration
- **Rule:** Physical teller cash drawers (e.g. `1-1-1-1-2 Teller - 1`) are literal asset accounts under `1-1-1-1 CASH AT HAND`. Any cash desk deposit, withdrawal, or till transfer must post directly to the teller's assigned GL code.

### BR-014: Password Re-Authentication on Share Dividends
- **Rule:** Authorizing a dividend declaration requires re-entering the staff user's login password (`Password*`). The backend must re-verify credentials before executing payouts.

### BR-015: Frozen Period Immutable Lock
- **Rule:** No transaction or journal voucher may be posted with a value date inside a closed financial period unless the period has been explicitly rolled back.

### BR-016: Trial Balance Imbalance Alarm during Period Close
- **Rule:** Closing a financial period performs an automated ledger audit. If debits != credits, the period closing is blocked and alert `tb_imbalance` is dispatched.

---

## 11. Validation Rules

### Confirmed Validations (Explicit in HTML)
1. **Required Fields:**
   - Individual: Branch, Salutation, First Name, Last Name, Gender, Marital Status, DOB, Country of Birth, Nationality, Home Ownership, Identification Type & Number, Entry Date, Phone Number, Country, Address, Next of Kin, Occupation, Employer Name/Address, Income Frequency/Amount, Signature, Identification Document, Profile Photo.
   - Group: Branch, Group Name, Group Type, Registration Number, Entry Date, Phone, Country, Physical Address.
   - Institution: Branch, Institution Name, Registration Number/Date, TIN Number, Business Type, Institution Category, Residence Ownership, Entry Date, Phone, Country, Address, Income Frequency/Amount.
   - User: First Name, Last Name, Username, Email Address, Password, Phone, Role, Branch, all 9 Min/Max Limit pairs.
   - Fixed Deposit: Member, Transfer Account, Depositor Name/Phone, Amount, Starts On, Period in Months, Interest Rate, Interest Interval, Payout Option.
   - Loan Calculator: Product, Amount, Interest Method, Annual Interest Rate, Frequency, Number of Installments, Start Date, Installment Amount.
   - Journal Voucher: Contact Person, Registration Date, Reference Number, Method, Comment, Entry Type, Debit Account, Credit Account, Balanced Amounts.
   - Dividend Declaration: Amount, Date, Share Period, Method, Saving Product, Reference Number, Comment, Password.
2. **Numeric Constraints:**
   - All financial amounts must be strictly positive decimals.
   - Min Amount must be `<= Max Amount` across all user limit pairs.
   - Total debits must equal total credits on Journal Vouchers.

---

## 12. Authentication Requirements

- Standard session authentication via JWT (`/api/auth/login`).
- 5-minute inactivity lockdown strictly monitored by front-end and enforced by backend session expiry.
- SMS OTP two-factor authentication toggled per user account (`/users/{id}/toggle-sms-otp`).
- Mandatory password re-verification on sensitive equity payouts (`share_dividends.html`).

---

## 13. Authorization Requirements

Complete mapping across all 34 modules and 382 permissions confirmed in previous sections, including:
- `savings` (24 permissions): standing orders, statements, reverse withdrawal/deposit, deposits, withdrawals, transfers, direct debit/credit.
- `loans` (45 permissions): CRB consent/report, sponsor, origination, approval, disbursement, repayment, refund, waivers, legal, write-off, rescheduling.
- `accounting` (19 permissions): register/reverse journal, approvals, bank transfers, divergent branch journals, budgets, period closing.
- `shares` (13 permissions): share purchase, transfer, liquidation, share value, dividends, rollback dividends.
- `chart` (5 permissions): register, update, activate, deactivate, delete chart account.
- `fixed assets` (9 permissions): register, update, delete, depreciation, dispose, revalue, transfer.
- `users` (10 permissions) & `branches` (5 permissions).

---

## 14. Functional Requirements

### FR-001 — Member Category Selection
- **Requirement:** The system shall present a registration gateway allowing the operator to select one of four member entity types: Individual, Group, Joint, or Institution.
- **Entity:** `Customer`
- **Actor:** Customer Care / Staff
- **Source:** `create_member_step_one.html`
- **Priority:** MUST HAVE
- **Preconditions:** Staff is authenticated and holds permission `register individual customer` / `register group customer` / etc.
- **Input:** Selected entity type (`INDIVIDUAL`, `GROUP`, `JOINT`, `INSTITUTION`).
- **Business Rules:** Navigates directly to the specialized multi-field schema for the chosen category.
- **Expected Result:** Renders appropriate registration form.
- **Failure Conditions:** Unauthorized access redirects to 403 Forbidden.
- **Evidence Level:** Explicit

### FR-002 — Individual Member Registration
- **Requirement:** The system shall capture complete individual KYC data across Bio Data, Contact Information, Next of Kin, Employment, Income, Bank Details, and Document Uploads, persisting the record in `PENDING` approval status.
- **Entity:** `Customer`, `CustomerIndividual`, `NextOfKin`, `EmploymentIncome`, `ExternalBank`
- **Actor:** Customer Care / Registration Officer
- **Source:** `create_individual_member.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid branch and designated savings officer selected.
- **Input:** Salutation, Names, Gender, Marital Status, DOB, Nationality, Home Ownership, Identification Type & Number, Entry Date, Phone, Physical Address, Next of Kin details, Occupation, Income Per Month, Signature scan, ID document scan, Photo file.
- **Business Rules:** Form submission enters the Maker-Checker queue (`list_member_applications.html`). System generates unique formatted `member_number` (e.g. `00350000519`).
- **Expected Result:** Member record created in `PENDING` state; event `customer_applications_pending_approval` dispatched.
- **Failure Conditions:** Missing mandatory fields; duplicate identification number / NIN.
- **Evidence Level:** Explicit

### FR-003 — Member Application Approval & Rejection
- **Requirement:** The system shall allow authorized managers to review pending member applications and execute an approval or rejection decision.
- **Entity:** `Customer`
- **Actor:** Branch Manager / Checker
- **Source:** `list_member_applications.html`
- **Priority:** MUST HAVE
- **Preconditions:** Member application exists in `PENDING` status. Checker is not the maker who created the record.
- **Input:** Application ID, Approval Decision (`APPROVE` or `REJECT`), Rejection Reason (if rejected).
- **Business Rules:** On approval, status transitions to `ACTIVE`, core savings passbook ledger is provisioned, and welcome SMS event is triggered. On rejection, status transitions to `REJECTED`.
- **Expected Result:** Application removed from pending queue and visible under appropriate master/rejected directory.
- **Failure Conditions:** Checker is maker (BR-001 violation); application already decided.
- **Evidence Level:** Explicit

### FR-004 — Group Member Registration
- **Requirement:** The system shall register solidarity lending groups and VSLAs with group type classification (Members vs Non Members) and official registration certificates.
- **Entity:** `Customer`, `CustomerGroup`
- **Actor:** Field Officer / Staff
- **Source:** `create_group.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated operator with group registration privilege.
- **Input:** Branch, Group Name, Group Type (`Members` or `Non Members`), Registration Number, Entry Date, Savings Officer, Phone, Address.
- **Business Rules:** Groups may hold group savings accounts and act as institutional borrowing entities.
- **Expected Result:** Group entity persisted in `PENDING` status awaiting approval.
- **Failure Conditions:** Duplicate group registration number within branch.
- **Evidence Level:** Explicit

### FR-005 — Joint Account Registration
- **Requirement:** The system shall register joint member entities shared by two or more individuals with shared contact information and loan limit settings.
- **Entity:** `Customer`, `CustomerJoint`
- **Actor:** Customer Care / Staff
- **Source:** `create_joint.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated operator.
- **Input:** Branch, Joint Name, Entry Date, Assigned Officer, Phone, Address, Loan Limit toggle.
- **Business Rules:** Joint entity requires multi-signatory rules for subsequent withdrawals.
- **Expected Result:** Joint customer record persisted.
- **Failure Conditions:** Missing mandatory branch, name, or phone.
- **Evidence Level:** Explicit

### FR-006 — Institution Customer Registration
- **Requirement:** The system shall register corporate and institutional customers, capturing legal categorization, URA TIN numbers, corporate category, and business sectors.
- **Entity:** `Customer`, `CustomerInstitution`
- **Actor:** Business Development / Customer Care
- **Source:** `create_institution.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated staff.
- **Input:** Institution Name, Registration Number, Registration Date, TIN Number, Business Type (e.g. Agriculture, SACCO, MDI, etc.), Institution Category (e.g. Limited Liability, NGO, etc.), Residence Ownership, Entry Date, Phone, Physical Address, Monthly Income.
- **Business Rules:** TIN must be validated for format and uniqueness.
- **Expected Result:** Institutional record created for approval.
- **Failure Conditions:** Duplicate TIN or registration number.
- **Evidence Level:** Explicit

### FR-007 — User Account Provisioning & Financial Authorization Limits
- **Requirement:** The system shall provision staff user accounts with individual operational bounds across 9 financial transaction categories and alert subscription selections.
- **Entity:** `User`, `UserNotificationSubscription`
- **Actor:** System Administrator
- **Source:** `create_user.html`
- **Priority:** MUST HAVE
- **Preconditions:** Administrator holds `register user account` permission.
- **Input:** First Name, Last Name, Username, Email, Password, Phone, Designation/Role, Station/Teller, Branch, 9 Min/Max Limit pairs, Selection of Notification Subscriptions (up to 22 checkboxes).
- **Business Rules:** Password must be hashed securely; Min Amount must not exceed Max Amount; limits strictly enforce maker-checker thresholds at runtime.
- **Expected Result:** User account created and enabled.
- **Failure Conditions:** Duplicate username or email; Min Amount > Max Amount.
- **Evidence Level:** Explicit

### FR-008 — User Lifecycle State Management (Deactivate, Delete, SMS OTP)
- **Requirement:** The system shall provide operational controls on staff user accounts to deactivate active users, soft-delete records, and toggle SMS OTP two-factor authentication.
- **Entity:** `User`
- **Actor:** System Administrator
- **Source:** `list_users.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target user exists.
- **Input:** Target User ID, Action (`DEACTIVATE`, `ACTIVATE`, `DELETE`, `ENABLE_SMS_OTP`, `DISABLE_SMS_OTP`).
- **Business Rules:** Deactivated users cannot authenticate (`ERR_ACCOUNT_INACTIVE`). Deleted users are soft-deleted to preserve transaction audit integrity.
- **Expected Result:** User status updated immediately; active sessions invalidated.
- **Failure Conditions:** Administrator cannot deactivate own account.
- **Evidence Level:** Explicit

### FR-009 — Role Profile Definition & Operating Hours Configuration
- **Requirement:** The system shall allow administrators to create and edit Role Profiles, configuring daily operating time windows (`open_time` to `close_time`) and assigning granular checkboxes from the 382 permission pool.
- **Entity:** `Role`, `RolePermission`
- **Actor:** System Administrator
- **Source:** `create_role.html`, `edit_role.html`, `user_details.html`
- **Priority:** MUST HAVE
- **Preconditions:** Administrator holds `register user profile` or `update user profile`.
- **Input:** Role Name, Open Time (Time picker), Close Time (Time picker), Array of selected permission keys.
- **Business Rules:** Operating hours enforce system access cutoff; permission updates take effect on subsequent token evaluation.
- **Expected Result:** Role persisted and available for user assignment.
- **Failure Conditions:** Empty role name; close time precedes open time.
- **Evidence Level:** Explicit

### FR-010 — Role Activation, Deactivation & Deletion
- **Requirement:** The system shall provide lifecycle management for role profiles, allowing activation, deactivation, and deletion.
- **Entity:** `Role`
- **Actor:** System Administrator
- **Source:** `list_roles.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target role exists.
- **Input:** Role ID, Action (`ACTIVATE`, `DEACTIVATE`, `DELETE`).
- **Business Rules:** A role cannot be deleted if active users are assigned to it (e.g., *"1 User assigned"* guard). Deactivating a role blocks authentication for all assigned users.
- **Expected Result:** Role status updated.
- **Failure Conditions:** Attempting to delete a role with assigned users (`ERR_ROLE_IN_USE`).
- **Evidence Level:** Explicit

### FR-011 — Real-Time Liquidity & Float Balance Monitoring
- **Requirement:** The system shall maintain and expose real-time aggregate balances for external payment channels: MSACCO Credit, MTN Mobile Money Float, Airtel Money Float, Bank Transfer Float, and General Float.
- **Entity:** `ExternalFloatLedger`
- **Actor:** System / Cashier / Manager
- **Source:** Global Top Bar across all 17 HTML files
- **Priority:** MUST HAVE
- **Preconditions:** Integration webhooks and ledger updates configured.
- **Input:** Channel Identifier, Float Inflow / Outflow, Current Balance.
- **Business Rules:** Float is debited on outgoing mobile money cashouts and credited on incoming deposits. Balance warnings fire when float breaches safety floor.
- **Expected Result:** Current float balances returned to client header widgets.
- **Failure Conditions:** Channel gateway timeout.
- **Evidence Level:** Explicit

### FR-012 — 5-Minute Inactivity Session Timeout
- **Requirement:** The system shall track client activity and invalidate sessions exceeding 300 seconds of inactivity.
- **Entity:** `UserSession`
- **Actor:** System / Authentication Engine
- **Source:** Idle Detector component across all 17 HTML files
- **Priority:** MUST HAVE
- **Preconditions:** Active session token.
- **Input:** Last activity timestamp.
- **Business Rules:** Inactivity > 5 minutes invalidates token and forces re-login.
- **Expected Result:** Session expired; client redirected to login.
- **Failure Conditions:** N/A
- **Evidence Level:** Explicit

### FR-013 — Asynchronous Job Monitoring & Failure Notification
- **Requirement:** The system shall capture execution logs for all background tasks and broadcast high-priority failure notifications to subscribed users whenever a job fails.
- **Entity:** `SystemJob`, `JobExecutionLog`, `Notification`
- **Actor:** System / Scheduler Engine
- **Source:** Global Header Alert: *"A recently started job has failed Today 10:26:26 PM"*
- **Priority:** MUST HAVE
- **Preconditions:** Background job dispatched.
- **Input:** Job ID, Status (`SUCCESS`, `FAILED`), Error Details.
- **Business Rules:** Failed job records error in `/settings/jobs/{id}/logs` and sends alert to users subscribed to `failed_job`.
- **Expected Result:** In-app notification with "Mark as read" trigger.
- **Failure Conditions:** Database connection loss during error logging.
- **Evidence Level:** Explicit

### FR-014 — Agency Banking Application Queue & Vetting
- **Requirement:** The system shall provide an application intake and maker-checker queue for third-party banking agents with pending and rejected tabs.
- **Entity:** `AgencyApplication`
- **Actor:** Agency Banking Lead / Manager
- **Source:** `list_agency_applications.html`
- **Priority:** MUST HAVE
- **Preconditions:** Agency application submitted.
- **Input:** Agent ID, Business Name, Location, Branch, Decision (`APPROVE`, `REJECT`).
- **Business Rules:** Approved agents obtain agency terminal credentials and float limits.
- **Expected Result:** Agent status updated.
- **Failure Conditions:** Incomplete KYC documentation.
- **Evidence Level:** Explicit

---
""")

    # 15. API Requirements
    sections.append("""## 15. API Requirements

Based on explicit form submissions, list page interactions, and the 190 discovered routes in the client bundle, the following RESTful API surface is required:

### Authentication & Session Subsystem
- `POST /api/auth/login`
  - **Purpose:** Authenticate staff credentials.
  - **Payload:** `{ username: "...", password: "..." }`
  - **Response:** `{ token: "...", refresh_token: "...", expires_in: 300, user: { ... } }`
  - **Validation & Rules:** Rate-limited; verifies `is_active=true`, `is_blocked=false`, and temporal window `open_time <= now <= close_time`.
- `POST /api/auth/logout`
  - **Purpose:** Invalidate current session token.
- `GET /api/auth/user`
  - **Purpose:** Return current authenticated user profile, permissions, and assigned limits.
- `POST /api/auth/refresh`
  - **Purpose:** Issue fresh JWT against valid refresh token.

### Customers & Membership Subsystem
- `GET /customers`
  - **Purpose:** Query paginated customer directory with filtering.
  - **Query Params:** `type` (individual, group, joint, institution), `branch_id`, `search`, `page`, `per_page`.
- `POST /customers/individuals`
  - **Purpose:** Submit new individual member application.
  - **Payload:** Multipart/form-data containing all Bio Data, Contact, Kin, Employment, Bank details, and uploaded binary files (signature, ID, photo).
  - **Response:** 201 Created `{ id: 1223, member_number: "00350000519", status: "PENDING" }`
- `POST /customers/groups`
  - **Purpose:** Register group member.
  - **Payload:** `{ branch_id, group_name, group_type, registration_number, entry_date, savings_officer_id, phone, address, ... }`
- `POST /customers/joints`
  - **Purpose:** Register joint account entity.
- `POST /customers/institutions`
  - **Purpose:** Register corporate/institutional entity.
  - **Payload:** `{ institution_name, registration_number, registration_date, tin_number, business_type, institution_category, ... }`
- `GET /customers/applications`
  - **Purpose:** Fetch maker-checker queue for member applications.
  - **Query Params:** `status=pending` or `status=rejected`, `branch_id`, `page`.
- `POST /customers/applications/{id}/approve`
  - **Purpose:** Approve pending member application and activate ledgers.
- `POST /customers/applications/{id}/reject`
  - **Purpose:** Reject pending member application with reason notes.
- `GET /customers/agent-applications`
  - **Purpose:** Fetch agency banking applications queue.

### Users & Security Subsystem
- `GET /users`
  - **Purpose:** Fetch list of system users.
  - **Query Params:** `status` (all, active, inactive), `search`, `page`.
- `POST /users`
  - **Purpose:** Provision new user account with transaction limits and notification subscriptions.
  - **Payload:** `{ first_name, last_name, username, email, password, phone, role_id, branch_id, station_id, limits: { ... }, notification_subscriptions: [ ... ] }`
- `PATCH /users/{id}`
  - **Purpose:** Update user details, limits, or assign new role.
- `POST /users/{id}/deactivate`
  - **Purpose:** Deactivate active user.
- `DELETE /users/{id}`
  - **Purpose:** Soft-delete user account.
- `POST /users/{id}/toggle-sms-otp`
  - **Purpose:** Enable or disable 2FA requirement.

### Roles & RBAC Subsystem
- `GET /users/roles`
  - **Purpose:** List all role profiles with assigned user counts and operating hours.
  - **Query Params:** `status` (all, active, inactive).
- `POST /users/roles`
  - **Purpose:** Create new role profile.
  - **Payload:** `{ role_name: "...", open_time: "00:00:00", close_time: "23:00:00", permissions: [ "register deposit", ... ] }`
- `GET /users/roles/{id}`
  - **Purpose:** Retrieve role definition and complete array of assigned permissions.
- `PATCH /users/roles/{id}`
  - **Purpose:** Update role permissions or operating window.
- `POST /users/roles/{id}/activate` / `POST /users/roles/{id}/deactivate`
  - **Purpose:** Toggle operational availability of role.
- `DELETE /users/roles/{id}`
  - **Purpose:** Delete unassigned role.

### Notifications & Float Subsystem
- `GET /notifications`
  - **Purpose:** Fetch current user's unread notifications.
- `POST /notifications/{id}/mark-read`
  - **Purpose:** Mark notification as read.
- `POST /notifications/mark-all-read`
  - **Purpose:** Mark all unread notifications as read.
- `GET /settings/float-balances`
  - **Purpose:** Fetch latest float balances for top-bar indicators (MSACCO, MTN, Airtel, Bank Transfer).

---
""")

    # 16. Data / Database Requirements
    sections.append("""## 16. Data / Database Requirements

### Conceptual Data Model
The database architecture must be a relational database (PostgreSQL recommended) enforcing ACID compliance and double-entry ledger immutability.

```sql
-- Core Branches Table
CREATE TABLE branches (
    id SERIAL PRIMARY KEY,
    branch_code VARCHAR(10) UNIQUE NOT NULL, -- e.g. '0035'
    name VARCHAR(100) NOT NULL, -- e.g. 'KABALE'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Core Roles Table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    open_time TIME NOT NULL DEFAULT '00:00:00',
    close_time TIME NOT NULL DEFAULT '23:59:59',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Role Permissions Table
CREATE TABLE role_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_key VARCHAR(100) NOT NULL,
    UNIQUE(role_id, permission_key)
);

-- Core Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    role_id INTEGER NOT NULL REFERENCES roles(id),
    station_id VARCHAR(50), -- e.g. 'Teller - 1'
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    sms_otp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    
    -- Maker-Checker Financial Limits
    loan_approval_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    loan_approval_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- User Notification Subscriptions Table
CREATE TABLE user_notification_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_key VARCHAR(100) NOT NULL,
    UNIQUE(user_id, event_key)
);

-- Master Customers Table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    member_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '00350000001'
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('INDIVIDUAL', 'GROUP', 'JOINT', 'INSTITUTION')),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    savings_officer_id BIGINT REFERENCES users(id),
    entry_date DATE NOT NULL,
    phone_primary VARCHAR(30) NOT NULL,
    phone_secondary VARCHAR(30),
    email VARCHAR(150),
    country VARCHAR(100) NOT NULL DEFAULT 'Uganda',
    town VARCHAR(100),
    physical_address TEXT NOT NULL,
    postal_code VARCHAR(30),
    postal_address VARCHAR(150),
    has_loan_limit BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'REJECTED', 'INACTIVE', 'DORMANT')),
    created_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Individual Profile Table
CREATE TABLE customer_individuals (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    salutation VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female')),
    marital_status VARCHAR(20) NOT NULL,
    is_dependent BOOLEAN NOT NULL DEFAULT FALSE,
    is_pwd BOOLEAN NOT NULL DEFAULT FALSE,
    date_of_birth DATE NOT NULL,
    country_of_birth VARCHAR(100) NOT NULL,
    nationality VARCHAR(100) NOT NULL DEFAULT 'Ugandan',
    home_ownership VARCHAR(20) NOT NULL CHECK (home_ownership IN ('Owner', 'Tenant', 'Other')),
    identification_type VARCHAR(50) NOT NULL,
    identification_number VARCHAR(100) NOT NULL,
    card_number VARCHAR(100),
    signature_file_path TEXT,
    identification_file_path TEXT,
    photo_file_path TEXT
);

-- Next of Kin Table
CREATE TABLE customer_next_of_kin (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    second_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    physical_address TEXT NOT NULL,
    relationship VARCHAR(30) NOT NULL
);

-- Group Profile Table
CREATE TABLE customer_groups (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    group_name VARCHAR(150) NOT NULL,
    group_type VARCHAR(50) NOT NULL CHECK (group_type IN ('Members', 'Non Members')),
    registration_number VARCHAR(100) NOT NULL
);

-- Joint Profile Table
CREATE TABLE customer_joints (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    joint_name VARCHAR(150) NOT NULL
);

-- Institution Profile Table
CREATE TABLE customer_institutions (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    institution_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100) NOT NULL,
    registration_date DATE NOT NULL,
    tin_number VARCHAR(50) NOT NULL,
    business_type VARCHAR(100) NOT NULL,
    institution_category VARCHAR(100) NOT NULL,
    residence_ownership VARCHAR(50) NOT NULL
);
```

---
""")

    # 17. Search, Filtering and Reporting
    sections.append("""## 17. Search, Filtering and Reporting

### Search Capabilities
- Master member search indexing across: Member Number, Name, Phone Number, and Group Affiliation.
- User search indexing: Username, First Name, Last Name, and Branch.

### Filtering Dimensions
- Status Filters: `All`, `Active`, `Inactive`, `Pending`, `Rejected`.
- Organizational Filters: Branch (`0035 KABALE`), Officer assignment, Customer Subtype.
- Date Range Filters: Registration date, approval date, transaction value date.

### Regulatory & Management Reports Catalog
The system specifies 109 discrete reporting capabilities including:
1. **Central Bank / BOU Returns:** Bank of Uganda Liquidity Return (`bou liquidity`), Bank of Uganda Banking Supervision Return (`bou bsa`).
2. **UMRA Returns:** Uganda Microfinance Regulatory Authority Balance Sheet (`umra balance sheet`), Income Statement (`umra income statement`), and PAR by Aging (`umra par by ageing`).
3. **Credit Operations Reports:** Loan Arrears vs Savings, Portfolio Monitoring, Cleared Loans, Due Loans, Written-off Loans, CRB Registers.
4. **General Ledger & Accounting:** Balance Sheet, Profit & Loss / Income Statement, Cash Flow Statement, Trial Balance, Backdated Transactions, Till Sheet Reconciliation.
5. **Shares & Equity:** Cumulative Shares, Shares Ledger, Dividends Shared Report.

---
""")

    # 18. File and Document Requirements
    sections.append("""## 18. File and Document Requirements

### Upload Specifications
1. **Signature File:** Uploaded during individual registration (`Signature*`). Stored securely; rendered on teller screens for withdrawal verification.
2. **Identification Document Scan:** Uploaded ID card/passport scan (`Indentification*`).
3. **Profile Photo:** Member portrait photograph (`Photo*`).
4. **Bulk Processing Files:** Bulk payroll/disbursement files (`.csv`, `.xlsx`) uploaded for batch processing.
5. **Database Backups:** Compressed database backup files downloadable by administrators (`download backup`, `restore backup`).

---
""")

    # 19. Notification and Event Requirements
    sections.append("""## 19. Notification and Event Requirements

### The 22 Core Notification Triggers
The backend event dispatcher must route notifications based on user subscriptions:
1. `blocked_user`: Dispatched when an account is locked out after consecutive failed authentications.
2. `bulk_files_pending_approval`: Dispatched when an institutional disbursement file is uploaded.
3. `customer_applications_pending_approval`: Dispatched when a member registration form is submitted.
4. `customer_kyc_updated`: Dispatched when member biodata or identity documents are altered.
5. `failed_job`: Dispatched immediately when an asynchronous background job errors out.
6. `fixed_deposit_applications_pending_approval`: Dispatched on term deposit placement.
7. `license_expiry_reminder`: Dispatched when software license term nears expiration.
8. `license_upgraded`: Dispatched when license tier or capacity is expanded.
9. `liquid_assets_balance_running_low`: Dispatched when branch cash or float balances breach minimum reserve.
10. `loan_applications_pending_approval`: Dispatched when a loan officer submits an application.
11. `loan_applications_pending_disbursement`: Dispatched when an approved loan is ready for payout.
12. `msacco_customer_onboarding`: Dispatched when a member self-registers via mobile USSD/App.
13. `msacco_loan_application`: Dispatched on mobile emergency loan request.
14. `opening_a_closed_period`: High-priority alert triggered if an accounting period is unsealed.
15. `reserve_amount_limit`: Dispatched when statutory reserves breach threshold.
16. `savings_account_applications_pending_approval`: Dispatched on savings account opening.
17. `standing_orders_pending_approval`: Dispatched when a recurring payment rule is established.
18. `tb_imbalance`: Critical financial alarm fired when Trial Balance debits != credits.
19. `transaction_mismatch`: Dispatched on settlement variance between CBS and mobile money/bank float.
20. `transactions_pending_approval`: Dispatched when a transaction breaches a teller's posting limit.
21. `upcoming_holidays`: Dispatched in advance of statutory holidays affecting loan schedules.
22. `updated_user_profile`: Dispatched when administrative permissions or limits change.

---
""")

    # 20. Audit and History Requirements
    sections.append("""## 20. Audit and History Requirements

### Immutability & Audit Trails
- **Audit Fields:** Every database entity must include `created_at`, `updated_at`, `created_by`, and `approved_by`.
- **System Audit Trail (`view audit trail`):** Comprehensive record capturing timestamp, actor user ID, client IP address, action performed, entity affected, old state, and new state.
- **Financial Immutability:** No financial transaction may be hard-deleted or updated in-place. Corrections must occur strictly via reversing entries (`reverse journal`, `reverse withdrawal`, `reverse deposit`, `reverse share purchase`).
- **Backdated Transaction Audit (`view backdated transactions`):** Specialized tracking for any entry posted with a historical value date.

---
""")

    # 21. Error Handling Requirements
    sections.append("""## 21. Error Handling Requirements

### Standard Error Taxonomy
- `400 Bad Request`: Input validation failure (`ERR_VALIDATION_FAILED`), duplicate unique field (`ERR_DUPLICATE_NIN`, `ERR_DUPLICATE_USERNAME`).
- `401 Unauthorized`: Missing, expired, or invalid bearer token (`ERR_SESSION_EXPIRED`, `ERR_UNAUTHENTICATED`).
- `403 Forbidden`:
  - Temporal restriction: `ERR_OUTSIDE_OPERATING_HOURS`.
  - Permission restriction: `ERR_PERMISSION_DENIED`.
  - Transaction limit restriction: `ERR_EXCEEDS_USER_LIMIT`.
  - Maker-checker violation: `ERR_CANNOT_APPROVE_OWN_RECORD`.
- `404 Not Found`: Target entity not found (`ERR_MEMBER_NOT_FOUND`).
- `409 Conflict`: Invalid state transition (`ERR_INVALID_STATUS_TRANSITION`), period already closed (`ERR_PERIOD_CLOSED`), Trial Balance imbalance (`ERR_TB_IMBALANCE`).
- `500 Internal Server Error`: Asynchronous job failure (`ERR_JOB_FAILED`), unexpected exception.

---
""")

    # 22. Security Requirements
    sections.append("""## 22. Security Requirements

### Observed Security Requirements
1. **Operating Hours Access Control:** Strictly enforced login and execution window per role.
2. **Idle Session Invalidation:** 5-minute timeout.
3. **Transaction Boundary Protection:** User min/max amount caps.
4. **Segregation of Duties (SoD):** Maker-checker separation for applications and transactions.
5. **Database Restoration Mode:** Dedicated administrative lock state (`activate restoration mode`).

### Recommended Security Enhancements
1. **Password Policy:** Minimum 12 characters, complexity requirements, Argon2id or bcrypt hashing.
2. **Rate Limiting:** IP and user-based throttling on authentication endpoints.
3. **Transport Security:** Strict TLS 1.3 encryption on all endpoints.
4. **Data Redaction:** PII and financial balances masked in audit log outputs.

---
""")

    # 23. Non-Functional Requirements
    sections.append("""## 23. Non-Functional Requirements

### Performance & Scalability
- **API Latency:** 95% of OLTP requests (deposits, balance checks) must respond in `< 250ms`.
- **Concurrency:** Support concurrent branch tellers and async mobile money webhooks without locking conflicts.
- **Reporting:** Heavy analytical and regulatory reports must execute against read-replicas or materialized views to prevent OLTP lockups.

### Availability & Resilience
- **Target Availability:** 99.9% uptime during operational branch hours.
- **Audit Durability:** Audit trail logs retained for minimum 7-year regulatory compliance window.

---
""")

    # 24. Cross-Page Findings
    sections.append("""## 24. Cross-Page Findings

1. **Vendor & Platform Identity:** Consistent branding across all headers: *"Savings Plus, Property of FutureLink Technologies | @2021 All rights reserved"*. FutureLink Technologies (FLT) provides the core banking platform engine.
2. **Misnamed Artifact:** `user_details.html` does not render an individual staff member's detail card; instead, it renders the exact form for editing a Role Profile (*"Edit Sp Data Migration Training"*), identical to `edit_role.html`.
3. **Shared Layout & Persistent State:** All 17 HTML files share an identical top status bar, navigation bar, idle detector widget, and failed job notification banner, indicating an Angular/Nuxt Single-Page Application shell.
4. **Entity Linkage:** Data created in `create_individual_member.html` flows directly into `list_member_applications.html` as a pending application, and subsequently appears in `list_members.html` upon approval.
5. **Currency Consistency:** The default currency across all balances and reports is Ugandan Shillings (`UGX`), with multi-currency / forex support configured in `/settings/institution/forex`.

---
""")

    # 25. Open Questions
    sections.append("""## 25. Open Questions

The following implementation specifics cannot be resolved strictly from the static HTML DOM and require stakeholder clarification:

| # | Subsystem | Open Question | Backend Impact |
|---|---|---|---|
| 1 | Regulatory Compliance | Is automated direct integration required for CRB inquiries (e.g. Metropol / Compuscan) or is it manual file upload? | External HTTP Client implementation vs static file storage |
| 2 | Biometrics / NIN | Does NIN verification connect directly to the NIRA (National Identification and Registration Authority) API or is it internal verification? | Integration adapter for NIRA gateway |
| 3 | Mobile Money Float | Are float balances polled via Telco APIs (MTN MoMo API, Airtel Money API) or updated via webhook callbacks from the MSACCO switch? | Asynchronous webhook listener vs scheduled balance polling worker |
| 4 | Accounting Engine | What is the exact Chart of Accounts structure (account code digit length, parent-child depth)? | Schema design of `chart_of_accounts` table |
| 5 | Storage Engine | Should uploaded documents (photos, signatures, IDs) be stored in local file storage or S3-compatible object storage? | Blob storage abstraction layer |
| 6 | Soft-Delete Policy | What is the permanent retention or purging lifecycle for soft-deleted users and customers? | Database cleanup / archiving jobs |

---
""")

    # 26. Assumptions
    sections.append("""## 26. Assumptions

1. **Database Engine:** PostgreSQL 15+ is assumed as the primary relational database to support JSONB attributes, transactions, and robust decimal precision.
2. **Statutory Jurisdiction:** The system operates under Ugandan legal and financial regulatory jurisdiction (BOU, UMRA, URA).
3. **Currency:** Primary accounting currency is `UGX` (Ugandan Shilling) with decimal precision set to 2 decimal places.
4. **Timezone:** Standard operating timezone is East Africa Time (EAT, UTC+3).
5. **Maker-Checker Strictness:** Maker-checker is mandatory for all member on-boarding, loan approvals, and transactions exceeding user-specific limits.

---
""")

    # 27. Traceability Matrix
    sections.append("""## 27. Traceability Matrix

| HTML Source File | Feature / Subsystem | Entities Involved | Requirement IDs | Evidence & Traceability |
|---|---|---|---|---|
| `create_member_step_one.html` | Member Type Selection | Customer | FR-001 | Cards for Individuals, Group, Joint, Institution |
| `create_individual_member.html` | Individual Onboarding | Customer, CustomerIndividual, NextOfKin, Employment, BankDetails | FR-002 | Complete 8-section form, file uploads, branch selection |
| `create_group.html` | Group Onboarding | Customer, CustomerGroup | FR-004 | Group Name, Registration number, Members/Non-members |
| `create_joint.html` | Joint Onboarding | Customer, CustomerJoint | FR-005 | Joint Name, shared addresses, loan limit toggle |
| `create_institution.html` | Institution Onboarding | Customer, CustomerInstitution | FR-006 | TIN, Business Type, Category, Ownership dropdowns |
| `list_members.html` | Member Directory | Customer, Branch | FR-001, FR-002 | Master table displaying members with phone, age, group |
| `list_groups.html` | Group Directory Tab | CustomerGroup | FR-004 | Groups filter subtab under /customers |
| `list_joints.html` | Joint Directory Tab | CustomerJoint | FR-005 | Joints filter subtab under /customers |
| `list_institutions.html` | Institution Directory Tab | CustomerInstitution | FR-006 | Institutions filter subtab under /customers |
| `list_member_applications.html` | Member Vetting Queue | Customer | FR-002, FR-003 | Pending / Rejected tabs, view application triggers |
| `list_agency_applications.html` | Agency Vetting Queue | AgencyApplication | FR-014 | Agent application pending / rejected tabs |
| `create_user.html` | User Provisioning | User, UserNotificationSubscription | FR-007 | Staff form, 9 Min/Max limit pairs, 22 alert checkboxes |
| `list_users.html` | User Administration | User | FR-008 | User cards, Deactivate, Delete, Enable SMS OTP buttons |
| `create_role.html` | Role Definition | Role, RolePermission | FR-009 | Role name, Open/Close time, 382 permission checkboxes |
| `edit_role.html` | Role Modification | Role, RolePermission | FR-009 | Permission editing and operating hour adjustments |
| `user_details.html` | Role Profile View | Role, RolePermission | FR-009 | Edit role profile UI misnamed as user_details.html |
| `list_roles.html` | Role Administration | Role | FR-009, FR-010 | Role cards, active user counts, operating hours |
| Global Top Bar / Shell | Liquidity & Alerts | ExternalFloat, Notification, Session | FR-011, FR-012, FR-013 | Float counters, Failed job alert, Idle detector |

---
""")

    # 28. Backend Implementation Considerations
    sections.append("""## 28. Backend Implementation Considerations

1. **Transactional Ledger Integrity:** Implement strict double-entry ledger primitives. Every financial event (deposit, withdrawal, disbursement, fee) must post balanced debit and credit entries to the general ledger within an isolated database transaction.
2. **Middleware Pipeline:**
   - `AuthenticationMiddleware`: Decodes JWT, validates active session.
   - `OperatingHoursMiddleware`: Evaluates current timestamp against user's role `open_time` and `close_time`.
   - `PermissionGuardMiddleware`: Checks route action against user's assigned role permission set.
   - `FinancialLimitInterceptor`: Checks transaction amount against user's min/max threshold.
3. **Asynchronous Processing:** Use a distributed task queue (e.g. Celery / BullMQ / Redis) for background jobs:
   - Interest accruals on savings and fixed deposits.
   - Batch standing order executions.
   - SMS and email notification dispatching.
   - Job health monitoring with automatic dispatch of `failed_job` alerts.

---
""")

    # 29. Final System Summary
    sections.append("""## 29. Final System Summary

The reverse-engineering of the 17 provided HTML artifacts establishes that **SavingsPlus** is an enterprise Core Banking System (CBS) designed for **NSIMBI SACCO LTD** by **FutureLink Technologies (FLT)**. 

The backend architecture requires:
- **Comprehensive Customer Information File (CIF):** Supporting Individuals, Groups, Joints, and Institutions with maker-checker onboarding.
- **Granular Security Framework:** 34 modules, 382 permissions, strict operating time windows, 5-minute idle timeouts, and individual financial limit ceilings across 9 transaction classes.
- **Integrated Operations:** End-to-end management of Savings, Loans, Shares, Accounting, Fixed Assets, Staff Payroll, External Float (MTN, Airtel, MSACCO, Bank), and Regulatory Compliance (BOU & UMRA).

This specification provides the exhaustive, unambiguous foundation for backend developers and automated coding agents to implement the database models, business logic services, and REST API controllers for the NSIMBI Core Banking System.
""")

    full_text = "
".join(sections)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write(full_text)
    
    # Also write a copy to ~/Downloads/MSACCO/REQUIREMENTS.md for user convenience
    downloads_copy = os.path.expanduser("~/Downloads/MSACCO/REQUIREMENTS.md")
    with open(downloads_copy, "w", encoding="utf-8") as f:
        f.write(full_text)

    print(f"REQUIREMENTS.md generated successfully at:
 - {OUTPUT_FILE}
 - {downloads_copy}")
    print(f"Total lines: {len(full_text.splitlines())}")

if __name__ == "__main__":
    build_requirements()

### FR-015 — Savings Account Directory & Status Filtering
- **Requirement:** The system shall list all customer savings accounts with account number, member name, branch, product, opened date, actual balance, available balance, and status filters (`All`, `Active`, `Inactive`, `Frozen`).
- **Entity:** `SavingsAccount`
- **Actor:** Cashier / Customer Care / Manager
- **Source:** `list_savings.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated staff.
- **Input:** Filter criteria (Status, Branch, Search term).
- **Business Rules:** Account number formatted as 12 digits (`003500005281`).
- **Expected Result:** Paginated list of savings accounts.
- **Failure Conditions:** Unauthorized access.
- **Evidence Level:** Explicit

### FR-016 — Savings Account Detail & Cash Desk Terminal
- **Requirement:** The system shall provide an operational cash desk screen for an individual savings account displaying Actual Balance, Available Balance, KYC photo/signature preview, and immediate transaction buttons: Deposit, Withdraw, New Transfers, Debit Account, Credit Account, Close, and Freeze.
- **Entity:** `SavingsAccount`, `SavingsTransaction`
- **Actor:** Cashier / Teller
- **Source:** `saving_account_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active savings account.
- **Input:** Account Number (`003500005281`).
- **Business Rules:** Displays Available Balance (`actual_balance - 5000 UGX reserve`).
- **Expected Result:** Detailed account ledger and action triggers.
- **Failure Conditions:** Account does not exist.
- **Evidence Level:** Explicit

### FR-017 — Savings SMS Alert Rules Configuration
- **Requirement:** The system shall allow operators to configure granular SMS transaction and loan notification subscriptions per savings account across 14 discrete triggers.
- **Entity:** `SavingsSMSAlertConfig`
- **Actor:** Customer Care / Staff
- **Source:** `edit_saving_account.html`, `saving_account_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target savings account exists.
- **Input:** Boolean flags for: On Deposit, On Withdrawal, Earning Interest, Earning Dividends, Outgoing Transfer, Incoming Transfer, Direct Debit, Direct Credit, Standing Order, Loan Application, Loan Disbursement, Loan Payment, Loan Refund.
- **Business Rules:** Saved flags dictate which account activities dispatch transactional SMS messages via the SMS gateway.
- **Expected Result:** SMS alert configuration persisted.
- **Failure Conditions:** Invalid account ID.
- **Evidence Level:** Explicit

### FR-018 — Fixed Deposit Placement & Booking
- **Requirement:** The system shall book term/fixed deposits, specifying principal amount, tenure in months, annual interest rate, interest payment interval, maturity payout option, and funding source (Cash counter vs Savings account offset).
- **Entity:** `FixedDeposit`, `SavingsAccount`
- **Actor:** Customer Care / Branch Manager
- **Source:** `create_fixed_deposit.html`
- **Priority:** MUST HAVE
- **Preconditions:** Member possesses an active savings account.
- **Input:** Member, Transfer/Payout Account, Depositor Name, Depositor Phone, Principal Amount, Starts On, Period in Months, Interest Rate (%), Interest Interval (`monthly`, `yearly`, `Not applicable`), Payout Option (`Principal`, `Principal & Interest`), Funding Method (`Till - Cash or cheque`, `Offset - From savings`).
- **Business Rules:** If offset selected, funds immediately placed on hold/debited from source savings account. Enters Maker-Checker approval queue (`list_fixed_deposit_applications.html`).
- **Expected Result:** Fixed deposit contract created in `PENDING` status.
- **Failure Conditions:** Insufficient available balance in source savings account.
- **Evidence Level:** Explicit

### FR-019 — Fixed Deposit Lifecycle & Termination
- **Requirement:** The system shall track fixed deposits through their complete lifecycle and support both premature and maturity termination with interest calculation.
- **Entity:** `FixedDeposit`
- **Actor:** Branch Manager
- **Source:** `list_fixed_deposits.html`, `fixed_deposit_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Fixed deposit in `RUNNING` or `MATURED_NOT_AWARDED` status.
- **Input:** Deposit ID, Action (`TERMINATE`), Interest Decision (`WITH_INTEREST`, `NO_INTEREST`).
- **Business Rules:** Terminating returns principal (and approved interest) to the designated transfer savings account. Status transitions to `AWARDED_ON_MATURITY`, `TERMINATED_NO_INTEREST`, or `TERMINATED_WITH_INTEREST`.
- **Expected Result:** Funds transferred; contract archived.
- **Failure Conditions:** Contract already liquidated.
- **Evidence Level:** Explicit

### FR-020 — Standing Orders Recurring Execution
- **Requirement:** The system shall maintain standing orders with automated periodic execution and track statuses: `Pending`, `Approved`, `Running`, `Rejected`, `Expired`.
- **Entity:** `StandingOrder`
- **Actor:** Customer Care / Scheduler
- **Source:** `list_standing_orders.html`
- **Priority:** MUST HAVE
- **Preconditions:** Source savings account active.
- **Input:** Source Account, Destination Account, Amount, Frequency, Start Date.
- **Business Rules:** Background worker sweeps source account on execution date. If balance insufficient, records failed execution and dispatches alert `standing_orders_pending_approval`.
- **Expected Result:** Transfer posted between accounts.
- **Failure Conditions:** Expired order or insufficient balance.
- **Evidence Level:** Explicit

### FR-021 — Debit Card Issuance Tracking
- **Requirement:** The system shall manage the requisition, approval, and issuance of co-branded Interswitch debit cards linked to member savings accounts.
- **Entity:** `DebitCardIssuance`
- **Actor:** Customer Care / Branch Manager
- **Source:** `list_debit_card_issuance.html`, `saving_account_detail.html`
- **Priority:** SHOULD HAVE
- **Preconditions:** Active savings account.
- **Input:** Savings Account Number, Card PAN / Serial Number.
- **Business Rules:** Card status tracks through `Pending` -> `Approved` -> `Issued` -> `Rejected`.
- **Expected Result:** Card linked for ATM/POS debit transactions.
- **Failure Conditions:** Account frozen or inactive.
- **Evidence Level:** Explicit

### FR-022 — Loan Amortization Calculator
- **Requirement:** The system shall provide an interactive loan amortization calculator supporting multiple interest calculation methods (`flat`, `declining`, `amortize`) and frequencies (`daily`, `weekly`, `monthly`, `annually`), generating installment amounts and repayment schedules.
- **Entity:** `LoanProduct`, `LoanRepaymentSchedule`
- **Actor:** Loan Officer / Customer
- **Source:** `loan_calculator.html`
- **Priority:** MUST HAVE
- **Preconditions:** Loan products configured in system.
- **Input:** Product ID, Principal Amount, Interest Method, Annual Interest Rate, Frequency, Number of Installments, Start Date, Grace Period (Months).
- **Business Rules:** Generates accurate mathematical repayment schedule preview.
- **Expected Result:** Installment amount and period-by-period breakdown of Principal, Interest, and Remaining Balance.
- **Failure Conditions:** Negative principal or rate.
- **Evidence Level:** Explicit

### FR-023 — Credit Reference Bureau (CRB / CBA) Vetting
- **Requirement:** The system shall integrate credit reporting workflows allowing officers to record borrower consent and perform credit vetting against Credit Bureau Africa (CBA).
- **Entity:** `CreditReportingInquiry`
- **Actor:** Loan Officer
- **Source:** `list_credit_reporting.html`
- **Priority:** MUST HAVE
- **Preconditions:** Borrower signed CRB consent form (`initiate crb report consent`).
- **Input:** Member NIN / Account Number, Bureau Identifier (`CBA`).
- **Business Rules:** Credit check results logged in bureau register (`view crb register`) prior to loan approval.
- **Expected Result:** Credit history report and risk rating returned.
- **Failure Conditions:** Bureau service unavailable.
- **Evidence Level:** Explicit

### FR-024 — Loan Servicing, Auto-Pay & Repayments
- **Requirement:** The system shall manage active loan contracts, displaying disbursement date, maturity date, interest rate (e.g. `42.0%`), loan officer, and linked repayment account, supporting manual payment posting and automated savings sweeps.
- **Entity:** `Loan`, `SavingsAccount`, `LoanRepayment`
- **Actor:** Loan Officer / Cashier / System Scheduler
- **Source:** `loan_detail.html`, `list_loans.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active loan contract in `RUNNING` status.
- **Input:** Loan Number (`1D04FE1229`), Payment Amount, Payment Mode.
- **Business Rules:** If `auto_pay_from_savings=true`, system automatically debits linked savings account (`003500000651`) on due date.
- **Expected Result:** Installment credited against outstanding interest and principal.
- **Failure Conditions:** Delinquent repayment incurs late penalties.
- **Evidence Level:** Explicit

### FR-025 — Loan Restructuring, Waivers & Legal Recovery
- **Requirement:** The system shall support credit risk interventions including interest waivers, penalty waivers, admin fee waivers, grace period additions, officer re-assignments, loan restructuring/rescheduling (generating new loan numbers referencing original facility), legal referrals (`Send to legal`), and bad debt write-offs (`Write off`).
- **Entity:** `Loan`
- **Actor:** Credit Manager
- **Source:** `loan_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Delinquent or performing loan requiring administrative modification.
- **Input:** Loan Number, Action (`WAIVE_INTEREST`, `WAIVE_PENALTY`, `WAIVE_ADMIN_FEES`, `RESCHEDULE`, `ADD_GRACE_PERIOD`, `TRANSFER_OFFICER`, `SEND_TO_LEGAL`, `WRITE_OFF`), Justification Notes, Adjusted Terms.
- **Business Rules:** Waivers post corresponding reversing entries. Rescheduling closes old loan and books new loan contract. Write-offs post against Loan Loss Provision accounts.
- **Expected Result:** Loan contract state and ledger updated.
- **Failure Conditions:** Insufficient authorization limits.
- **Evidence Level:** Explicit

---
""")

    # 15. API Requirements
    s.append("""## 15. API Requirements

### Extended API Catalog (Incorporating Savings & Loans)
- `GET /savings` — Query savings accounts with status filters (`active`, `inactive`, `frozen`).
- `GET /savings/{number}` — Fetch detailed account overview, available vs actual balance, and recent ledger entries.
- `POST /savings/{number}/deposit` — Post cash or counter deposit to savings account.
- `POST /savings/{number}/create-individual-withdrawal` — Post cash withdrawal from savings account.
- `POST /savings/{number}/direct-transfers` — Execute inter-account transfer.
- `POST /savings/{number}/freeze` / `POST /savings/{number}/unfreeze` — Toggle administrative hold.
- `POST /savings/{number}/close` — Liquidate balance and close account.
- `PATCH /savings/{number}/alerts` — Update 14 SMS alert rule checkboxes.
- `GET /savings/applications` — Fetch pending savings account opening applications.
- `POST /savings/applications/{id}/approve` — Approve savings account opening.
- `GET /savings/fixed-deposits` — List term deposits with 7 lifecycle status filters.
- `POST /savings/fixed-deposits` — Book new fixed deposit (Till or Savings Offset).
- `GET /savings/fixed-deposits/{id}` — Fetch fixed deposit terms and certificate data.
- `POST /savings/fixed-deposits/{id}/terminate` — Liquidate fixed deposit before or at maturity.
- `GET /savings/standing-orders` — List recurring standing orders.
- `POST /savings/{number}/standing-orders/create` — Setup new recurring standing order.
- `GET /savings/debit-cards` — List debit card issuance requests and statuses.
- `POST /savings/{number}/debit-cards` — Request Interswitch debit card issuance.
- `GET /loans` — List active loan facilities with portfolio filters (`outstanding`, `due`, `performing`, `sent_to_legal`).
- `GET /loans/{number}` — Fetch full loan details, linked repayment account, and amortization schedule.
- `POST /loans/calculator` — Calculate amortization schedule based on method (`flat`, `declining`, `amortize`).
- `GET /loans/applications` — Fetch loan origination pipeline (`approved`, `pending`, `rejected`, `cancelled`).
- `POST /loans/applications` — Submit new loan origination application.
- `POST /loans/applications/{id}/approve` — Sanction loan application.
- `POST /loans/applications/{id}/disburse` — Disburse approved funds into member savings account.
- `POST /loans/{number}/repayments` — Post manual loan repayment.
- `POST /loans/{number}/waiver-interest` — Waive accrued interest.
- `POST /loans/{number}/waiver-penalty` — Waive late payment penalties.
- `POST /loans/{number}/reschedule` — Restructure loan and issue new facility reference.
- `POST /loans/{number}/send-to-legal` — Forward defaulted loan to legal debt collection.
- `POST /loans/{number}/write-off` — Write off bad debt against loan loss provisions.
- `GET /loans/credit-reporting` — Query credit bureau history (CBA).

---
""")

    # 16. Data / Database Requirements
    s.append("""## 16. Data / Database Requirements

### Schema Additions for Savings and Loans
```sql
-- Savings Products Table
CREATE TABLE savings_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'FlexSave'
    code VARCHAR(20) UNIQUE NOT NULL,
    minimum_balance DECIMAL(18,2) NOT NULL DEFAULT 5000.00,
    opening_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_rate_annual DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Savings Accounts Table
CREATE TABLE savings_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(30) UNIQUE NOT NULL, -- e.g. '003500005281'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    product_id INTEGER NOT NULL REFERENCES savings_products(id),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED')),
    actual_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    opening_fee_charged BOOLEAN NOT NULL DEFAULT FALSE,
    last_ledger_fee_charged_at TIMESTAMP WITH TIME ZONE,
    last_transaction_at TIMESTAMP WITH TIME ZONE,
    opened_at DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Savings SMS Alert Rules
CREATE TABLE savings_sms_alert_configs (
    account_id BIGINT PRIMARY KEY REFERENCES savings_accounts(id) ON DELETE CASCADE,
    notify_on_deposit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_withdrawal BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_earning_interest BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_earning_dividends BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_outgoing_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_incoming_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_debit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_credit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_standing_order BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_application BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_loan_disbursement BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_payment BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_refund BOOLEAN NOT NULL DEFAULT FALSE
);

-- Fixed Deposits Table
CREATE TABLE fixed_deposits (
    id BIGSERIAL PRIMARY KEY,
    deposit_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    funding_source VARCHAR(30) NOT NULL CHECK (funding_source IN ('TILL_CASH', 'SAVINGS_OFFSET')),
    funding_savings_account_id BIGINT REFERENCES savings_accounts(id),
    payout_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    depositor_name VARCHAR(150) NOT NULL,
    depositor_phone VARCHAR(30) NOT NULL,
    principal_amount DECIMAL(18,2) NOT NULL,
    starts_on DATE NOT NULL,
    period_months INTEGER NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_interval VARCHAR(30) NOT NULL CHECK (interest_interval IN ('monthly', 'yearly', 'Not applicable')),
    payout_option VARCHAR(50) NOT NULL CHECK (payout_option IN ('Principal', 'Principal & Interest')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RUNNING', 'AWARDED', 'TERMINATED', 'MATURED_NOT_AWARDED', 'TERMINATED_NO_INTEREST', 'AWARDED_ON_MATURITY', 'TERMINATED_WITH_INTEREST')),
    matures_at DATE NOT NULL,
    accrued_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Standing Orders Table
CREATE TABLE standing_orders (
    id BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    destination_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    amount DECIMAL(18,2) NOT NULL,
    frequency VARCHAR(30) NOT NULL CHECK (frequency IN ('daily', 'weekly', 'monthly')),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'RUNNING', 'REJECTED', 'EXPIRED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Products Table
CREATE TABLE loan_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'NCHIEI Credit', 'Business Loans'
    code VARCHAR(20) UNIQUE NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    default_annual_interest_rate DECIMAL(5,2) NOT NULL,
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    min_principal DECIMAL(18,2) NOT NULL,
    max_principal DECIMAL(18,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Loans Table
CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    loan_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '1D04FE1229'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    loan_officer_id BIGINT NOT NULL REFERENCES users(id),
    product_id INTEGER NOT NULL REFERENCES loan_products(id),
    repayment_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    principal_disbursed DECIMAL(18,2) NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    installments_count INTEGER NOT NULL,
    grace_period_months INTEGER NOT NULL DEFAULT 0,
    installment_amount DECIMAL(18,2) NOT NULL,
    auto_pay_from_savings BOOLEAN NOT NULL DEFAULT TRUE,
    disbursed_at DATE NOT NULL,
    maturity_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING', 'PERFORMING', 'DUE', 'OUTSTANDING', 'SENT_TO_LEGAL', 'WRITTEN_OFF', 'RESCHEDULED', 'CLEARED')),
    rescheduled_from_loan_number VARCHAR(50),
    total_principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_penalty_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_penalty DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_admin_fees DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Repayment Schedules Table
CREATE TABLE loan_repayment_schedules (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(18,2) NOT NULL,
    interest_due DECIMAL(18,2) NOT NULL,
    total_installment DECIMAL(18,2) NOT NULL,
    principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    is_settled BOOLEAN NOT NULL DEFAULT FALSE
);
```

---
""")

    # 17 to 26 - Retain & Expand
    s.append("""## 17. Search, Filtering and Reporting

### Savings & Loan Specific Reporting Capabilities
From the 109 cataloged reports, the following are directly confirmed by the savings and loan captures:
- `view savings accounts`, `view savings ledger`, `view savings transactions`, `view failed standing orders`.
- `view fixed deposits`, `view fixed certificate`.
- `view loan applications`, `view loan arrears`, `view loan bookings`, `view loan schedule`, `view loan ledger`, `view loan repayments`, `view loan repayment summary`, `view cleared loans`, `view due loans`, `view written off loans`, `view rescheduled loans`.
- `view par by ageing`, `view par by ageing summary`, `view umra par by ageing` (Portfolio At Risk aging schedules for regulatory reporting).
- `view crb register`, `download crb files` (Credit Reference Bureau returns).

---

### FR-026 — Chart of Accounts Hierarchy Management
- **Requirement:** The system shall maintain a 5-tier hierarchical Chart of Accounts (Assets, Liabilities, Capital, Income, Expenses) with dash-separated account codes, supporting node creation, updates, and deactivation.
- **Entity:** `ChartOfAccount`
- **Actor:** Head of Finance / Accountant
- **Source:** `list_charts_of_accounts_assets.html`, `create_chart_of_account_asset.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated user with `register chart account` permission.
- **Input:** Account Name*, Parent Account Node, Subtype (Optional), Tags (Optional).
- **Business Rules:** System generates dash-separated hierarchical code; cannot deactivate an account with a non-zero balance.
- **Expected Result:** New GL node created in Chart of Accounts.
- **Failure Conditions:** Duplicate account code.
- **Evidence Level:** Explicit

### FR-027 — Double-Entry Journal Voucher Creation & Reversal
- **Requirement:** The system shall capture balanced double-entry Journal Vouchers across branches and ledger accounts, supporting maker-checker reversal workflows.
- **Entity:** `JournalVoucher`, `JournalVoucherItem`
- **Actor:** Accountant / Accounts Assistant
- **Source:** `create_journal.html`, `journals.html.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid active accounts in Chart of Accounts.
- **Input:** Contact Person*, Registration Date*, Reference Number*, Method (`cash`, `cheque`)*, Comment*, Debit Account*, Credit Account*, Amount*.
- **Business Rules:** Total debits must equal total credits; reversals must reference original voucher and provide justification.
- **Expected Result:** Journal voucher posted to General Ledger or queued for approval.
- **Failure Conditions:** Debits != Credits (`ERR_TB_OUT_OF_BALANCE`).
- **Evidence Level:** Explicit

### FR-028 — Financial Period Closing, TB Archival & Rollback Protection
- **Requirement:** The system shall close financial periods, generate an immutable Trial Balance snapshot PDF, lock the period against backdated transactions, and allow authorized rollback.
- **Entity:** `FinancialPeriod`
- **Actor:** Head of Finance / Administrator
- **Source:** `closing_and_restore_books.html`
- **Priority:** MUST HAVE
- **Preconditions:** All subledgers reconciled; Trial Balance in balance.
- **Input:** Period End Date, Action (`CLOSE`, `ROLLBACK`).
- **Business Rules:** Closing generates `Download TB(pdf)` snapshot; rollback triggers alert `opening_a_closed_period`.
- **Expected Result:** Period locked or reopened.
- **Failure Conditions:** Out of balance Trial Balance (`tb_imbalance`).
- **Evidence Level:** Explicit

### FR-029 — Financial Approvals Maker-Checker Queue
- **Requirement:** The system shall provide an approval queue for accounting transactions, journal vouchers, and bank transfers with pending, rejected, and approved queues.
- **Entity:** `FinancialApproval`
- **Actor:** Branch Manager / Head of Finance
- **Source:** `approvals.html`
- **Priority:** MUST HAVE
- **Preconditions:** Transaction submitted by maker.
- **Input:** Approval ID, Decision (`APPROVE`, `REJECT`), Reason.
- **Business Rules:** Checker cannot be maker (BR-001).
- **Expected Result:** Transaction committed to ledger or rejected.
- **Failure Conditions:** Checker is maker.
- **Evidence Level:** Explicit

### FR-030 — Fixed Asset Register & Depreciation
- **Requirement:** The system shall maintain a register of physical assets and asset profiles, supporting periodic depreciation calculation, revaluations, and disposals.
- **Entity:** `FixedAsset`, `AssetProfile`
- **Actor:** Accountant
- **Source:** `list_fixed_assets.html`, `list_asset_profiles.html`
- **Priority:** SHOULD HAVE
- **Preconditions:** Asset profile defined.
- **Input:** Asset Name, Profile, Purchase Date, Cost, Salvage Value, Useful Life.
- **Business Rules:** Depreciation posts monthly debit to Depreciation Expense and credit to Accumulated Depreciation.
- **Expected Result:** Asset tracked on balance sheet.
- **Failure Conditions:** Invalid profile.
- **Evidence Level:** Explicit

### FR-031 — Member Share Capital Administration
- **Requirement:** The system shall manage member share equity accounts, recording share capital subscriptions, purchases, transfers, and liquidations.
- **Entity:** `ShareAccount`, `ShareTransaction`
- **Actor:** Customer Care / Accountant
- **Source:** `list_shares.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active member.
- **Input:** Member ID, Number of Shares, Unit Price.
- **Business Rules:** Total share capital reflects under Capital tier of Chart of Accounts.
- **Expected Result:** Member share balance updated.
- **Failure Conditions:** Member inactive.
- **Evidence Level:** Explicit

### FR-032 — Password-Authenticated Share Dividend Payout
- **Requirement:** The system shall declare and distribute share dividends based on shareholding periods, requiring staff password re-authentication, supporting distribution directly into savings accounts or equity capitalization.
- **Entity:** `DividendDeclaration`
- **Actor:** Head of Finance / General Manager
- **Source:** `share_dividends.html`
- **Priority:** MUST HAVE
- **Preconditions:** Approved dividend pool; member shareholdings verified.
- **Input:** Amount*, Use Percentages*, Date*, Share Period (Months)*, Method (`savings`, `shares`)*, Saving Product, Reference Number*, Comment*, Password*.
- **Business Rules:** Password re-verified; credits destination savings product (`FlexSave`) or increases member shares held.
- **Expected Result:** Dividend disbursed across all qualifying members; event logged.
- **Failure Conditions:** Incorrect password (`ERR_INVALID_PASSWORD`).
- **Evidence Level:** Explicit

### FR-033 — Multi-Entity Batch Data Import Engine
- **Requirement:** The system shall provide batch data ingestion with downloadable templates and preview validation across 9 entity types: Individuals, Institutions, Joint Accounts, Groups, Group Members, Loans, Fixed Deposits, Savings, and savingsAccounts.
- **Entity:** `DataImportBatch`
- **Actor:** System Administrator / Migration Specialist
- **Source:** `imports.html`, `preview_import.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid CSV template.
- **Input:** Template Type*, Branch*, Reference Number*, Document File*.
- **Business Rules:** Preview validates column mappings and schema errors prior to commit.
- **Expected Result:** Bulk records ingested into respective database tables.
- **Failure Conditions:** File parsing failure or validation mismatch.
- **Evidence Level:** Explicit

---
""")

    # 15. API Requirements
    sections.append("""## 15. API Requirements

Based on explicit form submissions, list page interactions, and the 190 discovered routes in the client bundle, the following RESTful API surface is required:

### Authentication & Session Subsystem
- `POST /api/auth/login`
  - **Purpose:** Authenticate staff credentials.
  - **Payload:** `{ username: "...", password: "..." }`
  - **Response:** `{ token: "...", refresh_token: "...", expires_in: 300, user: { ... } }`
  - **Validation & Rules:** Rate-limited; verifies `is_active=true`, `is_blocked=false`, and temporal window `open_time <= now <= close_time`.
- `POST /api/auth/logout`
  - **Purpose:** Invalidate current session token.
- `GET /api/auth/user`
  - **Purpose:** Return current authenticated user profile, permissions, and assigned limits.
- `POST /api/auth/refresh`
  - **Purpose:** Issue fresh JWT against valid refresh token.

### Customers & Membership Subsystem
- `GET /customers`
  - **Purpose:** Query paginated customer directory with filtering.
  - **Query Params:** `type` (individual, group, joint, institution), `branch_id`, `search`, `page`, `per_page`.
- `POST /customers/individuals`
  - **Purpose:** Submit new individual member application.
  - **Payload:** Multipart/form-data containing all Bio Data, Contact, Kin, Employment, Bank details, and uploaded binary files (signature, ID, photo).
  - **Response:** 201 Created `{ id: 1223, member_number: "00350000519", status: "PENDING" }`
- `POST /customers/groups`
  - **Purpose:** Register group member.
  - **Payload:** `{ branch_id, group_name, group_type, registration_number, entry_date, savings_officer_id, phone, address, ... }`
- `POST /customers/joints`
  - **Purpose:** Register joint account entity.
- `POST /customers/institutions`
  - **Purpose:** Register corporate/institutional entity.
  - **Payload:** `{ institution_name, registration_number, registration_date, tin_number, business_type, institution_category, ... }`
- `GET /customers/applications`
  - **Purpose:** Fetch maker-checker queue for member applications.
  - **Query Params:** `status=pending` or `status=rejected`, `branch_id`, `page`.
- `POST /customers/applications/{id}/approve`
  - **Purpose:** Approve pending member application and activate ledgers.
- `POST /customers/applications/{id}/reject`
  - **Purpose:** Reject pending member application with reason notes.
- `GET /customers/agent-applications`
  - **Purpose:** Fetch agency banking applications queue.

### Users & Security Subsystem
- `GET /users`
  - **Purpose:** Fetch list of system users.
  - **Query Params:** `status` (all, active, inactive), `search`, `page`.
- `POST /users`
  - **Purpose:** Provision new user account with transaction limits and notification subscriptions.
  - **Payload:** `{ first_name, last_name, username, email, password, phone, role_id, branch_id, station_id, limits: { ... }, notification_subscriptions: [ ... ] }`
- `PATCH /users/{id}`
  - **Purpose:** Update user details, limits, or assign new role.
- `POST /users/{id}/deactivate`
  - **Purpose:** Deactivate active user.
- `DELETE /users/{id}`
  - **Purpose:** Soft-delete user account.
- `POST /users/{id}/toggle-sms-otp`
  - **Purpose:** Enable or disable 2FA requirement.

### Roles & RBAC Subsystem
- `GET /users/roles`
  - **Purpose:** List all role profiles with assigned user counts and operating hours.
  - **Query Params:** `status` (all, active, inactive).
- `POST /users/roles`
  - **Purpose:** Create new role profile.
  - **Payload:** `{ role_name: "...", open_time: "00:00:00", close_time: "23:00:00", permissions: [ "register deposit", ... ] }`
- `GET /users/roles/{id}`
  - **Purpose:** Retrieve role definition and complete array of assigned permissions.
- `PATCH /users/roles/{id}`
  - **Purpose:** Update role permissions or operating window.
- `POST /users/roles/{id}/activate` / `POST /users/roles/{id}/deactivate`
  - **Purpose:** Toggle operational availability of role.
- `DELETE /users/roles/{id}`
  - **Purpose:** Delete unassigned role.

### Notifications & Float Subsystem
- `GET /notifications`
  - **Purpose:** Fetch current user's unread notifications.
- `POST /notifications/{id}/mark-read`
  - **Purpose:** Mark notification as read.
- `POST /notifications/mark-all-read`
  - **Purpose:** Mark all unread notifications as read.
- `GET /settings/float-balances`
  - **Purpose:** Fetch latest float balances for top-bar indicators (MSACCO, MTN, Airtel, Bank Transfer).

---
""")

    # 16. Data / Database Requirements
    sections.append("""## 16. Data / Database Requirements

### Conceptual Data Model
The database architecture must be a relational database (PostgreSQL recommended) enforcing ACID compliance and double-entry ledger immutability.

```sql
-- Core Branches Table
CREATE TABLE branches (
    id SERIAL PRIMARY KEY,
    branch_code VARCHAR(10) UNIQUE NOT NULL, -- e.g. '0035'
    name VARCHAR(100) NOT NULL, -- e.g. 'KABALE'
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Core Roles Table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    open_time TIME NOT NULL DEFAULT '00:00:00',
    close_time TIME NOT NULL DEFAULT '23:59:59',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Role Permissions Table
CREATE TABLE role_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_key VARCHAR(100) NOT NULL,
    UNIQUE(role_id, permission_key)
);

-- Core Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    role_id INTEGER NOT NULL REFERENCES roles(id),
    station_id VARCHAR(50), -- e.g. 'Teller - 1'
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    sms_otp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    
    -- Maker-Checker Financial Limits
    loan_approval_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    loan_approval_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- User Notification Subscriptions Table
CREATE TABLE user_notification_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_key VARCHAR(100) NOT NULL,
    UNIQUE(user_id, event_key)
);

-- Master Customers Table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    member_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '00350000001'
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('INDIVIDUAL', 'GROUP', 'JOINT', 'INSTITUTION')),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    savings_officer_id BIGINT REFERENCES users(id),
    entry_date DATE NOT NULL,
    phone_primary VARCHAR(30) NOT NULL,
    phone_secondary VARCHAR(30),
    email VARCHAR(150),
    country VARCHAR(100) NOT NULL DEFAULT 'Uganda',
    town VARCHAR(100),
    physical_address TEXT NOT NULL,
    postal_code VARCHAR(30),
    postal_address VARCHAR(150),
    has_loan_limit BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'REJECTED', 'INACTIVE', 'DORMANT')),
    created_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Individual Profile Table
CREATE TABLE customer_individuals (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    salutation VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female')),
    marital_status VARCHAR(20) NOT NULL,
    is_dependent BOOLEAN NOT NULL DEFAULT FALSE,
    is_pwd BOOLEAN NOT NULL DEFAULT FALSE,
    date_of_birth DATE NOT NULL,
    country_of_birth VARCHAR(100) NOT NULL,
    nationality VARCHAR(100) NOT NULL DEFAULT 'Ugandan',
    home_ownership VARCHAR(20) NOT NULL CHECK (home_ownership IN ('Owner', 'Tenant', 'Other')),
    identification_type VARCHAR(50) NOT NULL,
    identification_number VARCHAR(100) NOT NULL,
    card_number VARCHAR(100),
    signature_file_path TEXT,
    identification_file_path TEXT,
    photo_file_path TEXT
);

-- Next of Kin Table
CREATE TABLE customer_next_of_kin (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    second_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    physical_address TEXT NOT NULL,
    relationship VARCHAR(30) NOT NULL
);

-- Group Profile Table
CREATE TABLE customer_groups (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    group_name VARCHAR(150) NOT NULL,
    group_type VARCHAR(50) NOT NULL CHECK (group_type IN ('Members', 'Non Members')),
    registration_number VARCHAR(100) NOT NULL
);

-- Joint Profile Table
CREATE TABLE customer_joints (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    joint_name VARCHAR(150) NOT NULL
);

-- Institution Profile Table
CREATE TABLE customer_institutions (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    institution_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100) NOT NULL,
    registration_date DATE NOT NULL,
    tin_number VARCHAR(50) NOT NULL,
    business_type VARCHAR(100) NOT NULL,
    institution_category VARCHAR(100) NOT NULL,
    residence_ownership VARCHAR(50) NOT NULL
);
```

---
""")

    # 17. Search, Filtering and Reporting
    sections.append("""## 17. Search, Filtering and Reporting

### Search Capabilities
- Master member search indexing across: Member Number, Name, Phone Number, and Group Affiliation.
- User search indexing: Username, First Name, Last Name, and Branch.

### Filtering Dimensions
- Status Filters: `All`, `Active`, `Inactive`, `Pending`, `Rejected`.
- Organizational Filters: Branch (`0035 KABALE`), Officer assignment, Customer Subtype.
- Date Range Filters: Registration date, approval date, transaction value date.

### Regulatory & Management Reports Catalog
The system specifies 109 discrete reporting capabilities including:
1. **Central Bank / BOU Returns:** Bank of Uganda Liquidity Return (`bou liquidity`), Bank of Uganda Banking Supervision Return (`bou bsa`).
2. **UMRA Returns:** Uganda Microfinance Regulatory Authority Balance Sheet (`umra balance sheet`), Income Statement (`umra income statement`), and PAR by Aging (`umra par by ageing`).
3. **Credit Operations Reports:** Loan Arrears vs Savings, Portfolio Monitoring, Cleared Loans, Due Loans, Written-off Loans, CRB Registers.
4. **General Ledger & Accounting:** Balance Sheet, Profit & Loss / Income Statement, Cash Flow Statement, Trial Balance, Backdated Transactions, Till Sheet Reconciliation.
5. **Shares & Equity:** Cumulative Shares, Shares Ledger, Dividends Shared Report.

---
""")

    # 18. File and Document Requirements
    sections.append("""## 18. File and Document Requirements

### Upload Specifications
1. **Signature File:** Uploaded during individual registration (`Signature*`). Stored securely; rendered on teller screens for withdrawal verification.
2. **Identification Document Scan:** Uploaded ID card/passport scan (`Indentification*`).
3. **Profile Photo:** Member portrait photograph (`Photo*`).
4. **Bulk Processing Files:** Bulk payroll/disbursement files (`.csv`, `.xlsx`) uploaded for batch processing.
5. **Database Backups:** Compressed database backup files downloadable by administrators (`download backup`, `restore backup`).

---
""")

    # 19. Notification and Event Requirements
    sections.append("""## 19. Notification and Event Requirements

### The 22 Core Notification Triggers
The backend event dispatcher must route notifications based on user subscriptions:
1. `blocked_user`: Dispatched when an account is locked out after consecutive failed authentications.
2. `bulk_files_pending_approval`: Dispatched when an institutional disbursement file is uploaded.
3. `customer_applications_pending_approval`: Dispatched when a member registration form is submitted.
4. `customer_kyc_updated`: Dispatched when member biodata or identity documents are altered.
5. `failed_job`: Dispatched immediately when an asynchronous background job errors out.
6. `fixed_deposit_applications_pending_approval`: Dispatched on term deposit placement.
7. `license_expiry_reminder`: Dispatched when software license term nears expiration.
8. `license_upgraded`: Dispatched when license tier or capacity is expanded.
9. `liquid_assets_balance_running_low`: Dispatched when branch cash or float balances breach minimum reserve.
10. `loan_applications_pending_approval`: Dispatched when a loan officer submits an application.
11. `loan_applications_pending_disbursement`: Dispatched when an approved loan is ready for payout.
12. `msacco_customer_onboarding`: Dispatched when a member self-registers via mobile USSD/App.
13. `msacco_loan_application`: Dispatched on mobile emergency loan request.
14. `opening_a_closed_period`: High-priority alert triggered if an accounting period is unsealed.
15. `reserve_amount_limit`: Dispatched when statutory reserves breach threshold.
16. `savings_account_applications_pending_approval`: Dispatched on savings account opening.
17. `standing_orders_pending_approval`: Dispatched when a recurring payment rule is established.
18. `tb_imbalance`: Critical financial alarm fired when Trial Balance debits != credits.
19. `transaction_mismatch`: Dispatched on settlement variance between CBS and mobile money/bank float.
20. `transactions_pending_approval`: Dispatched when a transaction breaches a teller's posting limit.
21. `upcoming_holidays`: Dispatched in advance of statutory holidays affecting loan schedules.
22. `updated_user_profile`: Dispatched when administrative permissions or limits change.

---
""")

    # 20. Audit and History Requirements
    sections.append("""## 20. Audit and History Requirements

### Immutability & Audit Trails
- **Audit Fields:** Every database entity must include `created_at`, `updated_at`, `created_by`, and `approved_by`.
- **System Audit Trail (`view audit trail`):** Comprehensive record capturing timestamp, actor user ID, client IP address, action performed, entity affected, old state, and new state.
- **Financial Immutability:** No financial transaction may be hard-deleted or updated in-place. Corrections must occur strictly via reversing entries (`reverse journal`, `reverse withdrawal`, `reverse deposit`, `reverse share purchase`).
- **Backdated Transaction Audit (`view backdated transactions`):** Specialized tracking for any entry posted with a historical value date.

---
""")

    # 21. Error Handling Requirements
    sections.append("""## 21. Error Handling Requirements

### Standard Error Taxonomy
- `400 Bad Request`: Input validation failure (`ERR_VALIDATION_FAILED`), duplicate unique field (`ERR_DUPLICATE_NIN`, `ERR_DUPLICATE_USERNAME`).
- `401 Unauthorized`: Missing, expired, or invalid bearer token (`ERR_SESSION_EXPIRED`, `ERR_UNAUTHENTICATED`).
- `403 Forbidden`:
  - Temporal restriction: `ERR_OUTSIDE_OPERATING_HOURS`.
  - Permission restriction: `ERR_PERMISSION_DENIED`.
  - Transaction limit restriction: `ERR_EXCEEDS_USER_LIMIT`.
  - Maker-checker violation: `ERR_CANNOT_APPROVE_OWN_RECORD`.
- `404 Not Found`: Target entity not found (`ERR_MEMBER_NOT_FOUND`).
- `409 Conflict`: Invalid state transition (`ERR_INVALID_STATUS_TRANSITION`), period already closed (`ERR_PERIOD_CLOSED`), Trial Balance imbalance (`ERR_TB_IMBALANCE`).
- `500 Internal Server Error`: Asynchronous job failure (`ERR_JOB_FAILED`), unexpected exception.

---
""")

    # 22. Security Requirements
    sections.append("""## 22. Security Requirements

### Observed Security Requirements
1. **Operating Hours Access Control:** Strictly enforced login and execution window per role.
2. **Idle Session Invalidation:** 5-minute timeout.
3. **Transaction Boundary Protection:** User min/max amount caps.
4. **Segregation of Duties (SoD):** Maker-checker separation for applications and transactions.
5. **Database Restoration Mode:** Dedicated administrative lock state (`activate restoration mode`).

### Recommended Security Enhancements
1. **Password Policy:** Minimum 12 characters, complexity requirements, Argon2id or bcrypt hashing.
2. **Rate Limiting:** IP and user-based throttling on authentication endpoints.
3. **Transport Security:** Strict TLS 1.3 encryption on all endpoints.
4. **Data Redaction:** PII and financial balances masked in audit log outputs.

---
""")

    # 23. Non-Functional Requirements
    sections.append("""## 23. Non-Functional Requirements

### Performance & Scalability
- **API Latency:** 95% of OLTP requests (deposits, balance checks) must respond in `< 250ms`.
- **Concurrency:** Support concurrent branch tellers and async mobile money webhooks without locking conflicts.
- **Reporting:** Heavy analytical and regulatory reports must execute against read-replicas or materialized views to prevent OLTP lockups.

### Availability & Resilience
- **Target Availability:** 99.9% uptime during operational branch hours.
- **Audit Durability:** Audit trail logs retained for minimum 7-year regulatory compliance window.

---
""")

    # 24. Cross-Page Findings
    sections.append("""## 24. Cross-Page Findings

1. **Vendor & Platform Identity:** Consistent branding across all headers: *"Savings Plus, Property of FutureLink Technologies | @2021 All rights reserved"*. FutureLink Technologies (FLT) provides the core banking platform engine.
2. **Misnamed Artifact:** `user_details.html` does not render an individual staff member's detail card; instead, it renders the exact form for editing a Role Profile (*"Edit Sp Data Migration Training"*), identical to `edit_role.html`.
3. **Shared Layout & Persistent State:** All 17 HTML files share an identical top status bar, navigation bar, idle detector widget, and failed job notification banner, indicating an Angular/Nuxt Single-Page Application shell.
4. **Entity Linkage:** Data created in `create_individual_member.html` flows directly into `list_member_applications.html` as a pending application, and subsequently appears in `list_members.html` upon approval.
5. **Currency Consistency:** The default currency across all balances and reports is Ugandan Shillings (`UGX`), with multi-currency / forex support configured in `/settings/institution/forex`.

---
""")

    # 25. Open Questions
    sections.append("""## 25. Open Questions

The following implementation specifics cannot be resolved strictly from the static HTML DOM and require stakeholder clarification:

| # | Subsystem | Open Question | Backend Impact |
|---|---|---|---|
| 1 | Regulatory Compliance | Is automated direct integration required for CRB inquiries (e.g. Metropol / Compuscan) or is it manual file upload? | External HTTP Client implementation vs static file storage |
| 2 | Biometrics / NIN | Does NIN verification connect directly to the NIRA (National Identification and Registration Authority) API or is it internal verification? | Integration adapter for NIRA gateway |
| 3 | Mobile Money Float | Are float balances polled via Telco APIs (MTN MoMo API, Airtel Money API) or updated via webhook callbacks from the MSACCO switch? | Asynchronous webhook listener vs scheduled balance polling worker |
| 4 | Accounting Engine | What is the exact Chart of Accounts structure (account code digit length, parent-child depth)? | Schema design of `chart_of_accounts` table |
| 5 | Storage Engine | Should uploaded documents (photos, signatures, IDs) be stored in local file storage or S3-compatible object storage? | Blob storage abstraction layer |
| 6 | Soft-Delete Policy | What is the permanent retention or purging lifecycle for soft-deleted users and customers? | Database cleanup / archiving jobs |

---
""")

    # 26. Assumptions
    sections.append("""## 26. Assumptions

1. **Database Engine:** PostgreSQL 15+ is assumed as the primary relational database to support JSONB attributes, transactions, and robust decimal precision.
2. **Statutory Jurisdiction:** The system operates under Ugandan legal and financial regulatory jurisdiction (BOU, UMRA, URA).
3. **Currency:** Primary accounting currency is `UGX` (Ugandan Shilling) with decimal precision set to 2 decimal places.
4. **Timezone:** Standard operating timezone is East Africa Time (EAT, UTC+3).
5. **Maker-Checker Strictness:** Maker-checker is mandatory for all member on-boarding, loan approvals, and transactions exceeding user-specific limits.

---
""")

    # 27. Traceability Matrix
    sections.append("""## 27. Traceability Matrix

| HTML Source File | Feature / Subsystem | Entities Involved | Requirement IDs | Evidence & Traceability |
|---|---|---|---|---|
| `create_member_step_one.html` | Member Type Selection | Customer | FR-001 | Cards for Individuals, Group, Joint, Institution |
| `create_individual_member.html` | Individual Onboarding | Customer, CustomerIndividual, NextOfKin, Employment, BankDetails | FR-002 | Complete 8-section form, file uploads, branch selection |
| `create_group.html` | Group Onboarding | Customer, CustomerGroup | FR-004 | Group Name, Registration number, Members/Non-members |
| `create_joint.html` | Joint Onboarding | Customer, CustomerJoint | FR-005 | Joint Name, shared addresses, loan limit toggle |
| `create_institution.html` | Institution Onboarding | Customer, CustomerInstitution | FR-006 | TIN, Business Type, Category, Ownership dropdowns |
| `list_members.html` | Member Directory | Customer, Branch | FR-001, FR-002 | Master table displaying members with phone, age, group |
| `list_groups.html` | Group Directory Tab | CustomerGroup | FR-004 | Groups filter subtab under /customers |
| `list_joints.html` | Joint Directory Tab | CustomerJoint | FR-005 | Joints filter subtab under /customers |
| `list_institutions.html` | Institution Directory Tab | CustomerInstitution | FR-006 | Institutions filter subtab under /customers |
| `list_member_applications.html` | Member Vetting Queue | Customer | FR-002, FR-003 | Pending / Rejected tabs, view application triggers |
| `list_agency_applications.html` | Agency Vetting Queue | AgencyApplication | FR-014 | Agent application pending / rejected tabs |
| `create_user.html` | User Provisioning | User, UserNotificationSubscription | FR-007 | Staff form, 9 Min/Max limit pairs, 22 alert checkboxes |
| `list_users.html` | User Administration | User | FR-008 | User cards, Deactivate, Delete, Enable SMS OTP buttons |
| `create_role.html` | Role Definition | Role, RolePermission | FR-009 | Role name, Open/Close time, 382 permission checkboxes |
| `edit_role.html` | Role Modification | Role, RolePermission | FR-009 | Permission editing and operating hour adjustments |
| `user_details.html` | Role Profile View | Role, RolePermission | FR-009 | Edit role profile UI misnamed as user_details.html |
| `list_roles.html` | Role Administration | Role | FR-009, FR-010 | Role cards, active user counts, operating hours |
| Global Top Bar / Shell | Liquidity & Alerts | ExternalFloat, Notification, Session | FR-011, FR-012, FR-013 | Float counters, Failed job alert, Idle detector |

---
""")

    # 28. Backend Implementation Considerations
    sections.append("""## 28. Backend Implementation Considerations

1. **Transactional Ledger Integrity:** Implement strict double-entry ledger primitives. Every financial event (deposit, withdrawal, disbursement, fee) must post balanced debit and credit entries to the general ledger within an isolated database transaction.
2. **Middleware Pipeline:**
   - `AuthenticationMiddleware`: Decodes JWT, validates active session.
   - `OperatingHoursMiddleware`: Evaluates current timestamp against user's role `open_time` and `close_time`.
   - `PermissionGuardMiddleware`: Checks route action against user's assigned role permission set.
   - `FinancialLimitInterceptor`: Checks transaction amount against user's min/max threshold.
3. **Asynchronous Processing:** Use a distributed task queue (e.g. Celery / BullMQ / Redis) for background jobs:
   - Interest accruals on savings and fixed deposits.
   - Batch standing order executions.
   - SMS and email notification dispatching.
   - Job health monitoring with automatic dispatch of `failed_job` alerts.

---
""")

    # 29. Final System Summary
    sections.append("""## 29. Final System Summary

The reverse-engineering of the 17 provided HTML artifacts establishes that **SavingsPlus** is an enterprise Core Banking System (CBS) designed for **NSIMBI SACCO LTD** by **FutureLink Technologies (FLT)**. 

The backend architecture requires:
- **Comprehensive Customer Information File (CIF):** Supporting Individuals, Groups, Joints, and Institutions with maker-checker onboarding.
- **Granular Security Framework:** 34 modules, 382 permissions, strict operating time windows, 5-minute idle timeouts, and individual financial limit ceilings across 9 transaction classes.
- **Integrated Operations:** End-to-end management of Savings, Loans, Shares, Accounting, Fixed Assets, Staff Payroll, External Float (MTN, Airtel, MSACCO, Bank), and Regulatory Compliance (BOU & UMRA).

This specification provides the exhaustive, unambiguous foundation for backend developers and automated coding agents to implement the database models, business logic services, and REST API controllers for the NSIMBI Core Banking System.
""")

    full_text = "\n".join(sections)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write(full_text)
    
    # Also write a copy to ~/Downloads/MSACCO/REQUIREMENTS.md for user convenience
    downloads_copy = os.path.expanduser("~/Downloads/MSACCO/REQUIREMENTS.md")
    with open(downloads_copy, "w", encoding="utf-8") as f:
        f.write(full_text)

    print(f"REQUIREMENTS.md generated successfully at:\n - {OUTPUT_FILE}\n - {downloads_copy}")
    print(f"Total lines: {len(full_text.splitlines())}")

if __name__ == "__main__":
    build_requirements()

### FR-015 — Savings Account Directory & Status Filtering
- **Requirement:** The system shall list all customer savings accounts with account number, member name, branch, product, opened date, actual balance, available balance, and status filters (`All`, `Active`, `Inactive`, `Frozen`).
- **Entity:** `SavingsAccount`
- **Actor:** Cashier / Customer Care / Manager
- **Source:** `list_savings.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated staff.
- **Input:** Filter criteria (Status, Branch, Search term).
- **Business Rules:** Account number formatted as 12 digits (`003500005281`).
- **Expected Result:** Paginated list of savings accounts.
- **Failure Conditions:** Unauthorized access.
- **Evidence Level:** Explicit

### FR-016 — Savings Account Detail & Cash Desk Terminal
- **Requirement:** The system shall provide an operational cash desk screen for an individual savings account displaying Actual Balance, Available Balance, KYC photo/signature preview, and immediate transaction buttons: Deposit, Withdraw, New Transfers, Debit Account, Credit Account, Close, and Freeze.
- **Entity:** `SavingsAccount`, `SavingsTransaction`
- **Actor:** Cashier / Teller
- **Source:** `saving_account_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active savings account.
- **Input:** Account Number (`003500005281`).
- **Business Rules:** Displays Available Balance (`actual_balance - 5000 UGX reserve`).
- **Expected Result:** Detailed account ledger and action triggers.
- **Failure Conditions:** Account does not exist.
- **Evidence Level:** Explicit

### FR-017 — Savings SMS Alert Rules Configuration
- **Requirement:** The system shall allow operators to configure granular SMS transaction and loan notification subscriptions per savings account across 14 discrete triggers.
- **Entity:** `SavingsSMSAlertConfig`
- **Actor:** Customer Care / Staff
- **Source:** `edit_saving_account.html`, `saving_account_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Target savings account exists.
- **Input:** Boolean flags for: On Deposit, On Withdrawal, Earning Interest, Earning Dividends, Outgoing Transfer, Incoming Transfer, Direct Debit, Direct Credit, Standing Order, Loan Application, Loan Disbursement, Loan Payment, Loan Refund.
- **Business Rules:** Saved flags dictate which account activities dispatch transactional SMS messages via the SMS gateway.
- **Expected Result:** SMS alert configuration persisted.
- **Failure Conditions:** Invalid account ID.
- **Evidence Level:** Explicit

### FR-018 — Fixed Deposit Placement & Booking
- **Requirement:** The system shall book term/fixed deposits, specifying principal amount, tenure in months, annual interest rate, interest payment interval, maturity payout option, and funding source (Cash counter vs Savings account offset).
- **Entity:** `FixedDeposit`, `SavingsAccount`
- **Actor:** Customer Care / Branch Manager
- **Source:** `create_fixed_deposit.html`
- **Priority:** MUST HAVE
- **Preconditions:** Member possesses an active savings account.
- **Input:** Member, Transfer/Payout Account, Depositor Name, Depositor Phone, Principal Amount, Starts On, Period in Months, Interest Rate (%), Interest Interval (`monthly`, `yearly`, `Not applicable`), Payout Option (`Principal`, `Principal & Interest`), Funding Method (`Till - Cash or cheque`, `Offset - From savings`).
- **Business Rules:** If offset selected, funds immediately placed on hold/debited from source savings account. Enters Maker-Checker approval queue (`list_fixed_deposit_applications.html`).
- **Expected Result:** Fixed deposit contract created in `PENDING` status.
- **Failure Conditions:** Insufficient available balance in source savings account.
- **Evidence Level:** Explicit

### FR-019 — Fixed Deposit Lifecycle & Termination
- **Requirement:** The system shall track fixed deposits through their complete lifecycle and support both premature and maturity termination with interest calculation.
- **Entity:** `FixedDeposit`
- **Actor:** Branch Manager
- **Source:** `list_fixed_deposits.html`, `fixed_deposit_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Fixed deposit in `RUNNING` or `MATURED_NOT_AWARDED` status.
- **Input:** Deposit ID, Action (`TERMINATE`), Interest Decision (`WITH_INTEREST`, `NO_INTEREST`).
- **Business Rules:** Terminating returns principal (and approved interest) to the designated transfer savings account. Status transitions to `AWARDED_ON_MATURITY`, `TERMINATED_NO_INTEREST`, or `TERMINATED_WITH_INTEREST`.
- **Expected Result:** Funds transferred; contract archived.
- **Failure Conditions:** Contract already liquidated.
- **Evidence Level:** Explicit

### FR-020 — Standing Orders Recurring Execution
- **Requirement:** The system shall maintain standing orders with automated periodic execution and track statuses: `Pending`, `Approved`, `Running`, `Rejected`, `Expired`.
- **Entity:** `StandingOrder`
- **Actor:** Customer Care / Scheduler
- **Source:** `list_standing_orders.html`
- **Priority:** MUST HAVE
- **Preconditions:** Source savings account active.
- **Input:** Source Account, Destination Account, Amount, Frequency, Start Date.
- **Business Rules:** Background worker sweeps source account on execution date. If balance insufficient, records failed execution and dispatches alert `standing_orders_pending_approval`.
- **Expected Result:** Transfer posted between accounts.
- **Failure Conditions:** Expired order or insufficient balance.
- **Evidence Level:** Explicit

### FR-021 — Debit Card Issuance Tracking
- **Requirement:** The system shall manage the requisition, approval, and issuance of co-branded Interswitch debit cards linked to member savings accounts.
- **Entity:** `DebitCardIssuance`
- **Actor:** Customer Care / Branch Manager
- **Source:** `list_debit_card_issuance.html`, `saving_account_detail.html`
- **Priority:** SHOULD HAVE
- **Preconditions:** Active savings account.
- **Input:** Savings Account Number, Card PAN / Serial Number.
- **Business Rules:** Card status tracks through `Pending` -> `Approved` -> `Issued` -> `Rejected`.
- **Expected Result:** Card linked for ATM/POS debit transactions.
- **Failure Conditions:** Account frozen or inactive.
- **Evidence Level:** Explicit

### FR-022 — Loan Amortization Calculator
- **Requirement:** The system shall provide an interactive loan amortization calculator supporting multiple interest calculation methods (`flat`, `declining`, `amortize`) and frequencies (`daily`, `weekly`, `monthly`, `annually`), generating installment amounts and repayment schedules.
- **Entity:** `LoanProduct`, `LoanRepaymentSchedule`
- **Actor:** Loan Officer / Customer
- **Source:** `loan_calculator.html`
- **Priority:** MUST HAVE
- **Preconditions:** Loan products configured in system.
- **Input:** Product ID, Principal Amount, Interest Method, Annual Interest Rate, Frequency, Number of Installments, Start Date, Grace Period (Months).
- **Business Rules:** Generates accurate mathematical repayment schedule preview.
- **Expected Result:** Installment amount and period-by-period breakdown of Principal, Interest, and Remaining Balance.
- **Failure Conditions:** Negative principal or rate.
- **Evidence Level:** Explicit

### FR-023 — Credit Reference Bureau (CRB / CBA) Vetting
- **Requirement:** The system shall integrate credit reporting workflows allowing officers to record borrower consent and perform credit vetting against Credit Bureau Africa (CBA).
- **Entity:** `CreditReportingInquiry`
- **Actor:** Loan Officer
- **Source:** `list_credit_reporting.html`
- **Priority:** MUST HAVE
- **Preconditions:** Borrower signed CRB consent form (`initiate crb report consent`).
- **Input:** Member NIN / Account Number, Bureau Identifier (`CBA`).
- **Business Rules:** Credit check results logged in bureau register (`view crb register`) prior to loan approval.
- **Expected Result:** Credit history report and risk rating returned.
- **Failure Conditions:** Bureau service unavailable.
- **Evidence Level:** Explicit

### FR-024 — Loan Servicing, Auto-Pay & Repayments
- **Requirement:** The system shall manage active loan contracts, displaying disbursement date, maturity date, interest rate (e.g. `42.0%`), loan officer, and linked repayment account, supporting manual payment posting and automated savings sweeps.
- **Entity:** `Loan`, `SavingsAccount`, `LoanRepayment`
- **Actor:** Loan Officer / Cashier / System Scheduler
- **Source:** `loan_detail.html`, `list_loans.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active loan contract in `RUNNING` status.
- **Input:** Loan Number (`1D04FE1229`), Payment Amount, Payment Mode.
- **Business Rules:** If `auto_pay_from_savings=true`, system automatically debits linked savings account (`003500000651`) on due date.
- **Expected Result:** Installment credited against outstanding interest and principal.
- **Failure Conditions:** Delinquent repayment incurs late penalties.
- **Evidence Level:** Explicit

### FR-025 — Loan Restructuring, Waivers & Legal Recovery
- **Requirement:** The system shall support credit risk interventions including interest waivers, penalty waivers, admin fee waivers, grace period additions, officer re-assignments, loan restructuring/rescheduling (generating new loan numbers referencing original facility), legal referrals (`Send to legal`), and bad debt write-offs (`Write off`).
- **Entity:** `Loan`
- **Actor:** Credit Manager
- **Source:** `loan_detail.html`
- **Priority:** MUST HAVE
- **Preconditions:** Delinquent or performing loan requiring administrative modification.
- **Input:** Loan Number, Action (`WAIVE_INTEREST`, `WAIVE_PENALTY`, `WAIVE_ADMIN_FEES`, `RESCHEDULE`, `ADD_GRACE_PERIOD`, `TRANSFER_OFFICER`, `SEND_TO_LEGAL`, `WRITE_OFF`), Justification Notes, Adjusted Terms.
- **Business Rules:** Waivers post corresponding reversing entries. Rescheduling closes old loan and books new loan contract. Write-offs post against Loan Loss Provision accounts.
- **Expected Result:** Loan contract state and ledger updated.
- **Failure Conditions:** Insufficient authorization limits.
- **Evidence Level:** Explicit

---
""")

    # 15. API Requirements
    s.append("""## 15. API Requirements

### Extended API Catalog (Incorporating Savings & Loans)
- `GET /savings` — Query savings accounts with status filters (`active`, `inactive`, `frozen`).
- `GET /savings/{number}` — Fetch detailed account overview, available vs actual balance, and recent ledger entries.
- `POST /savings/{number}/deposit` — Post cash or counter deposit to savings account.
- `POST /savings/{number}/create-individual-withdrawal` — Post cash withdrawal from savings account.
- `POST /savings/{number}/direct-transfers` — Execute inter-account transfer.
- `POST /savings/{number}/freeze` / `POST /savings/{number}/unfreeze` — Toggle administrative hold.
- `POST /savings/{number}/close` — Liquidate balance and close account.
- `PATCH /savings/{number}/alerts` — Update 14 SMS alert rule checkboxes.
- `GET /savings/applications` — Fetch pending savings account opening applications.
- `POST /savings/applications/{id}/approve` — Approve savings account opening.
- `GET /savings/fixed-deposits` — List term deposits with 7 lifecycle status filters.
- `POST /savings/fixed-deposits` — Book new fixed deposit (Till or Savings Offset).
- `GET /savings/fixed-deposits/{id}` — Fetch fixed deposit terms and certificate data.
- `POST /savings/fixed-deposits/{id}/terminate` — Liquidate fixed deposit before or at maturity.
- `GET /savings/standing-orders` — List recurring standing orders.
- `POST /savings/{number}/standing-orders/create` — Setup new recurring standing order.
- `GET /savings/debit-cards` — List debit card issuance requests and statuses.
- `POST /savings/{number}/debit-cards` — Request Interswitch debit card issuance.
- `GET /loans` — List active loan facilities with portfolio filters (`outstanding`, `due`, `performing`, `sent_to_legal`).
- `GET /loans/{number}` — Fetch full loan details, linked repayment account, and amortization schedule.
- `POST /loans/calculator` — Calculate amortization schedule based on method (`flat`, `declining`, `amortize`).
- `GET /loans/applications` — Fetch loan origination pipeline (`approved`, `pending`, `rejected`, `cancelled`).
- `POST /loans/applications` — Submit new loan origination application.
- `POST /loans/applications/{id}/approve` — Sanction loan application.
- `POST /loans/applications/{id}/disburse` — Disburse approved funds into member savings account.
- `POST /loans/{number}/repayments` — Post manual loan repayment.
- `POST /loans/{number}/waiver-interest` — Waive accrued interest.
- `POST /loans/{number}/waiver-penalty` — Waive late payment penalties.
- `POST /loans/{number}/reschedule` — Restructure loan and issue new facility reference.
- `POST /loans/{number}/send-to-legal` — Forward defaulted loan to legal debt collection.
- `POST /loans/{number}/write-off` — Write off bad debt against loan loss provisions.
- `GET /loans/credit-reporting` — Query credit bureau history (CBA).

---
""")

    # 16. Data / Database Requirements
    s.append("""## 16. Data / Database Requirements

### Schema Additions for Savings and Loans
```sql
-- Savings Products Table
CREATE TABLE savings_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'FlexSave'
    code VARCHAR(20) UNIQUE NOT NULL,
    minimum_balance DECIMAL(18,2) NOT NULL DEFAULT 5000.00,
    opening_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_rate_annual DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Savings Accounts Table
CREATE TABLE savings_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(30) UNIQUE NOT NULL, -- e.g. '003500005281'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    product_id INTEGER NOT NULL REFERENCES savings_products(id),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED')),
    actual_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    opening_fee_charged BOOLEAN NOT NULL DEFAULT FALSE,
    last_ledger_fee_charged_at TIMESTAMP WITH TIME ZONE,
    last_transaction_at TIMESTAMP WITH TIME ZONE,
    opened_at DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Savings SMS Alert Rules
CREATE TABLE savings_sms_alert_configs (
    account_id BIGINT PRIMARY KEY REFERENCES savings_accounts(id) ON DELETE CASCADE,
    notify_on_deposit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_withdrawal BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_earning_interest BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_earning_dividends BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_outgoing_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_incoming_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_debit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_credit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_standing_order BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_application BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_loan_disbursement BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_payment BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_refund BOOLEAN NOT NULL DEFAULT FALSE
);

-- Fixed Deposits Table
CREATE TABLE fixed_deposits (
    id BIGSERIAL PRIMARY KEY,
    deposit_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    funding_source VARCHAR(30) NOT NULL CHECK (funding_source IN ('TILL_CASH', 'SAVINGS_OFFSET')),
    funding_savings_account_id BIGINT REFERENCES savings_accounts(id),
    payout_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    depositor_name VARCHAR(150) NOT NULL,
    depositor_phone VARCHAR(30) NOT NULL,
    principal_amount DECIMAL(18,2) NOT NULL,
    starts_on DATE NOT NULL,
    period_months INTEGER NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_interval VARCHAR(30) NOT NULL CHECK (interest_interval IN ('monthly', 'yearly', 'Not applicable')),
    payout_option VARCHAR(50) NOT NULL CHECK (payout_option IN ('Principal', 'Principal & Interest')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RUNNING', 'AWARDED', 'TERMINATED', 'MATURED_NOT_AWARDED', 'TERMINATED_NO_INTEREST', 'AWARDED_ON_MATURITY', 'TERMINATED_WITH_INTEREST')),
    matures_at DATE NOT NULL,
    accrued_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Standing Orders Table
CREATE TABLE standing_orders (
    id BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    destination_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    amount DECIMAL(18,2) NOT NULL,
    frequency VARCHAR(30) NOT NULL CHECK (frequency IN ('daily', 'weekly', 'monthly')),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'RUNNING', 'REJECTED', 'EXPIRED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Products Table
CREATE TABLE loan_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL, -- e.g. 'NCHIEI Credit', 'Business Loans'
    code VARCHAR(20) UNIQUE NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    default_annual_interest_rate DECIMAL(5,2) NOT NULL,
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    min_principal DECIMAL(18,2) NOT NULL,
    max_principal DECIMAL(18,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Loans Table
CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    loan_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '1D04FE1229'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    loan_officer_id BIGINT NOT NULL REFERENCES users(id),
    product_id INTEGER NOT NULL REFERENCES loan_products(id),
    repayment_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    principal_disbursed DECIMAL(18,2) NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    installments_count INTEGER NOT NULL,
    grace_period_months INTEGER NOT NULL DEFAULT 0,
    installment_amount DECIMAL(18,2) NOT NULL,
    auto_pay_from_savings BOOLEAN NOT NULL DEFAULT TRUE,
    disbursed_at DATE NOT NULL,
    maturity_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING', 'PERFORMING', 'DUE', 'OUTSTANDING', 'SENT_TO_LEGAL', 'WRITTEN_OFF', 'RESCHEDULED', 'CLEARED')),
    rescheduled_from_loan_number VARCHAR(50),
    total_principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_penalty_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_penalty DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_admin_fees DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loan Repayment Schedules Table
CREATE TABLE loan_repayment_schedules (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(18,2) NOT NULL,
    interest_due DECIMAL(18,2) NOT NULL,
    total_installment DECIMAL(18,2) NOT NULL,
    principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    is_settled BOOLEAN NOT NULL DEFAULT FALSE
);
```

---
""")

    # 17 to 26 - Retain & Expand
    s.append("""## 17. Search, Filtering and Reporting

### Savings & Loan Specific Reporting Capabilities
From the 109 cataloged reports, the following are directly confirmed by the savings and loan captures:
- `view savings accounts`, `view savings ledger`, `view savings transactions`, `view failed standing orders`.
- `view fixed deposits`, `view fixed certificate`.
- `view loan applications`, `view loan arrears`, `view loan bookings`, `view loan schedule`, `view loan ledger`, `view loan repayments`, `view loan repayment summary`, `view cleared loans`, `view due loans`, `view written off loans`, `view rescheduled loans`.
- `view par by ageing`, `view par by ageing summary`, `view umra par by ageing` (Portfolio At Risk aging schedules for regulatory reporting).
- `view crb register`, `download crb files` (Credit Reference Bureau returns).

---

### FR-026 — Chart of Accounts Hierarchy Management
- **Requirement:** The system shall maintain a 5-tier hierarchical Chart of Accounts (Assets, Liabilities, Capital, Income, Expenses) with dash-separated account codes, supporting node creation, updates, and deactivation.
- **Entity:** `ChartOfAccount`
- **Actor:** Head of Finance / Accountant
- **Source:** `list_charts_of_accounts_assets.html`, `create_chart_of_account_asset.html`
- **Priority:** MUST HAVE
- **Preconditions:** Authenticated user with `register chart account` permission.
- **Input:** Account Name*, Parent Account Node, Subtype (Optional), Tags (Optional).
- **Business Rules:** System generates dash-separated hierarchical code; cannot deactivate an account with a non-zero balance.
- **Expected Result:** New GL node created in Chart of Accounts.
- **Failure Conditions:** Duplicate account code.
- **Evidence Level:** Explicit

### FR-027 — Double-Entry Journal Voucher Creation & Reversal
- **Requirement:** The system shall capture balanced double-entry Journal Vouchers across branches and ledger accounts, supporting maker-checker reversal workflows.
- **Entity:** `JournalVoucher`, `JournalVoucherItem`
- **Actor:** Accountant / Accounts Assistant
- **Source:** `create_journal.html`, `journals.html.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid active accounts in Chart of Accounts.
- **Input:** Contact Person*, Registration Date*, Reference Number*, Method (`cash`, `cheque`)*, Comment*, Debit Account*, Credit Account*, Amount*.
- **Business Rules:** Total debits must equal total credits; reversals must reference original voucher and provide justification.
- **Expected Result:** Journal voucher posted to General Ledger or queued for approval.
- **Failure Conditions:** Debits != Credits (`ERR_TB_OUT_OF_BALANCE`).
- **Evidence Level:** Explicit

### FR-028 — Financial Period Closing, TB Archival & Rollback Protection
- **Requirement:** The system shall close financial periods, generate an immutable Trial Balance snapshot PDF, lock the period against backdated transactions, and allow authorized rollback.
- **Entity:** `FinancialPeriod`
- **Actor:** Head of Finance / Administrator
- **Source:** `closing_and_restore_books.html`
- **Priority:** MUST HAVE
- **Preconditions:** All subledgers reconciled; Trial Balance in balance.
- **Input:** Period End Date, Action (`CLOSE`, `ROLLBACK`).
- **Business Rules:** Closing generates `Download TB(pdf)` snapshot; rollback triggers alert `opening_a_closed_period`.
- **Expected Result:** Period locked or reopened.
- **Failure Conditions:** Out of balance Trial Balance (`tb_imbalance`).
- **Evidence Level:** Explicit

### FR-029 — Financial Approvals Maker-Checker Queue
- **Requirement:** The system shall provide an approval queue for accounting transactions, journal vouchers, and bank transfers with pending, rejected, and approved queues.
- **Entity:** `FinancialApproval`
- **Actor:** Branch Manager / Head of Finance
- **Source:** `approvals.html`
- **Priority:** MUST HAVE
- **Preconditions:** Transaction submitted by maker.
- **Input:** Approval ID, Decision (`APPROVE`, `REJECT`), Reason.
- **Business Rules:** Checker cannot be maker (BR-001).
- **Expected Result:** Transaction committed to ledger or rejected.
- **Failure Conditions:** Checker is maker.
- **Evidence Level:** Explicit

### FR-030 — Fixed Asset Register & Depreciation
- **Requirement:** The system shall maintain a register of physical assets and asset profiles, supporting periodic depreciation calculation, revaluations, and disposals.
- **Entity:** `FixedAsset`, `AssetProfile`
- **Actor:** Accountant
- **Source:** `list_fixed_assets.html`, `list_asset_profiles.html`
- **Priority:** SHOULD HAVE
- **Preconditions:** Asset profile defined.
- **Input:** Asset Name, Profile, Purchase Date, Cost, Salvage Value, Useful Life.
- **Business Rules:** Depreciation posts monthly debit to Depreciation Expense and credit to Accumulated Depreciation.
- **Expected Result:** Asset tracked on balance sheet.
- **Failure Conditions:** Invalid profile.
- **Evidence Level:** Explicit

### FR-031 — Member Share Capital Administration
- **Requirement:** The system shall manage member share equity accounts, recording share capital subscriptions, purchases, transfers, and liquidations.
- **Entity:** `ShareAccount`, `ShareTransaction`
- **Actor:** Customer Care / Accountant
- **Source:** `list_shares.html`
- **Priority:** MUST HAVE
- **Preconditions:** Active member.
- **Input:** Member ID, Number of Shares, Unit Price.
- **Business Rules:** Total share capital reflects under Capital tier of Chart of Accounts.
- **Expected Result:** Member share balance updated.
- **Failure Conditions:** Member inactive.
- **Evidence Level:** Explicit

### FR-032 — Password-Authenticated Share Dividend Payout
- **Requirement:** The system shall declare and distribute share dividends based on shareholding periods, requiring staff password re-authentication, supporting distribution directly into savings accounts or equity capitalization.
- **Entity:** `DividendDeclaration`
- **Actor:** Head of Finance / General Manager
- **Source:** `share_dividends.html`
- **Priority:** MUST HAVE
- **Preconditions:** Approved dividend pool; member shareholdings verified.
- **Input:** Amount*, Use Percentages*, Date*, Share Period (Months)*, Method (`savings`, `shares`)*, Saving Product, Reference Number*, Comment*, Password*.
- **Business Rules:** Password re-verified; credits destination savings product (`FlexSave`) or increases member shares held.
- **Expected Result:** Dividend disbursed across all qualifying members; event logged.
- **Failure Conditions:** Incorrect password (`ERR_INVALID_PASSWORD`).
- **Evidence Level:** Explicit

### FR-033 — Multi-Entity Batch Data Import Engine
- **Requirement:** The system shall provide batch data ingestion with downloadable templates and preview validation across 9 entity types: Individuals, Institutions, Joint Accounts, Groups, Group Members, Loans, Fixed Deposits, Savings, and savingsAccounts.
- **Entity:** `DataImportBatch`
- **Actor:** System Administrator / Migration Specialist
- **Source:** `imports.html`, `preview_import.html`
- **Priority:** MUST HAVE
- **Preconditions:** Valid CSV template.
- **Input:** Template Type*, Branch*, Reference Number*, Document File*.
- **Business Rules:** Preview validates column mappings and schema errors prior to commit.
- **Expected Result:** Bulk records ingested into respective database tables.
- **Failure Conditions:** File parsing failure or validation mismatch.
- **Evidence Level:** Explicit

---

## 15. API Requirements

### Master RESTful API Surface
- **Auth:** `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/user`, `POST /api/auth/refresh`.
- **Customers:** `GET /customers`, `POST /customers/individuals`, `POST /customers/groups`, `POST /customers/joints`, `POST /customers/institutions`, `GET /customers/applications`, `POST /customers/applications/{id}/approve`, `POST /customers/applications/{id}/reject`, `GET /customers/agent-applications`.
- **Users & Roles:** `GET /users`, `POST /users`, `PATCH /users/{id}`, `POST /users/{id}/deactivate`, `DELETE /users/{id}`, `POST /users/{id}/toggle-sms-otp`, `GET /users/roles`, `POST /users/roles`, `GET /users/roles/{id}`, `PATCH /users/roles/{id}`, `POST /users/roles/{id}/activate`, `POST /users/roles/{id}/deactivate`, `DELETE /users/roles/{id}`.
- **Savings:** `GET /savings`, `GET /savings/{number}`, `POST /savings/{number}/deposit`, `POST /savings/{number}/create-individual-withdrawal`, `POST /savings/{number}/direct-transfers`, `POST /savings/{number}/freeze`, `POST /savings/{number}/unfreeze`, `POST /savings/{number}/close`, `PATCH /savings/{number}/alerts`, `GET /savings/applications`, `POST /savings/applications/{id}/approve`.
- **Fixed Deposits:** `GET /savings/fixed-deposits`, `POST /savings/fixed-deposits`, `GET /savings/fixed-deposits/{id}`, `POST /savings/fixed-deposits/{id}/terminate`.
- **Standing Orders:** `GET /savings/standing-orders`, `POST /savings/{number}/standing-orders/create`.
- **Debit Cards:** `GET /savings/debit-cards`, `POST /savings/{number}/debit-cards`.
- **Loans:** `GET /loans`, `GET /loans/{number}`, `POST /loans/calculator`, `GET /loans/applications`, `POST /loans/applications`, `POST /loans/applications/{id}/approve`, `POST /loans/applications/{id}/disburse`, `POST /loans/{number}/repayments`, `POST /loans/{number}/waiver-interest`, `POST /loans/{number}/waiver-penalty`, `POST /loans/{number}/reschedule`, `POST /loans/{number}/send-to-legal`, `POST /loans/{number}/write-off`, `GET /loans/credit-reporting`.
- **Accounting & General Ledger:** `GET /accounting/chart`, `POST /accounting/chart`, `PATCH /accounting/chart/{id}`, `POST /accounting/chart/{id}/deactivate`, `GET /accounting/journals`, `POST /accounting/journals`, `POST /accounting/journals/{id}/reverse`, `GET /accounting/approvals`, `POST /accounting/approvals/{id}/approve`, `POST /accounting/approvals/{id}/reject`, `GET /accounting/closed-periods`, `POST /accounting/closed-periods`, `POST /accounting/closed-periods/{id}/rollback`, `GET /accounting/fixed-assets`, `POST /accounting/fixed-assets`, `GET /accounting/fixed-assets/profiles`, `GET /accounting/budget`.
- **Shares & Dividends:** `GET /shares`, `POST /shares/create`, `POST /shares/dividends`.
- **Batch Imports:** `GET /settings/imports`, `POST /settings/imports`, `GET /settings/imports/preview`.
- **System Monitoring:** `GET /notifications`, `POST /notifications/{id}/mark-read`, `GET /settings/float-balances`.

---

## 16. Data / Database Requirements

### Complete Relational Schema (PostgreSQL DDL)
```sql
-- Branches Table
CREATE TABLE branches (
    id SERIAL PRIMARY KEY,
    branch_code VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Roles & Permissions Tables
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    open_time TIME NOT NULL DEFAULT '00:00:00',
    close_time TIME NOT NULL DEFAULT '23:59:59',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role_permissions (
    id SERIAL PRIMARY KEY,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_key VARCHAR(100) NOT NULL,
    UNIQUE(role_id, permission_key)
);

-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    role_id INTEGER NOT NULL REFERENCES roles(id),
    station_id VARCHAR(50), -- e.g. '1-1-1-1-2 Teller - 1'
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    sms_otp_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    
    -- Transaction Limits
    loan_approval_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    loan_approval_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    disbursement_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    deposits_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    withdrawals_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    shares_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    fixed_assets_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    jvs_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    default_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_min_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    transfer_max_amount DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_notification_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_key VARCHAR(100) NOT NULL,
    UNIQUE(user_id, event_key)
);

-- Customers Master Table
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    member_number VARCHAR(50) UNIQUE NOT NULL, -- e.g. '00350000001'
    customer_type VARCHAR(20) NOT NULL CHECK (customer_type IN ('INDIVIDUAL', 'GROUP', 'JOINT', 'INSTITUTION')),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    savings_officer_id BIGINT REFERENCES users(id),
    entry_date DATE NOT NULL,
    phone_primary VARCHAR(30) NOT NULL,
    phone_secondary VARCHAR(30),
    email VARCHAR(150),
    country VARCHAR(100) NOT NULL DEFAULT 'Uganda',
    town VARCHAR(100),
    physical_address TEXT NOT NULL,
    postal_code VARCHAR(30),
    postal_address VARCHAR(150),
    has_loan_limit BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'REJECTED', 'INACTIVE', 'DORMANT')),
    created_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    approved_at TIMESTAMP WITH TIME ZONE,
    rejection_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Individual Profile
CREATE TABLE customer_individuals (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    salutation VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('Male', 'Female')),
    marital_status VARCHAR(20) NOT NULL,
    is_dependent BOOLEAN NOT NULL DEFAULT FALSE,
    is_pwd BOOLEAN NOT NULL DEFAULT FALSE,
    date_of_birth DATE NOT NULL,
    country_of_birth VARCHAR(100) NOT NULL,
    nationality VARCHAR(100) NOT NULL DEFAULT 'Ugandan',
    home_ownership VARCHAR(20) NOT NULL CHECK (home_ownership IN ('Owner', 'Tenant', 'Other')),
    identification_type VARCHAR(50) NOT NULL,
    identification_number VARCHAR(100) NOT NULL,
    card_number VARCHAR(100),
    signature_file_path TEXT,
    identification_file_path TEXT,
    photo_file_path TEXT
);

CREATE TABLE customer_next_of_kin (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    second_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    physical_address TEXT NOT NULL,
    relationship VARCHAR(30) NOT NULL
);

CREATE TABLE customer_employment_income (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    occupation VARCHAR(100) NOT NULL,
    employer_name VARCHAR(150) NOT NULL,
    employer_address TEXT NOT NULL,
    income_frequency VARCHAR(50) NOT NULL,
    income_per_month DECIMAL(18,2) NOT NULL
);

CREATE TABLE customer_external_banks (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    primary_bank_name VARCHAR(100) NOT NULL,
    primary_bank_account VARCHAR(100) NOT NULL,
    secondary_bank_name VARCHAR(100),
    secondary_bank_account VARCHAR(100)
);

CREATE TABLE customer_groups (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    group_name VARCHAR(150) NOT NULL,
    group_type VARCHAR(50) NOT NULL CHECK (group_type IN ('Members', 'Non Members')),
    registration_number VARCHAR(100) NOT NULL
);

CREATE TABLE customer_joints (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    joint_name VARCHAR(150) NOT NULL
);

CREATE TABLE customer_institutions (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    institution_name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100) NOT NULL,
    registration_date DATE NOT NULL,
    tin_number VARCHAR(50) NOT NULL,
    business_type VARCHAR(100) NOT NULL,
    institution_category VARCHAR(100) NOT NULL,
    residence_ownership VARCHAR(50) NOT NULL
);

-- Savings Products & Accounts
CREATE TABLE savings_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    minimum_balance DECIMAL(18,2) NOT NULL DEFAULT 5000.00,
    opening_fee DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_rate_annual DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE savings_accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(30) UNIQUE NOT NULL, -- e.g. '003500005281'
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    product_id INTEGER NOT NULL REFERENCES savings_products(id),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED')),
    actual_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    available_balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    opening_fee_charged BOOLEAN NOT NULL DEFAULT FALSE,
    last_ledger_fee_charged_at TIMESTAMP WITH TIME ZONE,
    last_transaction_at TIMESTAMP WITH TIME ZONE,
    opened_at DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE savings_sms_alert_configs (
    account_id BIGINT PRIMARY KEY REFERENCES savings_accounts(id) ON DELETE CASCADE,
    notify_on_deposit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_withdrawal BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_earning_interest BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_earning_dividends BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_outgoing_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_incoming_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_debit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_direct_credit BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_standing_order BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_application BOOLEAN NOT NULL DEFAULT FALSE,
    notify_on_loan_disbursement BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_payment BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_loan_refund BOOLEAN NOT NULL DEFAULT FALSE
);

-- Fixed Deposits Table
CREATE TABLE fixed_deposits (
    id BIGSERIAL PRIMARY KEY,
    deposit_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    funding_source VARCHAR(30) NOT NULL CHECK (funding_source IN ('TILL_CASH', 'SAVINGS_OFFSET')),
    funding_savings_account_id BIGINT REFERENCES savings_accounts(id),
    payout_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    depositor_name VARCHAR(150) NOT NULL,
    depositor_phone VARCHAR(30) NOT NULL,
    principal_amount DECIMAL(18,2) NOT NULL,
    starts_on DATE NOT NULL,
    period_months INTEGER NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_interval VARCHAR(30) NOT NULL CHECK (interest_interval IN ('monthly', 'yearly', 'Not applicable')),
    payout_option VARCHAR(50) NOT NULL CHECK (payout_option IN ('Principal', 'Principal & Interest')),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RUNNING', 'AWARDED', 'TERMINATED', 'MATURED_NOT_AWARDED', 'TERMINATED_NO_INTEREST', 'AWARDED_ON_MATURITY', 'TERMINATED_WITH_INTEREST')),
    matures_at DATE NOT NULL,
    accrued_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE standing_orders (
    id BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    destination_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    amount DECIMAL(18,2) NOT NULL,
    frequency VARCHAR(30) NOT NULL CHECK (frequency IN ('daily', 'weekly', 'monthly')),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'RUNNING', 'REJECTED', 'EXPIRED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE debit_card_issuances (
    id BIGSERIAL PRIMARY KEY,
    savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    card_pan_masked VARCHAR(30),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'ISSUED', 'REJECTED')),
    requested_by BIGINT REFERENCES users(id),
    issued_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Loans & Servicing Tables
CREATE TABLE loan_products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    default_annual_interest_rate DECIMAL(5,2) NOT NULL,
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    min_principal DECIMAL(18,2) NOT NULL,
    max_principal DECIMAL(18,2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE loans (
    id BIGSERIAL PRIMARY KEY,
    loan_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    loan_officer_id BIGINT NOT NULL REFERENCES users(id),
    product_id INTEGER NOT NULL REFERENCES loan_products(id),
    repayment_savings_account_id BIGINT NOT NULL REFERENCES savings_accounts(id),
    principal_disbursed DECIMAL(18,2) NOT NULL,
    interest_rate_annual DECIMAL(5,2) NOT NULL,
    interest_method VARCHAR(20) NOT NULL CHECK (interest_method IN ('flat', 'declining', 'amortize')),
    repayment_frequency VARCHAR(20) NOT NULL DEFAULT 'monthly',
    installments_count INTEGER NOT NULL,
    grace_period_months INTEGER NOT NULL DEFAULT 0,
    installment_amount DECIMAL(18,2) NOT NULL,
    auto_pay_from_savings BOOLEAN NOT NULL DEFAULT TRUE,
    disbursed_at DATE NOT NULL,
    maturity_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RUNNING' CHECK (status IN ('RUNNING', 'PERFORMING', 'DUE', 'OUTSTANDING', 'SENT_TO_LEGAL', 'WRITTEN_OFF', 'RESCHEDULED', 'CLEARED')),
    rescheduled_from_loan_number VARCHAR(50),
    total_principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    total_penalty_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_interest DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_penalty DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    waived_admin_fees DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE loan_repayment_schedules (
    id BIGSERIAL PRIMARY KEY,
    loan_id BIGINT NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(18,2) NOT NULL,
    interest_due DECIMAL(18,2) NOT NULL,
    total_installment DECIMAL(18,2) NOT NULL,
    principal_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    interest_paid DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    is_settled BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE credit_reporting_inquiries (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    bureau VARCHAR(50) NOT NULL DEFAULT 'CBA',
    inquiry_type VARCHAR(50) NOT NULL,
    response_payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- General Ledger & Chart of Accounts
CREATE TABLE chart_of_accounts (
    id SERIAL PRIMARY KEY,
    account_code VARCHAR(50) UNIQUE NOT NULL, -- e.g. '1-1-1-1-2'
    name VARCHAR(150) NOT NULL, -- e.g. 'Teller - 1'
    account_category VARCHAR(30) NOT NULL CHECK (account_category IN ('ASSET', 'LIABILITY', 'CAPITAL', 'INCOME', 'EXPENSE')),
    parent_account_id INTEGER REFERENCES chart_of_accounts(id),
    subtype VARCHAR(50),
    tags VARCHAR(100),
    branch_id INTEGER REFERENCES branches(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE journal_vouchers (
    id BIGSERIAL PRIMARY KEY,
    voucher_number VARCHAR(50) UNIQUE NOT NULL,
    registration_date DATE NOT NULL,
    reference_number VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100) NOT NULL,
    payment_method VARCHAR(30) NOT NULL CHECK (payment_method IN ('cash', 'cheque')),
    comment TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'POSTED' CHECK (status IN ('PENDING', 'POSTED', 'REVERSED')),
    created_by BIGINT NOT NULL REFERENCES users(id),
    reversed_by BIGINT REFERENCES users(id),
    reversed_at TIMESTAMP WITH TIME ZONE,
    reversal_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE journal_voucher_items (
    id BIGSERIAL PRIMARY KEY,
    voucher_id BIGINT NOT NULL REFERENCES journal_vouchers(id) ON DELETE CASCADE,
    account_id INTEGER NOT NULL REFERENCES chart_of_accounts(id),
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    entry_type VARCHAR(10) NOT NULL CHECK (entry_type IN ('debit', 'credit')),
    amount DECIMAL(18,2) NOT NULL
);

CREATE TABLE financial_periods (
    id SERIAL PRIMARY KEY,
    period_end_date DATE NOT NULL UNIQUE,
    closed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    closed_by BIGINT NOT NULL REFERENCES users(id),
    is_rolled_back BOOLEAN NOT NULL DEFAULT FALSE,
    rolled_back_at TIMESTAMP WITH TIME ZONE,
    rolled_back_by BIGINT REFERENCES users(id),
    tb_snapshot_pdf_url TEXT
);

-- Member Shares & Dividends
CREATE TABLE share_accounts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT UNIQUE NOT NULL REFERENCES customers(id),
    total_shares INTEGER NOT NULL DEFAULT 0,
    share_unit_price DECIMAL(18,2) NOT NULL DEFAULT 10000.00,
    total_value DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dividend_declarations (
    id BIGSERIAL PRIMARY KEY,
    total_amount DECIMAL(18,2) NOT NULL,
    use_percentages BOOLEAN NOT NULL DEFAULT FALSE,
    declaration_date DATE NOT NULL,
    share_period_months INTEGER NOT NULL,
    payout_method VARCHAR(30) NOT NULL CHECK (payout_method IN ('savings', 'shares')),
    destination_savings_product_id INTEGER REFERENCES savings_products(id),
    reference_number VARCHAR(100) NOT NULL,
    comment TEXT NOT NULL,
    authorized_by BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'DISBURSED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Fixed Assets
CREATE TABLE fixed_assets (
    id BIGSERIAL PRIMARY KEY,
    asset_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    profile_id INTEGER,
    purchase_date DATE NOT NULL,
    purchase_cost DECIMAL(18,2) NOT NULL,
    salvage_value DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    useful_life_years INTEGER NOT NULL,
    depreciation_method VARCHAR(50) NOT NULL DEFAULT 'straight_line',
    accumulated_depreciation DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    net_book_value DECIMAL(18,2) NOT NULL,
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
);

-- Batch Imports
CREATE TABLE data_import_batches (
    id BIGSERIAL PRIMARY KEY,
    template_type VARCHAR(50) NOT NULL,
    reference_number VARCHAR(100) NOT NULL,
    branch_id INTEGER NOT NULL REFERENCES branches(id),
    file_path TEXT NOT NULL,
    total_records INTEGER NOT NULL DEFAULT 0,
    successful_records INTEGER NOT NULL DEFAULT 0,
    failed_records INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'UPLOADED',
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

---

## 17. Search, Filtering and Reporting

### Confirmed Regulatory & Operational Reports (109 Types)
- **Central Bank (BOU) Reports:** `view bou liquidity`, `view bou bsa`.
- **UMRA Reports:** `view umra balance sheet`, `view umra income statement`, `view umra par by ageing`.
- **Credit Operations:** `view loan applications`, `view loan arrears`, `view loan bookings`, `view loan schedule`, `view loan ledger`, `view loan repayments`, `view cleared loans`, `view due loans`, `view written off loans`, `view rescheduled loans`, `view crb register`.
- **Savings & Deposits:** `view savings accounts`, `view savings ledger`, `view savings transactions`, `view fixed deposits`, `view fixed certificate`, `view failed standing orders`.
- **General Ledger & Accounting:** `view general ledger`, `view trial balance`, `view balance sheet`, `view income statement`, `view cash flow statement`, `view journals`, `view backdated transactions`, `view audit trail`, `view mismatches`.
- **Fixed Assets:** `view fixed assets register`, `view asset depreciation`, `view fixed asset profiles`.
- **Shares & Equity:** `view share transactions`, `view shares ledger`, `view cumulative shares`, `view dividends shared`.

---

## 18. File and Document Requirements

1. **Trial Balance Snapshot PDFs:** Immutable PDF documents generated upon closing a financial period (`Download TB(pdf)`).
2. **Data Import Templates:** CSV templates for bulk ingestion of Individuals, Institutions, Joint Accounts, Groups, Group Members, Loans, Fixed Deposits, Savings, and savingsAccounts.
3. **Fixed Deposit & Loan Certificates:** Printable Certificate of Deposit (`Print` on `fixed_deposit_detail.html`) and Amortization Schedules (`Print original schedule` on `loan_detail.html`).
4. **Member KYC Attachments:** Portrait photo, Signature scan, Identification Document scan.

---

## 19. Notification and Event Requirements

The 22 core notification triggers now map directly to all subsystems:
- `tb_imbalance`: Dispatched if General Ledger debits != credits.
- `opening_a_closed_period`: High-priority alert dispatched on period rollback.
- `fixed_deposit_applications_pending_approval`: Dispatched on term deposit placement.
- `loan_applications_pending_approval`: Dispatched on loan origination.
- `standing_orders_pending_approval`: Dispatched on standing order setup or execution failure.
- `failed_job`: Dispatched on background task failure.
- `transactions_pending_approval`: Dispatched when teller transactions breach limit.

---

## 20. Audit and History Requirements

- **General Ledger Immutability:** Financial journals cannot be modified in-place; corrections occur exclusively via reversing entries (`reverse journal`).
- **Closed Period Auditability:** Period rollbacks log authorizer ID, timestamp, and audit trail record.
- **Sensitive Action Authentication:** Dividend distribution logs operator identity and cryptographic confirmation.
- **Rescheduled Debt Lineage:** Historical debt contracts preserved (`Rescheduled from LN00299`).

---

## 21. Error Handling Requirements

- `ERR_TB_OUT_OF_BALANCE`: Attempting to post imbalanced journal voucher or close out-of-balance period.
- `ERR_PERIOD_LOCKED`: Attempting to post transactions with value date inside a closed financial period.
- `ERR_INVALID_PASSWORD_CONFIRMATION`: Incorrect password provided during dividend execution.
- `ERR_INSUFFICIENT_AVAILABLE_BALANCE`: Attempting withdrawal/transfer that breaches minimum reserve.
- `ERR_ACCOUNT_FROZEN`: Debit attempted on frozen account.
- `ERR_OUTSIDE_OPERATING_HOURS`: Access attempted outside role window.

---

## 22. Security Requirements

- **Maker-Checker Segregation of Duties:** Journal creators cannot approve their own vouchers; loan officers cannot sanction loans.
- **Password Re-Authentication:** Enforced on dividend payouts.
- **5-Minute Inactivity Session Lockdown:** Strictly enforced across all screens.

---

## 23. Non-Functional Requirements

- **Financial Decimal Precision:** All monetary amounts stored as `DECIMAL(18,2)`.
- **Sub-Second OLTP Processing:** Ledger postings and balance inquiries `< 250ms`.
- **Audit Durability:** Minimum 7-year regulatory retention for general ledger and journal vouchers.

---

## 24. Cross-Page Findings

1. **Teller Station to GL Binding:** User workstation dropdowns (`1-1-1-1-2 Teller - 1` in `create_user.html`) match the literal Chart of Accounts codes under `1-1-1-1 CASH AT HAND` (`create_journal.html`).
2. **Double-Entry Symmetry:** Every savings cash desk transaction, loan disbursement, and share purchase posts balanced debit/credit entries to the General Ledger.
3. **Dividend Destination Mapping:** Dividends can be routed directly into the `FlexSave` savings product or capitalized into member shares.
4. **Member Account Numbering:** 11-digit member number + 1-digit sequence = 12-digit savings account.

---

## 25. Open Questions

1. **Automated Period Depreciation:** Does depreciation run automatically via monthly cron or is it triggered manually by the Accountant?
2. **Divergent Branch Reconciliations:** What is the settlement period and clearing account protocol for inter-branch transactions?
3. **Dividend Tax Withholding:** Does the system calculate withholding tax (WHT) on dividends prior to net payout into member savings?

---

## 26. Assumptions

1. Primary financial currency is Ugandan Shilling (`UGX`).
2. Chart of Accounts follows a 5-tier classification (Assets, Liabilities, Capital, Income, Expenses).
3. Standard operating timezone is East Africa Time (EAT, UTC+3).
4. Minimum savings balance reserve is `UGX 5,000.00`.

---

## 27. Traceability Matrix

| HTML Source File | Feature / Subsystem | Entities Involved | Requirement IDs | Evidence & Traceability |
|---|---|---|---|---|
| `create_member_step_one.html` | Member Type Selection | Customer | FR-001 | Cards for Individuals, Group, Joint, Institution |
| `create_individual_member.html` | Individual Onboarding | Customer, Individual, Kin | FR-002 | Complete 8-section form, file uploads |
| `create_group.html` | Group Onboarding | Customer, Group | FR-004 | Group Name, Registration number |
| `create_joint.html` | Joint Onboarding | Customer, Joint | FR-005 | Joint Name, shared addresses |
| `create_institution.html` | Institution Onboarding | Customer, Institution | FR-006 | TIN, Business Type, Category |
| `list_members.html` | Member Directory | Customer | FR-001, FR-002 | Master table displaying members with phone, age |
| `list_groups.html` | Group Directory Tab | CustomerGroup | FR-004 | Groups filter subtab under /customers |
| `list_joints.html` | Joint Directory Tab | CustomerJoint | FR-005 | Joints filter subtab under /customers |
| `list_institutions.html` | Institution Directory Tab | CustomerInstitution | FR-006 | Institutions filter subtab under /customers |
| `list_member_applications.html` | Member Vetting Queue | Customer | FR-002, FR-003 | Pending / Rejected tabs, view application triggers |
| `list_agency_applications.html` | Agency Vetting Queue | AgencyApplication | FR-014 | Agent application pending / rejected tabs |
| `create_user.html` | User Provisioning | User, Subscriptions | FR-007 | Staff form, 9 Min/Max limit pairs, 22 alert checkboxes |
| `list_users.html` | User Administration | User | FR-008 | User cards, Deactivate, Delete, Enable SMS OTP |
| `create_role.html` | Role Definition | Role, RolePermission | FR-009 | Role name, Open/Close time, 382 checkboxes |
| `edit_role.html` | Role Modification | Role, RolePermission | FR-009 | Permission editing and operating hour adjustments |
| `user_details.html` | Role Profile View | Role, RolePermission | FR-009 | Edit role profile UI misnamed as user_details.html |
| `list_roles.html` | Role Administration | Role | FR-009, FR-010 | Role cards, active user counts, operating hours |
| `list_savings.html` | Savings Directory | SavingsAccount, Product | FR-015 | Savings accounts, FlexSave, balance cards |
| `saving_account_detail.html` | Savings Account Desk | SavingsAccount, Transaction | FR-016 | Actual/Available balances, Deposit, Withdraw, Freeze |
| `edit_saving_account.html` | Savings Account Alerts | SavingsSMSAlertConfig | FR-017 | 14 SMS transaction alert checkboxes |
| `list_savings_applications.html` | Savings Approvals | SavingsApplication | FR-015 | Pending, Approved, Rejected savings applications |
| `list_savings_account_applications.html` | Savings Vetting Queue | SavingsApplication | FR-015 | Maker-checker savings account approval queue |
| `list_fixed_deposits.html` | Fixed Deposits Directory | FixedDeposit | FR-019 | 7 status filter tabs, Terminate trigger |
| `create_fixed_deposit.html` | Fixed Deposit Placement | FixedDeposit, SavingsAccount | FR-018 | Fixing terms, Till vs Savings offset funding |
| `fixed_deposit_detail.html` | Fixed Deposit Certificate | FixedDeposit | FR-019 | Certificate terms, payout on maturity, Print |
| `list_fixed_deposit_applications.html` | Fixed Deposit Approvals | FixedDepositApplication | FR-018 | Fixed deposit application vetting queue |
| `list_standing_orders.html` | Standing Orders Directory | StandingOrder | FR-020 | Pending, Approved, Running, Expired tabs |
| `list_debit_card_issuance.html` | Debit Card Issuance | DebitCardIssuance | FR-021 | Pending, Approved, Issued card queue |
| `list_loans.html` | Loans Portfolio Directory | Loan, Product | FR-024 | Outstanding, Due, Performing, Legal tabs |
| `list_loan_applications.html` | Loan Applications Queue | LoanApplication | FR-022 | Approved, Pending, Rejected, Cancelled pipeline |
| `loan_detail.html` | Loan Servicing Terminal | Loan, RepaymentSchedule | FR-024, FR-025 | Repayment account, auto-pay, waivers, reschedule |
| `loan_calculator.html` | Amortization Calculator | LoanProduct, Schedule | FR-022 | Flat, declining, amortize schedule generator |
| `list_credit_reporting.html` | Credit Bureau Queue | CreditReportingInquiry | FR-023 | CBA credit reporting and verification |
| `acccounting_home.html` | Accounting Portal | GeneralLedger | FR-026 | Hub for COA, Journals, Budgets, Fixed Assets |
| `list_charts_of_accounts_assets.html` | COA Assets | ChartOfAccount | FR-026 | Asset accounts list, + Account, Deactivate |
| `list_charts_of_accounts_liabilities.html` | COA Liabilities | ChartOfAccount | FR-026 | Liabilities accounts list, deposit GLs |
| `list_charts_of_accounts_capital.html` | COA Capital | ChartOfAccount | FR-026 | Capital and equity accounts list |
| `list_charts_of_accounts_equity.html` | COA Income | ChartOfAccount | FR-026 | Revenue and income accounts list |
| `list_charts_of_accounts_expenses.html` | COA Expenses | ChartOfAccount | FR-026 | Operating expense accounts list |
| `create_chart_of_account_asset.html` | COA Node Creation | ChartOfAccount | FR-026 | Account Name, Subtype, Tags, Parent Node |
| `create_journal.html` | Journal Voucher Posting | JournalVoucher, Items | FR-027 | Contact person, Ref No, Method, Debit/Credit accounts |
| `journals.html.html` | Journals Register | JournalVoucher | FR-027 | Journal list, View more, Reverse actions |
| `approvals.html` | Financial Approvals | FinancialApproval | FR-029 | Maker-checker queue for financial transactions |
| `budgets.html` | Budget Management | Budget, Application | FR-029 | Annual budget monitoring and variance |
| `closing_and_restore_books.html` | Period Closing & TB PDF | FinancialPeriod | FR-028 | Period close, Download TB(pdf), Rollback |
| `list_fixed_assets.html` | Fixed Assets Register | FixedAsset | FR-030 | Physical asset list, depreciation tracking |
| `list_asset_profiles.html` | Asset Depreciation Profiles | AssetProfile | FR-030 | Depreciation methods and asset classes |
| `imports.html` | Batch Import Intake | DataImportBatch | FR-033 | 9 template types, file upload, branch mapping |
| `preview_import.html` | Import Template Preview | DataImportBatch | FR-033 | Schema validation and preview before ingest |
| `list_shares.html` | Shares Register | ShareAccount | FR-031 | Member shareholdings and equity balances |
| `share_dividends.html` | Dividend Payout Engine | DividendDeclaration | FR-032 | Payout to savings/shares, password confirmation |

---

## 28. Backend Implementation Considerations

1. **Transactional Ledger Integrity:** Implement strict double-entry ledger primitives. Every deposit, withdrawal, fee, disbursement, repayment, and interest accrual must post balanced debit and credit entries to the general ledger within an isolated database transaction.
2. **Dual-Balance Enforcement:** Centralized balance service preventing any withdrawal from breaching the `UGX 5,000.00` minimum reserve.
3. **Amortization Engine:** Deterministic schedule generation for `flat`, `declining`, and `amortize` calculation methods matching the calculator outputs.
4. **Automated Sweep Worker:** High-reliability cron daemon executing daily auto-pay collections from linked savings accounts into running loans.
5. **Period Lock Enforcement:** Middleware rejecting any financial transaction whose value date falls within a closed financial period.
6. **Password Verification Guard:** Sensitive distribution endpoints (dividends) must require explicit password re-verification.

---

## 29. Final System Summary

The reverse-engineering of all **51 HTML artifacts** establishes the complete, production-grade operational architecture of **SavingsPlus** for **NSIMBI SACCO LTD**:
- **CIF & Membership:** 4 member types (Individuals, Groups, Joints, Institutions) with maker-checker onboarding.
- **Deposit & Savings Engine:** Liquid savings (`FlexSave`), 14 SMS alert toggles, term/fixed deposits with 7 lifecycle states, and recurring standing orders.
- **Credit Lifecycle Engine:** Loan origination, amortization modeling, CRB credit vetting, auto-pay sweeps, interest/penalty waivers, restructuring, and legal recovery.
- **General Ledger & Double-Entry Accounting:** 5-tier Chart of Accounts, balanced Journal Vouchers with maker-checker reversals, period closing with immutable TB PDF snapshotting and rollback protection.
- **Shares & Equity:** Share capital administration and password-authenticated dividend distribution.
- **Data Migration & Ingestion:** Standardized batch import engine across 9 core banking entities.
- **Enterprise Governance:** 34 modules, 382 permissions, strict operating time windows, 5-minute idle timeouts, and individual financial limit ceilings across 9 transaction classes.

This master specification provides the complete, unambiguous guide for backend developers and automated coding agents to implement the core database models, business logic services, and REST API controllers for the NSIMBI Core Banking System.
