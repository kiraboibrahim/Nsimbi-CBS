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
package org.apache.fineract.infrastructure.security.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.exception.OperatingHoursRestrictionException;
import org.apache.fineract.useradministration.exception.UserSuspendedException;
import org.apache.fineract.useradministration.service.RoleOperatingHoursValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class PlatformUserDetailsCheckerTest {

    @Mock
    private RoleOperatingHoursValidator roleOperatingHoursValidator;

    @InjectMocks
    private PlatformUserDetailsChecker checker;

    private AppUser appUser;

    @BeforeEach
    void setUp() {
        appUser = mock(AppUser.class);
    }

    @Test
    void shouldThrowCredentialsExpiredWhenCredentialsExpired() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.isCredentialsNonExpired()).thenReturn(false);

        assertThrows(CredentialsExpiredException.class, () -> checker.check(userDetails));
    }

    @Test
    void shouldThrowUserSuspendedExceptionWhenAppUserIsSuspended() {
        when(appUser.isCredentialsNonExpired()).thenReturn(true);
        when(appUser.isSuspended()).thenReturn(true);

        assertThrows(UserSuspendedException.class, () -> checker.check(appUser));
    }

    @Test
    void shouldDelegateOperatingHoursValidationWhenAppUserIsNotSuspended() {
        when(appUser.isCredentialsNonExpired()).thenReturn(true);
        when(appUser.isSuspended()).thenReturn(false);

        assertDoesNotThrow(() -> checker.check(appUser));
        verify(roleOperatingHoursValidator).validateUserOperatingHours(appUser);
    }

    @Test
    void shouldPropagateOperatingHoursRestrictionException() {
        when(appUser.isCredentialsNonExpired()).thenReturn(true);
        when(appUser.isSuspended()).thenReturn(false);
        doThrow(new OperatingHoursRestrictionException("Access restricted"))
                .when(roleOperatingHoursValidator).validateUserOperatingHours(appUser);

        assertThrows(OperatingHoursRestrictionException.class, () -> checker.check(appUser));
    }
}
