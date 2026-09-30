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

import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.Money;

/** One transaction's balance effects, calculated without changing the transaction or its account. */
record SavingsAccountRunningBalance(Money runningBalance, Money overdraftAmount) {

    static SavingsAccountRunningBalance calculate(Money openingBalance, Money amount, boolean credit, boolean debit, boolean hold) {
        Money overdraftAmount = Money.zero(openingBalance.getCurrency());
        Money transactionAmount = Money.zero(openingBalance.getCurrency());
        if (credit) {
            if (openingBalance.isLessThanZero()) {
                Money diffAmount = amount.plus(openingBalance);
                if (diffAmount.isGreaterThanZero()) {
                    overdraftAmount = amount.minus(diffAmount);
                } else {
                    overdraftAmount = amount;
                }
            }
            transactionAmount = transactionAmount.plus(amount);
        } else if (debit) {
            if (openingBalance.isLessThanZero()) {
                overdraftAmount = amount;
            }
            transactionAmount = transactionAmount.minus(amount);
        }

        Money runningBalance = openingBalance.plus(transactionAmount);
        if (MathUtil.isEmpty(overdraftAmount) && runningBalance.isLessThanZero() && !hold) {
            overdraftAmount = runningBalance.negated();
        }
        return new SavingsAccountRunningBalance(runningBalance, overdraftAmount);
    }
}
