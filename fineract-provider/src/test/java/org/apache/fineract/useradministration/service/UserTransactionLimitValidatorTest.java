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
package org.apache.fineract.useradministration.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.TransactionLimitType;
import org.apache.fineract.useradministration.domain.UserTransactionLimit;
import org.apache.fineract.useradministration.domain.UserTransactionLimitRepository;
import org.apache.fineract.useradministration.exception.TransactionLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserTransactionLimitValidatorTest {

    @Mock
    private UserTransactionLimitRepository userTransactionLimitRepository;

    @Mock
    private AppUser appUser;

    @InjectMocks
    private UserTransactionLimitValidator validator;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // leniency or setup if needed
    }

    @Test
    void shouldThrowIllegalArgumentWhenUserIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(null, TransactionLimitType.DEPOSIT, BigDecimal.TEN));
        assertEquals("User cannot be null for transaction limit validation", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenAmountIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(appUser, TransactionLimitType.DEPOSIT, null));
        assertEquals("Transaction amount cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenAmountIsNegative() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(appUser, TransactionLimitType.DEPOSIT, new BigDecimal("-1")));
        assertEquals("Transaction amount cannot be negative", exception.getMessage());
    }

    @Test
    void shouldThrowWhenNoSpecificAndNoDefaultLimitConfigured() {
        when(appUser.getId()).thenReturn(USER_ID);
        when(userTransactionLimitRepository.findByAppUserIdAndLimitType(USER_ID, TransactionLimitType.DEPOSIT))
                .thenReturn(Optional.empty());
        when(userTransactionLimitRepository.findByAppUserIdAndLimitType(USER_ID, TransactionLimitType.DEFAULT))
                .thenReturn(Optional.empty());

        TransactionLimitExceededException exception = assertThrows(TransactionLimitExceededException.class,
                () -> validator.validate(appUser, TransactionLimitType.DEPOSIT, new BigDecimal("50000")));
        assertEquals("Operator has no configured monetary authority for DEPOSIT", exception.getMessage());
    }

    @Test
    void shouldPassWhenAmountIsWithinSpecificLimitInclusive() {
        when(appUser.getId()).thenReturn(USER_ID);
        UserTransactionLimit limit = mock(UserTransactionLimit.class);
        when(limit.getMinAmount()).thenReturn(new BigDecimal("1000.00"));
        when(limit.getMaxAmount()).thenReturn(new BigDecimal("5000000.00"));
        when(userTransactionLimitRepository.findByAppUserIdAndLimitType(USER_ID, TransactionLimitType.DEPOSIT))
                .thenReturn(Optional.of(limit));

        // Exactly at minimum bound
        assertDoesNotThrow(() -> validator.validate(appUser, TransactionLimitType.DEPOSIT, new BigDecimal("1000.00")));
        // Within bounds
        assertDoesNotThrow(() -> validator.validate(appUser, TransactionLimitType.DEPOSIT, new BigDecimal("500000.00")));
        // Exactly at maximum bound
        assertDoesNotThrow(() -> validator.validate(appUser, TransactionLimitType.DEPOSIT, new BigDecimal("5000000.00")));
    }

    @Test
    void shouldThrowWhenAmountExceedsSpecificMaxLimit() {
        when(appUser.getId()).thenReturn(USER_ID);
        UserTransactionLimit limit = mock(UserTransactionLimit.class);
        when(limit.getMinAmount()).thenReturn(new BigDecimal("1000.00"));
        when(limit.getMaxAmount()).thenReturn(new BigDecimal("5000000.00"));
        when(userTransactionLimitRepository.findByAppUserIdAndLimitType(USER_ID, TransactionLimitType.DEPOSIT))
                .thenReturn(Optional.of(limit));

        assertThrows(TransactionLimitExceededException.class,
                () -> validator.validate(appUser, TransactionLimitType.DEPOSIT, new BigDecimal("5000000.01")));
    }

    @Test
    void shouldThrowWhenAmountIsBelowSpecificMinLimit() {
        when(appUser.getId()).thenReturn(USER_ID);
        UserTransactionLimit limit = mock(UserTransactionLimit.class);
        when(limit.getMinAmount()).thenReturn(new BigDecimal("1000.00"));
        when(limit.getMaxAmount()).thenReturn(new BigDecimal("5000000.00"));
        when(userTransactionLimitRepository.findByAppUserIdAndLimitType(USER_ID, TransactionLimitType.DEPOSIT))
                .thenReturn(Optional.of(limit));

        assertThrows(TransactionLimitExceededException.class,
                () -> validator.validate(appUser, TransactionLimitType.DEPOSIT, new BigDecimal("999.99")));
    }

    @Test
    void shouldFallbackToDefaultLimitWhenSpecificLimitMissing() {
        when(appUser.getId()).thenReturn(USER_ID);
        when(userTransactionLimitRepository.findByAppUserIdAndLimitType(USER_ID, TransactionLimitType.JVS))
                .thenReturn(Optional.empty());

        UserTransactionLimit defaultLimit = mock(UserTransactionLimit.class);
        when(defaultLimit.getMinAmount()).thenReturn(new BigDecimal("100.00"));
        when(defaultLimit.getMaxAmount()).thenReturn(new BigDecimal("1000000.00"));
        when(userTransactionLimitRepository.findByAppUserIdAndLimitType(USER_ID, TransactionLimitType.DEFAULT))
                .thenReturn(Optional.of(defaultLimit));

        // Should pass using DEFAULT limit
        assertDoesNotThrow(() -> validator.validate(appUser, TransactionLimitType.JVS, new BigDecimal("250000.00")));

        // Should fail using DEFAULT limit when exceeding
        assertThrows(TransactionLimitExceededException.class,
                () -> validator.validate(appUser, TransactionLimitType.JVS, new BigDecimal("2000000.00")));
    }

    @Test
    void shouldExemptUserWithAllFunctionsPermission() {
        when(appUser.hasSpecificPermissionTo("ALL_FUNCTIONS")).thenReturn(true);
        assertDoesNotThrow(() -> validator.validate(appUser, TransactionLimitType.DEPOSIT, new BigDecimal("999999999999")));
    }
}
