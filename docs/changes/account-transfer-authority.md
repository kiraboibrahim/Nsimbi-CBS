# Account Transfer Authority — Phase 1

## Phase 1 complete — 2026-09-30

This delivery is frozen to the enrolled core staff commands and standing-instruction setup. **It does not provide system-wide transfer-authority enforcement.** Deposit-closure planning and the other alternatives below are Phase 2, not active Phase 1 blockers. No commit, push, branch creation or PR has been performed.

### Phase 1 behaviour matrix

| Entry or operation | Implemented behaviour |
| --- | --- |
| CREATE_ACCOUNTTRANSFER and REFUNDBYTRANSFER_ACCOUNTTRANSFER | Current original-maker TRANSFER authority on the complete principal in the source account's currency, with inclusive limits; missing/unconfigured/invalid authority fails closed before the financial writer and approval queuing. Refund resolves the actual loan source even if the request claims a savings source. Existing permissions, eligibility, fees and accounting remain in their existing services. |
| Ordinary API, batch, cashier/teller using those commands | Same command guard and server-owned STAFF_API provenance. No role, header or query exemption. There is no dedicated spreadsheet-transfer importer in this checkout; indirect loan/deposit imports are excluded from this phase. |
| Initial execution, approval and execution retry | Shared dispatch uses the persisted CommandSource maker (the authenticated submitting maker initially), stored payload and origin. The checker retains permission/audit identity and cannot substitute their monetary limit. Current maker authority is re-evaluated on execution. Existing successful replay handling is unchanged. |
| Fixed standing-instruction creation/material update | Authorize full per-execution fixed principal in source currency before setup mutation; updates use the proposed amount or the stored amount. Schedule, priority and activation changes are covered. Source/destination updates remain unsupported. The update row is locked with PESSIMISTIC_WRITE inside the contextual handler transaction. |
| Pending fixed instruction, including due-now setup | Existing maker-checker rollback prevents unapproved setup from committing; approved execution rechecks the original maker. The focused Spring proxy/JDBC transaction-boundary test verifies rollback before approval and commit only after approval. It uses a mocked JDBC connection and does not certify live scheduler/database concurrency. |
| New/pending DUES setup and clones | Reject through setup validation and the domain creation factory. Pending approval uses the same guard. Loan automatic DUES creation is rejected before disbursement financial mutations when a linked account exists. This narrow loan change prevents new DUES; it is **not** loan-transfer monetary-authority coverage. |
| Existing DUES | Read and unchanged scheduled execution preserved; suspension and cancellation allowed. Reactivation, material modification, cloning and both fixed-to-DUES and DUES-to-fixed conversion rejected. Successful grandfathered execution logs only instruction ID and business date. |
| Trusted metadata/history | Raw API and batch JSON reaches reserved-metadata rejection. Enrolled commands use the existing version-2 codec and kind/origin allowlist. Untrusted pending enrolled commands require cancellation/resubmission. Completed flat audit remains readable. Deposit version 1, withdrawal version 2 and unrelated flat JSON remain compatible. |

Authority is per execution, not an invented cumulative limit. Separately calculated fees are excluded from transfer principal. No DIRECT_DEBIT, DIRECT_CREDIT, DEPOSITS or WITHDRAWALS authority is added to transfer legs. No FX conversion or destination/base-currency authority rule is introduced; existing cross-currency behaviour is preserved rather than certified as a coherent FX feature.

### Phase 2 exclusions and exact remaining work

- Fixed- and recurring-deposit closure transfers, including premature closure; immutable closure planning; exact interest, withholding-tax and net-proceeds authorization/application; stale-plan and financial-equivalence validation.
- Deposit activation transfers, loan disbursement/top-up alternatives, guarantee recovery, their actual indirect import paths, and other internal DTO/cross-module callers that bypass the enrolled commands. Add trusted original-maker context and authorization before parent financial mutations in that later scope.
- Database-backed scheduler concurrency, occurrence idempotency and concurrent setup/scheduler isolation validation. The current last-run-date logic and transaction-boundary tests are not a proof of those properties.
- Customer-mandate verification and consent evidence/lifecycle. Neither staff permissions nor TRANSFER authority establishes customer consent.

### Calculation classification and preservation

No calculation or closure-preview code remains in Phase 1. All ten calculation-related files were preserved in `/tmp/account-transfer-phase2-closure.patch` before targeted removal. No file overlapped Phase 1 runtime changes, so the four tracked calculation files were restored to HEAD and the six new calculation/test files were removed only after preservation and reverse-apply checking. No repository reset, stash or unrelated restoration was used.

| Change family | Classification |
| --- | --- |
| FD/RD pure rate lookup and rate tests | Validated shared extraction, but not required by Phase 1. Kept with the deferred closure work; subtype files also contain unfinished preview entry points. |
| Running-balance extraction and tests | Independently useful and previously validated; voluntarily deferred to keep the frozen Phase 1 focused. Existing execution used the extracted formula; no duplicate formula was created. |
| End-of-day calculation extraction | Shared behaviour-preserving prerequisite for projection, not required by Phase 1; preserved with its consumers/tests. |
| Balance projection, shared interest-period engine and subtype previews | Closure prerequisites, not an immutable closure plan. Deferred in full, including purity/equivalence fixtures. |

Files/hunks in the Phase 2 patch (complete working-tree calculation diffs relative to HEAD, including new files):

- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/FixedDepositAccount.java`
- `fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/RecurringDepositAccount.java`
- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccount.java`
- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountTransaction.java`
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/domain/DepositAccountApplicableInterestRateTest.java`
- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountRunningBalance.java`
- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountBalanceProjection.java`
- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountEndOfDayBalance.java`
- `fineract-savings/src/test/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountRunningBalanceTest.java`
- `fineract-savings/src/test/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountBalanceProjectionTest.java`

Patch size: **54516 bytes**. SHA-256: `f47d2d87687304d26eddfd21414f7ff7d3848092b64dbc97530f45e52b65b592`. The non-empty patch was forward-checked in a temporary baseline directory, reverse-checked before removal, and forward-checked against the resulting Phase 1 tree using:

```sh
git apply --check /tmp/account-transfer-phase2-closure.patch
```

To recover later, use `git apply /tmp/account-transfer-phase2-closure.patch` after checking the intended target tree. The recovery patch is local and outside Git; preserve it with the handoff.

### Exact Phase 1 changed files

- [SavingsTransactionCommandEnvelope.java](../../fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionCommandEnvelope.java)
- [SavingsTransactionKind.java](../../fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionKind.java)
- [CommandWrapperBuilder.java](../../fineract-core/src/main/java/org/apache/fineract/commands/service/CommandWrapperBuilder.java)
- [CreateAccountTransferCommandStrategy.java](../../fineract-provider/src/main/java/org/apache/fineract/batch/command/internal/CreateAccountTransferCommandStrategy.java)
- [AccountTransfersApiResource.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/api/AccountTransfersApiResource.java)
- [StandingInstructionApiResource.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/api/StandingInstructionApiResource.java)
- [StandingInstructionDataValidator.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/data/StandingInstructionDataValidator.java)
- [AccountTransferStandingInstruction.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/domain/AccountTransferStandingInstruction.java)
- [StandingInstructionDuesPolicy.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/domain/StandingInstructionDuesPolicy.java)
- [StandingInstructionRepository.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/domain/StandingInstructionRepository.java)
- [CreateAccountTransferCommandHandler.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/handler/CreateAccountTransferCommandHandler.java)
- [CreateStandingInstructionCommandHandler.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/handler/CreateStandingInstructionCommandHandler.java)
- [RefundByTransferCommandHandler.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/handler/RefundByTransferCommandHandler.java)
- [UpdateStandingInstructionCommandHandler.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/handler/UpdateStandingInstructionCommandHandler.java)
- [ExecuteStandingInstructionsTasklet.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/jobs/executestandinginstructions/ExecuteStandingInstructionsTasklet.java)
- [AccountTransferAuthorityService.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/service/AccountTransferAuthorityService.java)
- [StandingInstructionWritePlatformServiceImpl.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/service/StandingInstructionWritePlatformServiceImpl.java)
- [LoanWritePlatformServiceJpaRepositoryImpl.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanWritePlatformServiceJpaRepositoryImpl.java)
- [AccountTransferEnvelopeTest.java](../../fineract-provider/src/test/java/org/apache/fineract/commands/service/AccountTransferEnvelopeTest.java)
- [StandingInstructionDuesPolicyTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/account/domain/StandingInstructionDuesPolicyTest.java)
- [AccountTransferAuthorityTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/account/service/AccountTransferAuthorityTest.java)
- [StandingInstructionAuthorityTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/account/service/StandingInstructionAuthorityTest.java)
- [LoanWritePlatformServiceJpaRepositoryImplTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/loanaccount/service/LoanWritePlatformServiceJpaRepositoryImplTest.java)
- [SavingsDepositAuthorityTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsDepositAuthorityTest.java)
- [SavingsWithdrawalAuthorityTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsWithdrawalAuthorityTest.java)
- This report.

### Validation

Final focused validation passed: **213 tests executed, 213 passed, 0 failures, 0 errors, 0 skipped**. The historical 283-test count below includes Phase 2 calculations and is **not** the Phase 1 total.

Exact Phase 1 test command:

```sh
./gradlew :fineract-provider:test --tests '*AccountTransfer*Test' --tests '*StandingInstruction*Test' --tests '*LoanWritePlatformServiceJpaRepositoryImplTest.automaticDuesCreationRejectsBeforeDisbursementMutations' --tests '*Savings*AuthorityTest' --tests '*Savings*EnvelopeTest' --tests '*SavingsAnnualFee*Test' --tests '*CommandSourceServiceTest' --tests '*SynchronousCommandProcessingServiceTest' --offline --no-daemon --no-parallel --max-workers=1 -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/account-transfer-tests.gradle -x :fineract-provider:resolve
```

The temporary test init script sets maxHeapSize to 512 MiB and maxParallelForks to 1. The one selected loan test verifies new-DUES rejection only; no excluded loan-transfer or calculation tests are added to increase totals.

Scoped formatter command, using explicit changed-file collections and assertions that each intended file is a task input:

```sh
./gradlew :fineract-core:spotlessJavaApply :fineract-provider:spotlessJavaApply :fineract-core:spotlessJavaCheck :fineract-provider:spotlessJavaCheck --offline --no-daemon --no-parallel --max-workers=1 -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/account-transfer-phase1-spotless.gradle
```

Focused coverage includes limit boundaries, missing/unconfigured/malformed historical limits, source currency and refund source spoofing, original maker/checker behaviour, reduced/removed authority, execution retry with stored maker/origin/payload, pre-queue denial, batch/raw metadata, fixed create/update, pending setup transaction rollback, new/pending DUES rejection and existing DUES lifecycle restrictions. Existing deposit/withdrawal, annual-fee, envelope/history, unrelated JSON and command retry regressions are included. The initial Phase 1 run found a test fixture that tried to construct an invalid range through a constructor that correctly rejects it; the fixture now represents malformed persisted state explicitly. Its interrupted Mockito stub caused the second reported failure. The final successful run supersedes both fixture failures.

Scoped Spotless apply/check passed for all 25 changed Java files. Final tracked and untracked whitespace checks passed. Report links resolve; historical references to removed calculation files now identify their Phase 2 patch location. The final scope is 25 Java files plus this report, with no migrations or generated files. The preserved patch remains non-empty and passes `git apply --check`; its four tracked files match HEAD and its six new files are absent from Phase 1. Production changes and focused test fixtures were reviewed.

### Risks and rollout limitations

- This is core-command coverage only. Staff-transfer alternatives listed under Phase 2 remain outside this enforcement boundary.
- Grandfathered DUES remains variable and uncapped. Inventory active/suspended instructions and pending setup before rollout, and review replacement with separately authorized fixed instructions. No tenant inventory or data migration was performed.
- No customer consent is verified. A separate mandate/evidence design remains necessary.
- Mixed application versions are unsafe: older nodes can bypass new setup restrictions and cannot interpret the new protected envelopes. Coordinate node rollout and cancel/resubmit affected historical pending commands; completed history remains readable.
- Existing financial/accounting and scheduler algorithms are retained. Focused mocked-resource tests establish command and transaction behaviour, not a live tenant security or scheduler-concurrency certification.

### Suggested delivery text (not executed)

Signed commit: `git commit -S -m "feat(account): enforce transfer authority for core staff commands"`

PR title: **Account Transfer Authority — Phase 1: protect core staff commands**

PR description:

> Enforces the original submitting maker's current TRANSFER authority before financial execution or approval queuing for direct/refund account-transfer commands, including ordinary API/batch/cashier use. Protects fixed standing-instruction setup, rejects new/pending DUES and risky lifecycle changes, and preserves existing DUES suspension/cancellation/scheduled execution.
>
> Reuses trusted command envelopes while preserving deposit/withdrawal formats and completed audit readability. Calculation/closure changes are preserved separately in a verified local Phase 2 patch. Deposit closures, alternative internal transfer paths, scheduler database concurrency and customer-mandate verification are explicitly outside this PR. Validation results are listed above.

## Archived investigation and earlier implementation checkpoints

Everything below records earlier scope, decisions and validation. Earlier requests to stop or obtain design approval, and unfinished full-system scope, are historical and are not active Phase 1 blockers. The Phase 1 scope above is authoritative.

# Account-transfer monetary authority: implementation in progress

## Current implementation checkpoint — 2026-09-30

**The requested feature is still incomplete.** Direct/refund transfer and standing-instruction setup guards now exist, along with mutation-free interest calculation prerequisites. The immutable closure plan, exact net-proceeds application and authorization of the remaining alternative staff transfer paths are not yet implemented. No new policy or architectural blocker has been established; the remaining work is already authorized. Do not treat this checkpoint as a deployable implementation of the complete policy.

### Implemented authorization and DUES controls

- `SavingsTransactionKind.fromCommand`, `CommandWrapperBuilder` and the shared codec enroll CREATE_ACCOUNTTRANSFER, REFUNDBYTRANSFER_ACCOUNTTRANSFER and CREATE/UPDATE_STANDINGINSTRUCTION. Their four handlers use the existing typed dispatch and immutable maker context. `AccountTransferAuthorityService.authorize` checks the original maker's current TRANSFER row before calling the financial/setup writer. Ordinary API and batch construction share the enrolled builder; cashier use of those entry points receives the same guard. No DEPOSITS, WITHDRAWALS, DIRECT_DEBIT or DIRECT_CREDIT check is added to transfer legs.
- The principal is the requested full amount in the loaded source-account currency; separately calculated fees remain outside this amount. Refund uses the actual loan source regardless of a supplied source-type value. No exchange-rate calculation, destination-currency authority rule, visibility rule or existing financial eligibility check is replaced. The previously documented cross-currency inconsistencies are not resolved by this patch.
- REST create/refund and standing-instruction create/update accept raw JSON while retaining their documented DTO schema, so reserved metadata reaches the shared rejection boundary instead of disappearing during DTO binding. Deposit version 1 and existing savings version 2 formats remain unchanged; new kinds use version 2. Legacy pending commands for the enrolled operations fail closed with cancellation/resubmission guidance. Completed flat history stays readable. Existing dispatch retains persisted maker, flat handler payload, checker identity and stored approval payload.
- Fixed standing-instruction creation and updates require TRANSFER authority. An update authorizes the proposed amount or the stored amount when omitted, including schedule, priority and activation changes. `StandingInstructionRepository.findByIdForUpdate` obtains a pessimistic write lock within the contextual handler transaction; the writer uses that same locked row. Unsupported account changes remain rejected by the existing update parameter allowlist. Normal maker-checker rollback remains responsible for isolating pending setup from scheduler reads; database-level concurrency/approval isolation tests remain outstanding.
- `StandingInstructionDuesPolicy` rejects new DUES, fixed-to-DUES changes, DUES material changes, DUES-to-fixed conversion, and DUES reactivation. Existing DUES permits only a status-only suspension with formatting parameters, or the existing delete/cancel operation. The domain factory also rejects new DUES, covering internal creation/cloning. JPA hydration of historical records is unchanged.
- Loan disbursement has an additional early check for configured automatic DUES creation with a linked account. It rejects before disbursement mutations, payment-detail writes, business events or accounting; callers must disable that automatic creation before disbursement. The domain factory is a second guard, not the sole late guard.
- Existing scheduled DUES calculation, due selection, transfer and last-run update remain unchanged. Successful grandfathered execution logs instruction ID and business date. No scheduler-origin value is accepted in persisted staff envelopes. This does not yet constitute a database-backed proof of occurrence idempotency, concurrent scheduling or manual-job isolation. Grandfathered variable exposure remains uncapped. Staff permission and monetary authority do not prove customer consent.

### Calculation work implemented, not yet a closure plan

The previously validated pure rate and running-balance extractions remain in use. `SavingsAccountBalanceProjection.calculate` now calculates detached posting-period transaction inputs without changing the account or transactions or constructing replacement transactions. It projects replacement ordering using the existing date/audit/identifier ordering. Execution and projection share the replacement predicate and running-balance arithmetic. `SavingsAccountEndOfDayBalance.calculate` supplies the original end-date/day-count/cumulative-balance rules to both execution and projection, including existing rounding and raw requested-end-date behavior.

`SavingsAccount.calculateInterestPeriods` contains the shared interest-period calculation. Existing `calculateInterestUsing` retains managed balance/rate/summary updates; `previewInterestUsing` uses projected balances and an explicit rate without those updates. Fixed and recurring `previewClosureInterest` preserve their distinct maturity caps and premature-interest paths. Existing `calculateInterestPayable` execution retains its rate/summary behavior; preview suppresses those managed updates while using the same formulas.

Tests compare transaction snapshots before/after projection, independently assert balance/day/cumulative results, compare actual reversal/replacement execution ordering, and verify an independent five-day interest result of 5.00 on 1,000 at 36.5% with 365-day annual compounding. **PostingPeriod objects are calculation intermediates, not the required immutable closure plan.** No net-proceeds authorization or stale closure-plan application protection is claimed.

### Required work still outstanding

1. Capture exact interest posting/correction actions, withholding allocations, gross/net proceeds, currency and authoritative state/version in an immutable closure result. Apply those exact calculated values after authorization, without independent interest/tax recalculation. Integrate into manual FD/RD normal and premature closure before payment-detail, interest, tax, balance, accounting or event mutation. Preserve no-transfer closure controls and recalculate on approval/retry.
2. Propagate trusted original-maker context and add early preflight for remaining staff-controlled alternatives: deposit activation/closure, linked loan disbursement/top-up, guarantee recovery and their actual import entry points. Current direct-handler protection does not cover those parents or every internal DTO caller. Do not label internal DTO calls categorically exempt.
3. Complete closure interest/tax/net equivalence, stale-state and concurrency tests; persisted original-maker approval/retry across the remaining kinds; financial rollback/journal assertions; fixed due-now approval isolation; grandfathered scheduled DUES/manual-job/idempotent occurrence coverage. No customer-mandate redesign or migration is authorized or implemented.

### Validation

Final combined validation passed: **283 distinct tests — 67 savings and 216 provider; zero failures, errors or skips.** The earlier focused runs overlap this result and are not counted again. This is selected unit/regression coverage, not the outstanding database-backed closure/accounting/concurrency validation.

Exact combined command:

```sh
./gradlew :fineract-savings:test :fineract-provider:test --tests '*AccountTransfer*Test' --tests '*StandingInstruction*Test' --tests '*LoanWritePlatformServiceJpaRepositoryImplTest' --tests '*Savings*AuthorityTest' --tests '*Savings*EnvelopeTest' --tests '*SavingsAnnualFee*Test' --tests '*CommandSourceServiceTest' --tests '*DepositAccountApplicableInterestRateTest' --tests '*FixedDepositAccountInterestCalculationServiceImplTest' --offline --no-daemon --no-parallel --max-workers=1 -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/account-transfer-tests.gradle -x :fineract-provider:resolve
```

Scoped formatting/check command (passed, with explicit source-input assertions):

```sh
./gradlew :fineract-core:spotlessJavaApply :fineract-provider:spotlessJavaApply :fineract-savings:spotlessJavaApply :fineract-core:spotlessJavaCheck :fineract-provider:spotlessJavaCheck :fineract-savings:spotlessJavaCheck --offline --no-daemon --no-parallel --max-workers=1 -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/account-transfer-spotless.gradle
```

`git diff --check`, equivalent whitespace checks for untracked Java/report files, and relative report-link checks passed. Changed-file scope contains only feature Java/tests and this report; no generated files or migrations. The production diff, new calculation/authority/policy code and test fixtures were reviewed. No commit, push or PR was made.

All test runs use `--offline --no-daemon --no-parallel --max-workers=1 -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/account-transfer-tests.gradle -x :fineract-provider:resolve`. The temporary init script limits test heap to 512 MiB and parallel forks to 1. Initial fixture failures (unused Mockito stub, nested Mockito stubbing, business-date map type and unsupported null cleanup) were corrected; no financial assertion failure remains in the completed runs.

The scoped Spotless init script uses explicit project file collections and asserts that every intended Java file is an input. An earlier absolute-string pattern invocation did not establish source coverage and is not relied upon as validation. No generated files, schema changes, commits, pushes or PRs are intended.

### Working-tree file inventory

- [SavingsTransactionCommandEnvelope.java](../../fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionCommandEnvelope.java)
- [SavingsTransactionKind.java](../../fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionKind.java)
- [CommandWrapperBuilder.java](../../fineract-core/src/main/java/org/apache/fineract/commands/service/CommandWrapperBuilder.java)
- [AccountTransfersApiResource.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/api/AccountTransfersApiResource.java)
- [StandingInstructionApiResource.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/api/StandingInstructionApiResource.java)
- [StandingInstructionDataValidator.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/data/StandingInstructionDataValidator.java)
- [AccountTransferStandingInstruction.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/domain/AccountTransferStandingInstruction.java)
- [StandingInstructionDuesPolicy.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/domain/StandingInstructionDuesPolicy.java)
- [StandingInstructionRepository.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/domain/StandingInstructionRepository.java)
- [CreateAccountTransferCommandHandler.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/handler/CreateAccountTransferCommandHandler.java)
- [CreateStandingInstructionCommandHandler.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/handler/CreateStandingInstructionCommandHandler.java)
- [RefundByTransferCommandHandler.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/handler/RefundByTransferCommandHandler.java)
- [UpdateStandingInstructionCommandHandler.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/handler/UpdateStandingInstructionCommandHandler.java)
- [ExecuteStandingInstructionsTasklet.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/jobs/executestandinginstructions/ExecuteStandingInstructionsTasklet.java)
- [AccountTransferAuthorityService.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/service/AccountTransferAuthorityService.java)
- [StandingInstructionWritePlatformServiceImpl.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/service/StandingInstructionWritePlatformServiceImpl.java)
- [LoanWritePlatformServiceJpaRepositoryImpl.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanWritePlatformServiceJpaRepositoryImpl.java)
- [FixedDepositAccount.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/FixedDepositAccount.java)
- [RecurringDepositAccount.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/RecurringDepositAccount.java)
- [AccountTransferEnvelopeTest.java](../../fineract-provider/src/test/java/org/apache/fineract/commands/service/AccountTransferEnvelopeTest.java)
- [StandingInstructionDuesPolicyTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/account/domain/StandingInstructionDuesPolicyTest.java)
- [AccountTransferAuthorityTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/account/service/AccountTransferAuthorityTest.java)
- [StandingInstructionAuthorityTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/account/service/StandingInstructionAuthorityTest.java)
- [LoanWritePlatformServiceJpaRepositoryImplTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/loanaccount/service/LoanWritePlatformServiceJpaRepositoryImplTest.java)
- `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/domain/DepositAccountApplicableInterestRateTest.java` (preserved in the Phase 2 patch)
- [SavingsDepositAuthorityTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsDepositAuthorityTest.java)
- [SavingsWithdrawalAuthorityTest.java](../../fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/handler/SavingsWithdrawalAuthorityTest.java)
- [SavingsAccount.java](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccount.java)
- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountBalanceProjection.java` (preserved in the Phase 2 patch)
- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountEndOfDayBalance.java` (preserved in the Phase 2 patch)
- `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountRunningBalance.java` (preserved in the Phase 2 patch)
- [SavingsAccountTransaction.java](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountTransaction.java)
- `fineract-savings/src/test/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountBalanceProjectionTest.java` (preserved in the Phase 2 patch)
- `fineract-savings/src/test/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountRunningBalanceTest.java` (preserved in the Phase 2 patch)
- This report.

### Rollout and eventual review text

Do not deploy as complete transfer-authority coverage while the alternative paths and closure plan remain unfinished. Once complete, coordinate all application nodes for new protected envelopes and cancel/resubmit affected historical pending commands; completed history remains readable. Grandfathered DUES exposure and outstanding customer-mandate evidence require explicit rollout documentation.

No commit or PR has been created. Eventual complete-feature commit: `git commit -S -m "feat(account): enforce original-maker transfer monetary authority"`. Eventual PR title: “Enforce original-maker monetary authority for staff account transfers”. Its description must describe the final implemented closure plan and actual covered paths and include the final validation results; the current partial patch cannot use a completion claim.

## Archived calculation-extraction checkpoint

The following status and test totals describe the earlier checkpoint only; the current status above supersedes them.

## Latest authorization and actual implementation status

The two-phase closure-calculation refactor is approved by the latest continuation. The earlier ordering stop below is historical and does not require renewed policy approval. The fixed-only standing-instruction decision and all prior transfer-authority requirements remain approved.

**The feature is not complete.** This working tree contains the first calculation extractions, not a functioning closure plan or new TRANSFER enforcement. No new architectural stop has been established in this continuation. Do not deploy this patch as the monetary-authority feature.

### Implemented calculation extractions

- `FixedDepositAccount` and `RecurringDepositAccount`: extracted `calculateApplicableInterestRate(date, premature)` from the existing rate method. It returns the chart/penalty-adjusted annual rate without assigning `nominalAnnualInterestRate`. The existing `getEffectiveInterestRateAsFraction` delegates to it, retains the assignment, and performs the original division with the original `MathContext`. There is one copy of the rate selection and penalty logic.
- `SavingsAccountRunningBalance`: extracted the existing per-transaction credit/debit/hold and overdraft formula into a value result. It receives monetary values and flags, with no entity, repository, accounting or event dependency. `SavingsAccount.recalculateDailyBalances` uses that result and retains the existing transaction mutation, reversal/replacement and end-of-day update sequence.
- Focused tests cover repeatability and unchanged input amounts, nine literal expected balance/overdraft cases, unchanged account rate on pure lookup, and the existing stored-rate update/fraction on execution. These prove the extracted pieces only. They do **not** prove that full interest or closure calculation is mutation-free.

### Authorized work still outstanding

The remaining closure calculation must represent interest postings/corrections, withholding-tax allocations, derived transaction balances, and exact net proceeds without touching managed entities. Existing `calculateInterestUsing` still recalculates transaction balances and updates summaries; `recalculateDailyBalances` can also reverse and replace historical transactions and charge associations. `copyTransaction` is a replacement-transaction constructor, not a safe calculation snapshot: it retains account references and does not preserve the original identifier/derived state. Do not use it as a shallow-copy shortcut.

Next implement the immutable closure result and shared calculation/application path, retaining existing monetary rounding and transaction ordering. Recalculate from current version-checked state on submission, approval and execution retry; authorize original-maker TRANSFER against exact net source-currency proceeds before applying any financial effects. The existing savings account has an optimistic version, but no closure-plan stale-state check has yet been implemented or tested. No altered locking or transaction boundaries are introduced by the current extraction.

Still required: original-maker context/envelope propagation and guards for direct, batch, actual import, cashier and alternative transfer paths; fixed-instruction setup authorization; new/pending DUES rejection and grandfathered lifecycle restrictions; history, spoofing, retry and approval isolation coverage; full closure interest/tax/net/accounting equivalence and concurrency tests. The approved behavior matrix and rollout requirements in the historical material below remain requirements, not delivered behavior.

### Exact changed files

1. [FixedDepositAccount.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/FixedDepositAccount.java)
2. [RecurringDepositAccount.java](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/RecurringDepositAccount.java)
3. [SavingsAccount.java](../../fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccount.java)
4. `fineract-savings/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountRunningBalance.java` (preserved in the Phase 2 patch) (new)
5. `fineract-provider/src/test/java/org/apache/fineract/portfolio/savings/domain/DepositAccountApplicableInterestRateTest.java` (preserved in the Phase 2 patch) (new)
6. `fineract-savings/src/test/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountRunningBalanceTest.java` (preserved in the Phase 2 patch) (new)
7. This report (existing untracked file, updated).

### Validation for this extraction

All Gradle commands used `--offline --no-daemon --no-parallel --max-workers=1 -Dorg.gradle.jvmargs=-Xmx768m`. `/tmp/account-transfer-tests.gradle` sets each test task's `maxHeapSize = '512m'` and `maxParallelForks = 1`; provider runs exclude `:fineract-provider:resolve`.

Successful initial validation: `:fineract-savings:test :fineract-provider:test --tests '*DepositAccountApplicableInterestRateTest' --tests '*FixedDepositAccountInterestCalculationServiceImplTest'` with the flags above and `-I /tmp/account-transfer-tests.gradle -x :fineract-provider:resolve`: **64 savings tests and 7 provider tests passed, zero failed/errors/skipped**. The `--tests` filters apply to the provider task in this invocation, so the savings module's full 64-test suite ran.

Scoped formatting: `:fineract-provider:spotlessJavaApply :fineract-savings:spotlessJavaApply :fineract-provider:spotlessJavaCheck :fineract-savings:spotlessJavaCheck` with the common flags and `-I /tmp/account-transfer-spotless.gradle`: passed. That init script targets only the six changed Java files, skips the `buildSrc` build, and changes no repository configuration.

Intermediate failures were corrected: one test fixture tried to stub a private date accessor; nine assertions compared `Money` object identity rather than monetary values; the first formatting init script did not skip `buildSrc`. These are not remaining test failures. No financial-equivalence result for the complete closure flow is claimed.

Final provider regression command: `./gradlew :fineract-provider:test --tests '*DepositAccountApplicableInterestRateTest' --tests '*FixedDepositAccountInterestCalculationServiceImplTest' --tests '*SavingsDepositAuthorityTest' --tests '*SavingsWithdrawalAuthorityTest' --tests '*SavingsDepositEnvelopeTest' --tests '*SavingsWithdrawalEnvelopeTest' --tests '*SavingsAnnualFee*Test' --tests '*CommandSourceServiceTest' --tests '*StandingInstruction*Test' --tests '*AccountTransfer*Test' --offline --no-daemon --no-parallel --max-workers=1 -Dorg.gradle.jvmargs=-Xmx768m -I /tmp/account-transfer-tests.gradle -x :fineract-provider:resolve`: passed, **154 tests, zero failures/errors/skipped**. Together with the savings module, the final selected validation covers **218 distinct tests passed, 0 failed, 0 skipped**; the earlier seven provider tests are included in the 154 and are not counted twice.

`git diff --check`, whitespace checks for untracked additions, local report-link validation, and review of all changed/new Java files passed. Full closure integration/accounting/concurrency tests and tests for the unimplemented transfer guards have not been completed. Existing tests passing does not demonstrate the new security policy is enforced.

No migration, commit, push or PR. Suggested eventual signed commit for these extractions alone: `git commit -S -m "refactor(savings): separate rate and running-balance calculations"`. Suggested PR title for this limited diff: “Extract deposit rate and savings balance calculations”. Description: “Separates existing rate lookup and transaction balance arithmetic from entity mutation without changing posting behavior. Adds purity/value fixtures and runs existing regressions. Full closure planning and account-transfer monetary authority remain outstanding.” This is not a suggested claim that the requested feature is complete.

## Historical closure-ordering report (refactor now approved)


## Current decision: fixed-only setup approved; deposit-closure ordering stop

The latest continuation resolves the variable-amount product decision. No renewed approval of the monetary policy is needed. Branch remains `feat/account-transfer-authority` at `f59d141e007fe17366cc138f339e3e13dd8235ab`. **No production enforcement has been installed.** The remaining stop below concerns the previously approved alternative-transfer scope, not DUES policy.

### Authoritative behavior matrix for implementation

| Operation | Approved behavior (not yet implemented) |
| --- | --- |
| Ordinary direct, batch, cashier/teller and actual indirect spreadsheet transfers | Original maker's current TRANSFER authority, full principal in source currency, inclusive bounds; preserve fees, permissions and supported conversion. |
| Fixed standing-instruction creation/material modification | Authorize before persistence, activation or approval queue; recheck original maker on approval. Scheduled execution remains internal after authorization. |
| New or pending unapproved DUES creation/modification | Reject with actionable domain error; cancel pending work and resubmit a new fixed instruction. Completed history stays readable. |
| Existing active unchanged DUES | Preserve trusted scheduled execution and existing dues rules; identify grandfathered execution in audit/logging without sensitive data. This does not certify authority or consent. |
| Existing DUES cancellation or suspension | Allow risk-reducing operations. |
| Existing DUES reactivation, resumption, clone or material change | Reject, including changes of instruction type. **DUES-to-FIXED conversion is rejected**; the earlier proposal below is superseded. |
| Administrator-triggered scheduler | Keep permissions; only normally due instructions, no client-selected internal origin, future targeting or due-date override. |
| Retry | Same occurrence and unchanged accounts/instruction; prevent duplicates. Successful command replay performs no new movement. |
| Customer mandate | Separate unresolved evidence and lifecycle requirement; neither staff permission nor monetary authority establishes consent. |

### Pre-write ordering limitation and required refactor

The earlier continuation explicitly requires: **“If any staff-selectable transfer path cannot carry a trusted original maker or cannot be checked before financial writes, stop and report it.”** This is the applicable stop instruction; the fixed-only continuation retains the alternative-transfer scope.

Manual fixed-deposit closure with `TRANSFER_TO_SAVINGS` currently has no reusable, side-effect-free calculation of the exact transferred principal before its financial mutations:

1. [DepositAccountWritePlatformServiceJpaRepositoryImpl.closeFDAccount](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/service/DepositAccountWritePlatformServiceJpaRepositoryImpl.java) persists payment details before loading the account and calling the closure domain service.
2. [DepositAccountDomainServiceJpa.handleFDAccountClosure](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/DepositAccountDomainServiceJpa.java) calls `postMaturityInterest` and changes the closure date before constructing the transfer using `account.getAccountBalance()` and the staff-selected destination.
3. [FixedDepositAccount.postMaturityInterest](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/FixedDepositAccount.java) adds/reverses interest transactions, applies withholding tax, recalculates daily balances and updates the summary. These are exactly the mutations that must follow authorization.
4. The same class's `calculatePreMatureAmount` preview computes balance minus posted interest plus earned interest; it does not perform the withholding-tax calculation used by posting. The stored `maturityAmount` is also not a demonstrated substitute for the final current balance. Neither may safely be assumed to be the actual principal.

Moving payment-detail persistence later is straightforward but does not solve principal calculation. A guard inside `transferFunds` is too late. Checking an estimate, posting then checking and relying on rollback, or excluding this manual path would violate the approved boundary. This is an existing calculation-interface limitation, **not a claim that a correct refactor is impossible**.

Recommended next implementation boundary: extract a shared read-only closure calculation that includes interest corrections and withholding tax, authorize its resulting transfer principal, then apply that exact calculation within the same transaction with account concurrency protection. Cover fixed/recurring normal and premature closure variants and preserve their existing accounting. Do not duplicate financial formulas or introduce a migration. That calculation refactor must precede claiming complete alternative-path enforcement; no narrower ordinary-transfer-only feature is represented as complete here.

### Other stop-condition checks and limitations

- FIXED runtime principal is not replaced with computed dues: the tasklet replaces it only for instruction type DUES. Dues-based recurrence alone does not make a FIXED instruction variable.
- ACTIVE and DISABLED statuses are distinct, so suspension and reactivation can be differentiated.
- The tasklet uses existing due selection and does not read request job parameters to select arbitrary instructions. Periodic dates use business date; the dues helper additionally compares the returned due date with tenant-local calendar date. This existing difference has not been changed or certified by runtime tests.
- Do not infer a crash gap merely because transfer and last-run SQL appear separately. [ExecuteStandingInstructionsConfig](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/jobs/executestandinginstructions/ExecuteStandingInstructionsConfig.java) gives the tasklet a transaction manager, and JDBC uses the shared routing datasource. Transfer methods participate in the transaction.
- A separate unresolved concurrency concern exists: [SchedularWritePlatformServiceJpaRepositoryImpl](../../fineract-provider/src/main/java/org/apache/fineract/infrastructure/jobs/service/SchedularWritePlatformServiceJpaRepositoryImpl.java) returns `false` from its job-lock retry fallback; [SchedulerVetoer](../../fineract-provider/src/main/java/org/apache/fineract/infrastructure/jobs/service/SchedulerVetoer.java) interprets that as permission to run. Different `run.id` values can create different batch instances. However, account optimistic versions and transaction isolation also protect writes. **No duplicate-payment reproduction was established**, and this concern is not presented as a confirmed idempotency stop. Integration tests must establish same-occurrence retry behavior before release.

### Mandatory rollout inventory and follow-ups

Before deployment, inventory active and suspended DUES instructions, pending DUES creation/modification commands, next execution dates, source accounts, destination loan accounts and recent failures. No tenant inventory was executed in this source-only review. Operational staff must review grandfathered arrangements for replacement by separately authorized fixed instructions or a future ceiling model. Mixed application versions are unsafe: older instances can still create or modify DUES and cannot enforce the new command-envelope policy. No production-data mutation script is supplied.

A separate future DUES ceiling design must specify per-execution maximum and currency, supplier and approver, customer-mandate evidence, effective/expiry dates, changes requiring renewed approval, over-ceiling behavior, partial payments, retry semantics, audit history, migration and maker-checker behavior. No placeholder ceiling or authorization Boolean is approved. The customer-mandate follow-up below remains required. DIRECT_DEBIT/DIRECT_CREDIT operation mapping remains separate; neither is an extra transfer-leg check.

### Changed files, validation and proposed delivery

Only `docs/changes/account-transfer-authority.md` changed; it remains untracked. Production behavior remains unchanged, including existing permission to create DUES. No migrations, production/test edits, commits, pushes or PRs.

Source checks used `rg`, targeted `sed`/`cat`, `git status -sb`, and inspection of the supplied continuation instructions. Report verification uses local-link checks and `git diff --no-index --check /dev/null docs/changes/account-transfer-authority.md` because ordinary `git diff` omits this untracked file; also run `git diff --check`. Reviewed the complete report addition. Tests: **0 executed, 0 passed, 0 failed, 0 skipped**. Required focused authority, maker-checker, audit, scheduler concurrency, deposit/withdrawal and financial regressions remain unrun. Gradle and scoped Java Spotless were not run at this pre-implementation stop; no Java files changed. This is not release validation.

For this report-only result, suggested signed commit: `git commit -S -m "docs(account): record fixed-only policy and closure ordering blocker"`. Suggested PR title: “Record transfer policy and deposit-closure authorization prerequisite”. Suggested description: “Records the approved fixed-only standing-instruction and grandfathered DUES policies, rollout inventory, and the existing closure calculation ordering that prevents a pre-mutation principal check. Documentation only; enforcement and runtime tests remain outstanding.” Do not use the eventual feature PR title until implementation and required tests are complete. Neither commit nor PR was created.

## Archived variable-amount stop (resolved by latest fixed-only decision)

The following policy questions and conversion proposal are historical. The current behavior matrix above takes precedence.


## Current status: revised standing-instruction policy approved; variable-amount stop reached

The latest continuation supersedes the earlier standing-instruction policy question. Creation and material modification now require the initiating maker's current TRANSFER authority, with original-maker approval/retry handling. Authorized scheduled execution remains internal and does not recheck staff authority. **Implementation is approved but stopped before production edits because DUES instructions have no enforceable configured maximum.** Branch and starting commit remain `feat/account-transfer-authority` at `f59d141e007fe17366cc138f339e3e13dd8235ab`.

### Approved standing-instruction boundary

- Authorize the complete fixed per-execution principal, or an enforceable configured ceiling for variable execution, in source-account currency with inclusive bounds before persistence, activation, financial writes or approval queuing.
- On creation/material-modification approval, re-evaluate the original creating/modifying maker's current authority. Preserve checker permissions and audit identity; no impersonation.
- Block unauthorized and pending-approval due-now instructions from scheduler execution. Creation and modification cannot expose unapproved state between requests.
- Scheduled execution uses server-controlled STANDING_INSTRUCTION provenance for the exact approved instruction, without dependency on an active staff maker or scheduler monetary limits.
- Material changes include amount, source/destination, currency-affecting configuration, frequency, recurrence/end/first-execution dates, activation and any increased exposure. Source/destination are not currently accepted by the update DTO; do not silently enable those fields. Treat unsupported changes as rejected, or require a separately authorized new instruction.
- No administrative-field exemption is finalized before implementation review: priority changes affect processing order under limited funds, and status/date changes affect execution. Do not assume such fields are harmless merely because they are not amounts.
- Scheduled retry exemption requires unchanged authorized accounts/amount or ceiling and duplicate prevention. Existing last-run-date behavior alone is not claimed to establish crash-safe occurrence idempotency.
- Authority is per execution; no invented daily/monthly/lifetime aggregate limit. Residual cumulative exposure can exceed a staff member's per-execution maximum over repeated authorized occurrences.
- All previously approved direct/batch/import/cashier/alternative transfer, fee, currency, envelope and historical-command requirements remain applicable.

### Confirmed mandatory stop: DUES has no reliable configured ceiling

The latest instruction says: **“Stop again if: standing instructions have no fixed amount or enforceable ceiling”**, and requires reporting a product decision for variable amounts without a reliable maximum.

Source evidence:

| Evidence | Finding |
| --- | --- |
| [StandingInstructionType](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/domain/StandingInstructionType.java) | Distinguishes FIXED (1) and DUES (2). DUES is a variable amount type, not merely a fixed amount with a due-date recurrence. |
| [StandingInstructionDataValidator](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/data/StandingInstructionDataValidator.java), validateForCreate | Requires non-null amount for FIXED; no configured maximum parameter for DUES. |
| [AccountTransferStandingInstruction](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/domain/AccountTransferStandingInstruction.java) | Stores nullable amount plus type/status/schedule; no separate authorized ceiling. |
| [StandingInstructionCreationRequest](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/data/request/StandingInstructionCreationRequest.java) / [StandingInstructionUpdatesRequest](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/data/request/StandingInstructionUpdatesRequest.java) | Expose amount and instructionType, not a configured maximum. |
| [ExecuteStandingInstructionsTasklet](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/jobs/executestandinginstructions/ExecuteStandingInstructionsTasklet.java), execute | Initializes transactionAmount from instruction amount, then for DUES replaces it with standingInstructionDuesData.totalDueAmount(). No minimum-with-ceiling or maximum comparison precedes transferFunds. |
| [StandingInstructionReadPlatformServiceImpl](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/service/StandingInstructionReadPlatformServiceImpl.java), retriveLoanDuesData | Retrieves then-current incomplete loan schedule dues through business date. This is a current amount, not an authorized future ceiling. |

A supplied DUES `amount` is therefore not an enforceable ceiling: runtime replaces it. Today's dues, the first payment, zero, original loan principal, or a guessed future balance cannot safely stand in for a maximum. FIXED with a dues-based recurrence must be distinguished from instructionType DUES; the former does not become variable solely because of its scheduling trigger.

No production fail-closed guard has been installed at this stop: existing application behavior remains unchanged. Implementing the safe rejection and associated authority context belongs to the resumed implementation once the product boundary below is resolved. This report does not imply existing unbounded instructions are already blocked.

### Product decision needed to resume

Recommended bounded scope: reject new DUES instructions and material modifications that leave an instruction as DUES with an actionable domain error; allow authorization of FIXED instructions and conversion from DUES to a valid FIXED amount; retain current runtime validation for existing scheduled DUES instructions as expressly required by the user. Document the grandfathered variable-exposure risk. This needs explicit confirmation of the product boundary, not renewed approval of the already approved monetary policy.

Alternative: specify a configured ceiling's storage, lifecycle, runtime enforcement, treatment of dues above the ceiling (reject versus partial payment), historical instructions and approval linkage. Do not repurpose `amount` or introduce a schema migration without explicit approval. No ceiling design is invented here.

### Customer mandate remains outstanding

Keep these controls separate:

1. Staff operational authorization: existing operation permissions and maker-checker.
2. Monetary authority: allowed principal/ceiling per execution in source currency.
3. Customer mandate/consent: independent customer authorization evidence; **neither of the above proves consent**.

High-priority follow-up: define mandate reference, evidence/signed document, consent channel, customer identity, approved source and destination, amount or maximum, frequency, effective/expiry dates, revocation, audit history, maker-checker requirements, and digital-consent integration. Define lifecycle, storage, migration and verification requirements before implementation. No placeholder mandate-verified Boolean or migration is introduced.

### Validation and scope of this continuation

Only `docs/changes/account-transfer-authority.md` is updated. All earlier trace/evidence is preserved below as history. No production/test edits, Gradle builds, migrations, commits, pushes or PRs. Test counts: 0 executed, 0 passed, 0 failed, 0 skipped; the required implementation suite remains unrun. Source review used targeted reads of the files linked above, `git status -sb`, and `git rev-parse HEAD`; final verification checks local links, whitespace and report-only working-tree scope.

When resumed, add DUES rejection/ceiling tests alongside all approved standing-instruction tests: fixed bounds, due-now authorization and approval isolation, original maker, reduced authority, material changes, runtime staff-independence, origin spoofing, unchanged idempotent retry and changed-amount/destination rejection. Preserve existing-instruction runtime behavior explicitly; do not claim consent verified.

## Archived prior implementation stop (superseded by the decision above)

The prior standing-instruction bypass decision below is historical. Creation/material-modification monetary authorization has now been approved; do not request that approval again. The active blocker is the variable-amount product boundary described above.

## Previous status: implementation approved, standing-instruction bypass stop

The continuation instructions approve implementation on `feat/account-transfer-authority`, subject to explicit stop conditions. Approval supersedes the pending-policy statements in the archived Phase 1 review below. The branch remains at `f59d141e007fe17366cc138f339e3e13dd8235ab`. Existing report content was preserved; no production implementation has started because the standing-instruction stop condition is met in source.

### Approved decisions

| Area | Approved policy |
| --- | --- |
| Economic scope | Cover every staff-triggered new discretionary account-to-account movement whose principal or destination staff controls, including paths outside CREATE_ACCOUNTTRANSFER; do not re-route unrelated commands artificially. |
| Authority | TRANSFER only for one atomic movement identifying both source and destination; no additional DIRECT_DEBIT, DIRECT_CREDIT, DEPOSITS or WITHDRAWALS limit on its legs. |
| Subject | Initial submitting maker; persisted CommandSource.maker on approval/retry, with current authority re-evaluated and checker permissions/audit identity preserved. |
| Amount | Full transfer principal, source-account currency, inclusive limits. Exclude separately validated/system-calculated fees; do not permit principal disguised as a fee. |
| Cross-currency | If supported, actual source principal after existing conversion and before writes; preserve conversion/rounding. No base-currency or destination authority check. Do not invent missing FX logic. |
| Imports/batch/teller | No exemption; original maker per imported transfer, trusted provenance, existing failure semantics; ordinary batch/cashier uses staff origin. |
| Standing instructions | Existing customer-mandate execution exempt; preserve permissions and maker-checker. Stop if immediate staff-created instructions offer an unrestricted monetary substitute. No mandate redesign is authorized. |
| Other exclusions | Genuine scheduled/system work, interest, charge collection, reversals, undo, settlement and corrections remain outside discretionary staff transfer authority; no client-selected exemption. |
| Retry/replay | Preserve trusted metadata and prevent duplicates. Successful replay does not initiate a new transfer. Approval/execution retries must retain original maker; materially changed transfers require new authorization. The distinction between execution retry rechecking current authority and exempt replay must remain explicit in tests. |
| Compatibility | Extend shared typed framework; preserve exact deposit/withdrawal stored formats and behavior; decode once; validators receive flat payload; no migration, impersonation or new ThreadLocal. |
| History/order | Untrusted pending commands fail closed with cancel/resubmit error; completed audit remains readable. Check before balance, transfer, payment/accounting writes and approval queuing. |

### Confirmed stop: staff-created due-now standing instruction

The user's continuation section 2 explicitly states: **“If staff can create and immediately execute an instruction as an unrestricted substitute for an ordinary transfer, stop and report that bypass.”**

The inspected path permits a transfer unrestricted by the proposed TRANSFER monetary limit; it is **not** unrestricted by all existing security or account controls:

1. A user with CREATE_STANDINGINSTRUCTION can submit `POST /v1/standinginstructions` choosing source and destination savings accounts, a positive fixed amount, ACTIVE status, periodic daily recurrence with interval 1, and `validFrom` equal to the current business date. Supply otherwise valid office/client/account fields. `StandingInstructionDataValidator.validateForCreate` validates these fields but has no maker monetary limit or independent customer-mandate verification. It requires a non-null validFrom, not a future date.
2. With maker-checker disabled for CREATE_STANDINGINSTRUCTION (a supported configuration), creation persists the active instruction immediately. With maker-checker enabled, normal approval is required; this finding does not bypass that approval. `StandingInstructionAssembler` creates the instruction directly from supplied fields; there is no separately verified customer authorization gate in this path.
3. `StandingInstructionReadPlatformServiceImpl.retrieveAll(status)` includes active instructions effective today, not expired, and not already run today. A newly created instruction qualifies. `DefaultScheduledDateGenerator.isDateFallsInSchedule` treats a daily schedule starting today as due (`0 % 1 == 0`).
4. Staff with EXECUTEJOB_SCHEDULER can request `POST /v1/jobs/{id}?command=executeJob` on a batch-manager-enabled node for the standing-instruction job. Existing scheduler command permissions/maker-checker still apply. ALL_FUNCTIONS is an alternative permission, but is not necessary to establish this path; the relevant permissions can be assigned without a TRANSFER monetary-authority row. Without manual job permission, the next scheduled run can execute the same instruction.
5. `ExecuteStandingInstructionsTasklet.execute` takes the chosen amount and accounts from the instruction, creates an AccountTransferDTO and calls `transferFunds` directly. It does not require CREATE_ACCOUNTTRANSFER, original-maker TRANSFER authority, or independent customer-mandate evidence. The ordinary financial/account eligibility/balance rules still apply.
6. Success records last_run_date, preventing another normal run that day; it does not prevent the first substitute transfer. Changing amount/effective schedule through UPDATE_STANDINGINSTRUCTION is another setup surface; no new authority is established there either.

This is a source-confirmed alternative route under the stated role/configuration conditions, not a claim that every deployed staff role has those permissions or a live exploit was executed. No tenant permissions or customer data were modified.

Evidence:

- `StandingInstructionApiResource.create` constructs CREATE_STANDINGINSTRUCTION.
- [StandingInstructionDataValidator](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/data/StandingInstructionDataValidator.java), `validateForCreate` / `validateForUpdate`: caller-controlled amount/status/effective dates and no mandate/monetary gate.
- [StandingInstructionAssembler](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/domain/StandingInstructionAssembler.java), `assembleStandingInstruction`: fields copied into the new instruction.
- [StandingInstructionWritePlatformServiceImpl](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/service/StandingInstructionWritePlatformServiceImpl.java), `create` / `update`: persist mandate from that request.
- [StandingInstructionReadPlatformServiceImpl](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/service/StandingInstructionReadPlatformServiceImpl.java), `retrieveAll(Integer)`: effective-date/last-run selection.
- [DefaultScheduledDateGenerator](../../fineract-loan/src/main/java/org/apache/fineract/portfolio/loanaccount/loanschedule/domain/DefaultScheduledDateGenerator.java), `isDateFallsInSchedule`: same-day daily schedule is due.
- [SchedulerJobApiResource](../../fineract-provider/src/main/java/org/apache/fineract/infrastructure/jobs/api/SchedulerJobApiResource.java), `executeJob`: batch-manager and job-permission guards.
- [ExecuteStandingInstructionsTasklet](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/jobs/executestandinginstructions/ExecuteStandingInstructionsTasklet.java), `execute` / `transferAmount`: direct internal transfer with instruction amount.
- `PortfolioCommandSourceWritePlatformServiceImpl.logCommandSource` and `CommandSourceService.validateMakerChecker`: ordinary permissions and configurable approval remain applicable.

### Decision needed to resume

Either explicitly accept the existing standing-instruction creation/update permissions and configured maker-checker approval as sufficient mandate authorization **including newly created due-now instructions**, superseding this stop condition, or authorize a separately scoped mandate-verification restriction that prevents discretionary substitution while preserving runtime exemption. The current instruction says not to redesign mandates, so no such restriction has been invented or implemented.

Other approved decisions do not need to be approved again. Further implementation-specific checks (source-debit/fee ordering, complete trusted-maker coverage, and exact stored-format compatibility) remain to be completed after this stop is resolved; this report does not claim they passed.

### Validation and changed files for this continuation

Only `docs/changes/account-transfer-authority.md` changed (the existing untracked Phase 1 report was preserved and extended). No production or test code changed; no migration, commit, push or PR. No Gradle run was appropriate after the mandatory pre-implementation stop. Test counts: **0 executed, 0 passed, 0 failed, 0 skipped**; required implementation regressions remain unrun, not passed or waived.

Read-only checks used `git status -sb`, `git rev-parse HEAD`, targeted `cat`/`sed` reads and `rg` searches of the evidence files. Final verification checks report links, `git diff --check`, and confirms no tracked-file changes. Suggested eventual signed commit command (not executed): `git commit -S -m "feat(account): enforce original-maker transfer monetary authority"`. Eventual PR title: “Enforce original-maker monetary authority for staff account transfers”. Eventual PR description must reflect implemented paths and actual test results; it would be inaccurate to present this report-only stop as a completed feature.

## Archived Phase 1 review (superseded policy questions)

The material below records the original trace and proposals. Its pending decisions and conditional recommendations are historical; the approved decisions and active stop above are authoritative.

## Status and starting point

Phase 1 completed on 2026-09-29. **Production implementation is not approved and has not started.** The stop conditions concerning staff-selectable excluded routes, unclear direct-debit/direct-credit boundaries, and cross-currency semantics apply. The proposals below are conditional; they do not establish new business policy.

- Starting branch: clean `dev`, fetched from `origin/dev` and synchronized with `git merge --ff-only origin/dev` (already up to date).
- Exact starting and reviewed commit: `f59d141e007fe17366cc138f339e3e13dd8235ab`.
- Design branch: `feat/account-transfer-authority`, created from that commit. It remained clean when this review resumed.
- Confirmed merged prerequisites: status filtering (`e09f7a9019`, merge `92508ab8a4`); annual-fee rejection (`dba542f480`, merge `cbc78c0a3c`); deposit authority (`44a99c3c33`, merge `e983ec9de8`); withdrawal authority (`61bab19136`, merge `f59d141e00`).
- Confirmed `SavingsTransactionCommandEnvelope.java`, `SavingsWithdrawalAuthorityService.java`, and `docs/changes/savings-withdrawal-authority.md` exist.
- Consulted [SECURITY.md](../../SECURITY.md). Findings concern authenticated staff operation boundaries, not infrastructure compromise.
- Only this report is created. No production/test edits, migration, commit, push, or PR. No Gradle build or runtime experiment was required for this source review.

## Decisions required before implementation

1. **Staff-selectable alternatives:** decide whether linked-account FD/RD activation, manual FD/RD closure/premature closure, active-loan refund by transfer, loan disbursement to savings/top-up, and guarantee recovery are covered by TRANSFER or governed by an explicitly separate policy. They cannot all be labeled system-generated merely because they call an internal service.
2. **Standing-instruction mandate:** execution is scheduled, but staff choose the accounts, amount, and effective schedule. Decide whether creation/update needs its own mandate authorization, including immediately due instructions and manually triggered jobs. A blanket scheduler exemption otherwise leaves an alternative economic route.
3. **DIRECT_DEBIT/DIRECT_CREDIT:** define their business operations and whether one transfer needs only TRANSFER. Source supplies enum labels, not a rule requiring combinations. Do not apply all three automatically.
4. **Currency:** approve same-currency-only enforcement and explicit early rejection of mismatched currencies, or specify a separate cross-currency design. Current paths are inconsistent; there is no discovered FX conversion contract to preserve or reuse for an authority calculation.
5. **Fees:** approve full requested transfer principal as the limit basis, explicitly excluding separately charged fees, or require total source debit. A principal limit is not a cap on total account reduction.

These are the user's requested stop conditions. No excluded staff-selectable route is accepted as safe by this report, and no cross-currency authority rule is implemented.

## Authority types and business boundaries

The exact Java type is `org.apache.fineract.nsimbi.userroles.domain.MonetaryAuthorityType`; exact values include **TRANSFER**, **DIRECT_DEBIT**, **DIRECT_CREDIT**, DEPOSITS, WITHDRAWALS, DISBURSEMENT, and JOURNAL_VOUCHERS. See [enum](../../fineract-provider/src/main/java/org/apache/fineract/nsimbi/userroles/domain/MonetaryAuthorityType.java).

[NsimbiMonetaryAuthorityPolicyService](../../fineract-provider/src/main/java/org/apache/fineract/nsimbi/userroles/service/NsimbiMonetaryAuthorityPolicyService.java) looks up a current row by user, authority type, and currency, then delegates to the authority entity's inclusive amount bounds. Missing inputs/rows fail closed. It is a reusable policy, not an operation classifier.

Repository searches of production code and documentation found DIRECT_DEBIT and DIRECT_CREDIT as enum categories, without corresponding named operation handlers or documented business mappings. No separate debit-maker and credit-maker is supplied by an ordinary transfer: one initiating command causes both legs. This does **not** prove the two categories are synonyms for transfer legs. Proposed ordinary policy is TRANSFER alone, pending explicit confirmation. Preserve the previously approved DEPOSITS/WITHDRAWALS transfer exemptions.

## Entry-point matrix

References to `create`, `transferFunds`, `refundByTransfer`, `repayLoanWithTopup`, and `undo` below are methods of [AccountTransfersWritePlatformServiceImpl](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/service/AccountTransfersWritePlatformServiceImpl.java).

| Entry point / economic operation | Construction and execution | Staff-selectable or system-controlled | Proposed disposition |
| --- | --- | --- | --- |
| Direct `POST /v1/accounttransfers` | `AccountTransfersApiResource.create` -> builder -> CREATE_ACCOUNTTRANSFER -> handler -> `create` | Staff selects accounts, date, amount, offices and clients | Enforce original maker's TRANSFER |
| Batch `POST accounttransfers` | `CreateAccountTransferCommandStrategy.execute` builds CREATE_ACCOUNTTRANSFER directly from raw JSON; does not call the typed REST method | Ordinary staff batch | Same enforcement; assign origin in this separate construction path |
| Savings -> savings | `create`, savings withdrawal then savings deposit | Direct/batch staff, or DTO callers below | Cover direct/batch; classify DTO callers individually |
| Savings -> loan | `create`, withdrawal then loan repayment | Direct/batch staff; also schedules/charges/recovery | Same principal policy for ordinary requests; do not blanket-enroll all repayment services |
| Loan -> savings, ordinary transfer | `create`, loan refund then savings deposit | Direct/batch staff; loan refund conditions still apply | Cover as ordinary transfer; source loan currency, subject to currency decision |
| Active-loan refund -> savings | `POST /v1/accounttransfers/refundByTransfer` -> REFUNDBYTRANSFER_ACCOUNTTRANSFER -> `refundByTransfer` | Staff chooses source/destination/amount; bounded by amount paid in advance | New transfer, not reversal; propose inclusion, decision required |
| Loan -> loan | No ordinary `create` branch; `transferFunds` rejects unsupported combinations. `repayLoanWithTopup` implements a distinct loan top-up flow | Staff loan disbursement/top-up, not generic account-transfer API | Separate loan-policy decision; do not advertise ordinary loan-to-loan support |
| Dedicated spreadsheet account transfer | No GlobalEntityType, transfer importer, or ordinary transfer command builder caller discovered | Not present in inspected checkout | Do not invent importer or exemption |
| Loan spreadsheet import | `LoanImportHandler` uses `disburseLoanToSavingsApplication` when linked account supplied; loan service -> `transferFunds` | Original import submitter initiates loan operation | Indirect transfer route; scope must follow disbursement decision |
| FD/RD spreadsheet imports | `FixedDepositImportHandler` can activate and close; `RecurringDepositImportHandler` can activate; same deposit services as staff APIs | Import submitter initiates operations | Indirect routes; no independently proven authority-bypass entitlement |
| Teller/cashier via ordinary API/batch | Same ordinary command; request has no trusted cashier-origin classification | Staff | No role/status exemption; retain existing cash controls |
| Standing instruction setup | `StandingInstructionApiResource` -> CREATE/UPDATE/DELETE_STANDINGINSTRUCTION; stores mandate/details, no immediate `transferFunds` in create | Staff chooses mandate | Decide mandate authorization separately |
| Standing/recurring execution | `ExecuteStandingInstructionsTasklet` -> DTO -> `transferFunds`; fixed amount or computed loan dues | Scheduled execution of staff-configured mandate | Conditional exclusion only after mandate-risk decision |
| Manual scheduled-job trigger | `SchedulerJobApiResource.executeJob`: batch-manager mode plus ALL_FUNCTIONS or EXECUTEJOB_SCHEDULER; existing command processing | Staff with job permission can request run | Not an ordinary transfer endpoint, but immediate-due mandate route must be considered |
| FD/RD linked-account activation | `activateFDAccount` / `activateRDAccount` in deposit write service -> savings-to-savings DTO -> `transferFunds` | Staff activation, also import | Stop: same source-debit mechanism outside CREATE_ACCOUNTTRANSFER |
| Manual FD/RD close/premature close | Deposit domain service reads `onAccountClosureId`, `toSavingsAccountId`, transfer description; TRANSFER_TO_SAVINGS -> DTO -> `transferFunds` | Staff-selectable destination and close action | Stop: cannot assume internal settlement exemption |
| Automated FD maturity instructions | `UpdateDepositsAccountMaturityDetailsTasklet` -> `updateMaturityDetails` -> `handleFDAccountMaturityClosure` -> DTO | Scheduled execution of stored maturity instructions | Distinguish from manual closure; mandate/exemption decision |
| Deposit interest payout | `TransferInterestToSavingsTasklet` -> DTOs from deposit read service -> `transferFunds`; INTEREST_TRANSFER type | Computed interest and configured linked account | Normally exclude interest processing; preserve setup/trigger constraints |
| Loan disbursement to savings | DISBURSETOSAVINGS_LOAN -> loan write service -> `disburseLoanToSavings` -> `transferFunds` | Staff-selectable; linked destination | Separate DISBURSEMENT/TRANSFER policy boundary required |
| Loan top-up | Loan disbursement -> `disburseLoanToLoan` -> `repayLoanWithTopup` | Staff loan workflow | Separate loan-policy boundary required |
| Loan disbursement down-payment/charge legs; bulk disbursal | `LoanWritePlatformServiceJpaRepositoryImpl` constructs DTOs at disbursement and bulk-disbursement paths | Internal legs of staff-triggered loan operation | Do not add double authorization without approved rule |
| Loan charge payment | `LoanChargeWritePlatformServiceImpl.payLoanCharge` -> linked savings-to-loan DTO -> `transferFunds` | Staff charge payment constrained by obligation | Conditional charge-domain exclusion; different from arbitrary transfer |
| Scheduled loan charge collection | `TransferFeeChargeForLoansTasklet` -> DTO -> `transferFunds` | Job-generated obligation settlement | Normally excluded, retain charge constraints |
| Guarantor recovery | RECOVERGUARANTEES_LOAN -> `recoverFromGuarantor` -> `GuarantorDomainServiceImpl.transferFundsFromGuarantor` -> DTOs -> `transferFunds` | Staff-triggered recovery constrained by guarantees/loan | Explicit policy decision; not automatically system-only |
| Ordinary savings close with balance payout | Savings `close` -> savings domain withdrawal, no account-transfer DTO | Staff closure | Existing WITHDRAWALS policy; not a destination-account transfer |
| Client/branch/group transfer | Client lifecycle transfer service and loan/savings office-transfer bookkeeping | Staff organizational relocation | Distinct from customer-to-customer funds transfer; preserve current controls |
| Undo/reversal/correction | `undo`, reverse-transfer methods, loan/savings transaction-reference replacement services | Existing transaction IDs, no fresh caller-selected destination | Normally exempt true reversals/reference corrections; verify reversal-plus-new transfer is newly authorized |
| Cross-office money transfer | Ordinary accounts/offices/clients fields and same execution services; no same-office requirement in transfer detail validator | Staff | Same TRANSFER rule; retain visibility and accounting restrictions |

Production DTO caller inventory: deposit domain closure (FD manual/maturity, RD manual, FD/RD premature); deposit activation (FD/RD); deposit-interest tasklet; standing-instruction tasklet; loan write service (disbursement/down-payment/charge/bulk-disbursement and linked-savings disbursement); loan top-up; guarantor recovery; loan charge payment; scheduled loan-fee tasklet. There is no discovered general public `transferFunds` endpoint. That internal method nevertheless receives both staff-triggered and job-triggered work.

## Exact command and handler matrix

| Permission / action-entity pair | Handler or family | Financial meaning |
| --- | --- | --- |
| CREATE_ACCOUNTTRANSFER | `CreateAccountTransferCommandHandler` | Ordinary new transfer |
| REFUNDBYTRANSFER_ACCOUNTTRANSFER | `RefundByTransferCommandHandler` | Active-loan refund paid into savings; new movement |
| UNDO_ACCOUNTTRANSFER | `UndoAccountTransferCommandHandler` | Reverse referenced transfer |
| CREATE_STANDINGINSTRUCTION | `CreateStandingInstructionCommandHandler` | Create mandate |
| UPDATE_STANDINGINSTRUCTION | `UpdateStandingInstructionCommandHandler` | Change mandate |
| DELETE_STANDINGINSTRUCTION | `DeleteStandingInstructionCommandHandler` | Remove mandate |
| ACTIVATE_FIXEDDEPOSITACCOUNT / ACTIVATE_RECURRINGDEPOSITACCOUNT | `ActivateFixedDepositAccountCommandHandler` / `ActivateRecurringDepositAccountCommandHandler` | May fund from linked savings |
| CLOSE_FIXEDDEPOSITACCOUNT / CLOSE_RECURRINGDEPOSITACCOUNT | `CloseFixedDepositAccountCommandHandler` / `CloseRecurringDepositAccountCommandHandler` | May pay balance into selected savings |
| PREMATURECLOSE_FIXEDDEPOSITACCOUNT / PREMATURECLOSE_RECURRINGDEPOSITACCOUNT | Corresponding premature-close handlers | May pay balance into selected savings |
| DISBURSETOSAVINGS_LOAN | `DisburseLoanToSavingsCommandHandler` | Loan disbursement into linked savings |
| RECOVERGUARANTEES_LOAN | `RecoverFromGuarantorCommandHandler` | Guarantee-backed savings debit / loan repayment |
| PROPOSETRANSFER_CLIENT, PROPOSEANDACCEPTTRANSFER_CLIENT, ACCEPTTRANSFER_CLIENT, WITHDRAWTRANSFER_CLIENT, REJECTTRANSFER_CLIENT | Corresponding client transfer handlers | Move client/portfolio responsibility between offices |
| TRANSFERCLIENTS_GROUP | `TransferClientsBetweenGroupsCommandHandler` | Group membership relocation |

There is no discovered UPDATE_ACCOUNTTRANSFER command, ordinary transfer modification endpoint, or separate transfer-specific approval command. Approval/rejection/deletion use the generic maker-checker APIs. Do not confuse cancellation of an unexecuted audit command with financial undo.

## Maker-checker, persisted identity, retry and replay

Evidence: [SynchronousCommandProcessingService](../../fineract-core/src/main/java/org/apache/fineract/commands/service/SynchronousCommandProcessingService.java), [CommandSourceService](../../fineract-core/src/main/java/org/apache/fineract/commands/service/CommandSourceService.java), [PortfolioCommandSourceWritePlatformServiceImpl](../../fineract-core/src/main/java/org/apache/fineract/commands/service/PortfolioCommandSourceWritePlatformServiceImpl.java), [CommandSource](../../fineract-core/src/main/java/org/apache/fineract/commands/domain/CommandSource.java).

1. Submission passes ordinary command permissions, resolves an idempotency key, obtains the authenticated user, and builds/persists CommandSource with that user as `maker` (`maker_id`). Initial audit persistence may occur before monetary authorization; the required boundary is before financial writes and AWAITING_APPROVAL, not before all audit writes.
2. `processCommandAndSaveResult` calls the handler **before** `validateMakerChecker`. Successful unapproved execution then marks awaiting approval and throws `RollbackTransactionNotApprovedException`; transaction rollback removes financial work, and the framework persists the pending command status. Put the monetary guard before any financial operation so denial never queues.
3. `approveEntry` loads the stored command, verifies awaiting status and checker permissions/same-maker restrictions, rehydrates action/entity/IDs/JSON, and executes with approval=true. Security context remains the checker. `CommandSource.maker` remains the original maker.
4. Current ordinary transfer handlers receive only JsonCommand; downstream `authenticatedUser()` on approval sees the checker. Never use that current user for the proposed maker monetary limit. Pass a typed immutable execution context carrying persisted maker explicitly, as the merged savings framework does.
5. Internal retry reloads CommandSource using COMMAND_SOURCE_ID and its original idempotency key. Re-evaluate the original maker's current policy row on every actual execution. Use persisted business payload/origin as the authority source; replacement request bodies/headers must not change it.
6. PROCESSED replay returns the existing idempotent-success result/exception path without executing financial work; do not require new metadata or reauthorize completed history. ERROR with no retry context returns the existing failed-command path. Do not turn every failed HTTP replay into an untracked new transfer.
7. Generic reject/delete validates maker-checker permission and operates on pending command state without executing the financial handler. Retain this route for cancellation/resubmission of untrusted legacy commands.
8. Scheduled DTO transfers do not have a CREATE_ACCOUNTTRANSFER CommandSource maker. Do not fabricate one from the scheduler's current principal; mandate authorization, if approved, needs a separate explicit design.

Approval tests must prove reduced/removed/invalid maker limits reject even when checker has a larger limit or checker-superuser privileges, while checker permissions and audit attribution remain unchanged.

## Amount, currency, fees and cross-currency findings

Conditional same-currency proposal: original staff maker, current TRANSFER authority, **full requested `transferAmount`**, actual source account currency, inclusive minimum/maximum. Read account currency from the loaded source savings or loan, never from caller-supplied office/client IDs or a destination template. Both account types and their currencies must be resolved before payment-detail persistence.

Actual implementation findings:

- `create` uses the same BigDecimal for withdrawal/refund and deposit/repayment. No converted destination amount, rate, or base-currency conversion was discovered in the write service or request DTO.
- Direct savings-to-savings explicitly throws `DifferentCurrenciesException`, but only **after** withdrawal and deposit calls, within the transaction. Do not interpret this late guard as permission to perform pre-authorization writes.
- Direct savings-to-loan, loan-to-savings, `refundByTransfer`, DTO `transferFunds`, and top-up do not have an equivalent pair-currency check in the reviewed transfer service/assemblers. Template selection filters by currency, but handcrafted requests and internal DTOs bypass template selection. Source review cannot certify all cross-currency attempts are rejected downstream; do not describe cross-currency as uniformly supported or uniformly prohibited.
- Ordinary JSON loan-to-savings transfer history is assembled using destination savings currency (`AccountTransferAssembler.assembleLoanToSavingsTransfer(JsonCommand,...)`), whereas the DTO overload uses source loan currency. This reinforces the need to authorize against the source account, not persisted display currency.
- Savings debit paths use `isWithdrawalFeeApplicableForTransfer`. Fees can increase source debit above the requested principal. Existing `AccountTransferWithdrawalFeeTest` explicitly documents 30,000 - 15,000 transfer - 100 fee. Payment-type-dependent fee behavior also exists.
- FD/RD activation can fund opening balance **plus activation charges**. Closure uses calculated balance/interest rather than arbitrary API `transferAmount`. Those paths need their own amount extraction before mutation if included.

No cross-currency rule is proposed as final. Safest initial option for approval: explicitly reject mismatched currencies before any write and retain no FX feature. If FX is desired, choose source debit only, both currency values, or configured base equivalent; define rates, rounding, fees and retry-time valuation first. That is a separate business decision and may change current permissive behavior.

## Import, teller, schedule and system boundaries

[GlobalEntityType](../../fineract-core/src/main/java/org/apache/fineract/infrastructure/bulkimport/data/GlobalEntityType.java) has no account-transfer entity. Dedicated ordinary transfer imports are not present. Loan/FD/RD importers do create commands whose domain operations can transfer funds, as listed above. The inspected upload/resource/workbook paths do not establish a separate monetary entitlement authorizing unlimited imported transfers. Ordinary per-command permissions are not proof of an independent import-limit exemption.

Existing bulk import captures submitting user/context and restores execution context via the event listener/security-aware executor. If an indirect import route is included, assign provenance server-side at its real construction point and preserve the original submitter through its parent command; do not infer import origin during approval. A future dedicated importer would require per-row original-maker TRANSFER checks and row-level success/error reporting.

Teller API searches found no separate ordinary account-transfer construction path. A cashier calling the normal endpoint is ordinary STAFF_API. Neither cashier roles, request headers nor cash-drawer state establish an exemption.

Standing instruction creation saves a mandate, not an immediate transfer. The execution task selects active/effective instructions not already run for that business date; determines periodic due dates or loan dues; calls `transferFunds`; updates `last_run_date` only on success and writes success/failure history. Failed executions can be reconsidered on later runs. This is not the command envelope/idempotency retry path. A due-today mandate plus EXECUTEJOB_SCHEDULER (or waiting for a job) can result in a transfer without CREATE_ACCOUNTTRANSFER authority. Existing scheduling checks do not establish a separate monetary mandate limit.

Interest transfer, maturity handling and charge jobs derive amounts and linked destinations from stored state. Their execution can normally remain outside ordinary staff transfer checks, but staff-controlled setup and manual equivalent actions must be resolved first. Do not introduce a generic SYSTEM_INTERNAL label that any staff-triggered caller can use to bypass authorization.

## Reversals, corrections and organizational transfers

`undo` loads existing AccountTransferDetails, rejects an already reversed transfer, and operates on referenced legs. Savings-to-savings undoes both legs; savings-to-loan undoes savings and adjusts the referenced loan transaction to zero. Loan-to-savings undo explicitly throws unsupported-operation. Reverse methods used by loan lifecycle undo operate on existing transactions and mark transfers reversed. These are not arbitrary new destination transfers and should retain their existing permissions without fresh TRANSFER authority.

Savings transaction adjustment rejects account-transfer transactions. `SavingsAccountTransfersServiceImpl` and `LoanAccountTransfersServiceImpl` maintain transfer references during transaction replacement/correction; they are not ordinary transfer entry points. A new transfer submitted after undo is a new authorized operation, even when intended as a correction.

Client office/group transfers are organizational moves handled by `TransferWritePlatformServiceJpaRepositoryImpl`; they may generate account transfer-in/out bookkeeping but do not take an arbitrary customer destination-account transfer amount. Preserve their own lifecycle permissions, visibility and accounting. Do not enroll them just because the command name contains TRANSFER.

## Financial-write ordering and visibility

`AccountTransfersWritePlatformServiceImpl.create` and `refundByTransfer` persist payment details before invoking financial legs. `SavingsAccountDomainServiceJpa.handleWithdrawal` validates blocks and allowed withdrawal type, mutates the account, handles interest/balance/holds, saves transactions/account, and creates savings journals. Deposit and loan repayment/refund services also write financial data. Therefore checking after `create`, after `transferFunds`, or only before transfer-detail save is too late.

For ordinary requests, validate flat request/type, load both accounts using existing assembly paths, verify approved currency rules, authorize maker, **then** create payment details and execute the existing service behavior. Prefer an explicit-context service overload or a read-only preflight immediately before calling the existing write service, all within its transaction. Avoid duplicate balance/fee calculations. For manual deposit closure/activation, a guard merely inside `transferFunds` is too late for the stricter no-prior-mutation requirement: parent services already activate accounts/post maturity interest. Those parent operations require distinct preflight designs if included.

Transfer detail validation checks positive office/client/account IDs and account types, not a complete account-ownership or office-hierarchy authorization proof. The savings repository's `findOneWithNotFoundDetection` loads by ID and is not an authorization guard. Preserve all existing resource/template/assembler visibility restrictions; add cross-office and mismatched-account tests. This source review does not certify end-to-end object-level visibility security or change it silently.

Keep existing `isAccountTransfer` flags and journal processors intact. Savings transfer legs select transfer-specific clearing/control accounting; organizational account moves use transfer suspense bookkeeping. Do not apply the new policy indiscriminately to journal-entry creation or internal settlement.

## Trusted origin and shared-envelope proposal

Use the merged codec/dispatch architecture rather than a parallel transfer-only envelope. Current savings kinds are DEPOSIT, WITHDRAWAL, FORCE_WITHDRAWAL, ADJUSTTRANSACTION, CLOSE and GSIM_CLOSE; `fromCommand` excludes ACCOUNTTRANSFER, so transfers currently remain flat JSON.

Conditional minimal extension:

- Add a distinct typed account-transfer-create kind for `(CREATE, ACCOUNTTRANSFER)` and, if approved, a distinct refund-by-transfer kind. Do not add generic CREATE mapping for unrelated entities.
- STAFF_API is sufficient for existing ordinary direct/batch/cashier entry points. SPREADSHEET_IMPORT is needed only for actual included importer paths; do not mark every import trusted/exempt by default.
- STANDING_INSTRUCTION, SCHEDULED and SYSTEM_INTERNAL are candidate **internal execution classifications**, not automatically needed persisted command-envelope origins. Existing jobs bypass command dispatch. Do not add permissive persisted origins for routes that do not construct protected commands.
- Broaden naming through compatibility adapters or retain the existing public savings names while extending typed routing; preserve existing constructors/deposit adapters where practical. One shared codec, immutable explicit maker context, and kind/origin allowlist remain the essential design.
- Deposit stays exactly version 1 with `{version,origin}` metadata; existing savings version 2 remains exactly `{version,origin,kind}`. A new transfer kind can use the version-2 structure without rewriting existing rows or changing savings kind strings. Add transfer-specific errors without changing existing savings error contracts.
- Decode persisted envelope once for execution in CommandSourceService; pass flat payload to validators and financial services. Extend approval presentation and AuditReadPlatformServiceImpl unwrapping. Keep unrelated commands flat.
- REST AccountTransferRequest is a typed DTO serialized back to JSON; batch passes raw JSON. Reject reserved metadata at a boundary that sees the original request, including unknown-property handling, so DTO binding cannot silently mask a spoof attempt. Never use query/header/role values as origin.
- Strictly reject absent/null/unknown origin, wrong kind/action/entity, extra or malformed metadata, unsupported version and reserved nested payload metadata. Validate actual handler kind as well as stored kind.
- No ThreadLocal, mutable global state, security impersonation or migration. If extending scope requires new durable mandate identity or a schema change, stop for approval rather than improvising history inference.

## Historical pending and completed commands

Current CREATE_ACCOUNTTRANSFER and REFUNDBYTRANSFER_ACCOUNTTRANSFER audit records contain flat business JSON and `maker_id`, but no trusted origin. For every ultimately enrolled command, proposed policy is fail closed on execution/approval/retry with actionable cancellation/resubmission instructions. Do not infer provenance from payload, endpoint, permission, maker role or old audit entries. Retain generic rejection/deletion so legacy pending work can be cancelled.

Completed flat records remain readable; new envelopes display only business payload. Successful idempotent replay follows the existing result path without running a fresh guard. Coordinate rollout across application nodes because old nodes cannot interpret newly protected envelopes. Confirm the selected scope of refund and indirect parent commands before declaring which historical pending records require resubmission.

## Proposed changed files after approval

Minimum ordinary-transfer implementation candidates (none changed in Phase 1):

- `fineract-core/.../commands/domain/SavingsTransactionKind.java`, `SavingsTransactionOrigin.java`, `SavingsTransactionCommandEnvelope.java`, `SavingsTransactionExecutionContext.java`, `CommandWrapper.java` (only as required for compatibility/general naming).
- `fineract-core/.../commands/handler/SavingsTransactionCommandHandler.java` or a shared compatibility interface.
- `fineract-core/.../commands/service/CommandWrapperBuilder.java`, `CommandSourceService.java`, `PortfolioCommandSourceWritePlatformServiceImpl.java`.
- `fineract-provider/.../commands/service/AuditReadPlatformServiceImpl.java`.
- `fineract-provider/.../portfolio/account/api/AccountTransfersApiResource.java` and request binding boundary as needed.
- `fineract-provider/.../batch/command/internal/CreateAccountTransferCommandStrategy.java`.
- `fineract-provider/.../portfolio/account/handler/CreateAccountTransferCommandHandler.java`; `RefundByTransferCommandHandler.java` if included.
- `fineract-provider/.../portfolio/account/service/AccountTransfersWritePlatformService.java`, `AccountTransfersWritePlatformServiceImpl.java`; new `AccountTransferAuthorityService.java` using the existing Nsimbi policy.
- Focused tests under commands/service, portfolio/account/{api,handler,service}, and batch/command/internal; this design report updated with approved scope/results.

If indirect operations are included, additionally touch their parent handlers, deposit/loan services and actual import construction sites. If mandates are included, standing-instruction handlers/services require a separately approved authorization design. The final file list depends on these decisions; a core handler-only patch cannot claim universal staff-transfer coverage.

## Proposed tests and acceptance checks

1. Ordinary savings/savings, savings/loan and loan/savings: inclusive min/max, missing/null/invalid range, currency mismatch, full principal, source currency, permission retained; ordinary loan/loan rejected explicitly without writes if that correction is approved.
2. Before-denial ordering: no payment-detail save, balance mutation, withdrawal/deposit, refund/repayment, journal save or transfer-detail save; no AWAITING_APPROVAL. Cover maker-checker enabled/disabled and batch enclosing rollback.
3. Approval/retry: original maker retained, changed/removed limits rechecked, higher-limit checker cannot override, checker audit identity unchanged, stored payload wins over replacements, success replay does not reexecute.
4. Origin spoofing at raw REST binding, typed DTO, batch body, query and headers; kind/handler mismatch; missing maker; unsupported metadata; no inference during legacy approval/retry.
5. Byte-for-byte deposit v1 and withdrawal v2 compatibility; full deposit/withdrawal authority, import, audit and annual-fee regressions; unrelated command storage unchanged.
6. Fee-bearing transfers: principal and total debit distinguished, fee accounting/balance rules preserved; test approved fee semantics including payment-type-specific fees.
7. Currency matrix for every included direction and refund; mismatches rejected before writes if approved. No assumed exchange conversion. Cross-office visibility, client/account mismatch, blocked/held/lien/overdraft/date behavior unchanged.
8. RefundByTransfer is a new movement, not an exemption; existing paid-in-advance bound remains. True undo/reversal stays outside new-transfer guard; undo followed by new transfer invokes it.
9. For each approved indirect route: valid and denied activation, manual maturity/premature closure, linked disbursement, top-up, guarantees and charge cases as applicable. Verify guards precede parent mutation, not just DTO dispatch.
10. Standing instructions: mandate creation/update, due-now/manual-job route, successful last-run update, failed retry and scheduled job exclusion according to approved mandate policy. No scheduler principal substituted for original staff mandate identity.
11. Actual included imports: async original maker, per-row allowed/denied results, preserved progress/error output, trusted origin survives approval; do not claim a dedicated importer exists.
12. Integration validation with database rollback/journals and account concurrency, beyond mocked handler tests. No tests were run or claimed passing during this report-only phase.

## Evidence navigation

All links point to source at the reviewed commit; method names above identify relevant regions.

- [Direct API](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/api/AccountTransfersApiResource.java), [batch strategy](../../fineract-provider/src/main/java/org/apache/fineract/batch/command/internal/CreateAccountTransferCommandStrategy.java), [typed request](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/data/request/AccountTransferRequest.java).
- [Transfer writer](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/service/AccountTransfersWritePlatformServiceImpl.java), [detail validator](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/data/AccountTransfersDetailDataValidator.java), [transaction assembler](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/domain/AccountTransferAssembler.java).
- [Standing-instruction writer](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/service/StandingInstructionWritePlatformServiceImpl.java), [executor](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/account/jobs/executestandinginstructions/ExecuteStandingInstructionsTasklet.java), [scheduler API](../../fineract-provider/src/main/java/org/apache/fineract/infrastructure/jobs/api/SchedulerJobApiResource.java).
- [Deposit writer](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/service/DepositAccountWritePlatformServiceJpaRepositoryImpl.java), [deposit closure domain](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/DepositAccountDomainServiceJpa.java), [interest job](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/jobs/transferinteresttosavings/TransferInterestToSavingsTasklet.java).
- [Loan writer](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanWritePlatformServiceJpaRepositoryImpl.java), [loan charge writer](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/service/LoanChargeWritePlatformServiceImpl.java), [guarantor service](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/guarantor/service/GuarantorDomainServiceImpl.java), [loan charge job](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/loanaccount/jobs/transferfeechargeforloans/TransferFeeChargeForLoansTasklet.java).
- [FD importer](../../fineract-provider/src/main/java/org/apache/fineract/infrastructure/bulkimport/importhandler/fixeddeposits/FixedDepositImportHandler.java), [RD importer](../../fineract-provider/src/main/java/org/apache/fineract/infrastructure/bulkimport/importhandler/recurringdeposit/RecurringDepositImportHandler.java), [loan importer](../../fineract-provider/src/main/java/org/apache/fineract/infrastructure/bulkimport/importhandler/loan/LoanImportHandler.java).
- [Savings domain writes](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/savings/domain/SavingsAccountDomainServiceJpa.java), [fee integration-test source](../../integration-tests/src/test/java/org/apache/fineract/integrationtests/savings/AccountTransferWithdrawalFeeTest.java), [client transfer lifecycle](../../fineract-provider/src/main/java/org/apache/fineract/portfolio/transfer/service/TransferWritePlatformServiceJpaRepositoryImpl.java).
- [Shared codec](../../fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionCommandEnvelope.java), [kind routing](../../fineract-core/src/main/java/org/apache/fineract/commands/domain/SavingsTransactionKind.java), [audit display](../../fineract-provider/src/main/java/org/apache/fineract/commands/service/AuditReadPlatformServiceImpl.java).

## Review limitations and stop

This is a source-backed design inventory, not a runtime security certification. No database query inspected historical tenant rows; flat history follows the current persistence code. No cross-currency or permission exploit was executed. No independent import monetary entitlement or direct-debit/direct-credit rule was established. External extensions and deployment-specific permissions are outside the inspected checkout.

**Wait for explicit design approval and resolution of the five decisions above before implementation.** Phase 1 delivers the trace and reports the unsafe-to-assume exclusions; it does not silently choose the missing policy.
