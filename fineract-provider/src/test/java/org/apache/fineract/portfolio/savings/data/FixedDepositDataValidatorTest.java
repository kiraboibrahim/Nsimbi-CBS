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
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
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
class FixedDepositDataValidatorTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();

    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;

    @Mock
    private SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;

    private FixedDepositDataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FixedDepositDataValidator(fromJsonHelper, clientRepositoryWrapper, savingsAccountRepositoryWrapper);
    }

    @Test
    void testValidateForCreate_TillCash_Success() {
        Client client = mock(Client.class);
        when(client.isNotActive()).thenReturn(false);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(client);

        SavingsAccount payoutAccount = mock(SavingsAccount.class);
        when(payoutAccount.isNotActive()).thenReturn(false);
        when(payoutAccount.clientId()).thenReturn(1L);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(2L)).thenReturn(payoutAccount);

        String json = "{\n"
                + "  \"clientId\": 1,\n"
                + "  \"principalAmount\": 500000,\n"
                + "  \"periodMonths\": 12,\n"
                + "  \"interestRateAnnual\": 12.5,\n"
                + "  \"startsOn\": \"2026-10-01\",\n"
                + "  \"interestInterval\": \"MONTHLY\",\n"
                + "  \"payoutOption\": \"PRINCIPAL_AND_INTEREST\",\n"
                + "  \"payoutSavingsAccountId\": 2,\n"
                + "  \"fundingSource\": \"TILL_CASH\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        assertThatNoException().isThrownBy(() -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_SavingsOffset_Success() {
        Client client = mock(Client.class);
        when(client.isNotActive()).thenReturn(false);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(client);

        SavingsAccount account = mock(SavingsAccount.class);
        when(account.isNotActive()).thenReturn(false);
        when(account.clientId()).thenReturn(1L);
        when(account.getAccountBalance()).thenReturn(new BigDecimal("1000000"));
        when(account.getMinRequiredBalance()).thenReturn(BigDecimal.valueOf(5000));
        when(account.getSavingsHoldAmount()).thenReturn(BigDecimal.ZERO);

        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(2L)).thenReturn(account);

        String json = "{\n"
                + "  \"clientId\": 1,\n"
                + "  \"fundingSavingsAccountId\": 2,\n"
                + "  \"principalAmount\": 500000,\n"
                + "  \"periodMonths\": 6,\n"
                + "  \"interestRateAnnual\": 10.0,\n"
                + "  \"startsOn\": \"2026-10-01\",\n"
                + "  \"interestInterval\": \"MONTHLY\",\n"
                + "  \"payoutOption\": \"PRINCIPAL_AND_INTEREST\",\n"
                + "  \"payoutSavingsAccountId\": 2,\n"
                + "  \"fundingSource\": \"SAVINGS_OFFSET\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        assertThatNoException().isThrownBy(() -> validator.validateForCreate(json));
    }

    @Test
    void testValidateForCreate_SavingsOffset_InsufficientBalance_ThrowsException() {
        Client client = mock(Client.class);
        when(client.isNotActive()).thenReturn(false);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(client);

        // Balance: 500,000 UGX
        // Hold: 10,000 UGX
        // Reserve: 5,000 UGX
        // Available: 500,000 - 5,000 - 10,000 = 485,000 UGX
        // Principal requested: 500,000 UGX -> fails with insufficient.available.balance
        SavingsAccount account = mock(SavingsAccount.class);
        when(account.isNotActive()).thenReturn(false);
        when(account.clientId()).thenReturn(1L);
        when(account.getAccountBalance()).thenReturn(new BigDecimal("500000"));
        when(account.getMinRequiredBalance()).thenReturn(BigDecimal.valueOf(5000));
        when(account.getSavingsHoldAmount()).thenReturn(new BigDecimal("10000"));

        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(2L)).thenReturn(account);

        String json = "{\n"
                + "  \"clientId\": 1,\n"
                + "  \"fundingSavingsAccountId\": 2,\n"
                + "  \"principalAmount\": 500000,\n"
                + "  \"periodMonths\": 6,\n"
                + "  \"interestRateAnnual\": 10.0,\n"
                + "  \"startsOn\": \"2026-10-01\",\n"
                + "  \"interestInterval\": \"MONTHLY\",\n"
                + "  \"payoutOption\": \"PRINCIPAL_AND_INTEREST\",\n"
                + "  \"payoutSavingsAccountId\": 2,\n"
                + "  \"fundingSource\": \"SAVINGS_OFFSET\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForCreate(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void testValidateForCreate_SavingsOffset_InactiveSourceAccount_ThrowsException() {
        Client client = mock(Client.class);
        when(client.isNotActive()).thenReturn(false);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(client);

        SavingsAccount account = mock(SavingsAccount.class);
        when(account.isNotActive()).thenReturn(true);
        when(account.clientId()).thenReturn(1L);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(2L)).thenReturn(account);

        String json = "{\n"
                + "  \"clientId\": 1,\n"
                + "  \"fundingSavingsAccountId\": 2,\n"
                + "  \"principalAmount\": 100000,\n"
                + "  \"periodMonths\": 6,\n"
                + "  \"interestRateAnnual\": 10.0,\n"
                + "  \"startsOn\": \"2026-10-01\",\n"
                + "  \"interestInterval\": \"MONTHLY\",\n"
                + "  \"payoutOption\": \"PRINCIPAL_AND_INTEREST\",\n"
                + "  \"payoutSavingsAccountId\": 2,\n"
                + "  \"fundingSource\": \"SAVINGS_OFFSET\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForCreate(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void testValidateForCreate_MissingMandatoryFields_ThrowsException() {
        String json = "{\n"
                + "  \"clientId\": 1\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForCreate(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void testValidateForCreate_InvalidPeriodMonths_ThrowsException() {
        String json = "{\n"
                + "  \"clientId\": 1,\n"
                + "  \"principalAmount\": 500000,\n"
                + "  \"periodMonths\": 0,\n"
                + "  \"interestRateAnnual\": 12.5,\n"
                + "  \"startsOn\": \"2026-10-01\",\n"
                + "  \"interestInterval\": \"MONTHLY\",\n"
                + "  \"payoutOption\": \"PRINCIPAL_AND_INTEREST\",\n"
                + "  \"payoutSavingsAccountId\": 2,\n"
                + "  \"fundingSource\": \"TILL_CASH\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForCreate(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void testValidateForCreate_InvalidInterestRate_ThrowsException() {
        String json = "{\n"
                + "  \"clientId\": 1,\n"
                + "  \"principalAmount\": 500000,\n"
                + "  \"periodMonths\": 12,\n"
                + "  \"interestRateAnnual\": -5.0,\n"
                + "  \"startsOn\": \"2026-10-01\",\n"
                + "  \"interestInterval\": \"MONTHLY\",\n"
                + "  \"payoutOption\": \"PRINCIPAL_AND_INTEREST\",\n"
                + "  \"payoutSavingsAccountId\": 2,\n"
                + "  \"fundingSource\": \"TILL_CASH\",\n"
                + "  \"locale\": \"en\",\n"
                + "  \"dateFormat\": \"yyyy-MM-dd\"\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForCreate(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }
}
