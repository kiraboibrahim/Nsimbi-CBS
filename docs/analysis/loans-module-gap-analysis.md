# Loans module gap analysis

Historical audit date: 15 October 2026. The audit below describes the pre-implementation tree; see the implementation follow-up for subsequent changes. Static analysis of the current working tree; AGENTS.md and SECURITY.md consulted. Initial git status was clean. Only this document was added; no implementation, branch changes, commits or tests executed. Targeted source and test inspection verifies code paths and assertions, not deployed configuration or successful runtime execution. Loandisk-like features below are references for discussion, not accepted business rules.

## Capability matrix

Existing = inspected API/code capability; configuration = product/tenant/role choices required; backend gap = missing integration or query surface in inspected paths; UI unverified = no Loans frontend identified in the inspected module structure (SECURITY.md also places the web UI in a separate repository). All UI behavior is unverified.

| Capability | Classification | Evidence / implication |
|---|---|---|
| Loan products, terms, accounting and allocation strategy | Existing + configuration; UI unverified | E1 exposes products/templates and accounting/strategy options. Select supported schedule and accounting combinations before building screens. |
| Application, modification, rejection, withdrawal | Existing; UI unverified | E2 creates/modifies applications and dispatches lifecycle commands; ordinary modification is documented for pending applications. |
| Approval, undo approval, approved-amount changes | Existing + configuration; monetary backend gap | E2 has approve/undo and approved-amount history; action permissions exist, but Nsimbi amount authority is not enrolled (E8). |
| Disbursement, savings transfer, tranche undo | Existing + configuration; monetary backend gap | E2 dispatches disburse/disburseToSavings/undo commands. Do not infer payment-rail integration. |
| Repayment, allocation, adjustment/reversal | Existing + configuration; UI unverified | E3 provides repayment and adjustment; adjustment reverses and replaces. E10 tests reverse/replay and allocation requirements. |
| Charges, penalty calculation, waiver | Existing + configuration; UI unverified | E4 supplies loan-charge endpoints; E5 selects overdue schedule charges with business-date/grace/backdating conditions. |
| Schedule calculation and retrieval | Existing + configuration; UI unverified | E2 calculateLoanSchedule and schedule associations; E5 maps installment dates/components. |
| Rescheduling | Existing + configuration; UI unverified | E6 creates requests and approves/rejects them; E10 checks status and changed schedule. Keep distinct from closing as rescheduled. |
| Early settlement and closure | Existing + configuration; UI unverified | E3 prepayLoan template, foreclosure and close commands. A quote is not a completed settlement; product-dependent interest must be reconciled. |
| Write-off, undo, recovery, charge-off | Existing + configuration; UI unverified | E3 has separate commands. Accounting charge-off and lifecycle written-off closure must not be conflated. |
| Guarantors and collateral | Existing; workflow fit unverified | E7 exposes guarantors, account templates, legacy collateral and collateral-management APIs. Valuation, releases and guarantee recovery rules need acceptance criteria. |
| Loan documents and audit history | Existing; access completeness unverified | E7 document attachment APIs; E9 audits and E5 lifecycle actors/dates. Required-document gates and immutable history are not established. |
| Branch access and action permissions | Existing; full isolation unverified | E5 list/detail queries constrain office hierarchy including transfer destination; E8 checks task permissions. This does not prove isolation for every subresource/write. |
| Maker-checker | Existing + configuration | E8 task-level enablement, checker permissions and conditional same-user prohibition. Loan approval is a separate business action. |
| Basic listing/filtering | Existing; additional backend query gap | E2 accepts accountNo, externalId, clientId, numeric lifecycle status, pagination and ordering. It does not expose product, office, officer, due-date or aging filters. |
| Collections sheet | Existing + configuration; broader views gap | E11 generates/saves individual collection sheets; not evidence of a complete collections dashboard. |
| Due / missed / arrears / no repayments / past maturity / principal outstanding / 30–90 aging | Existing underlying data; backend query/report fit unverified or gap | See collection analysis below. No verified single standard list API implements these cohorts. |

## Evidence map

Paths below are repository-relative; Java paths use these roots:

- **P**: `fineract-provider/src/main/java/org/apache/fineract/`
- **L**: `fineract-loan/src/main/java/org/apache/fineract/`
- **C**: `fineract-core/src/main/java/org/apache/fineract/`
- **D**: `fineract-document/src/main/java/org/apache/fineract/`

| Ref | Relevant files, APIs and permissions |
|---|---|
| E1 | P`portfolio/loanproduct/api/LoanProductsApiResource.java`: `/v1/loanproducts`, templates, product creation; transactionProcessingStrategyCode and accountingRule inputs/options. |
| E2 | P`portfolio/loanaccount/api/LoansApiResource.java`: `/v1/loans`, `/{id}`, templates, `?command=calculateLoanSchedule`, lifecycle commands, `/{id}/approved-amount`. Listing at lines 492–515; state dispatch around 1351. Reads require `READ_LOAN`. |
| E3 | P`portfolio/loanaccount/api/LoanTransactionsApiResource.java`: `/v1/loans/{id}/transactions`, `/template?command=prepayLoan`, transaction adjustment by ID; dispatch around 577–625. Commands include repayment, writeoff, undowriteoff, recoverypayment, close, close-rescheduled, foreclosure and charge-off. |
| E4 | P`portfolio/loanaccount/api/LoanChargesApiResource.java`: `/v1/loans/{id}/charges`, templates and individual charges. |
| E5 | P`portfolio/loanaccount/service/LoanReadPlatformServiceImpl.java`: hierarchy-scoped detail around 208–220 and list around 327–405; summary/schedule/transaction mapping, overdue-charge selection around 1573. L`portfolio/loanaccount/service/LoanArrearsAgingServiceImpl.java` registers transaction/balance event listeners and maintains aging data; P`cob/loan/UpdateLoanArrearsAgingBusinessStep.java` and L`portfolio/loanaccount/jobs/updateloanarrearsageing/` provide batch paths. |
| E6 | L`portfolio/loanaccount/rescheduleloan/api/RescheduleLoansApiResource.java`: `/v1/rescheduleloans`, request ID, approve/reject and read permission checks. |
| E7 | P`portfolio/loanaccount/guarantor/api/GuarantorsApiResource.java`: `/v1/loans/{loanId}/guarantors`, `READ_GUARANTOR`, create/update/delete command flow. P`portfolio/collateral/api/CollateralsApiResource.java`; P`portfolio/collateralmanagement/api/{CollateralManagementApiResource,ClientCollateralManagementApiResource,LoanCollateralManagementApiResource}.java` (loan management base `/v1/loan-collateral-management`). D`infrastructure/documentmanagement/api/DocumentApiResource.java`: `/v1/loans/{id}/documents`, metadata, upload/update/delete/download; corresponding `service/DocumentReadPlatformServiceImpl.java`. |
| E8 | C`commands/service/{PortfolioCommandSourceWritePlatformServiceImpl,SynchronousCommandProcessingService}.java`: task permission checks and maker-checker enablement; checker validation around 129–146. C`useradministration/domain/AppUser.java`: action/checker validation. Permission seed evidence: `fineract-db/old-schema-files/0003-mifosx-permissions-and-authorisation-utf8.sql` contains `APPROVE_LOAN`, `DISBURSE_LOAN`, `REPAYMENT_LOAN`, `WRITEOFF_LOAN` and `_CHECKER` variants; historical seed is not proof of tenant grants. P`nsimbi/userroles/service/NsimbiMonetaryAuthorityPolicyService.java` and `domain/MonetaryAuthorityType.java`: LOAN_APPROVAL/DISBURSEMENT authority types, policy explicitly unenrolled in transaction handlers; no integration found in targeted loan/command searches. |
| E9 | P`commands/api/AuditsApiResource.java`: `/v1/audits`, individual entries/search template, `READ_AUDIT`. C`portfolio/loanaccount/domain/LoanStatus.java` defines lifecycle statuses. |
| E10 | `integration-tests/src/test/java/org/apache/fineract/integrationtests/`: `LoanApplicationApprovalTest.java` checks approval amounts and rejects advanced allocation without product configuration; `LoanTransactionReverseReplayTest.java` covers additional installments/charges; `LoanRescheduleRequestTest.java` checks rejection/approval and schedule amounts; `LoanPrepayAmountTest.java` compares prepayment interest under progressive/recalculation variants; `LoanWriteOffWithAdvancedPaymentAllocationTest.java` includes rejecting repayment undo after write-off; `LoanAccountArrearsAgeingCOBBusinessStepTest.java` asserts overdue date and principal/total overdue; `LoanAuditingIntegrationTest.java` checks creator/modifier timestamps/users; `UserLoanPermissionTest.java` checks 403 before grant and success after grant for selected newer actions, including `UPDATE_APPROVED_AMOUNT_LOAN`. P test counterpart `fineract-provider/src/test/java/org/apache/fineract/nsimbi/userroles/service/NsimbiMonetaryAuthorityPolicyServiceTest.java` only verifies missing authority denial for TRANSFER; it does not establish loan enforcement. |
| E11 | P`portfolio/collectionsheet/api/CollectionSheetApiResource.java` and `service/CollectionSheetReadPlatformServiceImpl.java`: `/v1/collectionsheet` generation requires collection-sheet read permission; saving uses command processing. `fineract-db/old-schema-files/0004-mifosx-core-reports-utf8.sql` contains legacy reporting definitions; current tenant availability, SQL portability, parameters and access enforcement were not verified. |

## Collection views: data exists, cohort contracts remain open

Verified E5 data includes installment due dates/completion, component outstanding amounts, principal/total outstanding, expected/actual maturity, transaction type/reversal markers, overdue component totals and overdue-since date from `m_loan_arrears_aging`. E9 lifecycle statuses include pending, approved, active, rejected, withdrawn, closed variants and overpaid. **Due, overdue, arrears and aging buckets are derived collection classifications, not lifecycle statuses.**

| View | Reuse candidate and unresolved definition |
|---|---|
| Due | Schedule due date plus remaining payable amount; decide today versus date range and whether earlier unpaid installments join the cohort. |
| Missed payments | Unpaid/part-paid installments before business date; decide grace, tolerance and whether counting installments or borrowers. |
| Arrears | Existing overdue component totals/date and delinquency features; validate freshness, grace and product behavior. |
| No repayments | Transaction predicate excluding reversed transactions; define repayment types, refunds, down payments, recoveries, waivers and disbursement eligibility. Principal repaid alone is insufficient. |
| Past maturity | Expected maturity before business date with an agreed outstanding threshold; define active/overpaid/written-off inclusion and rescheduled maturity. |
| Principal outstanding | Existing principalOutstanding summary; do not substitute totalOutstanding, which includes other components. Filter/sort and portfolio totals are not exposed as dedicated standard list predicates. |
| 30/90-day aging | Candidate business-date difference from overdueSinceDate or existing delinquency ranges; agree inclusive boundaries, 30+/90+ versus exclusive buckets, grace effects, and whether reporting overdue principal or the entire outstanding exposure. |

Prefer existing authorized reports/delinquency configuration after validating them in the target tenant. If inadequate, add a narrowly scoped server-side read query using Fineract data. Fetching every loan and filtering client-side would undermine pagination, totals and operational scale. Office filtering exists inside E5 search handling but the inspected E2 list resource does not pass an officeId parameter; hierarchy scope is independently applied.

## Critical security and accounting concerns

1. **High-priority verified integration gap: monetary authority.** Stored Nsimbi limits and the reusable policy do not establish enforcement on loan approval/disbursement. Product maximums and RBAC are different controls. Confirm whether person/currency/amount limits are required; if so enforce on every applicable execution path, including checker execution, amount changes, tranches, bulk and savings disbursement. This is a workflow-control gap, not a demonstrated upstream vulnerability.
2. **Maker-checker requires configuration review.** Same-user prevention applies only when same-maker-checker is disabled and the checker lacks checker-superuser privilege (E8). Enabling loan approval alone does not guarantee independent command authorization. Tenant role grants/configuration remain unverified.
3. **Branch isolation needs targeted negative verification.** Main reads are hierarchy-scoped, including transfer destination. Supporting document reads inspected directly query parent identifiers without visible local branch checks; guarantor/collateral and audit access require end-to-end review too. Global/interceptor checks were not traced fully, so no exploit or confirmed bypass is claimed. Verify cross-branch IDs and external IDs before exposing attachments/history or new reports.
4. **Accounting depends on configuration and transaction semantics.** Use existing allocation, reversal/replay, journal posting and settlement logic; never implement balance edits in a UI. P`portfolio/loanaccount/service/LoanJournalEntryPosterImpl.java` bridges transactions to accounting. Reconcile cash/accrual GL mappings, backdated transactions, penalties, write-off versus charge-off, recoveries and partial-payment allocation for selected products. Test presence does not prove all combinations.
5. **Collection freshness and history.** Aging has event and COB paths; deployed business steps, business date and successful processing are unverified. Exports must identify their as-of basis. SECURITY.md explicitly disclaims audit tamper-resistance; immutable regulatory history is an additional requirement if desired, not a repository security defect.

## Business questions

- Which products, individual/group loans, currencies, schedules, interest methods and accounting modes are in the first release?
- What does each collection view mean, including date boundaries, grace, partial payments, rescheduling and written-off loans? Is aging based on oldest unpaid installment, contractual delinquency or another measure?
- What branch/sub-branch and transfer access should officers, managers and collectors have? Is assignment a restriction or only a filter?
- Which actions need independent checking, and are same-user/superuser exceptions permitted? What approval/disbursement limits apply per currency, tranche, aggregate exposure and checker?
- What allocation order, reversal/backdating policy, penalty/waiver rules and settlement quotation validity are required?
- Which guarantor/collateral/document requirements block approval/disbursement, and what release, valuation, consent and retention rules apply?
- Is the frontend in another repository? Which current tenant reports, delinquency ranges, permissions and COB steps are actually enabled?

## Three prioritized implementation slices

1. **Smallest useful first slice: read-only loan register and loan detail.** Reuse paginated account/client/status lookup, summary, schedule and transaction APIs; show lifecycle status separately from overdue information and principal separately from total outstanding. Establish branch/permission negative checks and a visible business-date basis. Acceptance: scoped list/detail and reconciled sample balances, with no new lending action. If an external frontend already covers this, narrow this slice to verified missing read views.
2. **Controlled origination and servicing.** Configure a small approved product set; reuse application/approval/disbursement/repayment APIs, templates and maker-checker. Integrate Nsimbi monetary authority before enabling affected monetary actions; verify checker, alternate routes and amount changes. Add required guarantor/collateral/document workflow only after rules are agreed. Acceptance: denied unauthorized/cross-branch/over-limit actions and balanced accounting for selected journeys.
3. **Collection cohorts and exception workflows.** Validate existing reports/delinquency first, then add only missing server-side cohort filters, totals and exports for agreed due/missed/arrears/no-repayment/maturity/30–90 definitions. Reuse settlement, reschedule, waiver, reversal and write-off commands with configured authority. Acceptance: boundary-date and partial/reversed-payment fixtures reconcile to schedules, aging and GL; all exports preserve branch scope.

No runtime checks were necessary to establish these static findings. Runtime behavior, frontend completeness and tenant configuration remain explicitly unverified. Audit stops here.


## Monetary-authority implementation follow-up — 20 October 2026

The working tree now enrolls loan approval and disbursement in the existing Nsimbi currency-specific monetary-authority policy. This supersedes the monetary-integration gap recorded in the historical audit above.

- Command execution supplies the persisted original maker, including checker execution; the checker cannot substitute their monetary limit for the maker's. Existing checker permissions and maker-checker configuration still apply.
- Approval checks the requested approved amount, or the existing approved principal when the optional amount is omitted. Approved-amount changes and available-disbursement changes check the resulting total approved principal before mutation.
- Standard, savings-transfer, no-auto-payment and GLIM disbursement routes check the effective gross disbursement amount calculated by existing tranche logic, before posting. GLIM applies the policy to each child loan; this does not introduce a group-wide or cumulative exposure limit.
- The maker must have access through the loan office hierarchy and a configured authority for the loan currency. Missing authority or command maker is denied. Operators must configure LOAN_APPROVAL and DISBURSEMENT authority records before using these actions.
- No loan-specific checker monetary limit, group aggregate limit, frontend, collection-cohort query or tenant configuration is introduced by this change.

Regression coverage includes currency/office/amount denial, missing maker/context, original-maker propagation during checker execution, and approved-amount denial before loan mutation, history persistence or event publication. Validation: Java compilation succeeded and all 13 targeted unit tests passed (3 approved-amount tests, 6 monetary-authority tests, and 4 command-source tests). Spotless was applied to the affected Java modules. `git diff --check` passed. Database-backed end-to-end lending journeys and deployed tenant configuration were not exercised.


## PR review follow-up — 21 October 2026

Review of the changed enforcement paths found and fixed two defects: the disbursement check originally followed payment-detail persistence and a pre-business event, and collection-sheet bulk disbursement bypassed it. Disbursement now performs a read-only gross-amount check before mutations, payment writes or events, and retains the exact adjusted-amount check before transfers and posting. GLIM preflights every child. Both collection-sheet handlers carry the persisted maker through the loan command interface and preflight their disbursements before collection-sheet payments or repayments.

The existing policy remains unchanged: a missing person/type/currency record, or a record with neither amount bound configured, denies authority. Configure tenant LOAN_APPROVAL and DISBURSEMENT limits per maker and loan currency before enabling these operations. No tenant records were changed.

The original 13 passing targeted tests above describe the pre-review run. The affected post-review validation result is pending. Database-backed end-to-end tests remain unverified.


## PR preparation completed — 23 October 2026

Branch: `feat/loans-monetary-authority`; PR base: `dev`. Resumed the existing working tree without replacing edits. Review was limited to changed code/tests and directly relevant command, monetary-policy and disbursement framework methods. No additional concrete defect was found; the two fixes documented on 21 October remain in place. This session changed only this handoff document.

- **Ordering:** gross-amount preflight precedes loan mutations, payment-detail persistence and business events. The adjusted-amount check remains before transfers/posting. GLIM disbursement preflights all children; collection-sheet preflight precedes payments and repayments.
- **Amounts/routes:** approval uses the requested amount or existing approved principal; both amount-change endpoints check the resulting total approved principal. Standard, savings, no-auto-down-payment, GLIM and both collection-sheet routes carry authority context. Read-only preflight matches `adjustDisburseAmount` for explicit amounts, earliest-date undisbursed tranches, principal fallback and artificial tranches. Limits are per loan/disbursement, not aggregate group exposure.
- **Maker-checker:** `CommandSourceService` supplies the persisted maker, including checker execution. `PortfolioCommandSourceWritePlatformServiceImpl.validateMakerCheckerTransaction` still checks checker permission and the configured same-user/superuser rules; command processing still applies task-level maker-checker enablement. Checker authority does not replace maker authority. Legacy internal approval/disbursement overloads check the authenticated caller; enrolled command handlers always pass persisted maker context.
- **Missing configuration:** existing `NsimbiMonetaryAuthorityPolicyService.allows` denies missing records or required inputs. `NsimbiUserMonetaryAuthority.allows` denies records with neither bound set; configured bounds are inclusive. This PR does not change that policy.

### Validation and remaining verification

Prior evidence retained: Java compilation and the original 13 targeted tests passed (3 approved-amount, 6 authority, 4 command-source tests); unchanged approved-amount and command-source tests were not rerun. Earlier Spotless evidence is retained. Post-review validation passed: 17 tests (11 monetary-authority, 4 disbursement-ordering cases, 2 collection-sheet handler cases), zero failures/errors/skips; Gradle BUILD SUCCESSFUL. Main/test compilation tasks were up to date. `git diff --check` passed. Local run log: `/tmp/loans-pr-review-tests.log`; XML evidence: `fineract-provider/build/test-results/test/TEST-*.xml`. The 17-test run includes the original six authority cases; it is not 17 additional distinct tests on top of the original 13.

Targeted command used in this session:

```sh
./gradlew :fineract-provider:test --tests '*LoanMonetaryAuthorityServiceTest' --tests '*LoanDisbursementAuthorityOrderingTest' --tests '*CollectionSheetLoanAuthorityTest' --console=plain --max-workers=2
```

Before rollout, operators must configure tenant `LOAN_APPROVAL` and `DISBURSEMENT` records for each original maker and loan currency, with at least one bound (set a maximum to enforce a ceiling), appropriate office hierarchy access, action/checker permissions and agreed maker-checker settings. No limits were configured here.

Database-backed end-to-end tests remain unverified: allowed/denied approval and both amount changes; currency/boundary/missing-limit cases; queued commands checked by a different user after maker-limit changes; standard/savings/no-auto-payment/tranche/GLIM/collection-sheet disbursements; mixed-batch denial rollback with no payment, transaction, transfer, journal or event leakage; successful accounting reconciliation. Unit mocks do not establish transaction rollback or deployed Spring/tenant behavior.

**Next remaining Loans task:** run those monetary-authority acceptance journeys in an isolated test tenant, then verify branch-scoped read-only loan register/detail and cross-branch negative access as the next feature slice. Neither task was started here. Do not repeat the completed static authority review unless this diff changes.

### Exact staging and commit commands (prepared only; not executed)

The following explicit 31-file list includes the handoff and all reviewed implementation/test changes. It excludes future unrelated work; inspect the staged diff before committing.

```sh
git add -- \
  docs/analysis/loans-module-gap-analysis.md \
  fineract-core/src/main/java/org/apache/fineract/commands/domain/LoanCommandExecutionContext.java \
  fineract-core/src/main/java/org/apache/fineract/commands/handler/LoanCommandHandler.java \
  fineract-core/src/main/java/org/apache/fineract/commands/service/CommandSourceService.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/handler/DisburseLoanCommandHandler.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/handler/DisburseLoanToSavingsCommandHandler.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/handler/DisburseLoanWithoutAutoPaymentHandler.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/handler/GLIMLoanApplicationApprovalCommandHandler.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/handler/GlimLoanApplicationDisburseCommandHandler.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/handler/LoanApplicationApprovalCommandHandler.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/handler/LoanApprovedAmountModificationCommandHandler.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/handler/LoanAvailableDisbursementAmountModificationCommandHandler.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanApplicationWritePlatformService.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanApprovalAuthority.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanApprovedAmountWritePlatformService.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanApprovedAmountWritePlatformServiceImpl.java \
  fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanWritePlatformService.java \
  fineract-loan/src/test/java/org/apache/fineract/portfolio/loanaccount/service/LoanApprovedAmountAuthorityTest.java \
  fineract-provider/src/main/java/org/apache/fineract/portfolio/collectionsheet/handler/SaveIndividualCollectionSheetCommandHandler.java \
  fineract-provider/src/main/java/org/apache/fineract/portfolio/collectionsheet/handler/UpdateCollectionSheetCommandHandler.java \
  fineract-provider/src/main/java/org/apache/fineract/portfolio/collectionsheet/service/CollectionSheetWritePlatformService.java \
  fineract-provider/src/main/java/org/apache/fineract/portfolio/collectionsheet/service/CollectionSheetWritePlatformServiceJpaRepositoryImpl.java \
  fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanApplicationWritePlatformServiceJpaRepositoryImpl.java \
  fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanMonetaryAuthorityService.java \
  fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanWritePlatformServiceJpaRepositoryImpl.java \
  fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/starter/LoanAccountConfiguration.java \
  fineract-provider/src/test/java/org/apache/fineract/commands/service/CommandSourceServiceTest.java \
  fineract-provider/src/test/java/org/apache/fineract/portfolio/collectionsheet/service/CollectionSheetLoanAuthorityTest.java \
  fineract-provider/src/test/java/org/apache/fineract/portfolio/loanaccount/service/LoanDisbursementAuthorityOrderingTest.java \
  fineract-provider/src/test/java/org/apache/fineract/portfolio/loanaccount/service/LoanMonetaryAuthorityServiceTest.java \
  fineract-provider/src/test/java/org/apache/fineract/portfolio/loanaccount/service/LoanWritePlatformServiceJpaRepositoryImplTest.java
git diff --cached --check
git diff --cached --stat
git diff --cached
git commit -m "feat(loans): enforce maker monetary authority"
```

### Prepared PR

Title: `feat(loans): enforce maker monetary authority`

Base: `dev`; head: `feat/loans-monetary-authority`.

Description: Enforce the original maker's currency-specific approval and disbursement authority before financial side effects, covering approved/available amount changes, alternate disbursement routes, GLIM and collection sheets. Preserve existing maker-checker rules and fail closed under the existing missing-limit policy. Include regression coverage for maker propagation, amount selection and denial ordering. Validation: retain the original 13-test evidence and the affected-test result above. Requires tenant authority configuration; database-backed acceptance and accounting reconciliation remain unverified.

No staging, commit, push, tenant configuration or live financial transaction was performed. PR preparation stops here.
