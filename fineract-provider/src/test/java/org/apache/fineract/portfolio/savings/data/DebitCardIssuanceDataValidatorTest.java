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
class DebitCardIssuanceDataValidatorTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();

    @Mock
    private SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;

    private DebitCardIssuanceDataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DebitCardIssuanceDataValidator(fromJsonHelper, savingsAccountRepositoryWrapper);
    }

    @Test
    void testValidateForRequest_Success() {
        SavingsAccount account = mock(SavingsAccount.class);
        when(account.isNotActive()).thenReturn(false);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(account);

        String json = "{\n"
                + "  \"savingsAccountId\": 1,\n"
                + "  \"cardPanMasked\": \"5061********1234\"\n"
                + "}";

        assertThatNoException().isThrownBy(() -> validator.validateForRequest(json));
    }

    @Test
    void testValidateForRequest_InactiveSavingsAccount_ThrowsException() {
        SavingsAccount account = mock(SavingsAccount.class);
        when(account.isNotActive()).thenReturn(true);
        when(savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(1L)).thenReturn(account);

        String json = "{\n"
                + "  \"savingsAccountId\": 1,\n"
                + "  \"cardPanMasked\": \"5061********1234\"\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForRequest(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void testValidateForRequest_MissingSavingsAccountId_ThrowsException() {
        String json = "{\n"
                + "  \"cardPanMasked\": \"5061********1234\"\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForRequest(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void testValidateForRequest_MissingCardPanMasked_ThrowsException() {
        String json = "{\n"
                + "  \"savingsAccountId\": 1\n"
                + "}";

        assertThatThrownBy(() -> validator.validateForRequest(json))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }

    @Test
    void testValidateForAction_RejectMissingReason_ThrowsException() {
        String json = "{}";
        assertThatThrownBy(() -> validator.validateForAction(json, "reject"))
                .isInstanceOf(PlatformApiDataValidationException.class);
    }
}
