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
package org.apache.fineract.portfolio.savings.data;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StandingOrderDataValidatorTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();

    @Mock
    private SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;

    private StandingOrderDataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StandingOrderDataValidator(fromJsonHelper, savingsAccountRepositoryWrapper);
    }

    @Test
    void testValidateForCreate_Success() {
        SavingsAccount sourceAccount = mock(SavingsAccount.class);
        when(sourceAccount.isNotActive()).thenReturn(false);
        when(sourceAccount.getAccountBalance()).thenReturn(new BigDecimal("500000"));
        when(sourceAccount.getMinRequiredBalance()).thenReturn(BigDecimal.valueOf(5000));
        when(sourceAccount.getSavingsHoldAmount()).thenReturn(BigDecimal.ZERO);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(sourceAccount);

        SavingsAccount destAccount = mock(SavingsAccount.class);
        when(destAccount.isNotActive()).thenReturn(false);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(2L)).thenReturn(destAccount);

        String json = "{\n"
                + "  \"name\": \"Monthly Rent Transfer\",\n"
                + "  \"sourceAccountId\": 1,\n"
                + "  \"destinationAccountId\": 2,\n"
                + "  \"amount\": 200000,\n"
                + "  \"frequency\": \"MONTHLY\",\n"
                + "  \"startDate\": \"2026-10-05\",\n"
                + "  \"endDate\": \"2027-10-05\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        assertThatNoException().isThrownBy(() -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_SameSourceAndDestination_ThrowsException() {
        String json = "{\n"
                + "  \"name\": \"Invalid Same Account\",\n"
                + "  \"sourceAccountId\": 1,\n"
                + "  \"destinationAccountId\": 1,\n"
                + "  \"amount\": 50000,\n"
                + "  \"frequency\": \"MONTHLY\",\n"
                + "  \"startDate\": \"2026-10-05\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForCreate(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void testValidateForCreate_InsufficientBalance_ThrowsException() {
        // Balance: 100,000 UGX, Hold: 20,000 UGX, Reserve: 5,000 UGX -> Available: 75,000 UGX
        // Requested amount: 80,000 UGX -> fails
        SavingsAccount sourceAccount = mock(SavingsAccount.class);
        when(sourceAccount.isNotActive()).thenReturn(false);
        when(sourceAccount.getAccountBalance()).thenReturn(new BigDecimal("100000"));
        when(sourceAccount.getMinRequiredBalance()).thenReturn(BigDecimal.valueOf(5000));
        when(sourceAccount.getSavingsHoldAmount()).thenReturn(new BigDecimal("20000"));
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(sourceAccount);

        SavingsAccount destAccount = mock(SavingsAccount.class);
        when(destAccount.isNotActive()).thenReturn(false);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(2L)).thenReturn(destAccount);

        String json = "{\n"
                + "  \"name\": \"Overdraft transfer\",\n"
                + "  \"sourceAccountId\": 1,\n"
                + "  \"destinationAccountId\": 2,\n"
                + "  \"amount\": 80000,\n"
                + "  \"frequency\": \"DAILY\",\n"
                + "  \"startDate\": \"2026-10-05\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForCreate(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void testValidateForCreate_InactiveSource_ThrowsException() {
        SavingsAccount sourceAccount = mock(SavingsAccount.class);
        when(sourceAccount.isNotActive()).thenReturn(true);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(sourceAccount);

        SavingsAccount destAccount = mock(SavingsAccount.class);
        when(destAccount.isNotActive()).thenReturn(false);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(2L)).thenReturn(destAccount);

        String json = "{\n"
                + "  \"name\": \"Inactive source transfer\",\n"
                + "  \"sourceAccountId\": 1,\n"
                + "  \"destinationAccountId\": 2,\n"
                + "  \"amount\": 50000,\n"
                + "  \"frequency\": \"MONTHLY\",\n"
                + "  \"startDate\": \"2026-10-05\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForCreate(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void testValidateForCreate_EndDateBeforeStartDate_ThrowsException() {
        String json = "{\n"
                + "  \"name\": \"Invalid Dates\",\n"
                + "  \"sourceAccountId\": 1,\n"
                + "  \"destinationAccountId\": 2,\n"
                + "  \"amount\": 50000,\n"
                + "  \"frequency\": \"MONTHLY\",\n"
                + "  \"startDate\": \"2026-10-05\",\n"
                + "  \"endDate\": \"2026-09-01\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForCreate(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void testValidateForAction_RejectMissingReason_ThrowsException() {
        String json = "{}";
        assertThatThrownBy(() -> validator.validateForAction(json, "reject"))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }
}
