/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.service.TaxUtils;

/** Transaction-local value plan. Contains no entity references or mutable calculation periods. */
public final class DepositAccountClosurePlan {

    private record Interest(LocalDate date, BigDecimal amount, boolean userPosting, boolean replace) {
    }

    private record Tax(boolean existing, boolean replace, BigDecimal amount, Map<Long, BigDecimal> allocations) {

        private Tax {
            allocations = Map.copyOf(allocations);
        }
    }

    private record State(Long accountId, int version, List<Object> values) {
    }

    private record Posting(LocalDate date, BigDecimal amount, boolean overdraft) {
    }

    private final State state;
    private final List<Interest> interest;
    private final Tax tax;
    private final LocalDate postingDate;
    private final LocalDate calculationDate;
    private final BigDecimal rate;
    private final BigDecimal netProceeds;
    private final BigDecimal interestTotal;
    private final BigDecimal taxTotal;
    private final boolean premature;
    private final boolean fixed;
    private final String currency;
    private final BigDecimal summaryEarned;
    private final LocalDate businessDate;

    private DepositAccountClosurePlan(SavingsAccount account, List<Interest> interest, Tax tax, LocalDate postingDate,
            LocalDate calculationDate, BigDecimal rate, BigDecimal netProceeds, BigDecimal interestTotal, BigDecimal taxTotal,
            boolean premature, boolean fixed, BigDecimal summaryEarned) {
        this.summaryEarned = summaryEarned;
        this.businessDate = DateUtils.getBusinessLocalDate();
        this.state = snapshot(account, postingDate);
        this.interest = List.copyOf(interest);
        this.tax = tax;
        this.postingDate = postingDate;
        this.calculationDate = calculationDate;
        this.rate = rate;
        this.netProceeds = netProceeds;
        this.interestTotal = interestTotal;
        this.taxTotal = taxTotal;
        this.premature = premature;
        this.fixed = fixed;
        this.currency = account.getCurrency().getCode();
    }

    public BigDecimal netProceeds() {
        return netProceeds;
    }

    public BigDecimal interestTotal() {
        return interestTotal;
    }

    public BigDecimal taxTotal() {
        return taxTotal;
    }

    boolean premature() {
        return premature;
    }

    LocalDate postingDate() {
        return postingDate;
    }

    public String currency() {
        return currency;
    }

    static DepositAccountClosurePlan calculate(SavingsAccount account, List<PostingPeriod> periods, LocalDate postingDate,
            LocalDate calculationDate, BigDecimal rate, boolean premature, boolean fixed, Money alreadyPosted) {
        var currency = account.getCurrency();
        var wrapper = account.savingsAccountTransactionSummaryWrapper;
        var transactions = account.getTransactions();
        Money interestTotal = Money.of(currency, wrapper.calculateTotalInterestPosted(currency, transactions));
        Money overdraftTotal = Money.of(currency, wrapper.calculateTotalOverdraftInterest(currency, transactions));
        Money taxTotal = Money.of(currency, wrapper.calculateTotalWithholdTaxWithdrawal(currency, transactions));
        List<Interest> actions = new ArrayList<>();
        // Normal closure recalculates balances before choosing posting corrections. Project the same replacement order.
        Set<SavingsAccountTransaction> replaced = Collections.newSetFromMap(new IdentityHashMap<>());
        List<SavingsAccountTransaction> replacements = new ArrayList<>();
        if (!premature) {
            Money running = Money.zero(currency);
            for (var transaction : account.retrieveListOfTransactions()) {
                if (transaction.isReversed() || transaction.isReversalTransaction()) continue;
                var balance = SavingsAccountRunningBalance.calculate(running, transaction.getAmount(currency),
                        transaction.isCredit() || transaction.isAmountRelease(), transaction.isDebit() || transaction.isAmountOnHold(),
                        transaction.isAmountOnHold());
                running = balance.runningBalance();
                if (SavingsAccountBalanceProjection.requiresReplacement(account.calculatesClosureInterest(), transaction.getId(),
                        transaction.getOverdraftAmount(currency), balance.overdraftAmount(), transaction.isAccrual())) {
                    replaced.add(transaction);
                    replacements.add(transaction);
                }
            }
        }
        List<SavingsAccountTransaction> postingOrder = new ArrayList<>();
        for (var transaction : transactions)
            if (!replaced.contains(transaction)) postingOrder.add(transaction);
        postingOrder.addAll(replacements);
        List<Posting> postings = new ArrayList<>();
        for (var transaction : postingOrder) {
            if ((transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed())
                    && !transaction.isReversalTransaction()) {
                postings.add(new Posting(transaction.getTransactionDate(), transaction.getAmount(),
                        transaction.isOverdraftInterestAndNotReversed()));
            }
        }
        if (premature) {
            Money earned = Money.zero(currency);
            for (var period : periods)
                earned = earned.plus(period.getInterestEarned());
            Money remaining = earned.minus(alreadyPosted);
            if (!remaining.isZero()) {
                actions.add(new Interest(postingDate, remaining.getAmount(), false, false));
                interestTotal = interestTotal.plus(remaining);
            }
        } else {
            for (var period : periods) {
                LocalDate date = period.dateOfPostingTransaction().isAfter(postingDate) ? postingDate : period.dateOfPostingTransaction();
                Money amount = period.getInterestEarned();
                Posting previous = postings.stream().filter(p -> p.date().equals(date)).findFirst().orElse(null);
                if (previous == null || amount.getAmount().compareTo(previous.amount()) != 0) {
                    actions.add(new Interest(date, amount.getAmount(), period.isUserPosting(), previous != null));
                    if (previous != null) {
                        postings.remove(previous);
                        if (previous.overdraft())
                            overdraftTotal = overdraftTotal.minus(previous.amount());
                        else
                            interestTotal = interestTotal.minus(previous.amount());
                    }
                    interestTotal = interestTotal.plus(amount);
                    postings.add(new Posting(date, amount.getAmount(), false));
                }
            }
        }
        var existingTax = postingOrder.stream().filter(t -> t.isWithHoldTaxAndNotReversed() && t.occursOn(postingDate)).findFirst()
                .orElse(null);
        Tax tax = null;
        if (account.getTaxGroup() != null && interestTotal.isGreaterThanZero() && (existingTax != null || account.withHoldTax())) {
            Map<TaxComponent, BigDecimal> split = TaxUtils.splitTax(interestTotal.getAmount(), postingDate,
                    account.getTaxGroup().getTaxGroupMappings(), interestTotal.getAmount().scale());
            BigDecimal total = TaxUtils.totalTaxAmount(split);
            if (total.signum() > 0 && (existingTax == null || existingTax.getId() == null || replaced.contains(existingTax)
                    || total.compareTo(existingTax.getAmount()) != 0)) {
                Map<Long, BigDecimal> allocations = new LinkedHashMap<>();
                split.forEach((component, amount) -> allocations.put(component.getId(), amount));
                tax = new Tax(existingTax != null, existingTax != null && existingTax.getId() != null && !replaced.contains(existingTax),
                        Money.of(currency, total).getAmount(), allocations);
                taxTotal = taxTotal.minus(existingTax == null ? BigDecimal.ZERO : existingTax.getAmount()).plus(tax.amount());
            }
        }
        var summary = new SavingsAccountSummary();
        summary.updateSummary(currency, wrapper, transactions);
        BigDecimal net = summary.balanceWithPostings(currency, interestTotal.getAmount(), overdraftTotal.getAmount(), taxTotal.getAmount());
        BigDecimal summaryEarned = null;
        if (!premature && account.calculatesClosureInterest() || premature && !fixed) {
            summaryEarned = SavingsAccountSummary.calculatedInterestTotal(currency, periods);
        } else if (premature && fixed) {
            // Fixed premature calculation historically updates this summary before interest is calculated.
            summaryEarned = Money.zero(currency).getAmount();
        }
        return new DepositAccountClosurePlan(account, actions, tax, postingDate, calculationDate, rate, net, interestTotal.getAmount(),
                taxTotal.getAmount(), premature, fixed, summaryEarned);
    }

    public void validateCurrent(SavingsAccount account) {
        if (!state.equals(snapshot(account, postingDate))) {
            throw new GeneralPlatformDomainRuleException("error.msg.deposit.closure.plan.stale",
                    "The deposit changed after closure calculation. Retry to calculate and authorize current proceeds.");
        }
    }

    /** Applies only the amounts and allocations captured by calculation; never calls an interest or tax calculator. */
    void apply(SavingsAccount account) {
        validateCurrent(account);
        Map<TaxComponent, BigDecimal> allocations = new LinkedHashMap<>();
        if (tax != null) {
            for (var mapping : account.getTaxGroup().getTaxGroupMappings()) {
                var component = mapping.getTaxComponent();
                if (tax.allocations().containsKey(component.getId())) allocations.put(component, tax.allocations().get(component.getId()));
            }
            if (allocations.size() != tax.allocations().size()) throw new IllegalStateException("Closure tax components changed");
        }
        if (!premature) account.recalculateDailyBalances(Money.zero(account.getCurrency()), calculationDate, false, false);
        if (premature || summaryEarned != null) account.nominalAnnualInterestRate = rate;
        if (summaryEarned != null) account.summary.applyCalculatedInterestSummary(summaryEarned, businessDate);
        for (var action : interest) {
            if (action.replace()) account.findInterestPostingTransactionFor(action.date()).reverse();
            account.addTransaction(SavingsAccountTransaction.interestPosting(account, account.office(), action.date(),
                    Money.of(account.getCurrency(), action.amount()), action.userPosting()));
        }
        if (tax != null) {
            var existingTax = !tax.existing() ? null : account.findTransactionFor(postingDate, account.findWithHoldTransactions());
            if (existingTax != null && !tax.replace()) {
                existingTax.setAmount(Money.of(account.getCurrency(), tax.amount()));
                existingTax.getTaxDetails().clear();
                SavingsAccountTransaction.updateTaxDetails(allocations, existingTax);
            } else {
                if (existingTax != null) existingTax.reverse();
                account.addTransaction(SavingsAccountTransaction.withHoldTax(account, account.office(), postingDate,
                        Money.of(account.getCurrency(), tax.amount()), allocations));
            }
        }
        // RD historically recalculates for interest changes only; FD includes tax-only changes.
        if (!interest.isEmpty() || fixed && tax != null)
            account.recalculateDailyBalances(Money.zero(account.getCurrency()), postingDate, false, false);
        account.summary.updateSummary(account.getCurrency(), account.savingsAccountTransactionSummaryWrapper, account.getTransactions());
        if (account.getAccountBalance().compareTo(netProceeds) != 0) throw new IllegalStateException("Closure plan balance mismatch");
    }

    /** Receipt for the exact applied source state, carried only by the internal closure transfer call. */
    public static final class Applied {

        private final State state;
        private final BigDecimal amount;
        private final LocalDate closureDate;
        private final LocalDate balanceDate;
        private final LocalDate taxDate;

        private Applied(SavingsAccount account, BigDecimal amount, LocalDate closureDate, LocalDate balanceDate, LocalDate taxDate) {
            this.state = snapshot(account, taxDate);
            this.amount = amount;
            this.closureDate = closureDate;
            this.balanceDate = balanceDate;
            this.taxDate = taxDate;
        }

        public void validate(SavingsAccount account, BigDecimal amount, LocalDate date) {
            if (!state.equals(snapshot(account, taxDate)) || this.amount.compareTo(amount) != 0 || !closureDate.equals(date)) {
                throw new GeneralPlatformDomainRuleException("error.msg.deposit.closure.plan.stale",
                        "The applied closure plan no longer matches the transfer.");
            }
        }

        public void updateWithdrawalBalances(SavingsAccount account) {
            account.recalculateDailyBalances(Money.zero(account.getCurrency()), balanceDate, false, false);
            account.summary.updateSummary(account.getCurrency(), account.savingsAccountTransactionSummaryWrapper, account.transactions);
        }
    }

    public Applied appliedTo(SavingsAccount account, LocalDate closeDate, LocalDate withdrawalBalanceDate) {
        if (!Objects.equals(state.accountId(), account.getId()) || state.version() != account.getVersion()
                || account.getAccountBalance().compareTo(netProceeds) != 0) {
            throw new IllegalStateException("Closure plan has not been applied to this account");
        }
        return new Applied(account, netProceeds, closeDate, withdrawalBalanceDate, postingDate);
    }

    private static State snapshot(SavingsAccount account, LocalDate postingDate) {
        List<Object> values = new ArrayList<>(Arrays.asList(account.getCurrency().getCode(), account.getCurrency().getDigitsAfterDecimal(),
                account.nominalAnnualInterestRate, account.getAccountBalance(), account.getStatus(), account.closedOnDate,
                account.withHoldTax(), account.activatedOnDate, account.submittedOnDate, account.startInterestCalculationDate,
                account.interestCompoundingPeriodType, account.interestPostingPeriodType, account.interestCalculationType,
                account.interestCalculationDaysInYearType, account.nominalAnnualInterestRateOverdraft,
                account.minBalanceForInterestCalculation(), account.getOnHoldFunds(), account.getSavingsHoldAmount(),
                DateUtils.getBusinessLocalDate()));
        if (account.getTaxGroup() != null) {
            values.add(account.getTaxGroup().getId());
            values.add(account.getTaxGroup().getTaxGroupMappings().stream()
                    .map(mapping -> Collections.unmodifiableList(Arrays.asList(mapping.getTaxComponent().getId(), mapping.startDate(),
                            mapping.endDate(), mapping.getTaxComponent().getApplicablePercentage(postingDate))))
                    .collect(Collectors.toUnmodifiableSet()));
        }
        for (var transaction : account.getTransactions()) {
            values.add(Collections.unmodifiableList(Arrays.asList(transaction.getId(), transaction.getTransactionDate(),
                    transaction.getTransactionType(), transaction.getAmount(), transaction.isReversed(), transaction.getRunningBalance(),
                    transaction.getOverdraftAmount(account.getCurrency()).getAmount(), transaction.getCreatedDate(),
                    transaction.getBalanceEndDate(), transaction.getBalanceNumberOfDays(), transaction.getCumulativeBalance(),
                    transaction.getTaxDetails().stream().map(
                            detail -> Collections.unmodifiableList(Arrays.asList(detail.getTaxComponent().getId(), detail.getAmount())))
                            .collect(Collectors.toUnmodifiableSet()))));
        }
        return new State(account.getId(), account.getVersion(), Collections.unmodifiableList(values));
    }
}
