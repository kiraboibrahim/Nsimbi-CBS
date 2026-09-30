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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class SavingsAccountBalanceProjectionTest {

    private static final LocalDate START = LocalDate.of(2025, 1, 1);
    private static final LocalDate END = START.plusDays(4);
    private final MonetaryCurrency currency = new MonetaryCurrency("USD", 2, 0);

    @AfterEach
    void clearBusinessDate() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void projectionIsRepeatableAndDoesNotChangeTransactionsOrAccount() {
        try (var ignored = monetaryContext()) {
            var account = account();
            var deposit = SavingsAccountTransaction.deposit(account, null, null, START, money("100"), null);
            var withdrawal = SavingsAccountTransaction.withdrawal(account, null, null, START.plusDays(2), money("25"), null);
            account.transactions.addAll(List.of(deposit, withdrawal));
            var before = account.transactions.stream().map(this::snapshot).toList();
            var projected = SavingsAccountBalanceProjection.calculate(account.retrieveListOfTransactions(), Money.zero(currency), END, true,
                    false);
            var repeated = SavingsAccountBalanceProjection.calculate(account.retrieveListOfTransactions(), Money.zero(currency), END, true,
                    false);
            assertThat(projected).usingRecursiveComparison().isEqualTo(repeated);
            assertThat(account.transactions.stream().map(this::snapshot).toList()).isEqualTo(before);
            assertThat(account.nominalAnnualInterestRate).isEqualByComparingTo("5");
            assertThat(projected.get(0).getRunningBalance()).isEqualByComparingTo("100");
            assertThat(projected.get(0).getEndOfBalanceDate()).isEqualTo(START.plusDays(1));
            assertThat(projected.get(0).getBalanceNumberOfDays()).isEqualTo(2);
            assertThat(projected.get(1).getRunningBalance()).isEqualByComparingTo("75");
            assertThat(projected.get(1).getBalanceNumberOfDays()).isEqualTo(3);
            assertThrows(UnsupportedOperationException.class, projected::clear);

            account.recalculateDailyBalances(Money.zero(currency), END, false, false);
            assertThat(account.toSavingsAccountTransactionDetailsForPostingPeriodList()).usingRecursiveComparison().isEqualTo(projected);
            assertThat(deposit.getCumulativeBalance()).isEqualByComparingTo("200");
            assertThat(withdrawal.getCumulativeBalance()).isEqualByComparingTo("225");
        }
    }

    @Test
    void virtualReplacementMatchesExecutionOrderWithoutReversingOrCreatingTransactions() {
        try (var ignored = monetaryContext()) {
            var account = account();
            var deposit = persisted(SavingsAccountTransaction.deposit(account, null, null, START, money("100"), null), 1L);
            var withdrawal = persisted(SavingsAccountTransaction.withdrawal(account, null, null, START.plusDays(1), money("125"), null),
                    2L);
            withdrawal.setOverdraftAmount(money("10"));
            var laterDeposit = persisted(SavingsAccountTransaction.deposit(account, null, null, START.plusDays(1), money("20"), null), 3L);
            account.transactions.addAll(List.of(deposit, withdrawal, laterDeposit));
            var before = account.transactions.stream().map(this::snapshot).toList();
            var projected = SavingsAccountBalanceProjection.calculate(account.retrieveListOfTransactions(), Money.zero(currency), END, true,
                    false);
            assertThat(account.transactions.stream().map(this::snapshot).toList()).isEqualTo(before);
            assertThat(projected).extracting(value -> value.getId()).containsExactly(1L, 3L, null);
            assertThat(projected.get(1).getRunningBalance()).isEqualByComparingTo("-5");
            assertThat(projected.get(2).getRunningBalance()).isEqualByComparingTo("-25");

            account.recalculateDailyBalances(Money.zero(currency), END, false, false);
            assertThat(withdrawal.isReversed()).isTrue();
            assertThat(account.transactions).hasSize(4);
            assertThat(account.toSavingsAccountTransactionDetailsForPostingPeriodList()).usingRecursiveComparison().isEqualTo(projected);
        }
    }

    @Test
    void interestPreviewUsesExistingFormulaWithoutUpdatingManagedSummaryOrBalances() {
        try (var ignored = monetaryContext()) {
            var account = account();
            account.activatedOnDate = START;
            account.interestCalculationType = 1;
            account.interestCalculationDaysInYearType = 365;
            account.interestCompoundingPeriodType = 7;
            account.interestPostingPeriodType = 4;
            account.nominalAnnualInterestRate = new BigDecimal("36.5");
            account.nominalAnnualInterestRateOverdraft = BigDecimal.ZERO;
            account.summary = new SavingsAccountSummary();
            account.setHelpers(new SavingsAccountTransactionSummaryWrapper(),
                    new SavingsHelper(mock(AccountTransfersReadPlatformService.class)), null);
            var deposit = SavingsAccountTransaction.deposit(account, null, null, START, money("1000"), null);
            account.transactions.add(deposit);
            var before = snapshot(deposit);
            BigDecimal balanceBefore = account.summary.getAccountBalance();
            var preview = account.previewInterestUsing(MathContext.DECIMAL64, END, false, 1, new BigDecimal("36.5"));
            // 1000 * 36.5% / 365 * 5 days = 5.00; annual compounding has no intervening compound date.
            assertThat(preview).hasSize(1);
            assertThat(preview.getFirst().getInterestEarned().getAmount()).isEqualByComparingTo("5.00");
            assertThat(snapshot(deposit)).isEqualTo(before);
            assertThat(account.summary.getAccountBalance()).isEqualTo(balanceBefore);
            assertThat(account.summary.getLastInterestCalculationDate()).isNull();
            assertThat(account.nominalAnnualInterestRate).isEqualByComparingTo("36.5");

            var actual = account.calculateInterestUsing(MathContext.DECIMAL64, END, false, false, 1, null, false, false);
            assertThat(actual.getFirst().getInterestEarned().getAmount()).isEqualByComparingTo("5.00");
            assertThat(account.summary.getLastInterestCalculationDate()).isEqualTo(END);
        }
    }

    private SavingsAccount account() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, END)));
        var account = new SavingsAccount();
        account.currency = currency;
        account.nominalAnnualInterestRate = new BigDecimal("5");
        return account;
    }

    private SavingsAccountTransaction persisted(SavingsAccountTransaction transaction, long id) {
        var persisted = spy(transaction);
        doReturn(id).when(persisted).getId();
        doReturn(Optional.of(OffsetDateTime.parse("2025-01-01T00:00:00Z").plusSeconds(id))).when(persisted).getCreatedDate();
        return persisted;
    }

    private List<Object> snapshot(SavingsAccountTransaction transaction) {
        return java.util.Arrays.asList(transaction.getId(), transaction.getAmount(), transaction.isReversed(),
                transaction.getRunningBalance(), transaction.getOverdraftAmount(currency).getAmount(), transaction.getBalanceEndDate(),
                transaction.getBalanceNumberOfDays(), transaction.getCumulativeBalance(),
                transaction.getSavingsAccountChargesPaid().size());
    }

    private Money money(String value) {
        return Money.of(currency, new BigDecimal(value));
    }

    private MockedStatic<MoneyHelper> monetaryContext() {
        var helper = Mockito.mockStatic(MoneyHelper.class);
        helper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(16, RoundingMode.HALF_EVEN));
        helper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        return helper;
    }
}
