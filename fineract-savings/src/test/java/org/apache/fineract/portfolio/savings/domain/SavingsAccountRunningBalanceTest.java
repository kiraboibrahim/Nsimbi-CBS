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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class SavingsAccountRunningBalanceTest {

    @ParameterizedTest
    @CsvSource({ "100, 25, true, false, false, 125, 0", "100, 25, false, true, false, 75, 0", "10, 25, false, true, false, -15, 15",
            "-10, 25, false, true, false, -35, 25", "-10, 25, true, false, false, 15, 10", "-30, 25, true, false, false, -5, 25",
            "10, 25, false, true, true, -15, 0", "-10, 25, false, true, true, -35, 25", "100, 25, false, false, false, 100, 0" })
    void calculatesExistingBalanceRulesWithoutChangingInputs(String opening, String amount, boolean credit, boolean debit, boolean hold,
            String closing, String overdraft) {
        try (MockedStatic<MoneyHelper> helper = Mockito.mockStatic(MoneyHelper.class)) {
            helper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(16, RoundingMode.HALF_EVEN));
            MonetaryCurrency currency = new MonetaryCurrency("USD", 2, 0);
            Money openingBalance = Money.of(currency, new BigDecimal(opening));
            Money movement = Money.of(currency, new BigDecimal(amount));
            SavingsAccountRunningBalance result = SavingsAccountRunningBalance.calculate(openingBalance, movement, credit, debit, hold);
            assertThat(result.runningBalance().getAmount()).isEqualByComparingTo(closing);
            assertThat(result.overdraftAmount().getAmount()).isEqualByComparingTo(overdraft);
            assertThat(openingBalance.getAmount()).isEqualByComparingTo(opening);
            assertThat(movement.getAmount()).isEqualByComparingTo(amount);
            SavingsAccountRunningBalance repeated = SavingsAccountRunningBalance.calculate(openingBalance, movement, credit, debit, hold);
            assertThat(repeated.runningBalance().getAmount()).isEqualByComparingTo(result.runningBalance().getAmount());
            assertThat(repeated.overdraftAmount().getAmount()).isEqualByComparingTo(result.overdraftAmount().getAmount());
        }
    }
}
