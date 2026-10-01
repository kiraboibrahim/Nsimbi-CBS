# Nsimbi CBS Savings Module — Software Requirements Specification

## 1. Document control

| Attribute | Value |
|---|---|
| Document | SAV-SRS, revision 1.0, 2026-10-01 |
| Status | Implementation-grounded specification for review; not a production-readiness certificate |
| Authoritative baseline | `dev` / `origin/dev`, `93804b7cbc11aa6fb1da8c6b1cf0dc928430a906` after fetch and fast-forward verification |
| Documentation branch | `docs/savings-module-srs` |
| Audience | Developers, QA, product owners, auditors, CBS operations and release approvers |
| Purpose | Define observable savings requirements, authorization boundaries, evidence and outstanding release decisions |
| Scope | Ordinary savings, fixed deposits (FD), recurring deposits (RD), applicable grouped savings (GSIM), savings-side transfers, charges, interest, tax and accounting |
| Exclusions | Loan-product specification, mandate redesign, deferred alternative-transfer enforcement, new FX design and scheduler redesign |

**Interpretation.** “Shall” denotes a normative requirement. Requirements marked **Current** specify implemented behavior at the baseline; this does not imply complete integration validation. **Proposed** requirements describe acceptance or operational obligations not established as delivered. **Decision** entries require product agreement before implementation. The traceability and readiness matrices qualify every capability.

Evidence precedence is baseline implementation and tests, followed by the latest section of each change report. Archived stop conditions and exclusions are historical: core TRANSFER and manual FD/RD closure authority are now implemented. Earlier withdrawal-report statements that all transfers remain a backlog do not describe this baseline.

Primary reports: [annual fees](../changes/savings-annual-fee-command.md), [deposit authority](../changes/savings-deposit-authority.md), [withdrawal authority](../changes/savings-withdrawal-authority.md), [transfer and closure authority](../changes/account-transfer-authority.md). Repository API documentation: [legacy API reference](../../fineract-provider/src/main/resources/static/legacy-docs/apiLive.htm), supplemented by the API classes in §14. Integration infrastructure: [README](../../integration-tests/README.md) and [integration guide](../../fineract-doc/src/docs/en/chapters/testing/integration.adoc). Security assumptions follow [SECURITY.md](../../SECURITY.md).

**Validation record.** The prior read-only stabilization run on this exact commit passed **404 tests: 67 savings and 337 provider, zero failures/errors/skips**. It covered the merged status, annual-fee, deposit/withdrawal, core transfer, standing-instruction, closure and calculation work. It was not a live PostgreSQL integration run. This SRS cites that session result, not a new execution or a checked-in test-results artifact. No Gradle suite was rerun to prepare this document. Existing integration-test source is evidence of available scenarios, not evidence that those scenarios passed at this baseline.

The later database preflight found no running Fineract backend, no cached Fineract image and missing test-stack images, with another application's database occupying port 5432 and limited free memory. These are environment observations at preflight, not permanent product restrictions. No database-backed closure tests were executed.

### Terminology

| Term | Meaning |
|---|---|
| Maker / checker | Original submitting staff user / user approving a pending command |
| Monetary authority | Current user/type/currency amount bounds, additional to operation permissions |
| Principal | Full requested transfer amount; for FD/RD closure transfer, exact planned net proceeds |
| Trusted origin | Server-assigned, persisted command provenance; not a client header, role or request field |
| DUES | Variable-amount standing-instruction type; distinct from FIXED with a dues-based recurrence |
| Closure plan | Immutable current-state interest/tax/correction/net-proceeds result authorized before application |
| Replay / retry | Return of an already completed command / another execution attempt requiring current authorization |
| Business date | Configured processing date; not interchangeable with wall-clock date |
| GSIM | Group savings workflow; child-account behavior must be distinguished from ordinary individual savings |

## 2. Module overview and lifecycle

Ordinary savings holds customer balances and supports permitted transactions, charges and interest. FD and RD use deposit terms, maturity and premature-closure rules; RD additionally supports recurring deposits. GSIM exposes grouped workflows and child accounts; ordinary deposit monetary-authority coverage must not be generalized to every GSIM operation. Sources: [E-P](#e-p), [E-A](#e-a), [E-F](#e-f), [E-D](#e-d), [E-W](#e-w).

The lifecycle includes application, approval, activation, rejection or applicant withdrawal, closure, and FD/RD maturity or premature closure. Actions shall satisfy the account's current state, chronology and product rules; this is not an unrestricted transition graph. Account listing recognizes status IDs 100 pending, 200 approved, 300 active, 303 transfer in progress, 304 transfer on hold, 400 withdrawn, 500 rejected, 600 closed, 700 prematurely closed and 800 matured. Recognition in the filter does not make every status reachable for every account type. Sources: [E-A](#e-a), [E-L](#e-l), [E-F](#e-f).

## 3. Actors and authorization

| Actor | Responsibility and boundary |
|---|---|
| Customer/member | Economic owner and consent subject; staff authorization does not establish customer consent. Direct customer internet access is outside the repository threat model. |
| Teller/cashier | Uses permitted staff APIs/batch; no monetary-authority exemption from cashier status. |
| Maker | Supplies business instruction; persisted identity is the monetary-authority subject on approval/retry. |
| Checker | Supplies approval permission and audit identity; cannot substitute a larger monetary limit for the maker. |
| Supervisor/administrator | Configures products, permissions and limits under existing controls; role alone is not trusted provenance. |
| Scheduler | Executes configured jobs and due instructions under existing scheduling rules; not a substitute maker. |
| Spreadsheet importer | Propagates submitting identity and trusted import provenance; exemption differs between deposits and withdrawals. |
| System/internal service | Performs existing interest, charge, reversal and settlement work; an internal method call alone does not establish an exemption for discretionary staff transfers. |

Evidence: [E-D](#e-d), [E-W](#e-w), [E-X](#e-x), [E-S](#e-s), [E-CMD](#e-cmd), [E-IMP](#e-imp), [security model](../../SECURITY.md).

## 4. Functional requirements

All rows are **Current** unless explicitly marked otherwise. Evidence keys resolve to exact classes and tests in §14.

| ID | Normative requirement | Evidence |
|---|---|---|
| SAV-PROD-001 | The system shall validate product currency, precision, interest configuration, balance/overdraft options and applicable accounting mappings before accepting product configuration. | E-P |
| SAV-ACC-001 | The system shall validate applications and enforce permitted approval, rejection, withdrawal, activation and closure transitions, dates and account eligibility. | E-A, E-T, E-F |
| SAV-ACC-002 | Grouped closure shall propagate the original maker to each child-account withdrawal check; GSIM operations shall retain their distinct command classification. | E-W, E-ENV |
| SAV-TXN-001 | Ordinary API/batch deposits shall require current maker DEPOSITS authority on the full requested amount in account currency before financial writes. | E-D |
| SAV-TXN-002 | Ordinary, forced and overdraft withdrawals shall require current maker WITHDRAWALS authority on the full amount; force withdrawal shall additionally retain its operation permission. | E-W |
| SAV-TXN-003 | Withdrawal adjustment shall authorize the full replacement amount before undo/repost; ordinary savings closure paying a positive balance shall authorize that balance. | E-W, E-T |
| SAV-TXN-004 | Core account transfers and active-loan refunds by transfer shall require TRANSFER authority on source-currency principal. Transfer legs shall not additionally invoke DEPOSITS/WITHDRAWALS or DIRECT_DEBIT/DIRECT_CREDIT solely because they are transfer legs. | E-X |
| SAV-TXN-005 | Transactions shall preserve existing account blocks, holds, available-balance and overdraft checks; monetary authority shall not waive financial eligibility. | E-T, E-I, E-C |
| SAV-INT-001 | Interest execution shall use configured rate, compounding, posting, day-count, currency precision and transaction ordering; pure previews shall use shared calculation primitives without mutating account/transaction state. | E-I, E-F |
| SAV-INT-002 | Applicable withholding tax shall retain configured components, rounding and correction behavior; closure transfer shall use net proceeds after planned tax. | E-C, E-F |
| SAV-FEE-001 | Charge payment shall remain tied to its account obligation and outstanding amount; the charge route shall not accept an arbitrary transfer beneficiary as a withdrawal substitute. | E-W |
| SAV-FEE-002 | Manual `applyAnnualFees` shall reject as unsupported; scheduler charge collection shall continue through existing charge/account operations. | E-FEE, E-JOB |
| SAV-SI-001 | FIXED standing-instruction creation and material updates shall authorize the maker's current TRANSFER bounds before setup writes or approval queuing. | E-S |
| SAV-SI-002 | New DUES, cloning, material modification, reactivation and conversion in either direction shall reject. Existing DUES shall permit status-only suspension and deletion, and unchanged scheduled execution. | E-S |
| SAV-SI-003 | Scheduled execution shall use existing due-selection and fixed/dues amount rules; no caller-selected command-envelope origin shall create a scheduler exemption. | E-S, E-JOB, E-ENV |
| SAV-FDRD-001 | Manual normal and premature FD/RD transfer closure shall compute immutable interest corrections, tax allocations and exact net proceeds before payment detail or financial mutation. | E-C, E-F |
| SAV-FDRD-002 | The original maker shall authorize that exact net amount in source currency; execution shall apply the authorized plan without independently recalculating interest/tax during posting or source withdrawal. | E-C |
| SAV-FDRD-003 | Approval/retry shall plan from current locked source state; stale plans shall reject. Closure without an account transfer shall not invoke TRANSFER. | E-C, E-CMD |
| SAV-FDRD-004 | Maturity and premature-closure behavior shall preserve existing FD/RD term, rate and tax rules. Scheduled maturity shall retain its separate existing path. | E-F, E-C |
| SAV-AUTH-001 | Operation permission, monetary authority and configured maker-checker approval shall remain distinct gates; the checker shall retain audit identity. | E-CMD, E-POL |
| SAV-AUTH-002 | Enrolled execution shall reject absent/malformed provenance and handler/kind mismatch; client-supplied reserved metadata shall not establish trust. | E-ENV, E-X, E-C |
| SAV-AUD-001 | The system shall retain original maker, command state and business payload; audit presentation shall unwrap trusted envelopes without granting execution trust. | E-CMD, E-D, E-ENV |
| SAV-REP-001 | Listing shall validate the optional numeric status filter and preserve visibility, external-ID, pagination and ordering behavior. Omitting status shall include all visible statuses. | E-L |
| SAV-REP-002 | Authorized users shall retrieve supported account/transaction details; report/export outputs shall reflect their selected filters and access boundaries. Custom financial report correctness is not certified here. | E-A, E-T |
| SAV-IMP-001 | Ordinary batch shall retain staff authorization. Spreadsheet deposits alone shall use their explicit DEPOSITS exemption; spreadsheet withdrawals shall authorize each row using the original importer. | E-D, E-W, E-IMP |
| SAV-ACCNT-001 | Successful financial operations shall retain configured accounting mappings and transaction-linked journal creation. **Proposed release acceptance:** persisted debits shall equal credits and each effect shall occur once. | E-J, E-C, E-F |
| SAV-JOB-001 | Jobs shall retain existing charge collection, interest and scheduled execution boundaries; manual job permission shall not prove customer mandate or occurrence idempotency. | E-JOB, E-S, E-FEE |

## 5. Business rules

| ID | Rule | Evidence |
|---|---|---|
| SAV-BR-001 | Current: minimum/maximum comparisons shall be inclusive. One null bound is open-ended; both null, missing authority, invalid inputs or contradictory persisted bounds shall deny. | E-POL |
| SAV-BR-002 | Current: currency shall come from the source account, not caller-supplied display currency. Separately calculated fees shall remain outside transfer principal. No new FX conversion rule is specified. | E-X, E-C |
| SAV-BR-003 | Current: actual execution retries and approvals shall recheck current original-maker authority using persisted business payload. Completed replay shall not execute another movement. | E-CMD, E-C |
| SAV-BR-004 | Current: authorization denial shall precede protected payment/transaction/accounting writes and AWAITING_APPROVAL. Initial command audit/error persistence and generic error hooks may still occur. | E-CMD, E-D, E-W, E-C |
| SAV-BR-005 | Current: affected legacy pending commands without trusted metadata shall require cancellation/resubmission. Completed flat history shall remain readable. | E-ENV, E-D, E-X |
| SAV-BR-006 | Current: deposit version 1 `{version,origin}` and savings/transfer version 2 `{version,origin,kind}` metadata shall remain compatible; validators shall receive flat business payloads. | E-ENV |
| SAV-BR-007 | Current: fixed-instruction updates shall authorize proposed or retained principal, including priority, schedule and activation changes. Unsupported source/destination update fields shall remain rejected. | E-S |
| SAV-BR-008 | Current: existing DUES execution shall remain grandfathered and uncapped; new setup shall not infer a ceiling from today's dues or a supplied amount. Staff authority shall not be represented as consent. | E-S |
| SAV-BR-009 | Current: closure plan application shall validate current state/version. The source lock and immutable application receipt shall preserve the authorized source amount and date. Live contention behavior remains unvalidated. | E-C |
| SAV-BR-010 | Current: lifecycle/date/account constraints shall still apply after monetary approval. Reversal of an existing transfer shall remain distinct from a newly submitted discretionary transfer. | E-T, E-X, E-F |

## 6. API requirements

Paths are relative to the deployment's `/fineract-provider/api/v1` context. Numeric IDs are representative; supported external-ID variants retain the same policies. “Configured MC” means the generic command's maker-checker setting, not mandatory approval for every deployment. Permission names below identify the operation; configured super-permissions remain governed by the existing permission service. Sources: E-A, E-T, E-X, E-S, E-API, E-CMD and E-FEE.

| Method / endpoint or command | Purpose | Permission | Maker-checker | Monetary policy / important behavior |
|---|---|---|---|---|
| GET `/savingsaccounts?status=&offset=&limit=` | List accounts | READ_SAVINGSACCOUNT | No | Valid numeric status only; malformed/unsupported status validation error |
| GET `/savingsaccounts/{id}` | Account details | READ_SAVINGSACCOUNT | No | Existing visibility and not-found handling |
| POST `/savingsproducts`; PUT `/savingsproducts/{id}` | Configure product | CREATE/UPDATE_SAVINGSPRODUCT | Configured MC | Product validation; no transaction amount authority |
| POST `/savingsaccounts` | Apply | CREATE_SAVINGSACCOUNT | Configured MC | Application validation; not a deposit |
| POST `/savingsaccounts/{id}?command=approve\|activate\|close` | Lifecycle | APPROVE/ACTIVATE/CLOSE_SAVINGSACCOUNT respectively | Configured MC | Positive cash withdrawal on closure: WITHDRAWALS; lifecycle constraints remain |
| POST `/savingsaccounts/{id}/transactions?command=deposit\|withdrawal` | Money movement | DEPOSIT/WITHDRAWAL_SAVINGSACCOUNT | Configured MC | DEPOSITS/WITHDRAWALS respectively; denial before financial service |
| POST `/savingsaccounts/{id}/transactions?command=force-withdrawal` | Exceptional withdrawal | FORCE_WITHDRAWAL_SAVINGSACCOUNT | Configured MC | WITHDRAWALS plus exceptional permission/rules |
| POST `/savingsaccounts/{id}/transactions/{transactionId}?command=modify` | Adjustment | ADJUSTTRANSACTION_SAVINGSACCOUNT | Configured MC | Withdrawal replacement amount authorized; deposit adjustment retains existing behavior |
| POST `/accounttransfers` | Core transfer | CREATE_ACCOUNTTRANSFER | Configured MC | TRANSFER on full source principal; ordinary account combinations only |
| POST `/accounttransfers/refundByTransfer` | Loan refund to savings | REFUNDBYTRANSFER_ACCOUNTTRANSFER | Configured MC | TRANSFER on actual loan source; loan eligibility retained |
| POST `/standinginstructions`; PUT `/standinginstructions/{id}` | Setup/change instruction | CREATE/UPDATE_STANDINGINSTRUCTION | Configured MC | FIXED requires TRANSFER; restricted DUES lifecycle |
| DELETE `/standinginstructions/{id}` | Delete instruction | DELETE_STANDINGINSTRUCTION | Configured MC | Existing cancellation controls |
| POST `/fixeddepositaccounts/{id}?command=close\|prematureClose` | FD closure | CLOSE/PREMATURECLOSE_FIXEDDEPOSITACCOUNT | Configured MC | Transfer closure: planned net TRANSFER; nontransfer: no TRANSFER |
| POST `/recurringdepositaccounts/{id}?command=close\|prematureClose` | RD closure | CLOSE/PREMATURECLOSE_RECURRINGDEPOSITACCOUNT | Configured MC | Same boundary with RD calculation behavior |
| POST `/savingsaccounts/{id}?command=applyAnnualFees` | Unsupported manual fee | APPLYANNUALFEE_SAVINGSACCOUNT | Rejected before queue | HTTP 400 unsupported after earlier authentication/permission/parsing checks |
| POST `/makercheckers/{auditId}?command=approve\|reject` | Review command | Existing checker permission for underlying command | Approval operation | Stored maker/payload used; checker limit cannot override |

Ordinary batch requests shall retain the underlying command policy. Import template/upload routes shall preserve workbook row results and propagated maker identity (E-IMP, E-D, E-W). Command success/pending responses shall retain the existing command-result contract; acceptance into a pending workflow shall not be presented as proof of committed financial effects (E-CMD).

## 7. Data and audit requirements

| ID | Requirement | Evidence / qualification |
|---|---|---|
| SAV-DATA-001 | Current: account, transaction, charge, transfer, payment-detail and accounting records shall retain existing associations; monetary guards shall precede their protected writes. | E-T, E-X, E-C, E-J |
| SAV-DATA-002 | Current: original maker shall remain in `CommandSource.maker`; envelope metadata shall occupy existing command JSON rather than a new migration or client-supplied authorization flag. | E-CMD, E-ENV |
| SAV-DATA-003 | Current: audit display shall show business payload, while execution separately validates metadata. Legacy history readability shall not authorize legacy pending execution. | E-D, E-ENV |
| SAV-DATA-004 | Proposed: operational reconciliation shall correlate account transactions, transfers, journals and command outcomes and preserve investigation evidence. No immutable/tamper-proof audit guarantee is asserted. | E-J, E-CMD; SECURITY.md |
| SAV-DATA-005 | Decision: retention duration, legal hold, archival access and deletion policy shall be approved by the institution; no retention period is invented by this SRS. | Product/compliance decision |

## 8. Non-functional requirements

| ID | Requirement and delivery qualification |
|---|---|
| SAV-NFR-001 | Proposed acceptance: financial effects shall commit atomically or leave no partial financial state. Existing transactional implementation (E-T, E-CMD, E-C) requires live database rollback evidence. Failed-command audit records are permitted. |
| SAV-NFR-002 | Current: successful command replay shall bypass new execution (E-CMD). Proposed acceptance: persisted financial records shall remain exactly once under retry; external-event delivery semantics require explicit validation. |
| SAV-NFR-003 | Current: closure shall use source locking/state validation (E-C). Proposed acceptance: competing closure requests shall persist at most one effect, without timing-only race tests. |
| SAV-NFR-004 | Proposed: representative listing, posting and closure workloads shall meet approved latency/throughput targets. No measured SLA or capacity figure is available; product/operations shall set workload and target values. |
| SAV-NFR-005 | Current: ordinary permission, provenance and monetary checks shall remain distinct (E-POL, E-ENV, E-CMD). Proposed acceptance: cross-tenant and visibility violations shall be rejected end to end; tenant isolation follows SECURITY.md assumptions. |
| SAV-NFR-006 | Proposed: operations shall monitor failed commands/jobs and reconcile financial outcomes. Existing audit/job evidence shall not log customer secrets or imply mandate verification (E-CMD, E-JOB, E-S). |
| SAV-NFR-007 | Current: stored envelope formats, flat audit presentation and financial formulas shall preserve baseline compatibility (E-ENV, E-I, E-C). Deployment shall coordinate node versions and cancel/resubmit affected legacy pending commands. |
| SAV-NFR-008 | Current: monetary calculations shall preserve existing BigDecimal/Money precision, configured currency scale, rounding and transaction ordering (E-I, E-C, E-P). No undocumented conversion or universal two-decimal assumption shall be added. |

## 9. Error catalogue

Exact codes are stated only where confirmed. Authorization/domain errors use the existing mapper; earlier permission or validation failures can take precedence.

| Category | Confirmed behavior / code | Evidence |
|---|---|---|
| Validation | Existing validation response; status uses `must.be.an.integer` or `must.be.a.valid.savings.account.status.id` as validator suffixes, not independent full API codes | E-L |
| Unsupported manual annual fee | HTTP 400; global `validation.msg.validation.errors.exist`, detail `error.msg.command.unsupported` | E-FEE; annual-fee report |
| Deposit authority | HTTP 403 domain error, `error.msg.savings.deposit.monetary.authority.denied` | E-D; deposit report |
| Withdrawal authority | Domain error `error.msg.savings.withdrawal.monetary.authority.denied` | E-W |
| Transfer/closure authority | Domain error `error.msg.accounttransfer.monetary.authority.denied` | E-X, E-C |
| Unknown historical provenance | Actionable cancel/resubmit domain error; deposit code `error.msg.savings.deposit.untrusted.origin`; other kinds retain codec-specific errors | E-ENV; deposit report |
| Client metadata spoof | Deposit code `error.msg.savings.deposit.reserved.metadata`; other kinds retain codec-specific rejection | E-ENV |
| DUES restriction | `error.msg.standinginstruction.dues.requires.maximum` | E-S |
| Stale closure plan | `error.msg.deposit.closure.plan.stale`; no application of the stale plan | E-C |
| Lifecycle/balance violation | Existing operation-specific validators/domain errors; no single universal lifecycle code is specified | E-A, E-T, E-F |

## 10. Requirements traceability

Status labels describe evidence, not blanket production readiness. “Implemented but integration validation pending” includes inherited behavior with test sources that were not executed in the stabilization run.

| Requirement IDs | Exact source/test references | Classification |
|---|---|---|
| SAV-PROD-001 | E-P | Implemented but integration validation pending |
| SAV-ACC-001, SAV-TXN-005, SAV-BR-010 | E-A, E-T, E-F | Implemented but integration validation pending |
| SAV-ACC-002, SAV-TXN-002/003, SAV-FEE-001 | E-W, E-T | Implemented and unit-tested |
| SAV-TXN-001, SAV-IMP-001 | E-D, E-W, E-IMP | Implemented and unit-tested |
| SAV-TXN-004, SAV-BR-002 | E-X | Implemented and unit-tested |
| SAV-INT-001 | E-I, E-F | Implemented and unit-tested; broad financial integration pending |
| SAV-INT-002, SAV-FDRD-001/002/003 | E-C, E-F | Implemented and unit-tested; database integration pending |
| SAV-FDRD-004 | E-F, E-C | Implemented but integration validation pending |
| SAV-FEE-002 | E-FEE, E-JOB | Implemented and unit-tested |
| SAV-SI-001/002/003, SAV-BR-007/008 | E-S, E-JOB | Implemented and unit-tested; scheduler integration pending |
| SAV-AUTH-001/002, SAV-AUD-001, SAV-BR-001/003/004/005/006/009 | E-CMD, E-ENV, E-POL, E-C | Implemented and unit-tested |
| SAV-REP-001 | E-L | Implemented and unit-tested |
| SAV-REP-002, SAV-ACCNT-001, SAV-JOB-001, SAV-DATA-001 | E-A, E-T, E-J, E-JOB | Implemented but integration validation pending; reconciliation criterion proposed |
| SAV-DATA-002/003, SAV-NFR-007/008 | E-CMD, E-ENV, E-I, E-C | Implemented and unit-tested |
| SAV-DATA-004, SAV-NFR-001/002/003/005/006 | E-CMD, E-J, E-C; integration guide | Implemented mechanisms but integration validation pending; operational acceptance proposed |
| SAV-DATA-005, SAV-NFR-004 | No approved retention/SLA specification | Product decision required |
| Alternative transfer authority paths in §11 | Transfer report; E-IMP | Deferred |
| Customer mandate and bounded DUES replacement | E-S; transfer report | Product decision required |

## 11. Known gaps and risks

| Priority | Gap | Consequence / required evidence |
|---|---|---|
| High | Live PostgreSQL closure environment unavailable at preflight | No persisted rollback, payment-detail, event or balanced-journal proof; unit assertions are insufficient substitutes |
| High | Concurrent closure and transient execution retry not database-validated | At-most-one persisted financial effect remains an acceptance requirement, not a reproduced duplicate defect |
| High | Standing-instruction retry/concurrent execution not database-validated | Last-run handling and command replay tests do not prove scheduled occurrence idempotency |
| High | Grandfathered DUES remains uncapped | Inventory/review existing instructions; replacement or a separately approved ceiling design required |
| High | Customer mandate evidence absent from these controls | Permissions/limits do not prove customer consent; evidence, revocation, scope and lifecycle need product design |
| Deferred | Deposit activation, loan disbursement/top-up, guarantee recovery and indirect-import authority paths | Core/closure enforcement is not system-wide transfer authority; do not label all internal calls exempt |
| Medium | Existing cross-currency inconsistencies retained | No coherent FX capability is certified; a separate product/conversion specification is needed |

These risks derive from the latest transfer report and database preflight. This document establishes no new Critical defect and does not claim an exploit or duplicate-payment reproduction.

## 12. Acceptance criteria and test strategy

| Layer | Required acceptance | Present evidence |
|---|---|---|
| Unit | Inclusive/invalid limits, provenance, original maker, checker denial, DUES restrictions, pure calculations, stale-plan rejection and exact fixtures shall pass. | Selected 404-test stabilization at baseline; E-D/W/X/S/C/I/POL |
| Component | Stored approval/retry payloads, maker identity, pending rollback boundaries and completed replay shall be tested through command dispatch. | Existing command/proxy tests with mocked persistence; E-CMD/S/C |
| API integration | Supported endpoints, numeric/external IDs, permissions, error contracts, filtering and import row outcomes shall be exercised against a running server. | Existing integration sources; baseline live execution pending |
| Database integration | All four FD/RD modes shall prove success and denied zero financial persistence, unchanged destination/source on denial, reduced/removed maker rejection and approval recalculation. | Pending; no executed database closure matrix |
| Accounting reconciliation | Exact interest/tax/net shall reconcile with source/destination transactions; debit sum shall equal credit sum using fixture-created GL mappings; no duplicate set shall persist. | Journal helpers and FD/RD integration fixtures exist; execution pending |
| Concurrency/retry | Deterministically coordinated attempts shall yield at most one closure effect; replay and transient failure retry shall not duplicate transfers/journals. Test-only fault/control mechanisms shall not alter production transaction behavior. | Latch patterns exist; server-side overlap and fault injection require validation |
| UAT | Staff shall demonstrate maker/checker separation, rejection/resubmission, teller limits, closure statements and operational reconciliation; operations shall approve DUES inventory and rollout controls. | Not recorded as completed |

The database suite shall cover normal FD, premature FD, normal RD and premature RD, each with transfer. Denial evidence shall distinguish financial rollback from legitimate error audit records. Proposed environment shall reuse the repository PostgreSQL test Compose stack and documented integration runner; no parallel framework or production change is required by this SRS. Formatting/link checks are document validation, not product acceptance.

## 13. Delivery readiness matrix

| Capability | Status / evidence | Remaining risk | Recommended release action |
|---|---|---|---|
| Status filter | Unit-tested, E-L | Live API/visibility regression coverage | Include API smoke validation |
| Manual annual-fee rejection | Unit-tested, E-FEE | Existing scheduled accounting not end-to-end verified | Retain scheduler regression and operational reconciliation |
| Deposit/withdrawal authority | Unit/component-tested, E-D/W | Live transactions/imports/tenant behavior | Validate database rollback and coordinated envelope rollout |
| Core transfer authority | Unit/component-tested, E-X | Financial integration and explicitly excluded paths | Release only with documented scope and integration gate |
| FIXED setup / DUES restriction | Unit/component-tested, E-S | Grandfathered exposure, occurrence concurrency, consent | Inventory DUES and track separate mandate/idempotency work |
| Calculation primitives / FD-RD closure | Unit-tested, E-I/C; exact correction fixtures | Database concurrency, tax/journal reconciliation and retries | Gate financial readiness on four-mode database evidence |
| Product/lifecycle/accounting breadth | Existing implementation/integration fixtures, E-P/A/F/J | Not comprehensively executed in stabilization | Run risk-based integration and UAT before production claim |
| Deferred alternative transfers | Deferred | No universal TRANSFER boundary | Keep explicit backlog and operational scope disclosure |

**Single next engineering task:** implement the four-mode database-backed FD/RD closure authorization/rollback/reconciliation/retry/concurrency suite after restoring its test environment. Do not change production formulas, locking, envelopes or transaction boundaries as part of that test task without separate approval.

## 14. Evidence index

Each key below names exact baseline implementation and test files. Test presence does not imply execution; §§1, 10 and 12 define validation scope.

### E-P

[SavingsProduct](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsProduct.java); [SavingsProductDataValidator](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/data/SavingsProductDataValidator.java); [SavingsProductAccountingDataValidator](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/data/SavingsProductAccountingDataValidator.java); [SavingsProductCreationIntegrationTest](../../integration-tests/src/test/java/org/apache/fineract/integrationtests/SavingsProductCreationIntegrationTest.java).

### E-A

[SavingsAccountsApiResource](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/api/SavingsAccountsApiResource.java); [SavingsAccountDataValidator](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/data/SavingsAccountDataValidator.java); [SavingsAccountApplicationTransitionApiJsonValidator](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/service/SavingsAccountApplicationTransitionApiJsonValidator.java); [SavingsAccountsTest](../../integration-tests/src/test/java/org/apache/fineract/integrationtests/SavingsAccountsTest.java).

### E-L

[SavingsAccountsApiResource](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/api/SavingsAccountsApiResource.java); [SavingsAccountReadPlatformServiceImpl](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/service/SavingsAccountReadPlatformServiceImpl.java); [SavingsAccountsApiResourceTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/api/SavingsAccountsApiResourceTest.java); [SavingsAccountListingTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/service/SavingsAccountListingTest.java).

### E-T

[SavingsAccountTransactionsApiResource](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/api/SavingsAccountTransactionsApiResource.java); [SavingsAccountWritePlatformServiceJpaRepositoryImpl](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/service/SavingsAccountWritePlatformServiceJpaRepositoryImpl.java); [SavingsAccountTransactionTest](../../integration-tests/src/test/java/org/apache/fineract/integrationtests/SavingsAccountTransactionTest.java); [SavingsAccountWritePlatformServiceJpaRepositoryImplTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/service/SavingsAccountWritePlatformServiceJpaRepositoryImplTest.java).

### E-D

[DepositSavingsAccountCommandHandler](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/handler/DepositSavingsAccountCommandHandler.java); [SavingsDepositAuthorityTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsDepositAuthorityTest.java); [SavingsDepositOriginTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/api/SavingsDepositOriginTest.java); [SavingsDepositAuditTest](../../fineract-provider/src/test/java/org/apache/fineract/commands/service/SavingsDepositAuditTest.java).

### E-W

[SavingsWithdrawalAuthorityService](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/service/SavingsWithdrawalAuthorityService.java); [SavingsWithdrawalAuthorityTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsWithdrawalAuthorityTest.java); [SavingsWithdrawalOriginTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/api/SavingsWithdrawalOriginTest.java); [SavingsWithdrawalChargeExemptionTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsWithdrawalChargeExemptionTest.java).

### E-X

[AccountTransfersApiResource](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/api/AccountTransfersApiResource.java); [AccountTransferAuthorityService](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/service/AccountTransferAuthorityService.java); [AccountTransferAuthorityTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/account/service/AccountTransferAuthorityTest.java); [AccountTransferEnvelopeTest](../../fineract-provider/src/test/java/org/apache/fineract/commands/service/AccountTransferEnvelopeTest.java); [CreateAccountTransferCommandStrategyTest](../../fineract-provider/src/test/java/org/apache/fineract/batch/command/internal/CreateAccountTransferCommandStrategyTest.java).

### E-S

[StandingInstructionApiResource](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/api/StandingInstructionApiResource.java); [StandingInstructionDuesPolicy](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/domain/StandingInstructionDuesPolicy.java); [StandingInstructionAuthorityTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/account/service/StandingInstructionAuthorityTest.java); [StandingInstructionDuesPolicyTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/account/domain/StandingInstructionDuesPolicyTest.java); [ExecuteOverdueAndCurrentStandingInstructionsTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/account/jobs/executestandinginstructions/ExecuteOverdueAndCurrentStandingInstructionsTest.java).

### E-C

[DepositClosureAuthorityService](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/service/DepositClosureAuthorityService.java); [DepositAccountClosurePlan](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/DepositAccountClosurePlan.java); [DepositAccountClosurePlanTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/domain/DepositAccountClosurePlanTest.java); [DepositClosureAuthorityTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/service/DepositClosureAuthorityTest.java); [DepositClosureWithdrawalTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/domain/DepositClosureWithdrawalTest.java); [DepositClosureEnvelopeTest](../../fineract-provider/src/test/java/org/apache/fineract/commands/service/DepositClosureEnvelopeTest.java).

### E-I

[SavingsAccount](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccount.java); [SavingsAccountBalanceProjection](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountBalanceProjection.java); [SavingsAccountRunningBalance](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountRunningBalance.java); [SavingsAccountBalanceProjectionTest](../../fineract-savings/src/test/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountBalanceProjectionTest.java); [SavingsAccountRunningBalanceTest](../../fineract-savings/src/test/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountRunningBalanceTest.java); [FixedDepositAccountInterestCalculationServiceImplTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/service/FixedDepositAccountInterestCalculationServiceImplTest.java).

### E-F

[FixedDepositAccount](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/FixedDepositAccount.java); [RecurringDepositAccount](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/RecurringDepositAccount.java); [DepositAccountDomainServiceJpa](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/DepositAccountDomainServiceJpa.java); [FixedDepositTest](../../integration-tests/src/test/java/org/apache/fineract/integrationtests/FixedDepositTest.java); [RecurringDepositTest](../../integration-tests/src/test/java/org/apache/fineract/integrationtests/RecurringDepositTest.java).

### E-FEE

[ApplyAnnualFeeSavingsAccountCommandHandler](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/handler/ApplyAnnualFeeSavingsAccountCommandHandler.java); [SavingsAnnualFeeCommandTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/api/SavingsAnnualFeeCommandTest.java); [SavingsAnnualFeeMakerCheckerTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsAnnualFeeMakerCheckerTest.java); [SavingsAnnualFeeSchedulerTest](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/jobs/SavingsAnnualFeeSchedulerTest.java).

### E-CMD

[CommandSource](../../fineract-core/src/main/java/org/apache/fineract/commands/domain/CommandSource.java); [CommandSourceService](../../fineract-core/src/main/java/org/apache/fineract/commands/service/CommandSourceService.java); [SynchronousCommandProcessingService](../../fineract-core/src/main/java/org/apache/fineract/commands/service/SynchronousCommandProcessingService.java); [CommandSourceServiceTest](../../fineract-provider/src/test/java/org/apache/fineract/commands/service/CommandSourceServiceTest.java); [SynchronousCommandProcessingServiceTest](../../fineract-provider/src/test/java/org/apache/fineract/commands/service/SynchronousCommandProcessingServiceTest.java).

### E-ENV

[SavingsTransactionCommandEnvelope](../../fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionCommandEnvelope.java); [SavingsTransactionKind](../../fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionKind.java); [SavingsTransactionExecutionContext](../../fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionExecutionContext.java); [SavingsDepositEnvelopeTest](../../fineract-provider/src/test/java/org/apache/fineract/commands/service/SavingsDepositEnvelopeTest.java); [SavingsWithdrawalEnvelopeTest](../../fineract-provider/src/test/java/org/apache/fineract/commands/service/SavingsWithdrawalEnvelopeTest.java).

### E-POL

[NsimbiMonetaryAuthorityPolicyService](../../fineract-provider/src/main/java/org/apache/fineract/nsimbi/userroles/service/NsimbiMonetaryAuthorityPolicyService.java); [NsimbiUserMonetaryAuthority](../../fineract-provider/src/main/java/org/apache/fineract/nsimbi/userroles/domain/NsimbiUserMonetaryAuthority.java); [NsimbiMonetaryAuthorityPolicyServiceTest](../../fineract-provider/src/test/java/org/apache/fineract/nsimbi/userroles/service/NsimbiMonetaryAuthorityPolicyServiceTest.java); [NsimbiUserMonetaryAuthorityTest](../../fineract-provider/src/test/java/org/apache/fineract/nsimbi/userroles/domain/NsimbiUserMonetaryAuthorityTest.java).

### E-J

[SavingsAccountDomainServiceJpa](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountDomainServiceJpa.java); [JournalEntryHelper](../../integration-tests/src/test/java/org/apache/fineract/integrationtests/common/accounting/JournalEntryHelper.java); [FeignJournalEntryHelper](../../integration-tests/src/test/java/org/apache/fineract/integrationtests/client/feign/helpers/FeignJournalEntryHelper.java).

### E-API

[SavingsProductsApiResource](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/api/SavingsProductsApiResource.java); [FixedDepositAccountsApiResource](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/api/FixedDepositAccountsApiResource.java); [RecurringDepositAccountsApiResource](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/api/RecurringDepositAccountsApiResource.java); [MakercheckersApiResource](../../fineract-provider/src/main/java/org/apache/fineract/commands/api/MakercheckersApiResource.java).

### E-JOB

[ExecuteStandingInstructionsTasklet](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/jobs/executestandinginstructions/ExecuteStandingInstructionsTasklet.java); [ApplyAnnualFeeForSavingsTasklet](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/jobs/applyannualfeeforsavings/ApplyAnnualFeeForSavingsTasklet.java); [PayDueSavingsChargesTasklet](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/jobs/payduesavingscharges/PayDueSavingsChargesTasklet.java).

### E-IMP

[SavingsTransactionImportHandler](../../fineract-provider/src/main/java/org/apache/fineract/infrastructure/bulkimport/importhandler/savings/SavingsTransactionImportHandler.java); [FixedDepositImportHandler](../../fineract-provider/src/main/java/org/apache/fineract/infrastructure/bulkimport/importhandler/fixeddeposits/FixedDepositImportHandler.java); [RecurringDepositImportHandler](../../fineract-provider/src/main/java/org/apache/fineract/infrastructure/bulkimport/importhandler/recurringdeposit/RecurringDepositImportHandler.java); [LoanImportHandler](../../fineract-provider/src/main/java/org/apache/fineract/infrastructure/bulkimport/importhandler/loan/LoanImportHandler.java).
