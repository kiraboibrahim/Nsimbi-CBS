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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class DepositAccountClosurePlanTest {

    private static final LocalDate START = LocalDate.of(2025, 1, 1);
    private static final LocalDate CLOSE = START.plusDays(5);
    private final MonetaryCurrency currency = new MonetaryCurrency("USD", 2, 0);

    @AfterEach
    void reset() {
        ThreadLocalContextUtil.reset();
    }

    @ParameterizedTest
    @CsvSource({ "true,false", "true,true", "false,false", "false,true" })
    void purePlanMatchesLiteralInterestTaxNetAndExistingPosting(boolean fixed, boolean premature) {
        try (var ignored = moneyContext()) {
            var account = account(fixed);
            var before = snapshot(account);
            var plan = plan(account, premature);
            assertThat(snapshot(account)).isEqualTo(before);
            assertThat(plan.interestTotal()).isEqualByComparingTo("5.00");
            assertThat(plan.taxTotal()).isEqualByComparingTo("0.50");
            assertThat(plan.netProceeds()).isEqualByComparingTo("1004.50");
            var established = account(fixed);
            legacyPost(established, premature);
            apply(account, plan);
            assertThat(account.getAccountBalance()).isEqualByComparingTo(established.getAccountBalance());
            assertThat(snapshotTransactions(account)).isEqualTo(snapshotTransactions(established));
            assertThat(account.summary).usingRecursiveComparison().isEqualTo(established.summary);
            assertThrows(RuntimeException.class, () -> apply(account, plan));
        }
    }

    @ParameterizedTest
    @CsvSource({ "true,false", "true,true", "false,false", "false,true" })
    void changedTransactionOrVersionRejectsBeforeApplication(boolean fixed, boolean premature) {
        try (var ignored = moneyContext()) {
            var account = account(fixed);
            var plan = plan(account, premature);
            account.transactions.getFirst().setAmount(Money.of(currency, new BigDecimal("1001")));
            var changed = snapshot(account);
            assertThrows(RuntimeException.class, () -> apply(account, plan));
            assertThat(snapshot(account)).isEqualTo(changed);
            var fresh = plan(account, premature);
            ReflectionTestUtils.setField(account, "version", account.getVersion() + 1);
            assertThrows(RuntimeException.class, () -> apply(account, fresh));
        }
    }

    @ParameterizedTest
    @CsvSource({ "true,false,false", "true,true,false", "false,false,false", "false,true,false", "true,false,true", "true,true,true",
            "false,false,true", "false,true,true" })
    void existingInterestAndTaxCorrectionsMatchPosting(boolean fixed, boolean premature, boolean persisted) {
        try (var ignored = moneyContext()) {
            var account = account(fixed);
            var established = account(fixed);
            for (var a : List.of(account, established)) {
                var interest = SavingsAccountTransaction.interestPosting(a, null, CLOSE, Money.of(currency, new BigDecimal("10")), false);
                a.transactions.add(interest);
                var tax = SavingsAccountTransaction.withHoldTax(a, null, CLOSE, Money.of(currency, new BigDecimal("1.00")), Map.of());
                if (persisted) {
                    ReflectionTestUtils.setField(interest, "id", 9L);
                    ReflectionTestUtils.setField(tax, "id", 10L);
                }
                a.transactions.add(tax);
                a.summary.updateSummary(currency, a.savingsAccountTransactionSummaryWrapper, a.transactions);
            }
            var before = snapshot(account);
            var plan = plan(account, premature);
            assertThat(snapshot(account)).isEqualTo(before);
            legacyPost(established, premature);
            apply(account, plan);
            assertThat(account.getAccountBalance()).isEqualByComparingTo(established.getAccountBalance());
            assertThat(snapshotTransactions(account)).isEqualTo(snapshotTransactions(established));
        }
    }

    @ParameterizedTest
    @CsvSource({ "true,false", "true,true", "false,false", "false,true" })
    void appliedReceiptRejectsChangedAmountDateAndSecondWithdrawal(boolean fixed, boolean premature) {
        try (var ignored = moneyContext()) {
            var account = account(fixed);
            var plan = plan(account, premature);
            apply(account, plan);
            var receipt = plan.appliedTo(account, CLOSE, CLOSE);
            receipt.validate(account, plan.netProceeds(), CLOSE);
            assertThrows(RuntimeException.class, () -> receipt.validate(account, plan.netProceeds().add(BigDecimal.ONE), CLOSE));
            assertThrows(RuntimeException.class, () -> receipt.validate(account, plan.netProceeds(), CLOSE.plusDays(1)));
            account.transactions
                    .add(SavingsAccountTransaction.withdrawal(account, null, null, CLOSE, Money.of(currency, plan.netProceeds()), null));
            var before = snapshot(account);
            assertThrows(RuntimeException.class, () -> receipt.validate(account, plan.netProceeds(), CLOSE));
            assertThat(snapshot(account)).isEqualTo(before);
        }
    }

    private DepositAccountClosurePlan plan(SavingsAccount account, boolean premature) {
        return account instanceof FixedDepositAccount fd ? fd.planClosureInterest(CLOSE, premature, false, 1)
                : ((RecurringDepositAccount) account).planClosureInterest(CLOSE, premature, false, 1);
    }

    private void apply(SavingsAccount account, DepositAccountClosurePlan plan) {
        if (account instanceof FixedDepositAccount fd)
            fd.applyClosureInterest(plan, CLOSE);
        else
            ((RecurringDepositAccount) account).applyClosureInterest(plan, CLOSE);
    }

    private void legacyPost(SavingsAccount account, boolean premature) {
        if (account instanceof FixedDepositAccount fd) {
            if (premature)
                fd.postPreMaturityInterest(CLOSE, true, false, 1);
            else
                fd.postMaturityInterest(false, 1);
        } else {
            var rd = (RecurringDepositAccount) account;
            if (premature)
                rd.postPreMaturityInterest(CLOSE, true, false, 1, false);
            else
                rd.postMaturityInterest(false, 1, CLOSE, false);
        }
    }

    private SavingsAccount account(boolean fixed) {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, CLOSE)));
        SavingsAccount account = fixed ? spy(new FixedDepositAccount()) : spy(new RecurringDepositAccount());
        account.currency = currency;
        account.status = 300;
        if (fixed) doReturn(CLOSE).when((FixedDepositAccount) account).calculateMaturityDate();
        account.activatedOnDate = START;
        account.submittedOnDate = START;
        account.nominalAnnualInterestRate = new BigDecimal("36.5");
        account.nominalAnnualInterestRateOverdraft = BigDecimal.ZERO;
        account.interestCalculationType = 1;
        account.interestCalculationDaysInYearType = 365;
        account.interestCompoundingPeriodType = 7;
        account.interestPostingPeriodType = 4;
        account.summary = new SavingsAccountSummary();
        account.setHelpers(new SavingsAccountTransactionSummaryWrapper(),
                new SavingsHelper(mock(AccountTransfersReadPlatformService.class)), null);
        var term = mock(DepositAccountTermAndPreClosure.class);
        when(term.getMaturityDate()).thenReturn(CLOSE);
        when(term.depositAmount()).thenReturn(new BigDecimal("1000"));
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);
        if (!fixed) {
            var rd = (RecurringDepositAccount) account;
            doReturn(CLOSE).when(rd).calculateMaturityDate();
            var chart = mock(DepositAccountInterestRateChart.class);
            when(chart.getApplicableInterestRate(any(), any(), any(), any())).thenReturn(new BigDecimal("36.5"));
            ReflectionTestUtils.setField(rd, "chart", chart);
        }
        var component = mock(TaxComponent.class);
        when(component.getId()).thenReturn(1L);
        when(component.getApplicablePercentage(CLOSE)).thenReturn(new BigDecimal("10"));
        var group = mock(TaxGroup.class);
        when(group.getTaxGroupMappings()).thenReturn(Set.of(TaxGroupMappings.createTaxGroupMappings(component, START.minusDays(1))));
        ReflectionTestUtils.setField(account, "taxGroup", group);
        account.setWithHoldTax(true);
        var deposit = SavingsAccountTransaction.deposit(account, null, null, START, Money.of(currency, new BigDecimal("1000")), null);
        account.transactions.add(deposit);
        account.recalculateDailyBalances(Money.zero(currency), CLOSE, false, false);
        account.summary.updateSummary(currency, account.savingsAccountTransactionSummaryWrapper, account.transactions);
        return account;
    }

    private List<Object> snapshot(SavingsAccount account) {
        return Arrays.asList(account.nominalAnnualInterestRate, account.getAccountBalance(),
                account.summary.getLastInterestCalculationDate(), snapshotTransactions(account));
    }

    private List<?> snapshotTransactions(SavingsAccount account) {
        return account.transactions.stream()
                .map(t -> Arrays.asList(t.getTransactionType(), t.getTransactionDate(), t.getAmount(), t.isReversed(),
                        t.getRunningBalance(), t.getBalanceEndDate(), t.getBalanceNumberOfDays(), t.getCumulativeBalance(),
                        t.getTaxDetails().stream().map(d -> d.getAmount()).toList()))
                .toList();
    }

    private MockedStatic<MoneyHelper> moneyContext() {
        var helper = Mockito.mockStatic(MoneyHelper.class);
        helper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(16, RoundingMode.HALF_EVEN));
        helper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        return helper;
    }
}
