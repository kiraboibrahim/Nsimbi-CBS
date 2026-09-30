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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.domain.interest.SavingsAccountTransactionDetailsForPostingPeriod;

/** Read-only projection of the existing balance recalculation, including virtual replacement ordering. */
final class SavingsAccountBalanceProjection {

    private SavingsAccountBalanceProjection() {}

    private record Entry(Long id, Optional<OffsetDateTime> created, LocalDate date, BigDecimal amount, BigDecimal running, boolean deposit,
            boolean withdrawal, boolean charge, boolean dividend) {
    }

    static List<SavingsAccountTransactionDetailsForPostingPeriod> calculate(List<SavingsAccountTransaction> transactions,
            Money openingBalance, LocalDate calculationDate, boolean calculateInterest, boolean allowOverdraft) {
        MonetaryCurrency currency = openingBalance.getCurrency().copy();
        Money running = openingBalance;
        List<Entry> existing = new ArrayList<>();
        List<Entry> replacements = new ArrayList<>();
        for (var transaction : transactions) {
            if (transaction.isReversed() || transaction.isReversalTransaction()) {
                continue;
            }
            var balance = SavingsAccountRunningBalance.calculate(running, transaction.getAmount(currency),
                    transaction.isCredit() || transaction.isAmountRelease(), transaction.isDebit() || transaction.isAmountOnHold(),
                    transaction.isAmountOnHold());
            running = balance.runningBalance();
            if (transaction.isInterestPostingAndNotReversed() || transaction.isOverdraftInterestAndNotReversed()) {
                continue;
            }
            boolean replacement = requiresReplacement(calculateInterest, transaction.getId(), transaction.getOverdraftAmount(currency),
                    balance.overdraftAmount(), transaction.isAccrual());
            var entry = new Entry(replacement ? null : transaction.getId(), replacement ? Optional.empty() : transaction.getCreatedDate(),
                    transaction.getTransactionDate(), transaction.getAmount(), running.getAmount(), transaction.isDeposit(),
                    transaction.isWithdrawal(), transaction.isChargeTransactionAndNotReversed(),
                    transaction.isDividendPayoutAndNotReversed());
            (replacement ? replacements : existing).add(entry);
        }
        existing.addAll(replacements);
        existing.sort((left, right) -> {
            int comparison = DateUtils.compare(left.date(), right.date());
            if (comparison == 0) {
                comparison = DateUtils.compareWithNullsLast(left.created(), right.created());
            }
            if (comparison == 0 && left.id() != null && right.id() != null) {
                comparison = left.id().compareTo(right.id());
            }
            return comparison;
        });
        List<SavingsAccountTransactionDetailsForPostingPeriod> result = new ArrayList<>();
        LocalDate endDate = calculationDate;
        for (int index = existing.size() - 1; index >= 0; index--) {
            var entry = existing.get(index);
            var balance = SavingsAccountEndOfDayBalance.calculate(entry.date(), endDate, Money.of(currency, entry.running()));
            result.addFirst(new SavingsAccountTransactionDetailsForPostingPeriod(entry.id(), entry.date(), balance.endDate(),
                    entry.running(), entry.amount(), currency, balance.numberOfDays(), entry.deposit(), entry.withdrawal(), allowOverdraft,
                    entry.charge(), entry.dividend()));
            endDate = entry.date().minusDays(1);
        }
        return List.copyOf(result);
    }

    static boolean requiresReplacement(boolean calculateInterest, Long id, Money previousOverdraft, Money calculatedOverdraft,
            boolean accrual) {
        return calculateInterest && id != null && !previousOverdraft.isZero() && !MathUtil.isEqualTo(calculatedOverdraft, previousOverdraft)
                && !accrual;
    }
}
