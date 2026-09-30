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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DepositAccountApplicableInterestRateTest {

    private static final LocalDate START = LocalDate.of(2025, 1, 1);
    private static final LocalDate MATURITY = LocalDate.of(2026, 1, 1);
    private static final BigDecimal STORED_RATE = new BigDecimal("2.5");
    private static final BigDecimal CHART_RATE = new BigDecimal("7.25");

    @Test
    void fixedRateCalculationDoesNotChangeStoredRate() {
        FixedDepositAccount account = fixedAccount();
        assertThat(account.calculateApplicableInterestRate(MATURITY, false)).isEqualByComparingTo(CHART_RATE);
        assertThat(account.calculateApplicableInterestRate(MATURITY, false)).isEqualByComparingTo(CHART_RATE);
        assertThat(account.nominalAnnualInterestRate).isEqualByComparingTo(STORED_RATE);
    }

    @Test
    void fixedPostingRetainsExistingRateUpdateAndFraction() {
        FixedDepositAccount account = fixedAccount();
        assertThat(account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, MATURITY, false)).isEqualByComparingTo("0.0725");
        assertThat(account.nominalAnnualInterestRate).isEqualByComparingTo(CHART_RATE);
    }

    @Test
    void recurringRateCalculationDoesNotChangeStoredRate() {
        RecurringDepositAccount account = recurringAccount();
        assertThat(account.calculateApplicableInterestRate(MATURITY, false)).isEqualByComparingTo(CHART_RATE);
        assertThat(account.calculateApplicableInterestRate(MATURITY, false)).isEqualByComparingTo(CHART_RATE);
        assertThat(account.nominalAnnualInterestRate).isEqualByComparingTo(STORED_RATE);
    }

    @Test
    void recurringPostingRetainsExistingRateUpdateAndFraction() {
        RecurringDepositAccount account = recurringAccount();
        assertThat(account.getEffectiveInterestRateAsFraction(MathContext.DECIMAL64, MATURITY, false)).isEqualByComparingTo("0.0725");
        assertThat(account.nominalAnnualInterestRate).isEqualByComparingTo(CHART_RATE);
    }

    @Test
    void fixedWithoutChartUsesStoredRateWithoutChangingIt() {
        FixedDepositAccount account = new FixedDepositAccount();
        account.nominalAnnualInterestRate = STORED_RATE;
        assertThat(account.calculateApplicableInterestRate(MATURITY, false)).isEqualByComparingTo(STORED_RATE);
        assertThat(account.nominalAnnualInterestRate).isEqualByComparingTo(STORED_RATE);
    }

    private FixedDepositAccount fixedAccount() {
        FixedDepositAccount account = spy(new FixedDepositAccount());
        configure(account);
        doReturn(MATURITY).when(account).calculateMaturityDate();
        return account;
    }

    private RecurringDepositAccount recurringAccount() {
        RecurringDepositAccount account = spy(new RecurringDepositAccount());
        configure(account);
        doReturn(MATURITY).when(account).calculateMaturityDate();
        return account;
    }

    private void configure(SavingsAccount account) {
        account.nominalAnnualInterestRate = STORED_RATE;
        ReflectionTestUtils.setField(account, "submittedOnDate", START);
        DepositAccountTermAndPreClosure term = mock(DepositAccountTermAndPreClosure.class);
        when(term.depositAmount()).thenReturn(new BigDecimal("1000"));
        DepositAccountInterestRateChart chart = mock(DepositAccountInterestRateChart.class);
        when(chart.getApplicableInterestRate(eq(new BigDecimal("1000")), any(), eq(MATURITY), isNull())).thenReturn(CHART_RATE);
        ReflectionTestUtils.setField(account, "accountTermAndPreClosure", term);
        ReflectionTestUtils.setField(account, "chart", chart);
    }
}
